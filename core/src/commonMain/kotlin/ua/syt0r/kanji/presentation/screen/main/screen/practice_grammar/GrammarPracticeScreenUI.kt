package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.ui.GrammarPracticeFlashcardUI
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.ui.GrammarPracticeClozeUI
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.ui.GrammarPracticeConjugationUI
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.ui.GrammarPracticeScrambleUI
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.ui.GrammarPracticeDialogueUI
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.MutableGrammarReviewState

@Composable
fun GrammarPracticeScreenUI(
    state: GrammarPracticeScreenContract.State,
    onEvent: (GrammarPracticeScreenContract.Event) -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (val queueState = state.queueState) {
            is GrammarPracticeQueueState.Loading -> {
                FancyLoading(Modifier.align(Alignment.Center))
            }
            is GrammarPracticeQueueState.Review -> {
                // answeredCorrectly drives whether to show result UI (null = not answered yet)
                val answeredCorrectly = state.answeredCorrectly

                when (val reviewState = queueState.state) {
                    is MutableGrammarReviewState.Flashcard -> {
                        GrammarPracticeFlashcardUI(
                            state = reviewState,
                            answers = queueState.answers,
                            onAnswer = { answer ->
                                onEvent(GrammarPracticeScreenContract.Event.AnswerSrs(answer))
                            },
                            onVoiceClick = { text ->
                                onEvent(GrammarPracticeScreenContract.Event.PlayVoice(text))
                            }
                        )
                    }
                    is MutableGrammarReviewState.Cloze -> {
                        GrammarPracticeClozeUI(
                            state = reviewState,
                            answeredCorrectly = answeredCorrectly,
                            onAnswerSelected = { index ->
                                val isCorrect = index == reviewState.correctAnswerIndex
                                onEvent(GrammarPracticeScreenContract.Event.AnswerCloze(isCorrect))
                            },
                            onNext = {
                                onEvent(GrammarPracticeScreenContract.Event.ProceedToNext(queueState.answers))
                            },
                            onVoiceClick = { text ->
                                onEvent(GrammarPracticeScreenContract.Event.PlayVoice(text))
                            }
                        )
                    }
                    is MutableGrammarReviewState.ConjugationBuilder -> {
                        GrammarPracticeConjugationUI(
                            state = reviewState,
                            answeredCorrectly = answeredCorrectly,
                            onAnswerSubmit = { isCorrect ->
                                onEvent(GrammarPracticeScreenContract.Event.AnswerConjugation(isCorrect))
                            },
                            onNext = {
                                onEvent(GrammarPracticeScreenContract.Event.ProceedToNext(queueState.answers))
                            },
                            onVoiceClick = { text ->
                                onEvent(GrammarPracticeScreenContract.Event.PlayVoice(text))
                            }
                        )
                    }
                    is MutableGrammarReviewState.SentenceScramble -> {
                        GrammarPracticeScrambleUI(
                            state = reviewState,
                            answeredCorrectly = answeredCorrectly,
                            onAnswerSubmit = { isCorrect ->
                                onEvent(GrammarPracticeScreenContract.Event.AnswerScramble(isCorrect))
                            },
                            onNext = {
                                onEvent(GrammarPracticeScreenContract.Event.ProceedToNext(queueState.answers))
                            },
                            onVoiceClick = { text ->
                                onEvent(GrammarPracticeScreenContract.Event.PlayVoice(text))
                            }
                        )
                    }
                    is MutableGrammarReviewState.SurvivalDialogue -> {
                        GrammarPracticeDialogueUI(
                            state = reviewState,
                            answeredCorrectly = answeredCorrectly,
                            onAnswerSelected = { index ->
                                val isCorrect = index == reviewState.correctAnswerIndex
                                onEvent(GrammarPracticeScreenContract.Event.AnswerDialogue(isCorrect))
                            },
                            onNext = {
                                onEvent(GrammarPracticeScreenContract.Event.ProceedToNext(queueState.answers))
                            },
                            onVoiceClick = { text ->
                                onEvent(GrammarPracticeScreenContract.Event.PlayVoice(text))
                            }
                        )
                    }
                }
            }
            is GrammarPracticeQueueState.Summary -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Practice Complete! Duration: ${queueState.duration}")
                }
            }
        }
    }
}
