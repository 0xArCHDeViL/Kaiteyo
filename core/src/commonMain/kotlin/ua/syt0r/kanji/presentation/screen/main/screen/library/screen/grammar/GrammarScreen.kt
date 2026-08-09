package ua.syt0r.kanji.presentation.screen.main.screen.library.screen.grammar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.presentation.common.AutopaddedScrollableColumn
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeScreenConfiguration

@OptIn(ExperimentalResourceApi::class)
@Composable
fun GrammarScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPractice: (MainDestination.GrammarPractice) -> Unit
) {
    var chapters by remember { mutableStateOf<List<GrammarChapter>?>(null) }
    var selectedChapter by remember { mutableStateOf<GrammarChapter?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val bytes = Res.readBytes("files/bunpou_data.json")
            val jsonString = bytes.decodeToString()
            val parsedChapters = Json.decodeFromString<List<GrammarChapter>>(jsonString)
            chapters = parsedChapters
        }
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
                Spacer(modifier = Modifier.width(8.dp))
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
                                    items = chapter.points.flatMap {
                                        listOf(
                                            GrammarPracticeScreenConfiguration.Item.Flashcard(
                                                pointNumber = it.number,
                                                showMeaningInFront = false
                                            ),
                                            GrammarPracticeScreenConfiguration.Item.Cloze(
                                                pointNumber = it.number
                                            ),
                                            GrammarPracticeScreenConfiguration.Item.ConjugationBuilder(
                                                pointNumber = it.number
                                            ),
                                            GrammarPracticeScreenConfiguration.Item.SentenceScramble(
                                                pointNumber = it.number
                                            )
                                        )
                                    } + if (chapter.points.isNotEmpty()) {
                                        listOf(
                                            GrammarPracticeScreenConfiguration.Item.SurvivalDialogue(
                                                pointNumber = chapter.points.last().number
                                            )
                                        )
                                    } else emptyList()
                                )
                                onNavigateToPractice(MainDestination.GrammarPractice(config))
                            },
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
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
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(chapters) { chapter ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onChapterClick(chapter) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
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
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(chapter.points) { point ->
            GrammarPointCard(point = point)
        }
    }
}

@Composable
fun GrammarPointCard(point: GrammarPoint) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${point.number} ${point.formulaTitle}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (point.meaning.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
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
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (point.formulas.isNotEmpty()) {
                        Text(
                            text = "Formula",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        point.formulas.forEach { formula ->
                            FormulaText(text = formula)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    if (point.examples.isNotEmpty()) {
                        Text(
                            text = "Examples",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        point.examples.forEach { example ->
                            Text(
                                text = "• $example",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                    
                    if (point.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp)
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
fun FormulaText(text: String) {
    // Simple parser for ~~strikethrough~~
    val parts = text.split("~~")
    var isStrikethrough = false
    
    // We use a Row with FlowRow-like wrap or simply an AnnotatedString.
    // Actually, AnnotatedString is much better!
    androidx.compose.material3.Text(
        text = androidx.compose.ui.text.buildAnnotatedString {
            parts.forEachIndexed { index, part ->
                if (index % 2 != 0) { // It's inside ~~ ~~
                    withStyle(
                        style = androidx.compose.ui.text.SpanStyle(
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(part)
                    }
                } else {
                    withStyle(
                        style = androidx.compose.ui.text.SpanStyle(
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append(part)
                    }
                }
            }
        },
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}
