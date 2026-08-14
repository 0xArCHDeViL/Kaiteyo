package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.use_case

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.SearchQueryParser
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreenContract

class SearchScreenLoadMoreWordsUseCase(
    private val appDataRepository: AppDataRepository
) : SearchScreenContract.LoadMoreWordsUseCase {

    override suspend fun loadMore(state: SearchScreenContract.ScreenState) {
        val wordsState = state.words as MutableState
        val currentWords = wordsState.value
        wordsState.value = withContext(Dispatchers.IO) {
            val newItems = currentWords.items + appDataRepository.searchWords(
                query = SearchQueryParser.parse(state.query),
                offset = currentWords.items.size,
                limit = SearchScreenContract.LoadMoreWordsCount
            ).words
            currentWords.copy(items = newItems)
        }
    }

}