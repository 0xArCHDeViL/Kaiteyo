package ua.syt0r.kanji.core.app_data

import kotlinx.coroutines.Deferred
import kotlinx.serialization.Serializable
import ua.syt0r.kanji.BuildConfig
import ua.syt0r.kanji.core.app_data.data.CharacterRadical
import ua.syt0r.kanji.core.app_data.data.DetailedJapaneseWord
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.KanjiCatalogEntry
import ua.syt0r.kanji.core.app_data.data.KanjiData
import ua.syt0r.kanji.core.app_data.data.KanjiDetailData
import ua.syt0r.kanji.core.app_data.data.RadicalData
import ua.syt0r.kanji.core.app_data.data.ReadingType
import ua.syt0r.kanji.core.app_data.db.AppDataDatabase

const val AppDataSchemaVersion: Long = BuildConfig.appDataSchemaVersion.toLong()
const val AppDataPackRevision: Long = BuildConfig.appDataPackRevision.toLong()
const val AppDataPackResourceName: String = BuildConfig.appDataPackName
const val AppDataPackUrl: String = BuildConfig.appDataPackUrl
const val AppDataPackChecksumUrl: String = BuildConfig.appDataPackChecksumUrl

interface AppDataDatabaseProvider {
    fun provideAsync(): Deferred<AppDataDatabase>
}

data class ConnectedVocabElementData(
    val entryId: Long,
    val elementId: Long,
    val elementKind: String,
    val reading: String,
    val glossary: List<String>,
    val partOfSpeech: List<String>,
)

interface AppDataRepository {

    suspend fun getStrokes(character: String): List<String>
    suspend fun getRadicalsInCharacter(character: String): List<CharacterRadical>
    suspend fun getRadicalsInCharacters(characters: List<String>): Map<String, List<CharacterRadical>>

    suspend fun getMeanings(kanji: String): List<String>
    suspend fun getReadings(kanji: String): Map<String, ReadingType>
    suspend fun getClassificationsForKanji(kanji: String): List<String>
    suspend fun getKanjiForClassification(classification: String): List<String>
    suspend fun getCharacterReadingsOfLength(length: Int, limit: Int): List<String>
    suspend fun getData(kanji: String): KanjiData?

    // Kanji Browser catalog query: one row per kanji, all display metadata aggregated.
    suspend fun getKanjiCatalog(): List<KanjiCatalogEntry>
    suspend fun getKanjiDetail(kanji: String, vocabularyLimit: Int = 24): KanjiDetailData?

    // Legacy bulk queries retained for non-catalog callers during migration.
    suspend fun getAllKanji(): List<ua.syt0r.kanji.core.app_data.data.KanjiListEntry>
    suspend fun getAllKanjiMeanings(): List<ua.syt0r.kanji.core.app_data.data.KanjiMeaningEntry>
    suspend fun getAllKanjiReadings(): List<ua.syt0r.kanji.core.app_data.data.KanjiReadingEntry>
    suspend fun getAllClassifications(): List<ua.syt0r.kanji.core.app_data.data.KanjiClassificationEntry>
    suspend fun getKanjiStrokeCounts(): Map<String, Int>

    suspend fun getRadicals(): List<RadicalData>
    suspend fun getCharactersWithRadicals(radicals: List<String>): List<String>
    suspend fun getAllRadicalsInCharactersWithSelectedRadicals(radicals: Set<String>): List<String>

    suspend fun getWordsWithTextCount(text: String): Int
    suspend fun getWordsWithText(
        text: String,
        offset: Int = 0,
        limit: Int = Int.MAX_VALUE
    ): List<JapaneseWord>

    suspend fun searchWords(
        query: SearchQuery,
        offset: Int = 0,
        limit: Int = Int.MAX_VALUE
    ): SearchResult

    suspend fun getWordExamples(letter: String): List<JapaneseWord>

    suspend fun getWord(id: Long, kanjiReading: String?, kanaReading: String): JapaneseWord?
    suspend fun findWords(
        id: Long?,
        kanjiReading: String?,
        kanaReading: String?
    ): List<JapaneseWord>

    suspend fun getKanaWordsWithTextCount(text: String): Int
    suspend fun getKanaWords(char: String, limit: Int, offset: Int): List<JapaneseWord>
    suspend fun getDetailedWord(id: Long): DetailedJapaneseWord?

    suspend fun getImportDeckWordsCount(classification: String): Int
    suspend fun getImportDeckWords(classification: String): List<ImportDeckWord>

    suspend fun getSentencesWithTextCount(text: String): Int
    suspend fun getSentencesWithText(
        text: String,
        offset: Int = 0,
        limit: Int = Int.MAX_VALUE
    ): List<Sentence>

    suspend fun getWordSenses(idList: Set<Long>): List<VocabSenseGroup>
    suspend fun getConnectedVocabElementData(entryIds: Set<Long>): List<ConnectedVocabElementData>

}

data class ImportDeckWord(
    val id: Long,
    val kanji: String?,
    val kana: String,
    val meaning: String?
)

class VocabSenseGroup(
    val wordId: Long,
    val senseList: List<Sense>
) {

    data class Sense(
        val glossary: List<String>,
        val kanjiRestrictions: List<String>,
        val kanaRestrictions: List<String>
    )

    private fun getMatchingSense(kanjiReading: String?, kanaReading: String): Sense {
        return senseList.firstOrNull {
            val kanjiCheck = kanjiReading == null ||
                    it.kanjiRestrictions.isEmpty() ||
                    it.kanjiRestrictions.contains(kanjiReading)
            val kanaCheck = it.kanaRestrictions.isEmpty() ||
                    it.kanaRestrictions.contains(kanaReading)
            kanjiCheck && kanaCheck
        } ?: senseList.firstOrNull() ?: Sense(
            glossary = emptyList(),
            kanjiRestrictions = emptyList(),
            kanaRestrictions = emptyList()
        )
    }

    fun getMatchingMeaning(kanjiReading: String?, kanaReading: String): String {
        return getMatchingSense(kanjiReading, kanaReading).glossary.joinToString()
    }

}

data class Sentence(
    val value: String,
    val translation: String,
    val furigana: FuriganaString
)

data class JapaneseName(
    val id: Long,
    val kanji: String?,
    val kana: String,
    val nameType: String?,
    val meaning: String
)

data class SearchResult(
    val totalCount: Int,
    val words: List<JapaneseWord>,
    val characters: List<String> = emptyList(),
    val names: List<JapaneseName> = emptyList()
)

@Serializable
sealed interface WordClassification {

    val dbValue: String

    @Serializable
    data class JLPT(
        val level: Int
    ) : WordClassification {

        override val dbValue: String = "n$level"

        companion object {
            val all: List<JLPT> = (5 downTo 1).map { JLPT(it) }
        }
    }

    @Serializable
    data class Other(
        val index: Int
    ) : WordClassification {

        override val dbValue: String = "o$index"

        companion object {
            val all: List<Other> = (1..12).map { Other(it) }
        }
    }

}