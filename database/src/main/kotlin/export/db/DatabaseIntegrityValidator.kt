package export.db

import java.io.File
import java.sql.DriverManager

object DatabaseIntegrityValidator {

    fun validate(
        file: File,
        expectedKanjiCount: Int,
        expectedVocabCount: Int,
        expectedSentenceCount: Int,
        expectedDeckCardCount: Int
    ) {
        require(file.isFile && file.length() > 0) {
            "Database artifact is missing or empty: ${file.absolutePath}"
        }

        DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}").use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("PRAGMA foreign_keys = ON")

                statement.executeQuery("PRAGMA integrity_check").use { result ->
                    check(result.next() && result.getString(1) == "ok") {
                        "SQLite integrity_check failed for ${file.name}"
                    }
                }

                statement.executeQuery("PRAGMA foreign_key_check").use { result ->
                    if (result.next()) {
                        val violations = buildList {
                            do {
                                add(
                                    "table=${result.getString(1)} " +
                                        "rowid=${result.getLong(2)} " +
                                        "parent=${result.getString(3)}"
                                )
                            } while (result.next() && size < 20)
                        }
                        error(
                            "SQLite foreign_key_check failed for ${file.name}: " +
                                violations.joinToString()
                        )
                    }
                }

                checkCount(statement, "kanji_data", expectedKanjiCount)
                checkCount(statement, "vocab_entry", expectedVocabCount)
                checkCount(statement, "sentence", expectedSentenceCount)
                checkCount(statement, "vocab_deck_card", expectedDeckCardCount)
                check(count(statement, "vocab_furigana") > 0) { "vocab_furigana is empty" }
            }
        }
    }

    private fun checkCount(statement: java.sql.Statement, table: String, expected: Int) {
        val actual = count(statement, table)
        check(actual == expected.toLong()) {
            "$table coverage mismatch: expected=$expected actual=$actual"
        }
    }

    private fun count(statement: java.sql.Statement, table: String): Long =
        statement.executeQuery("SELECT COUNT(*) FROM $table").use { result ->
            check(result.next()) { "Unable to count $table" }
            result.getLong(1)
        }
}
