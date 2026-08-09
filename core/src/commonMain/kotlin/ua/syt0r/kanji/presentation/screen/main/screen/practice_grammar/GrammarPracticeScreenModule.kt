package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.data.GrammarPracticeQueueItemDescriptor
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case.DefaultGetGrammarPracticeFlashcardDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case.GetGrammarPracticeFlashcardDataUseCase

val grammarPracticeScreenModule = module {

    factory<GetGrammarPracticeFlashcardDataUseCase> {
        DefaultGetGrammarPracticeFlashcardDataUseCase()
    }

    factory<GrammarPracticeQueue> {
        DefaultGrammarPracticeQueue(
            coroutineScope = CoroutineScope(Dispatchers.IO),
            timeUtils = get(),
            srsCardRepository = get(),
            srsScheduler = get(),
            getFlashcardReviewStateUseCase = get(),
            reviewHistoryRepository = get(),
            analyticsManager = get()
        )
    }

    factory { (deckId: Long, items: List<GrammarPracticeQueueItemDescriptor>) ->
        GrammarPracticeViewModel(
            deckId = deckId,
            items = items,
            queue = get()
        )
    }

}
