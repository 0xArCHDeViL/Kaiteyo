package ua.syt0r.kanji.core.user_data.database.migration

import app.cash.sqldelight.db.SqlDriver
import ua.syt0r.kanji.core.user_data.database.UserDataDatabaseContract

/**
 * Migration v13 -> v14: preserve the scheduler and parameter-set provenance of each review.
 */
object UserDataDatabaseMigrationAfter13 : UserDataDatabaseContract.Migration {
    override val version: Long = 13

    override suspend fun execute(driver: SqlDriver) {
        driver.execute(
            identifier = null,
            sql = """
                CREATE TABLE IF NOT EXISTS review_scheduler_metadata(
                    key TEXT NOT NULL,
                    practice_type INTEGER NOT NULL,
                    timestamp INTEGER NOT NULL,
                    algorithm_version TEXT NOT NULL,
                    parameter_set_id TEXT NOT NULL,
                    PRIMARY KEY(key, practice_type, timestamp),
                    FOREIGN KEY(key, practice_type, timestamp)
                        REFERENCES review_history(key, practice_type, timestamp)
                        ON UPDATE CASCADE ON DELETE CASCADE
                );
            """.trimIndent(),
            parameters = 0,
        )
    }
}
