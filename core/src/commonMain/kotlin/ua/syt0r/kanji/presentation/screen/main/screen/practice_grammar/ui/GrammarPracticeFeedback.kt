package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.theme.Dimens

@Composable
fun GrammarPracticeFeedback(
    isCorrect: Boolean,
    expectedAnswer: String,
    onNext: () -> Unit,
    onVoiceClick: ((String) -> Unit)? = null,
) {
    val container = if (isCorrect) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val content = if (isCorrect) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Dimens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space2),
        ) {
            Text(
                text = if (isCorrect) "Benar" else "Belum tepat",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = content,
            )
            if (!isCorrect) {
                Row {
                    Text(
                        text = "Jawaban yang tepat: $expectedAnswer",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = content,
                    )
                    onVoiceClick?.let { play ->
                        IconButton(onClick = { play(expectedAnswer) }) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Dengarkan jawaban")
                        }
                    }
                }
            }
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Lanjut")
            }
        }
    }
}
