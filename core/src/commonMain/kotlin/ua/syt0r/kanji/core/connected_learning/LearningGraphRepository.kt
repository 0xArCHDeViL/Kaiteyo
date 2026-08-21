package ua.syt0r.kanji.core.connected_learning

import ua.syt0r.kanji.core.app_data.db.AppDataDatabase

interface LearningGraphRepository {
    suspend fun getNeighborhood(
        rootNodeKey: ConnectedNodeKey,
        maxDepth: Int = 2,
        edgeKinds: Set<GraphEdgeKind> = GraphEdgeKind.entries.toSet(),
        limit: Int = 256,
    ): List<LearningGraphNode>

    suspend fun getNeighbors(
        nodeId: Long,
        edgeKinds: Set<GraphEdgeKind>,
        limit: Int = 64,
    ): List<LearningGraphNeighbor>
}

data class LearningGraphNode(
    val nodeId: Long,
    val nodeKey: ConnectedNodeKey,
    val kind: GraphNodeKind,
    val kanji: String?,
    val reading: String?,
    val entryId: Long?,
    val elementId: Long?,
    val senseId: Long?,
    val sentenceId: Long?,
    val level: Long?,
    val priority: Double,
    val depth: Long,
)

data class LearningGraphNeighbor(
    val node: LearningGraphNode,
    val edgeKind: GraphEdgeKind,
    val weight: Double,
    val provenance: GraphProvenance,
)

class SqlDelightLearningGraphRepository(
    private val database: AppDataDatabase,
) : LearningGraphRepository {

    override suspend fun getNeighborhood(
        rootNodeKey: ConnectedNodeKey,
        maxDepth: Int,
        edgeKinds: Set<GraphEdgeKind>,
        limit: Int,
    ): List<LearningGraphNode> {
        require(maxDepth in 0..3) { "Learning graph depth must be between 0 and 3" }
        require(limit in 1..2_000) { "Learning graph limit must be between 1 and 2000" }

        return database.lettersQueries
            .getLearningNeighborhood(
                rootNodeKey = rootNodeKey.value,
                maxDepth = maxDepth.toLong(),
                edgeKinds = edgeKinds.map { it.name }.sorted(),
                limit = limit.toLong(),
            )
            .executeAsList()
            .map { row ->
                LearningGraphNode(
                    nodeId = row.node_id,
                    nodeKey = ConnectedNodeKey(row.node_key),
                    kind = parseNodeKind(row.node_kind),
                    kanji = row.kanji,
                    reading = row.reading,
                    entryId = row.entry_id,
                    elementId = row.element_id,
                    senseId = row.sense_id,
                    sentenceId = row.sentence_id,
                    level = row.level,
                    priority = row.priority,
                    depth = row.depth,
                )
            }
    }

    override suspend fun getNeighbors(
        nodeId: Long,
        edgeKinds: Set<GraphEdgeKind>,
        limit: Int,
    ): List<LearningGraphNeighbor> {
        require(nodeId > 0) { "Graph node ID must be positive" }
        require(limit in 1..2_000) { "Learning graph limit must be between 1 and 2000" }

        return database.lettersQueries
            .getLearningNeighbors(
                nodeId = nodeId,
                edgeKinds = edgeKinds.map { it.name }.sorted(),
                limit = limit.toLong(),
            )
            .executeAsList()
            .map { row ->
                LearningGraphNeighbor(
                    node = LearningGraphNode(
                        nodeId = row.node_id,
                        nodeKey = ConnectedNodeKey(row.node_key),
                        kind = parseNodeKind(row.node_kind),
                        kanji = row.kanji,
                        reading = row.reading,
                        entryId = row.entry_id,
                        elementId = row.element_id,
                        senseId = row.sense_id,
                        sentenceId = row.sentence_id,
                        level = row.level,
                        priority = row.priority,
                        depth = 1,
                    ),
                    edgeKind = parseEdgeKind(row.edge_kind),
                    weight = row.weight,
                    provenance = parseProvenance(row.provenance),
                )
            }
    }

    private fun parseNodeKind(value: String): GraphNodeKind =
        GraphNodeKind.entries.firstOrNull { it.name == value }
            ?: error("Unknown graph node kind: $value")

    private fun parseEdgeKind(value: String): GraphEdgeKind =
        GraphEdgeKind.entries.firstOrNull { it.name == value }
            ?: error("Unknown graph edge kind: $value")

    private fun parseProvenance(value: String): GraphProvenance =
        GraphProvenance.entries.firstOrNull { it.name == value }
            ?: error("Unknown graph provenance: $value")
}
