package ua.syt0r.kanji.core.grammar

import kotlin.random.Random

private val japaneseTokenRegex = Regex("[一-龯々ぁ-んァ-ヶー]+|[０-９0-9]+|[。、！？!?]|[^\\s]")
private val speakerPrefixRegex = Regex("^\\s*([A-Za-zＡ-Ｚａ-ｚ])[.．:：]\\s*")
private val inlineBracketTranslationRegex = Regex("[（(]\\s*([A-Za-z][^）)]*)[）)]")
private val postSentenceTranslationRegex = Regex("[。！？!?]\\s*([A-Za-z].*)$")
private val japaneseSegmentRegex = Regex("[ぁ-んァ-ヶ一-龯々ー０-９0-9\\s、。！？!?「」『』]+")

/** A normalized example line used by every grammar question mode. */
data class GrammarExample(
    val japanese: String,
    val meaning: String,
    val speaker: String?,
)

data class ClozeQuestion(
    val sentence: String,
    val meaning: String,
    val answer: String,
    val options: List<String>,
)

data class ConjugationQuestion(
    val dictionaryForm: String,
    val meaning: String,
    val answer: String,
    val syllables: List<String>,
)

data class ScrambleQuestion(
    val sentence: String,
    val meaning: String,
    val tokens: List<String>,
)

data class DialogueQuestion(
    val context: String,
    val lines: List<Pair<String, String>>,
    val answer: String,
    val options: List<String>,
)

class GrammarQuestionEngine {

    fun supportsCloze(point: GrammarPoint): Boolean = examples(point).any { example ->
        findClozeAnswer(point, tokenize(example.japanese)) != null
    }

    fun supportsConjugation(point: GrammarPoint): Boolean = targetConjugation(point.formulaTitle) != null

    fun supportsScramble(point: GrammarPoint): Boolean = examples(point).any { example ->
        tokenize(example.japanese).count { token -> token.any(::isJapanese) } >= 3
    }

    fun supportsDialogue(point: GrammarPoint): Boolean = examples(point).size >= 2

    fun examples(point: GrammarPoint): List<GrammarExample> = point.examples.flatMap(::parseExample)
        .filter { it.japanese.any(::isJapanese) }

    fun cloze(point: GrammarPoint, seed: Int): ClozeQuestion? {
        val parsed = examples(point)
        val candidate = parsed.asSequence()
            .mapNotNull { example ->
                val tokens = tokenize(example.japanese)
                val answer = findClozeAnswer(point, tokens) ?: return@mapNotNull null
                Triple(example, tokens, answer)
            }
            .toList()
            .takeIf { it.isNotEmpty() }
            ?.let { it[positive(seed, it.size)] }
            ?: return null

        val (example, tokens, answer) = candidate
        val options = buildOptions(
            answer = answer.value,
            pool = clozeDistractorPool(point.formulaTitle),
            seed = seed + 17,
        ) ?: return null
        val clozeTokens = tokens.toMutableList().apply { set(answer.index, "____") }
        return ClozeQuestion(
            sentence = renderTokens(clozeTokens, example.japanese),
            meaning = example.meaning.ifBlank { point.meaning },
            answer = answer.value,
            options = options,
        ).takeIf(GrammarQuestionValidator::cloze)
    }

    fun conjugation(point: GrammarPoint, seed: Int): ConjugationQuestion? {
        val target = targetConjugation(point.formulaTitle) ?: return null
        val verb = VERB_LEXICON[positive(seed, VERB_LEXICON.size)]
        val answer = conjugate(verb, target) ?: return null
        val distractors = listOf("ま", "す", "て", "た", "な", "い", "る", "で", "よ", "う")
            .filterNot { answer.contains(it) }
        val syllables = seededShuffle(
            answer.map { it.toString() } + seededShuffle(distractors, seed + 31).take(3),
            seed + 43,
        )
        return ConjugationQuestion(
            dictionaryForm = verb.dictionary,
            meaning = verb.meaning,
            answer = answer,
            syllables = syllables,
        ).takeIf(GrammarQuestionValidator::conjugation)
    }

    fun scramble(point: GrammarPoint, seed: Int): ScrambleQuestion? {
        val candidates = examples(point).mapNotNull { example ->
            val tokens = tokenize(example.japanese)
            if (tokens.count { token -> token.any(::isJapanese) } < 3) null else example to tokens
        }
        val (example, tokens) = candidates.takeIf { it.isNotEmpty() }?.let { it[positive(seed, it.size)] }
            ?: return null
        return ScrambleQuestion(
            sentence = renderTokens(tokens, example.japanese),
            meaning = example.meaning.ifBlank { point.meaning },
            tokens = seededShuffle(tokens, seed + 59),
        ).takeIf(GrammarQuestionValidator::scramble)
    }

    fun dialogue(point: GrammarPoint, seed: Int): DialogueQuestion? {
        val parsed = examples(point)
        if (parsed.isEmpty()) return null
        val ordered = seededShuffle(parsed, seed + 71)
        val answerExample = ordered.firstOrNull { it.japanese.length >= 2 } ?: return null
        val contextExample = ordered.firstOrNull { it.japanese != answerExample.japanese }
        val answer = answerExample.japanese
        val options = buildOptions(
            answer = answer,
            pool = ordered.map { it.japanese }.filter { it != answer },
            seed = seed + 73,
        ) ?: return null
        val prompt = contextExample?.japanese ?: point.meaning
        return DialogueQuestion(
            context = "Gunakan pola: ${GrammarMarkup.plainText(point.formulaTitle)}",
            lines = listOf(
                "Sensei" to prompt,
                "You" to "…",
            ),
            answer = answer,
            options = options,
        ).takeIf(GrammarQuestionValidator::dialogue)
    }

    fun tokenize(sentence: String): List<String> {
        val normalized = cleanSpeaker(sentence)
        return if (normalized.any { it == ' ' || it == '\u3000' }) {
            normalized.split(Regex("[\\s\\u3000]+"))
                .filter { it.isNotBlank() }
        } else {
            splitUnspacedJapanese(normalized)
        }
    }

    private fun splitUnspacedJapanese(text: String): List<String> = buildList {
        japaneseTokenRegex.findAll(text).forEach { match ->
            val rawToken = match.value
            if (!rawToken.any(::isJapanese)) {
                add(rawToken)
                return@forEach
            }
            val current = StringBuilder()
            var index = 0
            while (index < rawToken.length) {
                val particle = PARTICLES
                    .asSequence()
                    .sortedByDescending(String::length)
                    .firstOrNull { candidate ->
                        current.isNotEmpty() &&
                            rawToken.startsWith(candidate, index) &&
                            !(candidate == "で" && rawToken.startsWith("です", index))
                    }
                if (particle == null) {
                    current.append(rawToken[index])
                    index += 1
                } else {
                    addJapaneseLexeme(current.toString())
                    add(particle)
                    current.clear()
                    index += particle.length
                }
            }
            addJapaneseLexeme(current.toString())
        }
    }

    private fun MutableList<String>.addJapaneseLexeme(lexeme: String) {
        if (lexeme.isBlank()) return
        val ending = FORM_ENDINGS.firstOrNull { candidate ->
            lexeme.length > candidate.length && lexeme.endsWith(candidate)
        }
        if (ending == null) {
            add(lexeme)
        } else {
            add(lexeme.dropLast(ending.length))
            add(ending)
        }
    }

    fun renderTokens(tokens: List<String>, original: String): String =
        if (original.any { it == ' ' || it == '\u3000' }) tokens.joinToString(" ") else tokens.joinToString("")

    fun cleanSpeaker(text: String): String = text
        .lineSequence()
        .map(::japaneseOnlyLine)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .replace(Regex("\\s+"), " ")
        .trim()

    fun isJapanese(char: Char): Boolean =
        char in '\u3040'..'\u30ff' || char in '\u3400'..'\u9fff' || char in '\uff66'..'\uff9f'

    private fun parseExample(raw: String): List<GrammarExample> {
        val lines = raw.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()
        val fallbackMeaning = lines.filterNot { line -> line.any(::isJapanese) }.joinToString(" ")
        return lines.mapNotNull { line ->
            val japanese = japaneseOnlyLine(line)
            if (japanese.isBlank()) return@mapNotNull null
            val speaker = speakerPrefixRegex.find(line)?.groupValues?.getOrNull(1)
            GrammarExample(
                japanese = japanese,
                meaning = inlineMeaning(line).ifBlank { fallbackMeaning },
                speaker = speaker,
            )
        }
    }

    private fun japaneseOnlyLine(line: String): String {
        val withoutSpeaker = speakerPrefixRegex.replace(line.trim(), "")
        val withoutBracketTranslation = inlineBracketTranslationRegex.replace(withoutSpeaker, "")
        val postSentenceTranslation = postSentenceTranslationRegex.find(withoutBracketTranslation)
        val candidate = if (postSentenceTranslation == null) {
            withoutBracketTranslation
        } else {
            withoutBracketTranslation.substring(0, postSentenceTranslation.range.first + 1)
        }
        return japaneseSegmentRegex.findAll(candidate)
            .map { it.value.trim() }
            .filter { it.any(::isJapanese) }
            .maxByOrNull { segment -> segment.count(::isJapanese) }
            ?.trim()
            .orEmpty()
    }

    private fun inlineMeaning(line: String): String {
        val withoutSpeaker = speakerPrefixRegex.replace(line.trim(), "")
        inlineBracketTranslationRegex.find(withoutSpeaker)?.let { return it.groupValues[1].trim() }
        postSentenceTranslationRegex.find(withoutSpeaker)?.let { return it.groupValues[1].trim() }
        return ""
    }

    private data class ClozeAnswer(val index: Int, val value: String)

    private fun findClozeAnswer(point: GrammarPoint, tokens: List<String>): ClozeAnswer? {
        if (tokens.isEmpty()) return null
        val formula = GrammarMarkup.plainText(point.formulaTitle)
        val formulaParticles = PARTICLES.filter { particle ->
            Regex("(^|[\\s＋+／])${Regex.escape(particle)}($|[\\s／])").containsMatchIn(formula)
        }
        val struckCandidates = Regex("~~([^~]+)~~")
            .findAll(point.formulaTitle)
            .map { it.groupValues[1].trim() }
            .toList()
        val candidates = (formulaParticles + struckCandidates).distinct()
        return candidates.asSequence()
            .flatMap { candidate -> tokens.withIndex().asSequence().filter { it.value == candidate } }
            .map { ClozeAnswer(it.index, it.value) }
            .firstOrNull()
    }

    private fun clozeDistractorPool(formula: String): List<String> {
        val normalized = GrammarMarkup.plainText(formula)
        val family = PARTICLES.filter { particle -> normalized.contains(particle) }
        return (family + PARTICLES).distinct()
    }

    private fun buildOptions(answer: String, pool: List<String>, seed: Int): List<String>? {
        val distractors = seededShuffle(pool.filter { it != answer }.distinct(), seed)
            .take(3)
        if (distractors.size < 3 || answer.isBlank()) return null
        return seededShuffle(distractors + answer, seed + 1)
    }

    private fun targetConjugation(formula: String): ConjugationTarget? {
        val normalized = GrammarMarkup.plainText(formula)
        val marker = when {
            formula.contains("~~ます~~") -> "masu"
            formula.contains("~~ない~~") -> "nai"
            else -> null
        } ?: return null
        val suffix = when {
            marker == "masu" && normalized.contains("て") -> ConjugationTarget.Te
            marker == "masu" && normalized.contains("で") -> ConjugationTarget.Te
            marker == "masu" && normalized.contains("たら") -> ConjugationTarget.Tara
            marker == "masu" && normalized.contains("たり") -> ConjugationTarget.Tari
            marker == "masu" && normalized.contains("た") -> ConjugationTarget.Ta
            marker == "masu" && normalized.contains("る") -> ConjugationTarget.Dictionary
            marker == "nai" && normalized.contains("で") -> ConjugationTarget.NaiDe
            marker == "nai" -> ConjugationTarget.Nai
            else -> null
        }
        return suffix
    }

    private fun conjugate(verb: VerbEntry, target: ConjugationTarget): String? = when (target) {
        ConjugationTarget.Dictionary -> verb.dictionary
        ConjugationTarget.Masu -> verb.masu
        ConjugationTarget.Nai -> verb.nai
        ConjugationTarget.NaiDe -> verb.nai + "で"
        ConjugationTarget.Te -> verb.te
        ConjugationTarget.Ta -> verb.ta
        ConjugationTarget.Tara -> verb.ta + "ら"
        ConjugationTarget.Tari -> verb.ta + "り"
    }

    private fun positive(seed: Int, size: Int): Int = ((seed % size) + size) % size

    private fun <T> seededShuffle(values: List<T>, seed: Int): List<T> = values.shuffled(Random(seed))

    private enum class ConjugationTarget { Dictionary, Masu, Nai, NaiDe, Te, Ta, Tara, Tari }

    private data class VerbEntry(
        val dictionary: String,
        val meaning: String,
        val masu: String,
        val nai: String,
        val te: String,
        val ta: String,
    )

    private companion object {
        val PARTICLES = listOf("は", "が", "を", "に", "へ", "で", "と", "も", "の", "から", "まで", "より", "や")
        val FORM_ENDINGS = listOf("しました", "します", "でした", "ます", "ません", "です", "ない", "た", "て")

        val VERB_LEXICON = listOf(
            godan("いく", "Pergi"), godan("かく", "Menulis"), godan("きく", "Mendengar/bertanya"),
            godan("よむ", "Membaca"), godan("のむ", "Minum"), godan("はなす", "Berbicara"),
            godan("あう", "Bertemu"), godan("かう", "Membeli"), godan("まつ", "Menunggu"),
            godan("あそぶ", "Bermain"), godan("しぬ", "Mati"), godan("はいる", "Masuk"),
            godan("つくる", "Membuat"), godan("つかう", "Menggunakan"), godan("はたらく", "Bekerja"),
            ichidan("たべる", "Makan"), ichidan("みる", "Melihat"), ichidan("おきる", "Bangun"),
            ichidan("ねる", "Tidur"), irregular("する", "Melakukan"), irregular("くる", "Datang"),
        )

        fun godan(dictionary: String, meaning: String): VerbEntry {
            val ending = dictionary.last()
            val stem = dictionary.dropLast(1)
            val masuEnding = mapOf('う' to 'い', 'つ' to 'ち', 'る' to 'り', 'む' to 'み', 'ぶ' to 'び', 'ぬ' to 'に', 'く' to 'き', 'ぐ' to 'ぎ', 'す' to 'し')[ending]
                ?: return ichidan(dictionary, meaning)
            val naiEnding = mapOf('う' to 'わ', 'つ' to 'た', 'る' to 'ら', 'む' to 'ま', 'ぶ' to 'ば', 'ぬ' to 'な', 'く' to 'か', 'ぐ' to 'が', 'す' to 'さ')[ending]
                ?: ending
            val te = when (ending) {
                'う', 'つ', 'る' -> stem + "って"
                'む', 'ぶ', 'ぬ' -> stem + "んで"
                'く' -> if (dictionary == "いく") stem + "って" else stem + "いて"
                'ぐ' -> stem + "いで"
                'す' -> stem + "して"
                else -> dictionary
            }
            val ta = if (te.endsWith("で")) te.dropLast(1) + "だ" else te.dropLast(1) + "た"
            return VerbEntry(dictionary, meaning, stem + masuEnding + "ます", stem + naiEnding + "ない", te, ta)
        }

        fun ichidan(dictionary: String, meaning: String): VerbEntry {
            val stem = dictionary.dropLast(1)
            return VerbEntry(dictionary, meaning, stem + "ます", stem + "ない", stem + "て", stem + "た")
        }

        fun irregular(dictionary: String, meaning: String): VerbEntry = when (dictionary) {
            "する" -> VerbEntry(dictionary, meaning, "します", "しない", "して", "した")
            "くる" -> VerbEntry(dictionary, meaning, "きます", "こない", "きて", "きた")
            else -> ichidan(dictionary, meaning)
        }
    }
}
