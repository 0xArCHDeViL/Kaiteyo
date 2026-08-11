package ua.syt0r.kanji.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CompletableDeferred
import java.util.Locale

class AndroidAppTtsManager(
    private val context: Context
) : AppTtsManager {

    private var tts: TextToSpeech? = null
    private var isInitialized = CompletableDeferred<Boolean>()

    private fun initTts() {
        if (tts != null) return
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized.complete(true)
            } else {
                isInitialized.complete(false)
            }
        }
    }

    override suspend fun speak(text: String, language: String) {
        if (tts == null) {
            initTts()
        }
        val success = isInitialized.await()
        if (success) {
            tts?.language = Locale.forLanguageTag(language)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun stop() {
        tts?.stop()
    }
}
