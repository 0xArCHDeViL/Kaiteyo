package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case

import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.presentation.screen.main.screen.library.screen.grammar.GrammarChapter
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueItemDescriptor

interface GetGrammarPracticeDialogueDataUseCase {
    suspend operator fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.SurvivalDialogue): GrammarPracticeItemData.SurvivalDialogue
}

class DefaultGetGrammarPracticeDialogueDataUseCase : GetGrammarPracticeDialogueDataUseCase {
    @OptIn(ExperimentalResourceApi::class)
    override suspend fun invoke(descriptor: GrammarPracticeQueueItemDescriptor.SurvivalDialogue): GrammarPracticeItemData.SurvivalDialogue {
        val bytes = Res.readBytes("files/bunpou_data.json")
        val jsonString = bytes.decodeToString()
        val chapters = Json.decodeFromString<List<GrammarChapter>>(jsonString)

        val chapter = chapters.first { it.id.toLong() == descriptor.deckId }
        val point = chapter.points.first { it.number == descriptor.pointNumber }

        // Mock Dialogue logic for Milestone 3
        val rawExample = point.examples.firstOrNull() ?: "Example missing. Contoh hilang."
        val splitIndex = rawExample.indexOf("。")
        val (japanese, _) = if (splitIndex != -1 && splitIndex < rawExample.length - 1) {
            rawExample.substring(0, splitIndex + 1) to rawExample.substring(splitIndex + 1).trim()
        } else {
            rawExample to ""
        }

        val dialogueLines = listOf(
            "Sensei" to "こんにちは！",
            "You" to japanese, // The target grammar point usage
            "Sensei" to "なるほどね！"
        )
        
        val options = listOf(
            japanese, // Correct
            "Random wrong answer",
            "Another distractor"
        ).shuffled()
        
        val correctIndex = options.indexOf(japanese)
        
        return GrammarPracticeItemData.SurvivalDialogue(
            title = "Survival Dialogue: ${chapter.title}",
            context = "Respond to the teacher using the grammar point you learned: ${point.formulaTitle}",
            dialogueLines = dialogueLines.map { (speaker, text) -> 
                if (text == japanese) speaker to "..." // Hide the answer in the dialogue
                else speaker to text 
            },
            options = options,
            correctAnswerIndex = correctIndex
        )
    }
}
