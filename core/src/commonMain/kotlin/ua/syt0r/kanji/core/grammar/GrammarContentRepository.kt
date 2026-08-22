package ua.syt0r.kanji.core.grammar

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ua.syt0r.kanji.Res

interface GrammarContentRepository {
    suspend fun chapters(): List<GrammarChapter>
    suspend fun findPoint(deckId: Long, pointNumber: String): GrammarPoint?
    suspend fun findChapter(deckId: Long): GrammarChapter?
}

class DefaultGrammarContentRepository : GrammarContentRepository {

    private val mutex = Mutex()
    private var cachedChapters: List<GrammarChapter>? = null

    override suspend fun chapters(): List<GrammarChapter> = mutex.withLock {
        cachedChapters ?: load().also { cachedChapters = it }
    }

    override suspend fun findPoint(deckId: Long, pointNumber: String): GrammarPoint? =
        chapters().firstOrNull { it.id.toLong() == deckId }
            ?.points
            ?.firstOrNull { it.number == pointNumber }

    override suspend fun findChapter(deckId: Long): GrammarChapter? =
        chapters().firstOrNull { it.id.toLong() == deckId }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun load(): List<GrammarChapter> {
        val bytes = Res.readBytes("files/bunpou_data.json")
        return Json.decodeFromString(bytes.decodeToString())
    }
}
