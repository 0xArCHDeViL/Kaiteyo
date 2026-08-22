import kotlin.test.Test
import kotlin.test.assertEquals
import ua.syt0r.kanji.presentation.common.theme.AnimationConfig
import ua.syt0r.kanji.presentation.common.theme.AnimationSpeed
import ua.syt0r.kanji.presentation.common.theme.tweenDuration

class ThemeMotionTest {

    @Test
    fun reducedMotionDisablesAnimationDuration() {
        val config = AnimationConfig(
            speed = AnimationSpeed.Slow,
            reducedMotion = true,
            defaultDuration = 350,
        )

        assertEquals(0, tweenDuration(config))
    }

    @Test
    fun animationSpeedScalesCentralizedDuration() {
        assertEquals(
            350,
            tweenDuration(AnimationConfig(speed = AnimationSpeed.Normal), 350),
        )
        assertEquals(
            210,
            tweenDuration(AnimationConfig(speed = AnimationSpeed.Fast), 350),
        )
        assertEquals(
            0,
            tweenDuration(AnimationConfig(speed = AnimationSpeed.Instant), 350),
        )
    }
}
