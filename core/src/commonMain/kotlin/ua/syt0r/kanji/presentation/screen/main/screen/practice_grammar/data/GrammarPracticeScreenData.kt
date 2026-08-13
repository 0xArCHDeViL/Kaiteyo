package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data

sealed interface MutableGrammarReviewState {

    data class Unavailable(
        val title: String,
        val reason: String
    ) : MutableGrammarReviewState

    data class Flashcard(
        val title: String,
        val formula: String,
        val meaning: String,
        val examples: List<String>,
        val notes: String,
        val showMeaningInFront: Boolean
    ) : MutableGrammarReviewState

    data class Cloze(
        val title: String,
        val formula: String,
        val clozeSentence: String,
        val meaning: String,
        val options: List<String>,
        val correctAnswerIndex: Int
    ) : MutableGrammarReviewState

    data class ConjugationBuilder(
        val title: String,
        val formula: String,
        val verbDictionary: String,
        val verbMeaning: String,
        val targetConjugation: String,
        val syllables: List<String>
    ) : MutableGrammarReviewState

    data class SentenceScramble(
        val title: String,
        val formula: String,
        val meaning: String,
        val originalSentence: String,
        val scrambledParts: List<String>
    ) : MutableGrammarReviewState

    data class SurvivalDialogue(
        val title: String,
        val context: String,
        val dialogueLines: List<Pair<String, String>>,
        val options: List<String>,
        val correctAnswerIndex: Int
    ) : MutableGrammarReviewState

}
