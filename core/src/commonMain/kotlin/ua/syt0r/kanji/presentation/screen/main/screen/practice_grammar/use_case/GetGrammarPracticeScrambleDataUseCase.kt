package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case

import ua.syt0r.kanji.core.grammar.GrammarContentRepository
import ua.syt0r.kanji.core.grammar.GrammarQuestionEngine
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueItemDescriptor

interface GetGrammarPracticeScrambleDataUseCase {
    suspend operator fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.SentenceScramble): GrammarPracticeItemData.SentenceScramble
}

class DefaultGetGrammarPracticeScrambleDataUseCase(
    private val contentRepository: GrammarContentRepository,
    private val questionEngine: GrammarQuestionEngine,
) : GetGrammarPracticeScrambleDataUseCase {

    override suspend fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.SentenceScramble): GrammarPracticeItemData.SentenceScramble {
        val chapter = contentRepository.findChapter(descriptor.deckId)
            ?: error("Grammar chapter ${descriptor.deckId} not found")
        val point = chapter.points.firstOrNull { it.number == descriptor.pointNumber }
            ?: error("Grammar point ${descriptor.pointNumber} not found")
        val question = questionEngine.scramble(point, seedFor(descriptor))
            ?: error("No validated scramble question for ${point.number}")
        return GrammarPracticeItemData.SentenceScramble(
            pointNumber = point.number,
            title = chapter.title,
            formula = point.formulaTitle,
            meaning = question.meaning,
            originalSentence = question.sentence,
            scrambledParts = question.tokens,
        )
    }

    private fun seedFor(descriptor: GrammarPracticeQueueItemDescriptor.SentenceScramble): Int =
        (descriptor.deckId * 41 + descriptor.pointNumber.hashCode()).toInt()
}
