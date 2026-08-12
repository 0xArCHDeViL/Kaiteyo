package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.grammar.GrammarMarkup
import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.MutableGrammarReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.library.screen.grammar.FormulaText
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.FlashcardPracticeAnswerButtonsRow

@Composable
fun GrammarPracticeFlashcardUI(
    state: MutableGrammarReviewState.Flashcard,
    answers: PracticeAnswers,
    onAnswer: (PracticeAnswer) -> Unit,
    onVoiceClick: (String) -> Unit
) {
    val isFlipped = remember(state) { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !isFlipped.value) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "CardScale"
    )

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(Dimens.WindowPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                onClick = { if (!isFlipped.value) isFlipped.value = true },
                interactionSource = interactionSource,
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(Dimens.WindowPadding)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FormulaText(
                                text = state.title,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier
                            )
                            Spacer(modifier = Modifier.width(Dimens.Space2))
                            IconButton(onClick = { onVoiceClick(GrammarMarkup.japaneseSpeechText(state.title)) }) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Dengarkan judul")
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimens.Space6))
                        FormulaText(text = state.formula)

                        AnimatedVisibility(
                            visible = isFlipped.value,
                            enter = fadeIn() + expandVertically(spring(dampingRatio = Spring.DampingRatioLowBouncy)),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Spacer(modifier = Modifier.height(Dimens.Space10))
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Dimens.Alpha.Light),
                                    modifier = Modifier.fillMaxWidth(0.6f)
                                )
                                Spacer(modifier = Modifier.height(Dimens.Space8))
                                FormulaText(
                                    text = state.meaning,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        textAlign = TextAlign.Center
                                    )
                                )
                                if (state.examples.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(Dimens.Space6))
                                    state.examples.forEach { example ->
                                        Surface(
                                            shape = MaterialTheme.shapes.large,
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.Space2)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = example,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(Dimens.Space4).weight(1f)
                                                )
                                                IconButton(
                                                    onClick = {
                                                        onVoiceClick(GrammarMarkup.japaneseSpeechText(example))
                                                    },
                                                    modifier = Modifier.padding(end = Dimens.Space2)
                                                ) {
                                                    Icon(Icons.Default.VolumeUp, contentDescription = "Dengarkan contoh")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Space8))

            FlashcardPracticeAnswerButtonsRow(
                answers = answers,
                showAnswer = isFlipped,
                onRevealAnswerClick = { isFlipped.value = true },
                onAnswerClick = onAnswer
            )
        }
    }
}
