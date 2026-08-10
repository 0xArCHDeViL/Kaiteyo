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
import java.io.PrintWriter
import java.io.StringWriter

class CrashAnalyticEngine(
    private val context: Context,
    private val appPreferences: PreferencesContract.AppPreferences
) : Thread.UncaughtExceptionHandler {

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
        try {
            val sdcard = android.os.Environment.getExternalStorageDirectory()
            val logDir = File(sdcard, "Kaiteyo/CrashLogs")
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            
            val currentMoment = Clock.System.now()
            val time = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
            val filename = "crash_${time.year}_${time.monthNumber}_${time.dayOfMonth}_${time.hour}_${time.minute}_${time.second}.txt"
            val file = File(logDir, filename)
            
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            
            val logContent = buildString {
                appendLine("=== Kaiteyo Crash Analytic Engine ===")
                appendLine("Time: $time")
                appendLine("Thread: ${thread.name}")
                appendLine("Exception:")
                appendLine(sw.toString())
            }
            
            file.writeText(logContent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
