package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.use_case

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.SearchQueryParser
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseNameList
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreenContract

class SearchScreenLoadMoreNamesUseCase(
    private val appDataRepository: AppDataRepository
) : SearchScreenContract.LoadMoreNamesUseCase {

    override suspend fun loadMore(state: SearchScreenContract.ScreenState) {
        val namesState = state.names as MutableState
        val currentNames = namesState.value
        if (!currentNames.canLoadMore) return

        namesState.value = withContext(Dispatchers.IO) {
            val nextPage = appDataRepository.searchWords(
                query = SearchQueryParser.parse(state.query),
                offset = currentNames.items.size,
                limit = SearchScreenContract.LoadMoreWordsCount
            ).names
            currentNames.copy(items = currentNames.items + nextPage)
        }
    }
}
