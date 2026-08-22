package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astro.AstroCalculations
import com.example.astro.CelestialSnapshot
import com.example.data.local.entity.JournalEntry
import com.example.ui.components.CelestialLiveCard
import com.example.ui.components.JournalEntryCard
import com.example.ui.viewmodel.CalendarViewType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    selectedDate: Calendar,
    calendarViewType: CalendarViewType,
    allEntries: List<JournalEntry>,
    selectedDateEntries: List<JournalEntry>,
    currentSnapshot: CelestialSnapshot,
    onViewTypeChange: (CalendarViewType) -> Unit,
    onSelectDate: (Calendar) -> Unit,
    onNavigateMonth: (Int) -> Unit,
    onNavigateWeek: (Int) -> Unit,
    onNavigateDay: (Int) -> Unit,
    onResetToday: () -> Unit,
    onNewEntryForDate: (Calendar) -> Unit,
    onSelectEntry: (JournalEntry) -> Unit,
    onEditEntry: (JournalEntry) -> Unit,
    onDeleteEntry: (JournalEntry) -> Unit,
    onToggleFavorite: (JournalEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val dayHeaderFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen")
    ) {
        // View Type Selector Tabs (Monthly, Weekly, Daily)
        TabRow(
            selectedTabIndex = calendarViewType.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = calendarViewType == CalendarViewType.MONTHLY,
                onClick = { onViewTypeChange(CalendarViewType.MONTHLY) },
                text = { Text("Monthly View", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = calendarViewType == CalendarViewType.WEEKLY,
                onClick = { onViewTypeChange(CalendarViewType.WEEKLY) },
                text = { Text("Weekly View", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = calendarViewType == CalendarViewType.DAILY,
                onClick = { onViewTypeChange(CalendarViewType.DAILY) },
                text = { Text("Daily View", fontWeight = FontWeight.SemiBold) }
            )
        }

        when (calendarViewType) {
            CalendarViewType.MONTHLY -> {
                MonthlyCalendarView(
                    selectedDate = selectedDate,
                    allEntries = allEntries,
                    selectedDateEntries = selectedDateEntries,
                    onSelectDate = onSelectDate,
                    onNavigateMonth = onNavigateMonth,
                    onResetToday = onResetToday,
                    onNewEntry = { onNewEntryForDate(selectedDate) },
                    onSelectEntry = onSelectEntry,
                    onEditEntry = onEditEntry,
                    onDeleteEntry = onDeleteEntry,
                    onToggleFavorite = onToggleFavorite
                )
            }
            CalendarViewType.WEEKLY -> {
                WeeklyCalendarView(
                    selectedDate = selectedDate,
                    allEntries = allEntries,
                    onSelectDate = onSelectDate,
                    onNavigateWeek = onNavigateWeek,
                    onResetToday = onResetToday,
                    onNewEntryForDate = onNewEntryForDate,
                    onSelectEntry = onSelectEntry,
                    onEditEntry = onEditEntry,
                    onDeleteEntry = onDeleteEntry,
                    onToggleFavorite = onToggleFavorite
                )
            }
            CalendarViewType.DAILY -> {
                DailyCalendarView(
                    selectedDate = selectedDate,
                    entries = selectedDateEntries,
                    currentSnapshot = currentSnapshot,
                    onNavigateDay = onNavigateDay,
                    onResetToday = onResetToday,
                    onNewEntry = { onNewEntryForDate(selectedDate) },
                    onSelectEntry = onSelectEntry,
                    onEditEntry = onEditEntry,
                    onDeleteEntry = onDeleteEntry,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
    }
}

@Composable
fun MonthlyCalendarView(
    selectedDate: Calendar,
    allEntries: List<JournalEntry>,
    selectedDateEntries: List<JournalEntry>,
    onSelectDate: (Calendar) -> Unit,
    onNavigateMonth: (Int) -> Unit,
    onResetToday: () -> Unit,
    onNewEntry: () -> Unit,
    onSelectEntry: (JournalEntry) -> Unit,
    onEditEntry: (JournalEntry) -> Unit,
    onDeleteEntry: (JournalEntry) -> Unit,
    onToggleFavorite: (JournalEntry) -> Unit
) {
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val dayDetailFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    val todayCal = Calendar.getInstance()

    // Build the grid days for this month
    val monthCal = (selectedDate.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK) // 1 (Sun) .. 7 (Sat)
    val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Navigation Header
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNavigateMonth(-1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = monthYearFormat.format(selectedDate.time),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onResetToday() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = { onNavigateMonth(1) }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                        }
                    }
                }
            }
        }

        // Calendar Grid Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Day of week headers with ancient planetary rulers!
                    val dayRulers = listOf(
                        "Sun ☉", "Mon ☽", "Tue ♂", "Wed ☿", "Thu ♃", "Fri ♀", "Sat ♄"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        dayRulers.forEach { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    // 6 Rows of 7 Days
                    var dayCounter = 1 - (firstDayOfWeek - 1)

                    for (row in 0 until 6) {
                        if (dayCounter > daysInMonth) break

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (col in 0 until 7) {
                                val currentDayNum = dayCounter

                                if (currentDayNum in 1..daysInMonth) {
                                    val cellCal = (selectedDate.clone() as Calendar).apply {
                                        set(Calendar.DAY_OF_MONTH, currentDayNum)
                                    }
                                    val isSelected = selectedDate.get(Calendar.YEAR) == cellCal.get(Calendar.YEAR) &&
                                            selectedDate.get(Calendar.MONTH) == cellCal.get(Calendar.MONTH) &&
                                            selectedDate.get(Calendar.DAY_OF_MONTH) == currentDayNum

                                    val isToday = todayCal.get(Calendar.YEAR) == cellCal.get(Calendar.YEAR) &&
                                            todayCal.get(Calendar.MONTH) == cellCal.get(Calendar.MONTH) &&
                                            todayCal.get(Calendar.DAY_OF_MONTH) == currentDayNum

                                    // Check entries on this day
                                    val startDayMs = (cellCal.clone() as Calendar).apply {
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                    }.timeInMillis
                                    val endDayMs = (cellCal.clone() as Calendar).apply {
                                        set(Calendar.HOUR_OF_DAY, 23)
                                        set(Calendar.MINUTE, 59)
                                        set(Calendar.SECOND, 59)
                                    }.timeInMillis

                                    val entriesOnDay = allEntries.filter { it.timestamp in startDayMs..endDayMs }

                                    // Calculate Moon Phase for this day
                                    val moonPhaseTriple = AstroCalculations.calculateMoonPhase(cellCal.timeInMillis)
                                    val moonGlyph = moonPhaseTriple.first.glyph

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable { onSelectDate(cellCal) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$currentDayNum",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = moonGlyph,
                                                    fontSize = 8.sp
                                                )
                                                if (entriesOnDay.isNotEmpty()) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                dayCounter++
                            }
                        }
                    }
                }
            }
        }

        // Selected Day Details & Entries
        item {
            val cellSnapshot = AstroCalculations.calculateSnapshot(selectedDate.timeInMillis)

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = dayDetailFormat.format(selectedDate.time),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${cellSnapshot.moonPhase.glyph} ${cellSnapshot.moonPhase.title} in ${cellSnapshot.moonSign.signName} • Sun in ${cellSnapshot.sunSign.signName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = onNewEntry,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Entry")
                        }
                    }
                }
            }
        }

        // Entries list for selected date
        if (selectedDateEntries.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No meditation entries recorded for this date.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(selectedDateEntries, key = { it.id }) { entry ->
                JournalEntryCard(
                    entry = entry,
                    onClick = { onSelectEntry(entry) },
                    onEdit = { onEditEntry(entry) },
                    onDelete = { onDeleteEntry(entry) },
                    onToggleFavorite = { onToggleFavorite(entry) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun WeeklyCalendarView(
    selectedDate: Calendar,
    allEntries: List<JournalEntry>,
    onSelectDate: (Calendar) -> Unit,
    onNavigateWeek: (Int) -> Unit,
    onResetToday: () -> Unit,
    onNewEntryForDate: (Calendar) -> Unit,
    onSelectEntry: (JournalEntry) -> Unit,
    onEditEntry: (JournalEntry) -> Unit,
    onDeleteEntry: (JournalEntry) -> Unit,
    onToggleFavorite: (JournalEntry) -> Unit
) {
    val weekFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    val dayHeaderFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    // Get start of week (Sunday)
    val startOfWeekCal = (selectedDate.clone() as Calendar).apply {
        set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    val endOfWeekCal = (startOfWeekCal.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, 6)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Week Navigation Header
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNavigateWeek(-1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Week")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Week of ${weekFormat.format(startOfWeekCal.time)} – ${weekFormat.format(endOfWeekCal.time)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onResetToday() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Current Week",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = { onNavigateWeek(1) }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Week")
                        }
                    }
                }
            }
        }

        // 7 Day Cards for the Week
        for (i in 0 until 7) {
            val dayCal = (startOfWeekCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, i)
            }

            val startDayMs = (dayCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis
            val endDayMs = (dayCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.timeInMillis

            val dayEntries = allEntries.filter { it.timestamp in startDayMs..endDayMs }
            val daySnapshot = AstroCalculations.calculateSnapshot(dayCal.timeInMillis)
            val isToday = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR) &&
                    Calendar.getInstance().get(Calendar.YEAR) == dayCal.get(Calendar.YEAR)

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isToday)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = dayHeaderFormat.format(dayCal.time),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isToday) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Today",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { onNewEntryForDate(dayCal) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Entry", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        // Celestial Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Day Ruler: ${daySnapshot.planetaryHour.dayRulerPlanet.symbol} ${daySnapshot.planetaryHour.dayRulerPlanet.planetName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${daySnapshot.moonPhase.glyph} ${daySnapshot.moonSign.signName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (dayEntries.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                dayEntries.forEach { entry ->
                                    JournalEntryCard(
                                        entry = entry,
                                        onClick = { onSelectEntry(entry) },
                                        onEdit = { onEditEntry(entry) },
                                        onDelete = { onDeleteEntry(entry) },
                                        onToggleFavorite = { onToggleFavorite(entry) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun DailyCalendarView(
    selectedDate: Calendar,
    entries: List<JournalEntry>,
    currentSnapshot: CelestialSnapshot,
    onNavigateDay: (Int) -> Unit,
    onResetToday: () -> Unit,
    onNewEntry: () -> Unit,
    onSelectEntry: (JournalEntry) -> Unit,
    onEditEntry: (JournalEntry) -> Unit,
    onDeleteEntry: (JournalEntry) -> Unit,
    onToggleFavorite: (JournalEntry) -> Unit
) {
    val dayFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    val daySnapshot = AstroCalculations.calculateSnapshot(selectedDate.timeInMillis)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Day Navigation Header
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNavigateDay(-1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dayFormat.format(selectedDate.time),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onResetToday() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = { onNavigateDay(1) }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                        }
                    }
                }
            }
        }

        // Live Celestial Snapshot for this Day
        item {
            CelestialLiveCard(
                snapshot = daySnapshot,
                onLocationClick = {},
                onInfoClick = {}
            )
        }

        // Action Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Entries for this Date (${entries.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = onNewEntry,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Entry")
                }
            }
        }

        // Entries list
        if (entries.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No meditation entries for this day. Tap 'New Entry' to add one.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(entries, key = { it.id }) { entry ->
                JournalEntryCard(
                    entry = entry,
                    onClick = { onSelectEntry(entry) },
                    onEdit = { onEditEntry(entry) },
                    onDelete = { onDeleteEntry(entry) },
                    onToggleFavorite = { onToggleFavorite(entry) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
