package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.astro.AstroCalculations
import com.example.astro.CelestialSnapshot
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.JournalEntry
import com.example.data.local.entity.WisdomQuote
import com.example.data.repository.JournalRepository
import com.example.location.LocationHelper
import com.example.location.LocationPreset
import com.example.location.LocationPresets
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class CalendarViewType {
    MONTHLY, WEEKLY, DAILY
}

enum class ScopeTab {
    ALL, DAILY, WEEKLY, MONTHLY
}

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JournalRepository
    private val locationHelper: LocationHelper

    // Location state
    private val _city = MutableStateFlow("Mount Shasta, CA")
    val city: StateFlow<String> = _city.asStateFlow()

    private val _latitude = MutableStateFlow(41.3099)
    val latitude: StateFlow<Double> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow(-122.3106)
    val longitude: StateFlow<Double> = _longitude.asStateFlow()

    // Real-time Celestial Snapshot
    private val _celestialSnapshot = MutableStateFlow(
        AstroCalculations.calculateSnapshot(
            city = _city.value,
            latitude = _latitude.value,
            longitude = _longitude.value
        )
    )
    val celestialSnapshot: StateFlow<CelestialSnapshot> = _celestialSnapshot.asStateFlow()

    // Calendar state
    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()

    private val _calendarViewType = MutableStateFlow(CalendarViewType.MONTHLY)
    val calendarViewType: StateFlow<CalendarViewType> = _calendarViewType.asStateFlow()

    // Journal filter states
    private val _selectedScopeTab = MutableStateFlow(ScopeTab.ALL)
    val selectedScopeTab: StateFlow<ScopeTab> = _selectedScopeTab.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<String?>(null) // null = all types
    val selectedTypeFilter: StateFlow<String?> = _selectedTypeFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Wisdom Bank filter & search states
    private val _wisdomSearchQuery = MutableStateFlow("")
    val wisdomSearchQuery: StateFlow<String> = _wisdomSearchQuery.asStateFlow()

    private val _selectedWisdomTag = MutableStateFlow<String?>(null) // null = all tags
    val selectedWisdomTag: StateFlow<String?> = _selectedWisdomTag.asStateFlow()

    private val _selectedWisdomCategory = MutableStateFlow<String?>(null) // null = all categories
    val selectedWisdomCategory: StateFlow<String?> = _selectedWisdomCategory.asStateFlow()

    private val _onlyFavoriteWisdom = MutableStateFlow(false)
    val onlyFavoriteWisdom: StateFlow<Boolean> = _onlyFavoriteWisdom.asStateFlow()

    // Meditation Timer State
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerRemainingSeconds = MutableStateFlow(15 * 60)
    val timerRemainingSeconds: StateFlow<Int> = _timerRemainingSeconds.asStateFlow()

    private val _timerTotalSeconds = MutableStateFlow(15 * 60)
    val timerTotalSeconds: StateFlow<Int> = _timerTotalSeconds.asStateFlow()

    private val _timerFinished = MutableStateFlow(false)
    val timerFinished: StateFlow<Boolean> = _timerFinished.asStateFlow()

    private var timerJob: Job? = null
    private var tickerJob: Job? = null

    // Room entries
    val allEntries: StateFlow<List<JournalEntry>>

    // Filtered entries for Journal list screen
    val filteredEntries: StateFlow<List<JournalEntry>>

    // Entries for currently selected calendar date
    val selectedDateEntries: StateFlow<List<JournalEntry>>

    // Wisdom Bank state flows
    val allWisdom: StateFlow<List<WisdomQuote>>
    val filteredWisdom: StateFlow<List<WisdomQuote>>
    val allWisdomTags: StateFlow<List<Pair<String, Int>>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = JournalRepository(db.journalDao(), db.wisdomDao())
        locationHelper = LocationHelper(application)

        allEntries = repository.allEntries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allWisdom = repository.allWisdom.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        filteredEntries = combine(
            allEntries,
            _selectedScopeTab,
            _selectedTypeFilter,
            _searchQuery
        ) { entries, scopeTab, typeFilter, query ->
            entries.filter { entry ->
                val matchesScope = when (scopeTab) {
                    ScopeTab.ALL -> true
                    ScopeTab.DAILY -> entry.timeframeScope == "DAILY"
                    ScopeTab.WEEKLY -> entry.timeframeScope == "WEEKLY"
                    ScopeTab.MONTHLY -> entry.timeframeScope == "MONTHLY"
                }
                val matchesType = typeFilter == null || entry.entryType == typeFilter
                val matchesQuery = query.isBlank() ||
                        entry.title.contains(query, ignoreCase = true) ||
                        entry.content.contains(query, ignoreCase = true) ||
                        entry.tags.contains(query, ignoreCase = true) ||
                        entry.moonSignName.contains(query, ignoreCase = true) ||
                        entry.tattwaName.contains(query, ignoreCase = true) ||
                        entry.planetaryHourPlanet.contains(query, ignoreCase = true)

                matchesScope && matchesType && matchesQuery
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        selectedDateEntries = combine(allEntries, _selectedDate) { entries, cal ->
            val startOfDay = (cal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = (cal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            entries.filter { it.timestamp in startOfDay..endOfDay }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Calculate dynamic list of all custom tags from wisdom quotes with counts
        allWisdomTags = allWisdom.map { quotes ->
            val tagMap = mutableMapOf<String, Int>()
            quotes.forEach { quote ->
                if (quote.customTags.isNotBlank()) {
                    quote.customTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
                        tagMap[tag] = (tagMap[tag] ?: 0) + 1
                    }
                }
            }
            tagMap.entries.sortedByDescending { it.value }.map { it.key to it.value }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Filtered Wisdom Bank quotes
        filteredWisdom = combine(
            allWisdom,
            _wisdomSearchQuery,
            _selectedWisdomTag,
            _selectedWisdomCategory,
            _onlyFavoriteWisdom
        ) { quotes, query, selectedTag, selectedCategory, favoritesOnly ->
            quotes.filter { quote ->
                val matchesFav = !favoritesOnly || quote.isFavorite
                val matchesCategory = selectedCategory == null || quote.category.equals(selectedCategory, ignoreCase = true)
                val matchesTag = selectedTag == null || quote.customTags.split(",").map { it.trim().lowercase() }.contains(selectedTag.trim().lowercase())
                val matchesQuery = query.isBlank() ||
                        quote.quoteText.contains(query, ignoreCase = true) ||
                        quote.sourceTitle.contains(query, ignoreCase = true) ||
                        quote.customTags.contains(query, ignoreCase = true) ||
                        quote.category.contains(query, ignoreCase = true) ||
                        quote.moonSignName.contains(query, ignoreCase = true) ||
                        quote.planetaryHourPlanet.contains(query, ignoreCase = true)

                matchesFav && matchesCategory && matchesTag && matchesQuery
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed initial contemplative data if first launch
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        // Live ticker to update celestial state every 15 seconds
        startLiveTicker()
    }

    private fun startLiveTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                refreshCelestialSnapshot()
                delay(15000)
            }
        }
    }

    fun refreshCelestialSnapshot() {
        _celestialSnapshot.value = AstroCalculations.calculateSnapshot(
            timestamp = System.currentTimeMillis(),
            city = _city.value,
            latitude = _latitude.value,
            longitude = _longitude.value
        )
    }

    fun getSnapshotForTimestamp(timestamp: Long): CelestialSnapshot {
        return AstroCalculations.calculateSnapshot(
            timestamp = timestamp,
            city = _city.value,
            latitude = _latitude.value,
            longitude = _longitude.value
        )
    }

    // Location management
    fun setLocation(cityName: String, lat: Double, lng: Double) {
        _city.value = cityName
        _latitude.value = lat
        _longitude.value = lng
        refreshCelestialSnapshot()
    }

    fun selectLocationPreset(preset: LocationPreset) {
        setLocation(preset.cityName, preset.latitude, preset.longitude)
    }

    fun fetchDeviceLocation(onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val loc = locationHelper.getDeviceLocation()
            if (loc != null) {
                val resolvedCity = locationHelper.reverseGeocode(loc.first, loc.second)
                setLocation(resolvedCity, loc.first, loc.second)
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    // Filter controls
    fun setScopeTab(tab: ScopeTab) {
        _selectedScopeTab.value = tab
    }

    fun setTypeFilter(type: String?) {
        _selectedTypeFilter.value = type
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Calendar navigation
    fun setSelectedDate(calendar: Calendar) {
        _selectedDate.value = calendar.clone() as Calendar
    }

    fun setCalendarViewType(type: CalendarViewType) {
        _calendarViewType.value = type
    }

    fun navigateMonth(delta: Int) {
        val newCal = (_selectedDate.value.clone() as Calendar).apply {
            add(Calendar.MONTH, delta)
        }
        _selectedDate.value = newCal
    }

    fun navigateWeek(delta: Int) {
        val newCal = (_selectedDate.value.clone() as Calendar).apply {
            add(Calendar.WEEK_OF_YEAR, delta)
        }
        _selectedDate.value = newCal
    }

    fun navigateDay(delta: Int) {
        val newCal = (_selectedDate.value.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, delta)
        }
        _selectedDate.value = newCal
    }

    fun resetToToday() {
        _selectedDate.value = Calendar.getInstance()
    }

    // Meditation Timer
    fun setTimerDuration(minutes: Int) {
        if (!_isTimerRunning.value) {
            _timerTotalSeconds.value = minutes * 60
            _timerRemainingSeconds.value = minutes * 60
            _timerFinished.value = false
        }
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        _timerFinished.value = false
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerRemainingSeconds.value > 0 && _isTimerRunning.value) {
                delay(1000)
                _timerRemainingSeconds.value -= 1
            }
            if (_timerRemainingSeconds.value <= 0) {
                _isTimerRunning.value = false
                _timerFinished.value = true
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        _timerRemainingSeconds.value = _timerTotalSeconds.value
        _timerFinished.value = false
    }

    fun dismissTimerFinished() {
        _timerFinished.value = false
    }

    // Wisdom Bank Filter & Search Methods
    fun setWisdomSearchQuery(query: String) {
        _wisdomSearchQuery.value = query
    }

    fun setSelectedWisdomTag(tag: String?) {
        _selectedWisdomTag.value = tag
    }

    fun setSelectedWisdomCategory(category: String?) {
        _selectedWisdomCategory.value = category
    }

    fun toggleOnlyFavoriteWisdom() {
        _onlyFavoriteWisdom.value = !_onlyFavoriteWisdom.value
    }

    // Wisdom Bank Database CRUD
    fun saveWisdomQuote(quote: WisdomQuote, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = if (quote.id == 0L) {
                repository.insertWisdom(quote)
            } else {
                repository.updateWisdom(quote)
                quote.id
            }
            onComplete(id)
        }
    }

    fun deleteWisdomQuote(quote: WisdomQuote) {
        viewModelScope.launch {
            repository.deleteWisdom(quote)
        }
    }

    fun toggleWisdomFavorite(quote: WisdomQuote) {
        viewModelScope.launch {
            repository.updateWisdom(quote.copy(isFavorite = !quote.isFavorite))
        }
    }

    fun extractWisdomFromEntry(
        entry: JournalEntry,
        selectedQuoteText: String,
        customTags: String,
        category: String = "Revelation",
        onComplete: (Long) -> Unit = {}
    ) {
        val newQuote = WisdomQuote(
            journalEntryId = entry.id,
            sourceTitle = entry.title.ifBlank { "Contemplation Insight" },
            quoteText = selectedQuoteText.trim(),
            customTags = customTags.trim(),
            category = category,
            timestamp = entry.timestamp,
            moonPhaseGlyph = entry.moonPhaseGlyph,
            moonSignName = entry.moonSignName,
            planetaryHourPlanet = entry.planetaryHourPlanet,
            tattwaName = entry.tattwaName,
            isFavorite = false
        )
        saveWisdomQuote(newQuote, onComplete)
    }

    // Database CRUD
    fun saveEntry(entry: JournalEntry, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = if (entry.id == 0L) {
                repository.insertEntry(entry)
            } else {
                repository.updateEntry(entry)
                entry.id
            }
            onComplete(id)
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    fun toggleFavorite(entry: JournalEntry) {
        viewModelScope.launch {
            repository.updateEntry(entry.copy(isFavorite = !entry.isFavorite))
        }
    }
}
