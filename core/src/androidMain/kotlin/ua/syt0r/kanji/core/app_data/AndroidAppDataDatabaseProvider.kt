package ua.syt0r.kanji.core.app_data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.CustomVersionSqlSchema
import ua.syt0r.kanji.core.app_data.db.AppDataDatabase
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.readUserVersion
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.zip.GZIPInputStream

private const val AppDataDatabaseName = "kanji_data"
private const val DownloadBufferSize = 128 * 1024
private const val HttpRequestedRangeNotSatisfiable = 416

class AndroidAppDataDatabaseProvider(
    private val context: Context
) : AppDataDatabaseProvider {

    private val coroutineScope = CoroutineScope(context = Dispatchers.IO)

    override fun provideAsync(): Deferred<AppDataDatabase> = coroutineScope.async {
        val existing = getCurrentDatabase()
        if (existing != null) return@async existing

        try {
            createNewDatabaseFromPack()
        } catch (error: Throwable) {
            val fallback = getLegacyDatabase()
            if (fallback != null) {
                Logger.e("Full app-data pack unavailable; using existing legacy database: ${error.stackTraceToString()}")
                fallback
            } else {
                throw error
            }
        }
    }

    private suspend fun getCurrentDatabase(): AppDataDatabase? {
        val dbFile = context.getDatabasePath(AppDataDatabaseName)
        if (!dbFile.isFile) return null
        if (readDatabaseVersion(dbFile) != AppDataDatabaseVersion) return null

        return try {
            AppDataDatabase(createDriver(dbFile, AppDataDatabaseVersion).driver)
        } catch (error: Throwable) {
            Logger.e("Existing app-data database is unusable: ${error.stackTraceToString()}")
            null
        }
    }

    private suspend fun createNewDatabaseFromPack(): AppDataDatabase = withContext(Dispatchers.IO) {
        val archive = ensureDownloadedPack()
        val dbFile = context.getDatabasePath(AppDataDatabaseName)
        dbFile.parentFile?.mkdirs()
        val stagedDb = File(dbFile.parentFile, "${dbFile.name}.download")
        stagedDb.delete()

        try {
            GZIPInputStream(BufferedInputStream(FileInputStream(archive), DownloadBufferSize)).use { input ->
                FileOutputStream(stagedDb).use { output ->
                    input.copyTo(output, DownloadBufferSize)
                }
            }
            validateSqliteFile(stagedDb)
            context.deleteDatabase(AppDataDatabaseName)
            moveAtomically(stagedDb, dbFile)
        } finally {
            stagedDb.delete()
            archive.delete()
            File(archive.parentFile, "${archive.name}.sha256").delete()
        }

        AppDataDatabase(createDriver(dbFile, AppDataDatabaseVersion).driver)
    }

    private fun ensureDownloadedPack(): File {
        val directory = File(context.noBackupFilesDir, "app-data").apply { mkdirs() }
        val archive = File(directory, AppDataPackResourceName)
        val partial = File(directory, "${archive.name}.part")
        val checksum = File(directory, "${archive.name}.sha256")

        if (!archive.isFile) {
            downloadResumable(AppDataPackUrl, partial)
            moveAtomically(partial, archive)
        }

        downloadFresh(AppDataPackChecksumUrl, checksum)
        if (!matchesSha256(archive, checksum)) {
            archive.delete()
            checksum.delete()
            throw IllegalStateException("Downloaded app-data pack checksum mismatch")
        }
        return archive
    }

    private fun downloadResumable(url: String, partial: File) {
        val existingBytes = partial.length()
        val connection = openConnection(url).apply {
            if (existingBytes > 0L) setRequestProperty("Range", "bytes=$existingBytes-")
        }
        try {
            val responseCode = connection.responseCode
            val append = existingBytes > 0L && responseCode == HttpURLConnection.HTTP_PARTIAL
            if (responseCode !in 200..299) {
                if (existingBytes > 0L && responseCode == HttpRequestedRangeNotSatisfiable) {
                    partial.delete()
                    downloadResumable(url, partial)
                    return
                }
                throw IllegalStateException("HTTP $responseCode while downloading app-data pack")
            }
            if (!append) partial.delete()
            connection.inputStream.use { input ->
                FileOutputStream(partial, append).use { output ->
                    input.copyTo(output, DownloadBufferSize)
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun downloadFresh(url: String, target: File) {
        val connection = openConnection(url)
        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IllegalStateException("HTTP $responseCode while downloading app-data checksum")
            }
            FileOutputStream(target).use { output ->
                connection.inputStream.use { input ->
                    input.copyTo(output, DownloadBufferSize)
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun openConnection(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 120_000
            instanceFollowRedirects = true
            setRequestProperty("Accept-Encoding", "identity")
            connect()
        }

    private fun matchesSha256(file: File, checksumFile: File): Boolean {
        val expected = checksumFile.readText(StandardCharsets.UTF_8)
            .trim()
            .split(Regex("\\s+"))
            .firstOrNull()
            ?.lowercase()
            ?: return false
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(DownloadBufferSize)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) } == expected
    }

    private fun validateSqliteFile(file: File) {
        require(file.length() >= 16) { "Extracted app-data database is empty" }
        FileInputStream(file).use { input ->
            val header = ByteArray(16)
            require(input.read(header) == header.size) { "Extracted app-data database is truncated" }
            require(header.toString(StandardCharsets.US_ASCII) == "SQLite format 3\u0000") {
                "Extracted app-data database has an invalid SQLite header"
            }
        }
    }

    private fun moveAtomically(source: File, target: File) {
        try {
            Files.move(
                source.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private suspend fun getLegacyDatabase(): AppDataDatabase? {
        val dbFile = context.getDatabasePath(AppDataDatabaseName)
        if (!dbFile.isFile) return null
        return try {
            val version = readDatabaseVersion(dbFile)
            AppDataDatabase(createDriver(dbFile, version).driver)
        } catch (error: Throwable) {
            Logger.e("Legacy app-data database fallback is unusable: ${error.stackTraceToString()}")
            null
        }
    }

    private fun readDatabaseVersion(file: File): Long =
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { database ->
            database.version.toLong()
        }

    private data class DriverCreationResult(
        val driver: AndroidSqliteDriver,
        val wasUpgraded: Boolean
    )

    private suspend fun createDriver(
        dbFile: File,
        schemaVersion: Long
    ): DriverCreationResult {
        val schema = CustomVersionSqlSchema(schemaVersion, AppDataDatabase.Schema)
        var wasUpgraded = false
        val onDatabaseOpen = CompletableDeferred<Unit>()
        val driver = AndroidSqliteDriver(
            schema = schema,
            context = context,
            name = dbFile.name,
            callback = object : AndroidSqliteDriver.Callback(schema) {
                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    super.onUpgrade(db, oldVersion, newVersion)
                    Logger.d("oldVersion[$oldVersion] newVersion[$newVersion]")
                    wasUpgraded = true
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.setForeignKeyConstraintsEnabled(true)
                    db.execSQL("PRAGMA query_only = ON")
                    onDatabaseOpen.complete(Unit)
                }
            },
        )
        val version = driver.readUserVersion()
        onDatabaseOpen.await()
        Logger.d("driver ready version[$version] wasUpgraded[$wasUpgraded]")
        return DriverCreationResult(driver, wasUpgraded)
    }
}
