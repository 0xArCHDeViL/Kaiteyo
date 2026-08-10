package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.MutableGrammarReviewState

@Composable
fun GrammarPracticeDialogueUI(
    state: MutableGrammarReviewState.SurvivalDialogue,
    answeredCorrectly: Boolean?,
    onAnswerSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Dimens.WindowPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = state.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Dimens.Space2))
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = Dimens.Alpha.SemiOpaque),
                modifier = Modifier.padding(horizontal = Dimens.Space4)
            ) {
                Text(
                    text = state.context,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = Dimens.Space4, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.Space8))

        // Chat bubbles
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space4)
        ) {
            state.dialogueLines.forEach { (speaker, text) ->
                val isUser = speaker == "You"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = Dimens.Space5, vertical = 14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.Space6))

        AnimatedContent(
            targetState = answeredCorrectly,
            label = "DialogueAnswerState"
        ) { isCorrect ->
            if (isCorrect == null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space3)
                ) {
                    state.options.forEachIndexed { index, option ->
                        DialogueOptionCard(
                            text = option,
                            onClick = { onAnswerSelected(index) }
                        )
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    Text(
                        text = if (isCorrect) "Spot on!" else "Oops! That doesn't fit the context.",
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
private fun DialogueOptionCard(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "OptionScale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(
            modifier = Modifier.padding(horizontal = Dimens.Space6, vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}
