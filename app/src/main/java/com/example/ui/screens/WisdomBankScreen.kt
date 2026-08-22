package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.JournalEntry
import com.example.data.local.entity.WisdomQuote
import com.example.ui.components.EntryDetailDialog
import com.example.ui.components.WisdomContemplationDialog
import com.example.ui.components.WisdomEditorDialog
import com.example.ui.theme.NaturalBlueContainer
import com.example.ui.theme.NaturalForestDark
import com.example.ui.theme.NaturalLightSurfaceVariant
import com.example.ui.theme.NaturalMineralLight
import com.example.ui.theme.NaturalMintContainer
import com.example.ui.theme.NaturalSageContainer
import com.example.ui.theme.NaturalSandContainer
import com.example.ui.viewmodel.JournalViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val WISDOM_CATEGORIES = listOf(
    "All", "Revelation", "Insight", "Epiphany", "Axiom", "Core Truth", "Mantra"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WisdomBankScreen(
    viewModel: JournalViewModel,
    onNavigateToJournal: () -> Unit = {}
) {
    val context = LocalContext.current
    val allWisdom by viewModel.allWisdom.collectAsState()
    val filteredWisdom by viewModel.filteredWisdom.collectAsState()
    val wisdomTags by viewModel.allWisdomTags.collectAsState()
    val searchQuery by viewModel.wisdomSearchQuery.collectAsState()
    val selectedTag by viewModel.selectedWisdomTag.collectAsState()
    val selectedCategory by viewModel.selectedWisdomCategory.collectAsState()
    val onlyFavorites by viewModel.onlyFavoriteWisdom.collectAsState()
    val celestialSnapshot by viewModel.celestialSnapshot.collectAsState()
    val allEntries by viewModel.allEntries.collectAsState()

    var showEditorDialog by remember { mutableStateOf(false) }
    var editingQuote by remember { mutableStateOf<WisdomQuote?>(null) }
    var contemplatingQuote by remember { mutableStateOf<WisdomQuote?>(null) }
    var viewingEntryDetail by remember { mutableStateOf<JournalEntry?>(null) }

    // Choose a featured "Pearl of Revelation" (first favorite, or first quote)
    val featuredQuote = remember(allWisdom) {
        allWisdom.firstOrNull { it.isFavorite } ?: allWisdom.firstOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NaturalMintContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Wisdom Bank",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${allWisdom.size} Insights Tagged • ${celestialSnapshot.moonPhase.glyph} ${celestialSnapshot.moonSign.signName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NaturalSageContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "${celestialSnapshot.tattwaInfo.currentTattwa.tattwaName} Tattwa",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = NaturalForestDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingQuote = null
                    showEditorDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_wisdom_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Wisdom")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Add Pearl", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("wisdom_bank_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search & Filter Box
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setWisdomSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wisdom_search_bar"),
                        placeholder = {
                            Text("Search insights, tags, keywords, celestial signs...")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setWisdomSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filters Row (Favorites toggle + Categories)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Favorites only chip
                        FilterChip(
                            selected = onlyFavorites,
                            onClick = { viewModel.toggleOnlyFavoriteWisdom() },
                            label = { Text("⭐ Starred") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NaturalSandContainer,
                                selectedLabelColor = NaturalForestDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Category chips
                        WISDOM_CATEGORIES.forEach { category ->
                            val isSelected = if (category == "All") selectedCategory == null else selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setSelectedWisdomCategory(if (category == "All") null else category)
                                },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NaturalMintContainer,
                                    selectedLabelColor = NaturalForestDark
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Dynamic Tag Chips Row
                    if (wisdomTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tags:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (selectedTag != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.setSelectedWisdomTag(null) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("All Tags ✕", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            wisdomTags.forEach { (tag, count) ->
                                val isSelected = selectedTag.equals(tag, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else NaturalLightSurfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            if (isSelected) viewModel.setSelectedWisdomTag(null)
                                            else viewModel.setSelectedWisdomTag(tag)
                                        }
                                ) {
                                    Text(
                                        text = "#$tag ($count)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Featured Hero Card (When no active filters, or as a spotlight)
            if (searchQuery.isBlank() && selectedTag == null && selectedCategory == null && !onlyFavorites && featuredQuote != null) {
                item {
                    FeaturedWisdomHero(
                        quote = featuredQuote,
                        onContemplate = { contemplatingQuote = featuredQuote },
                        onToggleFavorite = { viewModel.toggleWisdomFavorite(featuredQuote) }
                    )
                }
            }

            // Empty state if no filtered results
            if (filteredWisdom.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (allWisdom.isEmpty()) "Your Wisdom Bank is waiting" else "No matching wisdom insights found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (allWisdom.isEmpty())
                                    "Open any journal entry and tap '✨ Tag as Wisdom' to distill revelations and organize them with custom tags."
                                else "Try clearing your search query or selecting a different tag.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (allWisdom.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        viewModel.setWisdomSearchQuery("")
                                        viewModel.setSelectedWisdomTag(null)
                                        viewModel.setSelectedWisdomCategory(null)
                                        if (onlyFavorites) viewModel.toggleOnlyFavoriteWisdom()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Clear All Filters")
                                }
                            } else {
                                Button(
                                    onClick = onNavigateToJournal,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Explore Journal Entries")
                                }
                            }
                        }
                    }
                }
            } else {
                // Wisdom Quotes Items
                items(filteredWisdom, key = { it.id }) { quote ->
                    WisdomCard(
                        quote = quote,
                        onContemplate = { contemplatingQuote = quote },
                        onEdit = {
                            editingQuote = quote
                            showEditorDialog = true
                        },
                        onDelete = { viewModel.deleteWisdomQuote(quote) },
                        onToggleFavorite = { viewModel.toggleWisdomFavorite(quote) },
                        onTagClick = { tag ->
                            viewModel.setSelectedWisdomTag(tag)
                        },
                        onOpenSource = { entryId ->
                            val found = allEntries.firstOrNull { it.id == entryId }
                            if (found != null) {
                                viewingEntryDetail = found
                            } else {
                                Toast.makeText(context, "Journal entry not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            // Bottom spacer for FAB clearance
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }

    // Dialogs
    if (showEditorDialog) {
        WisdomEditorDialog(
            initialQuote = editingQuote,
            currentSnapshot = celestialSnapshot,
            onDismiss = {
                showEditorDialog = false
                editingQuote = null
            },
            onSave = { quote ->
                viewModel.saveWisdomQuote(quote)
                showEditorDialog = false
                editingQuote = null
                Toast.makeText(context, "Wisdom insight saved ✨", Toast.LENGTH_SHORT).show()
            }
        )
    }

    contemplatingQuote?.let { quote ->
        WisdomContemplationDialog(
            quote = quote,
            onDismiss = { contemplatingQuote = null },
            onEdit = {
                editingQuote = quote
                contemplatingQuote = null
                showEditorDialog = true
            },
            onDelete = {
                viewModel.deleteWisdomQuote(quote)
                contemplatingQuote = null
            },
            onToggleFavorite = {
                viewModel.toggleWisdomFavorite(quote)
            },
            onOpenSourceEntry = { entryId ->
                contemplatingQuote = null
                val found = allEntries.firstOrNull { it.id == entryId }
                if (found != null) {
                    viewingEntryDetail = found
                }
            }
        )
    }

    viewingEntryDetail?.let { entry ->
        EntryDetailDialog(
            entry = entry,
            onDismiss = { viewingEntryDetail = null },
            onEdit = { viewingEntryDetail = null },
            onDelete = {
                viewModel.deleteEntry(entry)
                viewingEntryDetail = null
            },
            onToggleFavorite = { viewModel.toggleFavorite(entry) },
            onTagWisdom = { text ->
                viewModel.extractWisdomFromEntry(entry, text, entry.tags)
                Toast.makeText(context, "Saved to Wisdom Bank ✨", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeaturedWisdomHero(
    quote: WisdomQuote,
    onContemplate: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = NaturalMintContainer.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(NaturalSageContainer, NaturalBlueContainer)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable { onContemplate() }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CONTEMPLATIVE SPARK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (quote.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Favorite",
                        tint = if (quote.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "“${quote.quoteText}”",
                style = MaterialTheme.typography.titleMedium,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                color = NaturalForestDark,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "— ${quote.sourceTitle}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NaturalSageContainer
                ) {
                    Text(
                        text = "${quote.moonPhaseGlyph} ${quote.moonSignName} • ${quote.planetaryHourPlanet}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = NaturalForestDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WisdomCard(
    quote: WisdomQuote,
    onContemplate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTagClick: (String) -> Unit,
    onOpenSource: (Long) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val dateString = dateFormat.format(Date(quote.timestamp))

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable { onContemplate() }
            .testTag("wisdom_quote_card_${quote.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Category + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NaturalMineralLight.copy(alpha = 0.35f)
                ) {
                    Text(
                        text = quote.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NaturalForestDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Wisdom Pearl", "“${quote.quoteText}”\n— ${quote.sourceTitle}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied wisdom", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = if (quote.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Favorite",
                            tint = if (quote.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Quote Text
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "“",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = quote.quoteText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Custom Tags Flow
            if (quote.customTags.isNotBlank()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    quote.customTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NaturalSageContainer.copy(alpha = 0.7f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onTagClick(tag) }
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = NaturalForestDark,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Bottom Source & Alignment Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Source
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (quote.journalEntryId != null) {
                                Modifier.clickable { onOpenSource(quote.journalEntryId) }
                            } else Modifier
                        )
                ) {
                    Text(
                        text = "📖 ${quote.sourceTitle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Celestial Glyph Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${quote.moonPhaseGlyph} ${quote.moonSignName} • ${quote.tattwaName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
