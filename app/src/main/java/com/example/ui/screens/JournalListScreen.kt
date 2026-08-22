package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.JournalEntry
import com.example.ui.components.JournalEntryCard
import com.example.ui.viewmodel.ScopeTab

@Composable
fun JournalListScreen(
    entries: List<JournalEntry>,
    selectedScopeTab: ScopeTab,
    selectedTypeFilter: String?,
    searchQuery: String,
    onScopeTabChange: (ScopeTab) -> Unit,
    onTypeFilterChange: (String?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onNewEntry: () -> Unit,
    onSelectEntry: (JournalEntry) -> Unit,
    onEditEntry: (JournalEntry) -> Unit,
    onDeleteEntry: (JournalEntry) -> Unit,
    onToggleFavorite: (JournalEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewEntry,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("journal_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Journal Entry")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scope Tabs (Daily, Weekly, Monthly, All)
            TabRow(
                selectedTabIndex = selectedScopeTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedScopeTab == ScopeTab.ALL,
                    onClick = { onScopeTabChange(ScopeTab.ALL) },
                    text = { Text("All", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedScopeTab == ScopeTab.DAILY,
                    onClick = { onScopeTabChange(ScopeTab.DAILY) },
                    text = { Text("Daily", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedScopeTab == ScopeTab.WEEKLY,
                    onClick = { onScopeTabChange(ScopeTab.WEEKLY) },
                    text = { Text("Weekly", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedScopeTab == ScopeTab.MONTHLY,
                    onClick = { onScopeTabChange(ScopeTab.MONTHLY) },
                    text = { Text("Monthly", fontWeight = FontWeight.SemiBold) }
                )
            }

            // Search Bar & Filter Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search title, revelation, moon sign, tattwa...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("journal_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedTypeFilter == null,
                            onClick = { onTypeFilterChange(null) },
                            label = { Text("All Types", style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                    val types = listOf(
                        "REVELATION" to "Revelation ✧",
                        "INSPIRATION" to "Inspiration ✦",
                        "INSIGHT" to "Insight ⚛",
                        "MEDITATION_SESSION" to "Meditation 🧘",
                        "VISION_SYMBOL" to "Vision 👁"
                    )
                    items(types) { (typeKey, label) ->
                        FilterChip(
                            selected = selectedTypeFilter == typeKey,
                            onClick = {
                                onTypeFilterChange(if (selectedTypeFilter == typeKey) null else typeKey)
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }

            // Results count banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${entries.size} ${if (entries.size == 1) "entry" else "entries"} found",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                val scopeLabel = when (selectedScopeTab) {
                    ScopeTab.ALL -> "All Timeframes"
                    ScopeTab.DAILY -> "Daily Entries"
                    ScopeTab.WEEKLY -> "Weekly Syntheses"
                    ScopeTab.MONTHLY -> "Monthly Syntheses"
                }
                Text(
                    text = scopeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Entries List
            if (entries.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No journal entries found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try clearing search filters or create a new contemplation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        JournalEntryCard(
                            entry = entry,
                            onClick = { onSelectEntry(entry) },
                            onEdit = { onEditEntry(entry) },
                            onDelete = { onDeleteEntry(entry) },
                            onToggleFavorite = { onToggleFavorite(entry) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}
