package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.use_case

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.JapaneseName
import ua.syt0r.kanji.core.app_data.SearchQueryParser
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseNameList
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreenContract

class SearchScreenLoadMoreNamesUseCase(
    private val appDataRepository: AppDataRepository
) : SearchScreenContract.LoadMoreNamesUseCase {

    override suspend fun loadMore(
        state: SearchScreenContract.ScreenState
    ): PaginatableJapaneseNameList = withContext(Dispatchers.IO) {
        val currentNames = state.names.value
        if (!currentNames.canLoadMore) return@withContext currentNames

        val existingIds = currentNames.items.asSequence().map { it.id }.toSet()
        val response = appDataRepository.searchWords(
            query = SearchQueryParser.parse(state.query),
            offset = currentNames.nextOffset,
            limit = SearchScreenContract.LoadMoreWordsCount
        )
        val nextPage = response.names.filterNot { it.id in existingIds }
        currentNames.copy(
            items = currentNames.items + nextPage,
            nextOffset = minOf(
                currentNames.totalCount,
                currentNames.nextOffset + SearchScreenContract.LoadMoreWordsCount
            )
        )
    }
}
