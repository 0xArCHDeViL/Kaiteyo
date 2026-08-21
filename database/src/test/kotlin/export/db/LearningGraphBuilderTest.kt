package export.db

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LearningGraphBuilderTest {

    @Test
    fun buildsStableIdentitySafeGraph() {
        val (nodes, edges) = buildLearningGraph(
            radicals = listOf(DatabaseRadical("木", 4)),
            kanjiData = listOf(
                DatabaseKanjiData(
                    kanji = "休",
                    meanings = listOf("rest"),
                    onReadings = listOf("キュウ"),
                    kunReadings = listOf("やすむ"),
                    frequency = 100,
                    variantFamily = null,
                )
            ),
            kanjiRadicals = listOf(
                DatabaseKanjiRadical("休", "木", 2, 4),
            ),
            vocabulary = DatabaseVocabData(
                entries = listOf(Vocab_entry(100), Vocab_entry(101)),
                kanjiElements = listOf(
                    Vocab_kanji_element(1, 100, "休日", 1),
                    Vocab_kanji_element(2, 101, "休日", 2),
                ),
                kanjiInformation = emptyList(),
                kanjiPriorities = emptyList(),
                kanaElements = listOf(
                    Vocab_kana_element(3, 100, "きゅうじつ", 0, 1),
                    Vocab_kana_element(4, 101, "きゅうじつ", 0, 2),
                ),
                kanaRestrictions = emptyList(),
                kanaInformation = emptyList(),
                kanaPriorities = emptyList(),
                senses = listOf(
                    Vocab_sense(10, 100),
                    Vocab_sense(11, 101),
                ),
                senseKanjiRestrictions = emptyList(),
                senseReadingRestrictions = emptyList(),
                sensePartsOfSpeech = emptyList(),
                senseCrossReferences = emptyList(),
                senseAntonyms = emptyList(),
                senseFields = emptyList(),
                senseMiscellaneous = emptyList(),
                senseDialects = emptyList(),
                senseGlosses = emptyList(),
                senseInformation = emptyList(),
                senseExample = listOf(Vocab_sense_example(10, "休日", 900)),
                entities = emptyList(),
                furigana = emptyList(),
            ),
            sentences = listOf(
                Sentence(900, "休日です。", "It is a holiday.", 1.0, "[]")
            ),
        )

        assertEquals(nodes.map { it.nodeKey }.sorted(), nodes.map { it.nodeKey })
        assertEquals(
            nodes.map { it.nodeKey }.distinct().size,
            nodes.size,
        )
        assertTrue(edges.any { it.edgeKind == "COMPOSED_OF" })
        assertTrue(edges.any { it.edgeKind == "HAS_READING" })
        assertTrue(edges.any { it.edgeKind == "HAS_VOCABULARY" })
        assertTrue(edges.any { it.edgeKind == "HAS_SENSE" })
        assertTrue(edges.any { it.edgeKind == "APPEARS_IN_SENTENCE" })

        val vocabularyNodes = nodes.filter { it.nodeKind == "VOCABULARY_ELEMENT" }
        assertEquals(4, vocabularyNodes.size)
        assertEquals(
            setOf(
                "vocab-element:100|1",
                "vocab-element:101|2",
                "vocab-element:100|3",
                "vocab-element:101|4",
            ),
            vocabularyNodes.map { it.nodeKey }.toSet(),
        )
    }
}
