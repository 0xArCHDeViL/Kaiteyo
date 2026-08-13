package ua.syt0r.kanji.core.tts

import ua.syt0r.kanji.core.app_data.data.FuriganaString

/**
 * Normalizes text before it reaches a Japanese speech engine.
 *
 * KANJIDIC Kun readings are not all standalone pronunciations. A reading such
 * as `-と` or `-り` is a suffix/prefix fragment used with okurigana and must
 * never be spoken as the pronunciation of an isolated Kanji. The selector in
 * this object keeps that distinction instead of blindly stripping the marker.
 */
object JapaneseSpeechText {

    private val speakerPrefix = Regex("^\\s*[A-Za-zＡ-Ｚａ-ｚ]+\\s*[:：]\\s*")
    private val inlineLatinTranslation = Regex(
        "\\([^)]*[A-Za-z][^)]*\\)|（[^）]*[A-Za-zＡ-Ｚａ-ｚ][^）]*）"
    )
    private val controlCharacters = Regex("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F\\u007F]")
    private val whitespace = Regex("\\s+")
    private val readingMetadata = Regex("[（(].*")

    fun normalize(raw: String): String {
        return raw
            .replace(controlCharacters, " ")
            .replace(speakerPrefix, "")
            .replace(inlineLatinTranslation, "")
            .replace("~~", "")
            .replace(whitespace, " ")
            .trim()
    }

    /**
     * Removes notation used by the learning UI, but deliberately does not
     * erase a leading/trailing hyphen. The hyphen carries semantic information
     * in KANJIDIC and is inspected by [isStandaloneKanjiReading].
     */
    fun normalizeKanjiReading(raw: String): String {
        return raw
            .replace("~", "")
            .replace(".", "")
            .replace("・", "")
            .replace(readingMetadata, "")
            .trim()
    }

    /**
     * Returns true only for a complete kana pronunciation of one Kanji.
     * KANJIDIC uses a hyphen to mark a prefix/suffix fragment, so values such
     * as `-と` and `-り` are intentionally rejected rather than converted to
     * misleading standalone readings.
     */
    fun isStandaloneKanjiReading(raw: String): Boolean {
        // KANJIDIC hyphens mark prefix/suffix fragments and dots mark
        // okurigana boundaries. Both forms describe word-level readings, not
        // a complete pronunciation for an isolated Kanji.
        if (raw.contains('-') || raw.contains('.')) return false

        val normalized = normalizeKanjiReading(raw)
        return normalized.isNotEmpty() && normalized.all(::isKana)
    }

    /**
     * Chooses a precise pronunciation for an isolated Kanji.
     *
     * Ordinary Kun readings are preferred because they are the native
     * standalone readings learners expect. Kun fragments are skipped. On
     * readings are the deterministic fallback for Kanji whose data only has
     * Sino-Japanese readings.
     */
    fun selectStandaloneKanjiReading(
        kunReadings: List<String>,
        onReadings: List<String>
    ): String? {
        return (kunReadings.asSequence() + onReadings.asSequence())
            .filter(::isStandaloneKanjiReading)
            .map(::normalizeKanjiReading)
            .distinct()
            .firstOrNull()
    }

    private fun isKana(character: Char): Boolean {
        return character in '\u3040'..'\u309F' ||
            character in '\u30A0'..'\u30FF' ||
            character == 'ー'
    }
}

/**
 * Semantic TTS request used when visible Kanji and spoken pronunciation differ.
 * The Android implementation currently speaks [pronunciation] directly for
 * maximum engine compatibility; [displayText] remains the source text whose
 * pronunciation was resolved.
 */
data class JapaneseSpeechRequest(
    val displayText: String,
    val pronunciation: String? = null,
    val furigana: FuriganaString? = null,
    val kunReadings: List<String> = emptyList(),
    val onReadings: List<String> = emptyList(),
    val context: JapaneseSpeechContext = JapaneseSpeechContext.Auto,
    val language: String = "ja-JP"
)

fun JapaneseSpeechRequest.normalized(): JapaneseSpeechRequest {
    return copy(
        displayText = JapaneseSpeechText.normalize(displayText),
        pronunciation = pronunciation?.let(JapaneseSpeechText::normalize),
        kunReadings = kunReadings.map(JapaneseSpeechText::normalizeKanjiReading),
        onReadings = onReadings.map(JapaneseSpeechText::normalizeKanjiReading)
    )
}
