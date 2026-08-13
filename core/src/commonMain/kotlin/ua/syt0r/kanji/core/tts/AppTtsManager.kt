package ua.syt0r.kanji.core.tts

interface AppTtsManager {
    suspend fun speak(text: String, language: String = "ja-JP")

    suspend fun speak(request: JapaneseSpeechRequest) {
        val plan = JapanesePronunciationEngine.resolve(request) ?: return
        speak(plan.speakText, plan.language)
    }

    fun stop()
}
