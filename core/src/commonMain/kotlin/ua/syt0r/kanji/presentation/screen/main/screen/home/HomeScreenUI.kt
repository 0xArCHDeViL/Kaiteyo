package ua.syt0r.kanji.presentation.screen.main.screen.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
    if (LocalOrientation.current == Orientation.Landscape) {
        Row(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            // Detached Floating Sidebar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 24.dp, horizontal = 16.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .width(IntrinsicSize.Max)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 32.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = resolveString { appName },
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        SyncButton(
                            state = syncIconState,
                            onClick = onSyncButtonClick
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    availableTabs.forEach { tab ->
                        HorizontalTabButton(
                            tab = tab,
                            selected = tab == selectedTabState.value,
                            onClick = { onTabSelected(tab) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (!PlatformFeature.supported) return@Column
                }
            }

            // Main Content Area
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 40.dp, bottomStart = 40.dp)),
                color = MaterialTheme.colorScheme.background
            ) {
                screenTabContent.invoke()
            }
        }
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    title = {
                        Crossfade(
                            targetState = selectedTabState.value,
                            modifier = Modifier.width(IntrinsicSize.Max)
                        ) { tab ->
                            Text(
                                text = resolveString(tab.titleResolver),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.fillMaxWidth().wrapContentWidth()
                            )
                        }
                    },
                    actions = {
                        SyncButton(
                            state = syncIconState,
                            onClick = onSyncButtonClick
                        )
                        if (!PlatformFeature.supported) return@CenterAlignedTopAppBar
                    }
                )
            },
            bottomBar = {
                // Floating Action Bar (Pill style)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 24.dp,
                                shape = CircleShape,
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                            .padding(horizontal = 12.dp, vertical = 12.dp),
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
                    .consumeWindowInsets(paddingValues)
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
        }
    ) {
        IconButton(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
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
            transitionSpec = { scaleIn() togetherWith scaleOut() },
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
                            .size(12.dp)
                            .shadow(2.dp, CircleShape)
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
                            .size(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = CircleShape
                            )
                    )
                }
                SyncIconIndicator.Error -> IndicatorCircle(MaterialTheme.colorScheme.primary)
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
        targetValue = if (isPressed) 0.9f else if (selected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(300)
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom ripple handled by scale/color
                onClick = onClick
            )
            .testTag(tab.buttonTestTag)
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            tab.iconContent()
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
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(300)
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(tab.buttonTestTag),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Box(
                modifier = Modifier.size(24.dp),
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
