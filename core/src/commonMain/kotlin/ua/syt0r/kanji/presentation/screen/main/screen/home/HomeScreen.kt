package ua.syt0r.kanji.presentation.screen.main.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

@Composable
fun HomeScreen(
    mainNavigationState: MainNavigationState,
    viewModel: HomeScreenContract.ViewModel = getMultiplatformViewModel(),
) {
    val homeNavigationState = rememberHomeNavigationState(viewModel.defaultTab)

    HomeScreenUI(
        availableTabs = HomeScreenTab.VisibleTabs,
        selectedTabState = homeNavigationState.selectedTab,
        syncIconState = viewModel.syncIconState.collectAsState(),
        onTabSelected = homeNavigationState::navigate,
        onSyncButtonClick = {
            if (!viewModel.trySync()) mainNavigationState.navigate(MainDestination.Sync)
        }
    ) {
        HomeNavigationContent(homeNavigationState, mainNavigationState)
    }

    val analyticsManager = koinInject<AnalyticsManager>()
    LaunchedEffect(homeNavigationState) {
        snapshotFlow { homeNavigationState.selectedTab.value }
            .distinctUntilChanged()
            .onEach { analyticsManager.setScreen(it.analyticsName) }
            .launchIn(this)
    }
}
