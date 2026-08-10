package ua.syt0r.kanji.presentation.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.common.theme.LocalKaiteyoAccent
import ua.syt0r.kanji.presentation.common.theme.LocalSurfaceColors

// ============================================
// ELEGAN MULTIPLATFORM DIALOG SYSTEM
// Floating Island Dialog with Responsive Widths,
// Soft Ambient Shadows, and Glassmorphic Borders
// ============================================

@Composable
fun MultiplatformDialog(
    onDismissRequest: () -> Unit,
    containerColor: Color = LocalSurfaceColors.current.surfaceElevated,
    content: @Composable () -> Unit,
) {
    val accent = LocalKaiteyoAccent.current
    val surfaceColors = LocalSurfaceColors.current
    val shape = RoundedCornerShape(Dimens.Radius2xl)

    Dialog(
        onDismissRequest = onDismissRequest,
        content = {
            Surface(
                modifier = Modifier
                    .widthIn(min = 320.dp, max = 460.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = shape,
                        ambientColor = accent.primary.copy(alpha = 0.08f),
                        spotColor = accent.primary.copy(alpha = 0.15f)
                    )
                    .clip(shape)
                    .border(
                        width = 1.dp,
                        color = surfaceColors.border.copy(alpha = 0.3f),
                        shape = shape
                    ),
                color = containerColor,
                shape = shape
            ) {
                content()
            }
        }
    )
}

@Composable
fun MultiplatformDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    buttons: @Composable RowScope.() -> Unit,
    contentVerticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    paddedContent: Boolean = true
) = ExperimentalMultiplatformDialog(
    onDismissRequest,
    title,
    content,
    buttons,
    contentVerticalArrangement,
    paddedContent
)

@Composable
fun ExperimentalMultiplatformDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    buttons: @Composable RowScope.() -> Unit,
    contentVerticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    paddedContent: Boolean = true
) {

    MultiplatformDialog(
        onDismissRequest = onDismissRequest
    ) {

        Column(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .padding(top = 22.dp, bottom = 14.dp)
        ) {

            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 14.dp)
            ) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.titleLarge
                ) {
                    title()
                }
            }

            val contentScrollState = rememberScrollState()

            val visibleDividerColor = LocalSurfaceColors.current.border.copy(alpha = 0.3f)
            val hiddenDividerColor = Color.Transparent

            val topDividerColor = animateColorAsState(
                targetValue = when {
                    contentScrollState.canScrollBackward -> visibleDividerColor
                    else -> hiddenDividerColor
                }
            )

            HorizontalDivider(color = topDividerColor.value)

            Column(
                modifier = Modifier
                    .padding(horizontal = if (paddedContent) 24.dp else 0.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(contentScrollState),
                verticalArrangement = contentVerticalArrangement
            ) {
                content()
            }

            val bottomDividerColor = animateColorAsState(
                targetValue = when {
                    contentScrollState.canScrollForward -> visibleDividerColor
                    else -> hiddenDividerColor
                }
            )

            HorizontalDivider(color = bottomDividerColor.value)

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                modifier = Modifier
                    .height(IntrinsicSize.Max)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp)
            ) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                    buttons()
                }
            }

        }

    }

}