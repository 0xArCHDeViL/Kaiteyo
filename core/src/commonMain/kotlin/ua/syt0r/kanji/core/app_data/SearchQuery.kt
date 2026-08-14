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
     * Jisho-style search accepts common Hepburn/Kunrei variants. The database stores a
     * canonical Wanakana form, so query-time aliases are cheaper and safer than duplicating
     * every indexed row.
     */
    internal fun romajiVariants(value: String): List<String> {
        val lower = value.lowercase()
        val canonical = lower
            .replace("sya", "sha")
            .replace("syu", "shu")
            .replace("syo", "sho")
            .replace("tya", "cha")
            .replace("tyu", "chu")
            .replace("tyo", "cho")
            .replace("ti", "chi")
            .replace("tu", "tsu")
            .replace("dya", "ja")
            .replace("dyu", "ju")
            .replace("dyo", "jo")
            .replace("zi", "ji")
            .replace("zya", "ja")
            .replace("zyu", "ju")
            .replace("zyo", "jo")
        val kunrei = canonical
            .replace("sha", "sya")
            .replace("shu", "syu")
            .replace("sho", "syo")
            .replace("cha", "tya")
            .replace("chu", "tyu")
            .replace("cho", "tyo")
            .replace("chi", "ti")
            .replace("tsu", "tu")
            .replace("ja", "zya")
            .replace("ju", "zyu")
            .replace("zyo", "jo")
            .replace("ji", "zi")
        return linkedSetOf(lower, canonical, kunrei).filter(String::isNotEmpty)
    }
}
