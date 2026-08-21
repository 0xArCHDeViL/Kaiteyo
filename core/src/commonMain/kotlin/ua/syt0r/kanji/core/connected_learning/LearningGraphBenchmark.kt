package ua.syt0r.kanji.core.connected_learning

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.time.Duration
import kotlin.time.TimeSource
import kotlin.time.Duration.Companion.nanoseconds

/**
 * Device-runnable benchmark harness. It intentionally measures the repository and
 * SQLDelight driver together, not only an in-memory graph algorithm.
 */
data class LearningGraphBenchmarkConfig(
    val roots: List<ConnectedNodeKey>,
    val edgeKinds: Set<GraphEdgeKind>,
    val maxDepth: Int = 2,
    val limit: Int = 256,
    val warmupIterations: Int = 3,
    val measuredIterations: Int = 20,
    val concurrency: Int = 1,
) {
    init {
        require(roots.isNotEmpty()) { "At least one benchmark root is required" }
        require(maxDepth in 0..3) { "Benchmark depth must be between 0 and 3" }
        require(limit in 1..2_000) { "Benchmark limit must be between 1 and 2000" }
        require(warmupIterations >= 0) { "Warmup iterations must not be negative" }
        require(measuredIterations > 0) { "Measured iterations must be positive" }
        require(concurrency > 0) { "Concurrency must be positive" }
    }
}

data class LearningGraphBenchmarkResult(
    val sampleCount: Int,
    val p50: Duration,
    val p95: Duration,
    val p99: Duration,
    val max: Duration,
    val average: Duration,
    val averageResultCount: Double,
    val deterministicResultHash: Long,
) {
    val isEmpty: Boolean
        get() = sampleCount == 0
}

object LearningGraphBenchmark {

    suspend fun run(
        repository: LearningGraphRepository,
        config: LearningGraphBenchmarkConfig,
    ): LearningGraphBenchmarkResult = coroutineScope {
        repeat(config.warmupIterations) {
            config.roots.forEach { root ->
                repository.getNeighborhood(
                    rootNodeKey = root,
                    maxDepth = config.maxDepth,
                    edgeKinds = config.edgeKinds,
                    limit = config.limit,
                )
            }
        }

        val samples = (0 until config.concurrency).map { worker ->
            async {
                val workerSamples = ArrayList<Sample>(config.measuredIterations)
                repeat(config.measuredIterations) { iteration ->
                    val root = config.roots[(worker + iteration) % config.roots.size]
                    val start = TimeSource.Monotonic.markNow()
                    val rows = repository.getNeighborhood(
                        rootNodeKey = root,
                        maxDepth = config.maxDepth,
                        edgeKinds = config.edgeKinds,
                        limit = config.limit,
                    )
                    workerSamples += Sample(
                        elapsed = start.elapsedNow(),
                        resultCount = rows.size,
                        resultHash = rows.fold(17L) { hash, row ->
                            hash * 31L + row.nodeKey.value.hashCode().toLong()
                        },
                    )
                }
                workerSamples
            }
        }.awaitAll().flatten()

        val durations = samples.map { it.elapsed }.sorted()
        val totalNanos = durations.sumOf { it.inWholeNanoseconds }
        val average = (totalNanos / durations.size).nanoseconds

        LearningGraphBenchmarkResult(
            sampleCount = samples.size,
            p50 = percentile(durations, 0.50),
            p95 = percentile(durations, 0.95),
            p99 = percentile(durations, 0.99),
            max = durations.last(),
            average = average,
            averageResultCount = samples.map { it.resultCount }.average(),
            deterministicResultHash = samples.fold(17L) { hash, sample ->
                hash * 31L + sample.resultHash
            },
        )
    }

    private fun percentile(
        durations: List<Duration>,
        percentile: Double,
    ): Duration {
        val index = ((durations.size - 1) * percentile)
            .toInt()
            .coerceIn(0, durations.lastIndex)
        return durations[index]
    }

    private data class Sample(
        val elapsed: Duration,
        val resultCount: Int,
        val resultHash: Long,
    )
}
