import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ua.syt0r.kanji.core.app_data.data.DictionaryDetailTarget
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenData
import ua.syt0r.kanji.presentation.screen.main.screen.info.toDictionaryDetailTarget

class DetailDataIdentityTest {

    @Test
    fun vocabularyIdentityKeepsSharedReadingEntriesSeparate() {
        val husband = DictionaryDetailTarget.Vocabulary(
            entryId = 1001L,
            elementId = 11L,
            kanji = "主夫",
            kana = "しゅふ",
        )
        val wife = DictionaryDetailTarget.Vocabulary(
            entryId = 1002L,
            elementId = 21L,
            kanji = "主婦",
            kana = "しゅふ",
        )

        assertNotEquals(husband, wife)
        assertNotEquals(husband.entryId, wife.entryId)
        assertEquals("しゅふ", husband.kana)
        assertEquals("しゅふ", wife.kana)
    }

    @Test
    fun vocabularyIdentityPreservesSelectedElement() {
        val target = DictionaryDetailTarget.Vocabulary(
            entryId = 42L,
            elementId = 7L,
            kanji = "召し上がる",
            kana = "めしあがる",
        )

        assertEquals(42L, target.entryId)
        assertEquals(7L, target.elementId)
        assertEquals("召し上がる", target.kanji)
        assertEquals("めしあがる", target.kana)
    }

    @Test
    fun legacyVocabNavigationMapsToTypedTargetWhenEntryIdExists() {
        val legacy = InfoScreenData.Vocab(
            id = 42L,
            kanjiReading = "主夫",
            kanaReading = "しゅふ",
        )

        val target = legacy.toDictionaryDetailTarget()
        assertTrue(target is DictionaryDetailTarget.Vocabulary)
        assertEquals(42L, target.entryId)
        assertEquals("主夫", target.kanji)
        assertEquals("しゅふ", target.kana)
    }

    @Test
    fun legacyVocabNavigationWithoutEntryIdRemainsResolvableByExistingUseCase() {
        val legacy = InfoScreenData.Vocab(
            id = null,
            kanjiReading = "主婦",
            kanaReading = "しゅふ",
        )

        assertNull(legacy.toDictionaryDetailTarget())
    }
}
