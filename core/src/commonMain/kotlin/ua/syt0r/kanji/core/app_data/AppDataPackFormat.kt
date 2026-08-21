package ua.syt0r.kanji.core.app_data

internal object AppDataPackFormat {
    const val SqliteHeader = "SQLite format 3\u0000"
    val RequiredTables = setOf(
        "character_stroke",
        "kanji_data",
        "kanji_reading",
        "vocab_entry",
        "vocab_search_index"
    )

    fun isGzipHeader(bytes: ByteArray, count: Int): Boolean =
        count >= 2 &&
            bytes[0].toInt() and 0xff == 0x1f &&
            bytes[1].toInt() and 0xff == 0x8b

    fun supportsSchemaVersion(importedVersion: Long, expectedSchemaVersion: Long): Boolean =
        importedVersion == expectedSchemaVersion

    fun supportsPackRevision(importedRevision: Long, expectedRevision: Long): Boolean =
        importedRevision == expectedRevision
}
