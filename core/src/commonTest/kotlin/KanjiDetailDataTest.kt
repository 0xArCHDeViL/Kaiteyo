import kotlin.test.Test
import kotlin.test.assertEquals
import ua.syt0r.kanji.core.app_data.data.KanjiDetailData
import ua.syt0r.kanji.core.app_data.data.KanjiReadingData
import ua.syt0r.kanji.core.app_data.data.ReadingType

class KanjiDetailDataTest {

    @Test
    fun onAndKunReadingsRemainSeparate() {
        val detail = KanjiDetailData(
            kanji = "生",
            frequency = 12,
            variantFamily = null,
            meanings = listOf("life"),
            readings = listOf(
                KanjiReadingData("セイ", ReadingType.ON),
                KanjiReadingData("ショウ", ReadingType.ON),
                KanjiReadingData("いきる", ReadingType.KUN),
                KanjiReadingData("うまれる", ReadingType.KUN),
            ),
            classifications = listOf("n5"),
            strokePaths = emptyList(),
            radicals = emptyList(),
            vocabularyExamples = emptyList(),
        )

        assertEquals(listOf("セイ", "ショウ"), detail.onReadings.map { it.reading })
        assertEquals(listOf("いきる", "うまれる"), detail.kunReadings.map { it.reading })
    }

    @Test
    fun emptyReadingTypeDoesNotLeakIntoEitherSection() {
        val detail = KanjiDetailData(
            kanji = "一",
            frequency = null,
            variantFamily = null,
            meanings = emptyList(),
            readings = listOf(KanjiReadingData("いち", ReadingType.ON)),
            classifications = emptyList(),
            strokePaths = emptyList(),
            radicals = emptyList(),
            vocabularyExamples = emptyList(),
        )

        assertEquals(1, detail.onReadings.size)
        assertEquals(0, detail.kunReadings.size)
    }
}
