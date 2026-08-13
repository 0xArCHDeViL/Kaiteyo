import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ua.syt0r.kanji.core.app_data.data.VocabReadingInfo

class VocabReadingInfoTest {

    @Test
    fun mapsReleaseDatabaseVerboseMetadataToCanonicalInfo() {
        val expected = mapOf(
            "ateji (phonetic) reading" to VocabReadingInfo.AtejiReading,
            "gikun (meaning as reading) or jukujikun (special kanji reading)" to VocabReadingInfo.GikunReading,
            "irregular okurigana usage" to VocabReadingInfo.IrregularOkuriganaUsage,
            "word containing irregular kana usage" to VocabReadingInfo.IrregularKanaUsage,
            "word containing irregular kanji usage" to VocabReadingInfo.IrregularKanjiUsage,
            "out-dated or obsolete kana usage" to VocabReadingInfo.OutdatedKana,
            "word containing out-dated kanji or kanji usage" to VocabReadingInfo.OutdatedKanji,
            "rarely used kana form" to VocabReadingInfo.RarelyUsedKanaForm,
            "rarely used kanji form" to VocabReadingInfo.RarelyUsedKanjiForm,
            "search-only kana form" to VocabReadingInfo.SearchOnlyKanaForm,
            "search-only kanji form" to VocabReadingInfo.SearchOnlyKanjiForm,
        )

        expected.forEach { (value, info) ->
            assertEquals(info, VocabReadingInfo.fromJmDictValue(value))
        }
    }

    @Test
    fun mapsCanonicalMetadataAndTrimsWhitespace() {
        assertEquals(
            VocabReadingInfo.OutdatedKana,
            VocabReadingInfo.fromJmDictValue("  ok  ")
        )
        assertEquals(
            VocabReadingInfo.SearchOnlyKanjiForm,
            VocabReadingInfo.fromJmDictValue("sK")
        )
    }

    @Test
    fun returnsNullForFutureUnknownMetadataInsteadOfThrowing() {
        assertNull(VocabReadingInfo.fromJmDictValue("future JMdict metadata"))
    }
}
