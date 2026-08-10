package ua.syt0r.kanji.presentation.screen.main.screen.library.screen.radicals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.app_data.data.RadicalData
import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.common.theme.LocalKaiteyoAccent
import ua.syt0r.kanji.presentation.common.theme.LocalSurfaceColors
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.features.KaiteyoDataCenter
import ua.syt0r.kanji.presentation.screen.main.screen.kanji_browser.KanjiBrowserCriteria

// ============================================
// PROPER RADICALS EXPLORER VIEW (Library Tab)
// Non-modal, full-page embedded explorer view
// for browsing Japanese Radicals (部首)
// ============================================

@Composable
fun RadicalsExplorerScreen(
    dataCenter: KaiteyoDataCenter,
    navigationState: MainNavigationState,
    onBack: () -> Unit
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val scope = rememberCoroutineScope()

    var allRadicals by remember { mutableStateOf<List<RadicalData>>(emptyList()) }
    var selectedRadicals by remember { mutableStateOf<Set<String>>(emptySet()) }
    var matchingKanji by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedStrokeFilter by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        allRadicals = dataCenter.loadRadicals()
        isLoading = false
    }

    LaunchedEffect(selectedRadicals) {
        if (selectedRadicals.isEmpty()) {
            matchingKanji = emptySet()
        } else {
            scope.launch {
                matchingKanji = dataCenter.loadCharactersWithRadicals(selectedRadicals)
            }
        }
    }

    val availableStrokeCounts = remember(allRadicals) {
        allRadicals.map { it.strokesCount }.distinct().sorted()
    }

    val filteredRadicals = remember(allRadicals, selectedStrokeFilter) {
        if (selectedStrokeFilter == null) allRadicals
        else allRadicals.filter { it.strokesCount == selectedStrokeFilter }
    }

    val orientation = LocalOrientation.current
    val isLandscape = orientation == Orientation.Landscape

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColors.background)
    ) {
        // --- Top Bar ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = surfaceColors.surfaceElevated,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Text("←", style = androidx.compose.material3.MaterialTheme.typography.titleLarge, color = surfaceColors.textPrimary)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "部首 Radical Explorer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = surfaceColors.textPrimary
                        )
                        Text(
                            text = if (selectedRadicals.isEmpty()) "Select radicals to filter kanji catalog"
                            else "${selectedRadicals.size} selected · ${matchingKanji.size} kanji found",
                            style = MaterialTheme.typography.bodySmall,
                            color = surfaceColors.textMuted
                        )
                    }

                    if (selectedRadicals.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { selectedRadicals = emptySet() },
                            color = accent.primary.copy(alpha = Dimens.Alpha.Light)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Clear All",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accent.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = accent.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // --- Selected Radicals Chips Bar ---
                AnimatedVisibility(visible = selectedRadicals.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(selectedRadicals.toList()) { radical ->
                            Surface(
                                shape = RoundedCornerShape(Dimens.RadiusMd),
                                color = accent.primary,
                                contentColor = accent.onPrimary
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clickable { selectedRadicals = selectedRadicals - radical }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = radical,
                                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // --- Stroke Count Filter Chips ---
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedStrokeFilter == null,
                            onClick = { selectedStrokeFilter = null },
                            label = { Text("All Strokes") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accent.primary,
                                selectedLabelColor = accent.onPrimary
                            )
                        )
                    }
                    items(availableStrokeCounts) { stroke ->
                        FilterChip(
                            selected = selectedStrokeFilter == stroke,
                            onClick = {
                                selectedStrokeFilter = if (selectedStrokeFilter == stroke) null else stroke
                            },
                            label = { Text("$stroke 画") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accent.primary,
                                selectedLabelColor = accent.onPrimary
                            )
                        )
                    }
                }
            }
        }

        // --- Main Content Area ---
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Radicals Grid Panel
                Surface(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(Dimens.RadiusXl))
                        .border(1.dp, surfaceColors.border.copy(alpha = Dimens.Alpha.Medium), RoundedCornerShape(Dimens.RadiusXl)),
                    color = surfaceColors.surface
                ) {
                    RadicalsMatrixGrid(
                        radicals = filteredRadicals,
                        selectedRadicals = selectedRadicals,
                        onToggleRadical = { radical ->
                            selectedRadicals = if (selectedRadicals.contains(radical)) {
                                selectedRadicals - radical
                            } else {
                                selectedRadicals + radical
                            }
                        }
                    )
                }

                // Matching Kanji Results Panel
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(Dimens.RadiusXl))
                        .border(1.dp, surfaceColors.border.copy(alpha = Dimens.Alpha.Medium), RoundedCornerShape(Dimens.RadiusXl)),
                    color = surfaceColors.surface
                ) {
                    MatchingKanjiResultsPanel(
                        selectedRadicals = selectedRadicals,
                        matchingKanji = matchingKanji,
                        navigationState = navigationState
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Radicals Matrix Panel
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.2f)
                        .clip(RoundedCornerShape(Dimens.RadiusXl))
                        .border(1.dp, surfaceColors.border.copy(alpha = Dimens.Alpha.Medium), RoundedCornerShape(Dimens.RadiusXl)),
                    color = surfaceColors.surface
                ) {
                    RadicalsMatrixGrid(
                        radicals = filteredRadicals,
                        selectedRadicals = selectedRadicals,
                        onToggleRadical = { radical ->
                            selectedRadicals = if (selectedRadicals.contains(radical)) {
                                selectedRadicals - radical
                            } else {
                                selectedRadicals + radical
                            }
                        }
                    )
                }

                // Matching Kanji Results Panel
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(Dimens.RadiusXl))
                        .border(1.dp, surfaceColors.border.copy(alpha = Dimens.Alpha.Medium), RoundedCornerShape(Dimens.RadiusXl)),
                    color = surfaceColors.surface
                ) {
                    MatchingKanjiResultsPanel(
                        selectedRadicals = selectedRadicals,
                        matchingKanji = matchingKanji,
                        navigationState = navigationState
                    )
                }
            }
        }
    }
}

@Composable
private fun RadicalsMatrixGrid(
    radicals: List<RadicalData>,
    selectedRadicals: Set<String>,
    onToggleRadical: (String) -> Unit
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Radicals Matrix (${radicals.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = surfaceColors.textPrimary
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(radicals, key = { it.radical }) { radicalData ->
                val isSelected = selectedRadicals.contains(radicalData.radical)
                val bgColor = if (isSelected) accent.primary else surfaceColors.surfaceElevated
                val textColor = if (isSelected) accent.onPrimary else surfaceColors.textPrimary
                val borderColor = if (isSelected) accent.primary else surfaceColors.border.copy(alpha = Dimens.Alpha.Medium)

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(Dimens.RadiusMd))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(Dimens.RadiusMd))
                        .clickable { onToggleRadical(radicalData.radical) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = radicalData.radical,
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "${radicalData.strokesCount}画",
                            fontSize = 9.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchingKanjiResultsPanel(
    selectedRadicals: Set<String>,
    matchingKanji: Set<String>,
    navigationState: MainNavigationState
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (selectedRadicals.isEmpty()) "Select a radical above"
                else "Matching Kanji (${matchingKanji.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = surfaceColors.textPrimary
            )
        }

        if (selectedRadicals.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        tint = surfaceColors.textMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Tap any radical to discover matching Kanji",
                        style = MaterialTheme.typography.bodyMedium,
                        color = surfaceColors.textMuted
                    )
                }
            }
        } else if (matchingKanji.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No kanji matches this combination of radicals",
                    style = MaterialTheme.typography.bodyMedium,
                    color = surfaceColors.textMuted
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 56.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(matchingKanji.toList()) { character ->
                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(Dimens.RadiusLg))
                            .border(1.dp, surfaceColors.border.copy(alpha = Dimens.Alpha.Medium), RoundedCornerShape(Dimens.RadiusLg))
                            .clickable {
                                navigationState.navigate(
                                    MainDestination.KanjiBrowser(KanjiBrowserCriteria(query = character))
                                )
                            },
                        color = surfaceColors.surfaceElevated
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = character,
                                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = accent.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
