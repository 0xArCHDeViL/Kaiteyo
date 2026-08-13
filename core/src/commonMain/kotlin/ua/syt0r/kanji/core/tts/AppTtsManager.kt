package ua.syt0r.kanji.core.tts

interface AppTtsManager {
    suspend fun speak(text: String, language: String = "ja-JP")

    suspend fun speak(request: JapaneseSpeechRequest) {
        val normalizedRequest = request.normalized()
        if (normalizedRequest.pronunciation.isNotEmpty()) {
            speak(normalizedRequest.pronunciation, normalizedRequest.language)
        }
    }

    fun stop()
}
