package ua.syt0r.kanji.presentation.screen.main.screen.practice_grammar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeQueueProgress

@Composable
fun GrammarPracticeProgressHeader(progress: PracticeQueueProgress) {
    val total = progress.pending + progress.repeats + progress.completed
    val fraction = if (total == 0) 0f else (progress.completed.toFloat() / total).coerceIn(0f, 1f)
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.WindowPadding, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Latihan grammar", style = MaterialTheme.typography.labelLarge)
            Text(
                text = "${progress.pending + progress.repeats} tersisa",
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
