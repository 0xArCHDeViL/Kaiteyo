package ua.syt0r.kanji.presentation.preview.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Preview
import ua.syt0r.kanji.core.app_data.JapaneseName
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseNameList
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseWordList
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.common.ui.kanji.PreviewKanji
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.data.RadicalSearchState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.ui.SearchScreenUI

@Composable
private fun GenericPreview(
    screenState: ScreenState
) {
    AppTheme {
        SearchScreenUI(
            state = rememberUpdatedState(screenState),
            radicalsState = rememberUpdatedState(RadicalSearchState.random()),
            onSubmitInput = {},
            onRadicalsSectionExpanded = {},
            onRadicalsSelected = {},
            onCharacterClick = {},
            onWordClick = {},
            onScrolledToEnd = {},
            onNamesScrolledToEnd = {},
            onWordFeedback = {}
        )
    }
}

@Preview
@Composable
private fun EmptyStatePreview() {
    GenericPreview(
        screenState = ScreenState(
            isLoading = true,
            characters = emptyList(),
            names = rememberUpdatedState(PaginatableJapaneseNameList(0, emptyList())),
            words = rememberUpdatedState(PaginatableJapaneseWordList(0, emptyList())),
            query = ""
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun LoadedStatePreview() {
    GenericPreview(
        screenState = ScreenState(
            isLoading = false,
            characters = (0 until 10).map { PreviewKanji.randomKanji() },
            names = rememberUpdatedState(
                PaginatableJapaneseNameList(
                    totalCount = 4,
                    items = listOf(
                        JapaneseName(
                            id = 1,
                            kanji = "東京",
                            kana = "とうきょう",
                            nameType = "place name",
                            meaning = "Tokyo"
                        )
                    )
                )
            ),
            words = rememberUpdatedState(
                PaginatableJapaneseWordList(200, PreviewKanji.randomWords(20))
            ),
            query = ""
        )
    )
}