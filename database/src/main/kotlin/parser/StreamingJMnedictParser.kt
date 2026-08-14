package parser

import java.io.File
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamReader

data class StreamingJMnedictEntry(
    val entrySequence: Long,
    val kanji: List<String>,
    val readings: List<String>,
    val nameTypes: List<String>,
    val englishTranslations: List<String>
)

object StreamingJMnedictParser {

    fun forEach(file: File, onEntry: (StreamingJMnedictEntry) -> Unit) {
        file.inputStream().buffered().use { input ->
            val reader: XMLStreamReader = factory().createXMLStreamReader(input)
            try {
                while (reader.hasNext()) {
                    if (reader.eventType == XMLStreamConstants.START_ELEMENT && reader.localName == "entry") {
                        onEntry(readEntry(reader))
                    }
                    reader.next()
                }
            } finally {
                reader.close()
            }
        }
    }

    private fun readEntry(reader: XMLStreamReader): StreamingJMnedictEntry {
        var sequence: Long? = null
        val kanji = mutableListOf<String>()
        val readings = mutableListOf<String>()
        val nameTypes = mutableListOf<String>()
        val translations = mutableListOf<String>()

        while (reader.hasNext()) {
            when {
                reader.eventType == XMLStreamConstants.START_ELEMENT && reader.localName == "ent_seq" -> {
                    sequence = reader.elementText.toLongOrNull()
                }
                reader.eventType == XMLStreamConstants.START_ELEMENT && reader.localName == "keb" -> {
                    kanji += reader.elementText
                }
                reader.eventType == XMLStreamConstants.START_ELEMENT && reader.localName == "reb" -> {
                    readings += reader.elementText
                }
                reader.eventType == XMLStreamConstants.START_ELEMENT && reader.localName == "name_type" -> {
                    nameTypes += reader.elementText
                }
                reader.eventType == XMLStreamConstants.START_ELEMENT && reader.localName == "trans_det" -> {
                    val language = reader.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang")
                    val value = reader.elementText
                    if (language.isNullOrEmpty() || language == "eng") translations += value
                }
                reader.eventType == XMLStreamConstants.END_ELEMENT && reader.localName == "entry" -> break
            }
            reader.next()
        }

        return StreamingJMnedictEntry(
            entrySequence = requireNotNull(sequence) { "JMnedict entry is missing ent_seq" },
            kanji = kanji,
            readings = readings,
            nameTypes = nameTypes,
            englishTranslations = translations
        )
    }

    private fun factory(): XMLInputFactory = XMLInputFactory.newFactory().apply {
        setProperty(XMLInputFactory.IS_COALESCING, true)
        setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true)
        setProperty(XMLInputFactory.SUPPORT_DTD, true)
        setProperty("javax.xml.stream.isSupportingExternalEntities", false)
    }
}
