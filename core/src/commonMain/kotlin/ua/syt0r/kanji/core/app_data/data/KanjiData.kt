package ua.syt0r.kanji.core.app_data.data

class KanjiData(
    val kanji: String,
    val frequency: Int?,
    val variantFamily: String?
)

data class KanjiCatalogEntry(
    val kanji: String,
    val frequency: Int?,
    val meanings: List<String>,
    val onReadings: List<String>,
    val classifications: List<String>,
    val strokeCount: Int,
    val readings: List<String> = onReadings
)

data class KanjiListEntry(
    val kanji: String,
    val frequency: Int?
)

data class KanjiMeaningEntry(
    val kanji: String,
    val meaning: String
)

data class KanjiReadingEntry(
    val kanji: String,
    val readingType: String,
    val reading: String
)

data class KanjiClassificationEntry(
    val kanji: String,
    val classification: String
)


data class KanjiReadingData(
    val reading: String,
    val type: ReadingType,
)

data class KanjiDetailData(
    val kanji: String,
    val frequency: Int?,
    val variantFamily: String?,
    val meanings: List<String>,
    val readings: List<KanjiReadingData>,
    val classifications: List<String>,
    val strokePaths: List<String>,
    val radicals: List<CharacterRadical>,
    val vocabularyExamples: List<JapaneseWord>,
) {
    val onReadings: List<KanjiReadingData>
        get() = readings.filter { it.type == ReadingType.ON }

    val kunReadings: List<KanjiReadingData>
        get() = readings.filter { it.type == ReadingType.KUN }
}
