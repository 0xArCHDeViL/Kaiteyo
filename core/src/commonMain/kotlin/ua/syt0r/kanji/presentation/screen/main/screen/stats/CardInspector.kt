package ua.syt0r.kanji.presentation.screen.main.screen.stats

import ua.syt0r.kanji.presentation.common.theme.Dimens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.presentation.common.theme.LocalKaiteyoAccent
import ua.syt0r.kanji.presentation.common.theme.LocalSurfaceColors
import androidx.compose.material3.HorizontalDivider

// ============================================
// KAITEYO CARD INSPECTOR v1.2
// Every card shows: history, intervals, tags,
// flags, notes, accuracy, review graph, deck,
// creation date, last review, statistics
// ============================================

data class CardInspectionData(
    val character: String = "水",
    val meaning: String = "Water",
    val kunReading: String = "みず",
    val onReading: String = "スイ",
    val deck: String = "N5 Kanji",
    val createdAt: String = "2026-01-15",
    val lastReview: String = "2026-07-28",
    val totalReviews: Int = 47,
    val correctReviews: Int = 38,
    val accuracy: Float = 0.81f,
    val currentInterval: Int = 21,
    val maxInterval: Int = 180,
    val ease: Float = 2.5f,
    val lapses: Int = 3,
    val streak: Int = 7,
    val averageResponseTime: Int = 4200,
    val tags: List<String> = listOf("jlpt-n5", "water", "radical-水"),
    val flags: List<String> = listOf("favorite", "review-again"),
    val notes: String = "This kanji appears frequently in weather vocabulary."
)

@Composable
fun CardInspector(
    cardData: CardInspectionData = CardInspectionData()
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Header: Character + Meaning
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(Dimens.RadiusLg))
                    .background(accent.primary.copy(alpha = Dimens.Alpha.Subtle)),
                contentAlignment = Alignment.Center
            ) {
                Text(cardData.character, color = accent.primary, style = androidx.compose.material3.MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(cardData.meaning, color = surfaceColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${cardData.kunReading} · ${cardData.onReading}", color = surfaceColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                Text(cardData.deck, color = surfaceColors.textMuted, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = surfaceColors.border.copy(alpha = Dimens.Alpha.Medium))
        Spacer(modifier = Modifier.height(16.dp))

        // Stats grid
        Text("Review Statistics", style = MaterialTheme.typography.titleMedium,
            color = surfaceColors.textPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))

        val stats = listOf(
            "Total Reviews" to "${cardData.totalReviews}",
            "Correct" to "${cardData.correctReviews}",
            "Accuracy" to "${(cardData.accuracy * 100).toInt()}%",
            "Current Interval" to "${cardData.currentInterval}d",
            "Max Interval" to "${cardData.maxInterval}d",
            "Ease" to String.format("%.1f", cardData.ease),
            "Lapses" to "${cardData.lapses}",
            "Streak" to "${cardData.streak} days",
            "Avg Response" to "${cardData.averageResponseTime / 1000}s"
        )

        stats.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (label, value) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Dimens.RadiusMd))
                            .background(surfaceColors.surface)
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(value, color = accent.primary, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(label, color = surfaceColors.textMuted, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = surfaceColors.border.copy(alpha = Dimens.Alpha.Medium))
        Spacer(modifier = Modifier.height(16.dp))

        // Review graph (simplified)
        Text("Review History", style = MaterialTheme.typography.titleMedium,
            color = surfaceColors.textPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(Dimens.RadiusMd))
                .background(surfaceColors.surface)
                .padding(16.dp)
        ) {
            // Mini bar chart
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                listOf(0.3f, 0.5f, 0.4f, 0.7f, 0.6f, 0.8f, 0.5f, 0.9f, 0.7f, 0.6f, 0.8f, 0.4f).forEachIndexed { i, height ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((height * 80).dp)
                                .clip(RoundedCornerShape(Dimens.RadiusXs))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(accent.primary, accent.secondary)
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${i + 1}", color = surfaceColors.textMuted, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = surfaceColors.border.copy(alpha = Dimens.Alpha.Medium))
        Spacer(modifier = Modifier.height(16.dp))

        // Tags
        Text("Tags", style = MaterialTheme.typography.titleMedium,
            color = surfaceColors.textPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            cardData.tags.forEach { tag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Dimens.RadiusSm))
                        .background(accent.primary.copy(alpha = Dimens.Alpha.Subtle))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(tag, color = accent.primary, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Flags
        Text("Flags", style = MaterialTheme.typography.titleMedium,
            color = surfaceColors.textPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            cardData.flags.forEach { flag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Dimens.RadiusSm))
                        .background(accent.secondary.copy(alpha = Dimens.Alpha.Subtle))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(flag, color = accent.secondary, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Notes
        Text("Notes", style = MaterialTheme.typography.titleMedium,
            color = surfaceColors.textPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimens.RadiusMd))
                .background(surfaceColors.surface)
                .padding(14.dp)
        ) {
            Text(cardData.notes, color = surfaceColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, lineHeight = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = surfaceColors.border.copy(alpha = Dimens.Alpha.Medium))
        Spacer(modifier = Modifier.height(12.dp))

        // Timeline
        Text("Timeline", style = MaterialTheme.typography.titleMedium,
            color = surfaceColors.textPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        listOf(
            "Created" to cardData.createdAt,
            "First Review" to "2026-01-16",
            "Last Review" to cardData.lastReview,
            "Next Review" to "2026-08-18"
        ).forEach { (label, date) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accent.primary.copy(alpha = Dimens.Alpha.SemiOpaque))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(label, color = surfaceColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text(date, color = surfaceColors.textMuted, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
        }
    }
}