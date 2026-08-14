package parser

import FuriganaProvider
import ProjectData
import com.google.gson.Gson
import export.db.*
import export.json.FuriganaElement
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File

private data class DatabaseVocabSingleEntry(
    val entry: Vocab_entry,
    val kanjiElements: MutableList<Vocab_kanji_element> = mutableListOf(),
    val kanjiInformation: MutableList<Vocab_kanji_information> = mutableListOf(),
    val kanjiPriorities: MutableList<Vocab_kanji_priority> = mutableListOf(),
    val kanaElements: MutableList<Vocab_kana_element> = mutableListOf(),
    val kanaRestrictions: MutableList<Vocab_kana_restriction> = mutableListOf(),
    val kanaInformation: MutableList<Vocab_kana_information> = mutableListOf(),
    val kanaPriorities: MutableList<Vocab_kana_priority> = mutableListOf(),
    val senses: MutableList<Vocab_sense> = mutableListOf(),
    val senseKanjiRestrictions: MutableList<Vocab_sense_kanji_restriction> = mutableListOf(),
    val senseReadingRestrictions: MutableList<Vocab_sense_kana_restriction> = mutableListOf(),
    val sensePartsOfSpeech: MutableList<Vocab_sense_part_of_speech> = mutableListOf(),
    val senseCrossReferences: MutableList<Vocab_sense_cross_reference> = mutableListOf(),
    val senseAntonyms: MutableList<Vocab_sense_antonym> = mutableListOf(),
    val senseFields: MutableList<Vocab_sense_field> = mutableListOf(),
    val senseMiscellaneous: MutableList<Vocab_sense_miscellaneous> = mutableListOf(),
    val senseDialects: MutableList<Vocab_sense_dialect> = mutableListOf(),
    val senseGlosses: MutableList<Vocab_sense_gloss> = mutableListOf(),
    val senseInformation: MutableList<Vocab_sense_information> = mutableListOf(),
    val senseExample: MutableList<Vocab_sense_example> = mutableListOf(),
    val furigana: MutableList<Vocab_furigana> = mutableListOf()
)

object CompositeJMdictParser {

    fun parse(
        wordsPool: Set<Long>,
        file: File = ProjectData.jMdictWithExamplesFile,
    ): DatabaseVocabData {
        val idGenerator = IdGenerator()
        val furiganaProvider = FuriganaProvider(
            requestedReadings = collectRequestedFuriganaReadings(file, wordsPool)
        )
        val gson = Gson()
        val entities = StreamingJMdictParser.readEntities(file)
        val entries = mutableListOf<DatabaseVocabSingleEntry>()

        StreamingJMdictParser.forEachSupportedEntry(file, wordsPool) { entry ->
            entries += parseStreamingEntry(entry, idGenerator, furiganaProvider, gson)
        }

        return entries.run {
            DatabaseVocabData(
                entries = map { it.entry },
                kanjiElements = flatMap { it.kanjiElements },
                kanjiInformation = flatMap { it.kanjiInformation },
                kanjiPriorities = flatMap { it.kanjiPriorities },
                kanaElements = flatMap { it.kanaElements },
                kanaRestrictions = flatMap { it.kanaRestrictions },
                kanaInformation = flatMap { it.kanaInformation },
                kanaPriorities = flatMap { it.kanaPriorities },
                senses = flatMap { it.senses },
                senseKanjiRestrictions = flatMap { it.senseKanjiRestrictions },
                senseReadingRestrictions = flatMap { it.senseReadingRestrictions },
                sensePartsOfSpeech = flatMap { it.sensePartsOfSpeech },
                senseCrossReferences = flatMap { it.senseCrossReferences },
                senseAntonyms = flatMap { it.senseAntonyms },
                senseFields = flatMap { it.senseFields },
                senseMiscellaneous = flatMap { it.senseMiscellaneous },
                senseDialects = flatMap { it.senseDialects },
                senseGlosses = flatMap { it.senseGlosses }
                    .filter { it.language == null }
                    .distinct(),
                senseInformation = flatMap { it.senseInformation },
                senseExample = flatMap { it.senseExample },
                entities = entities,
                furigana = flatMap { it.furigana }.distinct()
            )
        }
    }

    const val ElementIdGeneratorKey = "ele"

    /**
     * The supplemental furigana source contains hundreds of thousands of
     * expressions. Collect only the surface/reading pairs represented by the
     * vocabulary database, then let [FuriganaProvider] stream-filter the source.
     */
    private fun collectRequestedFuriganaReadings(
        file: File,
        wordsPool: Set<Long>
    ): Set<Pair<String, String>> = buildSet {
        StreamingJMdictParser.forEachSupportedEntry(file, wordsPool) { entry ->
            entry.kanji.forEach { kanji ->
                entry.readings.forEach { kana ->
                    add(kanji.expression to kana.expression)
                }
            }
        }
    }

    private fun parseStreamingEntry(
        entry: StreamingJMdictEntry,
        idGenerator: IdGenerator,
        furiganaProvider: FuriganaProvider,
        gson: Gson
    ): DatabaseVocabSingleEntry {
        val dbEntry = DatabaseVocabSingleEntry(entry = Vocab_entry(entry.entrySequence))

        entry.kanji.forEach { kanji ->
            val elementId = idGenerator.nextId(ElementIdGeneratorKey)
            kanji.information.forEach { information ->
                dbEntry.kanjiInformation += Vocab_kanji_information(
                    elementId,
                    information.removeDataSurroundings()
                )
            }
            kanji.priorities.forEach { priority ->
                dbEntry.kanjiPriorities += Vocab_kanji_priority(elementId, priority)
            }
            val numericPriority = kanji.priorities
                .map { JMDictPriority.fromJMDictValue(it).asNumber() }
                .minOrNull()
                ?.toLong()
            dbEntry.kanjiElements += Vocab_kanji_element(
                elementId,
                entry.entrySequence,
                kanji.expression,
                numericPriority
            )
        }

        entry.readings.forEach { reading ->
            val elementId = idGenerator.nextId(ElementIdGeneratorKey)
            reading.restrictions.forEach { restriction ->
                dbEntry.kanaRestrictions += Vocab_kana_restriction(elementId, restriction)
            }
            reading.information.forEach { information ->
                dbEntry.kanaInformation += Vocab_kana_information(
                    elementId,
                    information.removeDataSurroundings()
                )
            }
            reading.priorities.forEach { priority ->
                dbEntry.kanaPriorities += Vocab_kana_priority(elementId, priority)
            }
            val numericPriority = reading.priorities
                .map { JMDictPriority.fromJMDictValue(it).asNumber() }
                .minOrNull()
                ?.toLong()
            dbEntry.kanaElements += Vocab_kana_element(
                elementId,
                entry.entrySequence,
                reading.expression,
                if (reading.noKanji) 1L else 0L,
                numericPriority
            )
        }

        entry.senses.forEach { sense ->
            val senseId = idGenerator.nextId("sense")
            dbEntry.senses += Vocab_sense(senseId, entry.entrySequence)
            sense.kanjiRestrictions.forEach {
                dbEntry.senseKanjiRestrictions += Vocab_sense_kanji_restriction(senseId, it)
            }
            sense.readingRestrictions.forEach {
                dbEntry.senseReadingRestrictions += Vocab_sense_kana_restriction(senseId, it)
            }
            sense.crossReferences.forEach {
                dbEntry.senseCrossReferences += Vocab_sense_cross_reference(senseId, it)
            }
            sense.antonyms.forEach {
                dbEntry.senseAntonyms += Vocab_sense_antonym(senseId, it)
            }
            sense.partsOfSpeech.forEach {
                dbEntry.sensePartsOfSpeech += Vocab_sense_part_of_speech(
                    senseId,
                    it.removeDataSurroundings()
                )
            }
            sense.fields.forEach {
                dbEntry.senseFields += Vocab_sense_field(senseId, it.removeDataSurroundings())
            }
            sense.miscellaneous.forEach {
                dbEntry.senseMiscellaneous += Vocab_sense_miscellaneous(
                    senseId,
                    it.removeDataSurroundings()
                )
            }
            sense.dialects.forEach {
                dbEntry.senseDialects += Vocab_sense_dialect(senseId, it.removeDataSurroundings())
            }
            sense.glosses.forEach { gloss ->
                dbEntry.senseGlosses += Vocab_sense_gloss(
                    sense_id = senseId,
                    gloss_text = gloss.text,
                    language = gloss.language,
                    type = gloss.type
                )
            }
            sense.information.forEach {
                dbEntry.senseInformation += Vocab_sense_information(senseId, it)
            }
            sense.examples
                .filter { it.sourceType == "tat" && it.japanese.isNotEmpty() && it.english.isNotEmpty() }
                .forEach { example ->
                    dbEntry.senseExample += Vocab_sense_example(
                        senseId,
                        example.text,
                        example.sourceId
                    )
                }
        }

        dbEntry.addFurigana(furiganaProvider, gson)
        return dbEntry
    }

    private fun String.removeDataSurroundings() = removePrefix("&").removeSuffix(";")

    private fun parseEntry(
        entryElement: Element,
        idGenerator: IdGenerator,
        furiganaProvider: FuriganaProvider,
        gson: Gson
    ): DatabaseVocabSingleEntry {
        val entryId = entryElement.selectFirst("ent_seq")!!.text().toLong()

        val dbEntry = DatabaseVocabSingleEntry(
            entry = Vocab_entry(entryId)
        )

        fun String.removeDataSurroundings() = removePrefix("&").removeSuffix(";")

        entryElement.select("k_ele").forEach {
            val elementId = idGenerator.nextId(ElementIdGeneratorKey)
            val reading = it.selectFirst("keb")!!.text()

            it.select("ke_inf").forEach {
                val infoValue = it.text().removeDataSurroundings()
                dbEntry.kanjiInformation.add(Vocab_kanji_information(elementId, infoValue))
            }

            val elementPriorities = it.select("ke_pri").map {
                val value = it.text()
                Vocab_kanji_priority(elementId, value)
            }
            dbEntry.kanjiPriorities.addAll(elementPriorities)

            val priority = elementPriorities
                .minOfOrNull { JMDictPriority.fromJMDictValue(it.priority).asNumber() }
                ?.toLong()

            dbEntry.kanjiElements.add(Vocab_kanji_element(elementId, entryId, reading, priority))
        }

        entryElement.select("r_ele").forEach {
            val elementId = idGenerator.nextId(ElementIdGeneratorKey)
            val reading = it.selectFirst("reb")!!.text()
            val noKanji = it.selectFirst("re_nokanji")?.let { 1L } ?: 0L


            it.select("re_restr").forEach {
                val kanjiReading = it.text()
                dbEntry.kanaRestrictions.add(Vocab_kana_restriction(elementId, kanjiReading))
            }

            it.select("re_inf").forEach {
                val information = it.text().removeDataSurroundings()
                dbEntry.kanaInformation.add(Vocab_kana_information(elementId, information))
            }

            val elementPriorities = it.select("re_pri").map {
                val value = it.text()
                Vocab_kana_priority(elementId, value)
            }
            dbEntry.kanaPriorities.addAll(elementPriorities)

            val priority = elementPriorities
                .minOfOrNull { JMDictPriority.fromJMDictValue(it.priority).asNumber() }
                ?.toLong()

            dbEntry.kanaElements.add(Vocab_kana_element(elementId, entryId, reading, noKanji, priority))
        }

        entryElement.select("sense").forEach {
            val senseId = idGenerator.nextId("sense")
            dbEntry.senses.add(Vocab_sense(senseId, entryId))

            it.select("stagk").forEach {
                dbEntry.senseKanjiRestrictions.add(Vocab_sense_kanji_restriction(senseId, it.text()))
            }

            it.select("stagr").forEach {
                dbEntry.senseReadingRestrictions.add(Vocab_sense_kana_restriction(senseId, it.text()))
            }

            it.select("xref").forEach {
                dbEntry.senseCrossReferences.add(Vocab_sense_cross_reference(senseId, it.text()))
            }

            it.select("ant").forEach {
                dbEntry.senseAntonyms.add(Vocab_sense_antonym(senseId, it.text()))
            }

            it.select("pos").forEach {
                dbEntry.sensePartsOfSpeech.add(Vocab_sense_part_of_speech(senseId, it.text().removeDataSurroundings()))
            }

            it.select("field").forEach {
                dbEntry.senseFields.add(Vocab_sense_field(senseId, it.text().removeDataSurroundings()))
            }

            it.select("misc").forEach {
                dbEntry.senseMiscellaneous.add(Vocab_sense_miscellaneous(senseId, it.text().removeDataSurroundings()))
            }

            it.select("dial").forEach {
                dbEntry.senseDialects.add(Vocab_sense_dialect(senseId, it.text().removeDataSurroundings()))
            }

            it.select("gloss").forEach {
                dbEntry.senseGlosses.add(
                    Vocab_sense_gloss(
                        sense_id = senseId,
                        gloss_text = it.text(),
                        language = it.attr("xml:lang").takeIf { it.isNotEmpty() },
                        type = it.attr("g_type")
                    )
                )
            }

            it.select("s_inf").forEach {
                dbEntry.senseInformation.add(Vocab_sense_information(senseId, it.text()))
            }

            it.select("example").forEach {
                val tatoebaId = it.selectFirst("ex_srce")!!
                    .takeIf { it.attr("exsrc_type") == "tat" }!!
                    .text()
                    .toLong()
                val text = it.selectFirst("ex_text")!!.text()
                val sentences = it.select("ex_sent")
                val japaneseSentence = sentences.first { it.attr("xml:lang") == "jpn" }.text()
                val translation = sentences.first { it.attr("xml:lang") == "eng" }.text()
                dbEntry.senseExample.add(Vocab_sense_example(senseId, text, tatoebaId))
            }
        }

        dbEntry.addFurigana(furiganaProvider, gson)

        return dbEntry
    }

}

private fun DatabaseVocabSingleEntry.addFurigana(furiganaProvider: FuriganaProvider, gson: Gson) {
    kanjiElements.flatMap { kanji -> kanaElements.map { kanji.reading to it.reading } }.forEach { (kanji, kana) ->
        val furiganaValue = furiganaProvider.getFuriganaForReading(kanji, kana)
            ?.map { it.toDbEntity() }
            ?.let { gson.toJson(it) }
        if (furiganaValue != null) {
            furigana.add(Vocab_furigana(kanji, kana, furiganaValue))
        }
    }
}

private fun FuriganaElement.toDbEntity() = DatabaseFuriganaItem(text, annotation)

class IdGenerator {

    private val idMap = mutableMapOf<String, Long>()

    fun nextId(key: String): Long {
        val current = idMap[key] ?: 0
        return (current + 1).also { idMap[key] = it }
    }

}