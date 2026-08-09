package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case

import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.presentation.screen.main.screen.library.screen.grammar.GrammarChapter
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueItemDescriptor

interface GetGrammarPracticeScrambleDataUseCase {
    suspend operator fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.SentenceScramble): GrammarPracticeItemData.SentenceScramble
}

class DefaultGetGrammarPracticeScrambleDataUseCase : GetGrammarPracticeScrambleDataUseCase {
    @OptIn(ExperimentalResourceApi::class)
    override suspend fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.SentenceScramble): GrammarPracticeItemData.SentenceScramble {
        val bytes = Res.readBytes("files/bunpou_data.json")
        val jsonString = bytes.decodeToString()
        val chapters = Json.decodeFromString<List<GrammarChapter>>(jsonString)

        val chapter = chapters.first { it.id.toLong() == descriptor.deckId }
        val point = chapter.points.first { it.number == descriptor.pointNumber }

        val rawExample = point.examples.firstOrNull() ?: "Example missing. Contoh hilang."
        
        val splitIndex = rawExample.indexOf("。")
        val (japanese, indonesian) = if (splitIndex != -1 && splitIndex < rawExample.length - 1) {
            rawExample.substring(0, splitIndex + 1) to rawExample.substring(splitIndex + 1).trim()
        } else {
            rawExample to ""
        }

        // Mock Scramble logic for Milestone 3
        // We will just artificially break the Japanese sentence into chunks.
        val cleanedJapanese = japanese.replace("A：", "").replace("B：", "").trim()
        val scrambledParts = cleanedJapanese.split(" ").filter { it.isNotBlank() }
        
        return GrammarPracticeItemData.SentenceScramble(
            pointNumber = point.number,
            title = chapter.title,
            formula = point.formulaTitle,
            meaning = indonesian.takeIf { it.isNotBlank() } ?: point.meaning,
            originalSentence = cleanedJapanese,
            scrambledParts = scrambledParts.shuffled()
        )
    }
}
