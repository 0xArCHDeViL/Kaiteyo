package export.db

private const val ComponentKind = "COMPONENT"
private const val KanjiKind = "KANJI"
private const val ReadingKind = "READING"
private const val VocabularyKind = "VOCABULARY_ELEMENT"
private const val SenseKind = "SENSE"
private const val SentenceKind = "SENTENCE"

private const val ComposedOfEdge = "COMPOSED_OF"
private const val HasReadingEdge = "HAS_READING"
private const val HasVocabularyEdge = "HAS_VOCABULARY"
private const val HasSenseEdge = "HAS_SENSE"
private const val AppearsInSentenceEdge = "APPEARS_IN_SENTENCE"
private const val DerivedProvenance = "DERIVED_AT_EXPORT"

fun buildLearningGraph(
    radicals: List<DatabaseRadical>,
    kanjiData: List<DatabaseKanjiData>,
    kanjiRadicals: List<DatabaseKanjiRadical>,
    vocabulary: DatabaseVocabData,
    sentences: List<Sentence>,
): Pair<List<DatabaseLearningNode>, List<DatabaseLearningEdge>> {
    val nodes = LinkedHashMap<String, DatabaseLearningNode>()
    val edges = LinkedHashSet<DatabaseLearningEdge>()
    val kanjiSet = kanjiData.mapTo(HashSet()) { it.kanji }
    val kanjiElementsByEntry = vocabulary.kanjiElements.groupBy { it.entry_id }
    val kanaElementsByEntry = vocabulary.kanaElements.groupBy { it.entry_id }
    val senseEntryById = vocabulary.senses.associate { it.id to it.entry_id }

    fun addNode(node: DatabaseLearningNode) {
        nodes.putIfAbsent(node.nodeKey, node)
    }

    fun addEdge(edge: DatabaseLearningEdge) {
        if (nodes.containsKey(edge.fromNodeKey) && nodes.containsKey(edge.toNodeKey)) {
            edges += edge
        }
    }

    radicals.forEach { radical ->
        addNode(
            DatabaseLearningNode(
                nodeKey = "component:${radical.radical}",
                nodeKind = ComponentKind,
                kanji = radical.radical,
                priority = 1.0 / (1.0 + radical.strokes.toDouble()),
            )
        )
    }

    kanjiData.forEach { kanji ->
        addNode(
            DatabaseLearningNode(
                nodeKey = "kanji:${kanji.kanji}",
                nodeKind = KanjiKind,
                kanji = kanji.kanji,
                priority = kanji.frequency?.let { 1_000_000.0 - it.toDouble() } ?: 0.0,
            )
        )

        (kanji.onReadings.map { "on" to it } + kanji.kunReadings.map { "kun" to it })
            .distinct()
            .forEach { (readingType, reading) ->
                addNode(
                    DatabaseLearningNode(
                        nodeKey = "reading:${kanji.kanji}|$readingType|$reading",
                        nodeKind = ReadingKind,
                        kanji = kanji.kanji,
                        reading = reading,
                    )
                )
                addEdge(
                    DatabaseLearningEdge(
                        fromNodeKey = "kanji:${kanji.kanji}",
                        toNodeKey = "reading:${kanji.kanji}|$readingType|$reading",
                        edgeKind = HasReadingEdge,
                        weight = 1.0,
                        provenance = DerivedProvenance,
                    )
                )
            }
    }

    kanjiRadicals.forEach { relation ->
        val kanjiKey = "kanji:${relation.kanji}"
        val componentKey = "component:${relation.radical}"
        if (kanjiSet.contains(relation.kanji)) {
            addEdge(
                DatabaseLearningEdge(
                    fromNodeKey = kanjiKey,
                    toNodeKey = componentKey,
                    edgeKind = ComposedOfEdge,
                    weight = 1.0 / (1.0 + relation.startPosition.toDouble()),
                    provenance = DerivedProvenance,
                )
            )
        }
    }

    vocabulary.kanjiElements.forEach { element ->
        val nodeKey = vocabularyNodeKey(element.entry_id, element.element_id)
        addNode(
            DatabaseLearningNode(
                nodeKey = nodeKey,
                nodeKind = VocabularyKind,
                reading = element.reading,
                entryId = element.entry_id,
                elementId = element.element_id,
                priority = element.priority?.let { 1.0 / (1.0 + it.toDouble()) } ?: 0.0,
            )
        )
        element.reading.forEach { character ->
            if (kanjiSet.contains(character.toString())) {
                addEdge(
                    DatabaseLearningEdge(
                        fromNodeKey = "kanji:$character",
                        toNodeKey = nodeKey,
                        edgeKind = HasVocabularyEdge,
                        weight = element.priority?.let { 1.0 / (1.0 + it.toDouble()) } ?: 0.0,
                        provenance = DerivedProvenance,
                    )
                )
            }
        }
    }

    vocabulary.kanaElements.forEach { element ->
        val nodeKey = vocabularyNodeKey(element.entry_id, element.element_id)
        addNode(
            DatabaseLearningNode(
                nodeKey = nodeKey,
                nodeKind = VocabularyKind,
                reading = element.reading,
                entryId = element.entry_id,
                elementId = element.element_id,
                priority = element.priority?.let { 1.0 / (1.0 + it.toDouble()) } ?: 0.0,
            )
        )
    }

    vocabulary.senses.forEach { sense ->
        val nodeKey = "sense:${sense.entry_id}|${sense.id}"
        addNode(
            DatabaseLearningNode(
                nodeKey = nodeKey,
                nodeKind = SenseKind,
                entryId = sense.entry_id,
                senseId = sense.id,
            )
        )
        kanjiElementsByEntry[sense.entry_id].orEmpty()
            .forEach { element ->
                addEdge(
                    DatabaseLearningEdge(
                        fromNodeKey = vocabularyNodeKey(element.entry_id, element.element_id),
                        toNodeKey = nodeKey,
                        edgeKind = HasSenseEdge,
                        weight = 1.0,
                        provenance = DerivedProvenance,
                    )
                )
            }
        kanaElementsByEntry[sense.entry_id].orEmpty()
            .forEach { element ->
                addEdge(
                    DatabaseLearningEdge(
                        fromNodeKey = vocabularyNodeKey(element.entry_id, element.element_id),
                        toNodeKey = nodeKey,
                        edgeKind = HasSenseEdge,
                        weight = 1.0,
                        provenance = DerivedProvenance,
                    )
                )
            }
    }

    sentences.forEach { sentence ->
        addNode(
            DatabaseLearningNode(
                nodeKey = "sentence:${sentence.tatoeba_id}",
                nodeKind = SentenceKind,
                sentenceId = sentence.tatoeba_id,
                priority = sentence.score,
            )
        )
    }

    vocabulary.senseExample.forEach { example ->
        val entryId = senseEntryById[example.sense_id] ?: return@forEach
        val senseKey = "sense:$entryId|${example.sense_id}"
        val sentenceKey = "sentence:${example.tatoeba_id}"
        addEdge(
            DatabaseLearningEdge(
                fromNodeKey = senseKey,
                toNodeKey = sentenceKey,
                edgeKind = AppearsInSentenceEdge,
                weight = 1.0,
                provenance = DerivedProvenance,
            )
        )
    }

    return nodes.values.sortedBy { it.nodeKey } to edges
        .sortedWith(compareBy<DatabaseLearningEdge> { it.fromNodeKey }
            .thenBy { it.edgeKind }
            .thenBy { it.toNodeKey })
}

private fun vocabularyNodeKey(entryId: Long, elementId: Long): String =
    "vocab-element:$entryId|$elementId"
