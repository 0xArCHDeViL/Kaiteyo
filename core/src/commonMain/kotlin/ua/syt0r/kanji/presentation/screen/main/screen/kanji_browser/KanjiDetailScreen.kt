package ua.syt0r.kanji.presentation.screen.main.screen.kanji_browser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ua.syt0r.kanji.core.app_data.data.CharacterRadical
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.KanjiDetailData
import ua.syt0r.kanji.core.app_data.data.KanjiReadingData
import ua.syt0r.kanji.core.app_data.data.ReadingType
import ua.syt0r.kanji.core.tts.AppTtsManager
import ua.syt0r.kanji.core.tts.JapaneseSpeechContext
import ua.syt0r.kanji.core.tts.JapaneseSpeechRequest
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.kaiteyoHeading
import ua.syt0r.kanji.presentation.common.ui.kanji.Kanji
import ua.syt0r.kanji.presentation.common.ui.kanji.parseKanjiStrokes
import ua.syt0r.kanji.presentation.common.theme.LocalKaiteyoAccent
import ua.syt0r.kanji.presentation.common.theme.LocalSurfaceColors
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.features.KaiteyoDataCenter
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.info.toInfoScreenData
import ua.syt0r.kanji.presentation.screen.main.screen.decks.colorFromHex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanjiDetailScreen(
    kanji: String,
    navigationState: MainNavigationState,
    dataCenter: KaiteyoDataCenter,
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val strings = resolveString { kanjiBrowser.detail }
    var detail by remember(kanji) { mutableStateOf<KanjiDetailData?>(null) }
    var loading by remember(kanji) { mutableStateOf(true) }
    var error by remember(kanji) { mutableStateOf(false) }
    var retryToken by remember(kanji) { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val appTtsManager = koinInject<AppTtsManager>()
    var speakingOnReading by remember(kanji) { mutableStateOf<String?>(null) }

    DisposableEffect(kanji, appTtsManager) {
        onDispose { appTtsManager.stop() }
    }

    LaunchedEffect(kanji, retryToken) {
        loading = true
        error = false
        try {
            detail = dataCenter.loadKanjiDetail(kanji)
        } catch (cancellation: kotlinx.coroutines.CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            error = true
            detail = null
        } finally {
            loading = false
        }
    }

    Column(Modifier.fillMaxSize().background(surfaceColors.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { navigationState.navigateBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = resolveString { kanjiBrowser.navigateUpDescription }, tint = surfaceColors.textPrimary)
            }
            Column(Modifier.weight(1f)) {
                Text(strings.headerLabel, color = surfaceColors.textMuted, style = MaterialTheme.typography.labelSmall)
                Text(kanji, color = surfaceColors.textPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            detail?.let { current ->
                IconButton(onClick = { scope.launch { dataCenter.toggleFavorite(current.kanji) } }) {
                    Icon(
                        if (dataCenter.isFavorite(current.kanji)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = resolveString { kanjiBrowser.favoriteAction },
                        tint = if (dataCenter.isFavorite(current.kanji)) accent.secondary else surfaceColors.textMuted,
                    )
                }
            }
        }

        when {
            loading -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(40.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp), color = accent.primary)
                Spacer(Modifier.height(12.dp))
                Text(strings.loadingMessage, color = surfaceColors.textMuted)
            }
            error -> DetailErrorState(message = strings.loadErrorMessage, onRetry = { retryToken++ }, surfaceColors = surfaceColors)
            detail == null -> DetailErrorState(message = strings.notFoundMessage, surfaceColors = surfaceColors)
            else -> KanjiDetailContent(
                detail = detail!!,
                dataCenter = dataCenter,
                navigationState = navigationState,
                speakingOnReading = speakingOnReading,
                onSpeakOnReading = { reading ->
                    if (speakingOnReading == reading) {
                        appTtsManager.stop()
                        speakingOnReading = null
                    } else {
                        speakingOnReading = reading
                        appTtsManager.stop()
                        scope.launch {
                            appTtsManager.speak(
                                JapaneseSpeechRequest(
                                    displayText = detail!!.kanji,
                                    pronunciation = reading,
                                    context = JapaneseSpeechContext.IsolatedKanji,
                                )
                            )
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun KanjiDetailContent(
    detail: KanjiDetailData,
    dataCenter: KaiteyoDataCenter,
    navigationState: MainNavigationState,
    speakingOnReading: String?,
    onSpeakOnReading: (String) -> Unit,
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val strings = resolveString { kanjiBrowser.detail }
    val onReadings = detail.onReadings
    val kunReadings = detail.kunReadings

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HeroSection(detail, dataCenter, navigationState)
        }
        item {
            MeaningSection(detail.meanings)
        }
        item {
            ReadingSection(
                title = strings.onYomiTitle,
                subtitle = strings.onYomiSubtitle,
                emptyMessage = strings.noOnYomiMessage,
                readings = onReadings,
                audioEnabled = true,
                activeReading = speakingOnReading,
                onAudioClick = onSpeakOnReading,
            )
        }
        item {
            ReadingSection(
                title = strings.kunYomiTitle,
                subtitle = strings.kunYomiSubtitle,
                emptyMessage = strings.noKunYomiMessage,
                readings = kunReadings,
            )
        }
        item {
            WritingSection(
                strokePaths = detail.strokePaths,
                onPractice = {
                    navigationState.navigate(
                        MainDestination.LetterPractice(
                            LetterPracticeScreenConfiguration(
                                practiceType = ScreenLetterPracticeType.Writing,
                                cards = listOf(
                                    LetterPracticeScreenConfiguration.Card(
                                        letter = detail.kanji,
                                        deckId = 0L,
                                    )
                                ),
                            )
                        )
                    )
                },
            )
        }
        item {
            ComponentSection(detail.radicals)
        }
        item {
            WhiteboardLinkSection(
                onOpenLearningMap = { navigationState.navigate(MainDestination.ConnectedLearning("kanji:${detail.kanji}")) },
            )
        }
        item {
            VocabularySection(
                vocabulary = detail.vocabularyExamples,
                onWordClick = { word ->
                    navigationState.navigate(MainDestination.Info(word.toInfoScreenData()))
                },
            )
        }
        item {
            LearningStatusSection(detail.kanji, dataCenter)
        }
        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeroSection(
    detail: KanjiDetailData,
    dataCenter: KaiteyoDataCenter,
    navigationState: MainNavigationState,
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val strings = resolveString { kanjiBrowser.detail }
    Card(colors = CardDefaults.cardColors(containerColor = surfaceColors.surfaceElevated), shape = MaterialTheme.shapes.extraLarge) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    detail.kanji,
                    color = surfaceColors.textPrimary,
                    style = MaterialTheme.typography.displayLarge,
                )
                Spacer(Modifier.width(18.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(detail.meanings.firstOrNull().orEmpty().ifBlank { strings.noMeaningMessage }, color = surfaceColors.textSecondary, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = when {
                            dataCenter.isLearned(detail.kanji) -> "${strings.inWritingReview} · ${dataCenter.srsStatus(detail.kanji)}"
                            else -> strings.notReviewed
                        },
                        color = if (dataCenter.isLearned(detail.kanji)) accent.primary else surfaceColors.textMuted,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailMetadataBadge(text = strings.strokeCount(detail.strokePaths.size), icon = Icons.Default.Straighten)
                detail.classifications.forEach { classification ->
                    DetailMetadataBadge(text = classification.uppercase())
                }
                detail.frequency?.takeIf { it > 0 }?.let { frequency ->
                    DetailMetadataBadge(text = strings.frequencyLabel(frequency))
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    navigationState.navigate(
                        MainDestination.LetterPractice(
                            LetterPracticeScreenConfiguration(
                                ScreenLetterPracticeType.Writing,
                                listOf(LetterPracticeScreenConfiguration.Card(detail.kanji, 0L)),
                            )
                        )
                    )
                }) {
                    Icon(Icons.Default.School, null)
                    Spacer(Modifier.width(6.dp))
                    Text(strings.practiceWritingButton)
                }
                OutlinedButton(onClick = { navigationState.navigate(MainDestination.ConnectedLearning("kanji:${detail.kanji}")) }) {
                    Icon(Icons.Default.AccountTree, null)
                    Spacer(Modifier.width(6.dp))
                    Text(strings.whiteboardTitle)
                }
                OutlinedButton(onClick = { navigationState.navigate(MainDestination.KanjiComponentMindMap) }) {
                    Icon(Icons.Default.AccountTree, null)
                    Spacer(Modifier.width(6.dp))
                    Text(strings.componentsTitle)
                }
            }
        }
    }
}

@Composable
private fun DetailMetadataBadge(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            icon?.let { Icon(it, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer) }
            Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
private fun MeaningSection(meanings: List<String>) {
    val strings = resolveString { kanjiBrowser.detail }
    DetailSection(title = strings.meaningTitle, icon = Icons.Default.MenuBook) {
        if (meanings.isEmpty()) {
            Text(strings.noMeaningMessage, color = LocalSurfaceColors.current.textMuted)
        } else {
            meanings.forEachIndexed { index, meaning ->
                Text("${index + 1}. $meaning", color = LocalSurfaceColors.current.textPrimary, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun ReadingSection(
    title: String,
    subtitle: String,
    emptyMessage: String,
    readings: List<KanjiReadingData>,
    audioEnabled: Boolean = false,
    activeReading: String? = null,
    onAudioClick: ((String) -> Unit)? = null,
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val strings = resolveString { kanjiBrowser.detail }
    var expanded by remember(title, readings) { mutableStateOf(false) }
    val visible = if (expanded) readings else readings.take(3)
    DetailSection(title = title, subtitle = subtitle) {
        if (readings.isEmpty()) {
            Text(emptyMessage, color = surfaceColors.textMuted)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                visible.forEachIndexed { index, reading ->
                    Row(
                        modifier = Modifier.fillMaxWidth().background(surfaceColors.surfaceInteractive, MaterialTheme.shapes.medium).padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(accent.primary))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            reading.reading,
                            modifier = Modifier.weight(1f),
                            color = surfaceColors.textPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
                        )
                        if (audioEnabled && onAudioClick != null) {
                            IconButton(
                                onClick = { onAudioClick(reading.reading) },
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(
                                    imageVector = if (activeReading == reading.reading) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = if (activeReading == reading.reading) strings.stopReadingDescription(reading.reading) else strings.playOnReadingDescription(reading.reading),
                                    tint = if (activeReading == reading.reading) accent.primary else surfaceColors.textMuted,
                                )
                            }
                        }
                    }
                }
                if (readings.size > 3) {
                    TextButton(onClick = { expanded = !expanded }) {
                        Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(if (expanded) strings.collapseReadings else strings.showAllReadings(readings.size))
                    }
                }
            }
        }
    }
}

@Composable
private fun WritingSection(strokePaths: List<String>, onPractice: () -> Unit) {
    val strokes = remember(strokePaths) { parseKanjiStrokes(strokePaths) }
    val strings = resolveString { kanjiBrowser.detail }
    DetailSection(title = strings.writingTitle, icon = Icons.Default.Straighten, subtitle = strings.writingSubtitle) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(
                modifier = Modifier.size(128.dp),
                color = LocalSurfaceColors.current.surfaceInteractive,
                shape = MaterialTheme.shapes.large,
            ) {
                if (strokes.isNotEmpty()) {
                    Kanji(strokes = strokes, modifier = Modifier.fillMaxSize().padding(12.dp))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.strokeCount(strokePaths.size), color = LocalSurfaceColors.current.textPrimary, style = MaterialTheme.typography.bodyLarge)
                OutlinedButton(onClick = onPractice) { Text(strings.practiceWritingButton) }
            }
        }
        Text(strings.writingExplanation, color = LocalSurfaceColors.current.textMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ComponentSection(radicals: List<CharacterRadical>) {
    val strings = resolveString { kanjiBrowser.detail }
    DetailSection(title = strings.componentsTitle, icon = Icons.Default.AccountTree) {
        if (radicals.isEmpty()) {
            Text(strings.noComponentsMessage, color = LocalSurfaceColors.current.textMuted)
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                radicals.forEach { radical ->
                    Surface(shape = MaterialTheme.shapes.medium, color = LocalSurfaceColors.current.surfaceInteractive) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(radical.radical, color = LocalKaiteyoAccent.current.primary, style = MaterialTheme.typography.headlineMedium)
                            Text(strings.componentStrokeCount(radical.startPosition), color = LocalSurfaceColors.current.textMuted, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WhiteboardLinkSection(onOpenLearningMap: () -> Unit) {
    val strings = resolveString { kanjiBrowser.detail }
    DetailSection(
        title = strings.whiteboardTitle,
        icon = Icons.Default.AccountTree,
        subtitle = strings.whiteboardSubtitle,
    ) {
        Text(
            strings.whiteboardExplanation,
            color = LocalSurfaceColors.current.textMuted,
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onOpenLearningMap, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.AccountTree, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(strings.openWhiteboardButton)
        }
    }
}

@Composable
private fun VocabularySection(vocabulary: List<JapaneseWord>, onWordClick: (JapaneseWord) -> Unit) {
    val strings = resolveString { kanjiBrowser.detail }
    DetailSection(title = strings.vocabularyTitle, icon = Icons.Default.MenuBook, subtitle = strings.vocabularySubtitle) {
        if (vocabulary.isEmpty()) {
            Text(strings.noVocabularyMessage, color = LocalSurfaceColors.current.textMuted)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                vocabulary.take(12).forEach { word ->
                    Surface(
                        onClick = { onWordClick(word) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = LocalSurfaceColors.current.surfaceInteractive,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1.4f)) {
                                Text(word.reading.kanjiReading ?: word.reading.kanaReading, color = LocalSurfaceColors.current.textPrimary, style = MaterialTheme.typography.bodyLarge)
                                Text(word.reading.kanaReading, color = LocalSurfaceColors.current.textMuted, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                word.glossary.firstOrNull().orEmpty(),
                                color = LocalSurfaceColors.current.textSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LearningStatusSection(kanji: String, dataCenter: KaiteyoDataCenter) {
    val strings = resolveString { kanjiBrowser.detail }
    DetailSection(title = strings.learningStatusTitle, icon = Icons.Default.School) {
        val surfaceColors = LocalSurfaceColors.current
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (dataCenter.isLearned(kanji)) strings.inWritingReview else strings.notReviewed, color = surfaceColors.textPrimary)
            if (dataCenter.isDifficult(kanji)) {
                Text(strings.difficultLabel, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            }
            dataCenter.cardFlagsFor(kanji).takeIf { it.id != 0 }?.let { flag ->
                Icon(Icons.Default.Flag, contentDescription = flag.displayName, tint = flag.colorFromHex())
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, subtitle: String? = null, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, content: @Composable () -> Unit) {
    val surfaceColors = LocalSurfaceColors.current
    Card(colors = CardDefaults.cardColors(containerColor = surfaceColors.surfaceElevated), shape = MaterialTheme.shapes.large) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                icon?.let { Icon(it, null, tint = LocalKaiteyoAccent.current.primary, modifier = Modifier.size(18.dp)) }
                Text(
                    title,
                    modifier = Modifier.kaiteyoHeading(),
                    color = surfaceColors.textPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            subtitle?.let { Text(it, color = surfaceColors.textMuted, style = MaterialTheme.typography.bodySmall) }
            content()
        }
    }
}

@Composable
private fun DetailErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    surfaceColors: ua.syt0r.kanji.presentation.common.theme.SurfaceColors,
) {
    val strings = resolveString { kanjiBrowser.detail }
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(message, color = surfaceColors.textPrimary, style = MaterialTheme.typography.bodyLarge)
            onRetry?.let {
                OutlinedButton(onClick = it) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(6.dp))
                    Text(strings.retryButton)
                }
            }
        }
    }
}
