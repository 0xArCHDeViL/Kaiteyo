package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.ui
import ua.syt0r.kanji.presentation.screen.main.screen.library.screen.grammar.FormulaText

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.MutableGrammarReviewState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GrammarPracticeConjugationUI(
    state: MutableGrammarReviewState.ConjugationBuilder,
    answeredCorrectly: Boolean?,
    onAnswerSubmit: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    var builtConjugation by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(Dimens.WindowPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Conjugate: " + state.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(Dimens.Space2))
        
        FormulaText(
            text = state.formula,
        )

        Spacer(modifier = Modifier.height(Dimens.Space8))

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(Dimens.WindowPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.verbDictionary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Dimens.Space2))
                Text(
                    text = state.verbMeaning,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.Space10))

        // Built text display
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.98f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "BuiltTextScale"
        )
        
        Surface(
            onClick = { if (answeredCorrectly == null) builtConjugation = "" },
            interactionSource = interactionSource,
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = Dimens.Space20)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(Dimens.Space4)) {
                Text(
                    text = builtConjugation.ifEmpty { "Tap syllables to build..." },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (builtConjugation.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Dimens.Alpha.SemiOpaque)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.Space12))

        AnimatedContent(
            targetState = answeredCorrectly,
            label = "ConjugationState"
        ) { isCorrect ->
            if (isCorrect == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Space3, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(Dimens.Space3)
                    ) {
                        state.syllables.forEach { syllable ->
                            SyllableChip(
                                text = syllable,
                                onClick = { builtConjugation += syllable }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.Space10))

                    Button(
                        onClick = { onAnswerSubmit(builtConjugation == state.targetConjugation) },
                        modifier = Modifier.fillMaxWidth().height(Dimens.Space12),
                        enabled = builtConjugation.isNotEmpty(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Submit Answer", style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    Text(
                        text = if (isCorrect) "Excellent!" else "Incorrect. Answer: ${state.targetConjugation}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        textAlign = TextAlign.Center
                    )
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
private fun SyllableChip(text: String, onClick: () -> Unit) {
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
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Box(modifier = Modifier.padding(horizontal = Dimens.Space6, vertical = 16.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
