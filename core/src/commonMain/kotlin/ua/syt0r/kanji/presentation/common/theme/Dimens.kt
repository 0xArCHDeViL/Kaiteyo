package ua.syt0r.kanji.presentation.common.theme

import androidx.compose.ui.unit.dp

// ============================================
// KAITEYO v1.2.0 — Dimensions & Spacing
// Premium layout system with density support
// ============================================

object Dimens {

    // --- Corner Radius System ---
    // Standard Android Material surface radii.
    val RadiusXs = 4.dp      // Checkboxes, small indicators
    val RadiusSm = 8.dp      // Buttons, inputs, small cards
    val RadiusMd = 12.dp     // Standard cards, list items
    val RadiusLg = 16.dp     // Large cards, modals, context panels
    val RadiusXl = 24.dp     // Sidebar panel, main content panel
    val Radius2xl = 32.dp    // Large containers, dialogs
    val RadiusFull = 999.dp  // Pill/circular - replaces CircleShape usage in cards

    // --- Spacing Scale (base values) ---
    val Space1 = 4.dp
    val Space2 = 8.dp
    val Space3 = 12.dp
    val Space4 = 16.dp
    val Space5 = 20.dp
    val Space6 = 24.dp
    val Space8 = 32.dp
    val Space10 = 40.dp
    val Space12 = 48.dp
    val Space16 = 64.dp
    val Space20 = 80.dp

    // --- Elevation Scale ---
    val ElevationNone = 0.dp
    val ElevationXs = 1.dp
    val ElevationSm = 2.dp
    val ElevationMd = 4.dp
    val ElevationLg = 8.dp
    val ElevationXl = 12.dp
    val Elevation2xl = 16.dp

    val ContentPadding = 20.dp
    val ContentPaddingSmall = 16.dp

    val IconXs = 16.dp
    val Icon = 24.dp
    val IconSmall = 20.dp
    val IconLg = 28.dp
    val IconXl = 32.dp
    val IconButton = 40.dp

    // --- Alpha Scale ---
    object Alpha {
        const val Subtle = 0.08f
        const val Light = 0.12f
        const val Medium = 0.25f
        const val SemiOpaque = 0.50f
        const val HighEmphasis = 0.87f
    }

    val ScreenWidth = 400.dp

    val PopupMinWidth = 160.dp
    val PopupMaxSize = 300.dp

    // --- Kaiteyo v1.2.0 Enhanced Layout ---
    val SidebarWidth = 260.dp
    val SidebarCompactWidth = 72.dp
    val SidebarRadius = 24.dp
    val SidebarFloatingRadius = 32.dp
    val ContentRadius = 24.dp
    val PanelGap = 24.dp
    val WindowPadding = 24.dp
    val CardMinWidth = 240.dp
    val CardMaxWidth = 400.dp
}