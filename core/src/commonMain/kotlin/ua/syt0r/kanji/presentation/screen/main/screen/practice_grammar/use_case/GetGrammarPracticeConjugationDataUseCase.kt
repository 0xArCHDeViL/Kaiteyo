package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case

import ua.syt0r.kanji.core.grammar.GrammarContentRepository
import ua.syt0r.kanji.core.grammar.GrammarQuestionEngine
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueItemDescriptor

interface GetGrammarPracticeConjugationDataUseCase {
    suspend operator fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.ConjugationBuilder): GrammarPracticeItemData.ConjugationBuilder
}

class DefaultGetGrammarPracticeConjugationDataUseCase(
    private val contentRepository: GrammarContentRepository,
    private val questionEngine: GrammarQuestionEngine,
) : GetGrammarPracticeConjugationDataUseCase {

    override suspend fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.ConjugationBuilder): GrammarPracticeItemData.ConjugationBuilder {
        val chapter = contentRepository.findChapter(descriptor.deckId)
            ?: error("Grammar chapter ${descriptor.deckId} not found")
        val point = chapter.points.firstOrNull { it.number == descriptor.pointNumber }
            ?: error("Grammar point ${descriptor.pointNumber} not found")
        val question = questionEngine.conjugation(point, seedFor(descriptor))
            ?: error("No validated conjugation question for ${point.number}")
        return GrammarPracticeItemData.ConjugationBuilder(
            pointNumber = point.number,
            title = chapter.title,
            formula = point.formulaTitle,
            verbDictionary = question.dictionaryForm,
            verbMeaning = question.meaning,
            targetConjugation = question.answer,
            syllables = question.syllables,
        )
    }

    private fun seedFor(descriptor: GrammarPracticeQueueItemDescriptor.ConjugationBuilder): Int =
        (descriptor.deckId * 37 + descriptor.pointNumber.hashCode()).toInt()
}
