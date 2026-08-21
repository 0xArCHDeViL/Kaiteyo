import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking
import ua.syt0r.kanji.core.app_data.ConnectedVocabElementData
import ua.syt0r.kanji.core.connected_learning.ConnectedItemKey
import ua.syt0r.kanji.core.connected_learning.ConnectedNodeKey
import ua.syt0r.kanji.core.connected_learning.GraphEdgeKind
import ua.syt0r.kanji.core.connected_learning.GraphNodeKind
import ua.syt0r.kanji.core.connected_learning.GraphProvenance
import ua.syt0r.kanji.core.connected_learning.LearningGraphConnection
import ua.syt0r.kanji.core.connected_learning.LearningGraphNeighbor
import ua.syt0r.kanji.core.connected_learning.LearningGraphNode
import ua.syt0r.kanji.core.connected_learning.LearningGraphRepository
import ua.syt0r.kanji.core.user_data.database.ConnectedReviewCard
import ua.syt0r.kanji.core.user_data.database.ConnectedReviewEvent
import ua.syt0r.kanji.core.user_data.database.ConnectedReviewItem
import ua.syt0r.kanji.core.user_data.database.ConnectedReviewRepository
import ua.syt0r.kanji.presentation.screen.main.screen.connected_learning.ConnectedVocabularyMetadataRepository
import ua.syt0r.kanji.presentation.screen.main.screen.connected_learning.DefaultConnectedLearningLoader

class DefaultConnectedLearningLoaderTest {

    @Test
    fun loadUsesBoundedGraphDataAndProducesRecommendations() = runBlocking {
        val root = node(
            id = 1,
            key = "kanji:休",
            kind = GraphNodeKind.KANJI,
            depth = 0,
            kanji = "休",
        )
        val vocabulary = node(
            id = 2,
            key = "vocab-element:100|1|やすみ",
            kind = GraphNodeKind.VOCABULARY_ELEMENT,
            depth = 1,
            reading = "やすみ",
            entryId = 100,
            elementId = 1,
        )
        val state = DefaultConnectedLearningLoader(
            graphRepository = FakeGraphRepository(
                nodes = listOf(root, vocabulary),
                connections = listOf(
                    LearningGraphConnection(
                        fromNodeId = root.nodeId,
                        toNodeId = vocabulary.nodeId,
                        node = vocabulary,
                        edgeKind = GraphEdgeKind.HAS_VOCABULARY,
                        weight = 1.0,
                        provenance = GraphProvenance.DERIVED_AT_EXPORT,
                    )
                ),
            ),
            reviewRepository = EmptyConnectedReviewRepository(),
            vocabularyRepository = ConnectedVocabularyMetadataRepository {
                listOf(
                    ConnectedVocabElementData(
                        entryId = 100,
                        elementId = 1,
                        elementKind = "KANJI",
                        reading = "休み",
                        glossary = listOf("rest"),
                        partOfSpeech = listOf("noun"),
                    )
                )
            },
        ).load(root.nodeKey)

        assertEquals(2, state.graphNodes.size)
        assertEquals(1, state.graphEdges.size)
        assertEquals("2 connected nodes · 1 verified links", state.pathSummary)
        assertEquals("休み", state.candidates.single().title)
        assertEquals("Graph key: vocab-element:100|1|やすみ · Reading: やすみ · Meaning: rest · POS: noun · Entry ID: 100 · Element ID: 1", state.graphNodes[1].description)
        assertTrue(state.candidates.single().score in 0.0..1.0)
    }

    private class FakeGraphRepository(
        private val nodes: List<LearningGraphNode>,
        private val connections: List<LearningGraphConnection>,
    ) : LearningGraphRepository {
        override suspend fun getNeighborhood(
            rootNodeKey: ConnectedNodeKey,
            maxDepth: Int,
            edgeKinds: Set<GraphEdgeKind>,
            limit: Int,
        ): List<LearningGraphNode> = nodes

        override suspend fun getNeighbors(
            nodeId: Long,
            edgeKinds: Set<GraphEdgeKind>,
            limit: Int,
        ): List<LearningGraphNeighbor> = emptyList()

        override suspend fun getEdgesFromNodes(
            nodeIds: Collection<Long>,
            edgeKinds: Set<GraphEdgeKind>,
            limit: Int,
        ): List<LearningGraphConnection> = connections
    }

    private class EmptyConnectedReviewRepository : ConnectedReviewRepository {
        private val changes = MutableSharedFlow<Unit>()
        override val changesFlow = changes.asSharedFlow()

        override suspend fun getCards(
            itemKeys: Collection<ConnectedItemKey>,
        ): Map<ConnectedItemKey, Map<ua.syt0r.kanji.core.connected_learning.ReviewDimension, ConnectedReviewCard>> = emptyMap()

        override suspend fun getDueCards(
            now: kotlinx.datetime.Instant,
            dimensions: Collection<ua.syt0r.kanji.core.connected_learning.ReviewDimension>,
            limit: Long,
        ): List<ConnectedReviewCard> = emptyList()

        override suspend fun upsertItem(item: ConnectedReviewItem) = Unit
        override suspend fun upsertCard(card: ConnectedReviewCard) = Unit
        override suspend fun recordEvent(event: ConnectedReviewEvent) = Unit
    }

    private fun node(
        id: Long,
        key: String,
        kind: GraphNodeKind,
        depth: Long,
        kanji: String? = null,
        reading: String? = null,
        entryId: Long? = null,
        elementId: Long? = null,
    ) = LearningGraphNode(
        nodeId = id,
        nodeKey = ConnectedNodeKey(key),
        kind = kind,
        kanji = kanji,
        reading = reading,
        entryId = entryId,
        elementId = elementId,
        senseId = null,
        sentenceId = null,
        level = null,
        priority = 1.0,
        depth = depth,
    )
}
