package com.example.data.repository

import com.example.data.local.dao.JournalDao
import com.example.data.local.dao.WisdomDao
import com.example.data.local.entity.JournalEntry
import com.example.data.local.entity.WisdomQuote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class JournalRepository(
    private val journalDao: JournalDao,
    private val wisdomDao: WisdomDao
) {

    val allEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()
    val allWisdom: Flow<List<WisdomQuote>> = wisdomDao.getAllWisdom()
    val favoriteWisdom: Flow<List<WisdomQuote>> = wisdomDao.getFavoriteWisdom()

    fun getEntriesByScope(scope: String): Flow<List<JournalEntry>> =
        journalDao.getEntriesByScope(scope)

    fun getEntriesByType(type: String): Flow<List<JournalEntry>> =
        journalDao.getEntriesByType(type)

    fun getEntriesByDateRange(startTime: Long, endTime: Long): Flow<List<JournalEntry>> =
        journalDao.getEntriesByDateRange(startTime, endTime)

    fun getEntryById(id: Long): Flow<JournalEntry?> =
        journalDao.getEntryById(id)

    suspend fun insertEntry(entry: JournalEntry): Long =
        journalDao.insertEntry(entry)

    suspend fun updateEntry(entry: JournalEntry) =
        journalDao.updateEntry(entry)

    suspend fun deleteEntry(entry: JournalEntry) =
        journalDao.deleteEntry(entry)

    suspend fun deleteEntryById(id: Long) =
        journalDao.deleteEntryById(id)

    // Wisdom Bank Operations
    fun getWisdomForEntry(entryId: Long): Flow<List<WisdomQuote>> =
        wisdomDao.getWisdomForEntry(entryId)

    suspend fun insertWisdom(quote: WisdomQuote): Long =
        wisdomDao.insertWisdom(quote)

    suspend fun updateWisdom(quote: WisdomQuote) =
        wisdomDao.updateWisdom(quote)

    suspend fun deleteWisdom(quote: WisdomQuote) =
        wisdomDao.deleteWisdom(quote)

    suspend fun deleteWisdomById(id: Long) =
        wisdomDao.deleteWisdomById(id)

    suspend fun seedInitialDataIfEmpty() {
        val count = journalDao.getEntryCount().first()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val dayMs = 86400000L

            val initialEntries = listOf(
                JournalEntry(
                    title = "The Golden Stillness Behind Breath",
                    content = "During silent meditation at dawn, as the breath naturally slowed to an almost imperceptible rhythm, a sudden wave of unconditional peace dissolved the lingering tension in the chest. A clear revelation arose: 'You are not the traveler traversing time; you are the timeless space through which all phenomena arise and dissolve.'",
                    entryType = "REVELATION",
                    timeframeScope = "DAILY",
                    timestamp = now - (1000 * 60 * 60 * 2), // 2 hours ago
                    durationMinutes = 30,
                    depthRating = 5,
                    tags = "Stillness, Non-Duality, Dawn, Breath",
                    city = "Mount Shasta, CA",
                    latitude = 41.3099,
                    longitude = -122.3106,
                    moonPhaseName = "Waxing Gibbous",
                    moonPhaseGlyph = "🌔",
                    moonIllumination = 84,
                    moonSignName = "Sagittarius",
                    moonSignDegree = 18,
                    sunSignName = "Leo",
                    sunSignDegree = 29,
                    planetaryHourPlanet = "Sun",
                    planetaryHourNumber = 1,
                    isDayHour = true,
                    tattwaName = "Akasha",
                    subTattwaName = "Tejas",
                    isFavorite = true
                ),
                JournalEntry(
                    title = "Flash of Creative Architecture",
                    content = "Sitting in open presence. Visualized a pristine geometric spiral connecting higher intuition with grounded daily action. Received creative inspiration on how to harmonize contemplative practice with practical work without fragmentation.",
                    entryType = "INSPIRATION",
                    timeframeScope = "DAILY",
                    timestamp = now - dayMs,
                    durationMinutes = 20,
                    depthRating = 4,
                    tags = "Creative, Geometry, Harmony, Work",
                    city = "Mount Shasta, CA",
                    latitude = 41.3099,
                    longitude = -122.3106,
                    moonPhaseName = "Waxing Gibbous",
                    moonPhaseGlyph = "🌔",
                    moonIllumination = 76,
                    moonSignName = "Scorpio",
                    moonSignDegree = 28,
                    sunSignName = "Leo",
                    sunSignDegree = 28,
                    planetaryHourPlanet = "Jupiter",
                    planetaryHourNumber = 4,
                    isDayHour = true,
                    tattwaName = "Vayu",
                    subTattwaName = "Apas",
                    isFavorite = true
                ),
                JournalEntry(
                    title = "Insight on Non-Attachment and Joy",
                    content = "Realized that emotional clinging to past spiritual experiences prevents fresh revelation in the present moment. True insight requires emptying the vessel completely before every meditation sitting.",
                    entryType = "INSIGHT",
                    timeframeScope = "DAILY",
                    timestamp = now - (dayMs * 2),
                    durationMinutes = 25,
                    depthRating = 4,
                    tags = "Surrender, Empty Cup, Joy",
                    city = "Mount Shasta, CA",
                    latitude = 41.3099,
                    longitude = -122.3106,
                    moonPhaseName = "First Quarter",
                    moonPhaseGlyph = "🌓",
                    moonIllumination = 62,
                    moonSignName = "Libra",
                    moonSignDegree = 12,
                    sunSignName = "Leo",
                    sunSignDegree = 27,
                    planetaryHourPlanet = "Venus",
                    planetaryHourNumber = 5,
                    isDayHour = true,
                    tattwaName = "Apas",
                    subTattwaName = "Prithvi",
                    isFavorite = false
                ),
                JournalEntry(
                    title = "Weekly Contemplation: Purification & Flow",
                    content = "Synthesis of this week's meditations: Major shift occurred across the midweek lunar transition into Scorpio. Consistently experienced deep physical relaxation during Water (Apas) tattwa phases. Key thematic lesson: Patience in the silence yields greater clarity than striving for visionary states.",
                    entryType = "REVELATION",
                    timeframeScope = "WEEKLY",
                    timestamp = now - (dayMs * 3),
                    durationMinutes = 45,
                    depthRating = 5,
                    tags = "Weekly Review, Water Element, Integration",
                    city = "Mount Shasta, CA",
                    latitude = 41.3099,
                    longitude = -122.3106,
                    moonPhaseName = "First Quarter",
                    moonPhaseGlyph = "🌓",
                    moonIllumination = 54,
                    moonSignName = "Virgo",
                    moonSignDegree = 24,
                    sunSignName = "Leo",
                    sunSignDegree = 26,
                    planetaryHourPlanet = "Saturn",
                    planetaryHourNumber = 8,
                    isDayHour = true,
                    tattwaName = "Prithvi",
                    subTattwaName = "Akasha",
                    isFavorite = true
                ),
                JournalEntry(
                    title = "Monthly Spiritual Synthesis: Solar Cycle Awakening",
                    content = "Reflections on the full lunar and solar cycle. Total meditation time reached 18 hours across 30 sessions. The alignment of sunrise meditations with Solar and Jupiter hours noticeably increased vitality and clarity in written revelations. The overarching spiritual insight of the month: 'Ground the transcendent into everyday acts of compassion.'",
                    entryType = "INSIGHT",
                    timeframeScope = "MONTHLY",
                    timestamp = now - (dayMs * 7),
                    durationMinutes = 60,
                    depthRating = 5,
                    tags = "Monthly Synthesis, Solar Awakening, Milestones",
                    city = "Mount Shasta, CA",
                    latitude = 41.3099,
                    longitude = -122.3106,
                    moonPhaseName = "New Moon",
                    moonPhaseGlyph = "🌑",
                    moonIllumination = 4,
                    moonSignName = "Leo",
                    moonSignDegree = 5,
                    sunSignName = "Leo",
                    sunSignDegree = 21,
                    planetaryHourPlanet = "Sun",
                    planetaryHourNumber = 1,
                    isDayHour = true,
                    tattwaName = "Tejas",
                    subTattwaName = "Akasha",
                    isFavorite = true
                )
            )

            for (entry in initialEntries) {
                journalDao.insertEntry(entry)
            }
        }

        val wisdomCount = wisdomDao.getWisdomCount().first()
        if (wisdomCount == 0) {
            val now = System.currentTimeMillis()
            val dayMs = 86400000L

            val seedWisdom = listOf(
                WisdomQuote(
                    journalEntryId = 1L,
                    sourceTitle = "The Golden Stillness Behind Breath",
                    quoteText = "You are not the traveler traversing time; you are the timeless space through which all phenomena arise and dissolve.",
                    customTags = "Non-Duality, Stillness, Presence, Timelessness",
                    category = "Revelation",
                    timestamp = now - (1000 * 60 * 60 * 2),
                    moonPhaseGlyph = "🌔",
                    moonSignName = "Sagittarius",
                    planetaryHourPlanet = "Sun",
                    tattwaName = "Akasha",
                    isFavorite = true
                ),
                WisdomQuote(
                    journalEntryId = 5L,
                    sourceTitle = "Monthly Spiritual Synthesis: Solar Cycle Awakening",
                    quoteText = "Ground the transcendent into everyday acts of compassion.",
                    customTags = "Compassion, Action, Solar Awakening, Dharma",
                    category = "Axiom",
                    timestamp = now - (dayMs * 7),
                    moonPhaseGlyph = "🌑",
                    moonSignName = "Leo",
                    planetaryHourPlanet = "Sun",
                    tattwaName = "Tejas",
                    isFavorite = true
                ),
                WisdomQuote(
                    journalEntryId = 4L,
                    sourceTitle = "Weekly Contemplation: Purification & Flow",
                    quoteText = "Patience in the silence yields greater clarity than striving for visionary states.",
                    customTags = "Patience, Silence, Meditation, Letting Go",
                    category = "Insight",
                    timestamp = now - (dayMs * 3),
                    moonPhaseGlyph = "🌓",
                    moonSignName = "Virgo",
                    planetaryHourPlanet = "Saturn",
                    tattwaName = "Prithvi",
                    isFavorite = false
                ),
                WisdomQuote(
                    journalEntryId = 3L,
                    sourceTitle = "Insight on Non-Attachment and Joy",
                    quoteText = "True insight requires emptying the vessel completely before every meditation sitting.",
                    customTags = "Empty Cup, Surrender, Freshness, Humility",
                    category = "Epiphany",
                    timestamp = now - (dayMs * 2),
                    moonPhaseGlyph = "🌓",
                    moonSignName = "Libra",
                    planetaryHourPlanet = "Venus",
                    tattwaName = "Apas",
                    isFavorite = true
                )
            )

            for (quote in seedWisdom) {
                wisdomDao.insertWisdom(quote)
            }
        }
    }
}

