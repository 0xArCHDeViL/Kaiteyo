package ua.syt0r.kanji.application

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import java.io.File
import ua.syt0r.kanji.core.app_data.CrashLogCleaner
import java.io.PrintWriter
import java.io.StringWriter

class CrashAnalyticEngine(
    private val context: Context,
    private val appPreferences: PreferencesContract.AppPreferences
) : Thread.UncaughtExceptionHandler, CrashLogCleaner {

    override fun clearAllLogs(): Int {
        var deletedCount = 0
        val targetDirs = listOfNotNull(
            File(context.filesDir, "CrashLogs"),
            context.getExternalFilesDir("CrashLogs"),
            runCatching { File(android.os.Environment.getExternalStorageDirectory(), "Kaiteyo/CrashLogs") }.getOrNull()
        )

        targetDirs.forEach { dir ->
            runCatching {
                if (dir.exists() && dir.isDirectory) {
                    dir.listFiles()?.forEach { file ->
                        if (file.isFile && file.delete()) {
                            deletedCount++
                        }
                    }
                }
            }
        }
        return deletedCount
    }

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    @Volatile
    private var isEnabled: Boolean = true

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
        try {
            runBlocking {
                isEnabled = appPreferences.crashAnalyticsEnabled.get()
            }
        } catch (_: Exception) {
            isEnabled = true
        }

        CoroutineScope(Dispatchers.Default).launch {
            appPreferences.crashAnalyticsEnabled.onModified.collect {
                try {
                    isEnabled = appPreferences.crashAnalyticsEnabled.get()
                } catch (_: Exception) {}
            }
        }
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        if (isEnabled) {
            writeLogToSdCard(thread, throwable)
        }
        defaultHandler?.uncaughtException(thread, throwable)
    }

    private fun writeLogToSdCard(thread: Thread, throwable: Throwable) {
        val currentMoment = Clock.System.now()
        val time = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
        val filename = "crash_${time.year}_${time.monthNumber}_${time.dayOfMonth}_${time.hour}_${time.minute}_${time.second}.txt"
        
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        
        val logContent = buildString {
            appendLine("=== Kaiteyo Crash Analytic Engine ===")
            appendLine("Time: $time")
            appendLine("Thread: ${thread.name}")
            appendLine("Exception Details:")
            appendLine(sw.toString())
        }
        
        android.util.Log.e("CrashAnalyticEngine", logContent, throwable)
        
        // Destination 1: Internal App Files Dir (100% guaranteed access without any permissions)
        runCatching {
            val internalLogDir = File(context.filesDir, "CrashLogs")
            if (!internalLogDir.exists()) internalLogDir.mkdirs()
            File(internalLogDir, filename).writeText(logContent)
            File(internalLogDir, "latest_crash.txt").writeText(logContent)
        }
        
        // Destination 2: App External Files Dir (/sdcard/Android/data/ua.syt0r.kanji/files/CrashLogs)
        runCatching {
            val externalAppDir = context.getExternalFilesDir("CrashLogs")
            if (externalAppDir != null) {
                if (!externalAppDir.exists()) externalAppDir.mkdirs()
                File(externalAppDir, filename).writeText(logContent)
                File(externalAppDir, "latest_crash.txt").writeText(logContent)
            }
        }
        
        // Destination 3: Public SDCard Path (/sdcard/Kaiteyo/CrashLogs)
        runCatching {
            val sdcard = android.os.Environment.getExternalStorageDirectory()
            val logDir = File(sdcard, "Kaiteyo/CrashLogs")
            if (!logDir.exists()) logDir.mkdirs()
            File(logDir, filename).writeText(logContent)
            File(logDir, "latest_crash.txt").writeText(logContent)
        }
    }
}
