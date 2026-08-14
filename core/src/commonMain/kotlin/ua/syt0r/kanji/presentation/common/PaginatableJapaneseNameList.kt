package ua.syt0r.kanji.presentation.common

import ua.syt0r.kanji.core.app_data.JapaneseName

data class PaginatableJapaneseNameList(
    val totalCount: Int,
    val items: List<JapaneseName>
) {
    val canLoadMore: Boolean = totalCount > items.size
}
