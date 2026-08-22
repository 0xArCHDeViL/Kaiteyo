package ua.syt0r.kanji.presentation.screen.main.screen.practice_common

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import ua.syt0r.kanji.presentation.common.theme.LocalKaiteyoAccent
import ua.syt0r.kanji.presentation.common.theme.LocalSurfaceColors
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.core.user_data.preferences.PreferencesNewCardsOrder
import ua.syt0r.kanji.practice_new_cards_config_first
import ua.syt0r.kanji.practice_new_cards_config_last
import ua.syt0r.kanji.practice_new_cards_config_mixed
import ua.syt0r.kanji.practice_new_cards_config_subtitle
import ua.syt0r.kanji.practice_new_cards_config_title
import ua.syt0r.kanji.practice_summary_button
import ua.syt0r.kanji.practice_summary_empty
import ua.syt0r.kanji.practice_summary_header_reviews
import ua.syt0r.kanji.practice_summary_header_time
import ua.syt0r.kanji.practice_summary_item_insights
import ua.syt0r.kanji.presentation.common.AppDropdownMenu
import ua.syt0r.kanji.presentation.common.AppDropdownMenuItem
import ua.syt0r.kanji.presentation.common.AppListItem
import ua.syt0r.kanji.presentation.common.AppListItemDefaults
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.theme.neutralButtonColors
import ua.syt0r.kanji.presentation.common.theme.snapToBiggerContainerCrossfadeTransitionSpec
import ua.syt0r.kanji.presentation.common.ui.FilledTextField
import kotlin.math.roundToInt
import kotlin.time.Duration

interface PracticeConfigurationCard

sealed interface PracticeToolbarState {

    object Idle : PracticeToolbarState

    object Configuration : PracticeToolbarState

    data class Review(
        val practiceQueueProgress: PracticeQueueProgress
    ) : PracticeToolbarState

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeToolbar(
    state: State<PracticeToolbarState>,
    onUpButtonClick: () -> Unit,
    onDetailClick: (() -> Unit)? = null,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onUpButtonClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        },
        title = {
            AnimatedContent(
                targetState = state.value,
                transitionSpec = snapToBiggerContainerCrossfadeTransitionSpec()
            ) {
                when (it) {
                    PracticeToolbarState.Configuration -> {
                        Text(
                            text = resolveString { commonPractice.configurationTitle }
                        )
                    }

                    else -> {}
                }
            }
        },
        actions = {
            if (onDetailClick != null && state.value is PracticeToolbarState.Review) {
                IconButton(onClick = onDetailClick) {
                    Icon(Icons.Default.Insights, contentDescription = "Open Kanji details")
                }
            }
            AnimatedContent(
                targetState = state.value,
                transitionSpec = snapToBiggerContainerCrossfadeTransitionSpec(),
                contentKey = { it is PracticeToolbarState.Review }
            ) {
                when (it) {
                    is PracticeToolbarState.Review -> {
                        val progress = it.practiceQueueProgress
                        PracticeProgressCounter(
                            pending = progress.pending,
                            repeat = progress.repeats,
                            completed = progress.completed
                        )
                    }

                    else -> {}
                }
            }
        }
    )
}

@Composable
fun PracticeProgressCounter(pending: Int, repeat: Int, completed: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(align = Alignment.CenterEnd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarCountItem(
            count = pending,
            color = MaterialTheme.extraColorScheme.pending
        )

        ToolbarCountItem(
            count = repeat,
            color = MaterialTheme.extraColorScheme.due
        )

        ToolbarCountItem(
            count = completed,
            color = MaterialTheme.extraColorScheme.success
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolbarCountItem(count: Int, color: Color) {
    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(color)
    ) {
        TextButton(onClick = {}) {
            Box(
                modifier = Modifier
                    .alignBy { it.measuredHeight }
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = count.toString(),
                color = color,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alignByBaseline()
            )
        }
    }
}


@Composable
fun PracticeConfigurationContainer(
    onClick: () -> Unit,
    practiceTypeMessage: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val accent = LocalKaiteyoAccent.current
    val surfaceColors = LocalSurfaceColors.current
    val shape = RoundedCornerShape(Dimens.Radius2xl)

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = shape,
                    ambientColor = accent.primary.copy(alpha = Dimens.Alpha.Subtle),
                    spotColor = accent.primary.copy(alpha = 0.16f)
                )
                .clip(shape)
                .border(
                    width = 1.dp,
                    color = surfaceColors.border.copy(alpha = Dimens.Alpha.Medium),
                    shape = shape
                ),
            color = surfaceColors.surfaceElevated,
            shape = shape
        ) {
            Column(
                modifier = Modifier.padding(Dimens.Space5)
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = practiceTypeMessage,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accent.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    content()
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(Dimens.RadiusLg),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent.primary,
                        contentColor = accent.onPrimary
                    )
                ) {
                    Text(
                        text = resolveString { commonPractice.configurationCompleteButton },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

class PracticeConfigurationCardsSelectorState(
    val cardsCount: Int,
    val shuffle: MutableState<Boolean>,
    val newCardsOrder: MutableState<PreferencesNewCardsOrder>
) {
    val range = 1f..cardsCount.toFloat()
    val selectedCountFloatState = mutableStateOf(cardsCount.toFloat())
    val selectedCountIntState = derivedStateOf { selectedCountFloatState.value.roundToInt() }
    val selectedCountTextState = mutableStateOf(selectedCountIntState.value.toString())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeConfigurationItemsSelector(
    state: PracticeConfigurationCardsSelectorState
) {

    Row(
        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = resolveString { commonPractice.configurationSelectedItemsLabel },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.alignByBaseline()
        )

        FilledTextField(
            value = state.selectedCountTextState.value,
            onValueChange = { textValue ->
                state.selectedCountTextState.value = textValue
                textValue.toIntOrNull()?.toFloat()
                    ?.coerceIn(state.range)
                    ?.also { state.selectedCountFloatState.value = it }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.widthIn(min = 80.dp).alignByBaseline()
        )

    }

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text(text = 1.toString())

        val accent = LocalKaiteyoAccent.current
        val surfaceColors = LocalSurfaceColors.current
        
        val colors = SliderDefaults.colors(
            activeTrackColor = accent.primary,
            inactiveTrackColor = surfaceColors.border,
            activeTickColor = accent.primary,
            inactiveTickColor = surfaceColors.textMuted.copy(alpha = Dimens.Alpha.Medium),
            thumbColor = accent.primary
        )

        Slider(
            value = state.selectedCountFloatState.value,
            onValueChange = {
                state.selectedCountFloatState.value = it
                state.selectedCountTextState.value = it.roundToInt().toString()
            },
            valueRange = state.range,
            modifier = Modifier.weight(1f),
            track = {
                SliderDefaults.Track(
                    sliderState = it,
                    thumbTrackGapSize = 2.dp,
                    colors = colors,
                    drawStopIndicator = {},
                    drawTick = { _, _ -> }
                )
            },
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = remember { MutableInteractionSource() },
                    colors = colors,
                    thumbSize = DpSize(6.dp, 30.dp)
                )
            }
        )

        Text(text = state.cardsCount.toString())

    }

    PracticeConfigurationOption(
        title = resolveString { commonPractice.shuffleConfigurationTitle },
        subtitle = resolveString { commonPractice.shuffleConfigurationMessage },
        checked = state.shuffle.value,
        onChange = { state.shuffle.value = it }
    )

    PracticeConfigurationEnumSelector(
        title = stringResource(Res.string.practice_new_cards_config_title),
        subtitle = stringResource(Res.string.practice_new_cards_config_subtitle),
        values = ScreenNewCardsOrder.entries,
        selected = ScreenNewCardsOrder.from(state.newCardsOrder.value),
        onSelected = { state.newCardsOrder.value = it.repoType }
    )

}

enum class ScreenNewCardsOrder(
    stringResource: StringResource,
    val repoType: PreferencesNewCardsOrder
) : DisplayableEnum {

    First(Res.string.practice_new_cards_config_first, PreferencesNewCardsOrder.First),
    Last(Res.string.practice_new_cards_config_last, PreferencesNewCardsOrder.Last),
    Mixed(Res.string.practice_new_cards_config_mixed, PreferencesNewCardsOrder.Mixed);

    override val titleResolver: StringResolveScope<String> = { stringResource(stringResource) }

    companion object {
        fun from(repoType: PreferencesNewCardsOrder) = entries.first { it.repoType == repoType }
    }

}

@Composable
fun ColumnScope.PracticeConfigurationCharactersPreview(
    characters: List<String>,
    selectedCharactersCount: State<Int>
) {

    var previewExpanded by remember { mutableStateOf(false) }

    Row(
        Modifier.clip(MaterialTheme.shapes.medium)
            .clickable(onClick = { previewExpanded = !previewExpanded })
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(start = 20.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = resolveString { commonPractice.configurationCharactersPreview },
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { previewExpanded = !previewExpanded }) {
            val icon = if (previewExpanded) Icons.Default.KeyboardArrowUp
            else Icons.Default.KeyboardArrowDown
            Icon(imageVector = icon, contentDescription = null)
        }
    }

    if (previewExpanded) {
        Text(
            text = buildAnnotatedString {
                append(characters.joinToString(""))
                addStyle(
                    style = SpanStyle(color = MaterialTheme.colorScheme.surfaceVariant),
                    start = selectedCharactersCount.value,
                    end = length
                )
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp),
            style = MaterialTheme.typography.titleLarge
        )
    }
}


@Composable
fun PracticeConfigurationOption(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {

    PracticeConfigurationItem(
        title = title,
        subtitle = subtitle,
        onClick = { if (enabled) onChange(!checked) }
    ) {

        Switch(
            checked = checked,
            onCheckedChange = { onChange(it) },
            colors = SwitchDefaults.colors(
                uncheckedTrackColor = MaterialTheme.colorScheme.background
            ),
            enabled = enabled
        )
    }

}

@Composable
private fun PracticeConfigurationItem(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .run { if (onClick != null) clickable(onClick = onClick) else this }
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        }

        content()

    }

}

@Composable
private fun PracticeConfigurationDropDownButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.width(IntrinsicSize.Max)
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(Icons.Default.ArrowDropDown, null)
    }

}

interface DisplayableEnum {
    val titleResolver: StringResolveScope<String>
}

@Composable
fun <T> PracticeConfigurationEnumSelector(
    title: String,
    subtitle: String,
    values: List<T>,
    selected: T,
    onSelected: (T) -> Unit
) where T : Enum<T>, T : DisplayableEnum {

    PracticeConfigurationItem(
        title = title,
        subtitle = subtitle
    ) {

        var expanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.offset(Dimens.ContentPaddingSmall)
        ) {

            PracticeConfigurationDropDownButton(
                text = resolveString(selected.titleResolver),
                onClick = { expanded = true }
            )

            AppDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                values.forEach {
                    AppDropdownMenuItem(
                        onClick = {
                            onSelected(it)
                            expanded = false
                        },
                        content = { Text(resolveString(it.titleResolver)) }
                    )
                }
            }
        }

    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeSummaryContainer(
    practiceDuration: Duration,
    summaryItemsCount: Int,
    onFinishClick: () -> Unit,
    extraHeaderContent: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {

    val accent = LocalKaiteyoAccent.current
    val surfaceColors = LocalSurfaceColors.current

    Column(
        modifier = Modifier.fillMaxSize()
            .wrapContentSize()
            .widthIn(max = 400.dp)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Header Card (Glassmorphism/Premium)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimens.RadiusXl),
            color = surfaceColors.surfaceElevated,
            shadowElevation = Dimens.ElevationLg
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(Dimens.Space5),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PracticeSummaryInfoLabel(
                    title = stringResource(Res.string.practice_summary_header_time),
                    data = resolveString {
                        commonPractice.summaryTimeSpentValue(practiceDuration)
                    }
                )

                PracticeSummaryInfoLabel(
                    title = stringResource(Res.string.practice_summary_header_reviews),
                    data = summaryItemsCount.toString()
                )

                extraHeaderContent()
            }
        }

        // List of items
        Column(
            modifier = Modifier.weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }

        // Finish Button
        Button(
            onClick = onFinishClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.Space12),
            shape = RoundedCornerShape(Dimens.RadiusLg),
            colors = ButtonDefaults.buttonColors(
                containerColor = accent.primary,
                contentColor = accent.onPrimary
            )
        ) {
            Text(
                text = stringResource(Res.string.practice_summary_button),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

    }

}

@Composable
fun PracticeSummaryEmptyList() {

    Text(
        text = stringResource(Res.string.practice_summary_empty),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.Space10)
            .wrapContentSize()
    )

}

@Composable
fun RowScope.PracticeSummaryInfoLabel(
    title: String,
    data: String
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = data,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = LocalKaiteyoAccent.current.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}


@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun PracticeSummaryItem(
    index: Int,
    header: @Composable () -> Unit,
    totalReviews: Deferred<Int>,
    nextInterval: Duration,
    onClick: () -> Unit
) {
    val reviewsCount = androidx.compose.runtime.produceState<Int?>(initialValue = null, totalReviews) {
        value = totalReviews.await()
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.RadiusLg),
        color = LocalSurfaceColors.current.surfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, LocalSurfaceColors.current.border.copy(alpha = Dimens.Alpha.SemiOpaque))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Leading index
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(LocalKaiteyoAccent.current.primary.copy(alpha = Dimens.Alpha.Subtle)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.plus(1).toString(),
                    color = LocalKaiteyoAccent.current.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Content
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Headline
                CompositionLocalProvider(
                    androidx.compose.material3.LocalTextStyle provides MaterialTheme.typography.titleMedium
                ) {
                    header()
                }
                
                // Supporting text
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(
                            Res.string.practice_summary_item_insights,
                            reviewsCount.value?.toString() ?: "...",
                            resolveString { commonPractice.formattedSrsInterval(nextInterval) }
                        ),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Trailing icon
            Icon(
                imageVector = Icons.AutoMirrored.Default.NavigateNext,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PracticeEarlyFinishDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit
) {

    val strings = resolveString { commonPractice }

    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = strings.earlyFinishDialogTitle,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        content = {
            Text(
                text = strings.earlyFinishDialogMessage,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(text = strings.earlyFinishDialogCancelButton)
            }
            TextButton(onClick = onConfirmClick) {
                Text(text = strings.earlyFinishDialogAcceptButton)
            }
        }
    )
}
