package ua.syt0r.kanji.core.tts

/**
 * Converts UI/formula text into text suitable for Japanese speech synthesis.
 * Japanese characters are intentionally preserved; transliteration belongs to
 * the learning UI, not to the speech engine.
 */
object JapaneseSpeechText {

    private val speakerPrefix = Regex("^\\s*[A-Za-zＡ-Ｚａ-ｚ]+\\s*[:：]\\s*")
    private val inlineLatinTranslation = Regex("\\([^)]*[A-Za-z][^)]*\\)|（[^）]*[A-Za-zＡ-Ｚａ-ｚ][^）]*）")
    private val controlCharacters = Regex("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F\\u007F]")
    private val whitespace = Regex("\\s+")

    fun normalize(raw: String): String {
        return raw
            .replace(controlCharacters, " ")
            .replace(speakerPrefix, "")
            .replace(inlineLatinTranslation, "")
            .replace("~~", "")
            .replace(whitespace, " ")
            .trim()
    }

    fun normalizeKanjiReading(raw: String): String {
        return raw
            .replace("~", "")
            .replace(".", "")
            .replace("・", "")
            .replace(Regex("[（(].*"), "")
            .trim()
    }
}
