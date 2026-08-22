package ua.syt0r.kanji.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val MinimumTouchTarget = 48.dp

fun Modifier.kaiteyoTouchTarget(
    minWidth: Dp = MinimumTouchTarget,
    minHeight: Dp = MinimumTouchTarget,
): Modifier = sizeIn(minWidth = minWidth, minHeight = minHeight)

fun Modifier.kaiteyoHeading(): Modifier = semantics { heading() }

fun Modifier.kaiteyoClickable(
    onClick: () -> Unit,
    contentDescription: String? = null,
    role: Role = Role.Button,
): Modifier =
    kaiteyoTouchTarget()
        .semantics(mergeDescendants = true) {
            this.role = role
            contentDescription?.let { this.contentDescription = it }
        }
        .clickable(onClick = onClick)
        .focusable()
