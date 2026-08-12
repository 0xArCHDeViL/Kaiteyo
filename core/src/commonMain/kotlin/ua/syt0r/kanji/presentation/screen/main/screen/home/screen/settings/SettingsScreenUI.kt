package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings

import ua.syt0r.kanji.presentation.common.theme.Dimens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DisplayableEnum
import kotlin.enums.EnumEntries

@Composable
fun SettingsScreenUI(
    state: State<ScreenState>,
    onBackupButtonClick: () -> Unit,
    onAccountButtonClick: () -> Unit,
    onSyncButtonClick: () -> Unit,
    onFeedbackButtonClick: () -> Unit,
    onAboutButtonClick: () -> Unit,
    loadedContent: @Composable ColumnScope.(ScreenState.Loaded) -> Unit
) {
    AnimatedContent(
        state.value,
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { screenState ->
        when (screenState) {
            ScreenState.Loading -> {
                CircularProgressIndicator(Modifier.fillMaxSize().wrapContentSize())
            }
            is ScreenState.Loaded -> {
                SettingsContent {
                    
                    Text(
                        text = "Preferences",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    
                    loadedContent(screenState)
                    
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Data & Sync",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    SettingsAccountButton(onAccountButtonClick)
                    SettingsSyncButton(onSyncButtonClick)
                    SettingsBackupButton(onBackupButtonClick)

                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "More",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    SettingsFeedbackButton(onFeedbackButtonClick)
                    SettingsAboutButton(onAboutButtonClick)
                }
            }
        }
    }
}

@Composable
fun SettingsContent(
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val orientation = LocalOrientation.current
            if (orientation == Orientation.Landscape) {
                Spacer(Modifier.height(12.dp))
            }
            content()
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    message: String,
    isEnabled: Boolean,
    onToggled: () -> Unit
) {
    val surfaceColors = ua.syt0r.kanji.presentation.common.theme.LocalSurfaceColors.current
    val shape = MaterialTheme.shapes.large

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.98f else if (isHovered) 1.02f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .border(1.dp, surfaceColors.border.copy(alpha = Dimens.Alpha.Medium), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current
            ) { onToggled() },
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered || isPressed) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = shape
    ) {
        ListItem(
            headlineContent = { Text(text = title, style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text(message, style = MaterialTheme.typography.bodyMedium) },
            trailingContent = {
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onToggled() },
                    colors = SwitchDefaults.colors(
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
fun SettingsBackupButton(onClick: () -> Unit) {
    SettingsTextButton(title = resolveString { settings.backupTitle }, onClick = onClick)
}

@Composable
fun SettingsAccountButton(onClick: () -> Unit) {
    SettingsTextButton(title = resolveString { settings.account }, onClick = onClick)
}

@Composable
fun SettingsSyncButton(onClick: () -> Unit) {
    SettingsTextButton(title = resolveString { settings.sync }, onClick = onClick)
}

@Composable
fun SettingsFeedbackButton(onClick: () -> Unit) {
    SettingsTextButton(title = resolveString { settings.feedbackTitle }, onClick = onClick)
}

@Composable
fun SettingsAboutButton(onClick: () -> Unit) {
    SettingsTextButton(title = resolveString { settings.aboutTitle }, onClick = onClick)
}

@Composable
fun SettingsTextButton(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    val surfaceColors = ua.syt0r.kanji.presentation.common.theme.LocalSurfaceColors.current
    val shape = MaterialTheme.shapes.large

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.98f else if (isHovered) 1.02f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .border(1.dp, surfaceColors.border.copy(alpha = Dimens.Alpha.Medium), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current
            ) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered || isPressed) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = shape
    ) {
        ListItem(
            headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
            supportingContent = when {
                subtitle != null -> { { Text(subtitle, style = MaterialTheme.typography.bodyMedium) } }
                else -> null
            },
            trailingContent = {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
fun <T> SettingsPreferencePickerDialog(
    onDismissRequest: () -> Unit,
    title: String,
    options: EnumEntries<T>,
    defaultSelected: T,
    onSelected: (T) -> Unit
) where T : DisplayableEnum, T : Enum<T> {
    var selected by remember { mutableStateOf(defaultSelected) }
    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        content = {
            options.forEach {
                val isOptionSelected = selected == it
                ListItem(
                    headlineContent = { Text(resolveString(it.titleResolver)) },
                    trailingContent = { if (isOptionSelected) Icon(Icons.Default.Check, null) },
                    colors = ListItemDefaults.colors(
                        containerColor = if (isOptionSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.large)
                        .clickable { selected = it }
                )
            }
        },
        buttons = {
            TextButton(onDismissRequest) { Text(resolveString { settings.pickerDialogCancel }) }
            TextButton(
                onClick = {
                    onSelected(selected)
                    onDismissRequest()
                }
            ) { Text(resolveString { settings.pickerDialogApply }) }
        }
    )
}
