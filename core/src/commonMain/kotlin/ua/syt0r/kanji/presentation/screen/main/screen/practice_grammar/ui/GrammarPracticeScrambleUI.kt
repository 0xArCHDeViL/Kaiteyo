package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.MutableGrammarReviewState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GrammarPracticeScrambleUI(
    state: MutableGrammarReviewState.SentenceScramble,
    answeredCorrectly: Boolean?,
    onAnswerSubmit: (Boolean) -> Unit,
    onNext: () -> Unit,
    onVoiceClick: (String) -> Unit
) {
    var selectedParts by remember { mutableStateOf(listOf<String>()) }
    var availableParts by remember(state.scrambledParts) { mutableStateOf(state.scrambledParts) }

    Column(
        modifier = Modifier.fillMaxSize().padding(Dimens.WindowPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Translate the sentence",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(Dimens.Space8))

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(Dimens.WindowPadding), contentAlignment = Alignment.Center) {
                Text(
                    text = state.meaning,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.Space10))

        // Slot area
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 100.dp)
        ) {
            FlowRow(
                modifier = Modifier.padding(Dimens.Space4),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space2, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space2, Alignment.CenterVertically)
            ) {
                if (selectedParts.isEmpty()) {
                    Text(
                        text = "Tap parts to construct sentence...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Dimens.Alpha.SemiOpaque),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = Dimens.Space4)
                    )
                } else {
                    selectedParts.forEach { part ->
                        ScrambleChip(
                            text = part,
                            isPrimary = true,
                            onClick = {
                                if (answeredCorrectly == null) {
                                    selectedParts = selectedParts - part
                                    availableParts = availableParts + part
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.Space12))

        AnimatedContent(
            targetState = answeredCorrectly,
            label = "ScrambleAnswerState"
        ) { isCorrect ->
            if (isCorrect == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Space3, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(Dimens.Space3)
                    ) {
                        availableParts.forEach { part ->
                            ScrambleChip(
                                text = part,
                                isPrimary = false,
                                onClick = {
                                    selectedParts = selectedParts + part
                                    availableParts = availableParts - part
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.Space10))

                    Button(
                        onClick = {
                            val constructedSentence = selectedParts.joinToString("")
                            val isActuallyCorrect = constructedSentence == state.originalSentence.replace(" ", "")
                            onAnswerSubmit(isActuallyCorrect)
                        },
                        modifier = Modifier.fillMaxWidth().height(Dimens.Space12),
                        enabled = availableParts.isEmpty(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Submit Answer", style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isCorrect) "Excellent!" else "Incorrect. Answer: ${state.originalSentence}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(Dimens.Space2))
                        IconButton(onClick = { onVoiceClick(state.originalSentence) }) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Play answer voice")
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.Space8))
                    Button(
                        onClick = onNext,
                        modifier = Modifier.fillMaxWidth().height(Dimens.Space12),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Continue", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScrambleChip(text: String, isPrimary: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ChipScale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.medium,
        color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Box(modifier = Modifier.padding(horizontal = Dimens.Space5, vertical = 14.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
