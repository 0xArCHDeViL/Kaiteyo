package ua.syt0r.kanji.core.tts

import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.toKanaReading

/**
 * Semantic context supplied by a caller before text reaches a platform TTS
 * engine. The context changes pronunciation policy without changing visible
 * UI text.
 */
enum class JapaneseSpeechContext {
    Auto,
    Grammar,
    Vocabulary,
    Sentence,
    IsolatedKanji,
    Kana
}

enum class JapaneseSpeechStrategy {
    ExplicitReading,
    Furigana,
    StandaloneKun,
    StandaloneOn,
    RawJapaneseFallback
}

data class JapaneseSpeechPlan(
    val displayText: String,
    val speakText: String,
    val language: String,
    val strategy: JapaneseSpeechStrategy
)

/**
 * Lightweight lexical pronunciation planner.
 *
 * This is intentionally not a waveform processor. It resolves the text that
 * should be synthesized, then delegates audio generation to Android TTS. It
 * remains deterministic, allocation-light, and usable from commonMain.
 */
object JapanesePronunciationEngine {

    fun resolve(request: JapaneseSpeechRequest): JapaneseSpeechPlan? {
        val displayText = JapaneseSpeechText.normalize(request.displayText)
        if (displayText.isEmpty()) return null

        val explicitReading = request.pronunciation
            ?.let(JapaneseSpeechText::normalize)
            ?.takeIf { it.isNotEmpty() }
        val context = request.context.takeUnless { it == JapaneseSpeechContext.Auto }
            ?: if (displayText.length == 1 && isKanji(displayText[0])) {
                JapaneseSpeechContext.IsolatedKanji
            } else {
                JapaneseSpeechContext.Sentence
            }

        return when (context) {
            JapaneseSpeechContext.IsolatedKanji -> resolveIsolatedKanji(
                request = request,
                displayText = displayText,
                explicitReading = explicitReading
            )

            JapaneseSpeechContext.Grammar,
            JapaneseSpeechContext.Vocabulary,
            JapaneseSpeechContext.Sentence,
            JapaneseSpeechContext.Kana -> resolveContextual(
                request = request,
                displayText = displayText,
                explicitReading = explicitReading
            )

            JapaneseSpeechContext.Auto -> error("Auto context must be resolved before dispatch")
        }
    }

    private fun resolveIsolatedKanji(
        request: JapaneseSpeechRequest,
        displayText: String,
        explicitReading: String?
    ): JapaneseSpeechPlan {
        val standaloneExplicit = explicitReading
            ?.takeIf(JapaneseSpeechText::isStandaloneKanjiReading)
            ?.let(JapaneseSpeechText::normalizeKanjiReading)
        if (standaloneExplicit != null) {
            return plan(displayText, standaloneExplicit, request.language, JapaneseSpeechStrategy.ExplicitReading)
        }

        val standaloneKun = request.kunReadings
            .asSequence()
            .filter(JapaneseSpeechText::isStandaloneKanjiReading)
            .map(JapaneseSpeechText::normalizeKanjiReading)
            .distinct()
            .firstOrNull()
        if (standaloneKun != null) {
            return plan(displayText, standaloneKun, request.language, JapaneseSpeechStrategy.StandaloneKun)
        }

        val standaloneOn = request.onReadings
            .asSequence()
            .filter(JapaneseSpeechText::isStandaloneKanjiReading)
            .map(JapaneseSpeechText::normalizeKanjiReading)
            .distinct()
            .firstOrNull()
        if (standaloneOn != null) {
            return plan(displayText, standaloneOn, request.language, JapaneseSpeechStrategy.StandaloneOn)
        }

        return plan(
            displayText = displayText,
            speakText = displayText,
            language = request.language,
            strategy = JapaneseSpeechStrategy.RawJapaneseFallback
        )
    }

    private fun resolveContextual(
        request: JapaneseSpeechRequest,
        displayText: String,
        explicitReading: String?
    ): JapaneseSpeechPlan {
        if (explicitReading != null) {
            return plan(displayText, explicitReading, request.language, JapaneseSpeechStrategy.ExplicitReading)
        }

        val furiganaReading = request.furigana
            ?.takeIf { it.compounds.any { compound -> !compound.annotation.isNullOrBlank() } }
            ?.toKanaReading()
            ?.let(JapaneseSpeechText::normalize)
            ?.takeIf { it.isNotEmpty() }
        if (furiganaReading != null) {
            return plan(displayText, furiganaReading, request.language, JapaneseSpeechStrategy.Furigana)
        }

        return plan(
            displayText = displayText,
            speakText = displayText,
            language = request.language,
            strategy = JapaneseSpeechStrategy.RawJapaneseFallback
        )
    }

    private fun plan(
        displayText: String,
        speakText: String,
        language: String,
        strategy: JapaneseSpeechStrategy
    ): JapaneseSpeechPlan {
        return JapaneseSpeechPlan(
            displayText = displayText,
            speakText = JapaneseSpeechText.normalize(speakText),
            language = language,
            strategy = strategy
        )
    }


    private fun isKanji(character: Char): Boolean {
        return character in '\u3400'..'\u4DBF' ||
            character in '\u4E00'..'\u9FFF' ||
            character in '\uF900'..'\uFAFF'
    }
}
