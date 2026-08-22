package ua.syt0r.kanji.presentation.common.theme

import android.animation.ValueAnimator

internal actual fun systemReducedMotion(): Boolean = !ValueAnimator.areAnimatorsEnabled()
