package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.astro.CelestialSnapshot
import com.example.data.local.entity.JournalEntry
import com.example.ui.theme.InspirationGold

@Composable
fun EntryEditorDialog(
    initialEntry: JournalEntry? = null,
    currentSnapshot: CelestialSnapshot,
    onDismiss: () -> Unit,
    onSave: (JournalEntry) -> Unit
) {
    var title by remember { mutableStateOf(initialEntry?.title ?: "") }
    var content by remember { mutableStateOf(initialEntry?.content ?: "") }
    var entryType by remember { mutableStateOf(initialEntry?.entryType ?: "REVELATION") }
    var timeframeScope by remember { mutableStateOf(initialEntry?.timeframeScope ?: "DAILY") }
    var durationMinutes by remember { mutableIntStateOf(initialEntry?.durationMinutes ?: 20) }
    var depthRating by remember { mutableIntStateOf(initialEntry?.depthRating ?: 5) }
    var tags by remember { mutableStateOf(initialEntry?.tags ?: "") }

    // Astronomical Snapshot data
    var moonPhaseName by remember { mutableStateOf(initialEntry?.moonPhaseName ?: currentSnapshot.moonPhase.title) }
    var moonPhaseGlyph by remember { mutableStateOf(initialEntry?.moonPhaseGlyph ?: currentSnapshot.moonPhase.glyph) }
    var moonIllumination by remember { mutableIntStateOf(initialEntry?.moonIllumination ?: currentSnapshot.moonIllumination) }
    var moonSignName by remember { mutableStateOf(initialEntry?.moonSignName ?: currentSnapshot.moonSign.signName) }
    var moonSignDegree by remember { mutableIntStateOf(initialEntry?.moonSignDegree ?: currentSnapshot.moonSignDegree) }
    var sunSignName by remember { mutableStateOf(initialEntry?.sunSignName ?: currentSnapshot.sunSign.signName) }
    var sunSignDegree by remember { mutableIntStateOf(initialEntry?.sunSignDegree ?: currentSnapshot.sunSignDegree) }
    var planetaryHourPlanet by remember { mutableStateOf(initialEntry?.planetaryHourPlanet ?: currentSnapshot.planetaryHour.rulingPlanet.planetName) }
    var planetaryHourNumber by remember { mutableIntStateOf(initialEntry?.planetaryHourNumber ?: currentSnapshot.planetaryHour.hourNumber) }
    var isDayHour by remember { mutableStateOf(initialEntry?.isDayHour ?: currentSnapshot.planetaryHour.isDayHour) }
    var tattwaName by remember { mutableStateOf(initialEntry?.tattwaName ?: currentSnapshot.tattwaInfo.currentTattwa.tattwaName) }
    var subTattwaName by remember { mutableStateOf(initialEntry?.subTattwaName ?: currentSnapshot.tattwaInfo.subTattwa.tattwaName) }
    var city by remember { mutableStateOf(initialEntry?.city ?: currentSnapshot.city) }
    var latitude by remember { mutableStateOf(initialEntry?.latitude ?: currentSnapshot.latitude) }
    var longitude by remember { mutableStateOf(initialEntry?.longitude ?: currentSnapshot.longitude) }

    val entryTypes = listOf(
        "REVELATION" to "Revelation ✧",
        "INSPIRATION" to "Inspiration ✦",
        "INSIGHT" to "Insight ⚛",
        "MEDITATION_SESSION" to "Meditation 🧘",
        "VISION_SYMBOL" to "Vision 👁"
    )

    val scopes = listOf(
        "DAILY" to "Daily Entry",
        "WEEKLY" to "Weekly Review",
        "MONTHLY" to "Monthly Synthesis"
    )

    val durations = listOf(5, 10, 15, 20, 30, 45, 60, 90)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 700.dp)
                .testTag("entry_editor_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialEntry == null) "Record Revelation & Insight" else "Edit Meditation Entry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_editor_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Entry Type Selector
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        entryTypes.forEach { (typeKey, label) ->
                            FilterChip(
                                selected = entryType == typeKey,
                                onClick = { entryType = typeKey },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Timeframe Scope Selector
                    Text(
                        text = "Timeframe Scope",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        scopes.forEach { (scopeKey, label) ->
                            FilterChip(
                                selected = timeframeScope == scopeKey,
                                onClick = { timeframeScope = scopeKey },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title or Revelation Theme") },
                        placeholder = { Text("e.g., Stillness Beyond The Mind") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("entry_title_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Content
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Revelation, Insights & Meditation Contemplation") },
                        placeholder = { Text("Describe the visions, inner knowing, breath sensations, and spiritual realizations experienced during meditation...") },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("entry_content_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Duration & Depth Rating Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Duration
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Meditation Duration",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                durations.forEach { dur ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (durationMinutes == dur) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { durationMinutes = dur }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${dur}m",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (durationMinutes == dur) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (durationMinutes == dur) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Depth Rating (Stars)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Meditative Depth",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = if (i <= depthRating) Icons.Default.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "Rating $i",
                                        tint = if (i <= depthRating) InspirationGold else MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { depthRating = i }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tags
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma separated)") },
                        placeholder = { Text("e.g., Silence, Kundalini, Third Eye, Peace") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Astrological Correspondences Card
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🌌 Attached Celestial Snapshot",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = {
                                        moonPhaseName = currentSnapshot.moonPhase.title
                                        moonPhaseGlyph = currentSnapshot.moonPhase.glyph
                                        moonIllumination = currentSnapshot.moonIllumination
                                        moonSignName = currentSnapshot.moonSign.signName
                                        moonSignDegree = currentSnapshot.moonSignDegree
                                        sunSignName = currentSnapshot.sunSign.signName
                                        sunSignDegree = currentSnapshot.sunSignDegree
                                        planetaryHourPlanet = currentSnapshot.planetaryHour.rulingPlanet.planetName
                                        planetaryHourNumber = currentSnapshot.planetaryHour.hourNumber
                                        isDayHour = currentSnapshot.planetaryHour.isDayHour
                                        tattwaName = currentSnapshot.tattwaInfo.currentTattwa.tattwaName
                                        subTattwaName = currentSnapshot.tattwaInfo.subTattwa.tattwaName
                                        city = currentSnapshot.city
                                        latitude = currentSnapshot.latitude
                                        longitude = currentSnapshot.longitude
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Sync Current",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "• Moon: $moonPhaseGlyph $moonPhaseName ($moonIllumination%) in $moonSignName $moonSignDegree°",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "• Sun: ☉ $sunSignName $sunSignDegree°",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "• Planetary Hour: $planetaryHourPlanet (${if (isDayHour) "Day" else "Night"} Hour #$planetaryHourNumber)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "• Tattwa: $tattwaName (Sub: $subTattwaName)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "• Location: $city (Lat: $latitude, Lon: $longitude)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (title.isBlank() && content.isNotBlank()) {
                                title = content.take(30).trim() + "..."
                            }
                            if (title.isBlank()) {
                                title = "Meditation Revelation"
                            }

                            val newEntry = JournalEntry(
                                id = initialEntry?.id ?: 0L,
                                title = title,
                                content = content,
                                entryType = entryType,
                                timeframeScope = timeframeScope,
                                timestamp = initialEntry?.timestamp ?: System.currentTimeMillis(),
                                durationMinutes = durationMinutes,
                                depthRating = depthRating,
                                tags = tags,
                                city = city,
                                latitude = latitude,
                                longitude = longitude,
                                moonPhaseName = moonPhaseName,
                                moonPhaseGlyph = moonPhaseGlyph,
                                moonIllumination = moonIllumination,
                                moonSignName = moonSignName,
                                moonSignDegree = moonSignDegree,
                                sunSignName = sunSignName,
                                sunSignDegree = sunSignDegree,
                                planetaryHourPlanet = planetaryHourPlanet,
                                planetaryHourNumber = planetaryHourNumber,
                                isDayHour = isDayHour,
                                tattwaName = tattwaName,
                                subTattwaName = subTattwaName,
                                isFavorite = initialEntry?.isFavorite ?: false
                            )

                            onSave(newEntry)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_entry_btn")
                    ) {
                        Text("Save Revelation")
                    }
                }
            }
        }
    }
}
