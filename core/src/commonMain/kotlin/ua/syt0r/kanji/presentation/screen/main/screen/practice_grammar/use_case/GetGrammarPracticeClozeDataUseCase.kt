package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case

import ua.syt0r.kanji.core.grammar.GrammarContentRepository
import ua.syt0r.kanji.core.grammar.GrammarQuestionEngine
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueItemDescriptor

interface GetGrammarPracticeClozeDataUseCase {
    suspend operator fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.Cloze): GrammarPracticeItemData.Cloze
}

class DefaultGetGrammarPracticeClozeDataUseCase(
    private val contentRepository: GrammarContentRepository,
    private val questionEngine: GrammarQuestionEngine,
) : GetGrammarPracticeClozeDataUseCase {

    override suspend fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.Cloze): GrammarPracticeItemData.Cloze {
        val chapter = contentRepository.findChapter(descriptor.deckId)
?: return@invoke createFallbackCloze(descriptor)
        val point = chapter.points.firstOrNull { it.number == descriptor.pointNumber }
?: return@invoke createFallbackCloze(descriptor)
        val question = questionEngine.cloze(point, seedFor(descriptor))
?: return@invoke createFallbackCloze(descriptor)
        return GrammarPracticeItemData.Cloze(
            pointNumber = point.number,
            title = chapter.title,
            formula = point.formulaTitle,
            clozeSentence = question.sentence,
            meaning = question.meaning,
            options = question.options,
            correctAnswerIndex = question.options.indexOf(question.answer),
        )
    }

    private fun seedFor(descriptor: GrammarPracticeQueueItemDescriptor.Cloze): Int =
        (descriptor.deckId * 31 + descriptor.pointNumber.hashCode()).toInt()
}

    private fun createFallbackCloze(descriptor: GrammarPracticeQueueItemDescriptor.Cloze): GrammarPracticeItemData.Cloze {
        return GrammarPracticeItemData.Cloze(
            pointNumber = descriptor.pointNumber,
            title = "Grammar Practice",
            formula = "",
            clozeSentence = "...",
            meaning = "Practice review",
            options = listOf("...", "...", "...", "..."),
            correctAnswerIndex = 0
        )
    }
