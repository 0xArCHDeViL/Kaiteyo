package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.core.tts.AppTtsManager
import ua.syt0r.kanji.core.tts.JapaneseSpeechContext
import ua.syt0r.kanji.core.tts.JapaneseSpeechRequest
import ua.syt0r.kanji.core.tts.KanaTtsManager
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWritingProgress
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.LetterPracticeScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeQueueState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeSummaryItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.GetLetterPracticeConfigurationUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.GetLetterPracticeQueueDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.GetLetterPracticeReviewStateUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.UpdateLetterPracticeConfigurationUseCase


class LetterPracticeViewModel(
    private val viewModelScope: CoroutineScope,
    private val getConfigurationUseCase: GetLetterPracticeConfigurationUseCase,
    private val updateConfigurationUseCase: UpdateLetterPracticeConfigurationUseCase,
    private val getQueueDataUseCase: GetLetterPracticeQueueDataUseCase,
    private val practiceQueue: DefaultLetterPracticeQueue,
    private val getReviewStateUseCase: GetLetterPracticeReviewStateUseCase,
    private val analyticsManager: AnalyticsManager,
    private val kanaTtsManager: KanaTtsManager,
    private val appTtsManager: AppTtsManager
) : LetterPracticeScreenContract.ViewModel {

    private lateinit var configuration: LetterPracticeScreenConfiguration

    private val _state = mutableStateOf<ScreenState>(ScreenState.Loading)
    override val state: State<ScreenState> = _state
    override val reviewErrors = practiceQueue.errors

    override fun initialize(configuration: LetterPracticeScreenConfiguration) {
        if (this::configuration.isInitialized) return
        this.configuration = configuration

        viewModelScope.launch {
            _state.value = ScreenState.Configuring(
                configuration = getConfigurationUseCase(configuration)
            )
        }
    }

    override fun configure() {
        val configurationState = _state.value as? ScreenState.Configuring ?: return
        _state.value = ScreenState.Loading

        viewModelScope.launch {

            updateConfigurationUseCase(configurationState.configuration)

            practiceQueue.initialize(
                items = getQueueDataUseCase(configurationState.configuration)
            )

            practiceQueue.state
                .onEach {
                    when (it) {
                        LetterPracticeQueueState.Loading -> {
                            _state.value = ScreenState.Loading
                        }

                        is LetterPracticeQueueState.Review -> {
                            val reviewState = it.toScreenState()
                            _state.value = reviewState

                            reviewState.autoReadFlow()
                                .onEach { text ->
                                    if (text is AutoReadText.Kana) speakKana(text.reading)
                                    else if (text is AutoReadText.KanjiReading) {
                                        speakYomikata(text.request)
                                    }
                                }
                                .launchIn(viewModelScope)
                        }

                        is LetterPracticeQueueState.Summary -> {
                            _state.value = it.toScreenState()
                        }
                    }
                }
                .launchIn(this)
        }

        reportConfiguration(configurationState)
    }

    override fun submitAnswer(answer: PracticeAnswer) {
        viewModelScope.launch { practiceQueue.submitAnswer(answer) }
    }

    override fun retryLastReview() {
        viewModelScope.launch { practiceQueue.retryLastFailedAnswer() }
    }

    override fun speakKana(reading: KanaReading) {
        viewModelScope.launch { kanaTtsManager.speak(reading) }
    }

    private fun speakYomikata(request: JapaneseSpeechRequest) {
        viewModelScope.launch { appTtsManager.speak(request) }
    }

    override fun finishPractice() {
        practiceQueue.immediateFinish()
    }

    private fun reportConfiguration(state: ScreenState.Configuring) {
        analyticsManager.sendEvent("letter_practice_configuration") {
            put("practice_type", configuration.practiceType.dataType.srsPracticeType.value)
            put("list_size", state.configuration.selectorState.selectedCountIntState.value)
        }
    }

    private fun LetterPracticeQueueState.Review.toScreenState(): ScreenState.Review {
        return ScreenState.Review(
            practiceProgress = progress,
            reviewState = getReviewStateUseCase(this)
        )
    }

    private fun LetterPracticeQueueState.Summary.toScreenState(): ScreenState.Summary {
        val accuracy: Float? = items.filterIsInstance<LetterPracticeSummaryItem.Writing>()
            .takeIf { it.isNotEmpty() }
            ?.let {
                val totalStrokeCount = it.fold(0) { sum, item -> sum + item.strokeCount }
                val totalMistakeCount = it.fold(0) { sum, item -> sum + item.mistakes }
                val correctStrokes = (totalStrokeCount - totalMistakeCount)
                    .coerceAtLeast(0)
                correctStrokes.toFloat() * 100 / totalStrokeCount
            }
        return ScreenState.Summary(
            duration = duration,
            accuracy = accuracy,
            items = items
        )
    }

    private sealed interface AutoReadText {
        data class Kana(val reading: KanaReading) : AutoReadText
        data class KanjiReading(
            val request: JapaneseSpeechRequest
        ) : AutoReadText
    }

    private fun ScreenState.Review.autoReadFlow(): Flow<AutoReadText> = callbackFlow {
        when {
            reviewState is LetterPracticeReviewState.Reading &&
                    reviewState.itemData is LetterPracticeItemData.KanaReadingData -> {

                snapshotFlow { reviewState.revealed.value }
                    .filter { it && reviewState.layout.kanaAutoPlay.value }
                    .take(1)
                    .onEach { send(AutoReadText.Kana(reviewState.itemData.reading)) }
                    .collect()

            }

            reviewState is LetterPracticeReviewState.Writing &&
                    reviewState.itemData is LetterPracticeItemData.KanaWritingData -> {

                snapshotFlow { reviewState.writerState.value }
                    .filter { reviewState.layout.kanaAutoPlay.value }
                    .onEach {
                        delay(200)
                        send(AutoReadText.Kana(reviewState.itemData.reading))
                    }
                    .collect()

            }

            reviewState is LetterPracticeReviewState.Writing &&
                    reviewState.itemData is LetterPracticeItemData.KanjiWritingData -> {

                val kanjiReview = reviewState
                val speechRequest = JapaneseSpeechRequest(
                    displayText = kanjiReview.itemData.character,
                    kunReadings = kanjiReview.itemData.kun,
                    onReadings = kanjiReview.itemData.on,
                    context = JapaneseSpeechContext.IsolatedKanji
                )

                snapshotFlow { kanjiReview.writerState.value.progress.value }
                    .dropWhile { it !is CharacterWritingProgress.Writing }
                    .filterIsInstance<CharacterWritingProgress.Completed.Idle>()
                    .take(1)
                    .onEach {
                        send(
                            AutoReadText.KanjiReading(speechRequest)
                        )
                    }
                    .collect()
            }
        }
        awaitClose()
    }


}
