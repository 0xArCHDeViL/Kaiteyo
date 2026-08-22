import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import ua.syt0r.kanji.core.connected_learning.ConnectedEntityKey
import ua.syt0r.kanji.core.connected_learning.ConnectedItemKey
import ua.syt0r.kanji.core.connected_learning.ReviewDimension
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardParams
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus
import ua.syt0r.kanji.core.user_data.database.ConnectedReviewCard
import ua.syt0r.kanji.core.user_data.database.UserDatabaseInfo
import ua.syt0r.kanji.core.user_data.database.ConnectedReviewCommit
import ua.syt0r.kanji.core.user_data.database.ConnectedReviewEvent
import ua.syt0r.kanji.core.user_data.database.ConnectedReviewItem
import ua.syt0r.kanji.core.user_data.database.UserDataDatabaseContract
import ua.syt0r.kanji.core.user_data.database.sqldelight.SqlDelightConnectedReviewRepository
import ua.syt0r.kanji.core.user_data.db.UserDataDatabase
import ua.syt0r.kanji.core.userdata.db.UserDataQueries
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.days

class ConnectedReviewRepositoryTest {

    private val now = LocalDateTime(2026, 1, 1, 12, 0).toInstant(TimeZone.UTC)

    @Test
    fun commitReviewWritesItemCardAndEventAtomically() = runBlocking {
        val manager = InMemoryUserDataManager()
        val repository = SqlDelightConnectedReviewRepository(manager, kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined))
        val commit = sampleCommit()

        repository.commitReview(commit)

        manager.database.userDataQueries.getConnectedReviewItem(commit.item.itemKey.value).executeAsOne()
        val cards = repository.getCards(listOf(commit.item.itemKey))
        val events = manager.database.userDataQueries
            .getConnectedReviewEventsForItems(listOf(commit.item.itemKey.value))
            .executeAsList()

        assertEquals(commit.item.itemKey.value, cards.keys.single().value)
        assertEquals(commit.card.dimension, cards.getValue(commit.item.itemKey).keys.single())
        assertEquals(1, events.size)
        assertEquals(commit.event.taskKind, events.single().task_kind)
    }

    @Test
    fun mismatchedConnectedCommitIsRejectedWithoutPartialWrite() = runBlocking {
        val manager = InMemoryUserDataManager()
        val repository = SqlDelightConnectedReviewRepository(manager, kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined))
        val commit = sampleCommit()
        val mismatchedCard = commit.card.copy(
            itemKey = ConnectedItemKey.from(ConnectedEntityKey.Kanji("学"))
        )

        assertFailsWith<IllegalArgumentException> {
            repository.commitReview(commit.copy(card = mismatchedCard))
        }

        assertEquals(0, manager.database.userDataQueries
            .getConnectedReviewEventsForItems(listOf(commit.item.itemKey.value))
            .executeAsList()
            .size)
        assertEquals(0, manager.database.userDataQueries
            .getConnectedFsrsCardsForItems(listOf(commit.item.itemKey.value))
            .executeAsList()
            .size)
        assertFailsWith<Exception> {
            manager.database.userDataQueries.getConnectedReviewItem(commit.item.itemKey.value).executeAsOne()
        }
    }

    private fun sampleCommit(): ConnectedReviewCommit {
        val itemKey = ConnectedItemKey.from(ConnectedEntityKey.Kanji("学"))
        val card = ConnectedReviewCard(
            itemKey = itemKey,
            dimension = ReviewDimension.KANJI_MEANING,
            card = FsrsCard(
                status = FsrsCardStatus.Review,
                params = FsrsCardParams.Existing(
                    difficulty = 5.0,
                    stability = 10.0,
                    reviewTime = now,
                ),
                interval = 2.days,
                lapses = 0,
                repeats = 1,
            ),
            dueAt = now.plus(2.days),
            firstSeen = now,
            suspended = false,
        )
        return ConnectedReviewCommit(
            item = ConnectedReviewItem(
                itemKey = itemKey,
                entityKind = "kanji",
                entityId = "学",
                createdAt = now,
            ),
            card = card,
            event = ConnectedReviewEvent(
                itemKey = itemKey,
                dimension = card.dimension,
                taskKind = "meaning",
                timestamp = now,
                durationMs = 750,
                grade = 3,
                mistakes = 0,
                deckId = 1,
            ),
        )
    }

    private class InMemoryUserDataManager : UserDataDatabaseContract.Manager {
        private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val database = UserDataDatabase(driver)

        override val databaseChangeEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 8)

        init {
            UserDataDatabase.Schema.create(driver)
        }

        override suspend fun <T> readTransaction(block: UserDataQueries.() -> T): T {
            val queries = database.userDataQueries
            return queries.transactionWithResult { block(queries) }
        }

        override suspend fun <T> writeTransaction(block: UserDataQueries.() -> T): T {
            val queries = database.userDataQueries
            return queries.transactionWithResult { block(queries) }
                .also { databaseChangeEvents.tryEmit(Unit) }
        }

        override suspend fun withDisconnectedDatabase(scope: suspend (UserDatabaseInfo) -> Unit) =
            error("Not supported by in-memory test manager")

        override suspend fun replaceDatabase(byteReadChannel: ByteReadChannel) =
            error("Not supported by in-memory test manager")
    }
}

private val Instant.millis get() = toEpochMilliseconds()
private fun Instant.plus(duration: kotlin.time.Duration): Instant =
    Instant.fromEpochMilliseconds(millis + duration.inWholeMilliseconds)
