package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.use_case

import androidx.compose.runtime.mutableStateOf
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.SearchQueryParser
import ua.syt0r.kanji.core.japanese.isKana
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseNameList
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseWordList
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreenContract

class SearchScreenProcessInputUseCase(
    private val appDataRepository: AppDataRepository
) : SearchScreenContract.ProcessInputUseCase {

    override suspend fun process(
        input: String
    ): SearchScreenContract.ScreenState {

        val knownCharacters = input.mapNotNull {
            val charString = it.toString()
            val areStrokesAvailable = appDataRepository.getStrokes(charString).isNotEmpty()

            val isKnown = areStrokesAvailable && when {
                it.isKana() -> true
                else -> appDataRepository.getReadings(charString).isNotEmpty()
            }

            if (isKnown) charString else null
        }


        val parsedQuery = SearchQueryParser.parse(input)
        val searchResult = input.takeIf { it.isNotBlank() }
            ?.let {
                appDataRepository.searchWords(
                    query = parsedQuery,
                    limit = SearchScreenContract.InitialWordsCount
                )
            }
            ?: ua.syt0r.kanji.core.app_data.SearchResult(0, emptyList())

        val isNamesQuery = parsedQuery.scope == ua.syt0r.kanji.core.app_data.SearchScope.Names
        val isWordsQuery = parsedQuery.scope == ua.syt0r.kanji.core.app_data.SearchScope.Words
        val namesTotalCount = if (isNamesQuery) searchResult.totalCount else 0
        val wordsTotalCount = if (isWordsQuery) searchResult.totalCount else 0

        val characters = when (parsedQuery.scope) {
            ua.syt0r.kanji.core.app_data.SearchScope.Words ->
                searchResult.characters.ifEmpty { knownCharacters }
            ua.syt0r.kanji.core.app_data.SearchScope.Kanji,
            ua.syt0r.kanji.core.app_data.SearchScope.Components,
            ua.syt0r.kanji.core.app_data.SearchScope.Names -> searchResult.characters
        }

        return SearchScreenContract.ScreenState(
            isLoading = false,
            characters = characters,
            names = mutableStateOf(
                PaginatableJapaneseNameList(
                    totalCount = namesTotalCount,
                    items = searchResult.names,
                    nextOffset = minOf(namesTotalCount, SearchScreenContract.InitialWordsCount)
                )
            ),
            words = mutableStateOf(
                PaginatableJapaneseWordList(
                    totalCount = wordsTotalCount,
                    items = searchResult.words,
                    nextOffset = minOf(wordsTotalCount, SearchScreenContract.InitialWordsCount)
                )
            ),
            query = input,
            scope = parsedQuery.scope,
            errorMessage = null
        )
    }

}