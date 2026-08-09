package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.srs.SrsCardRepository
import ua.syt0r.kanji.core.srs.SrsScheduler
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryRepository
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.BasePracticeQueue
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeQueue
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueItemDescriptor
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarSummaryItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case.GetGrammarPracticeFlashcardDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case.GetGrammarPracticeClozeDataUseCase
import ua.syt0r.kanji.core.srs.GrammarPracticeType

typealias GrammarPracticeQueue = PracticeQueue<GrammarPracticeQueueState, GrammarPracticeQueueItemDescriptor>

private typealias BaseGrammarPracticeQueue =
        BasePracticeQueue<GrammarPracticeQueueState, GrammarPracticeQueueItemDescriptor, GrammarPracticeQueueItem, GrammarSummaryItem>

class DefaultGrammarPracticeQueue(
    private val coroutineScope: CoroutineScope,
    timeUtils: TimeUtils,
    srsCardRepository: SrsCardRepository,
    srsScheduler: SrsScheduler,
    private val getFlashcardReviewStateUseCase: GetGrammarPracticeFlashcardDataUseCase,
    private val getClozeReviewStateUseCase: GetGrammarPracticeClozeDataUseCase,
    reviewHistoryRepository: ReviewHistoryRepository,
    analyticsManager: AnalyticsManager
) : BaseGrammarPracticeQueue(
    practiceScope = coroutineScope,
    timeUtils = timeUtils,
    srsCardRepository = srsCardRepository,
    reviewHistoryRepository = reviewHistoryRepository,
    srsScheduler = srsScheduler,
    analyticsManager = analyticsManager
), GrammarPracticeQueue {

    override suspend fun GrammarPracticeQueueItemDescriptor.toQueueItem(): GrammarPracticeQueueItem {
        val srsCardKey = when(this) {
            is GrammarPracticeQueueItemDescriptor.Flashcard -> GrammarPracticeType.Flashcard.toSrsKey(pointNumber)
            is GrammarPracticeQueueItemDescriptor.Cloze -> GrammarPracticeType.Cloze.toSrsKey(pointNumber)
        }
        return GrammarPracticeQueueItem(
            descriptor = this,
            srsCardKey = srsCardKey,
            srsCard = srsCardRepository.get(srsCardKey) ?: srsScheduler.newCard(),
            deckId = deckId,
            repeats = 0,
            totalMistakes = 0,
            data = coroutineScope.async(Dispatchers.IO, start = CoroutineStart.LAZY) {
                when (this@toQueueItem) {
                    is GrammarPracticeQueueItemDescriptor.Flashcard -> {
                        getFlashcardReviewStateUseCase(this@toQueueItem)
                    }
                    is GrammarPracticeQueueItemDescriptor.Cloze -> {
                        getClozeReviewStateUseCase(this@toQueueItem)
                    }
                }
            }
        )
    }

    override fun createSummaryItem(queueItem: GrammarPracticeQueueItem, totalReviews: Deferred<Int>): GrammarSummaryItem {
        return GrammarSummaryItem(
            pointNumber = queueItem.descriptor.pointNumber,
            title = "Grammar " + queueItem.descriptor.pointNumber, // We don't have direct access to title here, use pointNumber
            srsCardKey = queueItem.srsCardKey,
            srsCard = queueItem.srsCard,
            newSrsCard = queueItem.newSrsCard.getCompleted(),
            isDoneInFirstAttempt = queueItem.totalMistakes == 0 && queueItem.repeats == 0
        )
    }

    override fun getLoadingState(): GrammarPracticeQueueState = GrammarPracticeQueueState.Loading

    override suspend fun getReviewState(
        item: GrammarPracticeQueueItem,
        answers: PracticeAnswers
    ): GrammarPracticeQueueState {
        return GrammarPracticeQueueState.Review(
            progress = getProgress(),
            state = item.data.await().toReviewState(coroutineScope),
            answers = answers
        )
    }

    override fun getSummaryState(): GrammarPracticeQueueState {
        return GrammarPracticeQueueState.Summary(
            duration = timeUtils.now() - practiceStartInstant,
            items = summaryItems.values.toList()
        )
    }

}
