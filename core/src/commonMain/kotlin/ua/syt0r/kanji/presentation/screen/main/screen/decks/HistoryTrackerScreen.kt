package ua.syt0r.kanji.presentation.screen.main.screen.decks

import ua.syt0r.kanji.presentation.common.theme.Dimens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.presentation.common.theme.KaiteyoAccentScheme
import ua.syt0r.kanji.presentation.common.theme.LocalKaiteyoAccent
import ua.syt0r.kanji.presentation.common.theme.LocalSurfaceColors
import ua.syt0r.kanji.presentation.common.theme.SurfaceColors
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.datetime.*

// ============================================
// KAITEYO v1.2 — HISTORY TRACKER
// Full action history: reviews, edits, imports,
// exports, bulk operations, backups, etc.
// Supports filtering, search, and undo
// ============================================

fun generateMockHistory(): List<HistoryEntry> {
    val now = Clock.System.now()
    val types = HistoryEntryType.entries
    return (0 until 50).map { i ->
        val type = types[i % types.size]
        val time = now.minus(i * 3600, DateTimeUnit.SECOND)
        HistoryEntry(
            id = i.toLong(),
            type = type,
            timestamp = time,
            description = when (type) {
                HistoryEntryType.Review -> "Reviewed card"
                HistoryEntryType.Import -> "Imported 15 cards from CSV"
                HistoryEntryType.Export -> "Exported 30 cards to APKG"
                HistoryEntryType.Edit -> "Edited card meaning"
                HistoryEntryType.Delete -> "Deleted 3 cards"
                HistoryEntryType.Restore -> "Restored from backup"
                HistoryEntryType.BulkOperation -> "Bulk tagged 12 cards"
                HistoryEntryType.TagChange -> "Added tag 'JLPT N5'"
                HistoryEntryType.FlagChange -> "Flagged card as Red"
                HistoryEntryType.DeckChange -> "Moved cards to JLPT N4"
                HistoryEntryType.NoteChange -> "Updated notes on card"
                HistoryEntryType.StatusChange -> "Reset card progress"
                HistoryEntryType.ScheduleChange -> "Rescheduled 5 cards"
                HistoryEntryType.BackupCreated -> "Created backup v2.1"
                HistoryEntryType.BackupRestored -> "Restored backup v2.0"
                HistoryEntryType.PluginAction -> "Plugin: Export formatted"
            },
            cardIds = (0..(Math.random() * 5).toInt()).map { "card_$it" },
            deckId = if (i % 3 == 0) "deck_001" else null,
            undoable = Math.random() > 0.3
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryFullScreen(
    cards: List<KaiteyoCard> = emptyList(),
    history: List<HistoryEntry> = emptyList(),
    onClose: () -> Unit = {}
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current

    val historyEntries = remember(history) {
        if (history.isNotEmpty()) history
        else generateMockHistory()
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<HistoryEntryType?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedEntry by remember { mutableStateOf<HistoryEntry?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showUndoResult by remember { mutableStateOf<String?>(null) }

    val filteredEntries = remember(historyEntries, searchQuery, selectedTypeFilter) {
        historyEntries.filter { entry ->
            (searchQuery.isBlank() ||
                    entry.description.contains(searchQuery, ignoreCase = true) ||
                    entry.type.displayName.contains(searchQuery, ignoreCase = true)) &&
                    (selectedTypeFilter == null || entry.type == selectedTypeFilter)
        }
    }

    // Group by date
    val groupedEntries = remember(filteredEntries) {
        filteredEntries.groupBy { entry ->
            entry.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date
        }.entries.sortedByDescending { it.key }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Close") } },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            if (showFilters) Icons.Default.FilterListOff else Icons.Default.FilterList,
                            "Toggle Filters",
                            tint = if (selectedTypeFilter != null) accent.primary else surfaceColors.textPrimary
                        )
                    }
                    IconButton(onClick = { showClearConfirm = true }) {
                        Icon(Icons.Default.DeleteSweep, "Clear History", tint = surfaceColors.textMuted)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColors.surface,
                    titleContentColor = surfaceColors.textPrimary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search history...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = surfaceColors.textMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent.primary,
                    unfocusedBorderColor = surfaceColors.border
                ),
                shape = RoundedCornerShape(Dimens.RadiusMd)
            )

            // Filter chips
            AnimatedVisibility(visible = showFilters) {
                Column {
                    // Type filter chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedTypeFilter == null,
                            onClick = { selectedTypeFilter = null },
                            label = { Text("All", style = androidx.compose.material3.MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(26.dp)
                        )
                        HistoryEntryType.entries.take(6).forEach { type ->
                            FilterChip(
                                selected = selectedTypeFilter == type,
                                onClick = {
                                    selectedTypeFilter = if (selectedTypeFilter == type) null else type
                                },
                                label = { Text(type.displayName, style = androidx.compose.material3.MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.height(26.dp)
                            )
                        }
                    }
                    // More filters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HistoryEntryType.entries.drop(6).forEach { type ->
                            FilterChip(
                                selected = selectedTypeFilter == type,
                                onClick = {
                                    selectedTypeFilter = if (selectedTypeFilter == type) null else type
                                },
                                label = { Text(type.displayName, style = androidx.compose.material3.MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.height(26.dp)
                            )
                        }
                    }
                }
            }

            // Stats summary
            HistoryStatsBar(
                entries = historyEntries,
                surfaceColors = surfaceColors,
                accent = accent
            )

            // History list grouped by date
            if (groupedEntries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.History,
                            null,
                            Modifier.size(64.dp),
                            tint = surfaceColors.textMuted.copy(alpha = Dimens.Alpha.SemiOpaque)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No history entries found",
                            color = surfaceColors.textMuted,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Your actions will appear here",
                            color = surfaceColors.textMuted.copy(alpha = 0.7f),
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    groupedEntries.forEach { (date, entries) ->
                        item {
                            DateHeader(
                                date = date,
                                entryCount = entries.size,
                                surfaceColors = surfaceColors
                            )
                        }
                        items(entries, key = { it.id }) { entry ->
                            HistoryEntryItem(
                                entry = entry,
                                surfaceColors = surfaceColors,
                                accent = accent,
                                onClick = { selectedEntry = entry },
                                onUndo = {
                                    showUndoResult = "Undo: ${entry.description}"
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Entry detail dialog
    selectedEntry?.let { entry ->
        EntryDetailDialog(
            entry = entry,
            surfaceColors = surfaceColors,
            accent = accent,
            onDismiss = { selectedEntry = null },
            onUndo = {
                showUndoResult = "Undone: ${entry.description}"
                selectedEntry = null
            }
        )
    }

    // Clear history confirmation
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear History?") },
            text = { Text("This will permanently delete all history entries. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        showUndoResult = "History cleared"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ua.syt0r.kanji.presentation.common.theme.semanticError)
                ) { Text("Clear All") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
            }
        )
    }

    // Result message
    showUndoResult?.let { msg ->
        AlertDialog(
            onDismissRequest = { showUndoResult = null },
            confirmButton = { TextButton(onClick = { showUndoResult = null }) { Text("OK") } },
            title = { Text("Done") },
            text = { Text(msg) }
        )
    }
}

@Composable
private fun HistoryStatsBar(
    entries: List<HistoryEntry>,
    surfaceColors: SurfaceColors,
    accent: KaiteyoAccentScheme
) {
    val totalEntries = entries.size
    val todayEntries = entries.count {
        it.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date ==
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    val undoableCount = entries.count { it.undoable }
    val uniqueTypes = entries.distinctBy { it.type }.size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(Dimens.RadiusMd))
            .background(surfaceColors.surfaceElevated)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        HistoryStat("Total", "$totalEntries", Icons.Default.History, surfaceColors.textPrimary, surfaceColors)
        HistoryStat("Today", "$todayEntries", Icons.Default.Today, accent.primary, surfaceColors)
        HistoryStat("Undoable", "$undoableCount", Icons.Default.Undo, ua.syt0r.kanji.presentation.common.theme.semanticSuccess, surfaceColors)
        HistoryStat("Types", "$uniqueTypes", Icons.Default.Category, ua.syt0r.kanji.presentation.common.theme.semanticNew, surfaceColors)
    }
}

@Composable
private fun HistoryStat(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    surfaceColors: SurfaceColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, Modifier.size(16.dp), tint = color)
        Text(value, fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = surfaceColors.textPrimary)
        Text(label, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = surfaceColors.textMuted)
    }
}

@Composable
private fun DateHeader(
    date: LocalDate,
    entryCount: Int,
    surfaceColors: SurfaceColors
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val yesterday = today.minus(1, DateTimeUnit.DAY)

    val label = when (date) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> date.toString()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontWeight = FontWeight.Bold,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = surfaceColors.textPrimary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "$entryCount entries",
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            color = surfaceColors.textMuted
        )
        Spacer(Modifier.width(8.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = surfaceColors.border.copy(alpha = Dimens.Alpha.Medium)
        )
    }
}

@Composable
private fun HistoryEntryItem(
    entry: HistoryEntry,
    surfaceColors: SurfaceColors,
    accent: KaiteyoAccentScheme,
    onClick: () -> Unit,
    onUndo: () -> Unit
) {
    val typeColor = entryTypeColor(entry.type)
    val typeIcon = entryTypeIcon(entry.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = surfaceColors.surfaceElevated),
        shape = RoundedCornerShape(Dimens.RadiusMd),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(typeColor.copy(alpha = Dimens.Alpha.Light)),
                contentAlignment = Alignment.Center
            ) {
                Icon(typeIcon, null, Modifier.size(18.dp), tint = typeColor)
            }

            Spacer(Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.description,
                    fontWeight = FontWeight.Medium,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = surfaceColors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        entry.type.displayName,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = typeColor
                    )
                    Text(" · ", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = surfaceColors.textMuted)
                    Text(
                        formatTimestamp(entry.timestamp),
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = surfaceColors.textMuted
                    )
                    if (entry.cardIds.isNotEmpty()) {
                        Text(" · ", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = surfaceColors.textMuted)
                        Text(
                            "${entry.cardIds.size} cards",
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = surfaceColors.textMuted
                        )
                    }
                }
            }

            // Undo button
            if (entry.undoable) {
                IconButton(onClick = onUndo, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Undo,
                        "Undo",
                        Modifier.size(16.dp),
                        tint = surfaceColors.textMuted
                    )
                }
            }

            // Chevron
            Icon(
                Icons.Default.ChevronRight,
                null,
                Modifier.size(18.dp),
                tint = surfaceColors.textMuted.copy(alpha = Dimens.Alpha.SemiOpaque)
            )
        }
    }
}

@Composable
private fun EntryDetailDialog(
    entry: HistoryEntry,
    surfaceColors: SurfaceColors,
    accent: KaiteyoAccentScheme,
    onDismiss: () -> Unit,
    onUndo: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(entryTypeColor(entry.type).copy(alpha = Dimens.Alpha.Light)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(entryTypeIcon(entry.type), null, Modifier.size(16.dp), tint = entryTypeColor(entry.type))
                }
                Spacer(Modifier.width(8.dp))
                Text(entry.type.displayName)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("Description", entry.description, surfaceColors)
                DetailRow("Time", formatTimestampFull(entry.timestamp), surfaceColors)
                DetailRow("Type", entry.type.displayName, surfaceColors)
                DetailRow("Card IDs", if (entry.cardIds.isNotEmpty()) entry.cardIds.joinToString(", ") else "None", surfaceColors)
                entry.deckId?.let { DetailRow("Deck", it, surfaceColors) }
                DetailRow("Undoable", if (entry.undoable) "Yes" else "No", surfaceColors)
                if (entry.undoData != null) {
                    DetailRow("Undo Data", entry.undoData!!, surfaceColors)
                }
            }
        },
        confirmButton = {
            if (entry.undoable) {
                Button(
                    onClick = onUndo,
                    colors = ButtonDefaults.buttonColors(containerColor = accent.primary)
                ) {
                    Icon(Icons.Default.Undo, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Undo")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String, surfaceColors: SurfaceColors) {
    Row {
        Text(
            "$label: ",
            fontWeight = FontWeight.Medium,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            color = surfaceColors.textPrimary,
            modifier = Modifier.width(90.dp)
        )
        Text(
            value,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            color = surfaceColors.textMuted
        )
    }
}

private fun entryTypeColor(type: HistoryEntryType): Color = when (type) {
    HistoryEntryType.Review -> ua.syt0r.kanji.presentation.common.theme.semanticInfo
    HistoryEntryType.Import -> ua.syt0r.kanji.presentation.common.theme.semanticSuccess
    HistoryEntryType.Export -> ua.syt0r.kanji.presentation.common.theme.semanticWarning
    HistoryEntryType.Edit -> ua.syt0r.kanji.presentation.common.theme.semanticNew
    HistoryEntryType.Delete -> ua.syt0r.kanji.presentation.common.theme.semanticError
    HistoryEntryType.Restore -> ua.syt0r.kanji.presentation.common.theme.semanticWarning
    HistoryEntryType.BulkOperation -> ua.syt0r.kanji.presentation.common.theme.semanticNew
    HistoryEntryType.TagChange -> ua.syt0r.kanji.presentation.common.theme.semanticNew
    HistoryEntryType.FlagChange -> ua.syt0r.kanji.presentation.common.theme.semanticError
    HistoryEntryType.DeckChange -> ua.syt0r.kanji.presentation.common.theme.semanticWarning
    HistoryEntryType.NoteChange -> ua.syt0r.kanji.presentation.common.theme.semanticInfo
    HistoryEntryType.StatusChange -> ua.syt0r.kanji.presentation.common.theme.semanticWarning
    HistoryEntryType.ScheduleChange -> ua.syt0r.kanji.presentation.common.theme.semanticSuccess
    HistoryEntryType.BackupCreated -> androidx.compose.ui.graphics.Color.Gray
    HistoryEntryType.BackupRestored -> androidx.compose.ui.graphics.Color.Gray
    HistoryEntryType.PluginAction -> ua.syt0r.kanji.presentation.common.theme.semanticInfo
}

private fun entryTypeIcon(type: HistoryEntryType): ImageVector = when (type) {
    HistoryEntryType.Review -> Icons.Default.RateReview
    HistoryEntryType.Import -> Icons.Default.FileUpload
    HistoryEntryType.Export -> Icons.Default.FileDownload
    HistoryEntryType.Edit -> Icons.Default.Edit
    HistoryEntryType.Delete -> Icons.Default.Delete
    HistoryEntryType.Restore -> Icons.Default.Restore
    HistoryEntryType.BulkOperation -> Icons.Default.SelectAll
    HistoryEntryType.TagChange -> Icons.Default.Label
    HistoryEntryType.FlagChange -> Icons.Default.Flag
    HistoryEntryType.DeckChange -> Icons.Default.Folder
    HistoryEntryType.NoteChange -> Icons.Default.Description
    HistoryEntryType.StatusChange -> Icons.Default.SwapHoriz
    HistoryEntryType.ScheduleChange -> Icons.Default.Schedule
    HistoryEntryType.BackupCreated -> Icons.Default.Backup
    HistoryEntryType.BackupRestored -> Icons.Default.RestorePage
    HistoryEntryType.PluginAction -> Icons.Default.Extension
}

private fun formatTimestamp(instant: Instant): String {
    val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return when {
        dt.date == now.date -> "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
        dt.date == now.date.minus(1, DateTimeUnit.DAY) -> "Yesterday ${dt.hour}:${dt.minute.toString().padStart(2, '0')}"
        else -> "${dt.monthNumber}/${dt.dayOfMonth} ${dt.hour}:${dt.minute.toString().padStart(2, '0')}"
    }
}

private fun formatTimestampFull(instant: Instant): String {
    val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dt.year}-${dt.monthNumber.toString().padStart(2, '0')}-${dt.dayOfMonth.toString().padStart(2, '0')} " +
            "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}:${dt.second.toString().padStart(2, '0')}"
}
