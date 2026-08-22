package ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.material3.DropdownMenu
import ua.syt0r.kanji.core.app_data.data.formattedVocabStringReading
import ua.syt0r.kanji.presentation.common.copyCentered
import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.common.ui.VerticalScrollbar
import ua.syt0r.kanji.presentation.screen.main.screen.text_analysis.TextAnalysisNode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextAnalysisVocabCardPopup(
    showPopup: MutableState<Boolean>,
    node: TextAnalysisNode.Word,
    saveWord: (TextAnalysisNode.CardData) -> Unit
) {

    if (showPopup.value.not()) return

    val scrollState = rememberScrollState()

    DropdownMenu(
        expanded = showPopup.value,
        onDismissRequest = { showPopup.value = false },
        modifier = Modifier
            .widthIn(min = 280.dp, max = 360.dp)
            .heightIn(max = 480.dp),
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(vertical = Dimens.ContentPaddingSmall)
                    .padding(start = Dimens.ContentPaddingSmall),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space1),
            ) {
                node.cards.forEachIndexed { i, cardData ->
                    CardDataUI(
                        cardData = cardData,
                        onAddClick = {
                            saveWord(cardData)
                            showPopup.value = false
                        },
                    )
                    if (i != node.cards.lastIndex) {
                        Spacer(modifier = Modifier.height(Dimens.Space3))
                    }
                }
            }
            VerticalScrollbar(
                scrollState = scrollState,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = Dimens.Space2)
                    .padding(end = Dimens.Space1),
            )
        }
    }
}

@Composable
fun CardDataUI(
    cardData: TextAnalysisNode.CardData,
    onAddClick: () -> Unit
) {

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space2)
    ) {
        val reading = cardData.reading
        Text(
            text = formattedVocabStringReading(
                kanaReading = reading.kanaReading,
                kanjiReading = reading.kanjiReading
            ),
            style = MaterialTheme.typography.bodyLarge.copyCentered(),
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        )
                    IconButton(
                onClick = onAddClick,
                modifier = Modifier.align(Alignment.CenterVertically),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add ${reading.kanjiReading ?: reading.kanaReading} to deck",
                )
            }

    }

    cardData.notes.takeIf { it.isNotEmpty() }?.let { notes ->
        Text(
            text = notes.joinToString(),
            style = MaterialTheme.typography.labelMedium
        )
    }

    if (cardData.partOfSpeech.isNotEmpty()) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.Space2)) {
            cardData.partOfSpeech.forEach {
                val highlightColor = it.toHighlightColor(MaterialTheme.colorScheme.surfaceVariant)

                Text(
                    text = it.name,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .clip(RoundedCornerShape(Dimens.Space1))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .drawWithContent {
                            drawContent()
                            val highlightHeightPx = Dimens.Space1.toPx()
                            drawRect(
                                color = highlightColor,
                                topLeft = Offset(0f, size.height - highlightHeightPx),
                                size = size.copy(height = highlightHeightPx)
                            )
                        }
                        .padding(vertical = Dimens.Space1)
                        .padding(
                            horizontal = Dimens.Space2,
                            vertical = Dimens.Space1
                        )
                        .alignByBaseline()
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(Dimens.Space2))

    cardData.glossary.forEachIndexed { index, definition ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space2)
        ) {
            val style = MaterialTheme.typography.bodySmall
            Text(index.plus(1).toString(), style = style)
            Text(definition, Modifier.weight(1f), style = style)
        }
    }

}