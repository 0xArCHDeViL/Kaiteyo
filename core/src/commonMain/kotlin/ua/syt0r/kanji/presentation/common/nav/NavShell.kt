package ua.syt0r.kanji.presentation.common.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

/**
 * Keeps one mobile navigation hierarchy across every Android device class.
 * Phone orientation is enforced by the activity; tablet/pad orientation is left to Android.
 * This host deliberately owns no navigation chrome or secondary state.
 */
@Composable
fun NavShell(
    navigationState: MainNavigationState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        content()
    }
}
