package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wisdom_quotes")
data class WisdomQuote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val journalEntryId: Long? = null,
    val sourceTitle: String = "Spontaneous Insight",
    val quoteText: String,
    val customTags: String = "", // Comma-separated custom tags, e.g. "Stillness, Non-Duality, Breath"
    val category: String = "Revelation", // Revelation, Insight, Epiphany, Axiom, Mantra, Core Truth
    val timestamp: Long = System.currentTimeMillis(),
    val moonPhaseGlyph: String = "🌕",
    val moonSignName: String = "Scorpio",
    val planetaryHourPlanet: String = "Venus",
    val tattwaName: String = "Akasha",
    val isFavorite: Boolean = false
)
