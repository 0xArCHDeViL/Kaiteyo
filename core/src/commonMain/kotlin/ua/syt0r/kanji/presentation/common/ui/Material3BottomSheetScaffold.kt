package ua.syt0r.kanji.presentation.common.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Common bottom sheet scaffold.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3BottomSheetScaffold(
    scaffoldState: BottomSheetScaffoldState,
    modifier: Modifier = Modifier,
    sheetContent: @Composable () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetDragHandle = null,
        sheetContent = {
            Surface { sheetContent() }
        },
        modifier = modifier
    ) {

        Surface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) { content() }
        }

    }

}
