package ua.syt0r.kanji.presentation.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

open class BaseViewModel {
    protected val viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())

    open fun onCleared() {
        viewModelScope.cancel()
    }
}
