package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case

import ua.syt0r.kanji.core.grammar.GrammarContentRepository
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueItemDescriptor

interface GetGrammarPracticeFlashcardDataUseCase {
    suspend operator fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.Flashcard): GrammarPracticeItemData.Flashcard
}

class DefaultGetGrammarPracticeFlashcardDataUseCase(
    private val contentRepository: GrammarContentRepository,
) : GetGrammarPracticeFlashcardDataUseCase {

    override suspend fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.Flashcard): GrammarPracticeItemData.Flashcard {
        val chapter = contentRepository.findChapter(descriptor.deckId)
            ?: error("Grammar chapter ${descriptor.deckId} not found")
        val point = chapter.points.firstOrNull { it.number == descriptor.pointNumber }
            ?: error("Grammar point ${descriptor.pointNumber} not found")
        return GrammarPracticeItemData.Flashcard(
            title = chapter.title,
            formula = point.formulas.joinToString("\n").ifBlank { point.formulaTitle },
            meaning = point.meaning,
            examples = point.examples,
            notes = point.notes,
            showMeaningInFront = descriptor.showMeaningInFront,
        )
    }
}
