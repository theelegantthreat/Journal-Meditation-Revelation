package com.example.ui.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.JournalEntry
import com.example.ui.components.EntryDetailDialog
import com.example.ui.components.EntryEditorDialog
import com.example.ui.components.LocationSelectorDialog
import com.example.ui.components.TagWisdomDialog
import com.example.ui.viewmodel.JournalViewModel
import java.util.Calendar

enum class AppNavigationTab(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    JOURNAL("Journal", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    WISDOM("Wisdom", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    CALENDAR("Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    MEDITATION("Meditate", Icons.Filled.Spa, Icons.Outlined.Spa),
    GUIDE("Guide", Icons.Filled.SelfImprovement, Icons.Outlined.SelfImprovement)
}

@Composable
fun MainContainerScreen(
    viewModel: JournalViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(AppNavigationTab.HOME) }

    // Dialog states
    var isEditorOpen by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<JournalEntry?>(null) }
    var entryToView by remember { mutableStateOf<JournalEntry?>(null) }
    var isLocationSelectorOpen by remember { mutableStateOf(false) }
    var taggingWisdomEntry by remember { mutableStateOf<Pair<JournalEntry, String>?>(null) }

    // Observe ViewModel flows
    val celestialSnapshot by viewModel.celestialSnapshot.collectAsStateWithLifecycle()
    val allEntries by viewModel.allEntries.collectAsStateWithLifecycle()
    val filteredEntries by viewModel.filteredEntries.collectAsStateWithLifecycle()
    val selectedDateEntries by viewModel.selectedDateEntries.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val calendarViewType by viewModel.calendarViewType.collectAsStateWithLifecycle()
    val selectedScopeTab by viewModel.selectedScopeTab.collectAsStateWithLifecycle()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val timerRemainingSeconds by viewModel.timerRemainingSeconds.collectAsStateWithLifecycle()
    val timerTotalSeconds by viewModel.timerTotalSeconds.collectAsStateWithLifecycle()
    val timerFinished by viewModel.timerFinished.collectAsStateWithLifecycle()

    val currentCity by viewModel.city.collectAsStateWithLifecycle()
    val currentLat by viewModel.latitude.collectAsStateWithLifecycle()
    val currentLon by viewModel.longitude.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar")
            ) {
                AppNavigationTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        when (currentTab) {
            AppNavigationTab.HOME -> {
                HomeScreen(
                    snapshot = celestialSnapshot,
                    recentEntries = allEntries,
                    onOpenNewEntry = {
                        entryToEdit = null
                        isEditorOpen = true
                    },
                    onOpenTimer = { currentTab = AppNavigationTab.MEDITATION },
                    onOpenGuide = { currentTab = AppNavigationTab.GUIDE },
                    onOpenWisdom = { currentTab = AppNavigationTab.WISDOM },
                    onOpenLocationDialog = { isLocationSelectorOpen = true },
                    onSelectEntry = { entryToView = it },
                    onEditEntry = {
                        entryToEdit = it
                        isEditorOpen = true
                    },
                    onDeleteEntry = { viewModel.deleteEntry(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onNavigateToJournal = { currentTab = AppNavigationTab.JOURNAL },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppNavigationTab.JOURNAL -> {
                JournalListScreen(
                    entries = filteredEntries,
                    selectedScopeTab = selectedScopeTab,
                    selectedTypeFilter = selectedTypeFilter,
                    searchQuery = searchQuery,
                    onScopeTabChange = { viewModel.setScopeTab(it) },
                    onTypeFilterChange = { viewModel.setTypeFilter(it) },
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onNewEntry = {
                        entryToEdit = null
                        isEditorOpen = true
                    },
                    onSelectEntry = { entryToView = it },
                    onEditEntry = {
                        entryToEdit = it
                        isEditorOpen = true
                    },
                    onDeleteEntry = { viewModel.deleteEntry(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppNavigationTab.WISDOM -> {
                WisdomBankScreen(
                    viewModel = viewModel,
                    onNavigateToJournal = { currentTab = AppNavigationTab.JOURNAL }
                )
            }
            AppNavigationTab.CALENDAR -> {
                CalendarScreen(
                    selectedDate = selectedDate,
                    calendarViewType = calendarViewType,
                    allEntries = allEntries,
                    selectedDateEntries = selectedDateEntries,
                    currentSnapshot = celestialSnapshot,
                    onViewTypeChange = { viewModel.setCalendarViewType(it) },
                    onSelectDate = { viewModel.setSelectedDate(it) },
                    onNavigateMonth = { viewModel.navigateMonth(it) },
                    onNavigateWeek = { viewModel.navigateWeek(it) },
                    onNavigateDay = { viewModel.navigateDay(it) },
                    onResetToday = { viewModel.resetToToday() },
                    onNewEntryForDate = { dateCal ->
                        val dateSnapshot = viewModel.getSnapshotForTimestamp(dateCal.timeInMillis)
                        entryToEdit = JournalEntry(
                            title = "",
                            content = "",
                            entryType = "REVELATION",
                            timeframeScope = "DAILY",
                            timestamp = dateCal.timeInMillis,
                            city = dateSnapshot.city,
                            latitude = dateSnapshot.latitude,
                            longitude = dateSnapshot.longitude,
                            moonPhaseName = dateSnapshot.moonPhase.title,
                            moonPhaseGlyph = dateSnapshot.moonPhase.glyph,
                            moonIllumination = dateSnapshot.moonIllumination,
                            moonSignName = dateSnapshot.moonSign.signName,
                            moonSignDegree = dateSnapshot.moonSignDegree,
                            sunSignName = dateSnapshot.sunSign.signName,
                            sunSignDegree = dateSnapshot.sunSignDegree,
                            planetaryHourPlanet = dateSnapshot.planetaryHour.rulingPlanet.planetName,
                            planetaryHourNumber = dateSnapshot.planetaryHour.hourNumber,
                            isDayHour = dateSnapshot.planetaryHour.isDayHour,
                            tattwaName = dateSnapshot.tattwaInfo.currentTattwa.tattwaName,
                            subTattwaName = dateSnapshot.tattwaInfo.subTattwa.tattwaName
                        )
                        isEditorOpen = true
                    },
                    onSelectEntry = { entryToView = it },
                    onEditEntry = {
                        entryToEdit = it
                        isEditorOpen = true
                    },
                    onDeleteEntry = { viewModel.deleteEntry(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppNavigationTab.MEDITATION -> {
                MeditationTimerScreen(
                    isTimerRunning = isTimerRunning,
                    remainingSeconds = timerRemainingSeconds,
                    totalDurationSeconds = timerTotalSeconds,
                    timerFinished = timerFinished,
                    currentSnapshot = celestialSnapshot,
                    onStartTimer = { viewModel.startTimer() },
                    onPauseTimer = { viewModel.pauseTimer() },
                    onResetTimer = { viewModel.resetTimer() },
                    onSetDuration = { viewModel.setTimerDuration(it) },
                    onRecordRevelation = { durationMins ->
                        entryToEdit = JournalEntry(
                            title = "Revelation After Meditation",
                            content = "",
                            entryType = "REVELATION",
                            timeframeScope = "DAILY",
                            timestamp = System.currentTimeMillis(),
                            durationMinutes = durationMins,
                            depthRating = 5,
                            city = celestialSnapshot.city,
                            latitude = celestialSnapshot.latitude,
                            longitude = celestialSnapshot.longitude,
                            moonPhaseName = celestialSnapshot.moonPhase.title,
                            moonPhaseGlyph = celestialSnapshot.moonPhase.glyph,
                            moonIllumination = celestialSnapshot.moonIllumination,
                            moonSignName = celestialSnapshot.moonSign.signName,
                            moonSignDegree = celestialSnapshot.moonSignDegree,
                            sunSignName = celestialSnapshot.sunSign.signName,
                            sunSignDegree = celestialSnapshot.sunSignDegree,
                            planetaryHourPlanet = celestialSnapshot.planetaryHour.rulingPlanet.planetName,
                            planetaryHourNumber = celestialSnapshot.planetaryHour.hourNumber,
                            isDayHour = celestialSnapshot.planetaryHour.isDayHour,
                            tattwaName = celestialSnapshot.tattwaInfo.currentTattwa.tattwaName,
                            subTattwaName = celestialSnapshot.tattwaInfo.subTattwa.tattwaName
                        )
                        viewModel.dismissTimerFinished()
                        isEditorOpen = true
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppNavigationTab.GUIDE -> {
                CorrespondencesGuideScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }

    // Modal Dialogs
    if (isEditorOpen) {
        EntryEditorDialog(
            initialEntry = entryToEdit,
            currentSnapshot = celestialSnapshot,
            onDismiss = {
                isEditorOpen = false
                entryToEdit = null
            },
            onSave = { entry ->
                viewModel.saveEntry(entry)
                isEditorOpen = false
                entryToEdit = null
            }
        )
    }

    if (entryToView != null) {
        EntryDetailDialog(
            entry = entryToView!!,
            onDismiss = { entryToView = null },
            onEdit = {
                entryToEdit = entryToView
                entryToView = null
                isEditorOpen = true
            },
            onDelete = {
                val entry = entryToView
                if (entry != null) {
                    viewModel.deleteEntry(entry)
                }
                entryToView = null
            },
            onToggleFavorite = {
                val entry = entryToView
                if (entry != null) {
                    viewModel.toggleFavorite(entry)
                    entryToView = entry.copy(isFavorite = !entry.isFavorite)
                }
            },
            onTagWisdom = { text ->
                val entry = entryToView
                if (entry != null) {
                    taggingWisdomEntry = Pair(entry, text)
                }
            }
        )
    }

    taggingWisdomEntry?.let { (entry, quote) ->
        TagWisdomDialog(
            entry = entry,
            initialQuote = quote,
            onDismiss = { taggingWisdomEntry = null },
            onSaveWisdom = { quoteText, tags, category ->
                viewModel.extractWisdomFromEntry(
                    entry = entry,
                    selectedQuoteText = quoteText,
                    customTags = tags,
                    category = category
                )
                taggingWisdomEntry = null
            }
        )
    }

    if (isLocationSelectorOpen) {
        LocationSelectorDialog(
            currentCity = currentCity,
            currentLat = currentLat,
            currentLon = currentLon,
            onDismiss = { isLocationSelectorOpen = false },
            onSelectPreset = { preset ->
                viewModel.selectLocationPreset(preset)
            },
            onCustomLocation = { city, lat, lon ->
                viewModel.setLocation(city, lat, lon)
            },
            onFetchGps = { onResult ->
                viewModel.fetchDeviceLocation(onResult)
            }
        )
    }
}
