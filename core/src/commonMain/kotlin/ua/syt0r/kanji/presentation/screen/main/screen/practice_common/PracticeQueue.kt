package ua.syt0r.kanji.presentation.screen.main.screen.practice_common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.debounceFirst
import ua.syt0r.kanji.core.srs.SrsAnswers
import ua.syt0r.kanji.core.srs.SrsCard
import ua.syt0r.kanji.core.srs.SrsCardKey
import ua.syt0r.kanji.core.srs.SrsCardRepository
import ua.syt0r.kanji.core.srs.SrsScheduler
import ua.syt0r.kanji.core.srs.SrsMicroMlEngine
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.database.ReviewCommitRepository
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryItem
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryRepository
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days


data object PracticeReviewSaveFailed

interface PracticeQueue<State, Descriptor> {

    val state: StateFlow<State>
    val errors: Flow<PracticeReviewSaveFailed>

    suspend fun initialize(items: List<Descriptor>)
    suspend fun submitAnswer(answer: PracticeAnswer)
    suspend fun retryLastFailedAnswer()
    suspend fun skipCurrent()
    fun immediateFinish()

}

interface PracticeQueueItem<T : PracticeQueueItem<T>> {

    val srsCardKey: SrsCardKey
    val srsCard: SrsCard
    val deckId: Long
    val repeats: Int
    val totalMistakes: Int
    val data: Deferred<Any>

    fun copyForRepeat(answer: PracticeAnswer): T

}

data class PracticeQueueProgress(
    val pending: Int,
    val repeats: Int,
    val completed: Int
)

interface PracticeSummaryItem {
    val totalReviews: Deferred<Int>
    val nextInterval: Duration
}

abstract class BasePracticeQueue<State, Descriptor, QueueItem, SummaryItem>(
    private val practiceScope: CoroutineScope,
    protected val timeUtils: TimeUtils,
    protected val srsScheduler: SrsScheduler,
    protected val srsCardRepository: SrsCardRepository,
    protected val srsMicroMlEngine: SrsMicroMlEngine,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    private val reviewCommitRepository: ReviewCommitRepository,
    analyticsManager: AnalyticsManager
) : PracticeQueue<State, Descriptor>
        where QueueItem : PracticeQueueItem<QueueItem>,
              SummaryItem : PracticeSummaryItem {

    protected open lateinit var queue: MutableList<QueueItem>
    protected val summaryItems = mutableMapOf<SrsCardKey, SummaryItem>()

    protected lateinit var practiceStartInstant: Instant
    private lateinit var currentReviewStartInstant: Instant

    private val submittedAnswersChannel = Channel<PracticeAnswer>(Channel.BUFFERED)
    private val reviewErrorsChannel = Channel<PracticeReviewSaveFailed>(Channel.BUFFERED)
    override val errors: Flow<PracticeReviewSaveFailed> = reviewErrorsChannel.receiveAsFlow()
    private val answerHandlingMutex = Mutex()
    private var lastFailedAnswer: PracticeAnswer? = null

    private val _state: MutableStateFlow<State> = MutableStateFlow(value = this.getLoadingState())
    override val state: StateFlow<State> = _state

    private val reviewReporter = PracticeReviewReporter(analyticsManager)

    init {
        submittedAnswersChannel.consumeAsFlow()
            .debounceFirst()
            .onEach { handleAnswer(it) }
            .launchIn(practiceScope)
    }

    protected abstract suspend fun Descriptor.toQueueItem(): QueueItem
    protected abstract fun createSummaryItem(
        queueItem: QueueItem,
        totalReviews: Deferred<Int>
    ): SummaryItem

    protected abstract fun getLoadingState(): State
    protected abstract suspend fun getReviewState(item: QueueItem, answers: PracticeAnswers): State
    protected abstract fun getSummaryState(): State

    override suspend fun initialize(items: List<Descriptor>) {
        practiceStartInstant = timeUtils.now()
        queue = items.map { it.toQueueItem() }.toMutableList()
        updateState()
    }

    override suspend fun submitAnswer(answer: PracticeAnswer) {
        submittedAnswersChannel.send(answer)
    }

    override suspend fun retryLastFailedAnswer() {
        answerHandlingMutex.withLock {
            lastFailedAnswer?.let { handleAnswerLocked(it) }
        }
    }

    override suspend fun skipCurrent() {
        if (queue.isEmpty()) return
        queue.removeFirst()
        updateState()
    }

    override fun immediateFinish() {
        val isLoading = summaryItems.any { it.value.totalReviews.isCompleted.not() }
        if (isLoading) {
            _state.value = getLoadingState()
            practiceScope.launch {
                summaryItems.forEach { it.value.totalReviews.await() }
                _state.value = getSummaryState()
            }
        } else _state.value = getSummaryState()
    }

    protected fun getProgress(): PracticeQueueProgress {
        return PracticeQueueProgress(
            pending = queue.count { it.repeats == 0 },
            repeats = queue.count { it.repeats > 0 },
            completed = summaryItems.filter { it.value.nextInterval >= 1.days }.size
        )
    }

    private fun getAnswers(answers: SrsAnswers): PracticeAnswers {
        return PracticeAnswers(
            again = PracticeAnswer(answers.again),
            hard = PracticeAnswer(answers.hard),
            good = PracticeAnswer(answers.good),
            easy = PracticeAnswer(answers.easy)
        )
    }

    private suspend fun handleAnswer(answer: PracticeAnswer) {
        answerHandlingMutex.withLock { handleAnswerLocked(answer) }
    }

    private suspend fun handleAnswerLocked(answer: PracticeAnswer) {
        val item = queue.removeFirstOrNull() ?: return
        val updatedItem = item.copyForRepeat(answer)
        val instant = timeUtils.now()
        val reviewDuration = instant - currentReviewStartInstant
        val review = createReviewHistory(item, answer, instant, reviewDuration)

        try {
            reviewCommitRepository.commitReview(
                key = item.srsCardKey,
                card = answer.srsAnswer.card.fsrsCard,
                review = review,
                algorithmVersion = srsScheduler.algorithmVersion,
                parameterSetId = srsScheduler.parameterSetId,
            )
        } catch (cancellation: CancellationException) {
            queue.add(0, item)
            throw cancellation
        } catch (error: Exception) {
            queue.add(0, item)
            lastFailedAnswer = answer
            Logger.w("Review commit failed: ${error.message}")
            reviewErrorsChannel.send(PracticeReviewSaveFailed)
            updateState()
            return
        }

        lastFailedAnswer = null
        saveSummaryData(updatedItem)
        if (answer.srsAnswer.card.interval < 1.days) {
            placeItemBackToQueue(updatedItem)
        }

        updateState()
        srsMicroMlEngine.observe(item.srsCardKey, review)
        reviewReporter.reportReview(updatedItem, answer, reviewDuration)
    }

    private suspend fun updateState() {
        val item = queue.getOrNull(0)
        if (item == null) {
            immediateFinish()
        } else {
            if (!item.data.isCompleted) {
                _state.value = getLoadingState()
            }
            val time = timeUtils.now()
            val srsAnswers = srsMicroMlEngine.schedule(
                key = item.srsCardKey,
                card = item.srsCard,
                scheduler = srsScheduler,
                reviewTime = time
            )

            item.data.await()
            currentReviewStartInstant = timeUtils.now()

            _state.value = getReviewState(item, getAnswers(srsAnswers))

            queue.getOrNull(1)?.apply {
                data.start()
            }
        }
    }

    private fun placeItemBackToQueue(
        updatedQueueItem: QueueItem
    ) {
        val nextReviewTime = getExpectedReviewTime(updatedQueueItem.srsCard)
        val insertPosition = queue.asSequence()
            .map { getExpectedReviewTime(it.srsCard) }
            .indexOfFirst { nextReviewTime < it }
            .takeIf { it != -1 }
            ?.let {
                if (it == 0 && queue.size > 0) min(MIN_QUEUE_POSITION_SHIFT - 1, queue.size)
                else min(it, MAX_QUEUE_POSITION_SHIFT - 1)
            }
            ?: min(queue.size, MAX_QUEUE_POSITION_SHIFT - 1)

        queue.add(insertPosition, updatedQueueItem)
    }

    private fun getExpectedReviewTime(srsItem: SrsCard): Instant {
        return (srsItem.lastReview ?: Instant.DISTANT_PAST) + srsItem.interval
    }

    private fun saveSummaryData(queueItem: QueueItem) {
        val summaryItem = createSummaryItem(
            queueItem = queueItem,
            totalReviews = practiceScope.async {
                queueItem.srsCardKey.run {
                    reviewHistoryRepository.getTotalReviewCount(itemKey, practiceType)
                        .toInt()
                        .plus(1) // To count current review before its saved
                }
            }
        )
        summaryItems[queueItem.srsCardKey] = summaryItem
    }

    private fun createReviewHistory(
        queueItem: QueueItem,
        answer: PracticeAnswer,
        reviewStart: Instant,
        reviewDuration: Duration
    ): ReviewHistoryItem {
        val item = ReviewHistoryItem(
            key = queueItem.srsCardKey.itemKey,
            practiceType = queueItem.srsCardKey.practiceType,
            timestamp = reviewStart,
            duration = reviewDuration,
            grade = answer.srsAnswer.grade,
            mistakes = answer.mistakes,
            deckId = queueItem.deckId
        )
        return item
    }


    companion object {
        private const val MIN_QUEUE_POSITION_SHIFT = 3
        private const val MAX_QUEUE_POSITION_SHIFT = 10
    }

}

private class PracticeReviewReporter<T : PracticeQueueItem<*>>(
    private val analyticsManager: AnalyticsManager
) {

    fun reportReview(
        item: T,
        answer: PracticeAnswer,
        reviewDuration: Duration
    ) {
        analyticsManager.sendEvent("review") {
            put("key", item.srsCardKey.itemKey)
            put("practice_type", item.srsCardKey.practiceType)
            put("duration", reviewDuration.inWholeMilliseconds)
            put("mistakes", answer.mistakes)
            put("repeats", item.srsCard.fsrsCard.repeats)
            put("lapses", item.srsCard.fsrsCard.lapses)
        }
    }

}
