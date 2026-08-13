package ua.syt0r.kanji.core.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

/**
 * App-scoped Android TTS facade.
 *
 * Android owns the synthesis engine; this class only coordinates lifecycle,
 * locale capability checks, bounded input, and latest-request semantics.
 */
class AndroidAppTtsManager(
    context: Context,
    private val mainDispatcher: MainCoroutineDispatcher = Dispatchers.Main.immediate,
) : AppTtsManager {

    private val applicationContext = context.applicationContext
    private val requestSequence = AtomicLong(0L)
    private val scope = kotlinx.coroutines.CoroutineScope(SupervisorJob() + mainDispatcher)

    private var tts: TextToSpeech? = null
    private var initialization: CompletableDeferred<Boolean>? = null
    private var selectedLocale: Locale? = null

    override suspend fun speak(text: String, language: String) {
        val normalizedText = JapaneseSpeechText.normalize(text)
        if (normalizedText.isEmpty()) return
        enqueueSpeech(normalizedText, language)
    }

    override suspend fun speak(request: JapaneseSpeechRequest) {
        val normalizedRequest = request.normalized()
        if (normalizedRequest.pronunciation.isEmpty()) return

        // An isolated Kanji is ambiguous by design. Speaking the resolved kana
        // is the portable, deterministic path across Android TTS engines.
        enqueueSpeech(normalizedRequest.pronunciation, normalizedRequest.language)
    }

    private suspend fun enqueueSpeech(
        text: String,
        language: String,
        renderChunk: (String) -> CharSequence = { it }
    ) {
        withContext(mainDispatcher) {
            val engine = ensureInitialized() ?: return@withContext
            selectSupportedLocale(engine, language) ?: return@withContext
            val chunks = splitForSpeech(text, TextToSpeech.getMaxSpeechInputLength())
            if (chunks.isEmpty()) return@withContext

            val requestId = requestSequence.incrementAndGet()
            chunks.forEachIndexed { index, chunk ->
                val result = engine.speak(
                    renderChunk(chunk),
                    if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
                    Bundle(),
                    "kaiteyo-$requestId-$index"
                )
                if (result != TextToSpeech.SUCCESS) return@forEachIndexed
            }
        }
    }


    override fun stop() {
        scope.launchOnMain { tts?.stop() }
    }

    fun shutdown() {
        scope.launchOnMain {
            tts?.stop()
            tts?.shutdown()
            tts = null
            initialization = null
            selectedLocale = null
        }
    }

    private suspend fun ensureInitialized(): TextToSpeech? {
        tts?.let { return it }

        val pending = initialization ?: CompletableDeferred<Boolean>().also { deferred ->
            initialization = deferred
            val created = TextToSpeech(applicationContext) { status ->
                if (!deferred.isCompleted) deferred.complete(status == TextToSpeech.SUCCESS)
            }
            created.setOnUtteranceProgressListener(NoOpUtteranceProgressListener)
            tts = created
        }

        if (!pending.await()) {
            tts?.shutdown()
            tts = null
            initialization = null
            return null
        }
        return tts
    }

    private fun selectSupportedLocale(engine: TextToSpeech, language: String): Locale? {
        val requested = Locale.forLanguageTag(language).takeIf { it.language.isNotBlank() }
        val candidates = buildList {
            requested?.let { add(it) }
            if (requested?.language == "ja" || requested == null) {
                add(Locale.JAPAN)
                add(Locale.JAPANESE)
            }
        }.distinct()

        val locale = candidates.firstOrNull { candidate ->
            engine.isLanguageAvailable(candidate) >= TextToSpeech.LANG_AVAILABLE
        } ?: return null

        if (selectedLocale != locale) {
            val result = engine.setLanguage(locale)
            if (result < TextToSpeech.LANG_AVAILABLE) return null
            selectedLocale = locale
        }
        return locale
    }

    private fun splitForSpeech(text: String, maxLength: Int): List<String> {
        if (maxLength <= 0 || text.length <= maxLength) return listOf(text)

        val result = ArrayList<String>(text.length / maxLength + 1)
        var start = 0
        while (start < text.length) {
            val hardEnd = minOf(start + maxLength, text.length)
            val end = if (hardEnd == text.length) {
                hardEnd
            } else {
                val boundary = text.lastIndexOfAny(charArrayOf('。', '！', '？', '!', '?', '、', '，', ',', ' '), hardEnd - 1)
                if (boundary > start) boundary + 1 else hardEnd
            }
            text.substring(start, end).trim().takeIf { it.isNotEmpty() }?.let(result::add)
            start = end
        }
        return result
    }

    private fun kotlinx.coroutines.CoroutineScope.launchOnMain(block: () -> Unit) {
        launch { block() }
    }

    private object NoOpUtteranceProgressListener : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = Unit
        override fun onDone(utteranceId: String?) = Unit
        override fun onError(utteranceId: String?) = Unit
        override fun onError(utteranceId: String?, errorCode: Int) = Unit
    }
}
