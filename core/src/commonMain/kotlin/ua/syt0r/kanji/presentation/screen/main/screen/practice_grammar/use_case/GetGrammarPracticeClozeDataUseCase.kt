package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case

import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.presentation.screen.main.screen.library.screen.grammar.GrammarChapter
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueItemDescriptor

interface GetGrammarPracticeClozeDataUseCase {
    suspend operator fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.Cloze): GrammarPracticeItemData.Cloze
}

class DefaultGetGrammarPracticeClozeDataUseCase : GetGrammarPracticeClozeDataUseCase {
    @OptIn(ExperimentalResourceApi::class)
    override suspend fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.Cloze): GrammarPracticeItemData.Cloze {
        val bytes = Res.readBytes("files/bunpou_data.json")
        val jsonString = bytes.decodeToString()
        val chapters = Json.decodeFromString<List<GrammarChapter>>(jsonString)

        val chapter = chapters.first { it.id.toLong() == descriptor.deckId }
        val point = chapter.points.first { it.number == descriptor.pointNumber }

        // Find the first example sentence
        val rawExample = point.examples.firstOrNull() ?: "Example missing. Contoh hilang."
        
        // Very basic parsing: sentences often have a Japanese part and an Indonesian part
        // Example: "私 は タマ です。Saya adalah Tama."
        val splitIndex = rawExample.indexOf("。")
        val (japanese, indonesian) = if (splitIndex != -1 && splitIndex < rawExample.length - 1) {
            rawExample.substring(0, splitIndex + 1) to rawExample.substring(splitIndex + 1).trim()
        } else {
            rawExample to ""
        }

        // Extremely simple Cloze logic for grammar:
        // We look for common grammar words or particles to hide, or just randomly hide a part of the formula.
        // For demonstration of UI logic, we'll hide a random sequence of 1-3 kana.
        val options = listOf("は", "が", "を", "に") // Mocked options for now
        val answerIndex = 0
        
        // This is a naive mock replacement. In a real scenario, this would use a proper grammar parser
        // or a pre-defined clozes array in bunpou_data.json.
        val clozeSentence = japanese.replaceFirst("は", "___")

        return GrammarPracticeItemData.Cloze(
            pointNumber = point.number,
            title = chapter.title,
            formula = point.formulaTitle,
            clozeSentence = clozeSentence,
            meaning = indonesian.takeIf { it.isNotBlank() } ?: point.meaning,
            options = options,
            correctAnswerIndex = answerIndex
        )
    }
}
