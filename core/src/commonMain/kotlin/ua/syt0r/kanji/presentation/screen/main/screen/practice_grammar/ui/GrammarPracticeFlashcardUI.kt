package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.ScreenSurface
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.MutableGrammarReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.library.screen.grammar.FormulaText

@Composable
fun GrammarPracticeFlashcardUI(
    state: MutableGrammarReviewState.Flashcard,
    answers: PracticeAnswers,
    onAnswer: (Boolean) -> Unit,
    onNext: (PracticeAnswers) -> Unit
) {
    var isFlipped by remember(state) { mutableStateOf(false) }

    ScreenSurface {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable { if (answers == null && !isFlipped) isFlipped = true },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    FormulaText(text = state.formula)
                    
                    AnimatedVisibility(visible = isFlipped || answers != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.meaning,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            if (state.examples.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = state.examples.first(), // show first example
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (answers == null) {
                if (isFlipped) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { onAnswer(false) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Again")
                        }
                        Button(
                            onClick = { onAnswer(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Good")
                        }
                    }
                } else {
                    Button(
                        onClick = { isFlipped = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show Answer")
                    }
                }
            } else {
                Button(
                    onClick = { onNext(answers) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next")
                }
            }
        }
    }
}
