@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ua.syt0r.kanji.presentation.screen.main.screen.library

import ua.syt0r.kanji.presentation.common.theme.Dimens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import ua.syt0r.kanji.presentation.common.theme.KaiteyoAccentScheme
import ua.syt0r.kanji.presentation.common.theme.LocalKaiteyoAccent
import ua.syt0r.kanji.presentation.common.theme.LocalSurfaceColors
import ua.syt0r.kanji.presentation.common.theme.SurfaceColors
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.features.KaiteyoDataCenter
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreen
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreen
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreen
import ua.syt0r.kanji.presentation.screen.main.screen.kanji_browser.KanjiBrowserCriteria
import ua.syt0r.kanji.presentation.screen.main.screen.library.screen.grammar.GrammarScreen
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.width

// ============================================
// LIBRARY — the central hub
// Replaces the former Kanji/Vocabulary split
// with one consistent interface. Sections:
// Kanji · Vocabulary · Grammar · Sentences ·
// Radicals · Custom · Collections · Favorites ·
// Pinned · Recently Studied · Smart Collections
// ============================================

@Composable
fun LibraryScreen(navigationState: MainNavigationState) {
    val dataCenter = koinInject<KaiteyoDataCenter>()
    LaunchedEffect(Unit) { dataCenter.ensureLoaded() }

    var view by remember { mutableStateOf<LibraryView>(LibraryView.Hub) }

    when (view) {
        LibraryView.Hub -> LibraryHub(
            navigationState = navigationState,
            dataCenter = dataCenter,
            onOpenKanjiDecks = { view = LibraryView.KanjiDecks },
            onOpenVocab = { view = LibraryView.Vocabulary },
            onOpenGrammar = { view = LibraryView.Grammar },
            onOpenWordSearch = { view = LibraryView.WordSearch },
            onOpenRadicalSearch = { view = LibraryView.RadicalSearch }
        )
        LibraryView.KanjiDecks -> DrillDownScaffold(title = "字  Kanji Decks", onBack = { view = LibraryView.Hub }) {
            LettersDashboardScreen(mainNavigationState = navigationState)
        }
        LibraryView.Vocabulary -> DrillDownScaffold(title = "語  Vocabulary", onBack = { view = LibraryView.Hub }) {
            VocabDashboardScreen(mainNavigationState = navigationState)
        }
        LibraryView.WordSearch -> DrillDownScaffold(title = "🔎  Word & Sentence Search", onBack = { view = LibraryView.Hub }) {
            SearchScreen(mainNavigationState = navigationState, startWithRadicals = false)
        }
        LibraryView.RadicalSearch -> ua.syt0r.kanji.presentation.screen.main.screen.library.screen.radicals.RadicalsExplorerScreen(
            dataCenter = dataCenter,
            navigationState = navigationState,
            onBack = { view = LibraryView.Hub }
        )
        LibraryView.Grammar -> GrammarScreen(
            onNavigateBack = { view = LibraryView.Hub },
            onNavigateToPractice = { navigationState.navigate(it) }
        )
    }
}

private sealed interface LibraryView {
    data object Hub : LibraryView
    data object KanjiDecks : LibraryView
    data object Vocabulary : LibraryView
    data object WordSearch : LibraryView
    data object RadicalSearch : LibraryView
    data object Grammar : LibraryView
}

@Composable
private fun DrillDownScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    val surfaceColors = LocalSurfaceColors.current
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = surfaceColors.textSecondary)
            }
            Text(
                text = title,
                color = surfaceColors.textPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Box(Modifier.fillMaxSize()) { content() }
    }
}

@Composable
private fun LibraryHub(
    navigationState: MainNavigationState,
    dataCenter: KaiteyoDataCenter,
    onOpenKanjiDecks: () -> Unit,
    onOpenVocab: () -> Unit,
    onOpenGrammar: () -> Unit,
    onOpenWordSearch: () -> Unit,
    onOpenRadicalSearch: () -> Unit = {}
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current

    var radicalCount by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(Unit) {
        radicalCount = runCatching { dataCenter.loadRadicals().size }.getOrNull()
    }

    if (dataCenter.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading library…", color = surfaceColors.textMuted)
        }
        return
    }
    if (dataCenter.loadError) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Could not load the library", color = surfaceColors.textMuted)
        }
        return
    }

    val favorites = dataCenter.favorites.value.size
    val collections = dataCenter.collections.size
    val customCount = dataCenter.collections.count { !it.isSmart }
    val smartCount = dataCenter.collections.count { it.isSmart }
    val recently = dataCenter.collections
        .firstOrNull { it.isSmart && it.name == "Recently learned" }
        ?.cardIds?.size ?: 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "header") {
            Column(Modifier.padding(top = 8.dp, bottom = 2.dp)) {
                Text(
                    text = "Library",
                    color = surfaceColors.textPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your study hub — everything in one place",
                    color = surfaceColors.textMuted,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }
        }

        item(key = "stats") {
            StatRow(
                items = listOf(
                    StatData("Kanji", dataCenter.cards.size),
                    StatData("Favorites", favorites),
                    StatData("Reviews", dataCenter.totalReviews.value.toInt()),
                    StatData("Tags", dataCenter.tags.size)
                ),
                accent = accent,
                surfaceColors = surfaceColors
            )
        }

        item(key = "study-title") { SectionTitle("STUDY", accent, surfaceColors) }

        item(key = "study-items") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionCard(
                    glyph = "字",
                    title = "Kanji",
                    subtitle = "Browse, filter & review all kanji",
                    count = dataCenter.cards.size,
                    onClick = { navigationState.navigate(MainDestination.KanjiBrowser()) },
                    accent = accent,
                    surfaceColors = surfaceColors
                )
                SectionCard(
                    glyph = "📚",
                    title = "Kanji Decks",
                    subtitle = "Letter decks & spaced repetition",
                    onClick = onOpenKanjiDecks,
                    accent = accent,
                    surfaceColors = surfaceColors
                )
                SectionCard(
                    glyph = "語",
                    title = "Vocabulary",
                    subtitle = "Words, terms & vocab decks",
                    onClick = onOpenVocab,
                    accent = accent,
                    surfaceColors = surfaceColors
                )
                SectionCard(
                    glyph = "文",
                    title = "Grammar",
                    subtitle = "Particles & grammar terms",
                    onClick = onOpenGrammar,
                    accent = accent,
                    surfaceColors = surfaceColors
                )
                SectionCard(
                    glyph = "例",
                    title = "Sentences",
                    subtitle = "Example sentences (Tatoeba)",
                    onClick = onOpenWordSearch,
                    accent = accent,
                    surfaceColors = surfaceColors
                )
                SectionCard(
                    glyph = "部",
                    title = "Radicals",
                    subtitle = radicalCount?.let { "$it radicals — search by parts" }
                        ?: "Search by radical parts",
                    onClick = onOpenRadicalSearch,
                    accent = accent,
                    surfaceColors = surfaceColors
                )
            }
        }

        item(key = "find-title") { SectionTitle("FIND & ORGANIZE", accent, surfaceColors) }

        item(key = "find-grid") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionCard(
                    glyph = "🗂",
                    title = "Collections",
                    subtitle = "All smart & saved collections",
                    count = collections,
                    onClick = { navigationState.navigate(MainDestination.Collections) },
                    accent = accent,
                    surfaceColors = surfaceColors
                )
                SectionCard(
                    glyph = "★",
                    title = "Favorites",
                    subtitle = "Your starred kanji",
                    count = favorites,
                    onClick = {
                        navigationState.navigate(MainDestination.KanjiBrowser(KanjiBrowserCriteria(favoritesOnly = true)))
                    },
                    accent = accent,
                    surfaceColors = surfaceColors
                )
                SectionCard(
                    glyph = "📌",
                    title = "Pinned",
                    subtitle = "Quick access pinned items",
                    count = 0,
                    onClick = { navigationState.navigate(MainDestination.Collections) },
                    accent = accent,
                    surfaceColors = surfaceColors
                )
                SectionCard(
                    glyph = "🕐",
                    title = "Recently Studied",
                    subtitle = "Studied in the last 24 hours",
                    count = recently,
                    onClick = { navigationState.navigate(MainDestination.Collections) },
                    accent = accent,
                    surfaceColors = surfaceColors
                )
                SectionCard(
                    glyph = "📁",
                    title = "Custom",
                    subtitle = "Saved filters & decks",
                    count = customCount,
                    onClick = { navigationState.navigate(MainDestination.Collections) },
                    accent = accent,
                    surfaceColors = surfaceColors
                )
                SectionCard(
                    glyph = "✨",
                    title = "Smart Collections",
                    subtitle = "Auto-generated sets",
                    count = smartCount,
                    onClick = { navigationState.navigate(MainDestination.Collections) },
                    accent = accent,
                    surfaceColors = surfaceColors
                )
                SectionCard(
                    glyph = "🚩",
                    title = "Flagged",
                    subtitle = "Kanji with any flag set",
                    count = dataCenter.flags.size,
                    onClick = {
                        navigationState.navigate(MainDestination.KanjiBrowser(KanjiBrowserCriteria(showFlagged = true)))
                    },
                    accent = accent,
                    surfaceColors = surfaceColors
                )
            }
        }

        item(key = "spacer") { Spacer(Modifier.height(8.dp)) }
    }
}

private data class StatData(val label: String, val value: Int)

@Composable
private fun StatRow(
    items: List<StatData>,
    accent: KaiteyoAccentScheme,
    surfaceColors: SurfaceColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { item ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Dimens.RadiusXl))
                    .background(surfaceColors.surface)
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = item.value.toString(),
                    color = accent.primary,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.label,
                    color = surfaceColors.textMuted,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, accent: KaiteyoAccentScheme, surfaceColors: SurfaceColors) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .width(4.dp)
                .height(14.dp)
                .clip(CircleShape)
                .background(accent.primary)
        )
        Text(
            text = title,
            color = accent.primary,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun SectionCard(
    glyph: String,
    title: String,
    subtitle: String,
    count: Int? = null,
    onClick: (() -> Unit)?,
    accent: KaiteyoAccentScheme,
    surfaceColors: SurfaceColors
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) surfaceColors.surfaceInteractive else surfaceColors.surface,
        animationSpec = tween(200)
    )

    val base = Modifier
        .fillMaxWidth()
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clip(RoundedCornerShape(Dimens.RadiusXl))
        .background(backgroundColor)

    val clickable = if (onClick != null) {
        base.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    } else {
        base
    }

    Row(
        modifier = clickable.padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(Dimens.RadiusLg))
                .background(accent.primary.copy(alpha = Dimens.Alpha.Light)),
            contentAlignment = Alignment.Center
        ) {
            Text(glyph, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = surfaceColors.textPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = surfaceColors.textMuted,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (count != null) {
            Text(
                text = count.toString(),
                color = accent.primary,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}