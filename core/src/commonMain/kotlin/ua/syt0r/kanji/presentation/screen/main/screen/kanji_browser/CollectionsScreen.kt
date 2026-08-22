@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ua.syt0r.kanji.presentation.screen.main.screen.kanji_browser

import ua.syt0r.kanji.presentation.common.theme.Dimens
import ua.syt0r.kanji.presentation.common.kaiteyoHeading
import ua.syt0r.kanji.presentation.common.resources.string.resolveString

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import ua.syt0r.kanji.presentation.common.theme.LocalKaiteyoAccent
import ua.syt0r.kanji.presentation.common.theme.LocalSurfaceColors
import ua.syt0r.kanji.presentation.common.theme.KaiteyoAccentScheme
import ua.syt0r.kanji.presentation.common.theme.SurfaceColors
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.features.KaiteyoDataCenter
import ua.syt0r.kanji.presentation.screen.main.features.KaiteyoCollection
import ua.syt0r.kanji.presentation.screen.main.screen.decks.CardFlagType
import ua.syt0r.kanji.presentation.screen.main.screen.decks.CardTag
import kotlin.time.Duration.Companion.hours

// ============================================
// COLLECTIONS
// Smart collections · tag collections ·
// flag collections · favorites
// ============================================

@Composable
fun CollectionsScreen(
    navigationState: MainNavigationState,
    dataCenter: KaiteyoDataCenter
) {
    var selectedCollection by remember { mutableStateOf<KaiteyoCollection?>(null) }

    if (selectedCollection == null) {
        CollectionsOverview(
            navigationState = navigationState,
            dataCenter = dataCenter,
            onOpenCollection = { selectedCollection = it }
        )
    } else {
        CollectionDetail(
            collection = selectedCollection!!,
            dataCenter = dataCenter,
            onBack = { selectedCollection = null },
            onOpenBrowser = { criteria ->
                navigationState.navigate(
                    ua.syt0r.kanji.presentation.screen.main.MainDestination.KanjiBrowser(criteria)
                )
            }
        )
    }
}

@Composable
private fun CollectionsOverview(
    navigationState: MainNavigationState,
    dataCenter: KaiteyoDataCenter,
    onOpenCollection: (KaiteyoCollection) -> Unit
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val strings = resolveString { collections }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = { navigationState.navigateBack() }) {
                Icon(Icons.Default.ArrowBack, strings.backDescription, tint = surfaceColors.textSecondary)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = strings.title,
                    color = surfaceColors.textPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.availableCards(dataCenter.cards.size),
                    color = surfaceColors.textMuted,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "smart") {
                SectionTitle(strings.smartSectionTitle, accent, surfaceColors)
            }
            items(dataCenter.collections.filter { it.isSmart }, key = { it.id }) { collection ->
                CollectionCard(
                    collection = collection,
                    count = collection.cardIds.size,
                    onClick = { onOpenCollection(collection) },
                    accent = accent,
                    surfaceColors = surfaceColors,
                    title = strings.smartName(collection.id),
                )
            }

            item(key = "tags") {
                SectionTitle(strings.tagSectionTitle, accent, surfaceColors)
            }
            if (dataCenter.tags.isEmpty()) {
                item(key = "no-tags") {
                    Text(
                        text = strings.noTagsMessage,
                        color = surfaceColors.textMuted,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
            dataCenter.tags.forEach { tag ->
                item(key = "tag-${tag.id}") {
                    val ids = dataCenter.cardTags.entries
                        .filter { (_, tagIds) -> tag.id in tagIds }
                        .map { (cardId, _) -> cardId }
                        .toSet()
                    TagCollectionCard(
                        tag = tag,
                        count = ids.size,
                        onClick = {
                            onOpenCollection(
                                KaiteyoCollection(
                                    id = "tag-${tag.id}",
                                    name = tag.name,
                                    icon = "🏷",
                                    isSmart = false,
                                    criteria = "",
                                    cardIds = ids
                                )
                            )
                        },
                        accent = accent,
                        surfaceColors = surfaceColors
                    )
                }
            }

            item(key = "flags") {
                SectionTitle(strings.flagLabel, accent, surfaceColors)
            }
            CardFlagType.entries.filter { it != CardFlagType.None }.forEach { flag ->
                item(key = "flag-${flag.id}") {
                    val ids = dataCenter.cards
                        .filter { dataCenter.cardFlagsFor(it.id) == flag }
                        .map { it.id }
                        .toSet()
                    FlagCollectionCard(
                        flag = flag,
                        count = ids.size,
                        onClick = {
                            onOpenCollection(
                                KaiteyoCollection(
                                    id = "flag-${flag.id}",
                                    name = flag.displayName,
                                    icon = "🚩",
                                    isSmart = false,
                                    criteria = "",
                                    cardIds = ids
                                )
                            )
                        },
                        accent = accent,
                        surfaceColors = surfaceColors
                    )
                }
            }

            item(key = "custom") {
                SectionTitle(strings.customSectionTitle, accent, surfaceColors)
            }
            val customCollections = dataCenter.collections.filter { !it.isSmart && !it.id.startsWith("tag-") && !it.id.startsWith("flag-") }
            if (customCollections.isEmpty()) {
                item(key = "no-custom") {
                    Text(strings.noCustomMessage, color = surfaceColors.textMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
            items(customCollections, key = { it.id }) { collection ->
                CollectionCard(
                    collection = collection,
                    count = collection.cardIds.size,
                    onClick = { onOpenCollection(collection) },
                    accent = accent,
                    surfaceColors = surfaceColors,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, accent: KaiteyoAccentScheme, surfaceColors: SurfaceColors) {
    Text(
        text = title,
        color = accent.primary,
        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 8.dp).kaiteyoHeading()
    )
}

@Composable
private fun CollectionCard(
    collection: KaiteyoCollection,
    count: Int,
    onClick: () -> Unit,
    accent: KaiteyoAccentScheme,
    surfaceColors: SurfaceColors,
    title: String = collection.name,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = surfaceColors.surface,
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(Dimens.RadiusMd))
                .background(accent.primary.copy(alpha = Dimens.Alpha.Light)),
            contentAlignment = Alignment.Center
        ) {
            Text(collection.icon, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = surfaceColors.textPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (collection.isSmart) resolveString { collections }.autoGeneratedLabel else resolveString { collections }.customLabel,
                color = surfaceColors.textMuted,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = "$count",
            color = accent.primary,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
            }
    }
}
@Composable
private fun TagCollectionCard(
    tag: CardTag,
    count: Int,
    onClick: () -> Unit,
    accent: KaiteyoAccentScheme,
    surfaceColors: SurfaceColors
) {
    val color = tag.getDisplayColor()
    val strings = resolveString { collections }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = surfaceColors.surface,
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(Dimens.RadiusMd))
                .background(color.copy(alpha = Dimens.Alpha.Light)),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(14.dp).clip(CircleShape).background(color))
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = tag.name,
                color = surfaceColors.textPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = strings.tagLabel,
                color = surfaceColors.textMuted,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = "$count",
            color = accent.primary,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        }
    }
}

@Composable
private fun FlagCollectionCard(
    flag: CardFlagType,
    count: Int,
    onClick: () -> Unit,
    accent: KaiteyoAccentScheme,
    surfaceColors: SurfaceColors
) {
    val color = flag.colorFromHex()
    val strings = resolveString { collections }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = surfaceColors.surface,
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(Dimens.RadiusMd))
                .background(color.copy(alpha = Dimens.Alpha.Light)),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(14.dp).clip(CircleShape).background(color))
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = flag.displayName,
                color = surfaceColors.textPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = strings.flagLabel,
                color = surfaceColors.textMuted,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = "$count",
            color = accent.primary,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        }
    }
}

@Composable
private fun CollectionDetail(
    collection: KaiteyoCollection,
    dataCenter: KaiteyoDataCenter,
    onBack: () -> Unit,
    onOpenBrowser: (KanjiBrowserCriteria) -> Unit
) {
    val surfaceColors = LocalSurfaceColors.current
    val accent = LocalKaiteyoAccent.current
    val strings = resolveString { collections }
    val displayName = if (collection.isSmart) strings.smartName(collection.id) else collection.name
    val now = Clock.System.now()

    val cards = remember(collection, dataCenter.cards) {
        when {
            collection.id == "smart-recently-learned" ->
                dataCenter.cards.filter { card ->
                    card.lastReviewed.isNotBlank() &&
                        runCatching { kotlinx.datetime.Instant.parse(card.lastReviewed) > now - 24.hours }
                            .getOrDefault(false)
                }
            collection.id == "smart-needs-review" ->
                dataCenter.cards.filter { card ->
                    val srs = dataCenter.srsCards[card.id] ?: return@filter false
                    val last = srs.lastReview ?: return@filter false
                    last + srs.interval <= now
                }
            collection.id == "smart-frequently-failed" ->
                dataCenter.cards.filter { (dataCenter.srsCards[it.id]?.lapses ?: 0) >= 3 }
            collection.id == "smart-not-studied-30-days" ->
                dataCenter.cards.filter { dataCenter.notReviewedFor(it.id, 30) }
            collection.id == "smart-flagged" ->
                dataCenter.cards.filter { dataCenter.cardFlagsFor(it.id) != CardFlagType.None }
            collection.id == "smart-favorites" ->
                dataCenter.cards.filter { dataCenter.isFavorite(it.id) }
            else -> dataCenter.cards.filter { it.id in collection.cardIds }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, strings.backDescription, tint = surfaceColors.textSecondary)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = "${collection.icon} $displayName",
                    color = surfaceColors.textPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.cardCount(cards.size),
                    color = surfaceColors.textMuted,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }
            TextButton(
                onClick = {
                    onOpenBrowser(
                        KanjiBrowserCriteria(
                            favoritesOnly = collection.id == "smart-favorites",
                            showFlagged = collection.id == "smart-flagged",
                            minLapses = if (collection.id == "smart-frequently-failed") 3 else null,
                            notReviewedDaysAgo = if (collection.id == "smart-not-studied-30-days") 30 else null,
                            sortBy = if (collection.id == "smart-recently-learned" || collection.id == "smart-needs-review")
                                KanjiBrowserSort.LastReviewed else KanjiBrowserSort.Frequency
                        )
                    )
                }
            ) {
                Text(strings.openInBrowser, color = accent.primary, style = MaterialTheme.typography.bodySmall)
            }
        }

        if (cards.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(strings.emptyMessage, color = surfaceColors.textMuted)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(88.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cards, key = { it.id }) { card ->
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(surfaceColors.surface)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = card.character,
                                style = MaterialTheme.typography.headlineMedium,
                                color = surfaceColors.textPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                card.flag.takeIf { it != CardFlagType.None }?.let { flag ->
                                    Box(Modifier.size(6.dp).clip(CircleShape).background(flag.colorFromHex()))
                                }
                                if (card.isFavorite) {
                                    Text("★", color = accent.secondary, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                                }
                                dataCenter.classifications[card.id].orEmpty()
                                    .firstOrNull { it.startsWith("n") }
                                    ?.let { Text(it.uppercase(), color = surfaceColors.textMuted, style = MaterialTheme.typography.labelSmall) }
                            }
                        }
                    }
                }
            }
        }
    }
}
