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

        // Mock conjugation logic for Milestone 3
        // In a real app, this would use a robust dictionary and conjugation engine.
        val verbDictionary = "たべる" // taberu (to eat)
        val verbMeaning = "Makan (Bentuk Kamus)"
        
        // Since we want to conjugate according to the grammar point (e.g. 〜たい)
        val targetConjugation = "たべたい"
        val syllables = listOf("た", "べ", "た", "い", "ま", "す", "て") // include some distractors

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
}
