package ua.syt0r.kanji.presentation.screen.main.screen.home

import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

@Stable
interface HomeNavigationState {
    val selectedTab: State<HomeScreenTab>
    fun navigate(tab: HomeScreenTab)
}

@Composable
expect fun rememberHomeNavigationState(defaultTab: HomeScreenTab): HomeNavigationState

@Composable
expect fun HomeNavigationContent(
    homeNavigationState: HomeNavigationState,
    mainNavigationState: MainNavigationState
)


@Composable
fun rememberMultiplatformHomeNavigationState(defaultTab: HomeScreenTab): HomeNavigationState {
    val tabState = rememberSaveable { mutableStateOf<HomeScreenTab>(defaultTab) }
    return rememberSaveable { MultiplatformHomeNavigationState(tabState) }
}

class MultiplatformHomeNavigationState(
    override val selectedTab: MutableState<HomeScreenTab>
) : HomeNavigationState {
    override fun navigate(tab: HomeScreenTab) {
        selectedTab.value = tab
    }
}

@Composable
fun MultiplatformHomeNavigationContent(
    homeNavigationState: HomeNavigationState,
    mainNavigationState: MainNavigationState
) {
    homeNavigationState as MultiplatformHomeNavigationState

    val stateHolder = rememberSaveableStateHolder()

    androidx.compose.animation.AnimatedContent(
        targetState = homeNavigationState.selectedTab.value,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) togetherWith 
                androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(300))
        }
    ) { tab ->
        stateHolder.SaveableStateProvider(tab.name) {
            tab.content(mainNavigationState)
        }
    }

}
