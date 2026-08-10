package ua.syt0r.kanji.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import ua.syt0r.kanji.presentation.common.AppListItemDefaults.ListItemDefaultPaddings
import ua.syt0r.kanji.presentation.common.theme.Dimens

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape

object AppListItemDefaults {
    val ExtraPaddings = PaddingValues(
        horizontal = Dimens.Space3,
        vertical = Dimens.Space1
    )
    val ListItemDefaultPaddings = PaddingValues(
        horizontal = Dimens.ContentPaddingSmall,
        vertical = Dimens.Space2
    )
    val ClickableTrailingOffset = Dimens.Space3
}

@Composable
fun AppListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlineContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    paddingValues: PaddingValues = AppListItemDefaults.ExtraPaddings,
    colors: ListItemColors = ListItemDefaults.colors()
) {
    val surfaceColors = ua.syt0r.kanji.presentation.common.theme.LocalSurfaceColors.current
    val shape = MaterialTheme.shapes.large

    ListItem(
        headlineContent = headlineContent,
        modifier = modifier
            .padding(paddingValues)
            .border(Dimens.ElevationXs, surfaceColors.border.copy(alpha = Dimens.Alpha.Light), shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = Dimens.Alpha.Medium))
            .semantics(mergeDescendants = true) { role = Role.Button }
            .clickable(enabled = onClick != null, onClick = onClick ?: {})
            .focusable(enabled = onClick != null),
        overlineContent = overlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = colors
    )

}

@Composable
fun AppListItem(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(Dimens.Space2),
    paddingValues: PaddingValues = AppListItemDefaults.ExtraPaddings,
    rowContent: @Composable RowScope.() -> Unit
) {

    Row(
        modifier = modifier
            .padding(paddingValues)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .semantics(mergeDescendants = true) { role = Role.Button }
            .clickable(enabled = onClick != null, onClick = onClick ?: {})
            .focusable(enabled = onClick != null)
            .padding(ListItemDefaultPaddings),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement
    ) {
        rowContent()
    }

}
