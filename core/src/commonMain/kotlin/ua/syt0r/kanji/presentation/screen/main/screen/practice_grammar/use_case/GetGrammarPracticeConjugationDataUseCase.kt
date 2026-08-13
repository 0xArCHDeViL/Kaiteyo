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
?: return@invoke createFallbackConjugation(descriptor)
        val point = chapter.points.firstOrNull { it.number == descriptor.pointNumber }
?: return@invoke createFallbackConjugation(descriptor)
        val question = questionEngine.conjugation(point, seedFor(descriptor))
?: return@invoke createFallbackConjugation(descriptor)
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

    private fun createFallbackConjugation(descriptor: GrammarPracticeQueueItemDescriptor.ConjugationBuilder): GrammarPracticeItemData.ConjugationBuilder {
        return GrammarPracticeItemData.ConjugationBuilder(
            pointNumber = descriptor.pointNumber,
            title = "Conjugation Practice",
            formula = "",
            verbDictionary = "suru",
            verbMeaning = "to do",
            targetConjugation = "shimasu",
            syllables = listOf("し", "ま", "す")
        )
    }
