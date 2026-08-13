package ua.syt0r.kanji.core.app_data.data

import kotlinx.serialization.Serializable


@Serializable
data class VocabReading(
    val kanjiReading: String?,
    val kanaReading: String,
    val furigana: FuriganaString?
)

@Serializable
data class VocabSense(
    val glossary: List<String>,
    val partOfSpeechList: List<String>
)

@Serializable
data class JapaneseWord(
    val id: Long,
    val reading: VocabReading,
    val glossary: List<String>,
    val partOfSpeechList: List<String>
) {

    fun combinedGlossary(): String {
        return glossary.joinToString()
    }

}

data class DetailedVocabSense(
    val glossary: List<String>,
    val partOfSpeechList: List<String>,
    val readings: List<DetailedVocabReading>
)

data class DetailedVocabReading(
    val elementId: Long,
    val kanji: String?,
    val kana: String,
    val furigana: FuriganaString?,
    val info: Set<VocabReadingInfo>,
    val noKanji: Boolean
)

enum class VocabReadingInfo(
    val jmDictValue: String,
    private val verboseJmDictValue: String? = null
) {
    AtejiReading("ateji", "ateji (phonetic) reading"),
    GikunReading("gikun", "gikun (meaning as reading) or jukujikun (special kanji reading)"),
    IrregularOkuriganaUsage("io", "irregular okurigana usage"),
    IrregularKanaUsage("ik", "word containing irregular kana usage"),
    IrregularKanjiUsage("iK", "word containing irregular kanji usage"),
    OutdatedKana("ok", "out-dated or obsolete kana usage"),
    OutdatedKanji("oK", "word containing out-dated kanji or kanji usage"),
    RarelyUsedKanaForm("rk", "rarely used kana form"),
    RarelyUsedKanjiForm("rK", "rarely used kanji form"),
    SearchOnlyKanaForm("sk", "search-only kana form"),
    SearchOnlyKanjiForm("sK", "search-only kanji form");

    companion object {
        private val valuesByJmDictValue = entries
            .flatMap { info ->
                sequenceOf(info.jmDictValue, info.verboseJmDictValue)
                    .filterNotNull()
                    .map { value -> value to info }
            }
            .toMap()

        fun fromJmDictValue(value: String): VocabReadingInfo? =
            valuesByJmDictValue[value.trim()]
    }
}

data class DetailedJapaneseWord(
    val id: Long,
    val senseList: List<DetailedVocabSense>
)

fun VocabReading.formattedFurigana(): FuriganaString =
    formattedVocabReading(kanaReading, kanjiReading, furigana)

fun formattedKanaReading(reading: String): String = "【${reading}】"

fun formattedVocabReading(
    kanaReading: String,
    kanjiReading: String? = null,
    furigana: FuriganaString? = null
): FuriganaString = when {
    furigana != null -> {
        furigana
    }

    kanjiReading != null -> buildFuriganaString {
        append(kanjiReading)
        append(formattedKanaReading(kanaReading))
    }

    else -> buildFuriganaString {
        append(kanaReading)
    }
}

fun formattedVocabStringReading(
    kanaReading: String,
    kanjiReading: String? = null,
): String = when {
    kanjiReading != null -> buildString {
        append(kanjiReading)
        append(formattedKanaReading(kanaReading))
    }

    else -> kanaReading
}

private const val DefinitionDividerSymbol = "・"

fun formattedVocabDefinition(reading: FuriganaString, glossary: String) = buildFuriganaString {
    append(reading)
    append("$DefinitionDividerSymbol ")
    append(glossary)
}

fun formattedVocabDefinition(reading: VocabReading, glossary: String) =
    formattedVocabDefinition(reading.formattedFurigana(), glossary)

fun formattedVocabDefinition(reading: String, glossary: String) =
    "$reading$DefinitionDividerSymbol$glossary"
