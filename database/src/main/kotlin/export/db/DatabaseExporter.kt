package export.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import java.util.Locale

class DatabaseExporter(
    file: File,
    schemaVersion: Int,
    packRevision: Int,
) {

    private val driver: JdbcSqliteDriver
    private val database: KanjiDojoData

    init {
        if (file.exists()) file.delete()

        driver = JdbcSqliteDriver("jdbc:sqlite:${file.absolutePath}")
        KanjiDojoData.Schema.create(driver)
        database = KanjiDojoData(driver)
        driver.execute(
            identifier = null,
            sql = "PRAGMA user_version = $schemaVersion;",
            parameters = 0
        )
        driver.execute(
            identifier = null,
            sql = "PRAGMA application_id = $packRevision;",
            parameters = 0
        )
    }

    fun writeStrokes(characterToStrokes: List<DatabaseCharacterStrokeData>) = database.transaction {
        characterToStrokes.forEach { (char, strokes) ->
            strokes.forEachIndexed { index, path ->
                database.lettersQueries.insertCharacterStroke(
                    Character_stroke(
                        character = char,
                        stroke_number = index.toLong(),
                        stroke_path = path
                    )
                )
            }
        }
    }

    fun writeKanjiData(kanjiDataList: List<DatabaseKanjiData>) = database.transaction {
        kanjiDataList.forEach { kanjiData ->
            val kanji = kanjiData.kanji

            database.lettersQueries.insertKanjiData(
                Kanji_data(
                    kanji = kanji,
                    frequency = kanjiData.frequency?.toLong(),
                    variantFamily = kanjiData.variantFamily
                )
            )

            val readings = kanjiData.kunReadings.map { DatabaseKanjiReadingType.KUN to it } +
                    kanjiData.onReadings.map { DatabaseKanjiReadingType.ON to it }

            readings.forEach { (readingTypeEnum, readingStr) ->
                database.lettersQueries.insertKanjiReading(
                    Kanji_reading(
                        kanji = kanji,
                        reading_type = readingTypeEnum.value,
                        reading = readingStr
                    )
                )
            }

            kanjiData.meanings.forEachIndexed { priorityValue, meaningValue ->
                database.lettersQueries.insertKanjiMeaning(
                    Kanji_meaning(
                        kanji = kanji,
                        meaning = meaningValue,
                        priority = priorityValue.toLong()
                    )
                )
            }
        }
    }

    fun writeRadicals(radicals: List<DatabaseRadical>) = database.transaction {
        radicals.forEach {
            database.lettersQueries.insertRadical(
                Radical(radical = it.radical, strokesCount = it.strokes.toLong())
            )
        }
    }


    fun writeKanjiRadicals(data: List<DatabaseKanjiRadical>) = database.transaction {
        data.forEach {
            database.lettersQueries.insertKanjiRadical(
                Kanji_radical(
                    kanji = it.kanji,
                    radical = it.radical,
                    start_stroke = it.startPosition.toLong(),
                    strokes_count = it.strokesCount.toLong()
                )
            )
        }
    }


    fun writeKanjiClassifications(items: List<DatabaseKanjiClassification>) = database.transaction {
        items.forEach {
            database.lettersQueries.insertKanjiClassification(
                Kanji_classification(it.kanji, it.classification)
            )
        }
    }

    fun writeLetterVocabExamples(items: List<Letter_vocab_example>) {
        items.forEach { database.lettersQueries.insertLetterVocabExample(it) }
    }

    fun writeVocab(databaseVocabData: DatabaseVocabData) = database.transaction {
        databaseVocabData.apply {
            entries.forEach { database.vocabQueries.insert_vocab_entry(it) }
            kanjiElements.forEach { database.vocabQueries.insert_vocab_kanji_element(it) }
            kanjiInformation.forEach { database.vocabQueries.insert_vocab_kanji_information(it) }
            kanjiPriorities.forEach { database.vocabQueries.insert_vocab_kanji_priority(it) }
            kanaElements.forEach { database.vocabQueries.insert_vocab_kana_element(it) }
            kanaRestrictions.forEach { database.vocabQueries.insert_vocab_kana_restriction(it) }
            kanaInformation.forEach { database.vocabQueries.insert_vocab_kana_information(it) }
            kanaPriorities.forEach { database.vocabQueries.insert_vocab_kana_priority(it) }
            senses.forEach { database.vocabQueries.insert_vocab_sense(it) }
            senseKanjiRestrictions.forEach { database.vocabQueries.insert_vocab_sense_kanji_restriction(it) }
            senseReadingRestrictions.forEach { database.vocabQueries.insert_vocab_sense_kana_restriction(it) }
            sensePartsOfSpeech.forEach { database.vocabQueries.insert_vocab_sense_part_of_speech(it) }
            senseCrossReferences.forEach { database.vocabQueries.insert_vocab_sense_cross_reference(it) }
            senseAntonyms.forEach { database.vocabQueries.insert_vocab_sense_antonym(it) }
            senseFields.forEach { database.vocabQueries.insert_vocab_sense_field(it) }
            senseMiscellaneous.forEach { database.vocabQueries.insert_vocab_sense_miscellaneous(it) }
            senseDialects.forEach { database.vocabQueries.insert_vocab_sense_dialect(it) }
            senseGlosses.forEach { database.vocabQueries.insert_vocab_sense_gloss(it) }
            senseInformation.forEach { database.vocabQueries.insert_vocab_sense_information(it) }
            senseExample.forEach { database.vocabQueries.insert_vocab_sense_example(it) }
            entities.forEach { database.vocabQueries.insert_vocab_entity(it) }
            furigana.forEach { database.vocabQueries.insert_vocab_furigana(it) }
            writeVocabSearchIndex(this)
        }
    }

    private fun writeVocabSearchIndex(data: DatabaseVocabData) {
        val senseToEntry = data.senses.associate { it.id to it.entry_id }
        val tagsByEntry = HashMap<Long, MutableSet<String>>()

        fun insertValue(entryId: Long, field: String, value: String) {
            val cleanValue = value.trim()
            if (cleanValue.isEmpty()) return
            database.vocabQueries.insert_vocab_search_index(
                Vocab_search_index(
                    entry_id = entryId,
                    field_ = field,
                    value_ = cleanValue,
                    normalized = cleanValue.lowercase(Locale.ROOT)
                )
            )
        }

        fun insertReading(entryId: Long, value: String, field: String) {
            insertValue(entryId, field, value)
            insertValue(entryId, "romaji", value.searchRomaji())
        }

        fun insertTag(entryId: Long, value: String) {
            val tag = value.removePrefix("&").removeSuffix(";")
                .trim()
                .lowercase(Locale.ROOT)
            if (tag.isNotEmpty()) tagsByEntry.getOrPut(entryId) { LinkedHashSet() } += tag
        }

        data.kanjiElements.forEach { insertReading(it.entry_id, it.reading, "kanji") }
        data.kanaElements.forEach { insertReading(it.entry_id, it.reading, "kana") }
        data.kanjiPriorities.forEach { priority ->
            val entryId = data.kanjiElements.firstOrNull { it.element_id == priority.element_id }?.entry_id
                ?: return@forEach
            insertTag(entryId, priority.priority)
            if (priority.priority.startsWith("news") || priority.priority.startsWith("ichi") ||
                priority.priority.startsWith("spec") || priority.priority.startsWith("gai") ||
                priority.priority.startsWith("nf")
            ) insertTag(entryId, "common")
        }
        data.kanaPriorities.forEach { priority ->
            val entryId = data.kanaElements.firstOrNull { it.element_id == priority.element_id }?.entry_id
                ?: return@forEach
            insertTag(entryId, priority.priority)
            if (priority.priority.startsWith("news") || priority.priority.startsWith("ichi") ||
                priority.priority.startsWith("spec") || priority.priority.startsWith("gai") ||
                priority.priority.startsWith("nf")
            ) insertTag(entryId, "common")
        }

        data.senseGlosses.forEach { gloss ->
            if (gloss.language == null || gloss.language == "eng") {
                senseToEntry[gloss.sense_id]?.let { insertValue(it, "gloss", gloss.gloss_text) }
            }
        }
        data.sensePartsOfSpeech.forEach { value ->
            senseToEntry[value.sense_id]?.let { entryId ->
                insertTag(entryId, value.part_of_speech)
                EdrdgSearchTagMapper.canonicalPartOfSpeechTags(value.part_of_speech)
                    .forEach { canonicalTag -> insertTag(entryId, canonicalTag) }
            }
        }
        data.senseFields.forEach { value ->
            senseToEntry[value.sense_id]?.let { insertTag(it, value.field_name) }
        }
        data.senseMiscellaneous.forEach { value ->
            senseToEntry[value.sense_id]?.let { insertTag(it, value.miscellaneous_info) }
        }
        data.senseDialects.forEach { value ->
            senseToEntry[value.sense_id]?.let { insertTag(it, value.dialect) }
        }

        tagsByEntry.forEach { (entryId, tags) ->
            tags.forEach { tag ->
                database.vocabQueries.insert_vocab_search_tag(
                    Vocab_search_tag(entry_id = entryId, tag = tag)
                )
            }
        }
    }

    fun writeNames(items: List<DatabaseName>) = database.transaction {
        items.forEach { name ->
            val romaji = name.kana.searchRomaji()
            database.vocabQueries.insert_vocab_name(
                Vocab_name(
                    id = name.id,
                    kanji = name.kanji,
                    kana = name.kana,
                    romaji = romaji,
                    name_type = name.nameType,
                    meaning = name.meaning
                )
            )
        }
    }

    fun writeVocabDeckCards(items: List<Vocab_deck_card>) = database.transaction {
        items.forEach { database.vocabQueries.insert_vocab_deck_card(it) }
    }

    fun writeSentences(items: List<Sentence>) = database.transaction {
        items.forEach { database.vocabQueries.insert_sentence(it) }
    }

    /**
     * Compact the immutable application pack after all writes are complete.
     *
     * This is a lossless file-layout optimization: it preserves all logical
     * rows, schema metadata, and pack identity while removing interior page
     * fragmentation introduced by the export insertion order.
     */
    fun compact() {
        driver.execute(
            identifier = null,
            sql = "PRAGMA journal_mode = DELETE;",
            parameters = 0
        )
        driver.execute(
            identifier = null,
            sql = "VACUUM;",
            parameters = 0
        )
    }

    fun writeLearningGraph(
        nodes: List<DatabaseLearningNode>,
        edges: List<DatabaseLearningEdge>,
    ) = database.transaction {
        val nodeIds = nodes
            .distinctBy { it.nodeKey }
            .sortedBy { it.nodeKey }
            .mapIndexed { index, node ->
                val nodeId = index.toLong() + 1L
                database.lettersQueries.insertLearningNode(
                    node_id = nodeId,
                    node_key = node.nodeKey,
                    node_kind = node.nodeKind,
                    kanji = node.kanji,
                    reading = node.reading,
                    entry_id = node.entryId,
                    element_id = node.elementId,
                    sense_id = node.senseId,
                    sentence_id = node.sentenceId,
                    level = node.level,
                    priority = node.priority,
                )
                node.nodeKey to nodeId
            }
            .toMap()

        edges
            .distinct()
            .sortedWith(
                compareBy<DatabaseLearningEdge> { it.fromNodeKey }
                    .thenBy { it.edgeKind }
                    .thenBy { it.toNodeKey }
            )
            .forEach { edge ->
                val fromId = nodeIds[edge.fromNodeKey] ?: return@forEach
                val toId = nodeIds[edge.toNodeKey] ?: return@forEach
                database.lettersQueries.insertLearningEdge(
                    from_node_id = fromId,
                    to_node_id = toId,
                    edge_kind = edge.edgeKind,
                    weight = edge.weight,
                    provenance = edge.provenance,
                )
            }
    }

}