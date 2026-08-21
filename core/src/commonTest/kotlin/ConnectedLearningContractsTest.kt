import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import ua.syt0r.kanji.core.connected_learning.ConnectedEntityKey
import ua.syt0r.kanji.core.connected_learning.ConnectedItemKey
import ua.syt0r.kanji.core.connected_learning.ConnectedNodeKey
import ua.syt0r.kanji.core.connected_learning.GraphEdgeKind
import ua.syt0r.kanji.core.connected_learning.GraphNodeKind
import ua.syt0r.kanji.core.connected_learning.ReviewDimension

class ConnectedLearningContractsTest {

    @Test
    fun vocabularyElementsWithSharedReadingRemainDistinct() {
        val husband = ConnectedEntityKey.VocabularyElement(
            entryId = 1001,
            elementId = 1,
            reading = "しゅふ",
        )
        val wife = ConnectedEntityKey.VocabularyElement(
            entryId = 1002,
            elementId = 1,
            reading = "しゅふ",
        )

        assertNotEquals(
            ConnectedNodeKey.from(husband),
            ConnectedNodeKey.from(wife),
        )
        assertNotEquals(
            ConnectedItemKey.from(husband, variant = "reading"),
            ConnectedItemKey.from(wife, variant = "reading"),
        )
    }

    @Test
    fun canonicalKeysEscapeStructuralSeparators() {
        val entity = ConnectedEntityKey.Reading(
            kanji = "a|b",
            reading = "x:y=z%q",
        )

        val key = ConnectedNodeKey.from(entity).value

        assertEquals("reading:a%7Cb|x%3Ay%3Dz%25q", key)
        assertTrue(key.none { it == '\n' || it == '\r' })
    }

    @Test
    fun legacyVocabularyEntryHasExplicitLowerPrecisionIdentity() {
        val key = ConnectedNodeKey.from(
            ConnectedEntityKey.LegacyVocabularyEntry(entryId = 42)
        )

        assertEquals("vocab-entry:42", key.value)
    }

    @Test
    fun graphAndReviewEnumsHaveStableNamedValues() {
        assertEquals("KANJI_READING", ReviewDimension.KANJI_READING.name)
        assertEquals("HAS_VOCABULARY", GraphEdgeKind.HAS_VOCABULARY.name)
        assertEquals("VOCABULARY_ELEMENT", GraphNodeKind.VOCABULARY_ELEMENT.name)
    }
}
