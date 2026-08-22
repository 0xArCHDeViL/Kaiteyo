package ua.syt0r.kanji.presentation.screen.main.screen.connected_learning

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import ua.syt0r.kanji.core.connected_learning.ConnectedNodeKey
import ua.syt0r.kanji.presentation.common.kaiteyoHeading
import ua.syt0r.kanji.core.connected_learning.GraphEdgeKind
import ua.syt0r.kanji.core.connected_learning.GraphNodeKind
import ua.syt0r.kanji.core.connected_learning.MasteryLevel

private val GraphNodeSize = 56.dp

@Composable
fun ConnectedLearningScreen(
    state: ConnectedLearningUiState,
    onEvent: (ConnectedLearningEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val compact = maxWidth < 600.dp
        val contentPadding = if (compact) 16.dp else 24.dp

        when {
            state.isLoading -> ConnectedLearningLoading(contentPadding)
            state.errorMessage != null -> ConnectedLearningError(
                message = state.errorMessage,
                padding = contentPadding,
                onRetry = { onEvent(ConnectedLearningEvent.Retry) },
            )
            else -> {
                if (compact) {
                    ConnectedLearningCompactContent(
                        state = state,
                        contentPadding = contentPadding,
                        onEvent = onEvent,
                    )
                } else {
                    ConnectedLearningExpandedContent(
                        state = state,
                        contentPadding = contentPadding,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectedLearningCompactContent(
    state: ConnectedLearningUiState,
    contentPadding: Dp,
    onEvent: (ConnectedLearningEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(contentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ConnectedLearningHeader(state, onEvent) }
        item {
            MasteryGraphCard(
                state = state,
                onEvent = onEvent,
            )
        }
        item { ConnectedNodeDetailCard(state, onEvent) }
        item { CandidateLessonSection(state, onEvent) }
    }
}

@Composable
private fun ConnectedLearningExpandedContent(
    state: ConnectedLearningUiState,
    contentPadding: Dp,
    onEvent: (ConnectedLearningEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = contentPadding, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ConnectedLearningHeader(state, onEvent)
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MasteryGraphCard(
                state = state,
                onEvent = onEvent,
                modifier = Modifier.weight(1.5f),
            )
            ConnectedNodeDetailCard(
                state = state,
                onEvent = onEvent,
                modifier = Modifier.weight(1f),
            )
        }
        CandidateLessonSection(state, onEvent)
    }
}

@Composable
private fun ConnectedLearningHeader(
    state: ConnectedLearningUiState,
    onEvent: (ConnectedLearningEvent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Connected Learning",
                modifier = Modifier.kaiteyoHeading(),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = state.pathSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = { onEvent(ConnectedLearningEvent.Refresh) }) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "Refresh connected learning path",
            )
        }
    }
}

@Composable
private fun MasteryGraphCard(
    state: ConnectedLearningUiState,
    onEvent: (ConnectedLearningEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoGraph,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Mastery map",
                    modifier = Modifier
                        .kaiteyoHeading()
                        .weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${state.graphNodes.size} nodes",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
            MasteryNodeGraph(
                nodes = state.graphNodes,
                edges = state.graphEdges,
                selectedNodeKey = state.selectedNodeKey,
                onNodeSelected = { onEvent(ConnectedLearningEvent.SelectNode(it)) },
            )
            Spacer(Modifier.height(10.dp))
            MasteryLegend()
        }
    }
}

@Composable
private fun MasteryNodeGraph(
    nodes: List<MasteryNodeUi>,
    edges: List<GraphEdgeUi>,
    selectedNodeKey: ConnectedNodeKey?,
    onNodeSelected: (ConnectedNodeKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    val colors = MaterialTheme.colorScheme
    val nodesByKey = remember(nodes) { nodes.associateBy { it.key } }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 320.dp, max = 520.dp)
            .clip(MaterialTheme.shapes.large)
            .background(colors.surface)
            .pointerInput(Unit) {
                detectTransformGestures { _, panChange, zoomChange, _ ->
                    zoom = (zoom * zoomChange).coerceIn(0.7f, 2.4f)
                    pan += panChange
                }
            }
            .semantics {
                contentDescription = "Mastery graph with ${nodes.size} nodes. Pinch to zoom and drag to pan."
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            edges.forEach { edge ->
                val from = nodesByKey[edge.from] ?: return@forEach
                val to = nodesByKey[edge.to] ?: return@forEach
                val start = transformedPosition(from.position, zoom, pan, density)
                val end = transformedPosition(to.position, zoom, pan, density)
                drawLine(
                    color = edgeColor(edge.kind, colors),
                    start = start,
                    end = end,
                    strokeWidth = 2.dp.toPx().coerceAtLeast(1f),
                    cap = StrokeCap.Round,
                )
            }
        }

        nodes.forEach { node ->
            val isSelected = node.key == selectedNodeKey
            val nodePosition = transformedPosition(node.position, zoom, pan, density)
            val nodeSize = if (node.isAnchor) 68.dp else GraphNodeSize
            val nodeSizePx = with(density) { nodeSize.roundToPx() }
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            nodePosition.x.roundToInt() - nodeSizePx / 2,
                            nodePosition.y.roundToInt() - nodeSizePx / 2,
                        )
                    }
                    .size(nodeSize)
                    .clip(CircleShape)
                    .background(nodeColor(node.kind, colors))
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                color = masteryColor(node.mastery, colors),
                                shape = CircleShape,
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onNodeSelected(node.key) }
                    .semantics {
                        contentDescription = "${node.label}, ${node.mastery.name.lowercase()}"
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = node.label,
                    fontSize = if (node.isAnchor) 24.sp else 18.sp,
                    color = colors.onPrimaryContainer,
                )
            }
        }
    }
}

private fun transformedPosition(
    position: GraphPoint,
    zoom: Float,
    pan: Offset,
    density: androidx.compose.ui.unit.Density,
): Offset = with(density) {
    Offset(
        x = position.x.toPx() * zoom + pan.x,
        y = position.y.toPx() * zoom + pan.y,
    )
}

@Composable
private fun ConnectedNodeDetailCard(
    state: ConnectedLearningUiState,
    onEvent: (ConnectedLearningEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val node = state.graphNodes.firstOrNull { it.key == state.selectedNodeKey }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        if (node == null) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Select a node", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Choose a Kanji, reading, or vocabulary node to inspect its learning state.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(node.label, style = MaterialTheme.typography.displaySmall)
                Text(node.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    node.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge(node.mastery.name)
                    StatusBadge(node.kind.name.lowercase())
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onEvent(ConnectedLearningEvent.StartLesson(node.key)) },
                ) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start connected lesson")
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(label: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun CandidateLessonSection(
    state: ConnectedLearningUiState,
    onEvent: (ConnectedLearningEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Recommended next",
            modifier = Modifier.kaiteyoHeading(),
            style = MaterialTheme.typography.titleLarge,
        )
        if (state.candidates.isEmpty()) {
            Text(
                "No new candidate lesson is available yet.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            state.candidates.forEach { candidate ->
                CandidateLessonCard(candidate, onEvent)
            }
        }
    }
}

@Composable
private fun CandidateLessonCard(
    candidate: CandidateLessonUi,
    onEvent: (ConnectedLearningEvent) -> Unit,
) {
    Card(
        onClick = { onEvent(ConnectedLearningEvent.StartLesson(candidate.rootKey)) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(candidate.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    candidate.rationale,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = candidate.isDue,
                        onClick = {},
                        label = { Text(if (candidate.isDue) "Due" else "New") },
                    )
                    Text(
                        "${candidate.estimatedMinutes} min",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            Text(
                text = "${(candidate.score * 100).roundToInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun MasteryLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(MasteryLevel.New, MasteryLevel.Familiar, MasteryLevel.Stable, MasteryLevel.Mastered)
            .forEach { level ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(masteryColor(level, MaterialTheme.colorScheme))
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(level.name, style = MaterialTheme.typography.labelSmall)
                }
            }
    }
}

@Composable
private fun ConnectedLearningLoading(padding: Dp) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ConnectedLearningError(
    message: String,
    padding: Dp,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun nodeColor(kind: GraphNodeKind, colors: androidx.compose.material3.ColorScheme): Color = when (kind) {
    GraphNodeKind.COMPONENT -> colors.secondaryContainer
    GraphNodeKind.KANJI -> colors.primaryContainer
    GraphNodeKind.READING -> colors.tertiaryContainer
    GraphNodeKind.VOCABULARY_ELEMENT -> colors.surfaceVariant
    GraphNodeKind.LEGACY_VOCABULARY_ENTRY -> colors.surfaceVariant
    GraphNodeKind.SENSE -> colors.errorContainer
    GraphNodeKind.SENTENCE -> colors.surfaceContainerHighest
    GraphNodeKind.GRAMMAR -> colors.inversePrimary
}

private fun masteryColor(
    mastery: MasteryLevel,
    colors: androidx.compose.material3.ColorScheme,
): Color = when (mastery) {
    MasteryLevel.New -> colors.outline
    MasteryLevel.Learning -> colors.tertiary
    MasteryLevel.Familiar -> colors.secondary
    MasteryLevel.Partial -> colors.error
    MasteryLevel.Stable -> colors.primary
    MasteryLevel.Mastered -> colors.primaryContainer
}

private fun edgeColor(
    kind: GraphEdgeKind,
    colors: androidx.compose.material3.ColorScheme,
): Color = when (kind) {
    GraphEdgeKind.COMPOSED_OF -> colors.secondary
    GraphEdgeKind.HAS_READING -> colors.tertiary
    GraphEdgeKind.HAS_VOCABULARY -> colors.primary
    GraphEdgeKind.HAS_SENSE -> colors.outline
    GraphEdgeKind.APPEARS_IN_SENTENCE -> colors.inversePrimary
    else -> colors.outlineVariant
}

@Immutable
data class GraphPoint(val x: Dp, val y: Dp)

@Immutable
data class MasteryNodeUi(
    val key: ConnectedNodeKey,
    val label: String,
    val title: String,
    val description: String,
    val kind: GraphNodeKind,
    val mastery: MasteryLevel,
    val position: GraphPoint,
    val isAnchor: Boolean = false,
)

@Immutable
data class GraphEdgeUi(
    val from: ConnectedNodeKey,
    val to: ConnectedNodeKey,
    val kind: GraphEdgeKind,
)

@Immutable
data class CandidateLessonUi(
    val id: String,
    val rootKey: ConnectedNodeKey,
    val title: String,
    val rationale: String,
    val score: Double,
    val estimatedMinutes: Int,
    val isDue: Boolean,
)

data class ConnectedLearningUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val pathSummary: String = "Build one connected path from Kanji to real usage.",
    val graphNodes: List<MasteryNodeUi> = emptyList(),
    val graphEdges: List<GraphEdgeUi> = emptyList(),
    val selectedNodeKey: ConnectedNodeKey? = null,
    val activeLessonRootKey: ConnectedNodeKey? = null,
    val candidates: List<CandidateLessonUi> = emptyList(),
)

sealed interface ConnectedLearningEvent {
    data object Refresh : ConnectedLearningEvent
    data object Retry : ConnectedLearningEvent
    data class SelectNode(val key: ConnectedNodeKey) : ConnectedLearningEvent
    data class StartLesson(val key: ConnectedNodeKey) : ConnectedLearningEvent
}
