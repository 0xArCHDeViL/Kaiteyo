import kotlin.test.Test
import kotlin.test.assertEquals
import ua.syt0r.kanji.core.app_data.data.buildFuriganaString
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.FuriganaStringCompound
import ua.syt0r.kanji.core.app_data.data.toKanaReading
import ua.syt0r.kanji.core.tts.JapanesePronunciationEngine
import ua.syt0r.kanji.core.tts.JapaneseSpeechContext
import ua.syt0r.kanji.core.tts.JapaneseSpeechRequest
import ua.syt0r.kanji.core.tts.JapaneseSpeechStrategy

class JapanesePronunciationEngineTest {

    @Test
    fun resolvesHumanKanjiToStandaloneKunReading() {
        val plan = JapanesePronunciationEngine.resolve(
            JapaneseSpeechRequest(
                displayText = "人",
                kunReadings = listOf("-と", "-り", "ひと"),
                onReadings = listOf("ジン", "ニン"),
                context = JapaneseSpeechContext.IsolatedKanji
            )
        )

        requireNotNull(plan)
        assertEquals("人", plan.displayText)
        assertEquals("ひと", plan.speakText)
        assertEquals(JapaneseSpeechStrategy.StandaloneKun, plan.strategy)
    }

    @Test
    fun resolvesHyakuFromTheSelectedAuthoritativeReading() {
        val plan = JapanesePronunciationEngine.resolve(
            JapaneseSpeechRequest(
                displayText = "百",
                pronunciation = "ひゃく",
                context = JapaneseSpeechContext.IsolatedKanji,
            )
        )

        requireNotNull(plan)
        assertEquals("ひゃく", plan.speakText)
        assertEquals(JapaneseSpeechStrategy.ExplicitReading, plan.strategy)
    }

    @Test
    fun resolvesOkashiAsTheCompleteVocabularyReading() {
        val plan = JapanesePronunciationEngine.resolve(
            JapaneseSpeechRequest(
                displayText = "お菓子",
                pronunciation = "おかし",
                context = JapaneseSpeechContext.Vocabulary,
            )
        )

        requireNotNull(plan)
        assertEquals("おかし", plan.speakText)
        assertEquals(JapaneseSpeechStrategy.ExplicitReading, plan.strategy)
    }

    @Test
    fun emptyFuriganaAnnotationsDoNotDeleteVisibleCompoundText() {
        val hidden = FuriganaString(
            compounds = listOf(
                FuriganaStringCompound("お"),
                FuriganaStringCompound("菓子", ""),
            )
        )

        assertEquals("お菓子", hidden.toKanaReading())
    }

    @Test
    fun rejectsOkuriganaDotReadingForStandaloneKanji() {
        val plan = JapanesePronunciationEngine.resolve(
            JapaneseSpeechRequest(
                displayText = "学",
                pronunciation = "まな.ぶ",
                kunReadings = listOf("まな.ぶ"),
                onReadings = listOf("ガク"),
                context = JapaneseSpeechContext.IsolatedKanji
            )
        )

        requireNotNull(plan)
        assertEquals("ガク", plan.speakText)
        assertEquals(JapaneseSpeechStrategy.StandaloneOn, plan.strategy)
    }

    @Test
    fun rejectsInvalidExplicitFragmentAndUsesStandaloneCandidate() {
        val plan = JapanesePronunciationEngine.resolve(
            JapaneseSpeechRequest(
                displayText = "人",
                pronunciation = "-と",
                kunReadings = listOf("-と", "ひと"),
                context = JapaneseSpeechContext.IsolatedKanji
            )
        )

        requireNotNull(plan)
        assertEquals("ひと", plan.speakText)
        assertEquals(JapaneseSpeechStrategy.StandaloneKun, plan.strategy)
    }

    @Test
    fun fallsBackToOnWhenNoStandaloneKunExists() {
        val plan = JapanesePronunciationEngine.resolve(
            JapaneseSpeechRequest(
                displayText = "学",
                kunReadings = listOf("-まな"),
                onReadings = listOf("ガク"),
                context = JapaneseSpeechContext.IsolatedKanji
            )
        )

        requireNotNull(plan)
        assertEquals("ガク", plan.speakText)
        assertEquals(JapaneseSpeechStrategy.StandaloneOn, plan.strategy)
    }

    @Test
    fun usesFuriganaForContextRichVocabulary() {
        val plan = JapanesePronunciationEngine.resolve(
            JapaneseSpeechRequest(
                displayText = "日本",
                furigana = buildFuriganaString {
                    append("日", "に")
                    append("本", "ほん")
                },
                context = JapaneseSpeechContext.Vocabulary
            )
        )

        requireNotNull(plan)
        assertEquals("にほん", plan.speakText)
        assertEquals(JapaneseSpeechStrategy.Furigana, plan.strategy)
    }

    @Test
    fun keepsRawJapaneseTextWhenContextHasNoLexicalReading() {
        val text = "日本語を勉強します。"
        val plan = JapanesePronunciationEngine.resolve(
            JapaneseSpeechRequest(
                displayText = text,
                context = JapaneseSpeechContext.Grammar
            )
        )

        requireNotNull(plan)
        assertEquals(text, plan.speakText)
        assertEquals(JapaneseSpeechStrategy.RawJapaneseFallback, plan.strategy)
    }
}
