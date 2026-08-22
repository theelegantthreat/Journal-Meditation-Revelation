package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.astro.CelestialSnapshot

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val entryType: String, // REVELATION, INSPIRATION, INSIGHT, MEDITATION_SESSION, VISION_SYMBOL
    val timeframeScope: String, // DAILY, WEEKLY, MONTHLY
    val timestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 15,
    val depthRating: Int = 4, // 1 to 5
    val tags: String = "", // e.g. "Silence, Third Eye, Kundalini"
    val city: String = "Mount Shasta, CA",
    val latitude: Double = 41.3099,
    val longitude: Double = -122.3106,
    val moonPhaseName: String = "Full Moon",
    val moonPhaseGlyph: String = "🌕",
    val moonIllumination: Int = 98,
    val moonSignName: String = "Scorpio",
    val moonSignDegree: Int = 14,
    val sunSignName: String = "Leo",
    val sunSignDegree: Int = 29,
    val planetaryHourPlanet: String = "Venus",
    val planetaryHourNumber: Int = 3,
    val isDayHour: Boolean = true,
    val tattwaName: String = "Tejas",
    val subTattwaName: String = "Apas",
    val isFavorite: Boolean = false
) {
    companion object {
        fun fromSnapshot(
            title: String,
            content: String,
            entryType: String,
            timeframeScope: String,
            durationMinutes: Int,
            depthRating: Int,
            tags: String,
            snapshot: CelestialSnapshot
        ): JournalEntry {
            return JournalEntry(
                title = title,
                content = content,
                entryType = entryType,
                timeframeScope = timeframeScope,
                timestamp = snapshot.timestamp,
                durationMinutes = durationMinutes,
                depthRating = depthRating,
                tags = tags,
                city = snapshot.city,
                latitude = snapshot.latitude,
                longitude = snapshot.longitude,
                moonPhaseName = snapshot.moonPhase.title,
                moonPhaseGlyph = snapshot.moonPhase.glyph,
                moonIllumination = snapshot.moonIllumination,
                moonSignName = snapshot.moonSign.signName,
                moonSignDegree = snapshot.moonSignDegree,
                sunSignName = snapshot.sunSign.signName,
                sunSignDegree = snapshot.sunSignDegree,
                planetaryHourPlanet = snapshot.planetaryHour.rulingPlanet.planetName,
                planetaryHourNumber = snapshot.planetaryHour.hourNumber,
                isDayHour = snapshot.planetaryHour.isDayHour,
                tattwaName = snapshot.tattwaInfo.currentTattwa.tattwaName,
                subTattwaName = snapshot.tattwaInfo.subTattwa.tattwaName
            )
        }
    }
}
