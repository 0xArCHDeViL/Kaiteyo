import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ua.syt0r.kanji.core.app_data.SearchQueryParser
import ua.syt0r.kanji.core.app_data.SearchScope

class SearchQueryParserTest {

    @Test
    fun parsesEnglishJapaneseAndRomajiTerms() {
        val query = SearchQueryParser.parse("begin 召し上がる kantoku")

        assertEquals(listOf("begin", "召し上がる", "kantoku"), query.terms.map { it.value })
        assertEquals(SearchScope.Words, query.scope)
        assertTrue(query.tags.isEmpty())
    }

    @Test
    fun preservesWildcardOperators() {
        val query = SearchQueryParser.parse("??直*")

        assertEquals("??直*", query.terms.single().value)
        assertEquals("??直*", query.terms.single().normalized)
    }

    @Test
    fun parsesQuotedTermsAndTags() {
        val query = SearchQueryParser.parse("\"power\" #common #v5b #!vt")

        assertTrue(query.terms.single().quoted)
        assertEquals("power", query.terms.single().value)
        assertEquals(
            listOf("common", "v5b", "vt"),
            query.tags.map { it.value }
        )
        assertFalse(query.tags[0].negated)
        assertTrue(query.tags[2].negated)
    }

    @Test
    fun parsesSearchScopes() {
        assertEquals(SearchScope.Kanji, SearchQueryParser.parse("国連安保理 #k").scope)
        assertEquals(SearchScope.Components, SearchQueryParser.parse("冂虫 #c").scope)
        assertEquals(SearchScope.Names, SearchQueryParser.parse("eiji #name").scope)
    }
}
