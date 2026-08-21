package ua.syt0r.kanji.presentation.screen.main.screen.connected_learning

import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.connected_learning.ConnectedItemKey
import ua.syt0r.kanji.core.connected_learning.ConnectedNodeKey
import ua.syt0r.kanji.core.connected_learning.GraphNodeKind
import ua.syt0r.kanji.core.connected_learning.LessonCandidateFeatures
import ua.syt0r.kanji.core.connected_learning.LessonCandidateScorer
import ua.syt0r.kanji.core.connected_learning.LessonSelectionPolicy
import ua.syt0r.kanji.core.connected_learning.MasteryLevel
import ua.syt0r.kanji.core.connected_learning.MasteryPolicy
import ua.syt0r.kanji.core.connected_learning.MasteryReducer
import ua.syt0r.kanji.core.connected_learning.ReviewDimension
import ua.syt0r.kanji.core.connected_learning.LearningGraphConnection
import ua.syt0r.kanji.core.connected_learning.LearningGraphNode
import ua.syt0r.kanji.core.connected_learning.LearningGraphRepository
import ua.syt0r.kanji.core.user_data.database.ConnectedReviewRepository

class DefaultConnectedLearningLoader(
    private val graphRepository: LearningGraphRepository,
    private val reviewRepository: ConnectedReviewRepository,
    private val clock: Clock = Clock.System,
) : ConnectedLearningLoader {

    override suspend fun load(rootNodeKey: ConnectedNodeKey): ConnectedLearningUiState {
        val nodes = graphRepository.getNeighborhood(
            rootNodeKey = rootNodeKey,
            maxDepth = 2,
            limit = MAX_GRAPH_NODES,
        )
        check(nodes.any { it.nodeKey == rootNodeKey }) {
            "Connected learning node was not found: ${rootNodeKey.value}"
        }
        val nodeIds = nodes.map { it.nodeId }.distinct()
        val connections = graphRepository.getEdgesFromNodes(
            nodeIds = nodeIds,
            limit = MAX_GRAPH_EDGES,
        )
        val nodeById = nodes.associateBy { it.nodeId }
        val visibleConnections = connections.filter { connection ->
            connection.fromNodeId in nodeById && connection.toNodeId in nodeById
        }
        val itemKeys = nodes.map { ConnectedItemKey.fromNode(it.nodeKey) }
        val cardsByItem = reviewRepository.getCards(itemKeys)
        val now = clock.now()
        val masteryByNode = nodes.associate { node ->
            val itemKey = ConnectedItemKey.fromNode(node.nodeKey)
            val cards = cardsByItem[itemKey].orEmpty()
                .mapValues { (_, connectedCard) -> connectedCard.card }
            node.nodeKey to MasteryReducer.reduce(cards, policyFor(node.kind))
        }
        val degreeByNodeId = visibleConnections
            .flatMap { listOf(it.fromNodeId, it.toNodeId) }
            .groupingBy { it }
            .eachCount()
        val candidateFeatures = nodes
            .filter { it.nodeKey != rootNodeKey }
            .map { node ->
                val mastery = masteryByNode.getValue(node.nodeKey)
                LessonCandidateScorer.score(
                    LessonCandidateFeatures(
                        nodeKey = node.nodeKey,
                        nodeKind = node.kind,
                        prerequisiteReadiness = prerequisiteReadiness(node),
                        learnerWeakness = LessonCandidateScorer.weaknessFromMastery(mastery.aggregate),
                        graphLeverage = graphLeverage(degreeByNodeId[node.nodeId] ?: 0),
                        levelRelevance = levelRelevance(node.level),
                        novelty = if (mastery.aggregate == MasteryLevel.New) 1.0 else 0.25,
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
            pathSummary = "${nodes.size} connected nodes · ${visibleConnections.size} verified links",
            graphNodes = nodes.mapIndexed { index, node ->
                node.toUi(
                    mastery = masteryByNode.getValue(node.nodeKey).aggregate,
                    index = index,
                    isAnchor = node.nodeKey == rootNodeKey,
                )
            },
            graphEdges = visibleConnections.mapNotNull { it.toUi(nodeById) },
            selectedNodeKey = rootNodeKey,
            candidates = recommendations.map { candidate ->
                val candidateNode = nodeByKey(nodes, candidate.features.nodeKey)
                CandidateLessonUi(
                    id = candidate.features.nodeKey.value,
                    rootKey = candidate.features.nodeKey,
                    title = candidateNode.displayLabel(),
                    rationale = rationaleFor(candidate.features.mastery, candidate.features.isDueReview),
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

    private fun prerequisiteReadiness(node: LearningGraphNode): Double = when {
        node.depth <= 0L -> 1.0
        node.depth == 1L -> 0.85
        else -> 0.65
    }

    private fun graphLeverage(degree: Int): Double = (degree / 8.0).coerceIn(0.0, 1.0)

    private fun levelRelevance(level: Long?): Double = when (level) {
        null -> 0.5
        in 1L..5L -> 1.0
        in 6L..10L -> 0.75
        else -> 0.4
    }

    private fun LearningGraphNode.toUi(
        mastery: MasteryLevel,
        index: Int,
        isAnchor: Boolean,
    ) = MasteryNodeUi(
        key = nodeKey,
        label = displayLabel(),
        title = displayTitle(),
        description = metadataDescription(),
        kind = kind,
        mastery = mastery,
        position = graphPosition(index, depth),
        isAnchor = isAnchor,
    )

    private fun LearningGraphConnection.toUi(
        nodeById: Map<Long, LearningGraphNode>,
    ): GraphEdgeUi? {
        val from = nodeById[fromNodeId] ?: return null
        val to = nodeById[toNodeId] ?: return null
        return GraphEdgeUi(from = from.nodeKey, to = to.nodeKey, kind = edgeKind)
    }

    private fun LearningGraphNode.displayLabel(): String = when (kind) {
        GraphNodeKind.KANJI -> kanji
        GraphNodeKind.READING -> reading
        GraphNodeKind.VOCABULARY_ELEMENT -> kanji ?: reading
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

    private fun LearningGraphNode.metadataDescription(): String = buildList {
        add("Graph key: ${nodeKey.value}")
        kanji?.let { add("Kanji: $it") }
        reading?.let { add("Reading: $it") }
        entryId?.let { add("Entry ID: $it") }
        elementId?.let { add("Element ID: $it") }
        senseId?.let { add("Sense ID: $it") }
        sentenceId?.let { add("Sentence ID: $it") }
        level?.let { add("Level: $it") }
    }.joinToString(separator = " · ")

    private fun graphPosition(index: Int, depth: Long): GraphPoint {
        val column = (index % 5)
        val row = (index / 5)
        val x = 64 + column * 84 + depth.coerceIn(0L, 2L) * 12
        val y = 54 + row * 78
        return GraphPoint(x = x.toInt().dp, y = y.dp)
    }

    private fun nodeByKey(nodes: List<LearningGraphNode>, key: ConnectedNodeKey): LearningGraphNode =
        nodes.first { it.nodeKey == key }

    private fun rationaleFor(mastery: MasteryLevel, due: Boolean): String = when {
        due -> "Due review from connected mastery state"
        mastery == MasteryLevel.New -> "New node linked to the current path"
        else -> "Strengthen the weakest connected dimension"
    }

    private fun estimatedMinutes(kind: GraphNodeKind): Int = when (kind) {
        GraphNodeKind.SENTENCE,
        GraphNodeKind.GRAMMAR,
        -> 4
        else -> 2
    }

    private companion object {
        const val MAX_GRAPH_NODES = 256
        const val MAX_GRAPH_EDGES = 2_048
        const val RECOMMENDATION_COUNT = 6
    }
}
