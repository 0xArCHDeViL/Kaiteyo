import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ua.syt0r.kanji.core.app_data.data.KanjiReadingData
import ua.syt0r.kanji.core.app_data.data.ReadingType
import ua.syt0r.kanji.core.connected_learning.GraphNodeKind
import ua.syt0r.kanji.presentation.screen.main.features.MindMapCatalogItem
import ua.syt0r.kanji.presentation.screen.main.features.MindMapExplorerMode

class MindMapExplorerContractTest {

    @Test
    fun radicalAndComponentModesRemainDistinct() {
        val radical = MindMapCatalogItem(
            key = "心",
            label = "心",
            mode = MindMapExplorerMode.RADICALS,
            relatedKanjiCount = 12,
            strokeCount = 4,
        )
        val component = MindMapCatalogItem(
            key = "component:%E5%BF%83",
            label = "心",
            mode = MindMapExplorerMode.COMPONENTS,
            relatedKanjiCount = 0,
            nodeKind = GraphNodeKind.COMPONENT,
        )

        assertEquals(MindMapExplorerMode.RADICALS, radical.mode)
        assertEquals(MindMapExplorerMode.COMPONENTS, component.mode)
        assertTrue(radical.key != component.key)
        assertEquals(GraphNodeKind.COMPONENT, component.nodeKind)
    }

    @Test
    fun readingsRemainTypedAndSeparatedForAudioRequests() {
        val readings = listOf(
            KanjiReadingData("ハツ", ReadingType.ON),
            KanjiReadingData("はな", ReadingType.KUN),
        )

        assertEquals(listOf("ハツ"), readings.filter { it.type == ReadingType.ON }.map { it.reading })
        assertEquals(listOf("はな"), readings.filter { it.type == ReadingType.KUN }.map { it.reading })
    }
}
