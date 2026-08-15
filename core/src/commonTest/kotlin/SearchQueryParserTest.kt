import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ua.syt0r.kanji.core.app_data.SearchQueryParser
import ua.syt0r.kanji.core.app_data.SearchScope
import ua.syt0r.kanji.core.app_data.toGlobPatterns

class SearchQueryParserTest {

    @Test
    fun parsesEnglishJapaneseAndRomajiTerms() {
        val query = SearchQueryParser.parse("begin 召し上がる kantoku")

        assertEquals(listOf("begin", "召し上がる", "kantoku"), query.terms.map { it.value })
        assertEquals(SearchScope.Words, query.scope)
        assertTrue(query.tags.isEmpty())
    }

    @Test
    fun expandsRomajiAliasesForJishoStyleInput() {
        val kana = SearchQueryParser.parse("しゅふ").terms.single()
        val kunrei = SearchQueryParser.parse("syufu").terms.single()

        assertTrue(kana.romajiVariants.contains("shufu"))
        assertTrue(kana.romajiVariants.contains("syufu"))
        assertTrue(kunrei.romajiVariants.contains("shufu"))
        assertTrue(kunrei.romajiVariants.contains("syufu"))
    }

    @Test
    fun expandsMultipleRomajiMoraAliasesWithoutCorruptingWords() {
        val variants = SearchQueryParser.parse("syufu").terms.single().romajiVariants

        assertTrue(variants.contains("syufu"))
        assertTrue(variants.contains("shufu"))
        assertTrue(variants.contains("syuhu"))
        assertFalse(variants.any { it.contains("sfufu") })

        assertTrue(SearchQueryParser.parse("si").terms.single().romajiVariants.contains("shi"))
        assertTrue(SearchQueryParser.parse("ti").terms.single().romajiVariants.contains("chi"))
        assertTrue(SearchQueryParser.parse("tu").terms.single().romajiVariants.contains("tsu"))
    }

    @Test
    fun preservesWildcardOperators() {
        val query = SearchQueryParser.parse("??直*")

        assertEquals("??直*", query.terms.single().value)
        assertEquals("??直*", query.terms.single().normalized)
    }

    @Test
    fun usesSubstringMatchingByDefaultAndExactMatchingForQuotedTerms() {
        val substringPatterns = SearchQueryParser.parse("begin").terms.single().toGlobPatterns()
        val exactPatterns = SearchQueryParser.parse("\"begin\"").terms.single().toGlobPatterns()

        assertTrue(substringPatterns.contains("*begin*"))
        assertEquals(listOf("begin"), exactPatterns)
    }

    @Test
    fun keepsRawJapanesePatternForMixedKanjiAndKanaTerms() {
        val patterns = SearchQueryParser.parse("召し上がる").terms.single().toGlobPatterns()

        assertTrue(patterns.contains("*召し上がる*"))
    }

    @Test
    fun preservesExplicitWildcardBoundaries() {
        val patterns = SearchQueryParser.parse("??直*").terms.single().toGlobPatterns()

        assertEquals(listOf("??直*"), patterns)
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
