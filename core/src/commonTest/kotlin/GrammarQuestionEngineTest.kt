import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import ua.syt0r.kanji.core.grammar.GrammarMarkup
import ua.syt0r.kanji.core.grammar.GrammarQuestionEngine
import ua.syt0r.kanji.presentation.screen.main.screen.library.screen.grammar.GrammarPoint

class GrammarQuestionEngineTest {

    private val engine = GrammarQuestionEngine()

    @Test
    fun markupStrikesOnlyMarkedSegmentsAndCleansSpeechText() {
        val segments = GrammarMarkup.parse("KK ~~ます~~ てください")
        assertEquals(listOf("KK ", "ます", " てください"), segments.map { it.text })
        assertEquals(listOf(false, true, false), segments.map { it.struck })
        assertEquals("KK ます てください", GrammarMarkup.plainText("KK ~~ます~~ てください"))
        assertEquals("わたしは学生です。", GrammarMarkup.japaneseSpeechText("A：わたしは学生です。"))
    }

    @Test
    fun clozeIsDeterministicAndHasUniqueValidatedOptions() {
        val point = GrammarPoint(
            number = "【01】",
            formulaTitle = "KB は KB です",
            meaning = "identitas",
            formulas = emptyList(),
            examples = listOf("A：わたし は がくせい です。\nSaya pelajar."),
            notes = "",
        )
        val first = engine.cloze(point, seed = 7)
        val second = engine.cloze(point, seed = 7)
        assertNotNull(first)
        assertEquals(first, second)
        assertTrue(first.options.distinct().size == first.options.size)
        assertEquals("は", first.answer)
        assertTrue(first.sentence.contains("____"))
    }

    @Test
    fun conjugationSupportsVerbClassesWithoutFiveVerbRandomFallback() {
        val point = GrammarPoint(
            number = "【02】",
            formulaTitle = "KK ~~ます~~ て ください",
            meaning = "permintaan",
            formulas = emptyList(),
            examples = emptyList(),
            notes = "",
        )
        val question = engine.conjugation(point, seed = 11)
        assertNotNull(question)
        assertTrue(question.answer.endsWith("て") || question.answer.endsWith("で"))
        assertTrue(question.syllables.size >= question.answer.length)
    }

    @Test
    fun scramblePreservesDuplicateTokenIdentity() {
        val point = GrammarPoint(
            number = "【03】",
            formulaTitle = "KB は 〜",
            meaning = "kalimat",
            formulas = emptyList(),
            examples = listOf("これ は これ です。\nIni adalah ini."),
            notes = "",
        )
        val question = engine.scramble(point, seed = 3)
        assertNotNull(question)
        assertEquals(question.sentence.split(" ").size, question.tokens.size)
        assertEquals(question.sentence.split(" ").sorted(), question.tokens.sorted())
    }

    @Test
    fun unsupportedModesAreNotAdvertised() {
        val point = GrammarPoint(
            number = "【04】",
            formulaTitle = "〜です",
            meaning = "copula",
            formulas = emptyList(),
            examples = listOf("学生です。\nPelajar."),
            notes = "",
        )
        assertTrue(!engine.supportsConjugation(point))
        assertTrue(!engine.supportsScramble(point))
        assertTrue(!engine.supportsDialogue(point))
    }
}
