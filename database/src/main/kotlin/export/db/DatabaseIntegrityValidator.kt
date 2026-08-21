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
                check(count(statement, "learning_node") > 0) { "learning_node is empty" }
                check(count(statement, "learning_edge") > 0) { "learning_edge is empty" }
                check(
                    count(statement, "learning_node", "node_key") ==
                        countDistinct(statement, "learning_node", "node_key")
                ) { "learning_node contains duplicate node_key values" }
                check(
                    countQuery(
                        statement,
                        """
                        SELECT COUNT(*)
                        FROM learning_edge AS edge
                        LEFT JOIN learning_node AS source ON source.node_id = edge.from_node_id
                        LEFT JOIN learning_node AS target ON target.node_id = edge.to_node_id
                        WHERE source.node_id IS NULL OR target.node_id IS NULL
                        """.trimIndent()
                    ) == 0L
                ) { "learning_edge contains orphan node references" }
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
        countQuery(statement, "SELECT COUNT(*) FROM $table")

    private fun count(
        statement: java.sql.Statement,
        table: String,
        column: String,
    ): Long = countQuery(statement, "SELECT COUNT($column) FROM $table")

    private fun countDistinct(
        statement: java.sql.Statement,
        table: String,
        column: String,
    ): Long = countQuery(statement, "SELECT COUNT(DISTINCT $column) FROM $table")

    private fun countQuery(statement: java.sql.Statement, query: String): Long =
        statement.executeQuery(query).use { result ->
            check(result.next()) { "Unable to execute count query" }
            result.getLong(1)
        }
}
