package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar

import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueState

interface GrammarPracticeScreenContract {
    data class State(
        val queueState: GrammarPracticeQueueState
    )

    sealed interface Event {
        data class AnswerFlashcard(val isCorrect: Boolean) : Event
        data class ProceedToNext(val answers: PracticeAnswers) : Event
        data object EndPractice : Event
    }
}
