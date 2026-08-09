package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case

import kotlinx.serialization.json.Json
import ua.syt0r.kanji.presentation.screen.main.screen.library.screen.grammar.GrammarChapter
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueItemDescriptor
import ua.syt0r.kanji.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

interface GetGrammarPracticeFlashcardDataUseCase {
    suspend operator fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.Flashcard): GrammarPracticeItemData.Flashcard
}

class DefaultGetGrammarPracticeFlashcardDataUseCase : GetGrammarPracticeFlashcardDataUseCase {

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.Flashcard): GrammarPracticeItemData.Flashcard {
        // Load the data from JSON
        val bytes = ua.syt0r.kanji.Res.readBytes("files/bunpou_data.json")
        val jsonString = bytes.decodeToString()
        val chapters = Json.decodeFromString<List<GrammarChapter>>(jsonString)
        
        // Find the specific grammar point
        val point = chapters.flatMap { it.points }.first { it.number == descriptor.pointNumber }
        
        return GrammarPracticeItemData.Flashcard(
            title = point.formulaTitle,
            formula = point.formulas.joinToString("\n"),
            meaning = point.meaning,
            examples = point.examples,
            notes = point.notes,
            showMeaningInFront = descriptor.showMeaningInFront
        )
    }

}
