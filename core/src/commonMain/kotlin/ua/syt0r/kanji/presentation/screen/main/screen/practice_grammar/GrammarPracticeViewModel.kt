package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ua.syt0r.kanji.presentation.common.BaseViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueItemDescriptor

class GrammarPracticeViewModel(
    val deckId: Long,
    val items: List<GrammarPracticeQueueItemDescriptor>,
    private val queue: GrammarPracticeQueue
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        GrammarPracticeScreenContract.State(
            queueState = queue.stateFlow.value
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            queue.stateFlow.collect { queueState ->
                _state.value = _state.value.copy(queueState = queueState)
            }
        }
        queue.start(items)
    }

    fun setEvent(event: GrammarPracticeScreenContract.Event) {
        when (event) {
            is GrammarPracticeScreenContract.Event.AnswerFlashcard -> {
                queue.provideAnswers(
                    PracticeAnswers(
                        isCorrect = event.isCorrect,
                        mistakes = if (event.isCorrect) 0 else 1,
                        duration = 0 // simplified
                    )
                )
            }
            is GrammarPracticeScreenContract.Event.ProceedToNext -> {
                queue.proceedToNext()
            }
            is GrammarPracticeScreenContract.Event.EndPractice -> {
                queue.endPractice()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        queue.cancel()
    }
}
