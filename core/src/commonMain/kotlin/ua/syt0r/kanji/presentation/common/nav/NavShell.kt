package ua.syt0r.kanji.presentation.common.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.core.user_data.preferences.PreferencesDefaultHomeTab
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.common.theme.LocalKaiteyoAccent
import ua.syt0r.kanji.presentation.common.theme.LocalSurfaceColors
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
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

private val TabletRailWidth = 248.dp
private val TabletOuterPadding = 16.dp
private val TabletRailGap = 16.dp
private val TabletRailRadius = 24.dp
private val TabletItemRadius = 14.dp

@Composable
fun NavShell(
    navigationState: MainNavigationState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (LocalOrientation.current == Orientation.Portrait) {
        Box(modifier.fillMaxSize()) { content() }
        return
    }

    val appPreferences = koinInject<PreferencesContract.AppPreferences>()
    val defaultTab = rememberDefaultHomeTab(appPreferences)
    val homeNavState = rememberHomeNavigationState(defaultTab)
    val sections = buildNavSections(navigationState, homeNavState)

    CompositionLocalProvider(LocalHomeNavigationState provides homeNavState) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(TabletOuterPadding),
            horizontalArrangement = Arrangement.spacedBy(TabletRailGap)
        ) {
            TabletNavigationRail(
                sections = sections,
                modifier = Modifier
                    .width(TabletRailWidth)
                    .fillMaxHeight()
            )
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(TabletRailRadius),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Box(Modifier.fillMaxSize()) { content() }
            }
        }
    }
}

@Composable
private fun TabletNavigationRail(
    sections: List<NavSection>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(TabletRailRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Kaiteyo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
            sections.forEach { section ->
                section.title?.let { title ->
                    Text(
                        text = title(),
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalSurfaceColors.current.textMuted,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 2.dp)
                    )
                }
                section.entries.forEach { entry ->
                    TabletNavigationItem(entry)
                }
            }
        }
    }
}

@Composable
private fun TabletNavigationItem(entry: NavEntry) {
    val accent = LocalKaiteyoAccent.current
    val surfaceColors = LocalSurfaceColors.current
    val background = if (entry.selected) {
        accent.primary.copy(alpha = 0.14f)
    } else {
        Color.Transparent
    }
    val foreground = if (entry.selected) accent.primary else surfaceColors.textSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TabletItemRadius))
            .background(background)
            .clickable(enabled = entry.enabled, onClick = entry.onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val icon = entry.icon
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = entry.label(),
                modifier = Modifier.size(22.dp),
                tint = foreground
            )
        } else {
            Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
                entry.iconContent?.invoke()
            }
        }
        Text(
            text = entry.label(),
            style = MaterialTheme.typography.labelLarge,
            color = foreground,
            fontWeight = if (entry.selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
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
