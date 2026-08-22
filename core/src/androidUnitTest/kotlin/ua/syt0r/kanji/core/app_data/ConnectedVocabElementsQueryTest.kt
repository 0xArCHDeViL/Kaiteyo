package ua.syt0r.kanji.core.app_data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ua.syt0r.kanji.core.app_data.db.AppDataDatabase

class ConnectedVocabElementsQueryTest {

    @Test
    fun connectedElementsRemainOrderedAndNullableMetadataIsCoalesced() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            AppDataDatabase.Schema.create(driver)
            val database = AppDataDatabase(driver)

            execute(driver, "INSERT INTO vocab_entry(id) VALUES (42), (43)")
            execute(
                driver,
                "INSERT INTO vocab_kanji_element(element_id, entry_id, reading, priority) " +
                    "VALUES (2, 42, '学', 1)"
            )
            execute(
                driver,
                "INSERT INTO vocab_kana_element(element_id, entry_id, reading, no_kanji, priority) " +
                    "VALUES (1, 42, 'がく', 0, 1), (3, 43, 'よみ', 1, NULL)"
            )
            execute(driver, "INSERT INTO vocab_sense(id, entry_id) VALUES (10, 42)")
            execute(driver, "INSERT INTO vocab_sense_gloss(sense_id, gloss_text) VALUES (10, 'study')")
            execute(driver, "INSERT INTO vocab_sense_part_of_speech(sense_id, part_of_speech) VALUES (10, 'noun')")

            val rows = database.vocabQueries
                .getConnectedVocabElementsForEntries(" · ", listOf(42L, 43L))
                .executeAsList()

            assertEquals(listOf("KANA", "KANJI", "KANA"), rows.map { it.element_kind })
            assertEquals(listOf(1L, 2L, 3L), rows.map { it.element_id })
            assertEquals(listOf(42L, 42L, 43L), rows.map { it.entry_id })
            assertEquals(listOf("study", "study", ""), rows.map { it.glosses })
            assertEquals(listOf("noun", "noun", ""), rows.map { it.part_of_speech })
            assertTrue(rows.all { it.reading.isNotEmpty() })
        } finally {
            driver.close()
        }
    }

    private fun execute(driver: JdbcSqliteDriver, sql: String) {
        driver.execute(identifier = null, sql = sql, parameters = 0)
    }
}
