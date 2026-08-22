package ua.syt0r.kanji.presentation.screen.main.features

import kotlinx.serialization.Serializable
import ua.syt0r.kanji.core.connected_learning.GraphNodeKind
import ua.syt0r.kanji.core.connected_learning.LearningGraphConnection
import ua.syt0r.kanji.core.connected_learning.LearningGraphNode

@Serializable
internal enum class MindMapExplorerMode {
    RADICALS,
    COMPONENTS,
}

internal data class MindMapCatalogItem(
    val key: String,
    val label: String,
    val mode: MindMapExplorerMode,
    val relatedKanjiCount: Int,
    val strokeCount: Int? = null,
    val nodeKind: GraphNodeKind? = null,
)

internal data class MindMapGraphSnapshot(
    val rootKey: String,
    val nodes: List<LearningGraphNode>,
    val connections: List<LearningGraphConnection>,
)

internal data class MindMapCatalogPage(
    val items: List<MindMapCatalogItem>,
    val totalCount: Int,
    val offset: Int,
    val limit: Int,
) {
    val hasMore: Boolean
        get() = offset + items.size < totalCount
}
