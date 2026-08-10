package ua.syt0r.kanji.application

import android.content.Context
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // Run synchronously to ensure it writes before crash
        if (appPreferences.crashAnalyticsEnabled.get()) {
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
