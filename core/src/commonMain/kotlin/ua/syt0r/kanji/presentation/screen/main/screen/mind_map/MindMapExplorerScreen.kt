package ua.syt0r.kanji.presentation.screen.main.screen.mind_map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import ua.syt0r.kanji.core.connected_learning.GraphNodeKind
import ua.syt0r.kanji.core.connected_learning.LearningGraphNode
import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.common.theme.LocalKaiteyoAccent
import ua.syt0r.kanji.presentation.common.theme.LocalSurfaceColors
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.features.KaiteyoDataCenter
import ua.syt0r.kanji.presentation.screen.main.features.MindMapCatalogItem
import ua.syt0r.kanji.presentation.screen.main.features.MindMapCatalogPage
import ua.syt0r.kanji.presentation.screen.main.features.MindMapExplorerMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MindMapExplorerScreen(
    mode: MindMapExplorerMode,
    navigationState: MainNavigationState,
    dataCenter: KaiteyoDataCenter,
) {
    var query by remember(mode) { mutableStateOf("") }
    var offset by remember(mode) { mutableStateOf(0) }
    var catalogPage by remember(mode) { mutableStateOf<MindMapCatalogPage?>(null) }
    var selected by remember(mode) { mutableStateOf<MindMapCatalogItem?>(null) }
    var graph by remember(mode) { mutableStateOf<List<LearningGraphNode>>(emptyList()) }
    var loading by remember(mode) { mutableStateOf(true) }
    var graphLoading by remember(mode) { mutableStateOf(false) }
    var error by remember(mode) { mutableStateOf<String?>(null) }

    LaunchedEffect(mode, query, offset) {
        delay(180)
        loading = true
        error = null
        try {
            catalogPage = dataCenter.loadMindMapCatalogPage(
                mode = mode,
                query = query,
                offset = offset,
            )
            if (offset == 0) selected = null
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            catalogPage = null
            error = throwable.message ?: "Unable to load the mind-map catalog"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(selected) {
        val item = selected ?: run {
            graph = emptyList()
            return@LaunchedEffect
        }
        graphLoading = true
        try {
            graph = dataCenter.loadMindMapNeighborhood(mode = mode, key = item.key)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            graph = emptyList()
        } finally {
            graphLoading = false
        }
    }

    Column(Modifier.fillMaxSize().background(LocalSurfaceColors.current.surface)) {
        TopAppBar(
            title = {
                Column {
                    Text(mode.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Explore canonical connected relationships", style = MaterialTheme.typography.labelSmall, color = LocalSurfaceColors.current.textMuted)
                }
            },
            navigationIcon = {
                IconButton(onClick = { navigationState.navigateBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (selected != null) {
                    IconButton(onClick = { selected = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear selection")
                    }
                }
            },
        )
        BoxWithConstraints(Modifier.fillMaxSize().navigationBarsPadding()) {
            val expanded = maxWidth >= 840.dp
            if (expanded) {
                Row(Modifier.fillMaxSize()) {
                    CatalogPane(
                        mode = mode,
                        query = query,
                        onQueryChange = {
                            query = it
                            offset = 0
                        },
                        page = catalogPage,
                        selectedKey = selected?.key,
                        loading = loading,
                        error = error,
                        onSelect = { selected = it },
                        onLoadMore = { offset += catalogPage?.items?.size ?: 0 },
                        modifier = Modifier.width(360.dp).fillMaxSize(),
                    )
                    ExplorerDetailPane(
                        mode = mode,
                        selected = selected,
                        graph = graph,
                        loading = graphLoading,
                        onKanjiClick = { navigationState.navigate(MainDestination.KanjiDetail(it)) },
                        onComponentClick = { node ->
                            if (mode == MindMapExplorerMode.COMPONENTS) {
                                selected = MindMapCatalogItem(
                                    key = node.nodeKey.value,
                                    label = node.kanji ?: node.reading ?: node.nodeKey.value.substringAfter(':'),
                                    mode = MindMapExplorerMode.COMPONENTS,
                                    relatedKanjiCount = 0,
                                    nodeKind = node.kind,
                                )
                            } else {
                                navigationState.navigate(MainDestination.KanjiComponentMindMap)
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxSize(),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        CatalogPane(
                            mode = mode,
                            query = query,
                            onQueryChange = {
                                query = it
                                offset = 0
                            },
                            page = catalogPage,
                            selectedKey = selected?.key,
                            loading = loading,
                            error = error,
                            onSelect = { selected = it },
                            onLoadMore = { offset += catalogPage?.items?.size ?: 0 },
                        )
                    }
                    item {
                        ExplorerDetailPane(
                            mode = mode,
                            selected = selected,
                            graph = graph,
                            loading = graphLoading,
                            onKanjiClick = { navigationState.navigate(MainDestination.KanjiDetail(it)) },
                            onComponentClick = { node ->
                                selected = MindMapCatalogItem(
                                    key = node.nodeKey.value,
                                    label = node.kanji ?: node.reading ?: node.nodeKey.value.substringAfter(':'),
                                    mode = MindMapExplorerMode.COMPONENTS,
                                    relatedKanjiCount = 0,
                                    nodeKind = node.kind,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogPane(
    mode: MindMapExplorerMode,
    query: String,
    onQueryChange: (String) -> Unit,
    page: MindMapCatalogPage?,
    selectedKey: String?,
    loading: Boolean,
    error: String?,
    onSelect: (MindMapCatalogItem) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSurfaceColors.current
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            placeholder = { Text(if (mode == MindMapExplorerMode.RADICALS) "Search radicals" else "Search components") },
        )
        if (loading && page == null) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        error?.let {
            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
                Text(it, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
            }
        }
        Text(
            text = page?.let { "${it.totalCount} ${mode.itemLabel.lowercase()}" } ?: "Loading catalog…",
            color = colors.textMuted,
            style = MaterialTheme.typography.labelMedium,
        )
        if (page?.items.isNullOrEmpty() && !loading) {
            Text("No ${mode.itemLabel.lowercase()} match this search.", color = colors.textMuted)
        } else {
            page?.items?.forEach { item ->
                CatalogItemRow(
                    item = item,
                    selected = item.key == selectedKey,
                    onClick = { onSelect(item) },
                )
            }
            if (page?.hasMore == true) {
                TextButton(onClick = onLoadMore, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.ExpandMore, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Load more")
                }
            }
        }
    }
}

@Composable
private fun CatalogItemRow(item: MindMapCatalogItem, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = if (selected) accent.primary.copy(alpha = 0.12f) else colors.surfaceElevated,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                color = if (selected) accent.primary.copy(alpha = 0.18f) else colors.surfaceInteractive,
                shape = CircleShape,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(item.label, color = if (selected) accent.primary else colors.textPrimary, style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.label, color = colors.textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    buildString {
                        append(item.relatedKanjiCount)
                        append(" connected Kanji")
                        item.strokeCount?.let { append(" · $it strokes") }
                    },
                    color = colors.textMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Explore ${item.label}", tint = colors.textMuted)
        }
    }
}

@Composable
private fun ExplorerDetailPane(
    mode: MindMapExplorerMode,
    selected: MindMapCatalogItem?,
    graph: List<LearningGraphNode>,
    loading: Boolean,
    onKanjiClick: (String) -> Unit,
    onComponentClick: (LearningGraphNode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (selected == null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.surfaceElevated,
                shape = MaterialTheme.shapes.large,
            ) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AccountTree, contentDescription = null, tint = accent.primary, modifier = Modifier.size(32.dp))
                    Text("Select a ${mode.singularLabel.lowercase()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "Explore the connected Kanji graph without leaving this screen. Every node is bounded and linked to its canonical detail page.",
                        color = colors.textMuted,
                    )
                }
            }
            return@Column
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(selected.label, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                Text(mode.singularLabel, color = colors.textMuted, style = MaterialTheme.typography.labelMedium)
            }
            if (loading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }
        MindMapStage(
            root = selected.label,
            graph = graph,
            onKanjiClick = onKanjiClick,
            onComponentClick = onComponentClick,
        )
        GraphNodeGroups(
            graph = graph,
            onKanjiClick = onKanjiClick,
            onComponentClick = onComponentClick,
        )
    }
}

@Composable
private fun MindMapStage(
    root: String,
    graph: List<LearningGraphNode>,
    onKanjiClick: (String) -> Unit,
    onComponentClick: (LearningGraphNode) -> Unit,
) {
    val colors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val nodes = graph.filter { it.kanji != root || it.kind != GraphNodeKind.KANJI }.distinctBy { it.nodeKey.value }.take(20)
    Surface(color = colors.surfaceElevated, shape = MaterialTheme.shapes.large) {
        Box(Modifier.fillMaxWidth().height(240.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, 62.dp.toPx())
                nodes.forEachIndexed { index, _ ->
                    val x = size.width * ((index % 5) + 1) / 6f
                    val y = 148.dp.toPx() + (index / 5) * 34.dp.toPx()
                    drawLine(
                        color = accent.primary.copy(alpha = 0.24f),
                        start = center,
                        end = Offset(x, y),
                        strokeWidth = 2.dp.toPx(),
                    )
                }
            }
            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                MindMapNode(label = root, kind = "ROOT", emphasized = true, onClick = {})
                Spacer(Modifier.height(38.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    nodes.take(5).forEach { node ->
                        MindMapNode(
                            label = node.kanji ?: node.reading ?: node.nodeKey.value.substringAfter(':'),
                            kind = node.kind.name,
                            emphasized = false,
                            onClick = {
                                when {
                                    node.kind == GraphNodeKind.KANJI && node.kanji != null -> onKanjiClick(node.kanji)
                                    node.kind == GraphNodeKind.COMPONENT -> onComponentClick(node)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GraphNodeGroups(
    graph: List<LearningGraphNode>,
    onKanjiClick: (String) -> Unit,
    onComponentClick: (LearningGraphNode) -> Unit,
) {
    val colors = LocalSurfaceColors.current
    val groups = graph
        .filter { it.kind != GraphNodeKind.KANJI || it.depth > 0 }
        .groupBy { it.kind }
        .toSortedMap(compareBy { it.ordinal })
    groups.forEach { (kind, nodes) ->
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(kind.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            nodes.distinctBy { it.nodeKey.value }.take(64).forEach { node ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable {
                        when {
                            node.kind == GraphNodeKind.KANJI && node.kanji != null -> onKanjiClick(node.kanji)
                            node.kind == GraphNodeKind.COMPONENT -> onComponentClick(node)
                        }
                    },
                    color = colors.surfaceInteractive,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Hub, contentDescription = null, tint = LocalKaiteyoAccent.current.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(node.kanji ?: node.reading ?: node.nodeKey.value.substringAfter(':'), style = MaterialTheme.typography.bodyLarge)
                            Text(node.nodeKey.value, style = MaterialTheme.typography.labelSmall, color = colors.textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (node.kind == GraphNodeKind.KANJI || node.kind == GraphNodeKind.COMPONENT) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.textMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MindMapNode(label: String, kind: String, emphasized: Boolean, onClick: () -> Unit) {
    val colors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = if (emphasized) accent.primary.copy(alpha = 0.18f) else colors.surfaceInteractive,
        shape = RoundedCornerShape(if (emphasized) Dimens.RadiusLg else Dimens.RadiusMd),
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = if (emphasized) accent.primary else colors.textPrimary, fontSize = if (emphasized) MaterialTheme.typography.headlineMedium.fontSize else MaterialTheme.typography.bodyLarge.fontSize, fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Medium)
            Text(kind, color = colors.textMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private val MindMapExplorerMode.title: String
    get() = when (this) {
        MindMapExplorerMode.RADICALS -> "Radical mind map"
        MindMapExplorerMode.COMPONENTS -> "Kanji component mind map"
    }

private val MindMapExplorerMode.itemLabel: String
    get() = when (this) {
        MindMapExplorerMode.RADICALS -> "radicals"
        MindMapExplorerMode.COMPONENTS -> "components"
    }

private val MindMapExplorerMode.singularLabel: String
    get() = when (this) {
        MindMapExplorerMode.RADICALS -> "Radical"
        MindMapExplorerMode.COMPONENTS -> "Kanji component"
    }

private val GraphNodeKind.displayName: String
    get() = name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
