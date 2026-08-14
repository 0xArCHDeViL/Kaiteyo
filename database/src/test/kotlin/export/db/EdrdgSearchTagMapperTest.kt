package export.db

import kotlin.test.Test
import kotlin.test.assertContains

class EdrdgSearchTagMapperTest {

    @Test
    fun mapsEdrdgVerbLabelsToCanonicalSearchTags() {
        assertContains(
            EdrdgSearchTagMapper.canonicalPartOfSpeechTags("Godan verb with 'bu' ending"),
            "v5b"
        )
        assertContains(
            EdrdgSearchTagMapper.canonicalPartOfSpeechTags("transitive verb"),
            "vt"
        )
        assertContains(
            EdrdgSearchTagMapper.canonicalPartOfSpeechTags("Ichidan verb"),
            "v1"
        )
        assertContains(
            EdrdgSearchTagMapper.canonicalPartOfSpeechTags("suru verb - special class"),
            "vs"
        )
    }

    @Test
    fun mapsCommonEdrdgCategoriesToStableShortTags() {
        assertContains(
            EdrdgSearchTagMapper.canonicalPartOfSpeechTags("noun (common) (futsuumeishi)"),
            "noun"
        )
        assertContains(
            EdrdgSearchTagMapper.canonicalPartOfSpeechTags("adverb (fukushi)"),
            "adv"
        )
        assertContains(
            EdrdgSearchTagMapper.canonicalPartOfSpeechTags("adjective (keiyoushi)"),
            "adj"
        )
        assertContains(
            EdrdgSearchTagMapper.canonicalPartOfSpeechTags("expressions (phrases, clauses, etc.)"),
            "expression"
        )
    }
}
