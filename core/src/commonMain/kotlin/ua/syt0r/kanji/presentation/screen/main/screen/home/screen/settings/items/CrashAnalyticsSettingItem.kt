package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.items

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.CoroutineScope
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsSwitchRow

class CrashAnalyticsSettingItem(
    private val appPreferences: PreferencesContract.AppPreferences
) : SettingsScreenContract.ConfigurableListItem {

    override fun prepare(coroutineScope: CoroutineScope) {
        // Nothing to prepare
    }

    @Composable
    override fun content(mainNavigationState: MainNavigationState) {
        SettingsSwitchRow(
            title = "Crash Analytics",
            message = "Automated crash reports exported to /sdcard",
            isEnabled = appPreferences.crashAnalyticsEnabled.getAsStateFlow().collectAsState().value,
            onToggled = {
                appPreferences.crashAnalyticsEnabled.toggle()
            }
        )
    }

}
