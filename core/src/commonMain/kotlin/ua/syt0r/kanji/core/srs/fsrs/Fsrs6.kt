package ua.syt0r.kanji.core.srs.fsrs

import kotlinx.datetime.Instant
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

val fsrs6Configuration = FsrsAlgorithmConfiguration(
    w = listOf(
        0.212, 1.2931, 2.3065, 8.2956, 6.4133, 0.8334, 3.0194, 0.001,
        1.8722, 0.1666, 0.796, 1.4835, 0.0614, 0.2629, 1.6483, 0.6014,
        1.8729, 0.5425, 0.0912, 0.0658, 0.1542
    ),
    factor = 0.0,
    decay = -0.1542,
    requestRetention = 0.9,
    maxInterval = 355.days,
    parameterSetId = "fsrs6-default"
)

class Fsrs6(
    configuration: FsrsAlgorithmConfiguration = fsrs6Configuration
) : FsrsAlgorithm {

    override val version: FsrsAlgorithmVersion = FsrsAlgorithmVersion.FSRS6
    override val parameterSetId: String = configuration.parameterSetId

    private val w = configuration.requireParameterCount(expected = 21).w
    private val requestRetention = configuration.requestRetention.requireRetention()
    private val maxInterval = configuration.maxInterval
    private val minimalStability = 0.01
    private val decay = -w[20]
    private val factor = exp(ln(0.9) / decay) - 1.0

    init {
        require(decay < 0.0 && decay.isFinite()) { "FSRS-6 decay must be finite and negative" }
        require(factor > 0.0 && factor.isFinite()) { "FSRS-6 forgetting factor must be finite and positive" }
        require(maxInterval.isPositive()) { "FSRS-6 maximum interval must be positive" }
        require(configuration.parameterSetId.isNotBlank()) { "FSRS parameter-set ID must not be blank" }
    }

    override fun updatedParams(
        card: FsrsCard,
        rating: FsrsReviewRating,
        reviewTime: Instant
    ): FsrsCardParams.Existing {
        return when (card.params) {
            FsrsCardParams.New -> FsrsCardParams.Existing(
                difficulty = initialDifficulty(rating).coerceIn(1.0, 10.0),
                stability = initialStability(rating).coerceIn(minimalStability, MAX_STABILITY),
                reviewTime = reviewTime
            )

            is FsrsCardParams.Existing -> {
                val difficulty = nextDifficulty(card.params.difficulty, rating)
                val elapsedDuration = when (card.status) {
                    FsrsCardStatus.Learning, FsrsCardStatus.Relearning -> card.interval
                    else -> reviewTime - card.params.reviewTime
                }
                val elapsedDays = elapsedDuration.toDouble(DurationUnit.DAYS).coerceAtLeast(0.0)
                val retrievability = forgettingCurve(elapsedDays, card.params.stability)
                val stability = nextStability(
                    params = card.params,
                    status = card.status,
                    rating = rating,
                    retrievability = retrievability,
                    elapsedDays = elapsedDays
                ).coerceIn(minimalStability, MAX_STABILITY)

                FsrsCardParams.Existing(
                    difficulty = difficulty,
                    stability = stability,
                    reviewTime = reviewTime
                )
            }
        }
    }

    override fun nextInterval(cardParams: FsrsCardParams.Existing): Duration {
        val interval = cardParams.stability / factor *
            (requestRetention.pow(1.0 / decay) - 1.0)
        return interval.safeRoundedDays().coerceIn(1.days, maxInterval)
    }

    private fun initialStability(rating: FsrsReviewRating): Double =
        w[rating.grade - 1]

    private fun initialDifficulty(rating: FsrsReviewRating): Double =
        w[4] - exp(w[5] * (rating.grade - 1)) + 1.0

    private fun nextDifficulty(
        difficulty: Double,
        rating: FsrsReviewRating
    ): Double {
        val deltaDifficulty = -w[6] * (rating.grade - 3)
        val damped = (10.0 - difficulty) * deltaDifficulty / 9.0
        val nextDifficulty = difficulty + damped
        val easyDifficulty = initialDifficulty(FsrsReviewRating.Easy)
        return (w[7] * (easyDifficulty - nextDifficulty) + nextDifficulty)
            .coerceIn(1.0, 10.0)
    }

    private fun nextStability(
        params: FsrsCardParams.Existing,
        status: FsrsCardStatus,
        rating: FsrsReviewRating,
        retrievability: Double,
        elapsedDays: Double
    ): Double {
        val stability = when (status) {
            FsrsCardStatus.Learning, FsrsCardStatus.Relearning ->
                shortTermStability(params.stability, rating)

            FsrsCardStatus.Review -> when (rating) {
                FsrsReviewRating.Again -> forgetStability(
                    difficulty = params.difficulty,
                    stability = params.stability,
                    retrievability = retrievability
                )

                else -> recallStability(
                    difficulty = params.difficulty,
                    stability = params.stability,
                    retrievability = retrievability,
                    rating = rating
                )
            }

            FsrsCardStatus.New -> error("Existing FSRS params cannot have New status")
        }

        return if (elapsedDays == 0.0) {
            shortTermStability(params.stability, rating)
        } else {
            stability
        }
    }

    private fun forgettingCurve(elapsedDays: Double, stability: Double): Double =
        (1.0 + factor * elapsedDays / stability.coerceAtLeast(minimalStability))
            .pow(decay)
            .coerceIn(0.0, 1.0)

    private fun shortTermStability(
        stability: Double,
        rating: FsrsReviewRating
    ): Double {
        val sinc = exp(w[17] * (rating.grade - 3 + w[18])) *
            stability.coerceAtLeast(minimalStability).pow(-w[19])
        val multiplier = if (rating.grade >= FsrsReviewRating.Hard.grade) maxOf(sinc, 1.0) else sinc
        return stability * multiplier
    }

    private fun forgetStability(
        difficulty: Double,
        stability: Double,
        retrievability: Double
    ): Double {
        val newStability = w[11] * difficulty.pow(-w[12]) *
            ((stability + 1.0).pow(w[13]) - 1.0) *
            exp(w[14] * (1.0 - retrievability))
        val minimumAfterFailure = stability / exp(w[17] * w[18])
        return minOf(newStability, minimumAfterFailure)
    }

    private fun recallStability(
        difficulty: Double,
        stability: Double,
        retrievability: Double,
        rating: FsrsReviewRating
    ): Double {
        val hardPenalty = if (rating == FsrsReviewRating.Hard) w[15] else 1.0
        val easyBonus = if (rating == FsrsReviewRating.Easy) w[16] else 1.0
        return stability * (
            exp(w[8]) *
                (11.0 - difficulty) *
                stability.pow(-w[9]) *
                (exp(w[10] * (1.0 - retrievability)) - 1.0) *
                hardPenalty * easyBonus + 1.0
        )
    }

    private fun Double.safeRoundedDays(): Duration {
        if (!isFinite() || this <= 0.0) return 1.days
        return roundToInt().coerceAtLeast(1).days
    }

    private fun FsrsAlgorithmConfiguration.requireParameterCount(expected: Int) = apply {
        require(w.size == expected) { "Expected $expected FSRS parameters, got ${w.size}" }
        require(w.all(Double::isFinite)) { "FSRS parameters must be finite" }
        require(maxInterval.isPositive()) { "FSRS maximum interval must be positive" }
    }

    private fun Double.requireRetention(): Double = also {
        require(isFinite() && this in 0.1..0.999) {
            "FSRS desired retention must be finite and in [0.1, 0.999]"
        }
    }

    private fun Duration.isPositive(): Boolean = this > Duration.ZERO

    private companion object {
        const val MAX_STABILITY = 100_000.0
    }
}
