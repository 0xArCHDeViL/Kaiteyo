package ua.syt0r.kanji.core.user_data.database.sqldelight

import ua.syt0r.kanji.core.srs.SrsCardKey
import ua.syt0r.kanji.core.srs.fsrs.FsrsAlgorithmVersion
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardParams
import ua.syt0r.kanji.core.user_data.database.ReviewCommitRepository
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryItem
import ua.syt0r.kanji.core.user_data.database.UserDataDatabaseContract
import ua.syt0r.kanji.core.userdata.db.Fsrs_card

class SqlDelightReviewCommitRepository(
    private val databaseManager: UserDataDatabaseContract.Manager,
) : ReviewCommitRepository {

    override suspend fun commitReview(
        key: SrsCardKey,
        card: FsrsCard,
        review: ReviewHistoryItem,
        algorithmVersion: FsrsAlgorithmVersion,
        parameterSetId: String,
    ) {
        require(key.itemKey == review.key) { "Review key does not match card key" }
        require(key.practiceType == review.practiceType) {
            "Review practice type does not match card practice type"
        }
        require(card.params is FsrsCardParams.Existing) {
            "A reviewed card must have existing FSRS parameters"
        }
        require(parameterSetId.isNotBlank()) { "FSRS parameter-set ID must not be blank" }
        require(review.grade in 1..4) { "Review grade must be in 1..4" }
        require(review.duration >= kotlin.time.Duration.ZERO) { "Review duration must not be negative" }
        require(review.mistakes >= 0) { "Review mistakes must not be negative" }

        databaseManager.writeTransaction {
            upsertFsrsCard(card.toDatabaseCard(key))
            upsertReview(
                key = review.key,
                practice_type = review.practiceType,
                timestamp = review.timestamp.toEpochMilliseconds(),
                duration = review.duration.inWholeMilliseconds,
                grade = review.grade.toLong(),
                mistakes = review.mistakes.toLong(),
                deck_id = review.deckId,
            )
            upsertReviewSchedulerMetadata(
                key = review.key,
                practice_type = review.practiceType,
                timestamp = review.timestamp.toEpochMilliseconds(),
                algorithm_version = algorithmVersion.name,
                parameter_set_id = parameterSetId,
            )
        }
    }

    private fun FsrsCard.toDatabaseCard(key: SrsCardKey): Fsrs_card {
        val params = params as FsrsCardParams.Existing
        return Fsrs_card(
            key = key.itemKey,
            practice_type = key.practiceType,
            status = status.ordinal.toLong(),
            stability = params.stability,
            difficulty = params.difficulty,
            lapses = lapses.toLong(),
            repeats = repeats.toLong(),
            last_review = params.reviewTime.toEpochMilliseconds(),
            interval = interval.inWholeMilliseconds,
        )
    }
}
