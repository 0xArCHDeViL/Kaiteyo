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
        
        val splitIndex = rawExample.indexOf("。")
        val (japanese, indonesian) = if (splitIndex != -1 && splitIndex < rawExample.length - 1) {
            rawExample.substring(0, splitIndex + 1) to rawExample.substring(splitIndex + 1).trim()
        } else {
            rawExample to ""
        }

        // 1. Identify Target Particle / Keyword from Grammar Formula
        val commonParticles = listOf("は", "が", "を", "に", "へ", "で", "と", "も", "の", "より", "から", "まで", "か", "や", "ね", "よ")
        
        // Find which particle is in the grammar point formulaTitle and is also present in the Japanese sentence as an isolated token
        val targetParticle = commonParticles.firstOrNull { particle -> 
            point.formulaTitle.contains(particle) && Regex("(?<=^|\\s)$particle(?=\\s|$)").containsMatchIn(japanese)
        } ?: commonParticles.firstOrNull { particle -> 
            Regex("(?<=^|\\s)$particle(?=\\s|$)").containsMatchIn(japanese)
        } ?: "は" // Fallback

        // 2. Blank out the target particle in the sentence correctly using regex
        val regex = Regex("(?<=^|\\s)$targetParticle(?=\\s|$)")
        val clozeSentence = japanese.replaceFirst(regex, "____")
        
        // 3. Generate options ensuring target is present
        val baseOptions = mutableListOf("は", "が", "を", "に", "で", "と", "も", "か").filter { it != targetParticle }
        val options = (baseOptions.shuffled().take(3) + targetParticle).shuffled()
        
        val answerIndex = options.indexOf(targetParticle)

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
