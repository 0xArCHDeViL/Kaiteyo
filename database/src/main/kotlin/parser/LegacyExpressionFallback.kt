package parser

import com.google.gson.Gson
import export.db.DatabaseFuriganaItem
import export.db.DatabaseVocabData
import export.db.Vocab_entry
import export.db.Vocab_furigana
import export.db.Vocab_kana_element
import export.db.Vocab_kanji_element
import export.db.Vocab_sense
import export.db.Vocab_sense_gloss
import java.io.File

private data class LegacyExpression(
    val id: String,
    val readings: List<LegacyReading> = emptyList(),
    val meanings: List<LegacyMeaning> = emptyList()
)

private data class LegacyReading(
    val kanaExpression: String? = null,
    val kanjiExpression: String? = null,
    val furiganaExpression: List<LegacyFurigana>? = null
)

private data class LegacyFurigana(
    val text: String,
    val annotation: String? = null
)

private data class LegacyMeaning(
    val lang: String? = null,
    val values: List<String> = emptyList()
)

/**
 * Keeps the canonical supported-vocabulary set stable when a newer JMdict
 * snapshot removes entries that are still present in our exported expression
 * data. The fallback deliberately reads only repository-owned JSON and does
 * not invent vocabulary outside that canonical set.
 */
object LegacyExpressionFallback {

    fun parseMissing(
        missingIds: Set<Long>,
        expressionsDir: File = ProjectData.exportExpressionsDir
    ): DatabaseVocabData {
        if (missingIds.isEmpty()) return emptyData()

        val entries = mutableListOf<Vocab_entry>()
        val kanjiElements = mutableListOf<Vocab_kanji_element>()
        val kanaElements = mutableListOf<Vocab_kana_element>()
        val senses = mutableListOf<Vocab_sense>()
        val senseGlosses = mutableListOf<Vocab_sense_gloss>()
        val furigana = mutableListOf<Vocab_furigana>()
        val missingFiles = mutableListOf<File>()
        val gson = Gson()

        missingIds.sorted().forEach { entryId ->
            val file = File(expressionsDir, "$entryId.json")
            if (!file.isFile) {
                missingFiles += file
                return@forEach
            }

            val expression = gson.fromJson(file.readText(), LegacyExpression::class.java)
            val parsedId = expression.id.toLongOrNull()
            check(parsedId == entryId) {
                "Legacy expression file ${file.name} contains id ${expression.id}, expected $entryId"
            }

            entries += Vocab_entry(entryId)
            val hasKanji = expression.readings.any { it.kanjiExpression != null }
            val kanaReadings = expression.readings.mapNotNull { it.kanaExpression }.distinct()
            kanaReadings.forEachIndexed { index, reading ->
                kanaElements += Vocab_kana_element(
                    element_id = elementId(entryId, index),
                    entry_id = entryId,
                    reading = reading,
                    no_kanji = if (hasKanji) 0L else 1L,
                    priority = null
                )
            }

            expression.readings.mapNotNull { it.kanjiExpression }
                .distinct()
                .forEachIndexed { index, reading ->
                    kanjiElements += Vocab_kanji_element(
                        element_id = elementId(entryId, 100 + index),
                        entry_id = entryId,
                        reading = reading,
                        priority = null
                    )
                }

            val englishMeanings = expression.meanings
                .firstOrNull { it.lang == null || it.lang == "en" }
                ?.values
                .orEmpty()
                .distinct()
            if (englishMeanings.isNotEmpty()) {
                val senseId = senseId(entryId)
                senses += Vocab_sense(id = senseId, entry_id = entryId)
                englishMeanings.forEach { meaning ->
                    senseGlosses += Vocab_sense_gloss(
                        sense_id = senseId,
                        gloss_text = meaning,
                        language = null,
                        type = null
                    )
                }
            }

            expression.readings.forEach { reading ->
                val kanji = reading.kanjiExpression ?: return@forEach
                val kana = reading.kanaExpression ?: return@forEach
                val items = reading.furiganaExpression.orEmpty().map {
                    DatabaseFuriganaItem(text = it.text, annotation = it.annotation)
                }
                if (items.isNotEmpty()) {
                    furigana += Vocab_furigana(
                        text = kanji,
                        reading = kana,
                        furigana = gson.toJson(items)
                    )
                }
            }
        }

        check(missingFiles.isEmpty()) {
            "Missing canonical expression JSON for legacy vocab IDs: " +
                missingFiles.joinToString { it.name }
        }

        return DatabaseVocabData(
            entries = entries,
            kanjiElements = kanjiElements,
            kanjiInformation = emptyList(),
            kanjiPriorities = emptyList(),
            kanaElements = kanaElements,
            kanaRestrictions = emptyList(),
            kanaInformation = emptyList(),
            kanaPriorities = emptyList(),
            senses = senses,
            senseKanjiRestrictions = emptyList(),
            senseReadingRestrictions = emptyList(),
            sensePartsOfSpeech = emptyList(),
            senseCrossReferences = emptyList(),
            senseAntonyms = emptyList(),
            senseFields = emptyList(),
            senseMiscellaneous = emptyList(),
            senseDialects = emptyList(),
            senseGlosses = senseGlosses,
            senseInformation = emptyList(),
            senseExample = emptyList(),
            entities = emptyList(),
            furigana = furigana
        )
    }

    private fun elementId(entryId: Long, index: Int): Long =
        FALLBACK_ELEMENT_ID_BASE + entryId * 1_000L + index + 1L

    private fun senseId(entryId: Long): Long = FALLBACK_SENSE_ID_BASE + entryId

    private const val FALLBACK_ELEMENT_ID_BASE = 1_000_000_000L
    private const val FALLBACK_SENSE_ID_BASE = 2_000_000_000L

    private fun emptyData() = DatabaseVocabData(
        entries = emptyList(),
        kanjiElements = emptyList(),
        kanjiInformation = emptyList(),
        kanjiPriorities = emptyList(),
        kanaElements = emptyList(),
        kanaRestrictions = emptyList(),
        kanaInformation = emptyList(),
        kanaPriorities = emptyList(),
        senses = emptyList(),
        senseKanjiRestrictions = emptyList(),
        senseReadingRestrictions = emptyList(),
        sensePartsOfSpeech = emptyList(),
        senseCrossReferences = emptyList(),
        senseAntonyms = emptyList(),
        senseFields = emptyList(),
        senseMiscellaneous = emptyList(),
        senseDialects = emptyList(),
        senseGlosses = emptyList(),
        senseInformation = emptyList(),
        senseExample = emptyList(),
        entities = emptyList(),
        furigana = emptyList()
    )
}

fun DatabaseVocabData.withFallback(fallback: DatabaseVocabData): DatabaseVocabData = copy(
    entries = entries + fallback.entries,
    kanjiElements = kanjiElements + fallback.kanjiElements,
    kanjiInformation = kanjiInformation + fallback.kanjiInformation,
    kanjiPriorities = kanjiPriorities + fallback.kanjiPriorities,
    kanaElements = kanaElements + fallback.kanaElements,
    kanaRestrictions = kanaRestrictions + fallback.kanaRestrictions,
    kanaInformation = kanaInformation + fallback.kanaInformation,
    kanaPriorities = kanaPriorities + fallback.kanaPriorities,
    senses = senses + fallback.senses,
    senseKanjiRestrictions = senseKanjiRestrictions + fallback.senseKanjiRestrictions,
    senseReadingRestrictions = senseReadingRestrictions + fallback.senseReadingRestrictions,
    sensePartsOfSpeech = sensePartsOfSpeech + fallback.sensePartsOfSpeech,
    senseCrossReferences = senseCrossReferences + fallback.senseCrossReferences,
    senseAntonyms = senseAntonyms + fallback.senseAntonyms,
    senseFields = senseFields + fallback.senseFields,
    senseMiscellaneous = senseMiscellaneous + fallback.senseMiscellaneous,
    senseDialects = senseDialects + fallback.senseDialects,
    senseGlosses = senseGlosses + fallback.senseGlosses,
    senseInformation = senseInformation + fallback.senseInformation,
    senseExample = senseExample + fallback.senseExample,
    entities = entities + fallback.entities,
    furigana = furigana + fallback.furigana
)
