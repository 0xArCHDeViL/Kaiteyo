import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.JsonPrimitive
import ua.syt0r.kanji.core.srs.SrsCardKey
import ua.syt0r.kanji.core.srs.SrsMicroMlEngine
import ua.syt0r.kanji.core.srs.SrsScheduler
import ua.syt0r.kanji.core.srs.VocabPracticeType
import ua.syt0r.kanji.core.srs.DefaultSrsScheduler
import ua.syt0r.kanji.core.srs.fsrs.DefaultFsrsScheduler
import ua.syt0r.kanji.core.srs.fsrs.Fsrs5
import ua.syt0r.kanji.core.srs.fsrs.FsrsReviewRating
import ua.syt0r.kanji.core.suspended_property.SuspendedProperty
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class SrsMicroMlEngineTest {

    private val now: Instant = LocalDateTime(2026, 1, 1, 12, 0)
        .toInstant(TimeZone.UTC)

    @Test
    fun coldStartMatchesPureFsrs() = runBlocking {
        val storage = InMemoryStringProperty()
        val engine = SrsMicroMlEngine(storage, CoroutineScope(Dispatchers.Unconfined))
        val scheduler = DefaultSrsScheduler(DefaultFsrsScheduler(Fsrs5()))
        val key = SrsCardKey("cold-start", VocabPracticeType.Flashcard.srsPracticeType.value)
        val card = scheduler.newCard()

        val expected = scheduler.answers(card, now)
        val actual = engine.schedule(key, card, scheduler, now)

        assertEquals(expected, actual)
    }

    @Test
    fun learningStepsRemainUntouched() = runBlocking {
        val storage = InMemoryStringProperty()
        val engine = SrsMicroMlEngine(storage, CoroutineScope(Dispatchers.Unconfined))
        val scheduler = DefaultSrsScheduler(DefaultFsrsScheduler(Fsrs5()))
        val key = SrsCardKey("learning", VocabPracticeType.Flashcard.srsPracticeType.value)
        val newCard = scheduler.newCard()
        val answers = engine.schedule(key, newCard, scheduler, now)

        assertEquals(1.minutes, answers.again.card.interval)
        assertEquals(5.minutes, answers.hard.card.interval)
        assertEquals(10.minutes, answers.good.card.interval)
    }

    @Test
    fun hotPathRemainsLight() = runBlocking {
        val storage = InMemoryStringProperty()
        val engine = SrsMicroMlEngine(storage, CoroutineScope(Dispatchers.Unconfined))
        val scheduler = DefaultSrsScheduler(DefaultFsrsScheduler(Fsrs5()))
        val key = SrsCardKey("benchmark", VocabPracticeType.Flashcard.srsPracticeType.value)
        val card = scheduler.newCard()

        val elapsed = measureTime {
            repeat(1_000) {
                engine.schedule(key, card, scheduler, now)
            }
        }
        println("srs_micro_ml_1000_schedules=$elapsed")
        assertTrue(elapsed < 2.seconds)
    }

    @Test
    fun trainedProfilePreservesOrderingAndFiniteIntervals() = runBlocking {
        val storage = InMemoryStringProperty()
        val engine = SrsMicroMlEngine(storage, CoroutineScope(Dispatchers.Unconfined))
        val scheduler = DefaultSrsScheduler(DefaultFsrsScheduler(Fsrs5()))
        val key = SrsCardKey("trained", VocabPracticeType.Flashcard.srsPracticeType.value)
        var card = scheduler.answers(scheduler.newCard(), now).easy.card
        var reviewTime = now + card.interval

        repeat(64) { index ->
            val answers = engine.schedule(key, card, scheduler, reviewTime)
            val selected = if (index % 7 == 0) answers.hard else answers.good
            engine.observe(
                key = key,
                review = ReviewHistoryItem(
                    key = key.itemKey,
                    practiceType = key.practiceType,
                    timestamp = reviewTime,
                    duration = 4.minutes,
                    grade = selected.grade,
                    mistakes = if (selected.grade == FsrsReviewRating.Hard.grade) 1 else 0,
                    deckId = 1L,
                ),
            )
            card = selected.card
            reviewTime += maxOf(card.interval, 1.days)
        }

        val answers = engine.schedule(key, card, scheduler, reviewTime)
        assertTrue(answers.again.card.interval <= answers.hard.card.interval)
        assertTrue(answers.hard.card.interval <= answers.good.card.interval)
        assertTrue(answers.good.card.interval <= answers.easy.card.interval)
        assertTrue(answers.easy.card.interval.isFinite())
        assertTrue(storage.value.isNotBlank())
    }

    private class InMemoryStringProperty : SuspendedProperty<String> {
        override val key: String = "test"
        override val onModified = MutableSharedFlow<String>(extraBufferCapacity = 1)
        var value: String = ""

        override suspend fun get(): String = value

        override suspend fun set(value: String) {
            this.value = value
            onModified.tryEmit(value)
        }

        override suspend fun isModified(): Boolean = false

        override suspend fun backup(): JsonPrimitive? = null

        override suspend fun restore(value: JsonPrimitive) = Unit
    }
}
