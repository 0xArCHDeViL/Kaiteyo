package ua.syt0r.kanji.presentation.screen.main.screen.connected_learning

import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.math.abs
import kotlin.math.roundToInt
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.ConnectedVocabElementData
import ua.syt0r.kanji.core.connected_learning.ConnectedItemKey
import ua.syt0r.kanji.core.connected_learning.ConnectedNodeKey
import ua.syt0r.kanji.core.connected_learning.GraphEdgeKind
import ua.syt0r.kanji.core.connected_learning.GraphNodeKind
import ua.syt0r.kanji.core.connected_learning.LessonCandidateFeatures
import ua.syt0r.kanji.core.connected_learning.LessonCandidateScorer
import ua.syt0r.kanji.core.connected_learning.LessonSelectionPolicy
import ua.syt0r.kanji.core.connected_learning.MasteryLevel
import ua.syt0r.kanji.core.connected_learning.MasteryPolicy
import ua.syt0r.kanji.core.connected_learning.MasteryReducer
import ua.syt0r.kanji.core.connected_learning.ReviewDimension
import ua.syt0r.kanji.presentation.screen.main.screen.connected_learning.SkillNodeProgress
import ua.syt0r.kanji.core.connected_learning.LearningGraphConnection
import ua.syt0r.kanji.core.connected_learning.LearningGraphNode
import ua.syt0r.kanji.core.connected_learning.LearningGraphRepository
import ua.syt0r.kanji.core.user_data.database.ConnectedReviewRepository

fun interface ConnectedVocabularyMetadataRepository {
    suspend fun get(entryIds: Set<Long>): List<ConnectedVocabElementData>
}

class AppDataConnectedVocabularyMetadataRepository(
    private val appDataRepository: AppDataRepository,
) : ConnectedVocabularyMetadataRepository {
    override suspend fun get(entryIds: Set<Long>): List<ConnectedVocabElementData> =
        appDataRepository.getConnectedVocabElementData(entryIds)
}

class DefaultConnectedLearningLoader(
    private val graphRepository: LearningGraphRepository,
    private val reviewRepository: ConnectedReviewRepository,
    private val vocabularyRepository: ConnectedVocabularyMetadataRepository,
    private val clock: Clock = Clock.System,
) : ConnectedLearningLoader {

    override suspend fun load(rootNodeKey: ConnectedNodeKey): ConnectedLearningUiState {
        val neighborhood = graphRepository.getNeighborhood(
            rootNodeKey = rootNodeKey,
            maxDepth = TREE_MAX_DEPTH,
            limit = MAX_GRAPH_NODES,
        )
        check(neighborhood.any { it.nodeKey == rootNodeKey }) {
            "Connected learning node was not found: ${rootNodeKey.value}"
        }
        val neighborhoodById = neighborhood.associateBy { it.nodeId }
        val connections = graphRepository.getEdgesFromNodes(
            nodeIds = neighborhoodById.keys,
            limit = MAX_GRAPH_EDGES,
        )
        val prerequisiteConnections = connections.filter {
            it.edgeKind == GraphEdgeKind.PREREQUISITE_OF &&
                it.fromNodeId in neighborhoodById && it.toNodeId in neighborhoodById
        }
        val treeNodeIds = projectTreeNodeIds(
            rootNodeKey = rootNodeKey,
            nodes = neighborhood,
            prerequisiteConnections = prerequisiteConnections,
        )
        val nodes = neighborhood.filter { it.nodeId in treeNodeIds }
        val nodeById = nodes.associateBy { it.nodeId }
        val visibleConnections = prerequisiteConnections.filter { connection ->
            connection.fromNodeId in nodeById && connection.toNodeId in nodeById
        }
        val treeLayout = buildTreeLayout(
            rootNodeKey = rootNodeKey,
            nodes = nodes,
            prerequisiteConnections = visibleConnections,
        )
        val itemKeys = nodes.map { ConnectedItemKey.fromNode(it.nodeKey) }
        val cardsByItem = reviewRepository.getCards(itemKeys)
        val vocabularyByIdentity = vocabularyRepository
            .get(nodes.mapNotNull { it.entryId }.toSet())
            .associateBy { it.entryId to it.elementId }
        val now = clock.now()
        val masteryByNode = nodes.associate { node ->
            val itemKey = ConnectedItemKey.fromNode(node.nodeKey)
            val cards = cardsByItem[itemKey].orEmpty()
                .mapValues { (_, connectedCard) -> connectedCard.card }
            node.nodeKey to MasteryReducer.reduce(cards, policyFor(node.kind))
        }
        val connectionCountByNodeId = visibleConnections
            .flatMap { connection -> listOf(connection.fromNodeId, connection.toNodeId) }
            .groupingBy { it }
            .eachCount()
        val prerequisitesByNodeId = visibleConnections.groupBy { it.toNodeId }
        val dependentsByNodeId = visibleConnections.groupBy { it.fromNodeId }
        val masteryByNodeId = nodes.associate { node ->
            node.nodeId to masteryByNode.getValue(node.nodeKey).aggregate
        }
        val maxPriority = nodes.maxOfOrNull { it.priority } ?: 0.0
        val candidateFeatures = nodes
            .filter { it.nodeKey != rootNodeKey }
            .map { node ->
                val mastery = masteryByNode.getValue(node.nodeKey)
                LessonCandidateScorer.score(
                    LessonCandidateFeatures(
                        nodeKey = node.nodeKey,
                        nodeKind = node.kind,
                        prerequisiteReadiness = prerequisiteReadiness(
                            nodeId = node.nodeId,
                            prerequisitesByNodeId = prerequisitesByNodeId,
                            masteryByNodeId = masteryByNodeId,
                        ),
                        learnerWeakness = LessonCandidateScorer.weaknessFromMastery(mastery.aggregate),
                        graphLeverage = graphLeverage(
                            nodeId = node.nodeId,
                            prerequisitesByNodeId = prerequisitesByNodeId,
                            dependentsByNodeId = dependentsByNodeId,
                            connectedDegree = connectionCountByNodeId[node.nodeId] ?: 0,
                        ),
                        levelRelevance = levelRelevance(
                            node = node,
                            root = nodes.first { it.nodeKey == rootNodeKey },
                            maxPriority = maxPriority,
                        ),
                        novelty = novelty(
                            node = node,
                            mastery = mastery.aggregate,
                        ),
                        mastery = mastery.aggregate,
                        isDueReview = isDue(mastery, now),
                    )
                )
            }
        val recommendations = LessonCandidateScorer.select(
            candidates = candidateFeatures.map { it.features },
            policy = LessonSelectionPolicy(
                size = RECOMMENDATION_COUNT,
                maxPerKind = 2,
                includeDueReview = true,
            ),
        )

        return ConnectedLearningUiState(
            pathSummary = "Kanji skill tree · ${nodes.size} nodes · ${visibleConnections.size} prerequisite links",
            graphNodes = nodes.map { node ->
                val mastery = masteryByNode.getValue(node.nodeKey).aggregate
                val progress = progressFor(
                    mastery = mastery,
                    prerequisiteIds = visibleConnections
                        .filter { it.toNodeId == node.nodeId }
                        .map { it.fromNodeId },
                    masteryByNodeId = masteryByNodeId,
                )
                node.toUi(
                    mastery = mastery,
                    tier = treeLayout.tierByNodeId[node.nodeId] ?: 0,
                    lane = treeLayout.laneByNodeId[node.nodeId] ?: 0,
                    progress = progress,
                    isFrontier = progress == SkillNodeProgress.Available,
                    isAnchor = node.nodeKey == rootNodeKey,
                    vocabulary = vocabularyByIdentity,
                )
            },
            graphEdges = visibleConnections.mapNotNull { it.toUi(nodeById) },
            selectedNodeKey = rootNodeKey,
            candidates = recommendations.map { candidate ->
                val candidateNode = nodeByKey(nodes, candidate.features.nodeKey)
                CandidateLessonUi(
                    id = candidate.features.nodeKey.value,
                    rootKey = candidate.features.nodeKey,
                    title = candidateNode.displayLabel(vocabularyByIdentity),
                    rationale = rationaleFor(candidate.features),
                    score = candidate.score,
                    estimatedMinutes = estimatedMinutes(candidateNode.kind),
                    isDue = candidate.features.isDueReview,
                )
            },
        )
    }

    private fun policyFor(kind: GraphNodeKind): MasteryPolicy = when (kind) {
        GraphNodeKind.KANJI,
        GraphNodeKind.READING,
        -> MasteryPolicy.kanjiCore()

        GraphNodeKind.VOCABULARY_ELEMENT,
        GraphNodeKind.LEGACY_VOCABULARY_ENTRY,
        GraphNodeKind.SENSE,
        -> MasteryPolicy.vocabularyCore()

        GraphNodeKind.COMPONENT -> singleDimensionPolicy(ReviewDimension.COMPONENT_RECOGNITION)
        GraphNodeKind.SENTENCE -> singleDimensionPolicy(ReviewDimension.SENTENCE_COMPREHENSION)
        GraphNodeKind.GRAMMAR -> singleDimensionPolicy(ReviewDimension.GRAMMAR_APPLICATION)
    }

    private fun singleDimensionPolicy(dimension: ReviewDimension) = MasteryPolicy(
        requiredDimensions = setOf(dimension),
        familiarStability = 1.0,
        stableStability = 7.0,
        masteredStability = 30.0,
    )

    private fun isDue(
        mastery: ua.syt0r.kanji.core.connected_learning.ConnectedMasterySnapshot,
        now: Instant,
    ): Boolean = mastery.dimensions.values.any { dimension ->
        dimension.dueAt?.let { it <= now } == true
    }

    private fun prerequisiteReadiness(
        nodeId: Long,
        prerequisitesByNodeId: Map<Long, List<LearningGraphConnection>>,
        masteryByNodeId: Map<Long, MasteryLevel>,
    ): Double {
        val prerequisites = prerequisitesByNodeId[nodeId].orEmpty()
        if (prerequisites.isEmpty()) return 1.0

        return prerequisites.map { connection ->
            masteryReadiness(masteryByNodeId[connection.fromNodeId] ?: MasteryLevel.New)
        }.average().coerceIn(0.0, 1.0)
    }

    private fun graphLeverage(
        nodeId: Long,
        prerequisitesByNodeId: Map<Long, List<LearningGraphConnection>>,
        dependentsByNodeId: Map<Long, List<LearningGraphConnection>>,
        connectedDegree: Int,
    ): Double {
        val prerequisiteCount = prerequisitesByNodeId[nodeId].orEmpty().size
        val dependentCount = dependentsByNodeId[nodeId].orEmpty().size
        val structuralLeverage = dependentCount * 1.5 + prerequisiteCount
        val connectedCoverage = (connectedDegree / 12.0).coerceIn(0.0, 1.0)
        return ((structuralLeverage / 8.0) * 0.7 + connectedCoverage * 0.3)
            .coerceIn(0.0, 1.0)
    }

    private fun novelty(node: LearningGraphNode, mastery: MasteryLevel): Double {
        val masteryNovelty = when (mastery) {
            MasteryLevel.New -> 1.0
            MasteryLevel.Learning -> 0.78
            MasteryLevel.Partial -> 0.65
            MasteryLevel.Familiar -> 0.45
            MasteryLevel.Stable -> 0.25
            MasteryLevel.Mastered -> 0.0
        }
        val depthAdjustment = (1.0 - node.depth.coerceIn(0L, 3L) * 0.08)
        return (masteryNovelty * depthAdjustment).coerceIn(0.0, 1.0)
    }

    private fun levelRelevance(
        node: LearningGraphNode,
        root: LearningGraphNode,
        maxPriority: Double,
    ): Double {
        val levelScore = when {
            node.level == null -> (1.0 - node.depth.coerceIn(0L, 3L) * 0.2)
                .coerceIn(0.35, 1.0)
            root.level == null -> (1.0 - ((node.level - 1L).coerceAtLeast(0L) / 15.0))
                .coerceIn(0.25, 1.0)
            else -> (1.0 - abs(node.level - root.level).toDouble() / 10.0)
                .coerceIn(0.25, 1.0)
        }
        val priorityScore = if (maxPriority > 0.0) {
            (node.priority / maxPriority).coerceIn(0.0, 1.0)
        } else {
            0.35
        }
        return (levelScore * 0.7 + priorityScore * 0.3).coerceIn(0.0, 1.0)
    }

    private fun masteryReadiness(mastery: MasteryLevel): Double = when (mastery) {
        MasteryLevel.New -> 0.0
        MasteryLevel.Learning -> 0.25
        MasteryLevel.Partial -> 0.4
        MasteryLevel.Familiar -> 0.6
        MasteryLevel.Stable -> 0.85
        MasteryLevel.Mastered -> 1.0
    }

    private fun LearningGraphNode.toUi(
        mastery: MasteryLevel,
        tier: Int,
        lane: Int,
        progress: SkillNodeProgress,
        isFrontier: Boolean,
        isAnchor: Boolean,
        vocabulary: Map<Pair<Long, Long>, ConnectedVocabElementData>,
    ) = MasteryNodeUi(
        key = nodeKey,
        label = displayLabel(vocabulary),
        title = displayTitle(),
        description = metadataDescription(vocabulary),
        kind = kind,
        mastery = mastery,
        progress = progress,
        tier = tier,
        lane = lane,
        position = graphPosition(tier, lane),
        isFrontier = isFrontier,
        isAnchor = isAnchor,
    )

    private fun LearningGraphConnection.toUi(
        nodeById: Map<Long, LearningGraphNode>,
    ): GraphEdgeUi? {
        val from = nodeById[fromNodeId] ?: return null
        val to = nodeById[toNodeId] ?: return null
        return GraphEdgeUi(from = from.nodeKey, to = to.nodeKey, kind = edgeKind)
    }

    private fun LearningGraphNode.displayLabel(
        vocabulary: Map<Pair<Long, Long>, ConnectedVocabElementData>,
    ): String = when (kind) {
        GraphNodeKind.KANJI -> kanji
        GraphNodeKind.READING -> reading
        GraphNodeKind.VOCABULARY_ELEMENT -> vocabulary[entryId to elementId]?.reading ?: reading
        else -> null
    }?.takeIf { it.isNotEmpty() } ?: nodeKey.value

    private fun LearningGraphNode.displayTitle(): String = when (kind) {
        GraphNodeKind.KANJI -> "Kanji"
        GraphNodeKind.READING -> "Reading"
        GraphNodeKind.VOCABULARY_ELEMENT -> "Vocabulary element"
        GraphNodeKind.LEGACY_VOCABULARY_ENTRY -> "Vocabulary entry"
        GraphNodeKind.COMPONENT -> "Component"
        GraphNodeKind.SENSE -> "Sense"
        GraphNodeKind.SENTENCE -> "Sentence"
        GraphNodeKind.GRAMMAR -> "Grammar"
    }

    private fun LearningGraphNode.metadataDescription(
        vocabulary: Map<Pair<Long, Long>, ConnectedVocabElementData>,
    ): String = buildList {
        add("Graph key: ${nodeKey.value}")
        kanji?.let { add("Kanji: $it") }
        reading?.let { add("Reading: $it") }
        vocabulary[entryId to elementId]?.let { data ->
            data.glossary.takeIf { it.isNotEmpty() }?.let { add("Meaning: ${it.joinToString()}") }
            data.partOfSpeech.takeIf { it.isNotEmpty() }?.let { add("POS: ${it.joinToString()}") }
        }
        entryId?.let { add("Entry ID: $it") }
        elementId?.let { add("Element ID: $it") }
        senseId?.let { add("Sense ID: $it") }
        sentenceId?.let { add("Sentence ID: $it") }
        level?.let { add("Level: $it") }
    }.joinToString(separator = " · ")

    private fun graphPosition(tier: Int, lane: Int): GraphPoint {
        val x = 128 + lane * TREE_LANE_SPACING
        val y = 64 + tier * TREE_TIER_SPACING
        return GraphPoint(x = x.dp, y = y.dp)
    }

    private fun projectTreeNodeIds(
        rootNodeKey: ConnectedNodeKey,
        nodes: List<LearningGraphNode>,
        prerequisiteConnections: List<LearningGraphConnection>,
    ): Set<Long> {
        val root = nodes.first { it.nodeKey == rootNodeKey }
        val nodesById = nodes.associateBy { it.nodeId }
        val childrenById = prerequisiteConnections.groupBy { it.fromNodeId }
        val selected = linkedSetOf(root.nodeId)
        val queue = ArrayDeque<Pair<Long, Int>>()
        queue.addLast(root.nodeId to 0)

        while (queue.isNotEmpty() && selected.size < MAX_TREE_NODES) {
            val (parentId, depth) = queue.removeFirst()
            if (depth >= TREE_MAX_DEPTH) continue
            childrenById[parentId]
                .orEmpty()
                .asSequence()
                .sortedWith(
                    compareByDescending<LearningGraphConnection> { nodesById[it.toNodeId]?.priority ?: 0.0 }
                        .thenBy { nodesById[it.toNodeId]?.nodeKey?.value ?: it.toNodeId.toString() }
                )
                .forEach { connection ->
                    if (selected.size >= MAX_TREE_NODES) return@forEach
                    if (selected.add(connection.toNodeId)) {
                        queue.addLast(connection.toNodeId to depth + 1)
                    }
                }
        }
        return selected
    }

    private data class TreeLayout(
        val tierByNodeId: Map<Long, Int>,
        val laneByNodeId: Map<Long, Int>,
    )

    private fun buildTreeLayout(
        rootNodeKey: ConnectedNodeKey,
        nodes: List<LearningGraphNode>,
        prerequisiteConnections: List<LearningGraphConnection>,
    ): TreeLayout {
        val root = nodes.first { it.nodeKey == rootNodeKey }
        val nodesById = nodes.associateBy { it.nodeId }
        val childrenById = prerequisiteConnections.groupBy { it.fromNodeId }
        val tierByNodeId = linkedMapOf(root.nodeId to 0)
        val queue = ArrayDeque<Long>()
        queue.addLast(root.nodeId)

        while (queue.isNotEmpty()) {
            val parentId = queue.removeFirst()
            val parentTier = tierByNodeId.getValue(parentId)
            childrenById[parentId]
                .orEmpty()
                .sortedWith(
                    compareByDescending<LearningGraphConnection> { nodesById[it.toNodeId]?.priority ?: 0.0 }
                        .thenBy { nodesById[it.toNodeId]?.nodeKey?.value ?: it.toNodeId.toString() }
                )
                .forEach { connection ->
                    if (connection.toNodeId !in tierByNodeId) {
                        tierByNodeId[connection.toNodeId] = parentTier + 1
                        queue.addLast(connection.toNodeId)
                    }
                }
        }

        val laneByNodeId = tierByNodeId.entries
            .groupBy({ it.value }, { it.key })
            .flatMap { (_, ids) ->
                ids.sortedWith(
                    compareByDescending<Long> { nodesById[it]?.priority ?: 0.0 }
                        .thenBy { nodesById[it]?.nodeKey?.value ?: it.toString() }
                ).mapIndexed { index, nodeId -> nodeId to index }
            }
            .toMap()
        return TreeLayout(tierByNodeId, laneByNodeId)
    }

    private fun progressFor(
        mastery: MasteryLevel,
        prerequisiteIds: List<Long>,
        masteryByNodeId: Map<Long, MasteryLevel>,
    ): SkillNodeProgress = when {
        mastery == MasteryLevel.Mastered -> SkillNodeProgress.Mastered
        prerequisiteIds.any {
            masteryByNodeId[it] !in setOf(MasteryLevel.Stable, MasteryLevel.Mastered)
        } -> SkillNodeProgress.Locked
        mastery == MasteryLevel.New -> SkillNodeProgress.Available
        else -> SkillNodeProgress.Learning
    }

    private fun nodeByKey(nodes: List<LearningGraphNode>, key: ConnectedNodeKey): LearningGraphNode =
        nodes.first { it.nodeKey == key }

    private fun rationaleFor(features: LessonCandidateFeatures): String {
        val readiness = (features.prerequisiteReadiness * 100).roundToInt()
        return when {
            features.isDueReview -> "Due review · prerequisite readiness ${readiness}%"
            features.mastery == MasteryLevel.New -> "New path node · prerequisite readiness ${readiness}%"
            else -> "Strengthen ${features.mastery.name.lowercase()} mastery · prerequisite readiness ${readiness}%"
        }
    }

    private fun estimatedMinutes(kind: GraphNodeKind): Int = when (kind) {
        GraphNodeKind.SENTENCE,
        GraphNodeKind.GRAMMAR,
        -> 4
        else -> 2
    }

    private companion object {
        const val MAX_GRAPH_NODES = 96
        const val MAX_GRAPH_EDGES = 256
        const val MAX_TREE_NODES = 48
        const val TREE_MAX_DEPTH = 3
        const val TREE_TIER_SPACING = 96
        const val TREE_LANE_SPACING = 104
        const val RECOMMENDATION_COUNT = 6
    }
}
