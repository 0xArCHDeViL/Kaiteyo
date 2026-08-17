package ua.syt0r.kanji.core.app_data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.CustomVersionSqlSchema
import ua.syt0r.kanji.core.app_data.db.AppDataDatabase
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.readUserVersion
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
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
) : AppDataDatabaseProvider, AppDataSetupController {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val setupMutex = Mutex()
    private val databaseReady = CompletableDeferred<AppDataDatabase>()
    private val setupStateFlow = MutableStateFlow<AppDataSetupState>(AppDataSetupState.Checking)
    private var setupJob: Job? = null
    private var lastSetupAction: SetupAction? = null

    override val state: StateFlow<AppDataSetupState> = setupStateFlow.asStateFlow()

    override fun initialize() {
        if (setupStateFlow.value != AppDataSetupState.Checking || databaseReady.isCompleted) return
        coroutineScope.launch { prepareInitialState() }
    }

    override fun provideAsync(): Deferred<AppDataDatabase> = coroutineScope.async {
        prepareInitialState()
        databaseReady.await()
    }

    private suspend fun prepareInitialState() {
        setupMutex.withLock {
            if (databaseReady.isCompleted || setupStateFlow.value != AppDataSetupState.Checking) return@withLock
            val existing = getCurrentDatabase()
            if (existing != null) {
                databaseReady.complete(existing)
                setupStateFlow.value = AppDataSetupState.Ready
            } else {
                setupStateFlow.value = AppDataSetupState.ChoiceRequired
            }
        }
    }

    override fun chooseDownload() {
        startSetup(SetupAction.Download)
    }

    override fun chooseImport(uri: String) {
        startSetup(SetupAction.Import(uri))
    }

    override fun retry() {
        lastSetupAction?.let(::startSetup)
    }

    private fun startSetup(action: SetupAction) {
        if (databaseReady.isCompleted || setupJob?.isActive == true) return
        lastSetupAction = action
        setupJob = coroutineScope.launch {
            try {
                setupMutex.withLock {
                    if (databaseReady.isCompleted) return@withLock
                    setupStateFlow.value = when (action) {
                        SetupAction.Download -> AppDataSetupState.Downloading
                        is SetupAction.Import -> AppDataSetupState.Importing
                    }
                    try {
                        val database = when (action) {
                            SetupAction.Download -> createNewDatabaseFromPack()
                            is SetupAction.Import -> importDatabaseFromUri(action.uri)
                        }
                        databaseReady.complete(database)
                        setupStateFlow.value = AppDataSetupState.Ready
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Throwable) {
                        val fallback = getLegacyDatabase()
                        if (fallback != null) {
                            Logger.e("App-data setup failed; using existing legacy database: ${error.stackTraceToString()}")
                            databaseReady.complete(fallback)
                            setupStateFlow.value = AppDataSetupState.Ready
                        } else {
                            Logger.e("App-data setup failed: ${error.stackTraceToString()}")
                            setupStateFlow.value = AppDataSetupState.Error(error.message)
                        }
                    }
                }
            } finally {
                setupJob = null
            }
        }
    }

    private sealed interface SetupAction {
        data object Download : SetupAction
        data class Import(val uri: String) : SetupAction
    }

    private suspend fun importDatabaseFromUri(uriString: String): AppDataDatabase = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath(AppDataDatabaseName)
        dbFile.parentFile?.mkdirs()
        val stagedDb = File(dbFile.parentFile, "${dbFile.name}.import")
        stagedDb.delete()
        val uri = Uri.parse(uriString)

        try {
            val source = context.contentResolver.openInputStream(uri)
                ?: error("Unable to open selected database file")
            source.use { rawInput ->
                val bufferedInput = BufferedInputStream(rawInput, DownloadBufferSize)
                bufferedInput.mark(2)
                val header = ByteArray(2)
                val headerBytes = bufferedInput.read(header)
                bufferedInput.reset()
                val input: InputStream = if (AppDataPackFormat.isGzipHeader(header, headerBytes)) {
                    GZIPInputStream(bufferedInput, DownloadBufferSize)
                } else {
                    bufferedInput
                }
                input.use { databaseInput ->
                    FileOutputStream(stagedDb).use { output ->
                        databaseInput.copyTo(output, DownloadBufferSize)
                    }
                }
            }
            val importedVersion = validateSqliteFile(stagedDb)
            require(AppDataPackFormat.supportsSchemaVersion(importedVersion, AppDataSchemaVersion)) {
                "Unsupported app-data version $importedVersion; expected $AppDataSchemaVersion"
            }
            installStagedDatabase(stagedDb)
        } finally {
            stagedDb.delete()
        }
    }

    private suspend fun getCurrentDatabase(): AppDataDatabase? {
        val dbFile = context.getDatabasePath(AppDataDatabaseName)
        if (!dbFile.isFile) return null

        return try {
            if (readDatabaseVersion(dbFile) != AppDataSchemaVersion) return null
            AppDataDatabase(createDriver(dbFile, AppDataSchemaVersion).driver)
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
            val downloadedVersion = validateSqliteFile(stagedDb)
            require(AppDataPackFormat.supportsSchemaVersion(downloadedVersion, AppDataSchemaVersion)) {
                "Downloaded app-data version $downloadedVersion; expected $AppDataSchemaVersion"
            }
            installStagedDatabase(stagedDb)
        } finally {
            stagedDb.delete()
            archive.delete()
            File(archive.parentFile, "${archive.name}.sha256").delete()
        }

        AppDataDatabase(createDriver(dbFile, AppDataSchemaVersion).driver)
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

    private fun validateSqliteFile(file: File): Long {
        require(file.length() >= 16) { "Extracted app-data database is empty" }
        FileInputStream(file).use { input ->
            val header = ByteArray(16)
            require(input.read(header) == header.size) { "Extracted app-data database is truncated" }
            require(header.toString(StandardCharsets.US_ASCII) == AppDataPackFormat.SqliteHeader) {
                "Extracted app-data database has an invalid SQLite header"
            }
        }

        return SQLiteDatabase.openDatabase(
            file.path,
            null,
            SQLiteDatabase.OPEN_READONLY
        ).use { database ->
            val missingTables = AppDataPackFormat.RequiredTables.filterNot { table ->
                database.rawQuery(
                    "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ? LIMIT 1",
                    arrayOf(table)
                ).use { cursor -> cursor.moveToFirst() }
            }
            require(missingTables.isEmpty()) {
                "App-data database is missing required tables: ${missingTables.joinToString()}"
            }
            database.version.toLong()
        }
    }

    private suspend fun installStagedDatabase(stagedDb: File): AppDataDatabase = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath(AppDataDatabaseName)
        val backupFile = File(dbFile.parentFile, "${dbFile.name}.previous")
        backupFile.delete()
        val hadExistingDatabase = dbFile.isFile

        try {
            if (hadExistingDatabase) {
                Files.copy(
                    dbFile.toPath(),
                    backupFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
            context.deleteDatabase(AppDataDatabaseName)
            moveAtomically(stagedDb, dbFile)
            val database = AppDataDatabase(createDriver(dbFile, AppDataSchemaVersion).driver)
            backupFile.delete()
            database
        } catch (error: Throwable) {
            context.deleteDatabase(AppDataDatabaseName)
            if (backupFile.isFile) moveAtomically(backupFile, dbFile)
            throw error
        } finally {
            backupFile.delete()
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
