package ua.syt0r.kanji.presentation.screen.main

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.connected_learning.ConnectedLearningLoader
import ua.syt0r.kanji.presentation.screen.main.screen.connected_learning.AppDataConnectedVocabularyMetadataRepository
import ua.syt0r.kanji.presentation.screen.main.screen.connected_learning.ConnectedLearningScreenViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.connected_learning.ConnectedVocabularyMetadataRepository
import ua.syt0r.kanji.presentation.screen.main.screen.connected_learning.DefaultConnectedLearningLoader
import ua.syt0r.kanji.presentation.screen.main.screen.connected_learning.DefaultConnectedLearningViewModel
import ua.syt0r.kanji.presentation.screen.main.features.DeepLinkHandler
import ua.syt0r.kanji.presentation.screen.main.features.DeckFeaturesController
import ua.syt0r.kanji.presentation.screen.main.features.KaiteyoDataCenter

val mainScreenModule = module {

    single<ConnectedVocabularyMetadataRepository> {
        AppDataConnectedVocabularyMetadataRepository(
            appDataRepository = get(),
        )
    }

    single<ConnectedLearningLoader> {
        DefaultConnectedLearningLoader(
            graphRepository = get(),
            reviewRepository = get(),
            vocabularyRepository = get(),
        )
    }

    multiplatformViewModel<ConnectedLearningScreenViewModel> {
        DefaultConnectedLearningViewModel(
            viewModelScope = it.component1(),
            loader = get(),
        )
    }

    multiplatformViewModel<MainContract.ViewModel> {
        MainScreenViewModel(
            viewModelScope = it.component1(),
            appPreferences = get(),
            accountManager = get(),
            migrationObservable = get(),
            syncManager = get()
        )
    }

    single { DeepLinkHandler() }

    single {
        KaiteyoDataCenter(
            appDataRepository = get(),
            fsrsCardRepository = get(),
            cardDatabaseManager = get(),
            reviewHistoryRepository = get(),
            timeUtils = get(),
            learningGraphRepository = get(),
        )
    }

    single {
        DeckFeaturesController(
            dataCenter = get(),
            cardDatabaseManager = get(),
            reviewHistoryRepository = get(),
            fsrsCardRepository = get(),
            appPreferences = get(),
            timeUtils = get()
        )
    }

}