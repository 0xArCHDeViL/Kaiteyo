package ua.syt0r.kanji.presentation.common.theme

/**
 * Returns whether the platform requests animations to be reduced or disabled.
 * The Android actual delegates to the system animator scale setting.
 */
internal expect fun systemReducedMotion(): Boolean
