package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data

import kotlinx.serialization.Serializable

@Serializable
data class GrammarPracticeScreenConfiguration(
    val deckId: Long,
    val items: List<Item>
) {
    @Serializable
    sealed interface Item {
        @Serializable
        data class Flashcard(
            val pointNumber: String,
            val showMeaningInFront: Boolean
        ) : Item

        @Serializable
        data class Cloze(
            val pointNumber: String
        ) : Item
    }
}
