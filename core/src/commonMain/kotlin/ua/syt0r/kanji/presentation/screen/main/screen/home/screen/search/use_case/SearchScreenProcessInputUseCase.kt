package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.use_case

import androidx.compose.runtime.mutableStateOf
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.SearchQueryParser
import ua.syt0r.kanji.core.japanese.isKana
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


        val searchResult = input.takeIf { it.isNotBlank() }
            ?.let { query ->
                appDataRepository.searchWords(
                    query = SearchQueryParser.parse(query),
                    limit = SearchScreenContract.InitialWordsCount
                )
            }
            ?: ua.syt0r.kanji.core.app_data.SearchResult(0, emptyList())

        return SearchScreenContract.ScreenState(
            isLoading = false,
            characters = searchResult.characters.ifEmpty { knownCharacters },
            names = searchResult.names,
            words = mutableStateOf(
                PaginatableJapaneseWordList(searchResult.totalCount, searchResult.words)
            ),
            query = input
        )
    }

}