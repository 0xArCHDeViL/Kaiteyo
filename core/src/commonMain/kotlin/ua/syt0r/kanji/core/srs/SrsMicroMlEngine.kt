package ua.syt0r.kanji.core.srs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryItem
import ua.syt0r.kanji.core.suspended_property.SuspendedProperty
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

private fun Double.srsFiniteOrZero(): Double = if (isFinite()) this else 0.0

/**
 * A deliberately small online learner that calibrates FSRS intervals locally.
 * It never replaces FSRS difficulty/stability updates and never touches learning steps.
 */
class SrsMicroMlEngine(
    private val profileStorage: SuspendedProperty<String>,
    private val scope: CoroutineScope,
) {

    private val json = Json { ignoreUnknownKeys = true }
    private var profiles: Map<Long, MicroMlProfile> = emptyMap()
    private val pendingFeatures = mutableMapOf<String, MicroMlFeatures>()
    private val ready: Deferred<Unit> = scope.async { load() }

    suspend fun schedule(
        key: SrsCardKey,
        card: SrsCard,
        scheduler: SrsScheduler,
        reviewTime: Instant,
    ): SrsAnswers {
        ready.await()
        val baseline = scheduler.answers(card, reviewTime)
        val profile = profiles[key.practiceType] ?: MicroMlProfile()
        val features = featuresFor(card, reviewTime, profile)
            ?: return baseline
        pendingFeatures[pendingKey(key)] = features

        if (profile.sampleCount < MIN_SAMPLES) {
            return baseline
        }

        val correction = correctionFactor(profile, features)
        return enforceOrdering(
            baseline.copy(
                again = correctAnswer(baseline.again, correction),
                hard = correctAnswer(baseline.hard, correction),
                good = correctAnswer(baseline.good, correction),
                easy = correctAnswer(baseline.easy, correction),
            )
        )
    }

    suspend fun observe(
        key: SrsCardKey,
        review: ReviewHistoryItem,
    ) {
        ready.await()
        val old = profiles[key.practiceType] ?: MicroMlProfile()
        val features = pendingFeatures.remove(pendingKey(key)) ?: return
        val label = if (review.grade > 1) 1.0 else 0.0
        val prediction = predict(old, features)
        val error = (label - prediction).coerceIn(-1.0, 1.0)
        val learningRate = (BASE_LEARNING_RATE / (1.0 + old.sampleCount * 0.01))
            .coerceIn(MIN_LEARNING_RATE, BASE_LEARNING_RATE)
        val updatedWeights = old.weights.mapIndexed { index, weight ->
            (weight + learningRate * error * features.values[index]).coerceIn(-WEIGHT_BOUND, WEIGHT_BOUND)
        }
        val updated = old.copy(
            sampleCount = old.sampleCount + 1,
            bias = (old.bias + learningRate * error).coerceIn(-WEIGHT_BOUND, WEIGHT_BOUND),
            weights = updatedWeights,
            failureRate = exponentialMovingAverage(old.failureRate, if (label == 0.0) 1.0 else 0.0),
            mistakeRate = exponentialMovingAverage(
                old.mistakeRate,
                (review.mistakes / MISTAKE_NORMALIZER).coerceIn(0.0, 1.0),
            ),
            responseEffort = exponentialMovingAverage(
                old.responseEffort,
                (review.duration.inWholeSeconds / RESPONSE_SECONDS_NORMALIZER).coerceIn(0.0, 1.0),
            ),
        ).normalized()

        profiles = profiles + (key.practiceType to updated)
        if (updated.sampleCount == 1L || updated.sampleCount % PERSIST_EVERY_SAMPLES == 0L) {
            persist()
        }
    }

    suspend fun reset() {
        ready.await()
        profiles = emptyMap()
        pendingFeatures.clear()
        profileStorage.set("")
    }

    private suspend fun load() {
        val raw = runCatching { profileStorage.get() }.getOrNull().orEmpty()
        if (raw.isBlank()) return
        profiles = runCatching {
            json.decodeFromString<MicroMlProfileStore>(raw)
                .profiles
                .mapValues { it.value.normalized() }
        }.getOrElse { emptyMap() }
    }

    private fun persist() {
        val snapshot = MicroMlProfileStore(profiles = profiles)
        scope.launch {
            runCatching { profileStorage.set(json.encodeToString(snapshot)) }
        }
    }

    private fun featuresFor(
        card: SrsCard,
        reviewTime: Instant,
        profile: MicroMlProfile,
    ): MicroMlFeatures? {
        val params = card.fsrsCard.params as? ua.syt0r.kanji.core.srs.fsrs.FsrsCardParams.Existing
            ?: return null
        val elapsedDays = ((reviewTime - params.reviewTime).inWholeMilliseconds / MILLIS_PER_DAY)
            .toDouble()
            .coerceAtLeast(0.0)
        val stability = params.stability.coerceAtLeast(MIN_STABILITY)
        val retrievability = (1.0 + FSRS_FACTOR * elapsedDays / stability)
            .pow(FSRS_DECAY)
            .coerceIn(0.01, 0.999)
        val intervalDays = card.interval.inWholeMilliseconds
            .toDouble()
            .div(MILLIS_PER_DAY)
            .coerceAtLeast(0.01)
        val latenessRatio = (elapsedDays / intervalDays).coerceIn(0.0, 4.0) / 4.0
        val difficulty = ((params.difficulty - 1.0) / 9.0).coerceIn(0.0, 1.0)
        val stabilityLog = (ln(1.0 + stability) / ln(1.0 + MAX_STABILITY)).coerceIn(0.0, 1.0)
        return MicroMlFeatures(
            values = listOf(
                retrievability,
                latenessRatio,
                difficulty,
                stabilityLog,
                profile.failureRate,
                profile.mistakeRate,
                profile.responseEffort,
                (profile.sampleCount / CONFIDENCE_SAMPLE_DIVISOR).coerceIn(0.0, 1.0),
            )
        )
    }

    private fun correctionFactor(profile: MicroMlProfile, features: MicroMlFeatures): Double {
        val confidence = ((profile.sampleCount - MIN_SAMPLES).toDouble() / CONFIDENCE_SAMPLE_DIVISOR)
            .coerceIn(0.0, 1.0)
        val predictedRecall = predict(profile, features)
        return (1.0 + (predictedRecall - BASE_RETENTION) * confidence * CORRECTION_GAIN)
            .coerceIn(MIN_CORRECTION, MAX_CORRECTION)
    }

    private fun predict(profile: MicroMlProfile, features: MicroMlFeatures): Double {
        val z = (profile.bias + profile.weights.zip(features.values).sumOf { (weight, value) -> weight * value })
            .coerceIn(-SIGMOID_LIMIT, SIGMOID_LIMIT)
        return 1.0 / (1.0 + exp(-z))
    }

    private fun correctAnswer(answer: SrsAnswer, factor: Double): SrsAnswer {
        val fsrsCard = answer.card.fsrsCard
        if (fsrsCard.status == ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus.Learning ||
            fsrsCard.status == ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus.New
        ) {
            return answer
        }
        if (answer.card.interval < 1.days) return answer
        val corrected = scaleDuration(answer.card.interval, factor)
            .coerceIn(1.days, MAX_INTERVAL)
        return answer.copy(card = answer.card.copy(fsrsCard = fsrsCard.copy(interval = corrected)))
    }

    private fun enforceOrdering(answers: SrsAnswers): SrsAnswers {
        val hardInterval = maxOf(answers.again.card.interval, answers.hard.card.interval)
        val goodInterval = maxOf(hardInterval, answers.good.card.interval)
        val easyInterval = maxOf(goodInterval, answers.easy.card.interval)
        return answers.copy(
            hard = answers.hard.withInterval(hardInterval),
            good = answers.good.withInterval(goodInterval),
            easy = answers.easy.withInterval(easyInterval),
        )
    }

    private fun SrsAnswer.withInterval(interval: Duration): SrsAnswer = copy(
        card = card.copy(fsrsCard = card.fsrsCard.copy(interval = interval))
    )

    private fun pendingKey(key: SrsCardKey) = "${key.practiceType}:${key.itemKey}"

    private fun exponentialMovingAverage(previous: Double, value: Double): Double =
        (previous * (1.0 - EMA_ALPHA) + value * EMA_ALPHA).coerceIn(0.0, 1.0)

    private fun scaleDuration(duration: Duration, factor: Double): Duration =
        (duration.inWholeMilliseconds.toDouble() * factor).roundToLong().milliseconds

    @Serializable
    private data class MicroMlProfileStore(
        val profiles: Map<Long, MicroMlProfile> = emptyMap(),
    )

    @Serializable
    private data class MicroMlProfile(
        val version: Int = PROFILE_VERSION,
        val sampleCount: Long = 0,
        val bias: Double = 0.0,
        val weights: List<Double> = List(FEATURE_COUNT) { 0.0 },
        val failureRate: Double = 0.5,
        val mistakeRate: Double = 0.0,
        val responseEffort: Double = 0.0,
    ) {
        fun normalized(): MicroMlProfile {
            if (version != PROFILE_VERSION || sampleCount < 0) return MicroMlProfile()
            val safeWeights = if (weights.size == FEATURE_COUNT) weights else List(FEATURE_COUNT) { 0.0 }
            return copy(
                bias = bias.srsFiniteOrZero().coerceIn(-WEIGHT_BOUND, WEIGHT_BOUND),
                weights = safeWeights.map { it.srsFiniteOrZero().coerceIn(-WEIGHT_BOUND, WEIGHT_BOUND) },
                failureRate = failureRate.srsFiniteOrZero().coerceIn(0.0, 1.0),
                mistakeRate = mistakeRate.srsFiniteOrZero().coerceIn(0.0, 1.0),
                responseEffort = responseEffort.srsFiniteOrZero().coerceIn(0.0, 1.0),
            )
        }
    }

    private data class MicroMlFeatures(val values: List<Double>)

    private companion object {
        const val PROFILE_VERSION = 1
        const val FEATURE_COUNT = 8
        const val MIN_SAMPLES = 32L
        const val PERSIST_EVERY_SAMPLES = 8L
        const val BASE_RETENTION = 0.9
        const val BASE_LEARNING_RATE = 0.08
        const val MIN_LEARNING_RATE = 0.01
        const val WEIGHT_BOUND = 3.0
        const val CORRECTION_GAIN = 0.8
        const val MIN_CORRECTION = 0.85
        const val MAX_CORRECTION = 1.15
        const val FSRS_FACTOR = 19.0 / 81.0
        const val FSRS_DECAY = -0.5
        const val MIN_STABILITY = 0.01
        const val MAX_STABILITY = 100000.0
        const val MILLIS_PER_DAY = 86_400_000.0
        const val MISTAKE_NORMALIZER = 3.0
        const val RESPONSE_SECONDS_NORMALIZER = 60.0
        const val CONFIDENCE_SAMPLE_DIVISOR = 32.0
        const val SIGMOID_LIMIT = 12.0
        val MAX_INTERVAL = 355.days
        const val EMA_ALPHA = 0.05
    }
}
