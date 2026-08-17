package task

import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class LetterDeckDataIntegrityTest {

    @Test
    fun n5DeckContainsExpectedCoreCoverage() {
        val file = sequenceOf(
            File("database/data/letter_decks/n5.csv"),
            File("data/letter_decks/n5.csv")
        ).firstOrNull { it.isFile }
            ?: error("N5 letter deck source was not found")

        val kanji = file.readLines()
            .map(String::trim)
            .filter(String::isNotEmpty)

        assertEquals(80, kanji.size, "N5 source coverage must remain 80 kanji")
        assertEquals(80, kanji.toSet().size, "N5 source must not contain duplicate kanji")
        assertContains(kanji, "分", "分 is JLPT N5 and must remain in the source deck")
    }
}
