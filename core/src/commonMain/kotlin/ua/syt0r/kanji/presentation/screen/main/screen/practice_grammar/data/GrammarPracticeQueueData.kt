package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import ua.syt0r.kanji.core.srs.SrsCard
import ua.syt0r.kanji.core.srs.SrsCardKey
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeQueueItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeQueueProgress
import kotlin.time.Duration

sealed interface GrammarPracticeQueueState {
    data object Loading : GrammarPracticeQueueState

    data class Review(
        val state: MutableGrammarReviewState,
        val progress: PracticeQueueProgress,
        val answers: PracticeAnswers
    ) : GrammarPracticeQueueState

    data class Summary(
        val duration: Duration,
        val items: List<GrammarSummaryItem>
    ) : GrammarPracticeQueueState
}

data class GrammarPracticeQueueItem(
    val descriptor: GrammarPracticeQueueItemDescriptor,
    override val srsCardKey: SrsCardKey,
    override val srsCard: SrsCard,
    override val deckId: Long,
    override val repeats: Int,
    override val totalMistakes: Int,
    override val data: Deferred<GrammarPracticeItemData>,
) : PracticeQueueItem<GrammarPracticeQueueItem> {

    override fun copyForRepeat(answer: PracticeAnswer): GrammarPracticeQueueItem {
        return copy(
            srsCard = answer.srsAnswer.card,
            repeats = repeats + 1,
            totalMistakes = totalMistakes + answer.mistakes
        )
    }

}

sealed interface GrammarPracticeQueueItemDescriptor {
    val pointNumber: String
    val deckId: Long

    data class Flashcard(
        override val pointNumber: String,
        override val deckId: Long,
        val showMeaningInFront: Boolean
    ) : GrammarPracticeQueueItemDescriptor

    data class Cloze(
        override val pointNumber: String,
        override val deckId: Long
    ) : GrammarPracticeQueueItemDescriptor

    data class ConjugationBuilder(
        override val pointNumber: String,
        override val deckId: Long
    ) : GrammarPracticeQueueItemDescriptor

    data class SentenceScramble(
        override val pointNumber: String,
        override val deckId: Long
    ) : GrammarPracticeQueueItemDescriptor

    data class SurvivalDialogue(
        override val pointNumber: String,
        override val deckId: Long
    ) : GrammarPracticeQueueItemDescriptor
}

sealed interface GrammarPracticeItemData {
    fun toReviewState(coroutineScope: CoroutineScope): MutableGrammarReviewState

    data class Flashcard(
        val title: String,
        val formula: String,
        val meaning: String,
        val examples: List<String>,
        val notes: String,
        val showMeaningInFront: Boolean
    ) : GrammarPracticeItemData {
        override fun toReviewState(coroutineScope: CoroutineScope) = MutableGrammarReviewState.Flashcard(
            title = title,
            formula = formula,
            meaning = meaning,
            examples = examples,
            notes = notes,
            showMeaningInFront = showMeaningInFront
        )
    }

    data class Cloze(
        val pointNumber: String,
        val title: String,
        val formula: String,
        val clozeSentence: String,
        val meaning: String,
        val options: List<String>,
        val correctAnswerIndex: Int
    ) : GrammarPracticeItemData {
        override fun toReviewState(coroutineScope: CoroutineScope) = MutableGrammarReviewState.Cloze(
            title = title,
            formula = formula,
            clozeSentence = clozeSentence,
            meaning = meaning,
            options = options,
            correctAnswerIndex = correctAnswerIndex
        )
    }

    data class ConjugationBuilder(
        val pointNumber: String,
        val title: String,
        val formula: String,
        val verbDictionary: String,
        val verbMeaning: String,
        val targetConjugation: String,
        val syllables: List<String>
    ) : GrammarPracticeItemData {
        override fun toReviewState(coroutineScope: CoroutineScope) = MutableGrammarReviewState.ConjugationBuilder(
            title = title,
            formula = formula,
            verbDictionary = verbDictionary,
            verbMeaning = verbMeaning,
            targetConjugation = targetConjugation,
            syllables = syllables
        )
    }

    data class SentenceScramble(
        val pointNumber: String,
        val title: String,
        val formula: String,
        val meaning: String,
        val originalSentence: String,
        val scrambledParts: List<String>
    ) : GrammarPracticeItemData {
        override fun toReviewState(coroutineScope: CoroutineScope) = MutableGrammarReviewState.SentenceScramble(
            title = title,
            formula = formula,
            meaning = meaning,
            originalSentence = originalSentence,
            scrambledParts = scrambledParts
        )
    }

    data class SurvivalDialogue(
        val title: String,
        val context: String,
        val dialogueLines: List<Pair<String, String>>, // Speaker to Text (could be Japanese or translated)
        val options: List<String>,
        val correctAnswerIndex: Int
    ) : GrammarPracticeItemData {
        override fun toReviewState(coroutineScope: CoroutineScope) = MutableGrammarReviewState.SurvivalDialogue(
            title = title,
            context = context,
            dialogueLines = dialogueLines,
            options = options,
            correctAnswerIndex = correctAnswerIndex
        )
    }
}

data class GrammarSummaryItem(
    val pointNumber: String,
    val title: String,
    val srsCardKey: SrsCardKey,
    val srsCard: SrsCard,
    val newSrsCard: SrsCard,
    val isDoneInFirstAttempt: Boolean
)
