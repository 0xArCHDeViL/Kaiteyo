package ua.syt0r.kanji.core.srs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryRepository
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.presentation.screen.main.screen.library.screen.grammar.GrammarChapter
import ua.syt0r.kanji.presentation.screen.main.screen.library.screen.grammar.GrammarPoint
import kotlinx.serialization.json.Json
import ua.syt0r.kanji.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

interface GrammarSrsManager {
    val dataChangeFlow: SharedFlow<Unit>
    suspend fun getDecks(): GrammarSrsDecksData
    suspend fun getDeck(deckId: Long): GrammarSrsDeck
    suspend fun updateGrammarData(chapters: List<GrammarChapter>)
}

typealias GrammarSrsDecksData = SrsDecksData<GrammarSrsDeck, GrammarPracticeType>
typealias GrammarSrsDeckDescriptor = SrsDeckDescriptor<String, GrammarPracticeType>
typealias GrammarSrsDeckProgress = SrsDeckProgress<String>

data class GrammarSrsDeck(
    override val id: Long,
    override val title: String,
    override val position: Int,
    override val items: List<String>,
    override val lastReview: Instant?,
    override val progressMap: Map<GrammarPracticeType, GrammarSrsDeckProgress>,
    val grammarPoints: List<GrammarPoint> // specific to Grammar
) : SrsDeckData<GrammarPracticeType, String>

class DefaultGrammarSrsManager(
    private val srsCardRepository: SrsCardRepository,
    dailyLimitManager: DailyLimitManager,
    timeUtils: TimeUtils,
    private val appPreferences: PreferencesContract.AppPreferences,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    coroutineScope: CoroutineScope
) : SrsManager<String, GrammarPracticeType, GrammarSrsDeck>(
    deckChangesFlow = MutableSharedFlow(), // Triggered when chapters change, usually static
    srsChangesFlow = srsCardRepository.changesFlow,
    dailyLimitManager = dailyLimitManager,
    timeUtils = timeUtils,
    appPreferences = appPreferences,
    coroutineScope = coroutineScope
), GrammarSrsManager {

    override val practiceTypes: List<GrammarPracticeType> = GrammarPracticeType.entries

    private var _grammarChapters: List<GrammarChapter>? = null

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun getGrammarChapters(): List<GrammarChapter> {
        if (_grammarChapters == null) {
            val bytes = ua.syt0r.kanji.Res.readBytes("files/bunpou_data.json")
            val jsonString = bytes.decodeToString()
            _grammarChapters = Json.decodeFromString<List<GrammarChapter>>(jsonString)
        }
        return _grammarChapters!!
    }

    override suspend fun updateGrammarData(chapters: List<GrammarChapter>) {
        this._grammarChapters = chapters
    }

    override suspend fun getDecks(): GrammarSrsDecksData {
        return getDecksInternal()
    }

    override suspend fun getDeck(deckId: Long): GrammarSrsDeck {
        return getDecksInternal().decks.first { it.id == deckId }
    }

    override suspend fun getDeckDescriptors(): List<GrammarSrsDeckDescriptor> {
        return getGrammarChapters().map { getDeckDescriptor(it) }
    }

    private suspend fun getDeckDescriptor(chapter: GrammarChapter): GrammarSrsDeckDescriptor {
        val items = chapter.points.map { it.number } // Use number (e.g. 【０１】) as key
        val historyStats = reviewHistoryRepository
            .getReviewHistoryStatsForKeys(keys = items)

        val itemsDataMap = GrammarPracticeType.entries.associateWith { practiceType ->
            val itemsData: Map<String, SrsCardData> = items.associateWith { numberKey ->
                val key = practiceType.toSrsKey(numberKey)
                val card = srsCardRepository.get(key)
                val itemReviewStats = historyStats[numberKey]
                val practiceTypeStat = itemReviewStats?.practiceTypeToDataMap
                    ?.get(practiceType.srsPracticeType.value)
                SrsCardData(
                    key = key,
                    card = card,
                    status = getSrsStatus(card),
                    lapses = card?.fsrsCard?.lapses ?: 0,
                    repeats = card?.fsrsCard?.repeats ?: 0,
                    firstReview = practiceTypeStat?.firstReview,
                    firstReviewSrsDate = practiceTypeStat?.firstReview?.toSrsDate(),
                    lastReview = card?.lastReview,
                    lastReviewSrsDate = card?.lastReview?.toSrsDate(),
                    expectedReviewDate = card?.expectedReview?.toSrsDate()
                )
            }
            PracticeTypeDeckData(itemsData = itemsData)
        }

        return GrammarSrsDeckDescriptor(
            id = chapter.id.toLong(),
            title = chapter.title,
            position = chapter.id, // Just order by id
            lastReview = historyStats
                .flatMap { it.value.practiceTypeToDataMap.values }
                .maxOfOrNull { it.lastReview },
            items = items,
            itemsData = itemsDataMap
        )
    }

    override suspend fun getDeckSortConfiguration(): DeckSortConfiguration {
        return DeckSortConfiguration(
            sortByReviewDate = false // Just sort naturally for grammar chapters
        )
    }

    override suspend fun getDeckLimit(
        configuration: DailyLimitConfiguration,
        newDoneToday: Int,
        dueDoneToday: Int
    ): DeckLimit.EnabledDeckLimit {
        return when {
            configuration.isGrammarLimitCombined -> DeckLimit.Combined(
                limit = configuration.grammarCombinedLimit,
                newDone = newDoneToday,
                dueDone = dueDoneToday
            )

            else -> DeckLimit.Separate(
                limitsMap = configuration.grammarSeparatedLimit
            )
        }
    }

    override suspend fun createDeck(
        deckDescriptor: GrammarSrsDeckDescriptor,
        deckLimit: DeckLimit,
        currentSrsDate: LocalDate
    ): GrammarSrsDeck {
        val chapter = getGrammarChapters().first { it.id.toLong() == deckDescriptor.id }
        return GrammarSrsDeck(
            id = deckDescriptor.id,
            title = deckDescriptor.title,
            position = deckDescriptor.position,
            lastReview = deckDescriptor.lastReview,
            items = deckDescriptor.items,
            progressMap = deckDescriptor.itemsData.mapValues { (practiceType, deckData) ->
                deckData.toProgress(deckLimit, practiceType, currentSrsDate)
            },
            grammarPoints = chapter.points
        )
    }

}
