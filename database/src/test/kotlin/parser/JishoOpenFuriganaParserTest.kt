package parser

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals


class JishoOpenFuriganaParserTest {

    @Test
    fun parsesSegmentedRowsAndSkipsMalformedRows() {
        val file = File.createTempFile("kaiteyo-furigana", ".txt")
        try {
            file.writeText(
                """
                # comment
                100;食べ.る;た.べる
                malformed
                101;猫;ねこ
                """.trimIndent()
            )

            val items = buildList {
                JishoOpenFuriganaParser.forEach(file, ::add)
            }

            assertEquals(2, items.size)
            assertEquals(100L, items[0].jmdictId)
            assertEquals("食べる", items[0].surface)
            assertEquals("たべる", items[0].reading)
            assertEquals("食べ", items[0].segments[0].ruby)
            assertEquals("た", items[0].segments[0].rt)
            assertEquals("る", items[0].segments[1].ruby)
            assertEquals("べる", items[0].segments[1].rt)
            assertEquals("ねこ", items[1].segments[0].rt)
        } finally {
            file.delete()
        }
    }
}
