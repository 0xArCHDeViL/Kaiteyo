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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.CrashLogCleaner
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsTextButton

class CrashAnalyticsSettingItem(
    private val appPreferences: PreferencesContract.AppPreferences,
    private val crashLogCleaner: CrashLogCleaner? = null
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
        var clearStatus by androidx.compose.runtime.remember { mutableStateOf<String?>(null) }

        Column {
            SettingsSwitchRow(
                title = "Crash Analytics",
                message = "Automated crash reports exported to storage",
                isEnabled = isEnabledState.value,
                onToggled = {
                    isEnabledState.value = !isEnabledState.value
                }
            )

            Spacer(Modifier.height(8.dp))

            SettingsTextButton(
                title = "Clear All Crash Logs",
                subtitle = clearStatus ?: "Delete all exported crash logs & junk files",
                onClick = {
                    val count = crashLogCleaner?.clearAllLogs() ?: 0
                    clearStatus = if (count > 0) "Successfully deleted $count crash log file(s)" else "Storage is already clean (0 log files found)"
                }
            )
        }
    }

}
