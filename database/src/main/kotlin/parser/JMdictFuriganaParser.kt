package parser

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.File

data class JMdictFuriganaItem(
    @SerializedName("text")
    val kanjiExpression: String,

    @SerializedName("reading")
    val kanaExpression: String,

    @SerializedName("furigana")
    val furigana: List<JMDictFuriganaRubyItem>
)

data class JMDictFuriganaRubyItem(
    val ruby: String,
    val rt: String? = null
)

object JMdictFuriganaParser {

    private val gson = Gson()
    private val itemType = object : TypeToken<JMdictFuriganaItem>() {}.type

    /**
     * Streams the furigana source one item at a time. The source file is large,
     * so database export must not deserialize its complete array before filtering
     * down to the readings used by Kaiteyo's shipped vocabulary.
     */
    fun forEach(file: File, onItem: (JMdictFuriganaItem) -> Unit) {
        JsonReader(file.reader().buffered()).use { reader ->
            reader.beginArray()
            while (reader.hasNext()) {
                onItem(gson.fromJson(reader, itemType))
            }
            reader.endArray()
        }
    }

    /**
     * Compatibility helper for tooling that explicitly needs the full source.
     * Application-database export must use [forEach] instead.
     */
    fun parse(file: File): List<JMdictFuriganaItem> = buildList {
        forEach(file, ::add)
    }
}
