import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ua.syt0r.kanji.presentation.screen.main.features.CommandPaletteController
import ua.syt0r.kanji.presentation.screen.main.features.PaletteAction

class CommandPaletteControllerTest {

    @Test
    fun exactAndPrefixMatchesRankBeforeSubstringMatches() {
        val controller = CommandPaletteController()
        controller.setActions(
            listOf(
                PaletteAction(title = "Settings", keywords = "preferences", execute = {}),
                PaletteAction(title = "Search", keywords = "settings", execute = {}),
                PaletteAction(title = "Open Settings", execute = {}),
            )
        )

        controller.updateQuery("settings")

        assertEquals("Settings", controller.filteredActions.first().title)
        assertEquals(3, controller.filteredActions.size)
    }

    @Test
    fun selectionWrapsWithinCurrentFilteredResults() {
        val controller = CommandPaletteController()
        controller.setActions(
            listOf(
                PaletteAction(title = "One", execute = {}),
                PaletteAction(title = "Two", execute = {}),
            )
        )

        controller.selectPrevious()
        assertEquals(1, controller.selectedIndex)
        controller.selectNext()
        assertEquals(0, controller.selectedIndex)
    }

    @Test
    fun executeSelectedInvokesOnlyCurrentAction() {
        val controller = CommandPaletteController()
        var executed = false
        controller.setActions(
            listOf(
                PaletteAction(title = "Run", execute = { executed = true }),
            )
        )

        controller.executeSelected()

        assertTrue(executed)
    }
}
