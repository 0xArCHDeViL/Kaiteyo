package ua.syt0r.kanji.core.user_data.database

import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.connected_learning.ConnectedItemKey
import ua.syt0r.kanji.core.connected_learning.ReviewDimension
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard

/** Persisted identity metadata for one connected-learning review item. */
data class ConnectedReviewItem(
    val itemKey: ConnectedItemKey,
    val entityKind: String,
    val entityId: String,
    val entryId: Long? = null,
    val elementId: Long? = null,
    val senseId: Long? = null,
    val variant: String = "default",
    val legacyKey: String? = null,
    val legacyPracticeType: Long? = null,
    val createdAt: Instant,
)

/** FSRS state plus the connected-learning scheduling metadata around it. */
data class ConnectedReviewCard(
    val itemKey: ConnectedItemKey,
    val dimension: ReviewDimension,
    val card: FsrsCard,
    val dueAt: Instant,
    val firstSeen: Instant,
    val suspended: Boolean,
)

data class ConnectedReviewCommit(
    val item: ConnectedReviewItem,
    val card: ConnectedReviewCard,
    val event: ConnectedReviewEvent,
)

data class ConnectedReviewEvent(
    val itemKey: ConnectedItemKey,
    val dimension: ReviewDimension,
    val taskKind: String,
    val promptVariant: String = "",
    val timestamp: Instant,
    val durationMs: Long,
    val grade: Long,
    val mistakes: Long,
    val deckId: Long,
    val contextKey: String = "",
    val source: String = "connected",
)

interface ConnectedReviewRepository : ObservableRepository {
    suspend fun getCards(
        itemKeys: Collection<ConnectedItemKey>,
    ): Map<ConnectedItemKey, Map<ReviewDimension, ConnectedReviewCard>>

    suspend fun getDueCards(
        now: Instant,
        dimensions: Collection<ReviewDimension>,
        limit: Long,
    ): List<ConnectedReviewCard>

    suspend fun upsertItem(item: ConnectedReviewItem)

    suspend fun upsertCard(card: ConnectedReviewCard)

    suspend fun recordEvent(event: ConnectedReviewEvent)

    suspend fun commitReview(commit: ConnectedReviewCommit)
}
