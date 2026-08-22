package ua.syt0r.kanji.presentation.screen.main.screen.mind_map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import ua.syt0r.kanji.core.connected_learning.GraphEdgeKind
import ua.syt0r.kanji.core.connected_learning.GraphNodeKind
import ua.syt0r.kanji.core.connected_learning.LearningGraphConnection
import ua.syt0r.kanji.core.connected_learning.LearningGraphNode
import ua.syt0r.kanji.presentation.common.theme.LocalKaiteyoAccent
import ua.syt0r.kanji.presentation.common.theme.LocalSurfaceColors
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.features.KaiteyoDataCenter
import ua.syt0r.kanji.presentation.screen.main.features.MindMapCatalogItem
import ua.syt0r.kanji.presentation.screen.main.features.MindMapCatalogPage
import ua.syt0r.kanji.presentation.screen.main.features.MindMapExplorerMode
import ua.syt0r.kanji.presentation.screen.main.features.MindMapGraphSnapshot
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private const val MaxVisibleCanvasNodes = 192
private const val MinCanvasScale = 0.42f
private const val MaxCanvasScale = 2.8f

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
    var snapshot by remember(mode) { mutableStateOf<MindMapGraphSnapshot?>(null) }
    var loading by remember(mode) { mutableStateOf(true) }
    var graphLoading by remember(mode) { mutableStateOf(false) }
    var error by remember(mode) { mutableStateOf<String?>(null) }
    var showCatalog by remember(mode) { mutableStateOf(false) }
    var inspectedNodeKey by remember(mode) { mutableStateOf<String?>(null) }

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
            snapshot = null
            inspectedNodeKey = null
            return@LaunchedEffect
        }
        graphLoading = true
        error = null
        try {
            snapshot = dataCenter.loadMindMapNeighborhood(mode = mode, key = item.key)
            inspectedNodeKey = null
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            snapshot = null
            error = throwable.message ?: "Unable to load the connected graph"
        } finally {
            graphLoading = false
        }
    }

    val inspectedNode = snapshot?.nodes?.firstOrNull { it.nodeKey.value == inspectedNodeKey }
    val colors = LocalSurfaceColors.current

    Column(Modifier.fillMaxSize().background(colors.surface)) {
        ExplorerTopBar(
            mode = mode,
            selected = selected,
            onBack = navigationState::navigateBack,
            onOpenCatalog = { showCatalog = true },
            onClearSelection = {
                selected = null
                snapshot = null
            },
        )

        Box(Modifier.fillMaxSize()) {
            when {
                selected == null -> EmptyCanvasState(
                    mode = mode,
                    loading = loading,
                    error = error,
                    onOpenCatalog = { showCatalog = true },
                )

                snapshot == null && graphLoading -> {
                    CanvasLoadingState()
                }

                snapshot != null -> {
                    MindMapCanvasSurface(
                        snapshot = snapshot!!,
                        selectedNodeKey = inspectedNodeKey,
                        graphLoading = graphLoading,
                        onNodeSelected = { node ->
                            inspectedNodeKey = node.nodeKey.value
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                    snapshot?.let { graph ->
                        if (inspectedNode != null) {
                            MindMapInspector(
                                mode = mode,
                                node = inspectedNode,
                                graph = graph,
                                onOpenKanji = { kanji -> navigationState.navigate(MainDestination.KanjiDetail(kanji)) },
                                onOpenComponents = {
                                    if (mode == MindMapExplorerMode.COMPONENTS) {
                                        inspectedNodeKey = inspectedNode.nodeKey.value
                                    } else {
                                        navigationState.navigate(MainDestination.KanjiComponentMindMap)
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .navigationBarsPadding(),
                            )
                        }
                    }
                }
            }

            CanvasControlBar(
                scaleLabel = snapshot?.let { "Graph ${it.nodes.size} nodes" } ?: mode.itemLabel,
                onOpenCatalog = { showCatalog = true },
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
            )
        }
    }

    if (showCatalog) {
        ModalBottomSheet(onDismissRequest = { showCatalog = false }) {
            CatalogSheetContent(
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
                onSelect = {
                    selected = it
                    showCatalog = false
                },
                onLoadMore = { offset += catalogPage?.items?.size ?: 0 },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExplorerTopBar(
    mode: MindMapExplorerMode,
    selected: MindMapCatalogItem?,
    onBack: () -> Unit,
    onOpenCatalog: () -> Unit,
    onClearSelection: () -> Unit,
) {
    androidx.compose.material3.TopAppBar(
        title = {
            Column {
                Text(mode.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = selected?.label?.let { "Whiteboard · $it" } ?: "Canonical connected-learning graph",
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalSurfaceColors.current.textMuted,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onOpenCatalog) {
                Icon(Icons.Default.Search, contentDescription = "Open ${mode.itemLabel} catalog")
            }
            if (selected != null) {
                IconButton(onClick = onClearSelection) {
                    Icon(Icons.Default.Close, contentDescription = "Close graph")
                }
            }
        },
    )
}

@Composable
private fun EmptyCanvasState(
    mode: MindMapExplorerMode,
    loading: Boolean,
    error: String?,
    onOpenCatalog: () -> Unit,
) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
            color = LocalSurfaceColors.current.surfaceElevated,
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    Icons.Default.AccountTree,
                    contentDescription = null,
                    tint = LocalKaiteyoAccent.current.primary,
                    modifier = Modifier.size(48.dp),
                )
                Text(
                    text = "Choose a ${mode.singularLabel.lowercase()} to open its whiteboard",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Pan, pinch-zoom, and select canonical nodes. The graph is bounded for stable Android performance.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalSurfaceColors.current.textMuted,
                )
                Button(onClick = onOpenCatalog) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Open catalog")
                }
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun CanvasLoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CatalogSheetContent(
    mode: MindMapExplorerMode,
    query: String,
    onQueryChange: (String) -> Unit,
    page: MindMapCatalogPage?,
    selectedKey: String?,
    loading: Boolean,
    error: String?,
    onSelect: (MindMapCatalogItem) -> Unit,
    onLoadMore: () -> Unit,
) {
    val colors = LocalSurfaceColors.current
    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp, max = 680.dp).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${mode.title} catalog", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Select a root to render its graph", color = colors.textMuted, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.Tune, contentDescription = null, tint = LocalKaiteyoAccent.current.primary)
        }
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
            placeholder = { Text("Search ${mode.itemLabel}") },
        )
        if (loading && page == null) LinearProgressIndicator(Modifier.fillMaxWidth())
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
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (page?.items.isNullOrEmpty() && !loading) {
                item { Text("No ${mode.itemLabel.lowercase()} match this search.", color = colors.textMuted) }
            } else {
                items(page?.items.orEmpty(), key = { it.key }) { item ->
                    CatalogItemRow(item = item, selected = item.key == selectedKey, onClick = { onSelect(item) })
                }
                if (page?.hasMore == true) {
                    item {
                        TextButton(onClick = onLoadMore, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.ExpandMore, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Load more")
                        }
                    }
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .semantics(mergeDescendants = true) {
                contentDescription = "${item.label}, ${item.relatedKanjiCount} connected Kanji"
                role = Role.Button
            }
            .then(Modifier),
        color = if (selected) accent.primary.copy(alpha = 0.14f) else colors.surfaceElevated,
        shape = MaterialTheme.shapes.medium,
        onClick = onClick,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
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
        }
    }
}

@Composable
private fun MindMapCanvasSurface(
    snapshot: MindMapGraphSnapshot,
    selectedNodeKey: String?,
    graphLoading: Boolean,
    onNodeSelected: (LearningGraphNode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val colors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val outlineColor = MaterialTheme.colorScheme.outline
    val visibleNodes = remember(snapshot) {
        snapshot.nodes
            .distinctBy { it.nodeKey.value }
            .sortedWith(compareBy<LearningGraphNode> { it.depth }.thenBy { it.kind.ordinal }.thenBy { it.nodeKey.value })
            .take(MaxVisibleCanvasNodes)
    }
    val visibleNodeIds = remember(visibleNodes) { visibleNodes.mapTo(hashSetOf()) { it.nodeId } }
    val visibleConnections = remember(snapshot.connections, visibleNodeIds) {
        snapshot.connections.filter { connection ->
            connection.fromNodeId in visibleNodeIds && connection.toNodeId in visibleNodeIds
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val center = Offset(widthPx / 2f, heightPx / 2f)
        val positions = remember(snapshot.rootKey, visibleNodes, widthPx, heightPx) {
            layoutMindMapNodes(snapshot.rootKey, visibleNodes, widthPx, heightPx)
        }
        val nodeById = remember(visibleNodes) { visibleNodes.associateBy { it.nodeId } }
        val fitScale = remember(positions, widthPx, heightPx) { calculateFitScale(positions.values, widthPx, heightPx) }
        var scale by remember(snapshot.rootKey) { mutableStateOf(fitScale) }
        var pan by remember(snapshot.rootKey) { mutableStateOf(Offset.Zero) }

        LaunchedEffect(fitScale, snapshot.rootKey) {
            scale = fitScale
            pan = Offset.Zero
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.surface)
                .semantics {
                    contentDescription = "Interactive mind map canvas. Drag to pan and pinch to zoom."
                }
                .pointerInput(snapshot.rootKey) {
                    detectTransformGestures { _, panChange, zoomChange, _ ->
                        scale = (scale * zoomChange).coerceIn(MinCanvasScale, MaxCanvasScale)
                        pan = clampPan(pan + panChange, widthPx, heightPx)
                    }
                },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                visibleConnections.forEach { connection ->
                    val from = nodeById[connection.fromNodeId] ?: return@forEach
                    val to = nodeById[connection.toNodeId] ?: return@forEach
                    val fromPosition = transformPosition(positions[from.nodeKey.value] ?: return@forEach, center, scale, pan)
                    val toPosition = transformPosition(positions[to.nodeKey.value] ?: return@forEach, center, scale, pan)
                    drawConnection(
                        from = fromPosition,
                        to = toPosition,
                        edgeKind = connection.edgeKind,
                        color = if (connection.edgeKind == GraphEdgeKind.COMPOSED_OF) accent.primary.copy(alpha = 0.62f) else outlineColor.copy(alpha = 0.52f),
                        width = with(density) { (if (connection.edgeKind == GraphEdgeKind.COMPOSED_OF) 2.dp else 1.dp).toPx() },
                    )
                }
            }

            visibleNodes.forEach { node ->
                val worldPosition = positions[node.nodeKey.value] ?: return@forEach
                val screenPosition = transformPosition(worldPosition, center, scale, pan)
                val nodeSize = if (node.nodeKey.value == snapshot.rootKey) 92.dp else 76.dp
                val nodeSizePx = with(density) { nodeSize.toPx() }
                CanvasNode(
                    node = node,
                    selected = node.nodeKey.value == selectedNodeKey,
                    root = node.nodeKey.value == snapshot.rootKey,
                    onClick = { onNodeSelected(node) },
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (screenPosition.x - nodeSizePx / 2f).roundToInt(),
                                (screenPosition.y - nodeSizePx / 2f).roundToInt(),
                            )
                        }
                        .size(nodeSize)
                        .semantics {
                            contentDescription = "${nodeLabel(node)}, ${node.kind.name.lowercase()} node"
                            role = Role.Button
                        },
                )
            }

            CanvasZoomControls(
                scale = scale,
                onZoomIn = { scale = (scale * 1.2f).coerceAtMost(MaxCanvasScale) },
                onZoomOut = { scale = (scale / 1.2f).coerceAtLeast(MinCanvasScale) },
                onFit = {
                    scale = fitScale
                    pan = Offset.Zero
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            )

            if (graphLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }
    }
}

@Composable
private fun CanvasNode(
    node: LearningGraphNode,
    selected: Boolean,
    root: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val background = when {
        selected -> accent.primary.copy(alpha = 0.28f)
        root -> accent.primary.copy(alpha = 0.18f)
        node.kind == GraphNodeKind.KANJI -> colors.surfaceInteractive
        node.kind == GraphNodeKind.COMPONENT -> colors.surfaceElevated
        else -> colors.surfaceInteractive
    }
    Surface(
        modifier = modifier,
        onClick = onClick,
        color = background,
        shape = if (root) CircleShape else MaterialTheme.shapes.large,
        tonalElevation = if (selected) 4.dp else 0.dp,
        border = if (selected) BorderStroke(2.dp, accent.primary) else null,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = nodeLabel(node),
                color = if (selected || root) accent.primary else colors.textPrimary,
                style = if (root) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = node.kind.shortLabel,
                color = colors.textMuted,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CanvasZoomControls(
    scale: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onFit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = LocalSurfaceColors.current.surfaceElevated.copy(alpha = 0.94f),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 3.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onZoomOut) { Icon(Icons.Default.Remove, contentDescription = "Zoom out") }
            Text("${(scale * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium)
            IconButton(onClick = onZoomIn) { Icon(Icons.Default.Add, contentDescription = "Zoom in") }
            IconButton(onClick = onFit) { Icon(Icons.Default.FitScreen, contentDescription = "Fit graph") }
        }
    }
}

@Composable
private fun MindMapInspector(
    mode: MindMapExplorerMode,
    node: LearningGraphNode?,
    graph: MindMapGraphSnapshot,
    onOpenKanji: (String) -> Unit,
    onOpenComponents: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    Surface(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 76.dp),
        color = colors.surfaceElevated.copy(alpha = 0.97f),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (node == null) {
                Column(Modifier.weight(1f)) {
                    Text("Select a node", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Tap any node to inspect its canonical identity", color = colors.textMuted, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Column(Modifier.weight(1f)) {
                    Text(nodeLabel(node), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "${node.kind.shortLabel} · depth ${node.depth} · ${graph.connections.count { it.fromNodeId == node.nodeId || it.toNodeId == node.nodeId }} connections",
                        color = colors.textMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(node.nodeKey.value, color = colors.textMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                when {
                    node.kind == GraphNodeKind.KANJI && node.kanji != null -> {
                        TextButton(onClick = { onOpenKanji(node.kanji) }) {
                            Text("Details", color = accent.primary)
                        }
                    }
                    node.kind == GraphNodeKind.COMPONENT && mode == MindMapExplorerMode.RADICALS -> {
                        TextButton(onClick = onOpenComponents) { Text("Components", color = accent.primary) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CanvasControlBar(
    scaleLabel: String,
    onOpenCatalog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        color = LocalSurfaceColors.current.surfaceElevated.copy(alpha = 0.96f),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 3.dp,
    ) {
        TextButton(onClick = onOpenCatalog, modifier = Modifier.height(48.dp)) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Choose root · $scaleLabel")
        }
    }
}

private fun layoutMindMapNodes(
    rootKey: String,
    nodes: List<LearningGraphNode>,
    widthPx: Float,
    heightPx: Float,
): Map<String, Offset> {
    val center = Offset(widthPx / 2f, heightPx / 2f)
    val positions = linkedMapOf<String, Offset>()
    val root = nodes.firstOrNull { it.nodeKey.value == rootKey }
    root?.let { positions[it.nodeKey.value] = center }
    val grouped = nodes.filter { it.nodeKey.value != rootKey }.groupBy { it.depth.coerceAtLeast(1) }.toSortedMap()
    val radiusBase = min(widthPx, heightPx).coerceAtLeast(320f) * 0.22f
    grouped.forEach { (depth, group) ->
        val radius = radiusBase * depth
        val count = group.size.coerceAtLeast(1)
        group.forEachIndexed { index, node ->
            val angle = -Math.PI / 2.0 + (2.0 * Math.PI * index / count)
            positions[node.nodeKey.value] = Offset(
                x = center.x + (cos(angle) * radius).toFloat(),
                y = center.y + (sin(angle) * radius).toFloat(),
            )
        }
    }
    return positions
}

private fun calculateFitScale(positions: Collection<Offset>, widthPx: Float, heightPx: Float): Float {
    if (positions.isEmpty()) return 1f
    val minX = positions.minOf { it.x }
    val maxX = positions.maxOf { it.x }
    val minY = positions.minOf { it.y }
    val maxY = positions.maxOf { it.y }
    val contentWidth = (maxX - minX).coerceAtLeast(160f) + 120f
    val contentHeight = (maxY - minY).coerceAtLeast(160f) + 120f
    return min(widthPx / contentWidth, heightPx / contentHeight).coerceIn(MinCanvasScale, 1.15f)
}

private fun transformPosition(world: Offset, center: Offset, scale: Float, pan: Offset): Offset =
    Offset(
        x = center.x + (world.x - center.x) * scale + pan.x,
        y = center.y + (world.y - center.y) * scale + pan.y,
    )

private fun clampPan(pan: Offset, widthPx: Float, heightPx: Float): Offset =
    Offset(
        x = pan.x.coerceIn(-widthPx * 0.72f, widthPx * 0.72f),
        y = pan.y.coerceIn(-heightPx * 0.72f, heightPx * 0.72f),
    )

private fun DrawScope.drawConnection(
    from: Offset,
    to: Offset,
    edgeKind: GraphEdgeKind,
    color: androidx.compose.ui.graphics.Color,
    width: Float,
) {
    drawLine(
        color = color,
        start = from,
        end = to,
        strokeWidth = width,
        cap = if (edgeKind == GraphEdgeKind.COMPOSED_OF) StrokeCap.Round else StrokeCap.Butt,
    )
}

private fun nodeLabel(node: LearningGraphNode): String =
    node.kanji ?: node.reading ?: node.nodeKey.value.substringAfter(':')

private val GraphNodeKind.shortLabel: String
    get() = when (this) {
        GraphNodeKind.COMPONENT -> "component"
        GraphNodeKind.KANJI -> "kanji"
        GraphNodeKind.READING -> "reading"
        GraphNodeKind.VOCABULARY_ELEMENT -> "vocabulary"
        GraphNodeKind.LEGACY_VOCABULARY_ENTRY -> "vocabulary"
        GraphNodeKind.SENSE -> "sense"
        GraphNodeKind.SENTENCE -> "sentence"
        GraphNodeKind.GRAMMAR -> "grammar"
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
