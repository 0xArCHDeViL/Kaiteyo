package ua.syt0r.kanji.core.user_data.database.sqldelight

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryItem
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryRepository
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryStatItem
import ua.syt0r.kanji.core.user_data.database.StreakData
import ua.syt0r.kanji.core.user_data.database.UserDataDatabaseContract
import ua.syt0r.kanji.core.userdata.db.Review_history
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


class SqlDelightReviewHistoryRepository(
    private val userDataDatabaseManager: UserDataDatabaseContract.Manager
) : ReviewHistoryRepository {

    override suspend fun addReview(
        item: ReviewHistoryItem
    ) = userDataDatabaseManager.writeTransaction {
        item.run {
            upsertReview(
                key = key,
                practice_type = practiceType,
                timestamp = timestamp.toEpochMilliseconds(),
                duration = duration.inWholeMilliseconds,
                grade = grade.toLong(),
                mistakes = mistakes.toLong(),
                deck_id = deckId
            )
        }
    }

    override suspend fun getReviews(
        start: Instant,
        end: Instant
    ): List<ReviewHistoryItem> = userDataDatabaseManager.readTransaction {
        getReviewHistory(start.toEpochMilliseconds(), end.toEpochMilliseconds())
            .executeAsList()
            .map { it.converted() }
    }

    override suspend fun getFirstReviewTime(
        key: String,
        practiceType: Long
    ): Instant? = userDataDatabaseManager.readTransaction {
        getFirstReview(key, practiceType)
            .executeAsOneOrNull()
            ?.converted()
            ?.timestamp
    }

    override suspend fun getDeckLastReview(
        deckId: Long,
        practiceTypes: List<Long>
    ): Instant? = userDataDatabaseManager.readTransaction {
        getLastDeckReview(deckId, practiceTypes).executeAsOneOrNull()?.MAX
            ?.let { Instant.fromEpochMilliseconds(it) }
    }

    override suspend fun getTotalReviewsCount(): Long = userDataDatabaseManager
        .readTransaction { getTotalReviewsCount().executeAsOne() }

    override suspend fun getTotalReviewCount(
        key: String,
        practiceType: Long
    ): Long = userDataDatabaseManager.readTransaction {
        getTotalReviewsCountOfType(key, practiceType).executeAsOne()
    }

    override suspend fun getUniqueReviewItemsCount(
        practiceTypes: List<Long>
    ): Long = userDataDatabaseManager.readTransaction {
        getUniqueReviewItemsCountForPracticeTypes(practiceTypes).executeAsOne()
    }

    override suspend fun getTotalPracticeTime(
        singleReviewDurationLimit: Long
    ): Duration = userDataDatabaseManager.readTransaction {
        getTotalReviewsDuration(singleReviewDurationLimit).executeAsOneOrNull()
            ?.SUM
            ?.milliseconds
            ?: Duration.ZERO
    }

    override suspend fun getStreaks(
        timeOffset: LocalTime
    ) = userDataDatabaseManager.readTransaction {
        val offsetMillis = timeOffset.toMillisecondOfDay().toLong()
        getReviewStreaks(offsetMillis).executeAsList()
            .mapNotNull { row ->
                val startDate = row.start_date ?: return@mapNotNull null
                val endDate = row.end_date ?: return@mapNotNull null
                runCatching {
                    StreakData(
                        start = LocalDate.parse(startDate),
                        end = LocalDate.parse(endDate),
                        length = row.sequence_length.toInt()
                    )
                }.onFailure { error ->
                    Logger.w("Skipping malformed review streak: ${error.message}")
                }.getOrNull()
            }
    }

    override suspend fun getReviewHistoryStatsForKeys(
        keys: List<String>
    ): Map<String, ReviewHistoryStatItem> = userDataDatabaseManager.readTransaction {
        val timeDelimiter = "|"
        val practiceTypeDelimiter = ";"
        keys.chunked(500)
            .flatMap { getReviewHistoryStatsForKeys(it).executeAsList() }
            .associate {
                it.key to ReviewHistoryStatItem(
                    key = it.key,
                    practiceTypeToDataMap = it.ReviewData
                        ?.split(practiceTypeDelimiter)
                        ?.mapNotNull { encoded ->
                            runCatching {
                                val values = encoded.split(timeDelimiter)
                                require(values.size >= 3)
                                val practiceType = values[0].toLong()
                                practiceType to ReviewHistoryStatItem.PracticeTypeData(
                                    firstReview = Instant.fromEpochMilliseconds(values[1].toLong()),
                                    lastReview = Instant.fromEpochMilliseconds(values[2].toLong())
                                )
                            }.onFailure { error ->
                                Logger.w("Skipping malformed review stats value: ${error.message}")
                            }.getOrNull()
                        }
                        ?.toMap()
                        ?: emptyMap()
                )
            }
    }

    private fun Review_history.converted(): ReviewHistoryItem = ReviewHistoryItem(
        key = key,
        practiceType = practice_type,
        timestamp = Instant.fromEpochMilliseconds(timestamp),
        duration = duration.milliseconds,
        grade = grade.toInt(),
        mistakes = mistakes.toInt(),
        deckId = deck_id
    )

}
