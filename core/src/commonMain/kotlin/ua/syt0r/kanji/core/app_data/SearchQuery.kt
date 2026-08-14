package ua.syt0r.kanji.core.app_data

import ua.syt0r.kanji.core.japanese.kanaToRomaji

/** Structured, backend-neutral representation of the search box input. */
data class SearchQuery(
    val raw: String,
    val terms: List<SearchTerm>,
    val tags: List<SearchTag>,
    val scope: SearchScope
) {
    val isEmpty: Boolean
        get() = terms.isEmpty() && tags.isEmpty()
}

data class SearchTerm(
    val value: String,
    val normalized: String,
    val romajiNormalized: String,
    val romajiVariants: List<String>,
    val quoted: Boolean
)

data class SearchTag(
    val value: String,
    val negated: Boolean
)

enum class SearchScope {
    Words,
    Kanji,
    Components,
    Names
}

object SearchQueryParser {

    fun parse(input: String): SearchQuery {
        val tokens = tokenize(input)
        var scope = SearchScope.Words
        val terms = ArrayList<SearchTerm>(tokens.size)
        val tags = ArrayList<SearchTag>()

        tokens.forEach { token ->
            if (token.value.startsWith('#') && token.value.length > 1) {
                val directive = token.value.drop(1)
                when (directive.lowercase()) {
                    "k", "kanji" -> scope = SearchScope.Kanji
                    "c", "component", "components" -> scope = SearchScope.Components
                    "name", "names" -> scope = SearchScope.Names
                    else -> {
                        val negated = directive.startsWith('!')
                        val tag = directive.removePrefix("!").trim()
                        if (tag.isNotEmpty()) tags += SearchTag(tag.lowercase(), negated)
                    }
                }
            } else if (token.value.isNotBlank()) {
                val normalized = normalize(token.value)
                terms += SearchTerm(
                    value = token.value,
                    normalized = token.value.lowercase(),
                    romajiNormalized = normalized,
                    romajiVariants = romajiVariants(normalized),
                    quoted = token.quoted
                )
            }
        }

        return SearchQuery(
            raw = input,
            terms = terms,
            tags = tags,
            scope = scope
        )
    }

    private data class Token(val value: String, val quoted: Boolean)

    private fun tokenize(input: String): List<Token> {
        val result = ArrayList<Token>()
        val token = StringBuilder()
        var quoted = false
        var tokenWasQuoted = false

        fun flush() {
            if (token.isNotEmpty()) {
                result += Token(token.toString(), tokenWasQuoted)
                token.clear()
                tokenWasQuoted = false
            }
        }

        input.forEach { character ->
            when {
                character == '"' -> {
                    quoted = !quoted
                    tokenWasQuoted = true
                }
                character.isWhitespace() && !quoted -> flush()
                else -> token.append(character)
            }
        }
        flush()
        return result
    }

    /** Keep wildcard operators intact while making romaji and English matching case-insensitive. */
    internal fun normalize(value: String): String = value
        .kanaToRomaji()
        .lowercase()

    /**
     * Jisho-style search accepts common Hepburn/Kunrei/Nihon-shiki variants. The database
     * stores a canonical Wanakana form, so query-time aliases are cheaper and safer than
     * duplicating every indexed row. Variants are generated per mora rather than with global
     * string replacement: `shufu` must not become the invalid `sfufu` when `fu`/`hu` aliases
     * are expanded.
     */
    internal fun romajiVariants(value: String): List<String> {
        val lower = value.lowercase()
        if (lower.isEmpty()) return emptyList()

        val variants = linkedSetOf(lower)
        var partials = listOf("")
        var index = 0
        while (index < lower.length) {
            val aliasMatch = romajiAliases
                .asSequence()
                .flatMap { alias -> alias.forms.asSequence().map { form -> alias to form } }
                .filter { (_, form) -> lower.regionMatches(index, form, 0, form.length) }
                .maxByOrNull { (_, form) -> form.length }

            if (aliasMatch == null) {
                val character = lower[index].toString()
                partials = partials.map { it + character }
                index += 1
            } else {
                val (alias, matchedForm) = aliasMatch
                partials = partials
                    .flatMap { prefix -> alias.forms.map { prefix + it } }
                    .distinct()
                    .take(MaxRomajiVariants)
                index += matchedForm.length
            }
        }
        variants.addAll(partials)
        return variants.take(MaxRomajiVariants)
    }

    private data class RomajiAlias(val forms: List<String>)

    private val romajiAliases = listOf(
        RomajiAlias(listOf("sha", "sya")),
        RomajiAlias(listOf("shu", "syu")),
        RomajiAlias(listOf("sho", "syo")),
        RomajiAlias(listOf("shi", "si")),
        RomajiAlias(listOf("cha", "tya")),
        RomajiAlias(listOf("chu", "tyu")),
        RomajiAlias(listOf("cho", "tyo")),
        RomajiAlias(listOf("chi", "ti")),
        RomajiAlias(listOf("tsu", "tu")),
        RomajiAlias(listOf("fu", "hu")),
        RomajiAlias(listOf("ja", "zya")),
        RomajiAlias(listOf("ju", "zyu")),
        RomajiAlias(listOf("jo", "zyo")),
        RomajiAlias(listOf("ji", "zi"))
    )

    private const val MaxRomajiVariants = 32
}
