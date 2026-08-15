package parser

import export.db.Vocab_entity
import java.io.File
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamReader

internal data class StreamingJMdictEntry(
    val entrySequence: Long,
    val kanji: List<StreamingKanjiElement>,
    val readings: List<StreamingReadingElement>,
    val senses: List<StreamingSense>
)

internal data class StreamingKanjiElement(
    val expression: String,
    val information: List<String>,
    val priorities: List<String>
)

internal data class StreamingReadingElement(
    val expression: String,
    val noKanji: Boolean,
    val restrictions: List<String>,
    val information: List<String>,
    val priorities: List<String>
)

internal data class StreamingSense(
    val kanjiRestrictions: List<String>,
    val readingRestrictions: List<String>,
    val crossReferences: List<String>,
    val antonyms: List<String>,
    val partsOfSpeech: List<String>,
    val fields: List<String>,
    val miscellaneous: List<String>,
    val dialects: List<String>,
    val glosses: List<StreamingGloss>,
    val information: List<String>,
    val examples: List<StreamingExample>
)

internal data class StreamingGloss(
    val language: String?,
    val text: String,
    val type: String?
)

internal data class StreamingExample(
    val sourceId: Long,
    val sourceType: String,
    val text: String,
    val japanese: String,
    val english: String
)

internal object StreamingJMdictParser {

    private const val XmlNamespace = "http://www.w3.org/XML/1998/namespace"

    fun forEachEntry(
        file: File,
        onEntry: (StreamingJMdictEntry) -> Unit
    ) {
        file.inputStream().buffered().use { input ->
            val reader = factory().createXMLStreamReader(input)
            reader.use {
                while (it.hasNext()) {
                    if (it.eventType == XMLStreamConstants.START_ELEMENT && it.localName == "entry") {
                        onEntry(readEntry(it))
                    }
                    it.next()
                }
            }
        }
    }

    fun forEachSupportedEntry(
        file: File,
        wordsPool: Set<Long>,
        onEntry: (StreamingJMdictEntry) -> Unit
    ) {
        forEachEntry(file) { entry ->
            if (entry.entrySequence in wordsPool) onEntry(entry)
        }
    }

    fun readEntities(file: File): List<Vocab_entity> = file.bufferedReader().useLines { lines ->
        val entityRegex = "<!ENTITY (.*) \"(.*)\">".toRegex()
        lines.mapNotNull { entityRegex.find(it) }
            .map { Vocab_entity(it.groupValues[1], it.groupValues[2]) }
            .distinct()
            .toList()
    }

    private fun factory(): XMLInputFactory {
        // JMdict uses internal entity definitions for POS/gloss labels. The
        // JDK default of 64k expansions rejects a valid dictionary before the
        // supported-entry filter can discard unused entries. External entities
        // remain disabled, so only repository-owned internal DTD data expands.
        System.setProperty("jdk.xml.entityExpansionLimit", "0")
        System.setProperty("jdk.xml.totalEntitySizeLimit", "0")
        return XMLInputFactory.newFactory().apply {
            setProperty(XMLInputFactory.IS_COALESCING, true)
        setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true)
        setProperty(XMLInputFactory.SUPPORT_DTD, true)
            setProperty("javax.xml.stream.isSupportingExternalEntities", false)
        }
    }

    private fun readEntry(reader: XMLStreamReader): StreamingJMdictEntry {
        var entrySequence: Long? = null
        val kanji = mutableListOf<StreamingKanjiElement>()
        val readings = mutableListOf<StreamingReadingElement>()
        val senses = mutableListOf<StreamingSense>()

        while (reader.hasNext()) {
            when {
                reader.eventType == XMLStreamConstants.START_ELEMENT && reader.localName == "ent_seq" -> {
                    entrySequence = reader.elementText.toLong()
                }
                reader.eventType == XMLStreamConstants.START_ELEMENT && reader.localName == "k_ele" -> {
                    kanji += readKanji(reader)
                }
                reader.eventType == XMLStreamConstants.START_ELEMENT && reader.localName == "r_ele" -> {
                    readings += readReading(reader)
                }
                reader.eventType == XMLStreamConstants.START_ELEMENT && reader.localName == "sense" -> {
                    senses += readSense(reader)
                }
                reader.eventType == XMLStreamConstants.END_ELEMENT && reader.localName == "entry" -> {
                    break
                }
            }
            reader.next()
        }

        return StreamingJMdictEntry(
            entrySequence = requireNotNull(entrySequence) { "JMdict entry is missing ent_seq" },
            kanji = kanji,
            readings = readings,
            senses = senses
        )
    }

    private fun readKanji(reader: XMLStreamReader): StreamingKanjiElement {
        var expression: String? = null
        val information = mutableListOf<String>()
        val priorities = mutableListOf<String>()
        readUntilEnd(reader, "k_ele") {
            when (reader.localName) {
                "keb" -> expression = reader.elementText
                "ke_inf" -> information += reader.elementText
                "ke_pri" -> priorities += reader.elementText
            }
        }
        return StreamingKanjiElement(
            expression = requireNotNull(expression) { "k_ele is missing keb" },
            information = information,
            priorities = priorities
        )
    }

    private fun readReading(reader: XMLStreamReader): StreamingReadingElement {
        var expression: String? = null
        var noKanji = false
        val restrictions = mutableListOf<String>()
        val information = mutableListOf<String>()
        val priorities = mutableListOf<String>()
        readUntilEnd(reader, "r_ele") {
            when (reader.localName) {
                "reb" -> expression = reader.elementText
                "re_nokanji" -> {
                    reader.elementText
                    noKanji = true
                }
                "re_restr" -> restrictions += reader.elementText
                "re_inf" -> information += reader.elementText
                "re_pri" -> priorities += reader.elementText
            }
        }
        return StreamingReadingElement(
            expression = requireNotNull(expression) { "r_ele is missing reb" },
            noKanji = noKanji,
            restrictions = restrictions,
            information = information,
            priorities = priorities
        )
    }

    private fun readSense(reader: XMLStreamReader): StreamingSense {
        val kanjiRestrictions = mutableListOf<String>()
        val readingRestrictions = mutableListOf<String>()
        val crossReferences = mutableListOf<String>()
        val antonyms = mutableListOf<String>()
        val partsOfSpeech = mutableListOf<String>()
        val fields = mutableListOf<String>()
        val miscellaneous = mutableListOf<String>()
        val dialects = mutableListOf<String>()
        val glosses = mutableListOf<StreamingGloss>()
        val information = mutableListOf<String>()
        val examples = mutableListOf<StreamingExample>()

        readUntilEnd(reader, "sense") {
            when (reader.localName) {
                "stagk" -> kanjiRestrictions += reader.elementText
                "stagr" -> readingRestrictions += reader.elementText
                "xref" -> crossReferences += reader.elementText
                "ant" -> antonyms += reader.elementText
                "pos" -> partsOfSpeech += reader.elementText
                "field" -> fields += reader.elementText
                "misc" -> miscellaneous += reader.elementText
                "dial" -> dialects += reader.elementText
                "gloss" -> glosses += readGloss(reader)
                "s_inf" -> information += reader.elementText
                "example" -> examples += readExample(reader)
            }
        }

        return StreamingSense(
            kanjiRestrictions = kanjiRestrictions,
            readingRestrictions = readingRestrictions,
            crossReferences = crossReferences,
            antonyms = antonyms,
            partsOfSpeech = partsOfSpeech,
            fields = fields,
            miscellaneous = miscellaneous,
            dialects = dialects,
            glosses = glosses,
            information = information,
            examples = examples
        )
    }

    private fun readGloss(reader: XMLStreamReader): StreamingGloss {
        val language = reader.getAttributeValue(XmlNamespace, "lang")
            ?: reader.getAttributeValue(null, "xml:lang")
        val type = reader.getAttributeValue(null, "g_type").takeIf { !it.isNullOrEmpty() }
        val normalizedLanguage = language
            ?.takeIf { it.isNotEmpty() && it != "en" && it != "eng" }
        return StreamingGloss(
            language = normalizedLanguage,
            text = reader.elementText,
            type = type
        )
    }

    private fun readExample(reader: XMLStreamReader): StreamingExample {
        var sourceId: Long? = null
        var sourceType = ""
        var text = ""
        var japanese = ""
        var english = ""
        readUntilEnd(reader, "example") {
            when (reader.localName) {
                "ex_srce" -> {
                    sourceType = reader.getAttributeValue(null, "exsrc_type") ?: ""
                    sourceId = reader.elementText.toLong()
                }
                "ex_text" -> text = reader.elementText
                "ex_sent" -> {
                    val language = reader.getAttributeValue(XmlNamespace, "lang")
                        ?: reader.getAttributeValue(null, "xml:lang")
                    when (language) {
                        "jpn" -> japanese = reader.elementText
                        "eng" -> english = reader.elementText
                        else -> reader.elementText
                    }
                }
            }
        }
        return StreamingExample(
            sourceId = requireNotNull(sourceId) { "example is missing ex_srce" },
            sourceType = sourceType,
            text = text,
            japanese = japanese,
            english = english
        )
    }

    private inline fun readUntilEnd(
        reader: XMLStreamReader,
        element: String,
        consumeStartElement: () -> Unit
    ) {
        reader.next()
        while (reader.hasNext()) {
            if (reader.eventType == XMLStreamConstants.END_ELEMENT && reader.localName == element) return
            if (reader.eventType == XMLStreamConstants.START_ELEMENT) consumeStartElement()
            reader.next()
        }
        error("Unclosed JMdict element <$element>")
    }

    private fun XMLStreamReader.use(block: (XMLStreamReader) -> Unit) {
        try {
            block(this)
        } finally {
            close()
        }
    }
}
