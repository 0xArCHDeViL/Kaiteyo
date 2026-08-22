import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import ua.syt0r.kanji.core.srs.fsrs.Fsrs5
import ua.syt0r.kanji.core.srs.fsrs.Fsrs6
import ua.syt0r.kanji.core.srs.fsrs.fsrs5Configuration
import ua.syt0r.kanji.core.srs.fsrs.fsrs6Configuration
import ua.syt0r.kanji.core.srs.fsrs.FsrsAlgorithmConfiguration
import ua.syt0r.kanji.core.srs.fsrs.FsrsAlgorithmVersion
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardParams
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus
import ua.syt0r.kanji.core.srs.fsrs.FsrsReviewRating
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

data class FsrsGoldenVector(
    val rating: FsrsReviewRating,
    val expectedDifficulty: Double,
    val expectedStability: Double,
    val expectedIntervalDays: Int,
)

class FsrsGoldenVectorTest {

    private val now: Instant = LocalDateTime(2026, 1, 1, 12, 0)
        .toInstant(TimeZone.UTC)
    private val scheduler = Fsrs6()
    private val fsrs5 = Fsrs5()
    private val reviewCard = FsrsCard(
        status = FsrsCardStatus.Review,
        params = FsrsCardParams.Existing(
            difficulty = 6.0,
            stability = 10.0,
            reviewTime = now - 10.days
        ),
        interval = 10.days,
        lapses = 2,
        repeats = 8
    )

    @Test
    fun reviewStateMatchesFsrs6ReferenceVectors() {
        val vectors = listOf(
            FsrsGoldenVector(FsrsReviewRating.Again, 8.67046, 1.37649, 1),
            FsrsGoldenVector(FsrsReviewRating.Hard, 7.32984, 21.03906, 21),
            FsrsGoldenVector(FsrsReviewRating.Good, 5.98923, 28.35561, 28),
            FsrsGoldenVector(FsrsReviewRating.Easy, 4.64861, 44.37822, 44),
        )

        vectors.forEach { vector ->
            val params = scheduler.updatedParams(reviewCard, vector.rating, now)
            assertClose(vector.expectedDifficulty, params.difficulty)
            assertClose(vector.expectedStability, params.stability)
            assertEquals(
                vector.expectedIntervalDays.toDouble(),
                scheduler.nextInterval(params).toDouble(DurationUnit.DAYS)
            )
        }
    }

    @Test
    fun fsrs5BaselineMatchesReferenceVectors() {
        val vectors = listOf(
            FsrsGoldenVector(FsrsReviewRating.Again, 7.27939, 2.06585, 2),
            FsrsGoldenVector(FsrsReviewRating.Hard, 6.63331, 14.42826, 14),
            FsrsGoldenVector(FsrsReviewRating.Good, 5.98723, 29.12855, 29),
            FsrsGoldenVector(FsrsReviewRating.Easy, 5.34115, 67.19055, 67),
        )

        vectors.forEach { vector ->
            val params = fsrs5.updatedParams(reviewCard, vector.rating, now)
            assertClose(vector.expectedDifficulty, params.difficulty)
            assertClose(vector.expectedStability, params.stability)
            assertEquals(
                vector.expectedIntervalDays.toDouble(),
                fsrs5.nextInterval(params).toDouble(DurationUnit.DAYS)
            )
        }
    }

    @Test
    fun sameDayReviewUsesFsrs6ShortTermFormula() {
        val sameDayCard = reviewCard.copy(
            params = FsrsCardParams.Existing(
                difficulty = 6.0,
                stability = 10.0,
                reviewTime = now,
            ),
            interval = kotlin.time.Duration.ZERO,
        )
        val expectedStabilities = mapOf(
            FsrsReviewRating.Again to 3.0512489,
            FsrsReviewRating.Hard to 10.0,
            FsrsReviewRating.Good to 10.0,
            FsrsReviewRating.Easy to 15.5343079,
        )

        expectedStabilities.forEach { (rating, expected) ->
            assertClose(expected, scheduler.updatedParams(sameDayCard, rating, now).stability)
        }
    }

    @Test
    fun failureStabilityIsBoundByFsrs6FailureCap() {
        val lowStabilityCard = FsrsCard(
            status = FsrsCardStatus.Review,
            params = FsrsCardParams.Existing(
                difficulty = 5.0,
                stability = 0.1,
                reviewTime = now - 100.days,
            ),
            interval = 100.days,
            lapses = 0,
            repeats = 1,
        )

        val updated = scheduler.updatedParams(lowStabilityCard, FsrsReviewRating.Again, now)
        assertClose(0.0951728, updated.stability)
    }

    @Test
    fun maxIntervalPolicyIsAppliedAfterFsrs6IntervalCalculation() {
        val params = FsrsCardParams.Existing(
            difficulty = 5.0,
            stability = 100_000.0,
            reviewTime = now,
        )
        assertEquals(355.0, scheduler.nextInterval(params).toDouble(DurationUnit.DAYS))
    }

    @Test
    fun firstReviewDifficultyUsesFsrs6InitialVectorAndClamp() {
        val expected = listOf(6.4133, 5.1121707, 2.1181040, 1.0)
        FsrsReviewRating.entries.forEachIndexed { index, rating ->
            val params = scheduler.updatedParams(
                card = FsrsCard(
                    status = FsrsCardStatus.New,
                    params = FsrsCardParams.New,
                    interval = kotlin.time.Duration.ZERO,
                    lapses = 0,
                    repeats = 0,
                ),
                rating = rating,
                reviewTime = now,
            )
            assertClose(expected[index], params.difficulty)
        }
    }

    @Test
    fun algorithmsExposeStableVersionIdentity() {
        assertEquals(FsrsAlgorithmVersion.FSRS5, fsrs5.version)
        assertEquals(FsrsAlgorithmVersion.FSRS6, scheduler.version)
    }

    @Test
    fun newStateUsesFsrs6InitialStabilityVector() {
        val expected = listOf(0.212, 1.2931, 2.3065, 8.2956)
        FsrsReviewRating.entries.forEachIndexed { index, rating ->
            val params = scheduler.updatedParams(
                card = FsrsCard(
                    status = FsrsCardStatus.New,
                    params = FsrsCardParams.New,
                    interval = kotlin.time.Duration.ZERO,
                    lapses = 0,
                    repeats = 0
                ),
                rating = rating,
                reviewTime = now
            )
            assertClose(expected[index], params.stability)
        }
    }

    @Test
    fun invalidFsrs6ConfigurationIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            Fsrs6(
                FsrsAlgorithmConfiguration(
                    w = List(20) { 0.1 },
                    factor = 0.0,
                    decay = -0.1,
                    requestRetention = 0.9,
                    maxInterval = 355.days
                )
            )
        }

        assertFailsWith<IllegalArgumentException> {
            Fsrs6(
                FsrsAlgorithmConfiguration(
                    w = List(21) { 0.1 },
                    factor = 0.0,
                    decay = -0.1,
                    requestRetention = 1.0,
                    maxInterval = 355.days
                )
            )
        }

        assertFailsWith<IllegalArgumentException> {
            Fsrs5(
                FsrsAlgorithmConfiguration(
                    w = fsrs5Configuration.w,
                    factor = 0.0,
                    decay = -0.5,
                    requestRetention = 0.9,
                    maxInterval = 355.days,
                )
            )
        }

        assertFailsWith<IllegalArgumentException> {
            Fsrs6(
                FsrsAlgorithmConfiguration(
                    w = fsrs6Configuration.w,
                    factor = 0.0,
                    decay = -0.1542,
                    requestRetention = 0.9,
                    maxInterval = 355.days,
                    parameterSetId = " ",
                )
            )
        }
    }

    private fun assertClose(expected: Double, actual: Double) {
        assertTrue(
            abs(expected - actual) <= 0.0001,
            "Expected $expected, got $actual"
        )
    }
}
