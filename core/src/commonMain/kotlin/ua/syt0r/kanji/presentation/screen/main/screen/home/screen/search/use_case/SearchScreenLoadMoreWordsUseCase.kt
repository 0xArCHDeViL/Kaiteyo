package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.use_case

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.SearchQueryParser
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseWordList
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreenContract

class SearchScreenLoadMoreWordsUseCase(
    private val appDataRepository: AppDataRepository
) : SearchScreenContract.LoadMoreWordsUseCase {

    override suspend fun loadMore(
        state: SearchScreenContract.ScreenState
    ): PaginatableJapaneseWordList = withContext(Dispatchers.IO) {
        val currentWords = state.words.value
        if (!currentWords.canLoadMore) return@withContext currentWords

        val existingKeys = currentWords.items
            .asSequence()
            .map { it.identityKey() }
            .toSet()
        val response = appDataRepository.searchWords(
            query = SearchQueryParser.parse(state.query),
            offset = currentWords.nextOffset,
            limit = SearchScreenContract.LoadMoreWordsCount
        )
        val nextItems = response.words.filterNot { it.identityKey() in existingKeys }
        currentWords.copy(
            items = currentWords.items + nextItems,
            nextOffset = minOf(
                currentWords.totalCount,
                currentWords.nextOffset + SearchScreenContract.LoadMoreWordsCount
            )
        )
    }

    private fun JapaneseWord.identityKey(): String = buildString {
        append(id)
        append('|')
        append(reading.kanjiReading.orEmpty())
        append('|')
        append(reading.kanaReading)
    }
}
