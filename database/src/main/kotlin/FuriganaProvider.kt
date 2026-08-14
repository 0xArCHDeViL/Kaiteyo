import export.json.FuriganaElement
import parser.JMDictElementType
import parser.JMDictFuriganaRubyItem
import parser.JMdictFuriganaParser
import parser.JMdictItem
import parser.JishoOpenFuriganaParser

private data class FuriganaExpressionReading(
    val kanji: String,
    val kana: String
)

private data class FuriganaExpression(
    val elements: List<JMDictFuriganaRubyItem>
)

class FuriganaProvider(
    requestedReadings: Set<Pair<String, String>>
) {

    companion object {
        fun fromJMdictItems(items: Iterable<JMdictItem>): FuriganaProvider = FuriganaProvider(
            requestedReadings = buildSet {
                items.forEach { item ->
                    val kanji = item.elements
                        .filter { it.type == JMDictElementType.Kanji }
                        .map { it.expression }
                    val kana = item.elements
                        .filter { it.type == JMDictElementType.Reading }
                        .map { it.expression }
                    kanji.forEach { surface ->
                        kana.forEach { reading -> add(surface to reading) }
                    }
                }
            }
        )
    }

    private val requestedKeys = requestedReadings
        .mapTo(HashSet(requestedReadings.size)) { (kanji, kana) ->
            FuriganaExpressionReading(kanji, kana)
        }

    private val readingsMap: Map<FuriganaExpressionReading, FuriganaExpression> by lazy {
        buildMap {
            if (ProjectData.jishoOpenFuriganaFile.isFile) {
                JishoOpenFuriganaParser.forEach(ProjectData.jishoOpenFuriganaFile) { item ->
                    val key = FuriganaExpressionReading(item.surface, item.reading)
                    if (key in requestedKeys) put(key, FuriganaExpression(item.segments))
                }
            } else if (ProjectData.furiganaFile.isFile) {
                JMdictFuriganaParser.forEach(ProjectData.furiganaFile) { item ->
                    val key = FuriganaExpressionReading(item.kanjiExpression, item.kanaExpression)
                    if (key in requestedKeys) put(key, FuriganaExpression(item.furigana))
                }
            }
        }
    }

    fun getFuriganaForReading(kanjiReading: String, kanaReading: String): List<FuriganaElement>? {
        val reading = FuriganaExpressionReading(kanjiReading, kanaReading)
        return readingsMap[reading]?.elements?.map { FuriganaElement(it.ruby, it.rt) }
    }

}