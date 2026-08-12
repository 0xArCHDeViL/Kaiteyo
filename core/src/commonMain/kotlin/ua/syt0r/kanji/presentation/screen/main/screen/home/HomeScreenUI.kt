package ua.syt0r.kanji.presentation.screen.main.screen.home

import ua.syt0r.kanji.presentation.common.ui.KaiteyoScaffold

import ua.syt0r.kanji.presentation.common.theme.Dimens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ua.syt0r.kanji.PlatformFeature
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.IndicatorCircle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenUI(
    availableTabs: List<HomeScreenTab>,
    selectedTabState: State<HomeScreenTab>,
    syncIconState: State<SyncIconState>,
    onTabSelected: (HomeScreenTab) -> Unit,
    onSyncButtonClick: () -> Unit,
    screenTabContent: @Composable () -> Unit
) {
    val isLandscape = LocalOrientation.current == Orientation.Landscape
    
    KaiteyoScaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    title = {
                        AnimatedContent(
                            targetState = selectedTabState.value,
                            transitionSpec = {
                                slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it } + fadeIn() togetherWith
                                slideOutVertically(spring(stiffness = Spring.StiffnessLow)) { -it } + fadeOut()
                            }
                        ) { tab ->
                            Text(
                                text = resolveString(tab.titleResolver),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.fillMaxWidth().wrapContentWidth()
                            )
                        }
                    },
                    actions = {
                        SyncButton(state = syncIconState, onClick = onSyncButtonClick)
                    }
                )
            },
            bottomBar = {
                // Premium Fully Floating Pill Bottom Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(if (isLandscape) 0.5f else 1f)
                            .shadow(
                                elevation = 16.dp,
                                shape = CircleShape,
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = Dimens.Alpha.Medium),
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = Dimens.Alpha.Light)
                            )
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = Dimens.Alpha.Medium), CircleShape)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        availableTabs.forEach { tab ->
                            VerticalTabButton(
                                tab = tab,
                                selected = selectedTabState.value == tab,
                                onClick = { onTabSelected(tab) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedContent(
                    targetState = selectedTabState.value,
                    transitionSpec = {
                        fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                    }
                ) {
                    screenTabContent.invoke()
                }
            }
        }
}

@Composable
private fun SyncButton(
    state: State<SyncIconState>,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }.padding(end = 12.dp)
    ) {
        IconButton(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), CircleShape)
        ) {
            val loadingState = remember { derivedStateOf { state.value.loading } }
            val rotation = rememberSyncIconRotation(loadingState)

            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                modifier = Modifier.graphicsLayer { rotationZ = rotation.value },
                tint = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedContent(
            targetState = state.value.indicator,
            transitionSpec = { scaleIn(spring()) togetherWith scaleOut(spring()) },
            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
        ) {
            when (it) {
                SyncIconIndicator.Disabled,
                SyncIconIndicator.Conflict -> Box(Modifier.size(10.dp))
                SyncIconIndicator.PendingUpload -> IndicatorCircle(MaterialTheme.extraColorScheme.due)
                SyncIconIndicator.UpToDate -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier
                            .size(14.dp)
                            .shadow(4.dp, CircleShape)
                            .background(
                                color = MaterialTheme.extraColorScheme.success,
                                shape = CircleShape
                            )
                            .padding(2.dp),
                        tint = MaterialTheme.colorScheme.surface
                    )
                }
                SyncIconIndicator.Canceled -> {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = CircleShape
                            )
                    )
                }
                SyncIconIndicator.Error -> IndicatorCircle(MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun rememberSyncIconRotation(animate: State<Boolean>): Animatable<Float, AnimationVector1D> {
    val rotation = remember { Animatable(360f) }
    LaunchedEffect(Unit) {
        var shouldLoop = false
        val animateLoop = suspend {
            while (shouldLoop) {
                rotation.snapTo(360f)
                rotation.animateTo(0f, tween(1500, easing = LinearEasing))
            }
        }
        var animateLoopJob: Job? = null
        snapshotFlow { animate.value }.collect { shouldAnimate ->
            shouldLoop = shouldAnimate
            if (shouldAnimate) {
                val currentJob = animateLoopJob
                if (currentJob == null || currentJob.isCompleted)
                    animateLoopJob = launch { animateLoop() }
            }
        }
    }
    return rotation
}

@Composable
private fun RowScope.VerticalTabButton(
    tab: HomeScreenTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else if (selected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    val translationY by animateFloatAsState(
        targetValue = if (selected) -8f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(400)
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(400)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(tab.buttonTestTag)
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.translationY = translationY
            }
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                tab.iconContent()
            }
        }
    }
}

@Composable
private fun HorizontalTabButton(
    tab: HomeScreenTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else if (selected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(400)
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(400)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(Dimens.RadiusXl))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .testTag(tab.buttonTestTag),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                tab.iconContent()
            }
            Text(
                text = resolveString(tab.titleResolver),
                style = MaterialTheme.typography.titleMedium,
                color = contentColor
            )
        }
    }
}
