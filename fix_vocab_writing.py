import re

filepath = "core/src/commonMain/kotlin/ua/syt0r/kanji/presentation/screen/main/screen/practice_vocab/ui/VocabPracticeWritingUI.kt"

with open(filepath, 'r') as f:
    content = f.read()

# I will replace the Progress function content.
progress_old = """    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        CenteredBoxWithSide(
            modifier = Modifier,
            placeSideContentAtStart = false,
            centerContent = {
                Text(
                    text = reviewState.meaning,
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center
                )
            },
            sideContent = {
                IconButton(
                    enabled = revealAnswer.value,
                    onClick = onInfoClick
                ) {
                    Icon(Icons.Default.ArrowOutward, null)
                }
            },
        )

        if (reviewState.showKanaReading) {
            Text(
                text = reviewState.kanaReading,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }"""

progress_new = """    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.Space3)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                enabled = revealAnswer.value,
                onClick = onInfoClick
            ) {
                Icon(Icons.Default.ArrowOutward, null)
            }
        }

        ua.syt0r.kanji.presentation.common.FuriganaWordHeadline(
            reading = reviewState.summaryReading,
            glossary = listOf(reviewState.meaning)
        )

        val autoPlayEnabled = remember { androidx.compose.runtime.mutableStateOf(true) } // MOCK
        ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.ui.KanaVoiceMenu(
            autoPlayEnabled = autoPlayEnabled,
            clickable = revealAnswer.value,
            onAutoPlayToggleClick = { autoPlayEnabled.value = !autoPlayEnabled.value },
            onSpeakClick = { /* MOCK */ },
            modifier = Modifier
        )
"""

content = content.replace(progress_old, progress_new)

with open(filepath, 'w') as f:
    f.write(content)
