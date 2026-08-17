package ua.syt0r.kanji.core.app_data

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppDataPackFormatTest {

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
    fun acceptsOnlyExpectedDatabaseVersion() {
        assertTrue(AppDataPackFormat.supportsDatabaseVersion(21, 21))
        assertFalse(AppDataPackFormat.supportsDatabaseVersion(20, 21))
        assertFalse(AppDataPackFormat.supportsDatabaseVersion(22, 21))
    }
}
