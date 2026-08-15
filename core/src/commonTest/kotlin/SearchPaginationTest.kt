import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseNameList
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseWordList

class SearchPaginationTest {

    @Test
    fun wordsPaginationUsesBackendOffset() {
        val page = PaginatableJapaneseWordList(
            totalCount = 100,
            items = emptyList(),
            nextOffset = 50
        )

        assertTrue(page.canLoadMore)
        assertFalse(page.copy(nextOffset = 100).canLoadMore)
    }

    @Test
    fun namesPaginationUsesBackendOffset() {
        val page = PaginatableJapaneseNameList(
            totalCount = 50,
            items = emptyList(),
            nextOffset = 50
        )

        assertFalse(page.canLoadMore)
        assertTrue(page.copy(nextOffset = 25).canLoadMore)
    }
}
