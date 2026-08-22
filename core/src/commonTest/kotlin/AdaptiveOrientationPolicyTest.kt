package ua.syt0r.kanji.presentation

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import kotlin.test.Test
import kotlin.test.assertEquals
import ua.syt0r.kanji.presentation.common.ui.Orientation

class AdaptiveOrientationPolicyTest {

    @Test
    fun compactWindowRemainsPortrait() {
        assertEquals(
            Orientation.Portrait,
            resolveAppOrientation(
                widthSizeClass = WindowWidthSizeClass.Compact,
                heightSizeClass = WindowHeightSizeClass.Medium,
            )
        )
    }

    @Test
    fun expandedTabletPortraitRemainsPortrait() {
        assertEquals(
            Orientation.Portrait,
            resolveAppOrientation(
                widthSizeClass = WindowWidthSizeClass.Expanded,
                heightSizeClass = WindowHeightSizeClass.Expanded,
            )
        )
    }

    @Test
    fun expandedWidthMediumHeightUsesLandscapeThemeOrientation() {
        assertEquals(
            Orientation.Landscape,
            resolveAppOrientation(
                widthSizeClass = WindowWidthSizeClass.Expanded,
                heightSizeClass = WindowHeightSizeClass.Medium,
            )
        )
    }

    @Test
    fun wideShortWindowUsesLandscapeThemeOrientation() {
        assertEquals(
            Orientation.Landscape,
            resolveAppOrientation(
                widthSizeClass = WindowWidthSizeClass.Expanded,
                heightSizeClass = WindowHeightSizeClass.Compact,
            )
        )
    }
}
