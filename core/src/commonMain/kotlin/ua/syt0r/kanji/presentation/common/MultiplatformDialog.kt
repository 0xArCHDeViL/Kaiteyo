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
                    .widthIn(min = Dimens.CardMinWidth, max = Dimens.CardMaxWidth + 60.dp)
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Space4)
                    .shadow(
                        elevation = Dimens.ElevationXl,
                        shape = shape,
                        ambientColor = accent.primary.copy(alpha = Dimens.Alpha.Subtle),
                        spotColor = accent.primary.copy(alpha = Dimens.Alpha.Light)
                    )
                    .clip(shape)
                    .border(
                        width = Dimens.ElevationXs,
                        color = surfaceColors.border.copy(alpha = Dimens.Alpha.Medium),
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
                .padding(top = Dimens.Space5, bottom = Dimens.Space3)
        ) {

            Box(
                modifier = Modifier
                    .padding(horizontal = Dimens.Space6)
                    .padding(bottom = Dimens.Space3)
            ) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.titleLarge
                ) {
                    title()
                }
            }

            val contentScrollState = rememberScrollState()

            val visibleDividerColor = LocalSurfaceColors.current.border.copy(alpha = Dimens.Alpha.Medium)
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
                    .padding(horizontal = if (paddedContent) Dimens.Space6 else 0.dp)
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
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space3, Alignment.End),
                modifier = Modifier
                    .height(IntrinsicSize.Max)
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Space6)
                    .padding(top = Dimens.Space3)
            ) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                    buttons()
                }
            }

        }

    }

}