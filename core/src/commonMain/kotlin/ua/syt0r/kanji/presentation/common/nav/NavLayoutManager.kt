package ua.syt0r.kanji.presentation.common.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.presentation.common.theme.LayoutConfig
import ua.syt0r.kanji.presentation.common.theme.NavAutoHide
import ua.syt0r.kanji.presentation.common.theme.SidebarMode
import ua.syt0r.kanji.presentation.common.theme.SidebarPosition

// ============================================
// NAV LAYOUT MANAGER
// Persists the navigation shell layout (mode, position, auto-hide, size,
// floating offset, accent) across launches via DataStore preferences.
// ============================================

class NavLayoutManager(
    private val appPreferences: PreferencesContract.AppPreferences,
    dispatcher: CoroutineDispatcher = Dispatchers.Unconfined
) {

    private val coroutineScope = CoroutineScope(dispatcher)

    val sidebarMode: MutableState<SidebarMode> = mutableStateOf(SidebarMode.Expanded)
    val sidebarPosition: MutableState<SidebarPosition> = mutableStateOf(SidebarPosition.Left)
    val autoHide: MutableState<NavAutoHide> = mutableStateOf(NavAutoHide.Never)
    val collapsed: MutableState<Boolean> = mutableStateOf(false)
    val panelWidth: MutableState<Dp> = mutableStateOf(260.dp)
    val panelHeight: MutableState<Dp> = mutableStateOf(56.dp)
    val floatingOffset: MutableState<DpOffset> = mutableStateOf(DpOffset.Zero)
    val accentIndex: MutableState<Int> = mutableStateOf(-1)

    init {
        reload()

        appPreferences.navSidebarMode.onModified.onEach { reload() }.launchIn(coroutineScope)
        appPreferences.navSidebarPosition.onModified.onEach { reload() }.launchIn(coroutineScope)
        appPreferences.navAutoHide.onModified.onEach { reload() }.launchIn(coroutineScope)
        appPreferences.navCollapsed.onModified.onEach { reload() }.launchIn(coroutineScope)
        appPreferences.navWidth.onModified.onEach { reload() }.launchIn(coroutineScope)
        appPreferences.navHeight.onModified.onEach { reload() }.launchIn(coroutineScope)
        appPreferences.navFloatingOffsetX.onModified.onEach { reload() }.launchIn(coroutineScope)
        appPreferences.navFloatingOffsetY.onModified.onEach { reload() }.launchIn(coroutineScope)
        appPreferences.navAccentIndex.onModified.onEach { reload() }.launchIn(coroutineScope)
    }

    fun syncFrom(config: LayoutConfig) {
        coroutineScope.launch {
            if (sidebarMode.value != config.sidebarMode)
                appPreferences.navSidebarMode.set(config.sidebarMode.name)
            if (sidebarPosition.value != config.sidebarPosition)
                appPreferences.navSidebarPosition.set(config.sidebarPosition.name)
            if (autoHide.value != config.autoHide)
                appPreferences.navAutoHide.set(config.autoHide.name)
            if (collapsed.value != config.collapsed)
                appPreferences.navCollapsed.set(config.collapsed)
            if (panelWidth.value != config.panelWidth)
                appPreferences.navWidth.set(config.panelWidth.value.roundToInt())
            if (panelHeight.value != config.panelHeight)
                appPreferences.navHeight.set(config.panelHeight.value.roundToInt())
            if (floatingOffset.value != config.floatingOffset) {
                appPreferences.navFloatingOffsetX.set(config.floatingOffset.x.value.roundToInt())
                appPreferences.navFloatingOffsetY.set(config.floatingOffset.y.value.roundToInt())
            }
            if (accentIndex.value != config.accentIndex)
                appPreferences.navAccentIndex.set(config.accentIndex)
        }
    }

    private fun reload() {
        coroutineScope.launch {
            val modeStr = appPreferences.navSidebarMode.get()
            val posStr = appPreferences.navSidebarPosition.get()
            val autoHideStr = appPreferences.navAutoHide.get()
            val isCollapsed = appPreferences.navCollapsed.get()
            val width = appPreferences.navWidth.get()
            val height = appPreferences.navHeight.get()
            val offsetX = appPreferences.navFloatingOffsetX.get()
            val offsetY = appPreferences.navFloatingOffsetY.get()
            val accent = appPreferences.navAccentIndex.get()

            sidebarMode.value = enumByName(modeStr, SidebarMode.Expanded)
            sidebarPosition.value = enumByName(posStr, SidebarPosition.Left)
            autoHide.value = enumByName(autoHideStr, NavAutoHide.Never)
            collapsed.value = isCollapsed
            panelWidth.value = width.dp
            panelHeight.value = height.dp
            floatingOffset.value = DpOffset(x = offsetX.dp, y = offsetY.dp)
            accentIndex.value = accent
        }
    }

    private inline fun <reified T : Enum<T>> enumByName(name: String?, default: T): T {
        if (name == null) return default
        return enumValues<T>().firstOrNull { it.name == name } ?: default
    }

}

@Composable
fun rememberNavLayoutManager(appPreferences: PreferencesContract.AppPreferences): NavLayoutManager {
    return androidx.compose.runtime.remember { NavLayoutManager(appPreferences) }
}
