import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds
import ua.syt0r.kanji.core.connected_learning.ConnectedNodeKey
import ua.syt0r.kanji.core.connected_learning.GraphEdgeKind
import ua.syt0r.kanji.core.connected_learning.GraphNodeKind
import ua.syt0r.kanji.core.connected_learning.LearningGraphBenchmark
import ua.syt0r.kanji.core.connected_learning.LearningGraphBenchmarkConfig
import ua.syt0r.kanji.core.connected_learning.LearningGraphNode
import ua.syt0r.kanji.core.connected_learning.LearningGraphRepository

class LearningGraphBenchmarkTest {

    @Test
    fun benchmarkProducesStableNonEmptyMetrics() = runBlocking {
        val repository = FakeGraphRepository()
        val result = LearningGraphBenchmark.run(
            repository = repository,
            config = LearningGraphBenchmarkConfig(
                roots = listOf(ConnectedNodeKey("kanji:休")),
                edgeKinds = setOf(GraphEdgeKind.HAS_VOCABULARY),
                warmupIterations = 2,
                measuredIterations = 5,
                concurrency = 2,
            ),
        )

        assertEquals(10, result.sampleCount)
        assertTrue(result.p50 <= result.p95)
        assertTrue(result.p95 <= result.p99)
        assertTrue(result.p99 <= result.max)
        assertEquals(2.0, result.averageResultCount)
        assertTrue(result.deterministicResultHash != 0L)
    }

    private class FakeGraphRepository : LearningGraphRepository {
        override suspend fun getNeighborhood(
            rootNodeKey: ConnectedNodeKey,
            maxDepth: Int,
            edgeKinds: Set<GraphEdgeKind>,
            limit: Int,
        ): List<LearningGraphNode> = listOf(
            node("vocab-element:1|1"),
            node("vocab-element:1|2"),
        )

        override suspend fun getNeighbors(
            nodeId: Long,
            edgeKinds: Set<GraphEdgeKind>,
            limit: Int,
        ) = emptyList<ua.syt0r.kanji.core.connected_learning.LearningGraphNeighbor>()

        private fun node(key: String) = LearningGraphNode(
            nodeId = key.hashCode().toLong(),
            nodeKey = ConnectedNodeKey(key),
            kind = GraphNodeKind.VOCABULARY_ELEMENT,
            kanji = null,
            reading = null,
            entryId = 1,
            elementId = 1,
            senseId = null,
            sentenceId = null,
            level = null,
            priority = 1.0,
            depth = 1,
        )
    }
}
