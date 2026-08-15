package ua.syt0r.kanji.presentation.common

import ua.syt0r.kanji.core.app_data.data.JapaneseWord

data class PaginatableJapaneseWordList(
    val totalCount: Int,
    val items: List<JapaneseWord>,
    val nextOffset: Int = items.size
) {
    val canLoadMore: Boolean = nextOffset < totalCount
}
