package parser

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class StreamingJMdictParserTest {

    @Test
    fun readsEveryEntryWhenUsingUnfilteredTraversal() {
        val file = File.createTempFile("kaiteyo-jmdict", ".xml")
        try {
            file.writeText(
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <JMdict>
                  <entry>
                    <ent_seq>1326160</ent_seq>
                    <k_ele><keb>主婦</keb></k_ele>
                    <r_ele><reb>しゅふ</reb></r_ele>
                    <sense><gloss>housewife</gloss></sense>
                  </entry>
                  <entry>
                    <ent_seq>2069650</ent_seq>
                    <k_ele><keb>主夫</keb></k_ele>
                    <r_ele><reb>しゅふ</reb></r_ele>
                    <sense><gloss>househusband</gloss></sense>
                  </entry>
                </JMdict>
                """.trimIndent()
            )

            val entries = buildList {
                StreamingJMdictParser.forEachEntry(file, ::add)
            }

            assertEquals(listOf(1326160L, 2069650L), entries.map { it.entrySequence })
            assertEquals(listOf("主婦", "主夫"), entries.map { it.kanji.single().expression })
        } finally {
            file.delete()
        }
    }

    @Test
    fun retainsTheExistingSupportedEntryFilter() {
        val file = File.createTempFile("kaiteyo-jmdict-filter", ".xml")
        try {
            file.writeText(
                """
                <JMdict>
                  <entry>
                    <ent_seq>1326160</ent_seq>
                    <k_ele><keb>主婦</keb></k_ele>
                    <r_ele><reb>しゅふ</reb></r_ele>
                    <sense><gloss>housewife</gloss></sense>
                  </entry>
                  <entry>
                    <ent_seq>2069650</ent_seq>
                    <k_ele><keb>主夫</keb></k_ele>
                    <r_ele><reb>しゅふ</reb></r_ele>
                    <sense><gloss>househusband</gloss></sense>
                  </entry>
                </JMdict>
                """.trimIndent()
            )

            val entries = buildList {
                StreamingJMdictParser.forEachSupportedEntry(file, setOf(2069650L), ::add)
            }

            assertEquals(listOf(2069650L), entries.map { it.entrySequence })
            assertEquals("主夫", entries.single().kanji.single().expression)
        } finally {
            file.delete()
        }
    }
}
