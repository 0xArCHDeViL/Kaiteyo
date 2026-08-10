package ua.syt0r.kanji.presentation.screen.main.screen.decks

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
// KAITEYO DECK MANAGER v1.2
// Nested decks, folders, subfolders, virtual decks,
// smart decks, dynamic decks, pinned, favorite,
// hidden, archived, colors, icons, descriptions,
// statistics, notes, merge, split, bulk operations
// ============================================

data class KaiteyoDeck(
    val id: String = "deck_001",
    val name: String = "N5 Kanji",
    val description: String = "JLPT N5 level kanji characters",
    val parentId: String? = null,
    val color: Color = ua.syt0r.kanji.presentation.common.theme.semanticSuccess,
    val icon: String = "漢",
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val isArchived: Boolean = false,
    val isVirtual: Boolean = false,
    val isDynamic: Boolean = false,
    val isSmart: Boolean = false,
    val cardCount: Int = 120,
    val newCount: Int = 23,
    val reviewCount: Int = 45,
    val dueCount: Int = 12,
    val learningCount: Int = 8,
    val matureCount: Int = 89,
    val accuracy: Float = 0.85f,
    val retention: Float = 0.91f,
    val averageInterval: Int = 45,
    val totalStudyTime: Long = 3600000L,
    val createdAt: String = "2026-01-01",
    val lastStudied: String = "2026-07-28",
    val children: MutableList<KaiteyoDeck> = mutableListOf(),
    val filters: DeckFilters? = null
)

data class DeckFilters(
    val tags: List<String> = emptyList(),
    val flags: List<String> = emptyList(),
    val minInterval: Int = 0,
    val maxInterval: Int = 36500,
    val minDifficulty: Float = 0f,
    val maxDifficulty: Float = 1f,
    val onlyNew: Boolean = false,
    val onlyDue: Boolean = false,
    val onlySuspended: Boolean = false,
    val onlyFlagged: Boolean = false,
    val onlyTagged: String = "",
    val regex: String = ""
)

@Composable
fun DeckManager() {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    var selectedDeck by remember { mutableStateOf<KaiteyoDeck?>(null) }
    var viewMode by remember { mutableStateOf("Tree") }

    val rootDecks = remember {
        listOf(
            KaiteyoDeck(name = "JLPT", icon = "N", isPinned = true, cardCount = 650,
                children = mutableListOf(
                    KaiteyoDeck(name = "N5 Kanji", parentId = "jlpt", color = ua.syt0r.kanji.presentation.common.theme.semanticSuccess, cardCount = 120,
                        children = mutableListOf(
                            KaiteyoDeck(name = "N5 Week 1", parentId = "n5_kanji", cardCount = 15),
                            KaiteyoDeck(name = "N5 Week 2", parentId = "n5_kanji", cardCount = 15),
                            KaiteyoDeck(name = "N5 Week 3", parentId = "n5_kanji", cardCount = 15)
                        )),
                    KaiteyoDeck(name = "N4 Kanji", parentId = "jlpt", color = ua.syt0r.kanji.presentation.common.theme.semanticWarning, cardCount = 180),
                    KaiteyoDeck(name = "N3 Kanji", parentId = "jlpt", color = ua.syt0r.kanji.presentation.common.theme.semanticInfo, cardCount = 350)
                )),
            KaiteyoDeck(name = "Vocabulary", icon = "語", isFavorite = true, cardCount = 1200,
                children = mutableListOf(
                    KaiteyoDeck(name = "Core 2000", parentId = "vocab", cardCount = 500),
                    KaiteyoDeck(name = "Core 6000", parentId = "vocab", cardCount = 700)
                )),
            KaiteyoDeck(name = "Smart: Difficult", isSmart = true, isVirtual = true, icon = "⚡", cardCount = 45),
            KaiteyoDeck(name = "Smart: Forgotten", isSmart = true, isVirtual = true, icon = "🔄", cardCount = 23),
            KaiteyoDeck(name = "Archived 2025", isArchived = true, icon = "📦", cardCount = 0)
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Text("Deck Manager", style = MaterialTheme.typography.titleLarge,
            color = surfaceColors.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("${rootDecks.sumOf { it.cardCount }} cards across ${rootDecks.size + rootDecks.sumOf { it.children.size }} decks",
            color = surfaceColors.textMuted, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(16.dp))

        // View mode tabs
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Tree", "List", "Grid", "Smart", "Archived").forEach { mode ->
                val isSelected = viewMode == mode
                Box(modifier = Modifier.clip(RoundedCornerShape(Dimens.RadiusSm))
                    .background(if (isSelected) accent.primary.copy(alpha = Dimens.Alpha.Light) else surfaceColors.surface)
                    .clickable { viewMode = mode }.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Text(mode, color = if (isSelected) accent.primary else surfaceColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = surfaceColors.border.copy(alpha = Dimens.Alpha.Medium))
        Spacer(modifier = Modifier.height(8.dp))

        // Deck list
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(rootDecks) { deck -> DeckTreeItem(deck, accent, surfaceColors, 0) }
        }
    }
}

@Composable
private fun DeckTreeItem(deck: KaiteyoDeck, accent: ua.syt0r.kanji.presentation.common.theme.KaiteyoAccentScheme,
                          surfaceColors: ua.syt0r.kanji.presentation.common.theme.SurfaceColors, depth: Int = 0) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(Dimens.RadiusMd))
                .background(if (deck.isSmart) surfaceColors.surfaceElevated.copy(alpha = Dimens.Alpha.SemiOpaque) else surfaceColors.surface)
                .clickable { }.padding(horizontal = 12.dp + (depth * 20).dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Deck color indicator
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(deck.color))
            Spacer(modifier = Modifier.width(10.dp))
            // Icon
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(Dimens.RadiusSm))
                .background(if (deck.isVirtual) deck.color.copy(alpha = Dimens.Alpha.Light) else deck.color.copy(alpha = Dimens.Alpha.Subtle)),
                contentAlignment = Alignment.Center) {
                Text(deck.icon, color = if (deck.isVirtual) deck.color else deck.color, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.width(10.dp))
            // Name + stats
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(deck.name, color = surfaceColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    if (deck.isPinned) { Spacer(modifier = Modifier.width(4.dp)); Text("📌", style = androidx.compose.material3.MaterialTheme.typography.labelSmall) }
                    if (deck.isFavorite) { Spacer(modifier = Modifier.width(4.dp)); Text("★", color = Color(0xFFFFB347), style = androidx.compose.material3.MaterialTheme.typography.bodySmall) }
                    if (deck.isVirtual) { Spacer(modifier = Modifier.width(4.dp)); Text("⚡", style = androidx.compose.material3.MaterialTheme.typography.labelSmall) }
                    if (deck.isArchived) { Spacer(modifier = Modifier.width(4.dp)); Text("📦", style = androidx.compose.material3.MaterialTheme.typography.labelSmall) }
                }
                if (deck.description.isNotEmpty()) {
                    Text(deck.description, color = surfaceColors.textMuted, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, maxLines = 1)
                }
            }
            // Stats
            Column(horizontalAlignment = Alignment.End) {
                Text("${deck.cardCount}", color = surfaceColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                Text("${deck.dueCount} due", color = if (deck.dueCount > 0) accent.primary else surfaceColors.textMuted, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
            }
        }
        // Children
        deck.children.forEach { child -> DeckTreeItem(child, accent, surfaceColors, depth + 1) }
    }
}

// ============================================
// BULK OPERATIONS
// ============================================

enum class BulkOperation(val displayName: String) {
    AddTag("Add Tag"),
    RemoveTag("Remove Tag"),
    SetFlag("Set Flag"),
    ClearFlag("Clear Flag"),
    MoveToDeck("Move to Deck"),
    CopyToDeck("Copy to Deck"),
    Suspend("Suspend"),
    Unsuspend("Unsuspend"),
    Bury("Bury"),
    Unbury("Unbury"),
    Delete("Delete"),
    Archive("Archive"),
    Restore("Restore"),
    ChangeDifficulty("Change Difficulty"),
    SetPriority("Set Priority"),
    Reschedule("Reschedule"),
    Reset("Reset Progress")
}

data class BulkAction(
    val operation: BulkOperation,
    val parameters: Map<String, String> = emptyMap(),
    val selectedCardIds: List<String> = emptyList(),
    val previewBeforeExecute: Boolean = true,
    val confirmBeforeExecute: Boolean = true
)

// ============================================
// SMART DECKS (FILTER-BASED)
// ============================================

data class SmartDeckRule(
    val field: String = "", // tags, flags, difficulty, interval, deck, etc.
    val operator: String = "contains", // contains, equals, gt, lt, regex, etc.
    val value: String = "",
    val combineWith: String = "AND" // AND, OR, NOT
)

data class SmartDeckDefinition(
    val name: String,
    val rules: List<SmartDeckRule> = emptyList(),
    val sortBy: String = "due",
    val limit: Int = 100,
    val autoUpdate: Boolean = true
)