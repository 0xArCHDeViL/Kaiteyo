import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import ua.syt0r.kanji.core.srs.fsrs.FsrsMigrationValidator
import ua.syt0r.kanji.core.srs.fsrs.FsrsReplayReview
import ua.syt0r.kanji.core.srs.fsrs.FsrsReviewRating
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.days

class FsrsMigrationValidatorTest {

    private val start = LocalDateTime(2026, 1, 1, 12, 0).toInstant(TimeZone.UTC)
    private val validator = FsrsMigrationValidator()

    @Test
    fun replayComparesFsrs5AndFsrs6FromSameHistory() {
        val history = listOf(
            FsrsReplayReview(start, FsrsReviewRating.Good),
            FsrsReplayReview(start + 4.days, FsrsReviewRating.Good),
            FsrsReplayReview(start + 14.days, FsrsReviewRating.Again),
            FsrsReplayReview(start + 15.days, FsrsReviewRating.Good),
        )

        val comparison = validator.compare(history)

        assertEquals(1, comparison.fsrs5.lapses)
        assertEquals(1, comparison.fsrs6.lapses)
        assertNotEquals(
            comparison.fsrs5.params,
            comparison.fsrs6.params,
            "A versioned migration must expose material FSRS-5/FSRS-6 state differences"
        )
    }

    @Test
    fun emptyHistoryReplaysToNewCards() {
        val comparison = validator.compare(emptyList())

        assertEquals(0, comparison.fsrs5.repeats)
        assertEquals(0, comparison.fsrs6.repeats)
        assertEquals(0L, comparison.intervalDeltaMillis)
    }

    @Test
    fun migrationRejectsNonChronologicalHistory() {
        assertFailsWith<IllegalArgumentException> {
            validator.compare(
                listOf(
                    FsrsReplayReview(start + 1.days, FsrsReviewRating.Good),
                    FsrsReplayReview(start, FsrsReviewRating.Good),
                )
            )
        }
    }
}
