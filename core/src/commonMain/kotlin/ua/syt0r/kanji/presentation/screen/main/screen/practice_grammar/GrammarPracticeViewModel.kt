package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.tts.JapaneseSpeechContext
import ua.syt0r.kanji.core.tts.JapaneseSpeechRequest
import ua.syt0r.kanji.presentation.common.BaseViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeReviewSaveFailed
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueItemDescriptor
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueState

class GrammarPracticeViewModel(
    val deckId: Long,
    val items: List<GrammarPracticeQueueItemDescriptor>,
    private val queue: DefaultGrammarPracticeQueue,
    private val appTtsManager: ua.syt0r.kanji.core.tts.AppTtsManager
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        GrammarPracticeScreenContract.State(
            queueState = queue.state.value,
            answeredCorrectly = null
        )
    )
    val state = _state.asStateFlow()
    val reviewErrors: Flow<PracticeReviewSaveFailed> = queue.errors

    init {
        viewModelScope.launch {
            queue.state.collect { queueState ->
                // When queue advances to a new review item, reset answered state
                _state.value = _state.value.copy(
                    queueState = queueState,
                    answeredCorrectly = null
                )
            }
        }
        viewModelScope.launch {
            queue.initialize(items)
        }
    }

    fun setEvent(event: GrammarPracticeScreenContract.Event) {
        when (event) {
            is GrammarPracticeScreenContract.Event.AnswerFlashcard -> handleAnswer(event.isCorrect)
            is GrammarPracticeScreenContract.Event.AnswerCloze -> handleAnswer(event.isCorrect)
            is GrammarPracticeScreenContract.Event.AnswerConjugation -> handleAnswer(event.isCorrect)
            is GrammarPracticeScreenContract.Event.AnswerScramble -> handleAnswer(event.isCorrect)
            is GrammarPracticeScreenContract.Event.AnswerDialogue -> handleAnswer(event.isCorrect)
            is GrammarPracticeScreenContract.Event.AnswerSrs -> {
                viewModelScope.launch {
                    queue.submitAnswer(event.answer)
                }
            }
            is GrammarPracticeScreenContract.Event.ProceedToNext -> {
                val currentQueueState = _state.value.queueState as? GrammarPracticeQueueState.Review ?: return
                val wasCorrect = _state.value.answeredCorrectly ?: return
                val answer: PracticeAnswer = if (wasCorrect) currentQueueState.answers.good else currentQueueState.answers.again
                viewModelScope.launch {
                    queue.submitAnswer(answer)
                }
            }
            GrammarPracticeScreenContract.Event.SkipUnavailable -> {
                viewModelScope.launch {
                    queue.skipCurrent()
                }
            }
            is GrammarPracticeScreenContract.Event.EndPractice -> {
                queue.immediateFinish()
            }
            is GrammarPracticeScreenContract.Event.PlayVoice -> {
                viewModelScope.launch {
                    appTtsManager.speak(
                        JapaneseSpeechRequest(
                            displayText = event.text,
                            context = JapaneseSpeechContext.Grammar
                        )
                    )
                }
            }
        }
    }

    fun retryLastReview() {
        viewModelScope.launch { queue.retryLastFailedAnswer() }
    }

    private fun handleAnswer(isCorrect: Boolean) {
        // Record answer locally; actual SRS submission happens on ProceedToNext
        _state.value = _state.value.copy(answeredCorrectly = isCorrect)
    }

    override fun onCleared() {
        super.onCleared()
    }
}
