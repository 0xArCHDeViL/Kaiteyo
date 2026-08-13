package ua.syt0r.kanji.presentation.common.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.core.user_data.preferences.PreferencesDefaultHomeTab
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.common.theme.LocalKaiteyoAccent
import ua.syt0r.kanji.presentation.common.theme.LocalSurfaceColors
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.HomeNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.HomeScreenTab
import ua.syt0r.kanji.presentation.screen.main.screen.home.rememberHomeNavigationState

class NavEntry(
    val id: String,
    val label: @Composable () -> String,
    val icon: ImageVector?,
    val iconContent: (@Composable () -> Unit)? = null,
    val selected: Boolean,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)

class NavSection(
    val title: (@Composable () -> String)?,
    val entries: List<NavEntry>
)

val LocalHomeNavigationState = compositionLocalOf<HomeNavigationState?> { null }

private val WideNavigationBreakpoint = 840.dp
private val CompactRailWidth = 92.dp
private val CompactItemRadius = 14.dp

/**
 * Navigation adapts to available width instead of device orientation. A narrow window,
 * including tablet portrait and split-screen, keeps content full-width and uses a bounded
 * horizontal strip. A genuinely wide window receives a compact rail, never a fixed large sidebar.
 */
@Composable
fun NavShell(
    navigationState: MainNavigationState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val appPreferences = koinInject<PreferencesContract.AppPreferences>()
    val defaultTab = rememberDefaultHomeTab(appPreferences)
    val homeNavState = rememberHomeNavigationState(defaultTab)
    val sections = buildNavSections(navigationState, homeNavState)

    CompositionLocalProvider(LocalHomeNavigationState provides homeNavState) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (maxWidth >= WideNavigationBreakpoint) {
                WideNavigationLayout(sections = sections, content = content)
            } else {
                CompactNavigationLayout(sections = sections, content = content)
            }
        }
    }
}

@Composable
private fun WideNavigationLayout(
    sections: List<NavSection>,
    content: @Composable () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        CompactNavigationRail(
            sections = sections,
            modifier = Modifier
                .width(CompactRailWidth)
                .fillMaxHeight()
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            content()
        }
    }
}

@Composable
private fun CompactNavigationLayout(
    sections: List<NavSection>,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            content()
        }
        CompactNavigationStrip(
            sections = sections,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CompactNavigationRail(
    sections: List<NavSection>,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        sections.flatMap(NavSection::entries).forEach { entry ->
            NavigationRailItem(
                selected = entry.selected,
                enabled = entry.enabled,
                onClick = entry.onClick,
                icon = { NavigationEntryIcon(entry) },
                label = {
                    Text(
                        text = entry.label(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                alwaysShowLabel = false
            )
        }
    }
}

@Composable
private fun CompactNavigationStrip(
    sections: List<NavSection>,
    modifier: Modifier = Modifier
) {
    val accent = LocalKaiteyoAccent.current
    val surfaceColors = LocalSurfaceColors.current
    val entries = sections.flatMap(NavSection::entries)

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            entries.forEach { entry ->
                val background = if (entry.selected) accent.primary.copy(alpha = 0.16f) else Color.Transparent
                val foreground = if (entry.selected) accent.primary else surfaceColors.textSecondary

                Row(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(background)
                        .then(
                            if (entry.enabled) {
                                Modifier.clickable(onClick = entry.onClick)
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigationEntryIcon(entry = entry, tint = foreground)
                    Text(
                        text = entry.label(),
                        style = MaterialTheme.typography.labelLarge,
                        color = foreground,
                        fontWeight = if (entry.selected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationEntryIcon(
    entry: NavEntry,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val icon = entry.icon
    if (icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = entry.label(),
            modifier = Modifier.size(22.dp),
            tint = tint
        )
    } else {
        Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
            entry.iconContent?.invoke()
        }
    }
}

@Composable
private fun buildNavSections(
    navigationState: MainNavigationState,
    homeNavState: HomeNavigationState
): List<NavSection> {
    val currentDestination = navigationState.currentDestination.value
    val onHome = currentDestination is MainDestination.Home
    val selectedTab = homeNavState.selectedTab.value
    val homeEntries = HomeScreenTab.VisibleTabs.map { tab ->
        NavEntry(
            id = "home_${tab.name}",
            label = { resolveString(tab.titleResolver) },
            icon = null,
            iconContent = tab.iconContent,
            selected = onHome && selectedTab == tab,
            onClick = {
                if (!onHome) navigationState.navigateToTop(MainDestination.Home)
                homeNavState.navigate(tab)
            }
        )
    }
    val featureEntries = listOf(
        Triple<MainDestination, @Composable () -> String, ImageVector>(
            MainDestination.DeckBrowser,
            { resolveString { nav.decksLabel } },
            Icons.Outlined.CollectionsBookmark
        ),
        Triple<MainDestination, @Composable () -> String, ImageVector>(
            MainDestination.TextAnalysis,
            { resolveString { nav.textAnalysisLabel } },
            Icons.Outlined.Translate
        ),
        Triple<MainDestination, @Composable () -> String, ImageVector>(
            MainDestination.StatisticsDashboard,
            { resolveString { home.statsTabLabel } },
            Icons.Outlined.BarChart
        )
    ).map { (destination, label, icon) ->
        destinationEntry(destination, label, icon, currentDestination, navigationState)
    }
    val systemEntries = listOf(
        destinationEntry(
            MainDestination.Backup,
            { resolveString { nav.backupLabel } },
            Icons.Outlined.CloudUpload,
            currentDestination,
            navigationState
        ),
        destinationEntry(
            MainDestination.Sync,
            { resolveString { nav.syncLabel } },
            Icons.Outlined.Sync,
            currentDestination,
            navigationState
        ),
        destinationEntry(
            MainDestination.About,
            { resolveString { nav.aboutLabel } },
            Icons.Outlined.Info,
            currentDestination,
            navigationState
        )
    )
    return listOf(
        NavSection(title = { resolveString { nav.homeSection } }, entries = homeEntries),
        NavSection(title = { resolveString { nav.featuresSection } }, entries = featureEntries),
        NavSection(title = { resolveString { nav.systemSection } }, entries = systemEntries)
    )
}

private fun destinationEntry(
    destination: MainDestination,
    label: @Composable () -> String,
    icon: ImageVector,
    currentDestination: MainDestination?,
    navigationState: MainNavigationState
): NavEntry = NavEntry(
    id = "dest_${destination.analyticsName ?: destination::class.simpleName}",
    label = label,
    icon = icon,
    selected = currentDestination == destination,
    onClick = { navigationState.navigate(destination) }
)

@Composable
private fun rememberDefaultHomeTab(
    appPreferences: PreferencesContract.AppPreferences
): HomeScreenTab {
    var tab by remember { mutableStateOf(HomeScreenTab.GeneralDashboard) }
    LaunchedEffect(Unit) {
        tab = when (appPreferences.defaultHomeTab.get()) {
            PreferencesDefaultHomeTab.GeneralDashboard -> HomeScreenTab.GeneralDashboard
            PreferencesDefaultHomeTab.Letters,
            PreferencesDefaultHomeTab.Vocab -> HomeScreenTab.Library
        }
    }
    return tab
}
