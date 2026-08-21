package ua.syt0r.kanji.core.connected_learning

import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardParams
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus

/** Derived learner state. This is intentionally not an FSRS scheduler state. */
enum class MasteryLevel {
    New,
    Learning,
    Familiar,
    Partial,
    Stable,
    Mastered,
}

data class DimensionMastery(
    val dimension: ReviewDimension,
    val level: MasteryLevel,
    val status: FsrsCardStatus,
    val stability: Double,
    val lapses: Int,
    val lastReview: Instant?,
    val dueAt: Instant?,
)

data class MasteryPolicy(
    val requiredDimensions: Set<ReviewDimension>,
    val familiarStability: Double,
    val stableStability: Double,
    val masteredStability: Double,
    val maxMasteredLapses: Int = 0,
) {
    init {
        require(requiredDimensions.isNotEmpty()) { "At least one required dimension is needed" }
        require(familiarStability >= 0.0) { "Familiar stability must not be negative" }
        require(stableStability >= familiarStability) {
            "Stable stability must be >= familiar stability"
        }
        require(masteredStability >= stableStability) {
            "Mastered stability must be >= stable stability"
        }
        require(maxMasteredLapses >= 0) { "Max mastered lapses must not be negative" }
    }

    companion object {
        fun kanjiCore(): MasteryPolicy = MasteryPolicy(
            requiredDimensions = setOf(
                ReviewDimension.KANJI_MEANING,
                ReviewDimension.KANJI_READING,
            ),
            familiarStability = 1.0,
            stableStability = 7.0,
            masteredStability = 30.0,
        )

        fun vocabularyCore(): MasteryPolicy = MasteryPolicy(
            requiredDimensions = setOf(
                ReviewDimension.VOCAB_MEANING,
                ReviewDimension.VOCAB_READING,
                ReviewDimension.VOCAB_SENSE,
            ),
            familiarStability = 1.0,
            stableStability = 7.0,
            masteredStability = 30.0,
        )
    }
}

data class ConnectedMasterySnapshot(
    val aggregate: MasteryLevel,
    val dimensions: Map<ReviewDimension, DimensionMastery>,
    val completedRequiredDimensions: Int,
    val requiredDimensionCount: Int,
) {
    val isComplete: Boolean
        get() = aggregate == MasteryLevel.Mastered
}

object MasteryReducer {

    fun reduce(
        cards: Map<ReviewDimension, FsrsCard>,
        policy: MasteryPolicy,
    ): ConnectedMasterySnapshot {
        val dimensionStates = policy.requiredDimensions
            .sortedBy { it.name }
            .associateWith { dimension ->
                reduceDimension(
                    dimension = dimension,
                    card = cards[dimension],
                    policy = policy,
                )
            }

        val levels = dimensionStates.values.map { it.level }
        val aggregate = reduceAggregate(levels)

        return ConnectedMasterySnapshot(
            aggregate = aggregate,
            dimensions = dimensionStates,
            completedRequiredDimensions = levels.count {
                it == MasteryLevel.Stable || it == MasteryLevel.Mastered
            },
            requiredDimensionCount = policy.requiredDimensions.size,
        )
    }

    private fun reduceDimension(
        dimension: ReviewDimension,
        card: FsrsCard?,
        policy: MasteryPolicy,
    ): DimensionMastery {
        if (card == null || card.params == FsrsCardParams.New) {
            return DimensionMastery(
                dimension = dimension,
                level = MasteryLevel.New,
                status = FsrsCardStatus.New,
                stability = 0.0,
                lapses = card?.lapses ?: 0,
                lastReview = card?.lastReview,
                dueAt = null,
            )
        }

        val existing = card.params as FsrsCardParams.Existing
        val dueAt = existing.reviewTime + card.interval
        val level = when {
            card.status == FsrsCardStatus.Relearning -> MasteryLevel.Learning
            existing.stability < policy.familiarStability -> MasteryLevel.Familiar
            card.lapses > policy.maxMasteredLapses -> MasteryLevel.Partial
            existing.stability >= policy.masteredStability &&
                card.lapses <= policy.maxMasteredLapses -> MasteryLevel.Mastered
            existing.stability >= policy.stableStability -> MasteryLevel.Stable
            else -> MasteryLevel.Familiar
        }

        return DimensionMastery(
            dimension = dimension,
            level = level,
            status = card.status,
            stability = existing.stability,
            lapses = card.lapses,
            lastReview = card.lastReview,
            dueAt = dueAt,
        )
    }

    private fun reduceAggregate(levels: List<MasteryLevel>): MasteryLevel {
        if (levels.isEmpty()) return MasteryLevel.New
        if (levels.all { it == MasteryLevel.New }) return MasteryLevel.New
        if (levels.all { it == MasteryLevel.Mastered }) return MasteryLevel.Mastered
        if (levels.any { it == MasteryLevel.Partial }) return MasteryLevel.Partial
        if (levels.all {
                it == MasteryLevel.Stable || it == MasteryLevel.Mastered
            }) return MasteryLevel.Stable
        if (levels.any { it == MasteryLevel.Learning || it == MasteryLevel.New }) {
            return MasteryLevel.Partial
        }
        return MasteryLevel.Familiar
    }
}
