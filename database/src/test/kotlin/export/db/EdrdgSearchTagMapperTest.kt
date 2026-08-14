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
}
