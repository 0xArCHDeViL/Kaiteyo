package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search

import androidx.compose.runtime.State
import ua.syt0r.kanji.core.app_data.JapaneseName
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseNameList
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseWordList
import ua.syt0r.kanji.core.app_data.SearchScope
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.data.RadicalSearchListItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.data.RadicalSearchState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.data.SearchByRadicalsResult

interface SearchScreenContract {

    companion object {
        const val InitialWordsCount = 50
        const val LoadMoreWordsCount = 50
        const val LoadMoreWordsFromEndThreshold = 20
        const val SearchInputDebounceMillis = 120L
    }

    interface ViewModel {

        val state: State<ScreenState>
        val radicalsState: State<RadicalSearchState>

        fun search(input: String)
        fun loadMoreWords()
        fun loadMoreNames()

        // Added for performance issues, loaded list makes switching to screen junky
        fun loadRadicalsData()
        fun radicalsSearch(radicals: Set<String>)

    }

    data class ScreenState(
        val isLoading: Boolean,
        val characters: List<String>,
        val names: State<PaginatableJapaneseNameList>,
        val words: State<PaginatableJapaneseWordList>,
        val query: String,
        val scope: SearchScope = SearchScope.Words,
        val errorMessage: String? = null
    ) {
        val hasQuery: Boolean
            get() = query.isNotBlank()

        val totalResultCount: Int
            get() = when (scope) {
                SearchScope.Words -> words.value.totalCount
                SearchScope.Names -> names.value.totalCount
                SearchScope.Kanji, SearchScope.Components -> characters.size
            }

        companion object {
            fun empty(
                query: String = "",
                scope: SearchScope = SearchScope.Words,
                errorMessage: String? = null
            ) = ScreenState(
                isLoading = false,
                characters = emptyList(),
                names = androidx.compose.runtime.mutableStateOf(
                    PaginatableJapaneseNameList(0, emptyList())
                ),
                words = androidx.compose.runtime.mutableStateOf(
                    PaginatableJapaneseWordList(0, emptyList())
                ),
                query = query,
                scope = scope,
                errorMessage = errorMessage
            )
        }
    }

    interface ProcessInputUseCase {
        suspend fun process(input: String): ScreenState
    }

    interface LoadRadicalsUseCase {
        suspend fun load(): List<RadicalSearchListItem>
    }

    interface SearchByRadicalsUseCase {
        suspend fun search(radicals: Set<String>): SearchByRadicalsResult
    }

    interface LoadMoreWordsUseCase {
        suspend fun loadMore(state: ScreenState): PaginatableJapaneseWordList
    }

    interface LoadMoreNamesUseCase {
        suspend fun loadMore(state: ScreenState): PaginatableJapaneseNameList
    }

    interface UpdateEnabledRadicalsUseCase {
        suspend fun update(
            allRadicals: List<RadicalSearchListItem>,
            selectedRadicals: Set<String>
        ): List<RadicalSearchListItem>
    }

}