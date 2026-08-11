package ua.syt0r.kanji.core.tts

interface AppTtsManager {
    suspend fun speak(text: String, language: String = "ja-JP")
    fun stop()
}
