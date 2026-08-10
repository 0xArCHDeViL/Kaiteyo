package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.items

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsSwitchRow

class CrashAnalyticsSettingItem(
    private val appPreferences: PreferencesContract.AppPreferences
) : SettingsScreenContract.ConfigurableListItem {

    private lateinit var isEnabledState: MutableState<Boolean>

    override suspend fun prepare(coroutineScope: CoroutineScope) {
        isEnabledState = mutableStateOf(appPreferences.crashAnalyticsEnabled.get())

        snapshotFlow { isEnabledState.value }
            .drop(1)
            .onEach { appPreferences.crashAnalyticsEnabled.set(it) }
            .launchIn(coroutineScope)
    }

    @Composable
    override fun content(mainNavigationState: MainNavigationState) {
        SettingsSwitchRow(
            title = "Crash Analytics",
            message = "Automated crash reports exported to /sdcard",
            isEnabled = isEnabledState.value,
            onToggled = {
                isEnabledState.value = !isEnabledState.value
            }
        )
    }

}
