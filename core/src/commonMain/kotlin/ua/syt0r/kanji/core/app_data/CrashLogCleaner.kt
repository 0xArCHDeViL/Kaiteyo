package ua.syt0r.kanji.core.app_data

interface CrashLogCleaner {
    fun clearAllLogs(): Int
}

class NoOpCrashLogCleaner : CrashLogCleaner {
    override fun clearAllLogs(): Int = 0
}
