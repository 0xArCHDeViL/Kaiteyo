package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.app_data.SearchQueryParser
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseNameList
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseWordList
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.data.RadicalSearchState

class SearchViewModel(
    private val viewModelScope: CoroutineScope,
    private val processInputUseCase: SearchScreenContract.ProcessInputUseCase,
    private val loadMoreWordsUseCase: SearchScreenContract.LoadMoreWordsUseCase,
    private val loadMoreNamesUseCase: SearchScreenContract.LoadMoreNamesUseCase,
    private val loadRadicalsUseCase: SearchScreenContract.LoadRadicalsUseCase,
    private val searchByRadicalsUseCase: SearchScreenContract.SearchByRadicalsUseCase,
    private val updateEnabledRadicalsUseCase: SearchScreenContract.UpdateEnabledRadicalsUseCase,
    private val analyticsManager: AnalyticsManager
) : SearchScreenContract.ViewModel {

    private val searchQueriesChannel = Channel<String>(Channel.BUFFERED)
    private val loadMoreWordsChannel = Channel<Int>(Channel.RENDEZVOUS, BufferOverflow.DROP_LATEST)
    private val loadMoreNamesChannel = Channel<Int>(Channel.RENDEZVOUS, BufferOverflow.DROP_LATEST)
    private var wordsPageLoading = false
    private var namesPageLoading = false

    private val radicalsDataInitialLoadChannel = Channel<Unit>(Channel.BUFFERED)
    private val radicalsLoadedCompletable = CompletableDeferred<Unit>()
    private val radicalsSearchQueriesChannel = Channel<Set<String>>(Channel.BUFFERED)

    override val state = mutableStateOf(
        ScreenState(
            isLoading = false,
            characters = emptyList(),
            names = mutableStateOf(PaginatableJapaneseNameList(0, emptyList())),
            words = mutableStateOf(PaginatableJapaneseWordList(0, emptyList())),
            query = "",
            scope = ua.syt0r.kanji.core.app_data.SearchScope.Words,
            errorMessage = null
        )
    )

    override val radicalsState = mutableStateOf(
        RadicalSearchState(
            radicalsListItems = emptyList(),
            characterListItems = emptyList(),
            isLoading = true
        )
    )


    init {
        handleSearchQueries()
        handleRadicalsLoading(radicalsLoadedCompletable)
        handleRadicalSearchQueries(radicalsLoadedCompletable)
        handleLoadMoreWordsRequests()
        handleLoadMoreNamesRequests()
    }

    override fun search(input: String) {
        Logger.d("sending input $input")
        searchQueriesChannel.trySend(input)
    }

    override fun loadMoreWords() {
        Logger.d(">>")
        val currentState = state.value.words.value
        loadMoreWordsChannel.trySend(currentState.items.size)
    }

    override fun loadMoreNames() {
        Logger.d("load more names")
        val currentState = state.value.names.value
        loadMoreNamesChannel.trySend(currentState.items.size)
    }

    override fun loadRadicalsData() {
        radicalsDataInitialLoadChannel.trySend(Unit)
    }

    override fun radicalsSearch(radicals: Set<String>) {
        radicalsSearchQueriesChannel.trySend(radicals)
    }

    private fun handleSearchQueries() = viewModelScope.launch {
        searchQueriesChannel.consumeAsFlow()
            .distinctUntilChanged()
            .onEach {
                Logger.d("loading for $it")
                state.value = state.value.copy(isLoading = true, errorMessage = null)
            }
            .collectLatest { input ->
                try {
                    /***
                     * Async is not interrupted here, it executes completely but result is ignored,
                     * runInterruptible doesn't work as well, TODO interrupt
                     * More details: https://github.com/Kotlin/kotlinx.coroutines/issues/3109
                     */
                    Logger.d("start searching for $input")
                    val result = async(coroutineContext + Dispatchers.IO) {
                        Logger.d("processing input $input in background")
                        processInputUseCase.process(input)
                    }.await()
                    Logger.d("applying new state for $input")
                    state.value = result
                } catch (error: kotlinx.coroutines.CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    Logger.e("search for $input failed: ${error.stackTraceToString()}")
                    state.value = SearchScreenContract.ScreenState.empty(
                        query = input,
                        scope = SearchQueryParser.parse(input).scope,
                        errorMessage = error.message ?: "Unable to search the offline dictionary"
                    )
                }
            }
    }

    private fun handleRadicalsLoading(radicalsLoadedCompletable: CompletableDeferred<Unit>) {
        radicalsDataInitialLoadChannel.consumeAsFlow()
            .take(1)
            .onEach {
                val updatedState = withContext(Dispatchers.IO) {
                    RadicalSearchState(
                        radicalsListItems = loadRadicalsUseCase.load(),
                        characterListItems = emptyList(),
                        isLoading = false
                    )
                }
                radicalsState.value = updatedState
                radicalsLoadedCompletable.complete(Unit)
            }
            .launchIn(viewModelScope)
    }

    private fun handleRadicalSearchQueries(
        radicalsLoadedCompletable: CompletableDeferred<Unit>
    ) = viewModelScope.launch {
        radicalsSearchQueriesChannel.consumeAsFlow()
            .distinctUntilChanged()
            .onStart { radicalsLoadedCompletable.await() }
            .onEach { radicalsState.value = radicalsState.value.copy(isLoading = true) }
            .collectLatest { radicals ->
                val currentState = radicalsState.value

                val updatedState = async(Dispatchers.IO) {
                    val searchResult = searchByRadicalsUseCase.search(radicals)
                    val updatedRadicals = updateEnabledRadicalsUseCase.update(
                        allRadicals = currentState.radicalsListItems,
                        selectedRadicals = radicals
                    )
                    RadicalSearchState(
                        radicalsListItems = updatedRadicals,
                        characterListItems = searchResult.listData,
                        isLoading = false
                    )
                }

                radicalsState.value = updatedState.await()
            }
    }

        private fun handleLoadMoreWordsRequests() = viewModelScope.launch {
        loadMoreWordsChannel.consumeAsFlow()
            .collect {
                if (wordsPageLoading || !state.value.words.value.canLoadMore) return@collect
                wordsPageLoading = true
                try {
                    loadMoreWordsUseCase.loadMore(state.value)
                } finally {
                    wordsPageLoading = false
                }
            }
    }

    private fun handleLoadMoreNamesRequests() = viewModelScope.launch {
        loadMoreNamesChannel.consumeAsFlow()
            .collect {
                if (namesPageLoading || !state.value.names.value.canLoadMore) return@collect
                namesPageLoading = true
                try {
                    loadMoreNamesUseCase.loadMore(state.value)
                } finally {
                    namesPageLoading = false
                }
            }
    }

}
