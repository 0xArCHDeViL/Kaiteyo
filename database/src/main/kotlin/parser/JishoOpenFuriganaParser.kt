package parser

import java.io.File

data class JishoOpenFuriganaItem(
    val jmdictId: Long,
    val surface: String,
    val reading: String,
    val segments: List<JMDictFuriganaRubyItem>
)

object JishoOpenFuriganaParser {

    /**
     * Streams H. Lorenzi's derived JMdict furigana file without retaining its
     * hundreds of thousands of lines in memory.
     */
    fun forEach(file: File, onItem: (JishoOpenFuriganaItem) -> Unit) {
        file.bufferedReader().useLines { lines ->
            lines.forEach { rawLine ->
                val line = rawLine.trim()
                if (line.isEmpty() || line.startsWith('#')) return@forEach

                val columns = line.split(';', limit = 3)
                if (columns.size != 3) return@forEach

                val id = columns[0].toLongOrNull() ?: return@forEach
                val surface = columns[1].replace(".", "")
                val reading = columns[2].replace(".", "")
                if (surface.isEmpty() || reading.isEmpty()) return@forEach

                onItem(
                    JishoOpenFuriganaItem(
                        jmdictId = id,
                        surface = surface,
                        reading = reading,
                        segments = toSegments(columns[1], columns[2])
                    )
                )
            }
        }
    }

    private fun toSegments(surface: String, reading: String): List<JMDictFuriganaRubyItem> {
        val surfaceSegments = surface.split('.')
        val readingSegments = reading.split('.')
        if (surfaceSegments.size != readingSegments.size) {
            val plainSurface = surface.replace(".", "")
            val plainReading = reading.replace(".", "")
            return listOf(
                JMDictFuriganaRubyItem(
                    ruby = plainSurface,
                    rt = if (plainReading == plainSurface) null else plainReading
                )
            )
        }

        return surfaceSegments.zip(readingSegments).map { (ruby, annotation) ->
            JMDictFuriganaRubyItem(
                ruby = ruby,
                rt = if (annotation == ruby) null else annotation
            )
        }
    }
}
