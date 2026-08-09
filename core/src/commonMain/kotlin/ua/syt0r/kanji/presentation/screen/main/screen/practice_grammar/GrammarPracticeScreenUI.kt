package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ua.syt0r.kanji.presentation.common.FancyLoading
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.ui.GrammarPracticeFlashcardUI
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.ui.GrammarPracticeClozeUI
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
                when (val reviewState = queueState.state) {
                    is MutableGrammarReviewState.Flashcard -> {
                        GrammarPracticeFlashcardUI(
                            state = reviewState,
                            answers = queueState.answers,
                            onAnswer = { onEvent(GrammarPracticeScreenContract.Event.AnswerFlashcard(it)) },
                            onNext = { onEvent(GrammarPracticeScreenContract.Event.ProceedToNext(it)) }
                        )
                    }
                    is MutableGrammarReviewState.Cloze -> {
                        GrammarPracticeClozeUI(
                            state = reviewState,
                            answers = queueState.answers,
                            onAnswerSelected = { index -> 
                                onEvent(GrammarPracticeScreenContract.Event.AnswerCloze(index == reviewState.correctAnswerIndex)) 
                            },
                            onNext = { onEvent(GrammarPracticeScreenContract.Event.ProceedToNext(it)) }
                        )
                    }
                }
            }
            is GrammarPracticeQueueState.Summary -> {
                // Simplified summary for now
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Practice Complete! Duration: ${queueState.duration}")
                }
            }
        }
    }
}
