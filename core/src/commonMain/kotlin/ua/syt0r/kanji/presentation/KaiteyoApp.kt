package ua.syt0r.kanji.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import ua.syt0r.kanji.core.app_data.AppDataSetupController
import ua.syt0r.kanji.core.app_data.AppDataSetupState
import ua.syt0r.kanji.core.theme_manager.ThemeManager
import ua.syt0r.kanji.core.user_data.preferences.PreferencesTheme
import ua.syt0r.kanji.presentation.common.theme.AllAccentSchemes
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.common.theme.BaseMode
import ua.syt0r.kanji.presentation.common.theme.KaiteyoThemeState
import ua.syt0r.kanji.presentation.common.theme.LocalKaiteyoThemeState
import ua.syt0r.kanji.presentation.common.theme.KaiteyoAccentScheme
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.MainScreen
import ua.syt0r.kanji.presentation.screen.main.features.DeepLinkHandler

internal fun resolveAppOrientation(
    widthSizeClass: WindowWidthSizeClass,
    heightSizeClass: WindowHeightSizeClass,
): Orientation {
    val isWideLandscapeWindow =
        widthSizeClass != WindowWidthSizeClass.Compact &&
            heightSizeClass == WindowHeightSizeClass.Compact
    return if (isWideLandscapeWindow) Orientation.Landscape else Orientation.Portrait
}

@Composable
fun KaiteyoApp(
    windowSizeClass: WindowSizeClass,
    deepLinkHandler: DeepLinkHandler = koinInject(),
    themeManager: ThemeManager = koinInject(),
    appDataSetupController: AppDataSetupController = koinInject()
) {

    LaunchedEffect(appDataSetupController) {
        appDataSetupController.initialize()
    }

    val orientation = resolveAppOrientation(
        widthSizeClass = windowSizeClass.widthSizeClass,
        heightSizeClass = windowSizeClass.heightSizeClass,
    )

    // themeManager.currentTheme is a compose State<PreferencesTheme>, so 'by' delegate works with import
    val currentPrefTheme: PreferencesTheme by themeManager.currentTheme
    val themeState = remember { KaiteyoThemeState() }

    // Map PreferencesTheme to BaseMode
    val baseMode: BaseMode = when (currentPrefTheme) {
        PreferencesTheme.System -> {
            @Suppress("DEPRECATION")
            val isDark = androidx.compose.foundation.isSystemInDarkTheme()
            if (isDark) BaseMode.Dark else BaseMode.Light
        }
        PreferencesTheme.Light -> BaseMode.Light
        PreferencesTheme.Dark -> BaseMode.Dark
        PreferencesTheme.Amoled -> BaseMode.Oled
    }

    val useDarkTheme = baseMode != BaseMode.Light

    // Update theme state when preference changes
    LaunchedEffect(baseMode) {
        themeState.baseMode = baseMode
    }

    // Keep the selected accent scheme in the app theme state across recomposition.
    val accentScheme: KaiteyoAccentScheme = themeState.accentScheme

    CompositionLocalProvider(
        LocalKaiteyoThemeState provides themeState
    ) {
        AppTheme(
            useDarkTheme = useDarkTheme,
            useAmoledTheme = currentPrefTheme == ua.syt0r.kanji.core.user_data.preferences.PreferencesTheme.Amoled,
            orientation = orientation,
            baseMode = baseMode,
            accentScheme = accentScheme
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                when (appDataSetupController.state.collectAsState().value) {
                    AppDataSetupState.Ready -> MainScreen(deepLinkHandler)
                    else -> AppDataSetupScreen(appDataSetupController)
                }
            }
        }
    }

}