package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueItemDescriptor
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeScreenConfiguration

@Composable
fun GrammarPracticeScreen(
    configuration: GrammarPracticeScreenConfiguration,
    onNavigateBack: () -> Unit
) {
    val items = configuration.items.map {
        when (it) {
            is GrammarPracticeScreenConfiguration.Item.Flashcard -> GrammarPracticeQueueItemDescriptor.Flashcard(
                pointNumber = it.pointNumber,
                deckId = configuration.deckId,
                showMeaningInFront = it.showMeaningInFront
            )
            is GrammarPracticeScreenConfiguration.Item.Cloze -> GrammarPracticeQueueItemDescriptor.Cloze(
                pointNumber = it.pointNumber,
                deckId = configuration.deckId
            )
            is GrammarPracticeScreenConfiguration.Item.ConjugationBuilder -> GrammarPracticeQueueItemDescriptor.ConjugationBuilder(
                pointNumber = it.pointNumber,
                deckId = configuration.deckId
            )
            is GrammarPracticeScreenConfiguration.Item.SentenceScramble -> GrammarPracticeQueueItemDescriptor.SentenceScramble(
                pointNumber = it.pointNumber,
                deckId = configuration.deckId
            )
            is GrammarPracticeScreenConfiguration.Item.SurvivalDialogue -> GrammarPracticeQueueItemDescriptor.SurvivalDialogue(
                pointNumber = it.pointNumber,
                deckId = configuration.deckId
            )
        }
    }

    val viewModel = koinInject<GrammarPracticeViewModel>(
        parameters = { parametersOf(configuration.deckId, items) }
    )
    val state by viewModel.state.collectAsState()

    GrammarPracticeScreenUI(
        state = state,
        onEvent = viewModel::setEvent,
        onNavigateBack = onNavigateBack
    )
}
