package ua.syt0r.kanji.core.grammar

/** A display segment from a grammar formula. */
data class GrammarMarkupSegment(
    val text: String,
    val struck: Boolean,
)

object GrammarMarkup {

    fun parse(text: String): List<GrammarMarkupSegment> {
        if (text.isEmpty()) return emptyList()

        val segments = mutableListOf<GrammarMarkupSegment>()
        var cursor = 0
        var struck = false
        while (cursor < text.length) {
            if (text.startsWith("~~", cursor)) {
                struck = !struck
                cursor += 2
                continue
            }

            val markerIndex = text.indexOf("~~", cursor)
            val end = if (markerIndex >= 0) markerIndex else text.length
            if (end > cursor) {
                segments += GrammarMarkupSegment(
                    text = text.substring(cursor, end),
                    struck = struck,
                )
            }
            cursor = end
        }

        // Invalid content must never make the marker itself visible.
        if (struck && segments.isNotEmpty()) {
            return segments.map { it.copy(struck = it.struck) }
        }
        return segments
    }

    fun plainText(text: String): String = parse(text)
        .joinToString(separator = "") { it.text }
        .replace("~", "")
        .trim()

    fun japaneseSpeechText(text: String): String = plainText(text)
        .lineSequence()
        .map { line ->
            val separator = line.indexOfFirst { it == '：' || it == ':' }
            if (separator in 1..4) line.substring(separator + 1).trim() else line.trim()
        }
        .filter { it.isNotBlank() }
        .joinToString(separator = " ")
}
