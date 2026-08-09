package ua.syt0r.kanji

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.AppAccountScreenContent
import ua.syt0r.kanji.presentation.screen.main.AppAccountScreenContract
import ua.syt0r.kanji.presentation.screen.main.AppAccountScreenViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.account.AccountScreenContract

val flavorModule = module {
    single<AccountScreenContract.Content> { AppAccountScreenContent }

    multiplatformViewModel<AppAccountScreenContract.ViewModel> {
        AppAccountScreenViewModel(
            coroutineScope = it.component1(),
            accountManager = get()
        )
    }

}
