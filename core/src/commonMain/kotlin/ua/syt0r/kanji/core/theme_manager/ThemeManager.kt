package ua.syt0r.kanji.core.theme_manager

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.core.user_data.preferences.PreferencesTheme

open class ThemeManager(
    private val appPreferences: PreferencesContract.AppPreferences,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    protected val coroutineScope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _currentTheme = mutableStateOf(PreferencesTheme.System)
    val currentTheme: State<PreferencesTheme> = _currentTheme

    init {
        invalidate()
        appPreferences.theme
            .onModified
            .onEach { invalidate() }
            .launchIn(coroutineScope)
    }

    fun changeTheme(theme: PreferencesTheme) {
        coroutineScope.launch { appPreferences.theme.set(theme) }
    }

    fun invalidate() {
        Logger.logMethod()
        coroutineScope.launch {
            val currentTheme = appPreferences.theme.get()
            _currentTheme.value = currentTheme
            platformInvalidate(currentTheme)
        }
    }

    open fun platformInvalidate(theme: PreferencesTheme) {}

    val isDarkTheme: Boolean
        @Composable
        get() = when (currentTheme.value) {
            PreferencesTheme.System -> isSystemInDarkTheme()
            PreferencesTheme.Light -> false
            PreferencesTheme.Dark, PreferencesTheme.Amoled -> true
        }

    val isAmoledTheme: Boolean
        get() = currentTheme.value == PreferencesTheme.Amoled

}

val LocalThemeManager = compositionLocalOf<ThemeManager> {
    throw IllegalStateException("ThemeManager is not initialized")
}