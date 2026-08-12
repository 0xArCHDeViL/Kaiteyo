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
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case.DefaultGetGrammarPracticeClozeDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case.GetGrammarPracticeClozeDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case.DefaultGetGrammarPracticeConjugationDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case.GetGrammarPracticeConjugationDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case.DefaultGetGrammarPracticeScrambleDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case.GetGrammarPracticeScrambleDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case.DefaultGetGrammarPracticeDialogueDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.use_case.GetGrammarPracticeDialogueDataUseCase

val grammarPracticeScreenModule = module {

    factory<GetGrammarPracticeFlashcardDataUseCase> {
        DefaultGetGrammarPracticeFlashcardDataUseCase()
    }

    factory<GetGrammarPracticeClozeDataUseCase> {
        DefaultGetGrammarPracticeClozeDataUseCase()
    }

    factory<GetGrammarPracticeConjugationDataUseCase> {
        DefaultGetGrammarPracticeConjugationDataUseCase()
    }

    factory<GetGrammarPracticeScrambleDataUseCase> {
        DefaultGetGrammarPracticeScrambleDataUseCase()
    }

    factory<GetGrammarPracticeDialogueDataUseCase> {
        DefaultGetGrammarPracticeDialogueDataUseCase()
    }

    factory<DefaultGrammarPracticeQueue> {
        DefaultGrammarPracticeQueue(
            coroutineScope = CoroutineScope(Dispatchers.IO),
            timeUtils = get(),
            srsCardRepository = get(),
            srsScheduler = get(),
            srsMicroMlEngine = get(),
            getFlashcardReviewStateUseCase = get(),
            getClozeReviewStateUseCase = get(),
            getConjugationReviewStateUseCase = get(),
            getScrambleReviewStateUseCase = get(),
            getDialogueReviewStateUseCase = get(),
            reviewHistoryRepository = get(),
            analyticsManager = get()
        )
    }

    factory { (deckId: Long, items: List<GrammarPracticeQueueItemDescriptor>) ->
        GrammarPracticeViewModel(
            deckId = deckId,
            items = items,
            queue = get(),
            appTtsManager = get()
        )
    }

}
