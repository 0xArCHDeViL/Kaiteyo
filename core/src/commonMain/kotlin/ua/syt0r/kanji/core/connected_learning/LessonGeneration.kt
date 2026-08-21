package ua.syt0r.kanji.core.connected_learning

import kotlin.math.max
import kotlin.math.min

/** Inputs are already normalized to [0, 1] by the repository/domain boundary. */
data class LessonCandidateFeatures(
    val nodeKey: ConnectedNodeKey,
    val nodeKind: GraphNodeKind,
    val prerequisiteReadiness: Double,
    val learnerWeakness: Double,
    val graphLeverage: Double,
    val levelRelevance: Double,
    val novelty: Double,
    val mastery: MasteryLevel,
    val isDueReview: Boolean = false,
) {
    init {
        listOf(
            prerequisiteReadiness,
            learnerWeakness,
            graphLeverage,
            levelRelevance,
            novelty,
        ).forEach { require(it in 0.0..1.0) { "Candidate features must be normalized" } }
    }
}

data class CandidateScoreWeights(
    val prerequisiteReadiness: Double = 0.30,
    val learnerWeakness: Double = 0.25,
    val graphLeverage: Double = 0.20,
    val levelRelevance: Double = 0.15,
    val novelty: Double = 0.10,
) {
    init {
        val values = listOf(
            prerequisiteReadiness,
            learnerWeakness,
            graphLeverage,
            levelRelevance,
            novelty,
        )
        require(values.all { it >= 0.0 }) { "Candidate weights must not be negative" }
        require(values.sum() > 0.0) { "At least one candidate weight is required" }
    }

    fun normalized(): CandidateScoreWeights {
        val total = prerequisiteReadiness + learnerWeakness + graphLeverage +
            levelRelevance + novelty
        return CandidateScoreWeights(
            prerequisiteReadiness = prerequisiteReadiness / total,
            learnerWeakness = learnerWeakness / total,
            graphLeverage = graphLeverage / total,
            levelRelevance = levelRelevance / total,
            novelty = novelty / total,
        )
    }
}

data class ScoredLessonCandidate(
    val features: LessonCandidateFeatures,
    val score: Double,
)

data class LessonSelectionPolicy(
    val size: Int,
    val weights: CandidateScoreWeights = CandidateScoreWeights(),
    val maxPerKind: Int = Int.MAX_VALUE,
    val includeDueReview: Boolean = false,
) {
    init {
        require(size > 0) { "Lesson size must be positive" }
        require(maxPerKind > 0) { "maxPerKind must be positive" }
    }
}

object LessonCandidateScorer {

    fun score(
        features: LessonCandidateFeatures,
        weights: CandidateScoreWeights = CandidateScoreWeights(),
    ): ScoredLessonCandidate {
        val normalized = weights.normalized()
        val score = (
            features.prerequisiteReadiness * normalized.prerequisiteReadiness +
                features.learnerWeakness * normalized.learnerWeakness +
                features.graphLeverage * normalized.graphLeverage +
                features.levelRelevance * normalized.levelRelevance +
                features.novelty * normalized.novelty
            ).coerceIn(0.0, 1.0)
        return ScoredLessonCandidate(features = features, score = score)
    }

    fun rank(
        candidates: Iterable<LessonCandidateFeatures>,
        weights: CandidateScoreWeights = CandidateScoreWeights(),
    ): List<ScoredLessonCandidate> = candidates
        .asSequence()
        .map { score(it, weights) }
        .sortedWith(
            compareByDescending<ScoredLessonCandidate> { it.score }
                .thenBy { it.features.isDueReview.not() }
                .thenBy { it.features.nodeKind.ordinal }
                .thenBy { it.features.nodeKey.value }
        )
        .toList()

    fun select(
        candidates: Iterable<LessonCandidateFeatures>,
        policy: LessonSelectionPolicy,
    ): List<ScoredLessonCandidate> {
        val selected = ArrayList<ScoredLessonCandidate>(policy.size)
        val kindCounts = HashMap<GraphNodeKind, Int>()

        rank(candidates, policy.weights).forEach { candidate ->
            if (selected.size >= policy.size) return@forEach
            if (!policy.includeDueReview && candidate.features.isDueReview) return@forEach

            val kind = candidate.features.nodeKind
            val currentCount = kindCounts[kind] ?: 0
            if (currentCount >= policy.maxPerKind) return@forEach

            selected += candidate
            kindCounts[kind] = currentCount + 1
        }

        return selected
    }

    fun weaknessFromMastery(mastery: MasteryLevel): Double = when (mastery) {
        MasteryLevel.New -> 1.0
        MasteryLevel.Learning -> 0.9
        MasteryLevel.Partial -> 0.8
        MasteryLevel.Familiar -> 0.6
        MasteryLevel.Stable -> 0.2
        MasteryLevel.Mastered -> 0.0
    }
}

fun Iterable<ScoredLessonCandidate>.canonicalResultKey(): String =
    joinToString(separator = "|") { candidate ->
        "${candidate.features.nodeKey.value}:${candidate.score.toString()}"
    }
