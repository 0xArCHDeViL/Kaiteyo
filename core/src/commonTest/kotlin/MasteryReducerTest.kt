import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.connected_learning.ConnectedItemKey
import ua.syt0r.kanji.core.connected_learning.MasteryLevel
import ua.syt0r.kanji.core.connected_learning.MasteryPolicy
import ua.syt0r.kanji.core.connected_learning.MasteryReducer
import ua.syt0r.kanji.core.connected_learning.ReviewDimension
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardParams
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus
import kotlin.time.Duration.Companion.days

class MasteryReducerTest {

    private val reviewedAt = Instant.parse("2026-01-01T00:00:00Z")

    @Test
    fun missingRequiredDimensionPreventsMastery() {
        val snapshot = MasteryReducer.reduce(
            cards = mapOf(
                ReviewDimension.KANJI_MEANING to card(stability = 45.0),
            ),
            policy = MasteryPolicy.kanjiCore(),
        )

        assertEquals(MasteryLevel.Partial, snapshot.aggregate)
        assertEquals(MasteryLevel.Mastered, snapshot.dimensions[ReviewDimension.KANJI_MEANING]?.level)
        assertEquals(MasteryLevel.New, snapshot.dimensions[ReviewDimension.KANJI_READING]?.level)
        assertFalse(snapshot.isComplete)
    }

    @Test
    fun meaningMasteryDoesNotLeakIntoReading() {
        val snapshot = MasteryReducer.reduce(
            cards = mapOf(
                ReviewDimension.KANJI_MEANING to card(stability = 45.0),
                ReviewDimension.KANJI_READING to card(stability = 2.0),
            ),
            policy = MasteryPolicy.kanjiCore(),
        )

        assertEquals(MasteryLevel.Mastered, snapshot.dimensions[ReviewDimension.KANJI_MEANING]?.level)
        assertEquals(MasteryLevel.Familiar, snapshot.dimensions[ReviewDimension.KANJI_READING]?.level)
        assertEquals(MasteryLevel.Familiar, snapshot.aggregate)
    }

    @Test
    fun stableDimensionsProduceStableAggregate() {
        val snapshot = MasteryReducer.reduce(
            cards = mapOf(
                ReviewDimension.KANJI_MEANING to card(stability = 8.0),
                ReviewDimension.KANJI_READING to card(stability = 12.0),
            ),
            policy = MasteryPolicy.kanjiCore(),
        )

        assertEquals(MasteryLevel.Stable, snapshot.aggregate)
        assertEquals(2, snapshot.completedRequiredDimensions)
        assertTrue(snapshot.isComplete.not())
    }

    @Test
    fun masteredRequiresAllDimensionsAndCriticalLapsePolicy() {
        val snapshot = MasteryReducer.reduce(
            cards = mapOf(
                ReviewDimension.KANJI_MEANING to card(stability = 45.0, lapses = 0),
                ReviewDimension.KANJI_READING to card(stability = 45.0, lapses = 1),
            ),
            policy = MasteryPolicy.kanjiCore(),
        )

        assertEquals(MasteryLevel.Partial, snapshot.aggregate)
        assertEquals(MasteryLevel.Mastered, snapshot.dimensions[ReviewDimension.KANJI_MEANING]?.level)
        assertEquals(MasteryLevel.Partial, snapshot.dimensions[ReviewDimension.KANJI_READING]?.level)
    }

    private fun card(
        stability: Double,
        lapses: Int = 0,
    ): FsrsCard = FsrsCard(
        status = FsrsCardStatus.Review,
        params = FsrsCardParams.Existing(
            difficulty = 5.0,
            stability = stability,
            reviewTime = reviewedAt,
        ),
        interval = 1.days,
        lapses = lapses,
        repeats = 3,
    )
}
