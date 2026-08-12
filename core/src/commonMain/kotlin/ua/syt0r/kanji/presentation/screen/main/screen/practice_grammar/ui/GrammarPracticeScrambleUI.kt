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
    var selectedIndices by remember(state.scrambledParts) { mutableStateOf(emptyList<Int>()) }
    val selectedParts = selectedIndices.map { state.scrambledParts[it] }

    Column(
        modifier = Modifier.fillMaxSize().padding(Dimens.WindowPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Susun kalimat",
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
                                        if (selectedIndices.isEmpty()) {

                    Text(
                        text = "Pilih token untuk menyusun kalimat",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Dimens.Alpha.SemiOpaque),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = Dimens.Space4)
                    )
                } else {
                                                selectedIndices.forEach { index ->
                                ScrambleChip(
                                    text = state.scrambledParts[index],
                                    isPrimary = true,
                                    onClick = {
                                        if (answeredCorrectly == null) {
                                            selectedIndices = selectedIndices - index
                                        }
                                    },
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
                        state.scrambledParts.forEachIndexed { index, part ->
                            if (index !in selectedIndices) {
                                ScrambleChip(
                                    text = part,
                                    isPrimary = false,
                                    onClick = { selectedIndices = selectedIndices + index },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.Space10))

                    Button(
                        onClick = {
                            val constructedSentence = selectedParts.joinToString(if (state.originalSentence.contains(" ")) " " else "")
                            val isActuallyCorrect = constructedSentence == state.originalSentence
                            onAnswerSubmit(isActuallyCorrect)
                        },
                        modifier = Modifier.fillMaxWidth().height(Dimens.Space12),
                        enabled = selectedIndices.size == state.scrambledParts.size,
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Periksa jawaban", style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                GrammarPracticeFeedback(
                    isCorrect = isCorrect,
                    expectedAnswer = state.originalSentence,
                    onNext = onNext,
                    onVoiceClick = onVoiceClick,
                )
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
