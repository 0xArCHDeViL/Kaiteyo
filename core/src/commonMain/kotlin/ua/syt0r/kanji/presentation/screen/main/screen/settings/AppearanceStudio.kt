package ua.syt0r.kanji.presentation.screen.main.screen.settings

import ua.syt0r.kanji.presentation.common.theme.Dimens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.presentation.common.theme.AllAccentSchemes
import ua.syt0r.kanji.presentation.common.theme.AnimationConfig
import ua.syt0r.kanji.presentation.common.theme.AnimationSpeed
import ua.syt0r.kanji.presentation.common.theme.BaseMode
import ua.syt0r.kanji.presentation.common.theme.CornerRadiusStyle
import ua.syt0r.kanji.presentation.common.theme.GlowConfig
import ua.syt0r.kanji.presentation.common.theme.KaiteyoAccentScheme
import ua.syt0r.kanji.presentation.common.theme.KaiteyoGradient
import ua.syt0r.kanji.presentation.common.theme.LayoutConfig
import ua.syt0r.kanji.presentation.common.theme.LocalKaiteyoAccent
import ua.syt0r.kanji.presentation.common.theme.LocalKaiteyoThemeState
import ua.syt0r.kanji.presentation.common.theme.LocalSurfaceColors
import ua.syt0r.kanji.presentation.common.theme.PageTransitionType
import ua.syt0r.kanji.presentation.common.theme.RadiusConfig
import ua.syt0r.kanji.presentation.common.theme.SidebarMode
import ua.syt0r.kanji.presentation.common.theme.SidebarPosition
import ua.syt0r.kanji.presentation.common.theme.UIDensity
import ua.syt0r.kanji.presentation.common.theme.gradientForAccent
import ua.syt0r.kanji.presentation.common.theme.surfaceForBaseMode
import androidx.compose.material3.HorizontalDivider

// ============================================
// KAITEYO v1.2.0 — Appearance Studio
// Professional theme customization suite
// Color Editor · Gradient Editor · Live Preview
// Motion Studio · Layout Studio · Import/Export
// ============================================

private enum class StudioTab(val displayName: String) {
    Themes("Themes"),
    Colors("Colors"),
    Gradient("Gradient"),
    Motion("Motion"),
    Layout("Layout"),
    Export("Export")
}

@Composable
fun AppearanceStudio() {
    val themeState = LocalKaiteyoThemeState.current
    val currentAccent = LocalKaiteyoAccent.current
    val surfaceColors = LocalSurfaceColors.current

    var selectedTab by remember { mutableStateOf(StudioTab.Themes) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // ============================================
        // LEFT: Tab Navigation + Controls
        // ============================================
        Column(
            modifier = Modifier
                .width(440.dp)
                .fillMaxHeight()
        ) {
            // Header
            Text(
                text = "Appearance Studio",
                style = MaterialTheme.typography.titleLarge,
                color = surfaceColors.textPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Tab bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StudioTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val tabBg by animateColorAsState(
                        targetValue = if (isSelected) currentAccent.primary.copy(alpha = Dimens.Alpha.Light)
                            else Color.Transparent,
                        animationSpec = tween(200),
                        label = "tabBg"
                    )
                    val tabText by animateColorAsState(
                        targetValue = if (isSelected) currentAccent.primary
                            else surfaceColors.textSecondary,
                        animationSpec = tween(200),
                        label = "tabText"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Dimens.RadiusSm))
                            .background(tabBg)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.displayName,
                            color = tabText,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = surfaceColors.border.copy(alpha = Dimens.Alpha.Medium))
            Spacer(modifier = Modifier.height(8.dp))

            // Tab content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedTab) {
                    StudioTab.Themes -> ThemePresetsTab()
                    StudioTab.Colors -> ColorEditorTab()
                    StudioTab.Gradient -> GradientEditorTab()
                    StudioTab.Motion -> MotionStudioTab()
                    StudioTab.Layout -> LayoutStudioTab()
                    StudioTab.Export -> ThemeExportTab()
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxHeight().width(1.dp),
            color = surfaceColors.border.copy(alpha = Dimens.Alpha.Light)
        )
        Spacer(modifier = Modifier.width(12.dp))

        // ============================================
        // RIGHT: Live Preview
        // ============================================
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            LivePreviewPanel()
        }
    }
}

// ============================================
// TAB 1: Theme Presets
// ============================================

@Composable
private fun ThemePresetsTab() {
    val themeState = LocalKaiteyoThemeState.current
    val surfaceColors = LocalSurfaceColors.current
    val currentAccent = LocalKaiteyoAccent.current

    Text(
        text = "Theme Presets",
        style = MaterialTheme.typography.titleMedium,
        color = surfaceColors.textPrimary,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Choose a base theme to customize",
        style = MaterialTheme.typography.bodySmall,
        color = surfaceColors.textMuted
    )
    Spacer(modifier = Modifier.height(12.dp))

    // Base Mode selector
    Text(
        text = "Base Mode",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        BaseMode.entries.forEach { mode ->
            val isSelected = themeState.baseMode == mode
            val cardBg by animateColorAsState(
                targetValue = if (isSelected) currentAccent.primary.copy(alpha = Dimens.Alpha.Light)
                    else surfaceColors.surface,
                animationSpec = tween(200),
                label = "baseModeBg"
            )
            val cardBorder by animateColorAsState(
                targetValue = if (isSelected) currentAccent.primary
                    else surfaceColors.border.copy(alpha = Dimens.Alpha.Medium),
                animationSpec = tween(200),
                label = "baseModeBorder"
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Dimens.RadiusMd))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(Dimens.RadiusMd))
                    .clickable { themeState.baseMode = mode }
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(Dimens.RadiusSm))
                        .background(surfaceForBaseMode(mode).background)
                        .border(0.5.dp, surfaceColors.border.copy(alpha = Dimens.Alpha.Light), RoundedCornerShape(Dimens.RadiusSm))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mode.displayName,
                    color = if (isSelected) currentAccent.primary else surfaceColors.textSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Accent scheme grid
    Text(
        text = "Color Schemes",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(8.dp))

    AllAccentSchemes.chunked(2).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            row.forEach { scheme ->
                val isSelected = currentAccent.name == scheme.name
                val cardBg by animateColorAsState(
                    targetValue = if (isSelected) currentAccent.primary.copy(alpha = Dimens.Alpha.Light)
                        else surfaceColors.surface,
                    animationSpec = tween(200),
                    label = "schemeCardBg"
                )
                val cardBorder by animateColorAsState(
                    targetValue = if (isSelected) currentAccent.primary
                        else surfaceColors.border.copy(alpha = Dimens.Alpha.Light),
                    animationSpec = tween(200),
                    label = "schemeCardBorder"
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Dimens.RadiusMd))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(Dimens.RadiusMd))
                        .clickable { themeState.accentScheme = scheme }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Color dots
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        scheme.previewColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = scheme.name,
                        color = if (isSelected) currentAccent.primary else surfaceColors.textPrimary,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
    }
}

// ============================================
// TAB 2: Color Editor
// RGB / HSV / HSL / HEX with sliders
// ============================================

@Composable
private fun ColorEditorTab() {
    val themeState = LocalKaiteyoThemeState.current
    val surfaceColors = LocalSurfaceColors.current
    val currentAccent = LocalKaiteyoAccent.current

    Text(
        text = "Color Editor",
        style = MaterialTheme.typography.titleMedium,
        color = surfaceColors.textPrimary,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Fine-tune every color in your theme",
        style = MaterialTheme.typography.bodySmall,
        color = surfaceColors.textMuted
    )
    Spacer(modifier = Modifier.height(12.dp))

    // Color target selector
    var selectedColorTarget by remember { mutableStateOf("Primary") }
    val colorTargets = listOf("Primary", "Secondary", "Tertiary", "Background", "Surface", "Text")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        colorTargets.forEach { target ->
            val isSelected = selectedColorTarget == target
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Dimens.RadiusSm))
                    .background(
                        if (isSelected) currentAccent.primary.copy(alpha = Dimens.Alpha.Light)
                        else Color.Transparent
                    )
                    .clickable { selectedColorTarget = target }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = target,
                    color = if (isSelected) currentAccent.primary else surfaceColors.textSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Color preview swatch
    val currentColor = when (selectedColorTarget) {
        "Primary" -> currentAccent.primary
        "Secondary" -> currentAccent.secondary
        "Tertiary" -> currentAccent.tertiary ?: currentAccent.primary
        "Background" -> surfaceColors.background
        "Surface" -> surfaceColors.surface
        "Text" -> surfaceColors.textPrimary
        else -> currentAccent.primary
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(Dimens.RadiusMd))
                .background(currentColor)
                .border(1.dp, surfaceColors.border.copy(alpha = Dimens.Alpha.Medium), RoundedCornerShape(Dimens.RadiusMd))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = selectedColorTarget,
                color = surfaceColors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
            Text(
                text = colorToHex(currentColor),
                color = surfaceColors.textMuted,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // RGB Sliders
    Text(
        text = "RGB",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(6.dp))

    var red by remember { mutableStateOf((currentColor.red * 255).toInt()) }
    var green by remember { mutableStateOf((currentColor.green * 255).toInt()) }
    var blue by remember { mutableStateOf((currentColor.blue * 255).toInt()) }

    ColorSlider("R", red, 0..255, Color.Red.copy(alpha = Dimens.Alpha.Medium)) { red = it }
    ColorSlider("G", green, 0..255, Color.Green.copy(alpha = Dimens.Alpha.Medium)) { green = it }
    ColorSlider("B", blue, 0..255, Color.Blue.copy(alpha = Dimens.Alpha.Medium)) { blue = it }

    Spacer(modifier = Modifier.height(12.dp))

    // HEX input
    Text(
        text = "HEX",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(4.dp))

    var hexValue by remember { mutableStateOf(colorToHex(currentColor).removePrefix("#")) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "#",
            color = surfaceColors.textPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(Dimens.RadiusSm))
                .background(surfaceColors.surfaceInteractive)
                .border(1.dp, surfaceColors.border.copy(alpha = Dimens.Alpha.Medium), RoundedCornerShape(Dimens.RadiusSm))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            BasicTextField(
                value = TextFieldValue(hexValue),
                onValueChange = { hexValue = it.text.take(6).uppercase() },
                textStyle = TextStyle(
                    color = surfaceColors.textPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                ),
                singleLine = true,
                modifier = Modifier.width(100.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Opacity control
    Text(
        text = "Opacity",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(4.dp))
    var opacity by remember { mutableStateOf(1f) }
    Slider(
        value = opacity,
        onValueChange = { opacity = it },
        valueRange = 0f..1f,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = "${(opacity * 100).toInt()}%",
        color = surfaceColors.textMuted,
        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
    )
}

// ============================================
// TAB 3: Gradient Editor
// ============================================

@Composable
private fun GradientEditorTab() {
    val themeState = LocalKaiteyoThemeState.current
    val surfaceColors = LocalSurfaceColors.current
    val currentAccent = LocalKaiteyoAccent.current

    Text(
        text = "Gradient Editor",
        style = MaterialTheme.typography.titleMedium,
        color = surfaceColors.textPrimary,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Create custom gradient effects",
        style = MaterialTheme.typography.bodySmall,
        color = surfaceColors.textMuted
    )
    Spacer(modifier = Modifier.height(12.dp))

    // Gradient preview
    val gradient = gradientForAccent(currentAccent)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(Dimens.RadiusMd))
            .background(
                Brush.linearGradient(
                    colors = listOf(gradient.start, gradient.end),
                    tileMode = TileMode.Clamp
                )
            )
            .border(1.dp, surfaceColors.border.copy(alpha = Dimens.Alpha.Light), RoundedCornerShape(Dimens.RadiusMd))
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Gradient stops
    Text(
        text = "Gradient Stops",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Start color
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(gradient.start)
                .border(1.dp, surfaceColors.border.copy(alpha = Dimens.Alpha.Medium), CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Start: ${colorToHex(gradient.start)}",
            color = surfaceColors.textPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(gradient.end)
                .border(1.dp, surfaceColors.border.copy(alpha = Dimens.Alpha.Medium), CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "End: ${colorToHex(gradient.end)}",
            color = surfaceColors.textPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Angle control
    Text(
        text = "Angle: ${gradient.angle.toInt()}°",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(4.dp))
    var angle by remember { mutableStateOf(gradient.angle) }
    Slider(
        value = angle,
        onValueChange = { angle = it },
        valueRange = 0f..360f,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Intensity
    Text(
        text = "Intensity",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(4.dp))
    var intensity by remember { mutableStateOf(gradient.intensity) }
    Slider(
        value = intensity,
        onValueChange = { intensity = it },
        valueRange = 0f..2f,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = "${(intensity * 100).toInt()}%",
        color = surfaceColors.textMuted,
        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Preset gradients
    Text(
        text = "Presets",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(
            currentAccent.primary to currentAccent.secondary,
            currentAccent.secondary to currentAccent.primary,
            currentAccent.primary to currentAccent.primaryDark,
            currentAccent.secondary to currentAccent.secondaryDark
        ).forEach { (start, end) ->
            Box(
                modifier = Modifier
                    .size(48.dp, 32.dp)
                    .clip(RoundedCornerShape(Dimens.RadiusSm))
                    .background(Brush.linearGradient(listOf(start, end)))
                    .clickable { /* apply preset */ }
                    .border(0.5.dp, surfaceColors.border.copy(alpha = Dimens.Alpha.Light), RoundedCornerShape(Dimens.RadiusSm))
            )
        }
    }
}

// ============================================
// TAB 4: Motion Studio
// Animation presets and controls
// ============================================

@Composable
private fun MotionStudioTab() {
    val themeState = LocalKaiteyoThemeState.current
    val surfaceColors = LocalSurfaceColors.current
    val currentAccent = LocalKaiteyoAccent.current

    Text(
        text = "Motion Studio",
        style = MaterialTheme.typography.titleMedium,
        color = surfaceColors.textPrimary,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Control the feel of every interaction",
        style = MaterialTheme.typography.bodySmall,
        color = surfaceColors.textMuted
    )
    Spacer(modifier = Modifier.height(12.dp))

    // Animation presets
    Text(
        text = "Animation Preset",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(6.dp))

    val presets = listOf(
        "No Animation" to AnimationSpeed.Instant,
        "Minimal" to AnimationSpeed.Fast,
        "Standard" to AnimationSpeed.Normal,
        "Smooth" to AnimationSpeed.Slow,
        "Bouncy" to AnimationSpeed.Slow
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        presets.forEach { (name, speed) ->
            val isSelected = themeState.animationConfig.speed == speed
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Dimens.RadiusSm))
                    .background(
                        if (isSelected) currentAccent.primary.copy(alpha = Dimens.Alpha.Light)
                        else surfaceColors.surface
                    )
                    .border(
                        1.dp,
                        if (isSelected) currentAccent.primary else surfaceColors.border.copy(alpha = Dimens.Alpha.Light),
                        RoundedCornerShape(Dimens.RadiusSm)
                    )
                    .clickable {
                        themeState.animationConfig = themeState.animationConfig.copy(speed = speed)
                    }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name,
                    color = if (isSelected) currentAccent.primary else surfaceColors.textSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Spring controls
    Text(
        text = "Spring Physics",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = "Damping: ${String.format("%.1f", themeState.animationConfig.springDamping)}",
        color = surfaceColors.textPrimary,
        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
    )
    var damping by remember { mutableStateOf(themeState.animationConfig.springDamping) }
    Slider(
        value = damping,
        onValueChange = {
            damping = it
            themeState.animationConfig = themeState.animationConfig.copy(springDamping = it)
        },
        valueRange = 0.1f..2f,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Stiffness: ${themeState.animationConfig.springStiffness.toInt()}",
        color = surfaceColors.textPrimary,
        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
    )
    var stiffness by remember { mutableStateOf(themeState.animationConfig.springStiffness) }
    Slider(
        value = stiffness,
        onValueChange = {
            stiffness = it
            themeState.animationConfig = themeState.animationConfig.copy(springStiffness = it)
        },
        valueRange = 100f..1000f,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Transition type
    Text(
        text = "Page Transition",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(6.dp))

    PageTransitionType.entries.forEach { transition ->
        val isSelected = themeState.animationConfig.pageTransition == transition
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimens.RadiusSm))
                .background(
                    if (isSelected) currentAccent.primary.copy(alpha = Dimens.Alpha.Subtle)
                    else Color.Transparent
                )
                .clickable {
                    themeState.animationConfig = themeState.animationConfig.copy(pageTransition = transition)
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) currentAccent.primary
                        else surfaceColors.border
                    )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = transition.displayName,
                color = if (isSelected) currentAccent.primary else surfaceColors.textPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Duration
    Text(
        text = "Duration: ${themeState.animationConfig.defaultDuration}ms",
        color = surfaceColors.textPrimary,
        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
    )
    var duration by remember { mutableStateOf(themeState.animationConfig.defaultDuration.toFloat()) }
    Slider(
        value = duration,
        onValueChange = {
            duration = it
            themeState.animationConfig = themeState.animationConfig.copy(defaultDuration = it.toInt())
        },
        valueRange = 50f..800f,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Reduced motion toggle
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.RadiusSm))
            .clickable {
                themeState.animationConfig = themeState.animationConfig.copy(
                    reducedMotion = !themeState.animationConfig.reducedMotion
                )
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(Dimens.RadiusXs))
                .background(
                    if (themeState.animationConfig.reducedMotion) currentAccent.primary
                    else surfaceColors.border
                )
                .then(
                    if (themeState.animationConfig.reducedMotion) Modifier.padding(4.dp)
                    else Modifier.padding(2.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (themeState.animationConfig.reducedMotion) {
                Text("✓", color = currentAccent.onPrimary, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Reduced Motion",
            color = surfaceColors.textPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
        )
    }
}

// ============================================
// TAB 5: Layout Studio
// Density, radius, sidebar, spacing
// ============================================

@Composable
private fun LayoutStudioTab() {
    val themeState = LocalKaiteyoThemeState.current
    val surfaceColors = LocalSurfaceColors.current
    val currentAccent = LocalKaiteyoAccent.current

    Text(
        text = "Layout Studio",
        style = MaterialTheme.typography.titleMedium,
        color = surfaceColors.textPrimary,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Customize the spatial experience",
        style = MaterialTheme.typography.bodySmall,
        color = surfaceColors.textMuted
    )
    Spacer(modifier = Modifier.height(12.dp))

    // Density
    Text(
        text = "UI Density",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(6.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        UIDensity.entries.forEach { density ->
            val isSelected = themeState.layoutConfig.density == density
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Dimens.RadiusSm))
                    .background(
                        if (isSelected) currentAccent.primary.copy(alpha = Dimens.Alpha.Light)
                        else surfaceColors.surface
                    )
                    .border(
                        1.dp,
                        if (isSelected) currentAccent.primary else surfaceColors.border.copy(alpha = Dimens.Alpha.Light),
                        RoundedCornerShape(Dimens.RadiusSm)
                    )
                    .clickable {
                        themeState.layoutConfig = themeState.layoutConfig.copy(density = density)
                    }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = density.displayName,
                    color = if (isSelected) currentAccent.primary else surfaceColors.textSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Corner Radius
    Text(
        text = "Corner Radius Style",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(6.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        CornerRadiusStyle.entries.forEach { style ->
            val isSelected = themeState.radiusConfig.style == style
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Dimens.RadiusSm))
                    .background(
                        if (isSelected) currentAccent.primary.copy(alpha = Dimens.Alpha.Light)
                        else surfaceColors.surface
                    )
                    .border(
                        1.dp,
                        if (isSelected) currentAccent.primary else surfaceColors.border.copy(alpha = Dimens.Alpha.Light),
                        RoundedCornerShape(Dimens.RadiusSm)
                    )
                    .clickable {
                        themeState.radiusConfig = themeState.radiusConfig.copy(style = style)
                    }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = style.displayName,
                    color = if (isSelected) currentAccent.primary else surfaceColors.textSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Custom radius slider
    Text(
        text = "Custom Radius: ${(themeState.radiusConfig.customRadius ?: 12f).toInt()}dp",
        color = surfaceColors.textPrimary,
        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
    )
    var customRadius by remember { mutableStateOf(themeState.radiusConfig.customRadius ?: 12f) }
    Slider(
        value = customRadius,
        onValueChange = {
            customRadius = it
            themeState.radiusConfig = themeState.radiusConfig.copy(customRadius = it)
        },
        valueRange = 0f..48f,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Sidebar
    Text(
        text = "Sidebar",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(6.dp))

    // Sidebar mode
    Text(
        text = "Mode",
        color = surfaceColors.textMuted,
        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        SidebarMode.entries.take(4).forEach { mode ->
            val isSelected = themeState.layoutConfig.sidebarMode == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Dimens.RadiusSm))
                    .background(
                        if (isSelected) currentAccent.primary.copy(alpha = Dimens.Alpha.Light)
                        else Color.Transparent
                    )
                    .clickable {
                        themeState.layoutConfig = themeState.layoutConfig.copy(sidebarMode = mode)
                    }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.displayName,
                    color = if (isSelected) currentAccent.primary else surfaceColors.textSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Sidebar position
    Text(
        text = "Position",
        color = surfaceColors.textMuted,
        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SidebarPosition.entries.forEach { pos ->
            val isSelected = themeState.layoutConfig.sidebarPosition == pos
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Dimens.RadiusSm))
                    .background(
                        if (isSelected) currentAccent.primary.copy(alpha = Dimens.Alpha.Light)
                        else Color.Transparent
                    )
                    .clickable {
                        themeState.layoutConfig = themeState.layoutConfig.copy(sidebarPosition = pos)
                    }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pos.displayName,
                    color = if (isSelected) currentAccent.primary else surfaceColors.textSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Glow
    Text(
        text = "Glow Effects",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = "Intensity: ${String.format("%.1f", themeState.glowConfig.intensity)}",
        color = surfaceColors.textPrimary,
        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
    )
    var glowIntensity by remember { mutableStateOf(themeState.glowConfig.intensity) }
    Slider(
        value = glowIntensity,
        onValueChange = {
            glowIntensity = it
            themeState.glowConfig = themeState.glowConfig.copy(intensity = it)
        },
        valueRange = 0f..2f,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Radius: ${String.format("%.1f", themeState.glowConfig.radius)}",
        color = surfaceColors.textPrimary,
        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
    )
    var glowRadius by remember { mutableStateOf(themeState.glowConfig.radius) }
    Slider(
        value = glowRadius,
        onValueChange = {
            glowRadius = it
            themeState.glowConfig = themeState.glowConfig.copy(radius = it)
        },
        valueRange = 0f..2f,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Opacity: ${String.format("%.1f", themeState.glowConfig.opacity)}",
        color = surfaceColors.textPrimary,
        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
    )
    var glowOpacity by remember { mutableStateOf(themeState.glowConfig.opacity) }
    Slider(
        value = glowOpacity,
        onValueChange = {
            glowOpacity = it
            themeState.glowConfig = themeState.glowConfig.copy(opacity = it)
        },
        valueRange = 0f..1f,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Transparency / Blur
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.RadiusSm))
            .clickable {
                themeState.layoutConfig = themeState.layoutConfig.copy(
                    transparencyEnabled = !themeState.layoutConfig.transparencyEnabled
                )
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(Dimens.RadiusXs))
                .background(
                    if (themeState.layoutConfig.transparencyEnabled) currentAccent.primary
                    else surfaceColors.border
                ),
            contentAlignment = Alignment.Center
        ) {
            if (themeState.layoutConfig.transparencyEnabled) {
                Text("✓", color = currentAccent.onPrimary, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Enable Transparency",
            color = surfaceColors.textPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
        )
    }

    if (themeState.layoutConfig.transparencyEnabled) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Glass Opacity: ${(themeState.layoutConfig.glassOpacity * 100).toInt()}%",
            color = surfaceColors.textPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
        )
        var glassOpacity by remember { mutableStateOf(themeState.layoutConfig.glassOpacity) }
        Slider(
            value = glassOpacity,
            onValueChange = {
                glassOpacity = it
                themeState.layoutConfig = themeState.layoutConfig.copy(glassOpacity = it)
            },
            valueRange = 0.1f..1f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============================================
// TAB 6: Theme Export / Import
// ============================================

@Composable
private fun ThemeExportTab() {
    val themeState = LocalKaiteyoThemeState.current
    val surfaceColors = LocalSurfaceColors.current
    val currentAccent = LocalKaiteyoAccent.current

    Text(
        text = "Theme Export / Import",
        style = MaterialTheme.typography.titleMedium,
        color = surfaceColors.textPrimary,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Share your custom themes as JSON",
        style = MaterialTheme.typography.bodySmall,
        color = surfaceColors.textMuted
    )
    Spacer(modifier = Modifier.height(16.dp))

    // Export
    Text(
        text = "Export Current Theme",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(8.dp))

    val themeJson = buildThemeJson(
        accent = currentAccent,
        baseMode = themeState.baseMode,
        animationConfig = themeState.animationConfig,
        radiusConfig = themeState.radiusConfig,
        glowConfig = themeState.glowConfig,
        layoutConfig = themeState.layoutConfig
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(Dimens.RadiusSm))
            .background(surfaceColors.surfaceInteractive)
            .border(1.dp, surfaceColors.border.copy(alpha = Dimens.Alpha.Medium), RoundedCornerShape(Dimens.RadiusSm))
            .padding(12.dp)
    ) {
        Text(
            text = themeJson,
            color = surfaceColors.textSecondary,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            lineHeight = 14.sp
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = { /* Copy to clipboard */ },
        colors = ButtonDefaults.buttonColors(
            containerColor = currentAccent.primary,
            contentColor = currentAccent.onPrimary
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Copy Theme JSON")
    }

    Spacer(modifier = Modifier.height(20.dp))
    HorizontalDivider(color = surfaceColors.border.copy(alpha = Dimens.Alpha.Medium))
    Spacer(modifier = Modifier.height(12.dp))

    // Import
    Text(
        text = "Import Theme",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(8.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(Dimens.RadiusSm))
            .background(surfaceColors.surfaceInteractive.copy(alpha = Dimens.Alpha.SemiOpaque))
            .border(
                1.dp,
                surfaceColors.border.copy(alpha = Dimens.Alpha.Medium),
                RoundedCornerShape(Dimens.RadiusSm)
            )
            .clickable { /* Open file picker */ },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Drop JSON file here",
                color = surfaceColors.textMuted,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
            Text(
                text = "or click to browse",
                color = currentAccent.primary,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Saved presets
    Text(
        text = "Saved Presets",
        style = MaterialTheme.typography.bodyMedium,
        color = surfaceColors.textSecondary,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(8.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.RadiusSm))
            .background(surfaceColors.surface)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No saved presets yet. Export a theme to save it.",
            color = surfaceColors.textMuted,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
        )
    }
}

// ============================================
// LIVE PREVIEW PANEL
// Shows sidebar, cards, buttons, lists, dialogs
// ============================================

@Composable
private fun LivePreviewPanel() {
    val themeState = LocalKaiteyoThemeState.current
    val surfaceColors = LocalSurfaceColors.current
    val currentAccent = LocalKaiteyoAccent.current

    val previewSurface = surfaceForBaseMode(themeState.baseMode)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(Dimens.RadiusLg))
            .background(previewSurface.background)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Live Preview",
            style = MaterialTheme.typography.titleMedium,
            color = previewSurface.textPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Changes apply in real-time",
            style = MaterialTheme.typography.bodySmall,
            color = previewSurface.textMuted
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Preview layout: sidebar + content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(Dimens.RadiusMd))
                .background(previewSurface.surface)
                .padding(8.dp)
        ) {
            // Mini sidebar preview
            Column(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(Dimens.RadiusSm))
                    .background(previewSurface.surfaceElevated)
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Logo area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(Dimens.RadiusXs))
                        .background(currentAccent.primary.copy(alpha = Dimens.Alpha.Light)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "K",
                        color = currentAccent.primary,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Nav items
                listOf("◆", "◇", "◇", "◇").forEachIndexed { i, icon ->
                    val isActive = i == 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Dimens.RadiusXs))
                            .background(
                                if (isActive) currentAccent.primary.copy(alpha = Dimens.Alpha.Subtle)
                                else Color.Transparent
                            )
                            .padding(horizontal = 4.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = icon,
                            color = if (isActive) currentAccent.primary else previewSurface.textMuted,
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (i == 0) "Home" else "Item",
                            color = if (isActive) currentAccent.primary else previewSurface.textMuted,
                            fontSize = 7.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Mini progress
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(previewSurface.border)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(currentAccent.primary)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Content area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Content header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dashboard",
                            color = previewSurface.textPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Study overview",
                            color = previewSurface.textMuted,
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                        )
                    }
                    // Mini controls
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        listOf("⚙", "★").forEach { icon ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(previewSurface.surfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(icon, fontSize = 7.sp, color = previewSurface.textMuted)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Card grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("23", "156", "89").forEachIndexed { i, value ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(Dimens.RadiusSm))
                                .background(previewSurface.surfaceElevated)
                                .padding(6.dp)
                        ) {
                            Text(
                                text = value,
                                color = currentAccent.primary,
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = listOf("Learning", "Review", "Mastered")[i],
                                color = previewSurface.textMuted,
                                fontSize = 7.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Wide card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(Dimens.RadiusSm))
                        .background(previewSurface.surfaceElevated)
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "Continue Learning",
                            color = previewSurface.textPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Progress bars
                        listOf(0.45f, 0.25f, 0.15f).forEach { progress ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = listOf("N5", "N4", "Vocab")[listOf(0.45f, 0.25f, 0.15f).indexOf(progress)],
                                    color = previewSurface.textMuted,
                                    fontSize = 7.sp,
                                    modifier = Modifier.width(28.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(1.5.dp))
                                        .background(previewSurface.border)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(progress)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(1.5.dp))
                                            .background(currentAccent.primary)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    color = previewSurface.textMuted,
                                    fontSize = 7.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Button preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(5.dp))
                                .background(currentAccent.primary)
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Start Review",
                                color = currentAccent.onPrimary,
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// UTILITY FUNCTIONS
// ============================================

@Composable
private fun ColorSlider(
    label: String,
    value: Int,
    range: IntRange,
    trackColor: Color,
    onValueChange: (Int) -> Unit
) {
    val surfaceColors = LocalSurfaceColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            color = surfaceColors.textPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(20.dp)
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value.toString(),
            color = surfaceColors.textMuted,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.End
        )
    }
}

private fun colorToHex(color: Color): String {
    val r = (color.red * 255).toInt()
    val g = (color.green * 255).toInt()
    val b = (color.blue * 255).toInt()
    return "#${r.toString(16).padStart(2, '0').uppercase()}" +
            "${g.toString(16).padStart(2, '0').uppercase()}" +
            "${b.toString(16).padStart(2, '0').uppercase()}"
}

private fun buildThemeJson(
    accent: KaiteyoAccentScheme,
    baseMode: BaseMode,
    animationConfig: AnimationConfig,
    radiusConfig: RadiusConfig,
    glowConfig: GlowConfig,
    layoutConfig: LayoutConfig
): String {
    return """{
  "name": "Custom Theme",
  "base": "${baseMode.displayName}",
  "colors": {
    "primary": "${colorToHex(accent.primary)}",
    "secondary": "${colorToHex(accent.secondary)}",
    "onPrimary": "${colorToHex(accent.onPrimary)}",
    "onSecondary": "${colorToHex(accent.onSecondary)}"
  },
  "gradient": {
    "start": "${colorToHex(accent.gradientStart ?: accent.primary)}",
    "end": "${colorToHex(accent.gradientEnd ?: accent.secondary)}"
  },
  "animation": {
    "speed": "${animationConfig.speed.displayName}",
    "damping": ${String.format("%.1f", animationConfig.springDamping)},
    "stiffness": ${animationConfig.springStiffness.toInt()},
    "duration": ${animationConfig.defaultDuration},
    "transition": "${animationConfig.pageTransition.displayName}",
    "reducedMotion": ${animationConfig.reducedMotion}
  },
  "radius": {
    "style": "${radiusConfig.style.displayName}",
    "custom": ${radiusConfig.customRadius ?: "null"}
  },
  "glow": {
    "intensity": ${String.format("%.1f", glowConfig.intensity)},
    "radius": ${String.format("%.1f", glowConfig.radius)},
    "opacity": ${String.format("%.1f", glowConfig.opacity)}
  },
  "layout": {
    "density": "${layoutConfig.density.displayName}",
    "sidebarMode": "${layoutConfig.sidebarMode.displayName}",
    "sidebarPosition": "${layoutConfig.sidebarPosition.displayName}",
    "transparency": ${layoutConfig.transparencyEnabled},
    "glassOpacity": ${String.format("%.1f", layoutConfig.glassOpacity)}
  }
}"""
}