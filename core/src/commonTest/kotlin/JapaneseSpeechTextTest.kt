import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ua.syt0r.kanji.core.tts.JapaneseSpeechText

class JapaneseSpeechTextTest {

    @Test
    fun removesUiArtifactsButPreservesJapaneseText() {
        assertEquals(
            "私は学生です。",
            JapaneseSpeechText.normalize(
                "A：~~私は学生です。~~（Saya adalah seorang siswa）"
            )
        )
    }

    @Test
    fun normalizesJapaneseKanjiReadingNotationWithoutDestroyingMarkerMeaning() {
        assertEquals(
            "あたらしい",
            JapaneseSpeechText.normalizeKanjiReading("あたら.しい~")
        )
        assertEquals("-と", JapaneseSpeechText.normalizeKanjiReading("-と"))
    }

    @Test
    fun keepsKanjiKanaAndPunctuationForBroadCharacterCoverage() {
        val text = "漢字・かな、カタカナ！？々ー"
        assertEquals(text, JapaneseSpeechText.normalize(text))
    }

    @Test
    fun skipsKanjidicSuffixFragmentsAndSelectsStandaloneHumanReading() {
        assertFalse(JapaneseSpeechText.isStandaloneKanjiReading("-と"))
        assertFalse(JapaneseSpeechText.isStandaloneKanjiReading("-り"))
        assertTrue(JapaneseSpeechText.isStandaloneKanjiReading("ひと"))

        assertEquals(
            "ひと",
            JapaneseSpeechText.selectStandaloneKanjiReading(
                kunReadings = listOf("-と", "-り", "ひと"),
                onReadings = listOf("ジン", "ニン")
            )
        )
    }

    @Test
    fun fallsBackToOnReadingWhenKunOnlyContainsOkuriganaFragments() {
        assertEquals(
            "ガク",
            JapaneseSpeechText.selectStandaloneKanjiReading(
                kunReadings = listOf("-ぶ", "-まな"),
                onReadings = listOf("ガク")
            )
        )
    }

    @Test
    fun rejectsLatinAndMetadataAsSpeechReading() {
        assertFalse(JapaneseSpeechText.isStandaloneKanjiReading("hito"))
        assertEquals("ひと", JapaneseSpeechText.normalizeKanjiReading("ひと(rare)"))
        assertTrue(JapaneseSpeechText.isStandaloneKanjiReading("ひと"))
    }
}
