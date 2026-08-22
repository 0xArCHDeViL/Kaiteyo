package ua.syt0r.kanji.core.srs.fsrs

import kotlinx.datetime.Instant

data class FsrsReplayReview(
    val timestamp: Instant,
    val rating: FsrsReviewRating,
)

data class FsrsMigrationComparison(
    val fsrs5: FsrsCard,
    val fsrs6: FsrsCard,
) {
    val intervalDeltaMillis: Long
        get() = fsrs6.interval.inWholeMilliseconds - fsrs5.interval.inWholeMilliseconds

    val stabilityDelta: Double?
        get() {
            val oldParams = fsrs5.params as? FsrsCardParams.Existing ?: return null
            val newParams = fsrs6.params as? FsrsCardParams.Existing ?: return null
            return newParams.stability - oldParams.stability
        }

    val difficultyDelta: Double?
        get() {
            val oldParams = fsrs5.params as? FsrsCardParams.Existing ?: return null
            val newParams = fsrs6.params as? FsrsCardParams.Existing ?: return null
            return newParams.difficulty - oldParams.difficulty
        }
}

/**
 * Replays the same immutable history through two versioned schedulers.
 * The history must be ordered by timestamp and must contain only one answer per timestamp.
 */
class FsrsMigrationValidator(
    private val fsrs5Scheduler: FsrsScheduler = DefaultFsrsScheduler(Fsrs5()),
    private val fsrs6Scheduler: FsrsScheduler = DefaultFsrsScheduler(Fsrs6()),
) {

    fun compare(history: List<FsrsReplayReview>): FsrsMigrationComparison {
        require(history.zipWithNext().all { (first, second) -> first.timestamp < second.timestamp }) {
            "FSRS migration history must be strictly ordered by timestamp"
        }

        return FsrsMigrationComparison(
            fsrs5 = replay(fsrs5Scheduler, history),
            fsrs6 = replay(fsrs6Scheduler, history),
        )
    }

    private fun replay(
        scheduler: FsrsScheduler,
        history: List<FsrsReplayReview>,
    ): FsrsCard {
        var card = scheduler.newCard()
        history.forEach { review ->
            card = scheduler.schedule(card, review.timestamp).forRating(review.rating)
        }
        return card
    }

    private fun FsrsAnswers.forRating(rating: FsrsReviewRating): FsrsCard = when (rating) {
        FsrsReviewRating.Again -> again
        FsrsReviewRating.Hard -> hard
        FsrsReviewRating.Good -> good
        FsrsReviewRating.Easy -> easy
    }
}
