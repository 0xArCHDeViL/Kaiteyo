package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case

import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.presentation.screen.main.screen.library.screen.grammar.GrammarChapter
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueItemDescriptor

interface GetGrammarPracticeConjugationDataUseCase {
    suspend operator fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.ConjugationBuilder): GrammarPracticeItemData.ConjugationBuilder
}

class DefaultGetGrammarPracticeConjugationDataUseCase : GetGrammarPracticeConjugationDataUseCase {
    @OptIn(ExperimentalResourceApi::class)
    override suspend fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.ConjugationBuilder): GrammarPracticeItemData.ConjugationBuilder {
        val bytes = Res.readBytes("files/bunpou_data.json")
        val jsonString = bytes.decodeToString()
        val chapters = Json.decodeFromString<List<GrammarChapter>>(jsonString)

        val chapter = chapters.first { it.id.toLong() == descriptor.deckId }
        val point = chapter.points.first { it.number == descriptor.pointNumber }

        // Dynamic verb selection based on the chapter or random
        val (verbDictionary, verbMeaning, targetConjugation) = getVerbConjugation(chapter.id, point.formulaTitle)
        
        // Generate syllables and distractors
        val syllables = targetConjugation.map { it.toString() }.toMutableList()
        val distractors = listOf("ま", "す", "て", "た", "な", "い", "ん", "で", "る")
        syllables.addAll(distractors.shuffled().take(3))

        return GrammarPracticeItemData.ConjugationBuilder(
            pointNumber = point.number,
            title = chapter.title,
            formula = point.formulaTitle,
            verbDictionary = verbDictionary,
            verbMeaning = verbMeaning,
            targetConjugation = targetConjugation,
            syllables = syllables.shuffled()
        )
    }
    
    private fun getVerbConjugation(chapterId: Int, formula: String): Triple<String, String, String> {
        val verbs = listOf(
            Triple("たべる", "Makan (Bentuk Kamus)"),
            Triple("いく", "Pergi (Bentuk Kamus)"),
            Triple("のむ", "Minum (Bentuk Kamus)"),
            Triple("する", "Melakukan (Bentuk Kamus)"),
            Triple("くる", "Datang (Bentuk Kamus)")
        )
        val verb = verbs.random()
        val dict = verb.first
        
        // Basic conjugation logic
        val stem = when (dict) {
            "たべる" -> "たべ"
            "いく" -> "いき"
            "のむ" -> "のみ"
            "する" -> "し"
            "くる" -> "き"
            else -> dict.dropLast(1)
        }
        
        val naiStem = when (dict) {
            "たべる" -> "たべ"
            "いく" -> "いか"
            "のむ" -> "のま"
            "する" -> "し"
            "くる" -> "こ"
            else -> dict.dropLast(1)
        }
        
        val teForm = when (dict) {
            "たべる" -> "たべて"
            "いく" -> "いって"
            "のむ" -> "のんで"
            "する" -> "して"
            "くる" -> "きて"
            else -> dict.dropLast(1) + "て"
        }
        
        val taForm = teForm.dropLast(1) + (if (teForm.last() == 'で') "だ" else "た")

        val target = when {
            formula.contains("~~ます~~") -> stem + formula.substringAfter("~~ます~~").replace(" ", "")
            formula.contains("~~ない~~") -> naiStem + formula.substringAfter("~~ない~~").replace(" ", "")
            formula.contains("て") || formula.contains("で") -> teForm + formula.substringAfter("て").substringAfter("で").replace(" ", "")
            else -> dict
        }
        
        // Cleanup markdown and strange artifacts
        var cleanTarget = target.replace("~~", "").replace("／", "").replace("〜", "")
        if (cleanTarget.isBlank()) cleanTarget = dict
        
        // Basic fallback to stem + masu if we can't figure it out
        if (cleanTarget.length > 20) cleanTarget = stem + "ます"

        return Triple(dict, verb.second, cleanTarget)
    }
}
