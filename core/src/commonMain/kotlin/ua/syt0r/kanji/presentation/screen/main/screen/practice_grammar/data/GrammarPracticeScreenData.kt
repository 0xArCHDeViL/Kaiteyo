package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data

sealed interface MutableGrammarReviewState {

    data class Flashcard(
        val title: String,
        val formula: String,
        val meaning: String,
        val examples: List<String>,
        val notes: String,
        val showMeaningInFront: Boolean
    ) : MutableGrammarReviewState

}
