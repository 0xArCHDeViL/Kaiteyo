package ua.syt0r.kanji.core.app_data

internal object AppDataPackFormat {
    const val SqliteHeader = "SQLite format 3\u0000"

    fun isGzipHeader(bytes: ByteArray, count: Int): Boolean =
        count >= 2 &&
            bytes[0].toInt() and 0xff == 0x1f &&
            bytes[1].toInt() and 0xff == 0x8b

    fun supportsDatabaseVersion(importedVersion: Long, expectedVersion: Long): Boolean =
        importedVersion == expectedVersion
}
