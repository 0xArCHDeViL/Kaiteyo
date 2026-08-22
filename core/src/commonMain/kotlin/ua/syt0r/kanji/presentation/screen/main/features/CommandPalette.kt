package ua.syt0r.kanji.presentation.screen.main.features

import ua.syt0r.kanji.presentation.common.theme.Dimens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.CancellationException
import ua.syt0r.kanji.presentation.common.kaiteyoClickable
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.LocalAnimationConfig

// ============================================
// COMMAND PALETTE
// VS Code-style quick command palette.
// Fuzzy search across destinations, actions,
// filters and theme commands.
// ============================================

data class PaletteAction(
    val title: String,
    val subtitle: String = "",
    val keywords: String = "",
    val shortcut: String = "",
    val icon: ImageVector? = null,
    val category: String = "Navigate",
    val execute: () -> Unit
)

class CommandPaletteController {

    var isOpen by mutableStateOf(false)
        private set
    var query by mutableStateOf("")
        private set
    var selectedIndex by mutableStateOf(0)
        private set
    private val _actions = mutableStateListOf<PaletteAction>()

    val actions: List<PaletteAction> get() = _actions

    fun setActions(newActions: List<PaletteAction>) {
        _actions.clear()
        _actions.addAll(newActions)
    }

    val filteredActions by derivedStateOf {
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            _actions.take(12)
        } else {
            _actions
                .map { action ->
                    val haystack = (action.title + " " + action.subtitle + " " + action.keywords)
                        .lowercase()
                    val titleLower = action.title.lowercase()
                    val score = when {
                        titleLower == q -> 0
                        titleLower.startsWith(q) -> 1
                        haystack.startsWith(q) -> 2
                        haystack.contains(q) -> 3
                        else -> Int.MAX_VALUE
                    }
                    score to action
                }
                .filter { it.first != Int.MAX_VALUE }
                .sortedWith(compareBy({ it.first }, { it.second.title }))
                .take(12)
                .map { it.second }
        }
    }

    fun open() {
        query = ""
        selectedIndex = 0
        isOpen = true
    }

    fun close() {
        isOpen = false
    }

    fun toggle() {
        if (isOpen) close() else open()
    }

    fun updateQuery(newQuery: String) {
        query = newQuery
        selectedIndex = 0
    }

    fun selectNext() {
        val count = filteredActions.size
        if (count > 0) selectedIndex = (selectedIndex + 1) % count
    }

    fun selectPrevious() {
        val count = filteredActions.size
        if (count > 0) selectedIndex = (selectedIndex - 1 + count) % count
    }

    fun executeSelected() {
        filteredActions.getOrNull(selectedIndex)?.execute()
    }
}

/**
 * Global palette controller so platform entry points
 * (desktop key events) can open the palette.
 */
object KaiteyoPalette {
    val controller = CommandPaletteController()
}

@Composable
fun CommandPaletteOverlay(controller: CommandPaletteController = KaiteyoPalette.controller) {
    val strings = resolveString { commandPalette }
    val animationConfig = LocalAnimationConfig.current
    val enter = if (animationConfig.reducedMotion) {
        EnterTransition.None
    } else {
        fadeIn()
    }
    val exit = if (animationConfig.reducedMotion) {
        ExitTransition.None
    } else {
        fadeOut()
    }

    AnimatedVisibility(
        visible = controller.isOpen,
        enter = enter,
        exit = exit,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = Dimens.Alpha.SemiOpaque))
                    .kaiteyoClickable(
                        onClick = controller::close,
                        contentDescription = strings.dismissDescription,
                    ),
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = Dimens.Space3, vertical = Dimens.Space3)
                    .widthIn(max = 600.dp)
                    .heightIn(max = 680.dp),
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = MaterialTheme.shapes.extraLarge,
                        )
                ) {
                    PaletteSearchField(controller = controller)
                    PaletteResultsList(controller = controller)
                    PaletteFooter(strings = strings)
                }
            }
        }
    }
}

@Composable
private fun PaletteSearchField(controller: CommandPaletteController) {
    val strings = resolveString { commandPalette }
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(controller.isOpen) {
        if (controller.isOpen) {
            text = controller.query
            try {
                focusRequester.requestFocus()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: IllegalStateException) {
                // The field can leave composition while the palette closes.
            }
        }
    }

    LaunchedEffect(controller.query) {
        if (controller.isOpen && text != controller.query) text = controller.query
    }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            controller.updateQuery(it)
        },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .padding(horizontal = Dimens.Space3, vertical = Dimens.Space2),
        label = { Text(strings.searchLabel) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
            )
        },
        trailingIcon = if (text.isNotEmpty()) {
            {
                IconButton(onClick = { controller.updateQuery("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = strings.clearQueryDescription,
                    )
                }
            }
        } else {
            null
        },
        singleLine = true,
        supportingText = if (controller.query.isNotEmpty()) {
            { Text(strings.escapeHint) }
        } else {
            null
        },
    )
}

@Composable
private fun PaletteResultsList(controller: CommandPaletteController) {
    val strings = resolveString { commandPalette }
    val listState = rememberLazyListState()
    val filtered = controller.filteredActions

    LaunchedEffect(controller.selectedIndex, filtered.size) {
        val index = controller.selectedIndex
        if (index > 0) listState.animateScrollToItem(index)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp, max = 360.dp)
    ) {
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = strings.emptyTitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = strings.emptyMessage,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 8.dp, vertical = 8.dp
                )
            ) {
                itemsIndexed(filtered, key = { _, action -> action.title + action.shortcut }) { index, action ->
                    PaletteResultItem(
                        action = action,
                        isSelected = index == controller.selectedIndex,
                        onClick = { action.execute() }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaletteResultItem(
    action: PaletteAction,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .semantics { selected = isSelected },
        shape = MaterialTheme.shapes.medium,
                    color = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },

    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.Space3, vertical = Dimens.Space2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space2),
        ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = MaterialTheme.shapes.small,
                ),
            contentAlignment = Alignment.Center
        ) {
            if (action.icon != null) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = action.category.take(1),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = action.title,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (action.subtitle.isNotBlank()) {
                Text(
                    text = action.subtitle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,

                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (action.shortcut.isNotBlank()) {
            Text(
                text = action.shortcut,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
    }
}

@Composable
private fun PaletteFooter(strings: ua.syt0r.kanji.presentation.common.resources.string.CommandPaletteStrings) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = Dimens.Space3, vertical = Dimens.Space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FooterHint(icon = Icons.Default.KeyboardArrowUp, label = strings.upHint)
        FooterHint(icon = Icons.Default.KeyboardArrowDown, label = strings.downHint)
        FooterHint(icon = Icons.Default.ArrowForward, label = strings.enterHint)
        Spacer(Modifier.weight(1f))
        Text(
            text = strings.openShortcutHint,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun FooterHint(
    icon: ImageVector,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}
