package ua.syt0r.kanji.presentation.screen.main.screen.connected_learning

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.connected_learning.ConnectedNodeKey

fun interface ConnectedLearningLoader {
    suspend fun load(rootNodeKey: ConnectedNodeKey): ConnectedLearningUiState
}

interface ConnectedLearningScreenViewModel {
    val state: StateFlow<ConnectedLearningUiState>
    fun load(rootNodeKey: ConnectedNodeKey)
    fun onEvent(event: ConnectedLearningEvent)
}

class DefaultConnectedLearningViewModel(
    private val viewModelScope: CoroutineScope,
    private val loader: ConnectedLearningLoader,
    initialState: ConnectedLearningUiState = ConnectedLearningUiState(),
) : ConnectedLearningScreenViewModel {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<ConnectedLearningUiState> = _state.asStateFlow()

    private var rootNodeKey: ConnectedNodeKey? = null

    override fun load(rootNodeKey: ConnectedNodeKey) {
        if (this.rootNodeKey == rootNodeKey && _state.value.graphNodes.isNotEmpty()) return
        this.rootNodeKey = rootNodeKey
        refresh(rootNodeKey)
    }

    override fun onEvent(event: ConnectedLearningEvent) {
        when (event) {
            ConnectedLearningEvent.Refresh,
            ConnectedLearningEvent.Retry -> rootNodeKey?.let(::refresh)
            is ConnectedLearningEvent.SelectNode -> {
                _state.value = _state.value.copy(selectedNodeKey = event.key)
            }
            is ConnectedLearningEvent.StartLesson -> {
                _state.value = _state.value.copy(
                    selectedNodeKey = event.key,
                    activeLessonRootKey = event.key,
                )
            }
        }
    }

    private fun refresh(rootNodeKey: ConnectedNodeKey) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null,
            )
            runCatching { loader.load(rootNodeKey) }
                .onSuccess { loaded ->
                    _state.value = loaded.copy(
                        isLoading = false,
                        errorMessage = null,
                        selectedNodeKey = loaded.selectedNodeKey ?: rootNodeKey,
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to load connected learning",
                    )
                }
        }
    }
}
