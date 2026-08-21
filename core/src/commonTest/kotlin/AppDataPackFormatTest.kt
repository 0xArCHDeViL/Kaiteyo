package ua.syt0r.kanji.core.app_data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppDataPackFormatTest {

    @Test
    fun sqliteHeaderIsTheBinarySQLiteHeader() {
        assertEquals(16, AppDataPackFormat.SqliteHeader.encodeToByteArray().size)
        assertTrue(AppDataPackFormat.SqliteHeader.endsWith("\u0000"))
        assertFalse(AppDataPackFormat.SqliteHeader.endsWith("\\u0000"))
    }

    @Test
    fun detectsGzipMagicHeader() {
        assertTrue(AppDataPackFormat.isGzipHeader(byteArrayOf(0x1f.toByte(), 0x8b.toByte()), 2))
        assertTrue(AppDataPackFormat.isGzipHeader(byteArrayOf(0x1f.toByte(), 0x8b.toByte(), 0x08.toByte()), 3))
    }

    @Test
    fun rejectsNonGzipOrTruncatedHeader() {
        assertFalse(AppDataPackFormat.isGzipHeader(byteArrayOf(0x1f.toByte()), 1))
        assertFalse(AppDataPackFormat.isGzipHeader(byteArrayOf(0x1f.toByte(), 0x00.toByte()), 2))
        assertFalse(AppDataPackFormat.isGzipHeader(byteArrayOf(0x00.toByte(), 0x8b.toByte()), 2))
    }

    @Test
    fun acceptsOnlyExpectedSchemaVersion() {
        assertTrue(AppDataPackFormat.supportsSchemaVersion(22, 22))
        assertFalse(AppDataPackFormat.supportsSchemaVersion(21, 22))
        assertFalse(AppDataPackFormat.supportsSchemaVersion(23, 22))
    }

    @Test
    fun acceptsOnlyExpectedPackRevision() {
        assertTrue(AppDataPackFormat.supportsPackRevision(23, 23))
        assertFalse(AppDataPackFormat.supportsPackRevision(22, 23))
        assertFalse(AppDataPackFormat.supportsPackRevision(24, 23))
    }

    @Test
    fun requiredSchemaContainsKanjiAndVocabularyTables() {
        assertTrue("character_stroke" in AppDataPackFormat.RequiredTables)
        assertTrue("kanji_data" in AppDataPackFormat.RequiredTables)
        assertTrue("kanji_reading" in AppDataPackFormat.RequiredTables)
        assertTrue("vocab_entry" in AppDataPackFormat.RequiredTables)
        assertTrue("vocab_search_index" in AppDataPackFormat.RequiredTables)
        assertTrue("learning_node" in AppDataPackFormat.RequiredTables)
        assertTrue("learning_edge" in AppDataPackFormat.RequiredTables)
    }
}
