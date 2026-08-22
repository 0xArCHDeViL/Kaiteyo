package ua.syt0r.kanji.presentation.screen.main.screen.library.screen.grammar

import ua.syt0r.kanji.core.grammar.GrammarMarkup
import ua.syt0r.kanji.core.grammar.GrammarQuestionEngine
import ua.syt0r.kanji.presentation.common.theme.Dimens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import org.koin.compose.koinInject
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeScreenConfiguration

@Composable
fun GrammarScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPractice: (MainDestination.GrammarPractice) -> Unit
) {
    val contentRepository = koinInject<ua.syt0r.kanji.core.grammar.GrammarContentRepository>()
    val questionEngine = koinInject<GrammarQuestionEngine>()
    var chapters by remember { mutableStateOf<List<GrammarChapter>?>(null) }
    var selectedChapter by remember { mutableStateOf<GrammarChapter?>(null) }

    LaunchedEffect(contentRepository) {
        chapters = contentRepository.chapters()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                IconButton(onClick = {
                    if (selectedChapter != null) {
                        selectedChapter = null
                    } else {
                        onNavigateBack()
                    }
                }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(Dimens.Space2))
                Text(
                    text = selectedChapter?.title ?: "Grammar / Bunpou",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (chapters == null) {
                FancyLoading(Modifier.fillMaxSize())
            } else {
                if (selectedChapter == null) {
                    GrammarChapterList(
                        chapters = chapters!!,
                        onChapterClick = { selectedChapter = it }
                    )
                } else {
                    Column(Modifier.fillMaxSize()) {
                        Button(
                            onClick = {
                                val chapter = selectedChapter!!
                                val config = GrammarPracticeScreenConfiguration(
                                    deckId = chapter.id.toLong(),
                                    items = chapter.points.flatMap { point ->
                                        buildList {
                                            add(
                                                GrammarPracticeScreenConfiguration.Item.Flashcard(
                                                    pointNumber = point.number,
                                                    showMeaningInFront = false,
                                                )
                                            )
                                            if (questionEngine.supportsCloze(point)) {
                                                add(GrammarPracticeScreenConfiguration.Item.Cloze(point.number))
                                            }
                                            if (questionEngine.supportsConjugation(point)) {
                                                add(GrammarPracticeScreenConfiguration.Item.ConjugationBuilder(point.number))
                                            }
                                            if (questionEngine.supportsScramble(point)) {
                                                add(GrammarPracticeScreenConfiguration.Item.SentenceScramble(point.number))
                                            }
                                            if (questionEngine.supportsDialogue(point)) {
                                                add(GrammarPracticeScreenConfiguration.Item.SurvivalDialogue(point.number))
                                            }
                                        }
                                    }
                                )
                                onNavigateToPractice(MainDestination.GrammarPractice(config))
                            },
                            shape = RoundedCornerShape(Dimens.RadiusXl),
                            modifier = Modifier.fillMaxWidth().height(54.dp).padding(horizontal = Dimens.ContentPadding)
                        ) {
                            Text("Practice Chapter")
                        }
                        GrammarPointList(chapter = selectedChapter!!)
                    }
                }
            }
        }
    }
}

@Composable
fun GrammarChapterList(
    chapters: List<GrammarChapter>,
    onChapterClick: (GrammarChapter) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimens.ContentPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space3)
    ) {
        items(chapters, key = { chapter -> chapter.id }) { chapter ->
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val isHovered by interactionSource.collectIsHoveredAsState()
            
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else if (isHovered) 1.02f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(RoundedCornerShape(Dimens.RadiusLg))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current
                    ) { onChapterClick(chapter) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isHovered || isPressed) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isHovered) Dimens.ElevationMd else Dimens.ElevationSm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(Dimens.ContentPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(Dimens.RadiusMd))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = Dimens.Alpha.Subtle)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(Dimens.Space2))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = chapter.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${chapter.points.size} Patterns",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = "Open",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun GrammarPointList(chapter: GrammarChapter) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimens.ContentPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
    ) {
        items(chapter.points, key = { point -> point.number }) { point ->
            GrammarPointCard(point = point)
        }
    }
}

@Composable
fun GrammarPointCard(point: GrammarPoint) {
    var expanded by remember { mutableStateOf(false) }
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isHovered) 1.01f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(Dimens.RadiusLg))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current
            ) { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered || isPressed) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHovered) Dimens.ElevationMd else Dimens.ElevationSm)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Dimens.ContentPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${point.number} ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FormulaText(
                                text = point.formulaTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    if (point.meaning.isNotBlank()) {
                        Spacer(modifier = Modifier.height(Dimens.Space1))
                        Text(
                            text = point.meaning,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = Dimens.ContentPadding)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = Dimens.Alpha.Subtle))
                    Spacer(modifier = Modifier.height(Dimens.ContentPadding))
                    
                    if (point.formulas.isNotEmpty()) {
                        Text(
                            text = "Formula",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(Dimens.Space1))
                        point.formulas.forEach { formula ->
                            FormulaText(text = formula)
                        }
                        Spacer(modifier = Modifier.height(Dimens.ContentPadding))
                    }
                    
                    if (point.examples.isNotEmpty()) {
                        Text(
                            text = "Examples",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(Dimens.Space2))
                        point.examples.forEach { example ->
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(Dimens.RadiusSm),
                                modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.Space2)
                            ) {
                                Text(
                                    text = example.trim(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(Dimens.Space3)
                                )
                            }
                        }
                    }
                    
                    if (point.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = Dimens.Alpha.SemiOpaque)
                            ),
                            shape = RoundedCornerShape(Dimens.RadiusSm)
                        ) {
                            Text(
                                text = point.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormulaText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge
) {
    androidx.compose.material3.Text(
        text = androidx.compose.ui.text.buildAnnotatedString {
            GrammarMarkup.parse(text).forEach { segment ->
                withStyle(
                    style = androidx.compose.ui.text.SpanStyle(
                        textDecoration = if (segment.struck) TextDecoration.LineThrough else null,
                        color = if (segment.struck) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (segment.struck) FontWeight.Bold else FontWeight.SemiBold,
                    )
                ) {
                    append(segment.text)
                }
            }
        },
        style = style,
        modifier = modifier.padding(bottom = 4.dp),
    )
}
