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
            ?: error("Grammar chapter ${descriptor.deckId} not found")
        val point = chapter.points.firstOrNull { it.number == descriptor.pointNumber }
            ?: error("Grammar point ${descriptor.pointNumber} not found")
        val question = questionEngine.cloze(point, seedFor(descriptor))
            ?: error("No validated cloze question for ${point.number}")
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
