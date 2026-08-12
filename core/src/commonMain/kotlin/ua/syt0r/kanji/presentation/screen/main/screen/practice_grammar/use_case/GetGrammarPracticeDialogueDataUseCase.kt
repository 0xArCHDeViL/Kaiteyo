package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case

import ua.syt0r.kanji.core.grammar.GrammarContentRepository
import ua.syt0r.kanji.core.grammar.GrammarQuestionEngine
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueItemDescriptor

interface GetGrammarPracticeDialogueDataUseCase {
    suspend operator fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.SurvivalDialogue): GrammarPracticeItemData.SurvivalDialogue
}

class DefaultGetGrammarPracticeDialogueDataUseCase(
    private val contentRepository: GrammarContentRepository,
    private val questionEngine: GrammarQuestionEngine,
) : GetGrammarPracticeDialogueDataUseCase {

    override suspend fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.SurvivalDialogue): GrammarPracticeItemData.SurvivalDialogue {
        val chapter = contentRepository.findChapter(descriptor.deckId)
            ?: error("Grammar chapter ${descriptor.deckId} not found")
        val point = chapter.points.firstOrNull { it.number == descriptor.pointNumber }
            ?: error("Grammar point ${descriptor.pointNumber} not found")
        val question = questionEngine.dialogue(point, seedFor(descriptor))
            ?: error("No validated dialogue question for ${point.number}")
        return GrammarPracticeItemData.SurvivalDialogue(
            title = "Dialog — ${chapter.title}",
            context = question.context,
            dialogueLines = question.lines,
            options = question.options,
            correctAnswerIndex = question.options.indexOf(question.answer),
        )
    }

    private fun seedFor(descriptor: GrammarPracticeQueueItemDescriptor.SurvivalDialogue): Int =
        (descriptor.deckId * 43 + descriptor.pointNumber.hashCode()).toInt()
}
