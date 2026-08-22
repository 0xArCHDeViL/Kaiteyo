package ua.syt0r.kanji.core.user_data.database

import ua.syt0r.kanji.core.srs.srsApplicationScopeQualifier
import org.koin.core.module.Module
import ua.syt0r.kanji.core.user_data.database.migration.UserDataDatabaseMigrationProvider
import ua.syt0r.kanji.core.user_data.database.sqldelight.SqlDelightConnectedReviewRepository
import ua.syt0r.kanji.core.user_data.database.sqldelight.SqlDelightFsrsCardRepository
import ua.syt0r.kanji.core.user_data.database.sqldelight.SqlDelightLetterPracticeRepository
import ua.syt0r.kanji.core.user_data.database.sqldelight.SqlDelightReviewCommitRepository
import ua.syt0r.kanji.core.user_data.database.sqldelight.SqlDelightReviewHistoryRepository
import ua.syt0r.kanji.core.user_data.database.sqldelight.SqlDelightTextAnalysisRepository
import ua.syt0r.kanji.core.user_data.database.sqldelight.SqlDelightVocabPracticeRepository
import ua.syt0r.kanji.core.user_data.database.use_case.DefaultUpdateLocalDataTimestampUseCase
import ua.syt0r.kanji.core.user_data.database.use_case.UpdateLocalDataTimestampUseCase

fun Module.addUserDataDatabaseDefinitions() {

    single<UserDataDatabaseContract.Manager> {
        DefaultUserDataDatabaseManager(
            databasePlatformHandler = get(),
            updateLocalDataTimestampUseCase = get(),
            coroutineScope = get(srsApplicationScopeQualifier),
        )
    }

    single<UserDataDatabaseContract.MigrationProvider> {
        UserDataDatabaseMigrationProvider(
            preferences = get(),
            appDataRepository = get(),
            observable = get()
        )
    }

    single<UserDataDatabaseContract.MigrationObservable> {
        DefaultMigrationObservable()
    }

    single<LetterPracticeRepository> {
        SqlDelightLetterPracticeRepository(
            databaseManager = get(),
            coroutineScope = get(srsApplicationScopeQualifier)
        )
    }

    single<VocabPracticeRepository> {
        SqlDelightVocabPracticeRepository(
            databaseManager = get(),
            coroutineScope = get(srsApplicationScopeQualifier)
        )
    }

    factory<UpdateLocalDataTimestampUseCase> {
        DefaultUpdateLocalDataTimestampUseCase(
            appPreferences = get(),
            timeUtils = get()
        )
    }

    single<FsrsCardRepository> {
        SqlDelightFsrsCardRepository(
            userDataDatabaseManager = get(),
            coroutineScope = get(srsApplicationScopeQualifier)
        )
    }

    single<ConnectedReviewRepository> {
        SqlDelightConnectedReviewRepository(
            userDataDatabaseManager = get(),
            coroutineScope = get(srsApplicationScopeQualifier)
        )
    }

    single<CardDatabaseManager> {
        CardDatabaseManagerImpl(
            transactionScope = ObservableUserDataRepository(
                databaseManager = get(),
                coroutineScope = get(srsApplicationScopeQualifier),
            )
        )
    }

    single<ReviewCommitRepository> {
        SqlDelightReviewCommitRepository(
            databaseManager = get()
        )
    }

    single<ReviewHistoryRepository> {
        SqlDelightReviewHistoryRepository(
            userDataDatabaseManager = get()
        )
    }

    single<TextAnalysisRepository> {
        SqlDelightTextAnalysisRepository(
            manager = get(),
            coroutineScope = get(srsApplicationScopeQualifier)
        )
    }

}