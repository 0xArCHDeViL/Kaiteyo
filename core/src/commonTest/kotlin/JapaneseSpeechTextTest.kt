import kotlin.test.Test
import kotlin.test.assertEquals

class JapaneseSpeechTextTest {

    @Test
    fun removesUiArtifactsButPreservesJapaneseText() {
        assertEquals(
            "私は学生です。",
            ua.syt0r.kanji.core.tts.JapaneseSpeechText.normalize(
                "A：~~私は学生です。~~（Saya adalah seorang siswa）"
            )
        )
    }

    @Test
    fun normalizesJapaneseKanjiReadingNotation() {
        assertEquals(
            "あたらしい",
            ua.syt0r.kanji.core.tts.JapaneseSpeechText.normalizeKanjiReading("あたら.しい~")
        )
    }

    @Test
    fun keepsKanjiKanaAndPunctuationForBroadCharacterCoverage() {
        val text = "漢字・かな、カタカナ！？々ー"
        assertEquals(text, ua.syt0r.kanji.core.tts.JapaneseSpeechText.normalize(text))
    }
}
