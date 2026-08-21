package ua.syt0r.kanji.core.user_data.database.sqldelight

import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.connected_learning.ConnectedItemKey
import ua.syt0r.kanji.core.connected_learning.ReviewDimension
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardParams
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus
import ua.syt0r.kanji.core.user_data.database.ConnectedReviewCard
import ua.syt0r.kanji.core.user_data.database.ConnectedReviewEvent
import ua.syt0r.kanji.core.user_data.database.ConnectedReviewItem
import ua.syt0r.kanji.core.user_data.database.ConnectedReviewRepository
import ua.syt0r.kanji.core.user_data.database.ObservableRepository
import ua.syt0r.kanji.core.user_data.database.ObservableUserDataRepository
import ua.syt0r.kanji.core.user_data.database.UserDataDatabaseContract
import ua.syt0r.kanji.core.userdata.db.Connected_fsrs_card
import kotlin.time.Duration.Companion.milliseconds

class SqlDelightConnectedReviewRepository private constructor(
    observableRepository: ObservableUserDataRepository,
) : ConnectedReviewRepository,
    ObservableRepository by observableRepository,
    UserDataDatabaseContract.TransactionScope by observableRepository {

    constructor(
        userDataDatabaseManager: UserDataDatabaseContract.Manager,
    ) : this(ObservableUserDataRepository(userDataDatabaseManager))

    override suspend fun getCards(
        itemKeys: Collection<ConnectedItemKey>,
    ): Map<ConnectedItemKey, Map<ReviewDimension, ConnectedReviewCard>> {
        if (itemKeys.isEmpty()) return emptyMap()
        val rows = readTransaction {
            getConnectedFsrsCardsForItems(itemKeys.map { it.value }.distinct())
                .executeAsList()
        }
        return rows.mapNotNull { row -> row.toConnectedReviewCardOrNull() }
            .groupBy { it.itemKey }
            .mapValues { (_, cards) -> cards.associateBy { it.dimension } }
    }

    override suspend fun getDueCards(
        now: Instant,
        dimensions: Collection<ReviewDimension>,
        limit: Long,
    ): List<ConnectedReviewCard> {
        require(dimensions.isNotEmpty()) { "At least one review dimension is required" }
        require(limit in 1..2_000) { "Connected review limit must be between 1 and 2000" }
        return readTransaction {
            getDueConnectedFsrsCards(
                now = now.toEpochMilliseconds(),
                dimensions = dimensions.map { it.name }.distinct().sorted(),
                limit = limit,
            ).executeAsList()
        }.mapNotNull { it.toConnectedReviewCardOrNull() }
    }

    override suspend fun upsertItem(item: ConnectedReviewItem) {
        writeTransaction {
            upsertConnectedReviewItem(
                item.itemKey.value,
                item.entityKind,
                item.entityId,
                item.entryId,
                item.elementId,
                item.senseId,
                item.variant,
                item.legacyKey,
                item.legacyPracticeType,
                item.createdAt.toEpochMilliseconds(),
            )
        }
    }

    override suspend fun upsertCard(card: ConnectedReviewCard) {
        writeTransaction {
            upsertConnectedFsrsCard(
                card.itemKey.value,
                card.dimension.name,
                card.card.status.ordinal.toLong(),
                card.card.params.stabilityOrZero(),
                card.card.params.difficultyOrZero(),
                card.card.lapses.toLong(),
                card.card.repeats.toLong(),
                card.card.lastReview?.toEpochMilliseconds(),
                card.dueAt.toEpochMilliseconds(),
                card.card.interval.inWholeMilliseconds,
                card.firstSeen.toEpochMilliseconds(),
                if (card.suspended) 1L else 0L,
            )
        }
    }

    override suspend fun recordEvent(event: ConnectedReviewEvent) {
        writeTransaction {
            insertConnectedReviewEvent(
                event.itemKey.value,
                event.dimension.name,
                event.taskKind,
                event.promptVariant,
                event.timestamp.toEpochMilliseconds(),
                event.durationMs,
                event.grade,
                event.mistakes,
                event.deckId,
                event.contextKey,
                event.source,
            )
        }
    }

    private fun Connected_fsrs_card.toConnectedReviewCardOrNull(): ConnectedReviewCard? {
        val dimension = ReviewDimension.entries.firstOrNull { it.name == this.dimension }
            ?: return null
        val status = FsrsCardStatus.entries.getOrNull(status.toInt()) ?: return null
        val lastReview = last_review?.let(Instant::fromEpochMilliseconds)
        val params = if (lastReview == null) {
            FsrsCardParams.New
        } else {
            FsrsCardParams.Existing(
                difficulty = difficulty,
                stability = stability,
                reviewTime = lastReview,
            )
        }
        return ConnectedReviewCard(
            itemKey = ConnectedItemKey(item_key),
            dimension = dimension,
            card = FsrsCard(
                status = status,
                params = params,
                interval = interval.milliseconds,
                lapses = lapses.toInt(),
                repeats = repeats.toInt(),
            ),
            dueAt = Instant.fromEpochMilliseconds(due_at),
            firstSeen = Instant.fromEpochMilliseconds(first_seen),
            suspended = suspended != 0L,
        )
    }

    private fun FsrsCardParams.stabilityOrZero(): Double = when (this) {
        FsrsCardParams.New -> 0.0
        is FsrsCardParams.Existing -> stability
    }

    private fun FsrsCardParams.difficultyOrZero(): Double = when (this) {
        FsrsCardParams.New -> 0.0
        is FsrsCardParams.Existing -> difficulty
    }
}
