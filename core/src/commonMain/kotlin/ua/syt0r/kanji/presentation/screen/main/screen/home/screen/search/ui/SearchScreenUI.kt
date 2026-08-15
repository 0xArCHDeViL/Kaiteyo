package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.ui

import ua.syt0r.kanji.presentation.common.theme.Dimens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.app_data.JapaneseName
import ua.syt0r.kanji.core.app_data.SearchScope
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.CollapsibleContainer
import ua.syt0r.kanji.presentation.common.CollapsibleContainerState
import ua.syt0r.kanji.presentation.common.JapaneseWordUI
import ua.syt0r.kanji.presentation.common.isNearListEnd
import ua.syt0r.kanji.presentation.common.rememberCollapsibleContainerState
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.trackItemPosition
import ua.syt0r.kanji.presentation.common.ui.kanji.HighlightedLetter
import ua.syt0r.kanji.presentation.dialog.SaveWordDialog
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.data.RadicalSearchState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.AnimatedVisibility

@Composable
fun SearchScreenUI(
    state: State<ScreenState>,
    radicalsState: State<RadicalSearchState>,
    onSubmitInput: (String) -> Unit,
    onRadicalsSectionExpanded: () -> Unit,
    onRadicalsSelected: (Set<String>) -> Unit,
    onCharacterClick: (String) -> Unit,
    onWordClick: (JapaneseWord) -> Unit,
    onScrolledToEnd: () -> Unit,
    onNamesScrolledToEnd: () -> Unit,
    onWordFeedback: (JapaneseWord) -> Unit,
    startWithRadicals: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    var showRadicalSearch by rememberSaveable { mutableStateOf(startWithRadicals) }

    val inputState = rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    val selectedRadicalsState = rememberSaveable() {
        mutableStateOf(emptySet<String>())
    }

    LaunchedEffect(Unit) {
        snapshotFlow { inputState.value }
            .onEach { onSubmitInput(it.text) }
            .launchIn(this)
        snapshotFlow { selectedRadicalsState.value }
            .onEach { onRadicalsSelected(it) }
            .launchIn(this)

        if (startWithRadicals) {
            onRadicalsSectionExpanded()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            val searchContainerState = rememberCollapsibleContainerState()

            CollapsibleContainer(searchContainerState) {
                InputSection(
                    inputState = inputState,
                    onOpenRadicalSearch = {
                        showRadicalSearch = true
                        onRadicalsSectionExpanded()
                    },
                    onAppendQueryToken = { token ->
                        inputState.value = inputState.value.run {
                            val separator = if (text.isBlank()) "" else " "
                            val nextText = text + separator + token
                            TextFieldValue(nextText, TextRange(nextText.length))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(2.dp)
            ) {
                val isProgressVisible = remember { derivedStateOf { state.value.isLoading } }
                androidx.compose.animation.AnimatedVisibility(
                    visible = isProgressVisible.value,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Transparent
                    )
                }
            }

            AnimatedVisibility(
                visible = state.value.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "Opening the full offline JMdict index…",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ListContent(
                screenState = state.value,
                searchContainerState = searchContainerState,
                onCharacterClick = onCharacterClick,
                onWordClick = onWordClick,
                onScrolledToEnd = onScrolledToEnd,
                onNamesScrolledToEnd = onNamesScrolledToEnd,
                onRetry = { onSubmitInput(state.value.query) }
            )
        }

        if (showRadicalSearch) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showRadicalSearch = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.85f)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(Dimens.Radius2xl),
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = Dimens.Alpha.Subtle),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = Dimens.Alpha.Light)
                        )
                        .clip(RoundedCornerShape(Dimens.Radius2xl))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {}
                ) {
                    Column(Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "Radicals",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.align(Alignment.Center)
                            )
                            IconButton(
                                onClick = { showRadicalSearch = false },
                                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Dimens.Alpha.Subtle))
                        )

                        RadicalSearch(
                            state = radicalsState,
                            selectedRadicals = selectedRadicalsState,
                            onCharacterClick = {
                                inputState.value = inputState.value.run {
                                    TextFieldValue(
                                        text = text + it,
                                        selection = TextRange(text.length + 1)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InputSection(
    inputState: MutableState<TextFieldValue>,
    onOpenRadicalSearch: () -> Unit,
    onAppendQueryToken: (String) -> Unit,
    modifier: Modifier
) {
    var enteredText by inputState
    val interactionSource = remember { MutableInteractionSource() }
    val isInputFocused = remember { mutableStateOf(false) }

    val isHintVisible = !isInputFocused.value && enteredText.text.isEmpty()
    val hasText = enteredText.text.isNotEmpty()

    val elevation by animateFloatAsState(
        targetValue = if (isInputFocused.value) 16f else 4f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )
    val hintAlpha by animateFloatAsState(targetValue = if (isHintVisible) 1f else 0f)
    val clearAlpha by animateFloatAsState(targetValue = if (hasText) 1f else 0f)
    val clearScale by animateFloatAsState(targetValue = if (hasText) 1f else 0.5f)
    val borderAlpha by animateFloatAsState(targetValue = if (isInputFocused.value) 0.5f else 0f)

    val color = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .graphicsLayer {
                shadowElevation = elevation.dp.toPx()
                shape = RoundedCornerShape(28.dp)
                clip = true
            }
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onOpenRadicalSearch,
                    modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GridView,
                        contentDescription = "Radicals",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = enteredText,
                        onValueChange = { enteredText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isInputFocused.value = it.isFocused },
                        maxLines = 1,
                        singleLine = true,
                        interactionSource = interactionSource,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = color)
                    )

                    if (hintAlpha > 0f) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.graphicsLayer { alpha = hintAlpha }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = color.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp).padding(end = 6.dp)
                            )
                            Text(
                                text = resolveString { search.inputHint },
                                style = MaterialTheme.typography.bodyLarge,
                                color = color.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                if (clearAlpha > 0f) {
                    IconButton(
                        onClick = { enteredText = TextFieldValue() },
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer {
                                alpha = clearAlpha
                                scaleX = clearScale
                                scaleY = clearScale
                            }
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isInputFocused.value || hasText) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("#common", "#name", "#k", "#c", "#!vt").forEach { token ->
                        AssistChip(
                            onClick = { onAppendQueryToken(token) },
                            label = { Text(token, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListContent(
    screenState: ScreenState,
    searchContainerState: CollapsibleContainerState,
    onCharacterClick: (String) -> Unit,
    onWordClick: (JapaneseWord) -> Unit,
    onScrolledToEnd: () -> Unit,
    onNamesScrolledToEnd: () -> Unit,
    onRetry: () -> Unit
) {
    val listState = rememberLazyListState()
    val canLoadMoreWords = remember(screenState) {
        derivedStateOf { screenState.words.value.canLoadMore }
    }
    val canLoadMoreNames = remember(screenState) {
        derivedStateOf { screenState.names.value.canLoadMore }
    }

    if (canLoadMoreWords.value) {
        LaunchedEffect(Unit) {
            snapshotFlow { listState.layoutInfo }
                .map { it.isNearListEnd(SearchScreenContract.LoadMoreWordsFromEndThreshold) }
                .filter { it }
                .collect { onScrolledToEnd() }
        }
    }

    if (canLoadMoreNames.value) {
        LaunchedEffect(Unit) {
            snapshotFlow { listState.layoutInfo }
                .map { it.isNearListEnd(SearchScreenContract.LoadMoreWordsFromEndThreshold) }
                .filter { it }
                .collect { onNamesScrolledToEnd() }
        }
    }

    val shouldShowScrollUpButton = remember {
        derivedStateOf { listState.firstVisibleItemIndex > 5 }
    }

    var wordToAddToVocabDeck by remember { mutableStateOf<JapaneseWord?>(null) }
    wordToAddToVocabDeck?.let {
        SaveWordDialog(
            word = it,
            onDismissRequest = { wordToAddToVocabDeck = null }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val contentBottomPadding = remember { mutableStateOf(0.dp) }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(searchContainerState.nestedScrollConnection),
            contentPadding = PaddingValues(bottom = contentBottomPadding.value + 80.dp)
        ) {
            when {
                screenState.errorMessage != null -> item(key = "search-error") {
                    SearchStateCard(
                        title = "Search unavailable",
                        body = "The offline JMdict index could not be opened. Your data was not removed.",
                        actionLabel = "Retry",
                        onAction = onRetry,
                        isError = true
                    )
                }
                !screenState.hasQuery && !screenState.isLoading -> item(key = "search-welcome") {
                    SearchWelcomeState()
                }
                screenState.hasQuery && !screenState.isLoading && screenState.totalResultCount == 0 -> item(key = "search-empty") {
                    SearchStateCard(
                        title = "No matches in the full JMdict index",
                        body = "Try another spelling, reading, romaji form, meaning, or remove a filter tag.",
                        actionLabel = null,
                        onAction = null,
                        isError = false
                    )
                }
            }

            if (screenState.hasQuery && !screenState.isLoading) {
                item(key = "search-mode") {
                    SearchModeSummary(
                        scope = screenState.scope,
                        resultCount = screenState.totalResultCount
                    )
                }
            }

            if (screenState.characters.isNotEmpty()) {
                item {
                    SearchHeader(text = resolveString { search.charactersTitle(screenState.characters.size) })
                }
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        items(screenState.characters) {
                            HighlightedLetter(
                                letter = it,
                                onClick = onCharacterClick,
                                aspectRatioConstraintOrientation = Orientation.Vertical
                            )
                        }
                    }
                }
            }

            val currentNamesState = screenState.names.value
            if (currentNamesState.items.isNotEmpty()) {
                stickyHeader {
                    SearchHeader(
                        text = resolveString { search.namesTitle(currentNamesState.totalCount) },
                        supportingText = loadedCountLabel(
                            loaded = currentNamesState.items.size,
                            total = currentNamesState.totalCount
                        ),
                        isSticky = true
                    )
                }
                items(
                    items = currentNamesState.items,
                    key = { name -> "name-${name.id}" }
                ) { name ->
                    JapaneseNameResult(name)
                }
                if (currentNamesState.canLoadMore) {
                    item(key = "names-footer") {
                        SearchResultFooter(
                            loaded = currentNamesState.items.size,
                            total = currentNamesState.totalCount,
                            isLoading = screenState.isLoading
                        )
                    }
                }
            }

            val currentWordsState = screenState.words.value
            if (currentWordsState.totalCount > 0) {
                stickyHeader {
                    SearchHeader(
                        text = resolveString { search.wordsTitle(currentWordsState.totalCount) },
                        supportingText = loadedCountLabel(
                            loaded = currentWordsState.items.size,
                            total = currentWordsState.totalCount
                        ),
                        isSticky = true
                    )
                }
                
                item { Spacer(Modifier.height(8.dp)) }

                itemsIndexed(
                    items = currentWordsState.items,
                    key = { _, word ->
                        "word-${word.id}-${word.reading.kanjiReading.orEmpty()}-${word.reading.kanaReading}"
                    }
                ) { index, word ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        JapaneseWordUI(
                            index = index,
                            word = word,
                            onClick = { onWordClick(word) },
                            onFuriganaClick = onCharacterClick,
                            addWordToVocabDeckClick = { wordToAddToVocabDeck = word }
                        )
                    }
                }
                if (currentWordsState.canLoadMore) {
                    item(key = "words-footer") {
                        SearchResultFooter(
                            loaded = currentWordsState.items.size,
                            total = currentWordsState.totalCount,
                            isLoading = screenState.isLoading
                        )
                    }
                }
            }
        }

        // Fading edge at the top for sticky header effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            Color.Transparent
                        )
                    )
                )
        )

        AnimatedVisibility(
            visible = shouldShowScrollUpButton.value,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp)
                .trackItemPosition { contentBottomPadding.value = it.heightFromScreenBottom }
        ) {
            val coroutineScope = rememberCoroutineScope()
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch { listState.animateScrollToItem(0) }
                    coroutineScope.launch { searchContainerState.expand() }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Scroll to top")
            }
        }
    }
}

@Composable
private fun SearchWelcomeState() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        shape = RoundedCornerShape(Dimens.RadiusXl),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Search the full JMdict index",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Find vocabulary, readings, romaji, meanings, names, kanji, and component matches without a curated subset boundary.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchModeSummary(scope: SearchScope, resultCount: Int) {
    val scopeLabel = when (scope) {
        SearchScope.Words -> "Vocabulary"
        SearchScope.Kanji -> "Kanji"
        SearchScope.Components -> "Components"
        SearchScope.Names -> "Names"
    }
    Text(
        text = "$scopeLabel · $resultCount total matches in full JMdict",
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SearchStateCard(
    title: String,
    body: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    isError: Boolean
) {
    val containerColor = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    }
    val contentColor = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        shape = RoundedCornerShape(Dimens.RadiusXl),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isError) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
private fun SearchResultFooter(loaded: Int, total: Int, isLoading: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        }
        Text(
            text = "Showing $loaded of $total · scroll to load more",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun loadedCountLabel(loaded: Int, total: Int): String =
    "Showing $loaded of $total"

@Composable
private fun JapaneseNameResult(name: JapaneseName) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(Dimens.RadiusLg),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.widthIn(min = 92.dp, max = 180.dp)) {
                Text(
                    text = name.kanji ?: name.kana,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (name.kanji != null) {
                    Text(
                        text = name.kana,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.meaning,
                    style = MaterialTheme.typography.bodyLarge
                )
                name.nameType?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(
    text: String,
    supportingText: String? = null,
    isSticky: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSticky) MaterialTheme.colorScheme.background.copy(alpha = 0.9f) 
                else Color.Transparent
            )
            .padding(horizontal = 24.dp, vertical = if (isSticky) 12.dp else 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                supportingText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
