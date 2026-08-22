@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ua.syt0r.kanji.presentation.screen.main.screen.kanji_browser

import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.common.resources.string.resolveString

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip as MaterialFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ua.syt0r.kanji.core.app_data.data.RadicalData
import ua.syt0r.kanji.presentation.common.kaiteyoClickable
import ua.syt0r.kanji.presentation.common.theme.LocalKaiteyoAccent
import ua.syt0r.kanji.presentation.common.theme.LocalSurfaceColors
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.features.KaiteyoDataCenter
import ua.syt0r.kanji.presentation.screen.main.screen.decks.CardFlagType
import ua.syt0r.kanji.presentation.screen.main.screen.decks.KaiteyoCard

// ============================================
// KANJI BROWSER
// Search · Radical search · JLPT · Grade ·
// Frequency · Strokes · Learned/Unlearned ·
// Difficult · Flagged · Favorites
// Grid / List / Detail
// ============================================

@Serializable
data class KanjiBrowserCriteria(
    val query: String = "",
    val jlptLevels: Set<Int> = emptySet(),
    val grades: Set<Int> = emptySet(),
    val minStrokes: Int? = null,
    val maxStrokes: Int? = null,
    val minFrequency: Int? = null,
    val maxFrequency: Int? = null,
    val showLearned: Boolean = false,
    val showUnlearned: Boolean = false,
    val showDifficult: Boolean = false,
    val showFlagged: Boolean = false,
    val flags: Set<Int> = emptySet(),
    val favoritesOnly: Boolean = false,
    val minLapses: Int? = null,
    val notReviewedDaysAgo: Int? = null,
    val radicals: Set<String> = emptySet(),
    val viewMode: KanjiBrowserViewMode = KanjiBrowserViewMode.Grid,
    val sortBy: KanjiBrowserSort = KanjiBrowserSort.Frequency
)

@Serializable
enum class KanjiBrowserViewMode { Grid, List }

@Serializable
enum class KanjiBrowserSort { Frequency, StrokeCount, JLPT, Difficulty, LastReviewed, Kanji }

@Composable
fun KanjiBrowserScreen(
    navigationState: MainNavigationState,
    dataCenter: KaiteyoDataCenter,
    initialCriteria: KanjiBrowserCriteria = KanjiBrowserCriteria()
) {
    var query by remember { mutableStateOf(initialCriteria.query) }
    var jlptLevels by remember { mutableStateOf(initialCriteria.jlptLevels) }
    var grades by remember { mutableStateOf(initialCriteria.grades) }
    var minStrokes by remember { mutableStateOf(initialCriteria.minStrokes) }
    var maxStrokes by remember { mutableStateOf(initialCriteria.maxStrokes) }
    var minFrequency by remember { mutableStateOf(initialCriteria.minFrequency) }
    var maxFrequency by remember { mutableStateOf(initialCriteria.maxFrequency) }
    var showLearned by remember { mutableStateOf(initialCriteria.showLearned) }
    var showUnlearned by remember { mutableStateOf(initialCriteria.showUnlearned) }
    var showDifficult by remember { mutableStateOf(initialCriteria.showDifficult) }
    var showFlagged by remember { mutableStateOf(initialCriteria.showFlagged) }
    var flags by remember { mutableStateOf(initialCriteria.flags) }
    var favoritesOnly by remember { mutableStateOf(initialCriteria.favoritesOnly) }
    var minLapses by remember { mutableStateOf(initialCriteria.minLapses) }
    var notReviewedDaysAgo by remember { mutableStateOf(initialCriteria.notReviewedDaysAgo) }
    var radicals by remember { mutableStateOf(initialCriteria.radicals) }
    var viewMode by remember { mutableStateOf(initialCriteria.viewMode) }
    var sortBy by remember { mutableStateOf(initialCriteria.sortBy) }

    var showFilters by remember { mutableStateOf(false) }
    var showRadicals by remember { mutableStateOf(initialCriteria.radicals.isNotEmpty()) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var radicalFilteredSet by remember { mutableStateOf<Set<String>?>(null) }
    var flagPickerTarget by remember { mutableStateOf<List<String>?>(null) }
    var tagPickerTarget by remember { mutableStateOf<List<String>?>(null) }

        val scope = rememberCoroutineScope()
    val strings = resolveString { kanjiBrowser }
    // Radical search: query DB for chars containing all selected radicals

    LaunchedEffect(radicals) {
        if (radicals.isEmpty()) {
            radicalFilteredSet = null
        } else {
            radicalFilteredSet = dataCenter.loadCharactersWithRadicals(radicals)
        }
    }

    val filteredCards by remember(dataCenter.cards, query, jlptLevels, grades, minStrokes, maxStrokes,
        minFrequency, maxFrequency, showLearned, showUnlearned, showDifficult, showFlagged, flags,
        favoritesOnly, minLapses, notReviewedDaysAgo, radicalFilteredSet, sortBy) {
        derivedStateOf {
            var list: List<KaiteyoCard> = dataCenter.cards
            val minS = minStrokes
            val maxS = maxStrokes
            val minF = minFrequency
            val maxF = maxFrequency
            val minL = minLapses
            val notReviewed = notReviewedDaysAgo
            if (radicalFilteredSet != null) {
                list = list.filter { it.id in radicalFilteredSet!! }
            }
            if (favoritesOnly) list = list.filter { dataCenter.isFavorite(it.id) }
            if (showLearned) list = list.filter { dataCenter.isLearned(it.id) }
            if (showUnlearned) list = list.filter { !dataCenter.isLearned(it.id) }
            if (showDifficult) list = list.filter { dataCenter.isDifficult(it.id) }
            if (showFlagged) list = list.filter { dataCenter.cardFlagsFor(it.id) != CardFlagType.None }
            if (flags.isNotEmpty()) list = list.filter { dataCenter.cardFlagsFor(it.id).id in flags }
            if (jlptLevels.isNotEmpty()) {
                list = list.filter { card ->
                    val classes = dataCenter.classifications[card.id].orEmpty()
                    jlptLevels.any { level -> classes.contains("n$level") }
                }
            }
            if (grades.isNotEmpty()) {
                list = list.filter { card ->
                    val classes = dataCenter.classifications[card.id].orEmpty()
                    grades.any { grade -> classes.contains("o$grade") }
                }
            }
            if (minS != null || maxS != null) {
                list = list.filter { card ->
                    val strokes = dataCenter.strokeCounts[card.id] ?: return@filter false
                    (minS == null || strokes >= minS) &&
                        (maxS == null || strokes <= maxS)
                }
            }
            if (minF != null || maxF != null) {
                list = list.filter { card ->
                    val freq = dataCenter.frequencies[card.id] ?: return@filter false
                    (minF == null || freq >= minF) &&
                        (maxF == null || freq <= maxF)
                }
            }
            if (minL != null) {
                list = list.filter { (dataCenter.srsCards[it.id]?.lapses ?: 0) >= minL }
            }
            if (notReviewed != null) {
                list = list.filter { dataCenter.notReviewedFor(it.id, notReviewed) }
            }
            if (query.isNotBlank()) {
                val q = query.trim()
                val lower = q.lowercase()
                list = list.filter { card ->
                    card.character.contains(q) ||
                        card.meaning.lowercase().contains(lower) ||
                        card.reading.contains(q)
                }
            }
            when (sortBy) {
                KanjiBrowserSort.Frequency -> list.sortedBy { dataCenter.frequencies[it.id] ?: Int.MAX_VALUE }
                KanjiBrowserSort.StrokeCount -> list.sortedBy { dataCenter.strokeCounts[it.id] ?: 0 }
                KanjiBrowserSort.JLPT -> list.sortedBy {
                    (dataCenter.classifications[it.id].orEmpty()
                        .firstOrNull { c -> c.startsWith("n") }?.drop(1)?.toIntOrNull()
                        ?: 99)
                }
                KanjiBrowserSort.Difficulty -> list.sortedByDescending { dataCenter.isDifficult(it.id) }
                KanjiBrowserSort.LastReviewed -> list.sortedByDescending {
                    dataCenter.srsCards[it.id]?.lastReview?.toEpochMilliseconds() ?: 0L
                }
                KanjiBrowserSort.Kanji -> list.sortedBy { it.id }
            }
        }
    }

    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        // Header
        BrowserHeader(
            query = query,
            onQueryChange = { query = it },
            showFilters = showFilters,
            onToggleFilters = { showFilters = !showFilters },
            showRadicals = showRadicals,
            onToggleRadicals = { showRadicals = !showRadicals },
            viewMode = viewMode,
            onViewModeChange = { viewMode = it },
            selectionMode = selectionMode,
            onToggleSelectionMode = { selectionMode = !selectionMode },
            onClose = { navigationState.navigateBack() }
        )

        // Selection toolbar
        AnimatedVisibility(visible = selectionMode) {
            SelectionToolbar(
                selectedCount = selectedIds.size,
                onClear = { selectedIds = emptySet() },
                onFlag = { flagPickerTarget = selectedIds.toList() },
                onTag = { tagPickerTarget = selectedIds.toList() },
                onFavorite = {
                    scope.launch {
                        selectedIds.forEach { dataCenter.toggleFavorite(it) }
                        selectedIds = emptySet()
                    }
                },
                onResetProgress = {
                    scope.launch {
                        dataCenter.resetProgress(selectedIds.toList())
                        selectedIds = emptySet()
                    }
                }
            )
        }

        // Filters panel
        AnimatedVisibility(visible = showFilters) {
            BrowserFilters(
                jlptLevels = jlptLevels,
                onJlptToggle = { level ->
                    jlptLevels = if (level in jlptLevels) jlptLevels - level else jlptLevels + level
                },
                grades = grades,
                onGradeToggle = { grade ->
                    grades = if (grade in grades) grades - grade else grades + grade
                },
                minStrokes = minStrokes,
                maxStrokes = maxStrokes,
                onMinStrokes = { minStrokes = it },
                onMaxStrokes = { maxStrokes = it },
                minFrequency = minFrequency,
                maxFrequency = maxFrequency,
                onMinFrequency = { minFrequency = it },
                onMaxFrequency = { maxFrequency = it },
                showLearned = showLearned,
                onShowLearned = { showLearned = it },
                showUnlearned = showUnlearned,
                onShowUnlearned = { showUnlearned = it },
                showDifficult = showDifficult,
                onShowDifficult = { showDifficult = it },
                showFlagged = showFlagged,
                onShowFlagged = { showFlagged = it },
                flags = flags,
                onFlagToggle = { flagId ->
                    flags = if (flagId in flags) flags - flagId else flags + flagId
                },
                favoritesOnly = favoritesOnly,
                onFavoritesOnly = { favoritesOnly = it },
                minLapses = minLapses,
                onMinLapses = { minLapses = it },
                notReviewedDaysAgo = notReviewedDaysAgo,
                onNotReviewedDaysAgo = { notReviewedDaysAgo = it },
                sortBy = sortBy,
                onSortBy = { sortBy = it },
                onReset = {
                    jlptLevels = emptySet(); grades = emptySet()
                    minStrokes = null; maxStrokes = null
                    minFrequency = null; maxFrequency = null
                    showLearned = false; showUnlearned = false
                    showDifficult = false; showFlagged = false
                    flags = emptySet(); favoritesOnly = false
                    minLapses = null; notReviewedDaysAgo = null
                }
            )
        }

        // Radical picker
        AnimatedVisibility(visible = showRadicals) {
            RadicalPicker(
                dataCenter = dataCenter,
                selectedRadicals = radicals,
                onRadicalsChange = { radicals = it },
                scope = scope
            )
        }

        // Results
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                dataCenter.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(strings.loadingMessage, color = surfaceColors.textMuted)
                }
                filteredCards.isEmpty() -> BrowserEmptyState(
                    hasFilters = query.isNotBlank() || jlptLevels.isNotEmpty() || grades.isNotEmpty() ||
                        flags.isNotEmpty() || showFlagged || favoritesOnly || radicals.isNotEmpty(),
                    onClear = {
                        query = ""; jlptLevels = emptySet(); grades = emptySet()
                        flags = emptySet(); showFlagged = false; favoritesOnly = false
                        radicals = emptySet(); radicalFilteredSet = null
                    }
                )
                else -> when (viewMode) {
                    KanjiBrowserViewMode.Grid -> LazyVerticalGrid(
                        columns = GridCells.Adaptive(88.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredCards, key = { it.id }) { card ->
                            KanjiGridTile(
                                card = card,
                                dataCenter = dataCenter,
                                selectionMode = selectionMode,
                                isSelected = card.id in selectedIds,
                                onSelect = {
                                    selectedIds = if (it in selectedIds) selectedIds - it else selectedIds + it
                                },
                                onClick = { navigationState.navigate(MainDestination.KanjiDetail(card.id)) },
                                onLongClick = {
                                    selectionMode = true
                                    selectedIds = setOf(card.id)
                                }
                            )
                        }
                    }
                    KanjiBrowserViewMode.List -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredCards, key = { it.id }) { card ->
                            KanjiListRow(
                                card = card,
                                dataCenter = dataCenter,
                                selectionMode = selectionMode,
                                isSelected = card.id in selectedIds,
                                onSelect = {
                                    selectedIds = if (it in selectedIds) selectedIds - it else selectedIds + it
                                },
                                onClick = { navigationState.navigate(MainDestination.KanjiDetail(card.id)) },
                                onLongClick = {
                                    selectionMode = true
                                    selectedIds = setOf(card.id)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Result count bar
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
            Text(
                text = "${filteredCards.size} kanji",
                color = surfaceColors.textMuted,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
    }

    flagPickerTarget?.let { targets ->
        FlagPickerDialog(
            currentFlag = targets.singleOrNull()?.let { dataCenter.cardFlagsFor(it) },
            onPick = { flagType ->
                scope.launch { dataCenter.setFlag(targets, flagType) }
                flagPickerTarget = null
                selectedIds = emptySet()
                selectionMode = false
            },
            onDismiss = { flagPickerTarget = null }
        )
    }

    tagPickerTarget?.let { targets ->
        TagPickerDialog(
            dataCenter = dataCenter,
            onApply = { tagId, add ->
                scope.launch {
                    if (add) dataCenter.addTagToCards(targets, tagId)
                    else dataCenter.removeTagFromCards(targets, tagId)
                }
                tagPickerTarget = null
            },
            onDismiss = { tagPickerTarget = null }
        )
    }
}

// ============================================
// Header
// ============================================

@Composable
private fun BrowserHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    showFilters: Boolean,
    onToggleFilters: () -> Unit,
    showRadicals: Boolean,
    onToggleRadicals: () -> Unit,
    viewMode: KanjiBrowserViewMode,
    onViewModeChange: (KanjiBrowserViewMode) -> Unit,
    selectionMode: Boolean,
    onToggleSelectionMode: () -> Unit,
    onClose: () -> Unit
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val strings = resolveString { kanjiBrowser }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = strings.navigateUpDescription,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = strings.title,
                color = surfaceColors.textPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )
        }
        HeaderIconButton(
            icon = Icons.Default.SelectAll,
            selected = selectionMode,
            contentDescription = if (selectionMode) strings.selectionModeActiveDescription else strings.selectionModeDescription,
            onClick = onToggleSelectionMode,
            accent = accent,
            surfaceColors = surfaceColors,
        )
        HeaderIconButton(
            icon = Icons.Default.FilterList,
            selected = showFilters,
            contentDescription = if (showFilters) strings.hideFiltersDescription else strings.showFiltersDescription,
            onClick = onToggleFilters,
            accent = accent,
            surfaceColors = surfaceColors,
        )
        HeaderIconButton(
            icon = Icons.Outlined.Flag,
            selected = showRadicals,
            contentDescription = if (showRadicals) strings.hideRadicalsDescription else strings.showRadicalsDescription,
            onClick = onToggleRadicals,
            accent = accent,
            surfaceColors = surfaceColors,
        )
        HeaderIconButton(
            icon = if (viewMode == KanjiBrowserViewMode.Grid) Icons.Default.List else Icons.Default.GridView,
            selected = false,
            contentDescription = if (viewMode == KanjiBrowserViewMode.Grid) strings.switchToListDescription else strings.switchToGridDescription,
            onClick = { onViewModeChange(if (viewMode == KanjiBrowserViewMode.Grid) KanjiBrowserViewMode.List else KanjiBrowserViewMode.Grid) },
            accent = accent,
            surfaceColors = surfaceColors
        )
    }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        singleLine = true,
        label = { Text(strings.searchLabel) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = strings.clearSearchDescription)
                }
            }
        },
        shape = MaterialTheme.shapes.small,
    )
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    contentDescription: String,
    onClick: () -> Unit,

    accent: ua.syt0r.kanji.presentation.common.theme.KaiteyoAccentScheme,
    surfaceColors: ua.syt0r.kanji.presentation.common.theme.SurfaceColors
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val bg by animateColorAsState(
        targetValue = if (selected) accent.primary.copy(alpha = Dimens.Alpha.Light)
        else if (hovered) surfaceColors.surfaceInteractive else Color.Transparent,
        label = "hdrBg"
    )
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(Dimens.RadiusMd))
            .background(bg)
            .hoverable(interactionSource),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .semantics { this.selected = selected },
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = if (selected) accent.primary else surfaceColors.textSecondary,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

// ============================================
// Selection toolbar
// ============================================

@Composable
private fun SelectionToolbar(
    selectedCount: Int,
    onClear: () -> Unit,
    onFlag: () -> Unit,
    onTag: () -> Unit,
    onFavorite: () -> Unit,
    onResetProgress: () -> Unit
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val strings = resolveString { kanjiBrowser }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(Dimens.RadiusLg))
            .background(surfaceColors.surfaceElevated)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = strings.selectedCount(selectedCount),
                color = accent.primary,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onClear) {
                Text(strings.clearSelection)
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BulkActionButton(strings.flagAction, onClick = onFlag)
            BulkActionButton(strings.tagAction, onClick = onTag)
            BulkActionButton(strings.favoriteAction, onClick = onFavorite)
            BulkActionButton(strings.resetProgressAction, onClick = onResetProgress)
        }
    }
}

@Composable
private fun BulkActionButton(
    label: String,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        Text(label)
    }
}

// ============================================
// Filters
// ============================================

@Composable
private fun BrowserFilters(
    jlptLevels: Set<Int>,
    onJlptToggle: (Int) -> Unit,
    grades: Set<Int>,
    onGradeToggle: (Int) -> Unit,
    minStrokes: Int?,
    maxStrokes: Int?,
    onMinStrokes: (Int?) -> Unit,
    onMaxStrokes: (Int?) -> Unit,
    minFrequency: Int?,
    maxFrequency: Int?,
    onMinFrequency: (Int?) -> Unit,
    onMaxFrequency: (Int?) -> Unit,
    showLearned: Boolean,
    onShowLearned: (Boolean) -> Unit,
    showUnlearned: Boolean,
    onShowUnlearned: (Boolean) -> Unit,
    showDifficult: Boolean,
    onShowDifficult: (Boolean) -> Unit,
    showFlagged: Boolean,
    onShowFlagged: (Boolean) -> Unit,
    flags: Set<Int>,
    onFlagToggle: (Int) -> Unit,
    favoritesOnly: Boolean,
    onFavoritesOnly: (Boolean) -> Unit,
    minLapses: Int?,
    onMinLapses: (Int?) -> Unit,
    notReviewedDaysAgo: Int?,
    onNotReviewedDaysAgo: (Int?) -> Unit,
    sortBy: KanjiBrowserSort,
    onSortBy: (KanjiBrowserSort) -> Unit,
    onReset: () -> Unit
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val strings = resolveString { kanjiBrowser }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(surfaceColors.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(strings.filtersTitle, color = surfaceColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onReset) {
                Text(strings.resetFilters, color = accent.primary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
        }

        FilterSection(strings.jlptFilter) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items((5 downTo 1).toList()) { level ->
                    FilterChip(
                        label = "N$level",
                        selected = level in jlptLevels,
                        onClick = { onJlptToggle(level) },
                        accent = accent,
                        surfaceColors = surfaceColors
                    )
                }
            }
        }

        FilterSection(strings.gradeFilter) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf(1, 2, 3, 4, 5, 6, 8, 9, 10)) { grade ->
                    FilterChip(
                        label = when (grade) {
                            8 -> "Secondary"
                            9 -> "Names"
                            10 -> "Names Var."
                            else -> grade.toString()
                        },
                        selected = grade in grades,
                        onClick = { onGradeToggle(grade) },
                        accent = accent,
                        surfaceColors = surfaceColors
                    )
                }
            }
        }

        FilterSection(strings.statusFilter) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip("Learned", showLearned, { onShowLearned(!showLearned) }, accent, surfaceColors)
                FilterChip("Unlearned", showUnlearned, { onShowUnlearned(!showUnlearned) }, accent, surfaceColors)
                FilterChip("Difficult", showDifficult, { onShowDifficult(!showDifficult) }, accent, surfaceColors)
                FilterChip("Flagged", showFlagged, { onShowFlagged(!showFlagged) }, accent, surfaceColors)
                FilterChip("Favorites", favoritesOnly, { onFavoritesOnly(!favoritesOnly) }, accent, surfaceColors)
                if (minLapses != null) {
                    FilterChip("Failed ≥ $minLapses", true, { onMinLapses(null) }, accent, surfaceColors)
                } else {
                    FilterChip("Failed 3+", false, { onMinLapses(3) }, accent, surfaceColors)
                }
                if (notReviewedDaysAgo != null) {
                    FilterChip("Not reviewed ≥ ${notReviewedDaysAgo}d", true, { onNotReviewedDaysAgo(null) }, accent, surfaceColors)
                } else {
                    FilterChip("Not reviewed 30d", false, { onNotReviewedDaysAgo(30) }, accent, surfaceColors)
                }
            }
        }

        FilterSection(strings.flagsFilter) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CardFlagType.entries.filter { it != CardFlagType.None }) { flag ->
                    FilterChip(
                        label = flag.displayName,
                        selected = flag.id in flags,
                        onClick = { onFlagToggle(flag.id) },
                        accent = accent,
                        surfaceColors = surfaceColors,
                        dotColor = flag.colorFromHex()
                    )
                }
            }
        }

        FilterSection(strings.strokesFilter) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                NumberStepper(value = minStrokes, label = strings.minLabel, onChange = onMinStrokes, accent = accent, surfaceColors = surfaceColors)
                Text("–", color = surfaceColors.textMuted)
                NumberStepper(value = maxStrokes, label = strings.maxLabel, onChange = onMaxStrokes, accent = accent, surfaceColors = surfaceColors)
            }
        }

        FilterSection(strings.frequencyFilter) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                NumberStepper(value = minFrequency, label = strings.minLabel, onChange = onMinFrequency, accent = accent, surfaceColors = surfaceColors)
                Text("–", color = surfaceColors.textMuted)
                NumberStepper(value = maxFrequency, label = strings.maxLabel, onChange = onMaxFrequency, accent = accent, surfaceColors = surfaceColors)
            }
        }

        FilterSection(strings.sortFilter) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(KanjiBrowserSort.entries) { sort ->
                    FilterChip(
                        label = sort.localizedLabel(strings),
                        selected = sortBy == sort,
                        onClick = { onSortBy(sort) },
                        accent = accent,
                        surfaceColors = surfaceColors
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    val surfaceColors = LocalSurfaceColors.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, color = surfaceColors.textMuted, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
        content()
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    accent: ua.syt0r.kanji.presentation.common.theme.KaiteyoAccentScheme,
    surfaceColors: ua.syt0r.kanji.presentation.common.theme.SurfaceColors,
    dotColor: Color? = null
) {
    MaterialFilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
            )
        },
        leadingIcon = dotColor?.let { color ->
            {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = surfaceColors.surfaceInteractive,
            labelColor = surfaceColors.textSecondary,
            selectedContainerColor = accent.primary.copy(alpha = 0.14f),
            selectedLabelColor = accent.primary,
        ),
    )
}

@Composable
private fun NumberStepper(
    value: Int?,
    label: String,
    onChange: (Int?) -> Unit,
    accent: ua.syt0r.kanji.presentation.common.theme.KaiteyoAccentScheme,
    surfaceColors: ua.syt0r.kanji.presentation.common.theme.SurfaceColors
) {
    val strings = resolveString { kanjiBrowser }
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(surfaceColors.surfaceInteractive)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, color = surfaceColors.textMuted, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(accent.primary.copy(alpha = Dimens.Alpha.Light))
                .semantics {
                    role = Role.Button
                    contentDescription = strings.decreaseValueDescription
                }
                .clickable {
                    val newValue = ((value ?: 0) - 1).coerceAtLeast(0)
                    onChange(newValue)
                },
            contentAlignment = Alignment.Center
        ) {
            Text("−", color = accent.primary, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = value?.toString() ?: strings.anyValue,
            color = surfaceColors.textPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(36.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(accent.primary.copy(alpha = Dimens.Alpha.Light))
                .semantics {
                    role = Role.Button
                    contentDescription = strings.increaseValueDescription
                }
                .clickable { onChange((value ?: 0) + 1) },
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = accent.primary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun KanjiBrowserSort.localizedLabel(strings: ua.syt0r.kanji.presentation.common.resources.string.KanjiBrowserStrings): String = when (this) {
    KanjiBrowserSort.Frequency -> strings.frequencyFilter
    KanjiBrowserSort.StrokeCount -> strings.strokesFilter
    KanjiBrowserSort.JLPT -> strings.jlptFilter
    KanjiBrowserSort.Difficulty -> strings.difficultySort
    KanjiBrowserSort.LastReviewed -> strings.lastReviewedSort
    KanjiBrowserSort.Kanji -> strings.kanjiSort
}

// ============================================
// Radical picker
// ============================================

@Composable
private fun RadicalPicker(
    dataCenter: KaiteyoDataCenter,
    selectedRadicals: Set<String>,
    onRadicalsChange: (Set<String>) -> Unit,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val strings = resolveString { kanjiBrowser }
    var radicals by remember { mutableStateOf<List<RadicalData>>(emptyList()) }

    LaunchedEffect(Unit) {
        radicals = dataCenter.loadRadicals()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(surfaceColors.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(strings.radicalSearchTitle, color = surfaceColors.textPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            if (selectedRadicals.isNotEmpty()) {
                Text(strings.radicalSelectedCount(selectedRadicals.size), color = accent.primary, style = MaterialTheme.typography.bodySmall)
            }
        }
        val grouped = radicals.groupBy { it.strokesCount }
        LazyColumn(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            grouped.toSortedMap().forEach { (count, group) ->
                item(key = "group-$count") {
                    Text(
                        text = strings.strokeCount(count),
                        color = surfaceColors.textMuted,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                item(key = "radicals-$count") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        group.forEach { radical ->
                            RadicalChip(
                                radical = radical.radical,
                                selected = radical.radical in selectedRadicals,
                                onClick = {
                                    onRadicalsChange(
                                        if (radical.radical in selectedRadicals) selectedRadicals - radical.radical
                                        else selectedRadicals + radical.radical
                                    )
                                },
                                accent = accent,
                                surfaceColors = surfaceColors
                            )
                        }
                    }
                }
            }
        }
        if (selectedRadicals.isNotEmpty()) {
            TextButton(
                onClick = { onRadicalsChange(emptySet()) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(strings.clearRadicals, color = accent.primary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun RadicalChip(
    radical: String,
    selected: Boolean,
    onClick: () -> Unit,
    accent: ua.syt0r.kanji.presentation.common.theme.KaiteyoAccentScheme,
    surfaceColors: ua.syt0r.kanji.presentation.common.theme.SurfaceColors
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(MaterialTheme.shapes.small)
            .background(if (selected) accent.primary.copy(alpha = 0.18f) else surfaceColors.surfaceInteractive)
            .border(1.dp, if (selected) accent.primary.copy(alpha = Dimens.Alpha.SemiOpaque) else Color.Transparent, MaterialTheme.shapes.small)
            .semantics {
                role = Role.Checkbox
                this.selected = selected
                contentDescription = radical
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            radical,
            color = if (selected) accent.primary else surfaceColors.textPrimary,
            fontSize = 17.sp
        )
    }
}

// ============================================
// Grid / List items
// ============================================

@Composable
private fun KanjiGridTile(
    card: KaiteyoCard,
    dataCenter: KaiteyoDataCenter,
    selectionMode: Boolean,
    isSelected: Boolean,
    onSelect: (String) -> Unit,
    onClick: (String) -> Unit,
    onLongClick: (String) -> Unit
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val strings = resolveString { kanjiBrowser }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    val bg by animateColorAsState(
        targetValue = when {
            isSelected -> accent.primary.copy(alpha = 0.16f)
            hovered -> surfaceColors.surfaceInteractive
            else -> surfaceColors.surface
        },
        label = "tileBg"
    )

    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(bg)
            .border(1.dp, if (isSelected) accent.primary.copy(alpha = Dimens.Alpha.SemiOpaque) else Color.Transparent, MaterialTheme.shapes.medium)
            .kaiteyoClickable(
                onClick = { if (selectionMode) onSelect(card.id) else onClick(card.id) },
                contentDescription = card.character,
                role = if (selectionMode) Role.Checkbox else Role.Button,
            )
            .semantics { this.selected = selectionMode && isSelected }
            .hoverable(interactionSource)
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = card.character,
                style = MaterialTheme.typography.headlineMedium,
                color = surfaceColors.textPrimary,
                fontWeight = FontWeight.Normal
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                card.flag.takeIf { it != CardFlagType.None }?.let { flag ->
                    Box(Modifier.size(6.dp).clip(CircleShape).background(flag.colorFromHex()))
                }
                if (card.isFavorite) {
                    Icon(Icons.Default.Star, null, tint = accent.secondary, modifier = Modifier.size(10.dp))
                }
                dataCenter.classifications[card.id].orEmpty()
                    .firstOrNull { it.startsWith("n") }
                    ?.let { level ->
                        Text(
                            text = level.uppercase(),
                            color = surfaceColors.textMuted,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
            }
        }
        if (selectionMode) {
            Icon(
                imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) accent.primary else surfaceColors.textMuted,
                modifier = Modifier.align(Alignment.TopStart).size(16.dp)
            )
        }
    }
}

@Composable
private fun KanjiListRow(
    card: KaiteyoCard,
    dataCenter: KaiteyoDataCenter,
    selectionMode: Boolean,
    isSelected: Boolean,
    onSelect: (String) -> Unit,
    onClick: (String) -> Unit,
    onLongClick: (String) -> Unit
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val strings = resolveString { kanjiBrowser }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    val bg by animateColorAsState(
        targetValue = when {
            isSelected -> accent.primary.copy(alpha = 0.14f)
            hovered -> surfaceColors.surfaceInteractive
            else -> surfaceColors.surface
        },
        label = "rowBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(bg)
            .kaiteyoClickable(
                onClick = { if (selectionMode) onSelect(card.id) else onClick(card.id) },
                contentDescription = card.character,
                role = if (selectionMode) Role.Checkbox else Role.Button,
            )
            .semantics { this.selected = selectionMode && isSelected }
            .hoverable(interactionSource)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (selectionMode) {
            Icon(
                imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) accent.primary else surfaceColors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = card.character,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            color = surfaceColors.textPrimary,
            modifier = Modifier.width(44.dp)
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = card.reading.ifBlank { "—" },
                color = surfaceColors.textSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                                        text = card.meaning.take(60).ifBlank { strings.noMeaning },

                color = surfaceColors.textMuted,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            dataCenter.strokeCounts[card.id]?.let { strokes ->
                Text(strings.strokeCount(strokes), color = surfaceColors.textMuted, style = MaterialTheme.typography.labelSmall)
            }
            dataCenter.classifications[card.id].orEmpty().firstOrNull { it.startsWith("n") }?.let {
                JlptBadge(it, accent, surfaceColors)
            }
            card.flag.takeIf { it != CardFlagType.None }?.let { flag ->
                Box(Modifier.size(8.dp).clip(CircleShape).background(flag.colorFromHex()))
            }
            if (card.isFavorite) {
                Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
            }
            if (dataCenter.isDifficult(card.id)) {
                Text(strings.difficultyWarning, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun JlptBadge(
    level: String,
    accent: ua.syt0r.kanji.presentation.common.theme.KaiteyoAccentScheme,
    surfaceColors: ua.syt0r.kanji.presentation.common.theme.SurfaceColors
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimens.RadiusSm))
            .background(accent.primary.copy(alpha = Dimens.Alpha.Light))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(level.uppercase(), color = accent.primary, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

// ============================================
// Empty state
// ============================================

@Composable
private fun BrowserEmptyState(hasFilters: Boolean, onClear: () -> Unit) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val strings = resolveString { kanjiBrowser }

    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(Dimens.RadiusXl))
                    .background(accent.primary.copy(alpha = Dimens.Alpha.Subtle)),
                contentAlignment = Alignment.Center
            ) {
                Text("字", style = androidx.compose.material3.MaterialTheme.typography.headlineLarge, color = accent.primary)
            }
            Text(strings.noKanjiFound, color = surfaceColors.textPrimary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                if (hasFilters) strings.adjustFiltersMessage else strings.searchPrompt,
                color = surfaceColors.textMuted,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
            if (hasFilters) {
                OutlinedButton(onClick = onClear) {
                    Text(strings.clearFilters, color = accent.primary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// ============================================
// ============================================

@Composable
fun FlagPickerDialog(
    currentFlag: CardFlagType?,
    onPick: (CardFlagType) -> Unit,
    onDismiss: () -> Unit
) {
    val surfaceColors = LocalSurfaceColors.current
    val strings = resolveString { kanjiBrowser }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp).padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = surfaceColors.surfaceElevated
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(strings.setFlagTitle, color = surfaceColors.textPrimary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                CardFlagType.entries.forEach { flag ->
                    val color = if (flag == CardFlagType.None) surfaceColors.textMuted else flag.colorFromHex()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(if (currentFlag == flag) color.copy(alpha = Dimens.Alpha.Light) else Color.Transparent)
                            .kaiteyoClickable(
                                onClick = { onPick(flag) },
                                contentDescription = if (flag == CardFlagType.None) strings.noFlag else flag.displayName,
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(Modifier.size(16.dp).clip(CircleShape).background(color))
                        Text(
                            if (flag == CardFlagType.None) strings.noFlag else flag.displayName,
                            color = surfaceColors.textPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.weight(1f))
                        if (currentFlag == flag) {
                            Icon(Icons.Default.Done, null, tint = color, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(strings.cancelButton, color = surfaceColors.textMuted)
                }
            }
        }
    }
}

// ============================================
// Tag picker dialog (bulk)
// ============================================

@Composable
fun TagPickerDialog(
    dataCenter: KaiteyoDataCenter,
    onApply: (Long, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val strings = resolveString { kanjiBrowser }
    var newTagName by remember { mutableStateOf("") }
    var newTagColor by remember { mutableStateOf("#C2FC8B") }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp).padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = surfaceColors.surfaceElevated
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(strings.tagsTitle, color = surfaceColors.textPrimary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)

                dataCenter.tags.forEach { tag ->
                    val color = tag.getDisplayColor()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(color.copy(alpha = 0.10f))
                            .kaiteyoClickable(
                                onClick = { onApply(tag.id, true) },
                                contentDescription = tag.name,
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(Modifier.size(12.dp).clip(CircleShape).background(color))
                        Text(tag.name, color = surfaceColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.weight(1f))
                        Text("+", color = color, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Dimens.RadiusMd))
                            .background(surfaceColors.surfaceInteractive)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = newTagName,
                            onValueChange = { newTagName = it },
                            textStyle = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(color = surfaceColors.textPrimary),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(accent.primary),
                            singleLine = true,
                            decorationBox = { inner ->
                                if (newTagName.isEmpty()) {
                                    Text(strings.newTagNamePlaceholder, color = surfaceColors.textMuted, style = MaterialTheme.typography.bodySmall)
                                }
                                inner()
                            }
                        )
                    }
                    TextButton(
                        enabled = newTagName.isNotBlank(),
                        onClick = {
                            scope.launch {
                                val id = dataCenter.createTag(newTagName.trim(), newTagColor)
                                onApply(id, true)
                            }
                        }
                    ) {
                        Text(strings.createButton, color = accent.primary, style = MaterialTheme.typography.bodySmall)
                    }
                }

                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(strings.cancelButton, color = surfaceColors.textMuted)
                }
            }
        }
    }
}
