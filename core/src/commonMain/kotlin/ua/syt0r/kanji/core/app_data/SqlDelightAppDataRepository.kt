package ua.syt0r.kanji.core.app_data

import kotlinx.coroutines.Deferred
import kotlin.random.Random
import ua.syt0r.kanji.core.app_data.data.CharacterRadical
import ua.syt0r.kanji.core.app_data.data.DetailedJapaneseWord
import ua.syt0r.kanji.core.app_data.data.DetailedVocabReading
import ua.syt0r.kanji.core.app_data.data.DetailedVocabSense
import ua.syt0r.kanji.core.app_data.data.FuriganaDBEntityCreator
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.FuriganaStringCompound
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.KanjiCatalogEntry
import ua.syt0r.kanji.core.app_data.data.KanjiData
import ua.syt0r.kanji.core.app_data.data.RadicalData
import ua.syt0r.kanji.core.app_data.data.ReadingType
import ua.syt0r.kanji.core.app_data.data.VocabReading
import ua.syt0r.kanji.core.app_data.data.VocabReadingInfo
import ua.syt0r.kanji.core.japanese.isKana
import ua.syt0r.kanji.core.japanese.isKanji
import ua.syt0r.kanji.core.japanese.kanaToRomaji
import ua.syt0r.kanji.core.app_data.db.AppDataDatabase
import ua.syt0r.kanji.core.appdata.db.GetVocabKanaElementsForEntries
import ua.syt0r.kanji.core.appdata.db.GetVocabKanjiElementsForEntries
import ua.syt0r.kanji.core.appdata.db.GetVocabReadingsWithText
import ua.syt0r.kanji.core.appdata.db.GetVocabSearchEntries
import ua.syt0r.kanji.core.appdata.db.GetVocabSensesWithDetails
import ua.syt0r.kanji.core.appdata.db.LettersQueries
import ua.syt0r.kanji.core.appdata.db.VocabQueries
import ua.syt0r.kanji.core.logger.Logger

class SqlDelightAppDataRepository(
    private val deferredDatabase: Deferred<AppDataDatabase>
) : AppDataRepository {

    private data class SearchReadingCandidate(
        val entryId: Long,
        val elementId: Long,
        val reading: String,
        val priority: Long?,
        val isKana: Long
    )

    private suspend fun <T> lettersQuery(
        queryScope: LettersQueries.() -> T
    ): T {
        return queryScope(deferredDatabase.await().lettersQueries)
    }

    private suspend fun <T> vocabQuery(
        queryScope: VocabQueries.() -> T
    ): T {
        return queryScope(deferredDatabase.await().vocabQueries)
    }

    override suspend fun getStrokes(character: String): List<String> = lettersQuery {
        getStrokes(character).executeAsList()
    }

    override suspend fun getRadicalsInCharacter(
        character: String
    ): List<CharacterRadical> = lettersQuery {
        getCharacterRadicals(character).executeAsList().map {
            it.run {
                CharacterRadical(
                    character = character,
                    radical = radical,
                    startPosition = start_stroke.toInt(),
                    strokesCount = strokes_count.toInt()
                )
            }
        }
    }

    override suspend fun getRadicalsInCharacters(
        characters: List<String>
    ): Map<String, List<CharacterRadical>> = lettersQuery {
        getCharacterRadicalsForCharacters(characters)
            .executeAsList()
            .groupBy { it.kanji }
            .mapValues { (character, rows) ->
                rows.map {
                    CharacterRadical(
                        character = character,
                        radical = it.radical,
                        startPosition = it.start_stroke.toInt(),
                        strokesCount = it.strokes_count.toInt()
                    )
                }
            }
    }

    override suspend fun getMeanings(kanji: String): List<String> = lettersQuery {
        getKanjiMeanings(kanji).executeAsList()
    }

    override suspend fun getReadings(
        kanji: String
    ): Map<String, ReadingType> = lettersQuery {
        getKanjiReadings(kanji).executeAsList().associate { readingData ->
            readingData.reading to ReadingType.entries
                .find { it.value == readingData.reading_type }!!
        }
    }

    override suspend fun getClassificationsForKanji(kanji: String): List<String> = lettersQuery {
        getClassificationsForKanji(kanji).executeAsList()
    }

    override suspend fun getKanjiForClassification(
        classification: String
    ): List<String> = lettersQuery {
        getKanjiWithClassification(classification).executeAsList()
    }

    override suspend fun getCharacterReadingsOfLength(
        length: Int, limit: Int
    ): List<String> = vocabQuery {
        getVocabKanaReadingsOfLength(
            length = length.toLong(),
            seed = Random.nextLong(Int.MAX_VALUE.toLong()),
            limit = limit.toLong()
        ).executeAsList()
    }

    override suspend fun getData(kanji: String): KanjiData? = lettersQuery {
        getKanjiData(kanji).executeAsOneOrNull()?.run {
            KanjiData(
                kanji = kanji, frequency = frequency?.toInt(), variantFamily = variantFamily
            )
        }
    }

    override suspend fun getRadicals(): List<RadicalData> = lettersQuery {
        getRadicals().executeAsList().map { RadicalData(it.radical, it.strokesCount.toInt()) }
    }

    override suspend fun getKanjiCatalog(): List<KanjiCatalogEntry> = lettersQuery {
        getKanjiCatalog(DELIMITER).executeAsList().map { row ->
            KanjiCatalogEntry(
                kanji = row.kanji,
                frequency = row.frequency?.toInt(),
                meanings = row.meanings.splitValues(),
                onReadings = row.on_readings.splitValues(),
                classifications = row.classifications.splitValues(),
                strokeCount = row.stroke_count.toInt(),
                readings = row.all_readings.splitValues()
            )
        }
    }

    override suspend fun getAllKanji(): List<ua.syt0r.kanji.core.app_data.data.KanjiListEntry> = lettersQuery {
        getAllKanji().executeAsList().map {
            ua.syt0r.kanji.core.app_data.data.KanjiListEntry(
                kanji = it.kanji,
                frequency = it.frequency?.toInt()
            )
        }
    }

    override suspend fun getAllKanjiMeanings(): List<ua.syt0r.kanji.core.app_data.data.KanjiMeaningEntry> = lettersQuery {
        getAllKanjiMeanings().executeAsList().map {
            ua.syt0r.kanji.core.app_data.data.KanjiMeaningEntry(
                kanji = it.kanji,
                meaning = it.meaning
            )
        }
    }

    override suspend fun getAllKanjiReadings(): List<ua.syt0r.kanji.core.app_data.data.KanjiReadingEntry> = lettersQuery {
        getAllKanjiReadings().executeAsList().map {
            ua.syt0r.kanji.core.app_data.data.KanjiReadingEntry(
                kanji = it.kanji,
                readingType = it.reading_type,
                reading = it.reading
            )
        }
    }

    override suspend fun getAllClassifications(): List<ua.syt0r.kanji.core.app_data.data.KanjiClassificationEntry> = lettersQuery {
        getAllClassifications().executeAsList().map {
            ua.syt0r.kanji.core.app_data.data.KanjiClassificationEntry(
                kanji = it.kanji,
                classification = it.class_
            )
        }
    }

    override suspend fun getKanjiStrokeCounts(): Map<String, Int> = lettersQuery {
        getKanjiStrokeCounts().executeAsList().associate {
            it.character to it.stroke_count.toInt()
        }
    }

    override suspend fun getCharactersWithRadicals(
        radicals: List<String>
    ): List<String> = lettersQuery {
        getCharsWithRadicals(radicals, radicals.size.toLong()).executeAsList()
    }

    override suspend fun getAllRadicalsInCharactersWithSelectedRadicals(
        radicals: Set<String>
    ): List<String> = lettersQuery {
        getAllRadicalsInCharactersWithSelectedRadicals(
            radicals,
            radicals.size.toLong()
        ).executeAsList()
    }


    override suspend fun getWordsWithTextCount(text: String): Int = vocabQuery {
        getCountOfVocabReadingsWithText(text = text, includeKanjiReadings = true).executeAsOne()
            .toInt()
    }

    override suspend fun getWordsWithText(
        text: String, offset: Int, limit: Int
    ): List<JapaneseWord> = vocabQuery {
        val readingRows = getVocabReadingsWithText(
            text = text,
            includeKanjiReadings = true,
            offset = offset.toLong(),
            limit = limit.toLong()
        ).executeAsList()
        if (readingRows.isEmpty()) return@vocabQuery emptyList()

        val entryIds = readingRows.map { it.entry_id }.distinct()
        val senseRows = entryIds
            .asSequence()
            .chunked(100)
            .flatMap { ids -> getVocabSensesWithDetails(ids, DELIMITER).executeAsList().asSequence() }
            .toList()
        val sensesByEntry = senseRows.groupBy { it.entry_id }
        val kanjiByEntry = getVocabKanjiElementsForEntries(DELIMITER, entryIds)
            .executeAsList()
            .groupBy { it.entry_id }
        val kanaByEntry = getVocabKanaElementsForEntries(DELIMITER, entryIds)
            .executeAsList()
            .groupBy { it.entry_id }

        val kanjiReadings = kanjiByEntry.values.flatten().map { it.reading }.distinct()
        val kanaReadings = kanaByEntry.values.flatten().map { it.reading }.distinct()
        val furiganaByPair = if (kanjiReadings.isNotEmpty() && kanaReadings.isNotEmpty()) {
            getFuriganaForWord(kanjiReadings, kanaReadings).executeAsList().associateBy(
                { Pair(it.text, it.reading) },
                { it.furigana }
            )
        } else {
            emptyMap()
        }

        readingRows.mapNotNull { row ->
            val kanjiRows = kanjiByEntry[row.entry_id].orEmpty()
            val kanaRows = kanaByEntry[row.entry_id].orEmpty()
            val selectedKanji = if (row.isKana == 0L) {
                kanjiRows.firstOrNull { it.element_id == row.element_id }
            } else {
                null
            }
            val selectedKana = if (row.isKana == 1L) {
                kanaRows.firstOrNull { it.element_id == row.element_id }
            } else {
                val selectedKanjiReading = selectedKanji?.reading
                kanaRows.firstOrNull { kana ->
                    val restrictions = kana.restricted_kanji.splitValues()
                    restrictions.isEmpty() || selectedKanjiReading == null || selectedKanjiReading in restrictions
                } ?: kanaRows.firstOrNull()
            }
            val kanji = selectedKanji?.reading
            val kana = selectedKana?.reading ?: kanaRows.firstOrNull()?.reading ?: row.reading
            val sense = sensesByEntry[row.entry_id]
                ?.firstOrNull { senseRow ->
                    val kanjiRestrictions = senseRow.kanji_restrictions.orEmpty().splitValues()
                    val kanaRestrictions = senseRow.kana_restrictions.orEmpty().splitValues()
                    (kanjiRestrictions.isEmpty() || kanji != null && kanji in kanjiRestrictions) &&
                            (kanaRestrictions.isEmpty() || kana in kanaRestrictions)
                }
                ?: return@mapNotNull null
            JapaneseWord(
                id = row.entry_id,
                reading = VocabReading(
                    kanjiReading = kanji,
                    kanaReading = kana,
                    furigana = if (kanji != null && kana != null) {
                        furiganaByPair[Pair(kanji, kana)]?.parseAsFurigana()
                    } else {
                        null
                    }
                ),
                glossary = sense.glosses?.split(DELIMITER)?.filter(String::isNotEmpty) ?: emptyList(),
                partOfSpeechList = sense.explanations?.split(DELIMITER)?.filter(String::isNotEmpty) ?: emptyList()
            )
        }
    }

    override suspend fun searchWords(
        query: SearchQuery,
        offset: Int,
        limit: Int
    ): SearchResult {
        if (query.scope == SearchScope.Kanji) return searchKanji(query)
        if (query.scope == SearchScope.Components) return searchComponents(query)
        if (query.scope == SearchScope.Names) return searchNames(query, offset, limit)

        return vocabQuery {
            val fields = listOf("kanji", "kana", "romaji", "gloss")
            val preferredElementsByEntry = query.terms
                .asSequence()
                .filter { term -> term.value.any { character -> character.isKana() || character.isKanji() } }
                .flatMap { term ->
                    term.toGlobPatterns().asSequence().flatMap { pattern ->
                        searchVocabElementsByReading(pattern).executeAsList().asSequence()
                            .map { row ->
                                SearchReadingCandidate(
                                    entryId = row.entry_id,
                                    elementId = row.element_id,
                                    reading = row.reading,
                                    priority = row.priority,
                                    isKana = row.isKana
                                )
                            }
                    }
                }
                .groupBy { it.entryId }
            val positiveTags = query.tags.filterNot(SearchTag::negated)
            val negativeTags = query.tags.filter(SearchTag::negated)
            val positiveTagIds = positiveTags.associateWith { tag -> getVocabEntryIdsForTag(tag) }
            val negativeTagIds = negativeTags.associateWith { tag -> getVocabEntryIdsForTag(tag) }

            val matchedIds = when {
                query.terms.isNotEmpty() -> query.terms
                    .map { term ->
                        term.toGlobPatterns().asSequence()
                            .flatMap { pattern ->
                                searchVocabEntryIds(fields, pattern).executeAsList().asSequence()
                            }
                            .toSet()
                    }
                    .reduceOrNull { left, right -> left intersect right }
                    ?.toMutableSet()
                    ?: mutableSetOf()

                positiveTagIds.isNotEmpty() -> positiveTagIds.values
                    .reduceOrNull { left, right -> left intersect right }
                    ?.toMutableSet()
                    ?: mutableSetOf()

                else -> getAllVocabEntryIds().executeAsList().toMutableSet()
            }

            if (query.terms.isNotEmpty()) {
                positiveTagIds.values.forEach(matchedIds::retainAll)
            }
            negativeTagIds.values.forEach(matchedIds::removeAll)

            val requestedWindow = offset.toLong().coerceAtLeast(0L) +
                    limit.toLong().coerceAtLeast(0L)
            val chunkLimit = requestedWindow.coerceAtLeast(1L)

            val sortedEntries = matchedIds
                .toList()
                .chunked(SearchEntryIdChunkSize)
                .flatMap { ids ->
                    if (ids.isEmpty()) emptyList()
                    else getVocabSearchEntries(
                        entryIds = ids,
                        limit = chunkLimit,
                        offset = 0
                    ).executeAsList()
                }
                .sortedWith(
                    compareBy<GetVocabSearchEntries> {
                        it.priority == null
                    }.thenBy { it.priority ?: Long.MAX_VALUE }
                        .thenBy { it.element_id }
                        .thenBy { it.entry_id }
                )

            val page = sortedEntries
                .drop(offset.coerceAtLeast(0))
                .take(limit.coerceAtLeast(0))
                .mapNotNull { element ->
                    val preferredElement = preferredElementsByEntry[element.entry_id]
                        ?.minWithOrNull(
                            compareBy<SearchReadingCandidate> {
                                it.priority == null
                            }
                                .thenBy { it.priority ?: Long.MAX_VALUE }
                                .thenBy { it.elementId }
                        )
                    val selectedElement = preferredElement ?: SearchReadingCandidate(
                        entryId = element.entry_id,
                        elementId = element.element_id,
                        reading = element.reading,
                        priority = element.priority,
                        isKana = element.isKana
                    )
                    getWord(
                        id = selectedElement.entryId,
                        kanaReading = selectedElement.reading.takeIf { selectedElement.isKana == 1L },
                        kanjiReading = selectedElement.reading.takeIf { selectedElement.isKana == 0L },
                        elementId = selectedElement.elementId
                    )
                }

            SearchResult(totalCount = matchedIds.size, words = page)
        }
    }

    private fun VocabQueries.getVocabEntryIdsForTag(tag: SearchTag): Set<Long> =
        tag.storageValues()
            .asSequence()
            .flatMap { value -> getVocabEntryIdsWithTag(value).executeAsList().asSequence() }
            .toSet()

    private fun SearchTag.storageValues(): List<String> = when (value) {
        "verb", "verbs" -> VerbSearchTags
        "v5", "godan" -> VerbSearchTags.filter { it.startsWith("v5") }
        "transitive", "transitive-verb", "vt" -> listOf("vt", "transitive verb")
        "intransitive", "intransitive-verb", "vi" -> listOf("vi", "intransitive verb")
        "ichidan", "ichidan-verb", "v1" -> listOf("v1", "ichidan verb")
        "suru", "suru-verb", "vs" -> listOf("vs", "suru verb", "noun or participle which takes the aux. verb suru")
        "kuru", "kuru-verb", "vk" -> listOf("vk", "kuru verb - special class")
        "noun" -> listOf("noun", "noun (common) (futsuumeishi)")
        "adj", "adjective" -> listOf("adj", "adjective (keiyoushi)", "adjectival nouns or quasi-adjectives (keiyodoshi)")
        "adv", "adverb" -> listOf("adv", "adverb (fukushi)", "adverb taking the 'to' particle")
        "expression", "expressions" -> listOf("expression", "expressions (phrases, clauses, etc.)")
        else -> listOf(value)
    }

    private fun String.toSqlLikePattern(): String = buildString(length) {
        for (character in this@toSqlLikePattern) {
            when (character) {
                '*' -> append('%')
                '?' -> append('_')
                '%', '_', '\\' -> append('\\').append(character)
                else -> append(character)
            }
        }
    }

    private suspend fun searchNames(
        query: SearchQuery,
        offset: Int,
        limit: Int
    ): SearchResult = vocabQuery {
        val safeOffset = offset.coerceAtLeast(0).toLong()
        val safeLimit = limit.coerceAtLeast(0).toLong()
        val matchedIds: Set<Long>
        val pageIds: List<Long>
        val totalCount: Int

        if (query.terms.isEmpty()) {
            matchedIds = emptySet()
            totalCount = getAllVocabNameCount().executeAsOne().toInt()
            pageIds = getAllVocabNameIdsPage(
                limit = safeLimit,
                offset = safeOffset
            ).executeAsList()
        } else {
            matchedIds = query.terms
                .map { term ->
                    term.toGlobPatterns().asSequence()
                        .flatMap { pattern ->
                            searchVocabNameIds(pattern.toSqlLikePattern())
                                .executeAsList()
                                .asSequence()
                        }
                        .toSet()
                }
                .reduceOrNull { left, right -> left intersect right }
                ?: emptySet()
            totalCount = matchedIds.size
            pageIds = matchedIds
                .asSequence()
                .sorted()
                .drop(safeOffset.toInt())
                .take(safeLimit.toInt())
                .toList()
        }

        val names = getVocabNamesByIds(pageIds)
            .executeAsList()
            .map {
                JapaneseName(
                    id = it.id,
                    kanji = it.kanji,
                    kana = it.kana,
                    nameType = it.name_type,
                    meaning = it.meaning
                )
            }
        SearchResult(totalCount = totalCount, words = emptyList(), names = names)
    }

    private suspend fun searchKanji(query: SearchQuery): SearchResult {
        val catalog = lettersQuery {
            getKanjiCatalog(DELIMITER).executeAsList()
        }
        val terms = query.terms
        val matched = catalog.filter { row ->
            terms.all { term ->
                val candidates = listOf(
                    row.kanji,
                    row.meanings,
                    row.all_readings
                )
                termMatchesAny(term, candidates)
            }
        }
        return SearchResult(
            totalCount = matched.size,
            words = emptyList(),
            characters = matched.map { it.kanji }
        )
    }

    private suspend fun searchComponents(query: SearchQuery): SearchResult {
        val components = query.terms
            .flatMap { it.value.asSequence() }
            .filterNot { it.isWhitespace() }
            .map(Char::toString)
            .distinct()
            .toList()
        if (components.isEmpty()) return SearchResult(0, emptyList())
        val characters = lettersQuery {
            getCharsWithRadicals(components, components.size.toLong()).executeAsList()
        }
        return SearchResult(
            totalCount = characters.size,
            words = emptyList(),
            characters = characters
        )
    }

    private fun termMatchesAny(term: SearchTerm, candidates: List<String>): Boolean {
        val patterns = term.toGlobPatterns()
        return candidates
            .asSequence()
            .flatMap { candidate ->
                candidate.splitValues()
                    .asSequence()
                    .flatMap { value ->
                        sequenceOf(value, value.kanaToRomaji())
                    }
            }
            .map(String::lowercase)
            .any { candidate -> patterns.any { pattern -> candidate.matchesGlob(pattern) } }
    }

    private fun String.matchesGlob(pattern: String): Boolean {
        val regex = buildString(length + pattern.length) {
            append('^')
            pattern.forEach { character ->
                when (character) {
                    '*' -> append(".*")
                    '?' -> append('.')
                    else -> append(Regex.escape(character.toString()))
                }
            }
            append('$')
        }
        return Regex(regex).matches(this)
    }

    override suspend fun getWordExamples(letter: String): List<JapaneseWord> {
        val entries = lettersQuery { getVocabExamplesForLetter(letter).executeAsList() }

        val wordIdList = entries.map { it.vocab_id }.toSet()
        val vocabSenses = getWordSenses(wordIdList).associateBy { it.wordId }

        return vocabQuery {
            entries.mapNotNull { entry ->
                val sense = vocabSenses[entry.vocab_id]
                    ?.senseList
                    ?.firstOrNull()
                    ?: return@mapNotNull null

                JapaneseWord(
                    id = entry.vocab_id,
                    reading = VocabReading(
                        kanjiReading = entry.kanji,
                        kanaReading = entry.kana,
                        furigana = entry.kanji?.let { searchFurigana(entry.kanji, entry.kana) }
                            ?.executeAsOneOrNull()
                            ?.parseAsFurigana()
                    ),
                    glossary = sense.glossary,
                    partOfSpeechList = emptyList()
                )
            }
        }
    }

    override suspend fun getWord(
        id: Long,
        kanjiReading: String?,
        kanaReading: String
    ): JapaneseWord? = vocabQuery {
        getWord(
            id = id,
            kanaReading = kanaReading,
            kanjiReading = kanjiReading
        )
    }

    override suspend fun findWords(
        id: Long?,
        kanjiReading: String?,
        kanaReading: String?
    ): List<JapaneseWord> = vocabQuery {
        val elements = findVocabElementsByIdOrReading(
            entryId = id ?: -1,
            kanjiReading = kanjiReading ?: "",
            kanaReading = kanaReading ?: ""
        ).executeAsList()

        elements.groupBy { it.entry_id }
            .filter { id == null || id == it.key }
            .mapNotNull { (wordId, elements) ->

                when {
                    kanjiReading != null && kanaReading != null -> {
                        getWord(
                            id = wordId,
                            kanaReading = kanaReading,
                            kanjiReading = kanjiReading
                        )
                    }

                    kanjiReading == null && kanaReading == null -> {
                        val element = elements.first()
                        getWord(
                            id = wordId,
                            kanaReading = element.reading.takeIf { element.isKana == 1L },
                            kanjiReading = element.reading.takeIf { element.isKana == 0L }
                        )
                    }

                    else -> {
                        val element = elements
                            .firstOrNull { it.reading == kanjiReading || it.reading == kanaReading }
                            ?: return@mapNotNull null
                        getWord(
                            id = wordId,
                            kanaReading = element.reading.takeIf { element.isKana == 1L },
                            kanjiReading = element.reading.takeIf { element.isKana == 0L }
                        )
                    }
                }
            }
    }

    override suspend fun getKanaWordsWithTextCount(text: String): Int = vocabQuery {
        getCountOfVocabReadingsWithText(text = text, includeKanjiReadings = false).executeAsOne()
            .toInt()
    }

    override suspend fun getKanaWords(
        char: String,
        limit: Int,
        offset: Int
    ): List<JapaneseWord> = vocabQuery {
        getVocabReadingsWithText(
            text = char,
            includeKanjiReadings = false,
            offset = offset.toLong(),
            limit = limit.toLong()
        )
            .executeAsList()
            .map { getWord(it.entry_id, it.reading, null, it.element_id)!! }
    }

    override suspend fun getSentencesWithTextCount(text: String): Int = vocabQuery {
        runCatching {
            getSentencesWithTextCount(text).executeAsOne().toInt()
        }.getOrElse {
            Logger.e(it.message ?: it.stackTraceToString())
            0
        }
    }

    override suspend fun getSentencesWithText(
        text: String,
        offset: Int,
        limit: Int
    ): List<Sentence> = vocabQuery {
        val rows = runCatching {
            // TODO remove try catch after investigation
            getSentencesWithText(text = text, offset = offset.toLong(), limit = limit.toLong())
                .executeAsList()
        }.getOrElse {
            Logger.e(it.message ?: it.stackTraceToString())
            emptyList()
        }

        rows.map {
            Sentence(
                value = it.sentence,
                translation = it.translation,
                furigana = it.furigana.parseAsFurigana()
            )
        }
    }

    override suspend fun getWordSenses(idList: Set<Long>): List<VocabSenseGroup> = vocabQuery {
        idList.asSequence()
            .chunked(100)
            .flatMap { getVocabSensesWithDetails(it, DELIMITER).executeAsList() }
            .groupBy { it.entry_id }
            .map { (wordId, senseItems) ->
                VocabSenseGroup(
                    wordId = wordId,
                    senseList = senseItems.map {
                        VocabSenseGroup.Sense(
                            glossary = it.glosses!!.split(DELIMITER),
                            kanjiRestrictions = it.kanji_restrictions
                                ?.split(DELIMITER) ?: emptyList(),
                            kanaRestrictions = it.kana_restrictions
                                ?.split(DELIMITER) ?: emptyList()
                        )
                    }
                )
            }
    }

    override suspend fun getDetailedWord(id: Long): DetailedJapaneseWord? = vocabQuery {
        getDetailedWordInternal(id)
    }

    override suspend fun getImportDeckWordsCount(classification: String): Int = vocabQuery {
        getVocabDeckCardsCount(classification).executeAsOne().toInt()
    }

    override suspend fun getImportDeckWords(
        classification: String
    ): List<ImportDeckWord> = vocabQuery {
        getVocabDeckCards(classification)
            .executeAsList()
            .map {
                ImportDeckWord(
                    id = it.jmdict_seq,
                    kanji = it.kanji,
                    kana = it.kana,
                    meaning = it.definition
                )
            }
    }

    private fun VocabQueries.getDetailedWordInternal(id: Long): DetailedJapaneseWord? {
        val senseElements = getVocabSensesWithDetails(listOf(id), DELIMITER).executeAsList()

        val kanjiElements = getVocabKanjiElementsWithDetails(
            delimiter = DELIMITER,
            wordId = id
        ).executeAsList()
        val kanaElements = getVocabKanaElementsWithDetails(
            delimiter = DELIMITER,
            wordId = id
        ).executeAsList()

        if (senseElements.isEmpty() || (kanjiElements.isEmpty() && kanaElements.isEmpty())) {
            Logger.d("Not enough info about jmDictWord[$id]")
            return null
        }

        val kanaElementsWithReadings = kanaElements.associateWith {
            DetailedVocabReading(
                elementId = it.element_id,
                kanji = null,
                kana = it.reading,
                furigana = null,
                info = it.informations.parseAsVocabReadingInfoSet(),
                noKanji = it.no_kanji == 1L
            )
        }

        val kanjiList = kanjiElements.map { it.reading }.distinct()
        val kanaList = kanaElements.map { it.reading }.distinct()

        val furiganaMap = if (kanjiList.isNotEmpty() && kanaList.isNotEmpty()) {
            getFuriganaForWord(kanjiList, kanaList).executeAsList().associateBy(
                { Pair(it.text, it.reading) },
                { it.furigana }
            )
        } else {
            emptyMap()
        }

        val kanjiReadings = kanjiElements.flatMap { kanjiElement ->
            val kanjiReadingInfo = kanjiElement.informations.parseAsVocabReadingInfoSet()

            val matchingKanaReadings = kanaElementsWithReadings.filter { (kanaElement, _) ->
                val restrictedKanji = kanaElement.restricted_kanji
                    ?.split(DELIMITER)
                    ?.filter(String::isNotEmpty)
                    ?: emptyList()
                kanjiReadingInfo.contains(VocabReadingInfo.SearchOnlyKanjiForm) ||
                        restrictedKanji.isEmpty() ||
                        restrictedKanji.contains(kanjiElement.reading)
            }

            matchingKanaReadings.map { (kanaElement, kanaReading) ->
                val kanji = kanjiElement.reading
                val kana = kanaElement.reading
                DetailedVocabReading(
                    elementId = kanjiElement.element_id,
                    kanji = kanji,
                    kana = kana,
                    furigana = furiganaMap[Pair(kanji, kana)]?.parseAsFurigana(),
                    info = kanjiReadingInfo.plus(kanaReading.info),
                    noKanji = kanaReading.noKanji
                )
            }
        }

        val kanaReadings = kanaElementsWithReadings.values

        val allReadings = kanjiReadings.plus(kanaReadings).sortedWith(vocabReadingsComparator)

        val senseList = senseElements.map { senseElement ->
            val senseKanjiRestrictions = senseElement.kanji_restrictions?.split(DELIMITER)
                ?.toSet()
                ?: emptySet()

            val senseKanaRestrictions = senseElement.kana_restrictions?.split(DELIMITER)
                ?.toSet()
                ?: emptySet()

            val filteredReadings = allReadings.filter {
                val matchesKanjiRestrictions = senseKanjiRestrictions.isEmpty() ||
                        senseKanjiRestrictions.contains(it.kanji)

                val matchesKanaRestrictions = senseKanaRestrictions.isEmpty() ||
                        senseKanaRestrictions.contains(it.kana)

                matchesKanjiRestrictions && matchesKanaRestrictions
            }

            DetailedVocabSense(
                glossary = senseElement.glosses?.split(DELIMITER) ?: emptyList(),
                partOfSpeechList = senseElement.explanations?.split(DELIMITER) ?: emptyList(),
                readings = filteredReadings
            )
        }

        return DetailedJapaneseWord(
            id = id,
            senseList = senseList
        )
    }

    private fun VocabQueries.getWord(
        id: Long,
        kanaReading: String?,
        kanjiReading: String?,
        elementId: Long? = null
    ): JapaneseWord? {
        val detailedWord = getDetailedWordInternal(id) ?: return null
        var fallback: JapaneseWord? = null

        fun toJapaneseWord(
            sense: DetailedVocabSense,
            reading: DetailedVocabReading
        ) = JapaneseWord(
            id = id,
            reading = VocabReading(
                kanjiReading = reading.kanji,
                kanaReading = reading.kana,
                furigana = reading.furigana
            ),
            glossary = sense.glossary,
            partOfSpeechList = sense.partOfSpeechList
        )

        for (sense in detailedWord.senseList) {
            for (reading in sense.readings) {
                val matchesKanjiConstraint = kanjiReading == null || reading.kanji == kanjiReading
                val matchesKanaConstraint = kanaReading == null || reading.kana == kanaReading
                if (!matchesKanjiConstraint || !matchesKanaConstraint) continue

                val result = toJapaneseWord(sense, reading)
                if (elementId == null || reading.elementId == elementId) return result
                if (fallback == null) fallback = result
            }
        }

        if (fallback != null) {
            Logger.d("Using reading fallback for element[$elementId], id[$id]")
            return fallback
        }
        Logger.d("Word not found, id[$id], kanaReading[$kanaReading], kanjiReading[$kanjiReading]")
        return null
    }

    private fun String.splitValues(): List<String> =
        if (isEmpty()) emptyList() else split(DELIMITER)

    private fun String.parseAsFurigana(): FuriganaString = FuriganaDBEntityCreator
        .fromJsonString(this)
        .map { FuriganaStringCompound(it.text, it.annotation) }
        .let { FuriganaString(it) }

    private fun String?.parseAsVocabReadingInfoSet(): Set<VocabReadingInfo> {
        return this
            ?.split(DELIMITER)
            ?.asSequence()
            ?.filter(String::isNotEmpty)
            ?.mapNotNull { jmDictInfoValue ->
                VocabReadingInfo.fromJmDictValue(jmDictInfoValue)
            }
            ?.toSet()
            ?: emptySet()
    }

    companion object {

        private const val DELIMITER = "|||"

        private const val SearchEntryIdChunkSize = 500

        private val VerbSearchTags = listOf(
            "vt", "vi", "v1", "v5r", "v5s", "v5k", "v5m", "v5u", "v5t", "v5g", "v5b",
            "v5k-s", "v5aru", "vk", "vs"
        )

        private val readingInfoSetWithLowerPriority = setOf(
            VocabReadingInfo.IrregularKanaUsage,
            VocabReadingInfo.IrregularKanjiUsage,
            VocabReadingInfo.OutdatedKana,
            VocabReadingInfo.OutdatedKanji,
            VocabReadingInfo.SearchOnlyKanaForm,
            VocabReadingInfo.SearchOnlyKanjiForm,
            VocabReadingInfo.RarelyUsedKanjiForm,
            VocabReadingInfo.RarelyUsedKanaForm
        )

        // asc order -> false - 0, true - 1
        private val vocabReadingsComparator = compareBy<DetailedVocabReading>(
            { it.noKanji },
            { it.kanji == null || it.info.intersect(readingInfoSetWithLowerPriority).isNotEmpty() },
            { it.elementId }
        )

    }

}
