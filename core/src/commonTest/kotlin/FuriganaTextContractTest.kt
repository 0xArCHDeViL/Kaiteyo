import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.FuriganaStringCompound
import ua.syt0r.kanji.presentation.common.ui.toRenderableFuriganaString

class FuriganaTextContractTest {

    @Test
    fun malformedCompoundsBecomeSafePlainTextWithoutLosingReadableContent() {
        val input = FuriganaString(
            compounds = listOf(
                FuriganaStringCompound(text = "学", annotation = "がく"),
                FuriganaStringCompound(text = "校", annotation = ""),
                FuriganaStringCompound(text = "", annotation = "こう"),
                FuriganaStringCompound(text = "", annotation = ""),
            )
        )

        val renderable = input.toRenderableFuriganaString()

        assertEquals(
            listOf(
                FuriganaStringCompound(text = "学", annotation = "がく"),
                FuriganaStringCompound(text = "校"),
                FuriganaStringCompound(text = "こう"),
            ),
            renderable.compounds,
        )
        assertEquals("学校こう", renderable.compounds.joinToString("") { it.text })
    }

    @Test
    fun renderableCompoundsNeverUseBlankInlineContentInputs() {
        val renderable = FuriganaString(
            compounds = listOf(
                FuriganaStringCompound(text = "", annotation = "読み"),
                FuriganaStringCompound(text = "語", annotation = "ご"),
                FuriganaStringCompound(text = "語", annotation = " "),
            )
        ).toRenderableFuriganaString()

        assertTrue(
            renderable.compounds.all { compound ->
                compound.annotation == null ||
                    (compound.text.isNotBlank() && compound.annotation.isNotBlank())
            }
        )
    }
}
