package com.example.astro

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AkashaViolet
import com.example.ui.theme.ApasWaterSilver
import com.example.ui.theme.PrithviEarthGold
import com.example.ui.theme.TejasFire
import com.example.ui.theme.VayuSkyBlue
import java.util.Calendar

enum class MoonPhaseType(val title: String, val glyph: String, val description: String) {
    NEW_MOON("New Moon", "🌑", "Time for planting sacred intentions, deep inner stillness, and initiating new meditative cycles."),
    WAXING_CRESCENT("Waxing Crescent", "🌒", "Emerging inspiration, gathering subtle energy, setting devotional focus."),
    FIRST_QUARTER("First Quarter", "🌓", "Strength, overcoming mental inertia, deepening concentration."),
    WAXING_GIBBOUS("Waxing Gibbous", "🌔", "Refining insight, spiritual expansion, contemplation before culmination."),
    FULL_MOON("Full Moon", "🌕", "Peak illumination, spiritual revelation, heightened psychic receptivity."),
    WANING_GIBBOUS("Waning Gibbous", "🌖", "Gratitude, integrating revelations, sharing wisdom, contemplation."),
    LAST_QUARTER("Last Quarter", "🌗", "Release of mental friction, surrender, forgiveness, purification."),
    WANING_CRESCENT("Waning Crescent", "🌘", "Deep rest, meditative dissolution, listening to the inner void.")
}

enum class ZodiacSign(
    val signName: String,
    val glyph: String,
    val element: String,
    val modality: String,
    val keywords: String
) {
    ARIES("Aries", "♈", "Fire", "Cardinal", "Courage, initiative, awakening spiritual fire"),
    TAURUS("Taurus", "♉", "Earth", "Fixed", "Grounding, inner sanctuary, somatic peace"),
    GEMINI("Gemini", "♊", "Air", "Mutable", "Clarity, synthesis of insights, sacred breath"),
    CANSER("Cancer", "♋", "Water", "Cardinal", "Emotional purification, divine mother, intuition"),
    LEO("Leo", "♌", "Fire", "Fixed", "Heart center radiance, sovereign presence, pure light"),
    VIRGO("Virgo", "♍", "Earth", "Mutable", "Discernment, devotion, sacred alignment"),
    LIBRA("Libra", "♎", "Air", "Cardinal", "Harmonic balance, inner equilibrium, stillness"),
    SCORPIO("Scorpio", "♏", "Water", "Fixed", "Transmutation, deep revelation, mystical rebirth"),
    SAGITTARIUS("Sagittarius", "♐", "Fire", "Mutable", "Higher truth, philosophical vision, expansive joy"),
    CAPRICORN("Capricorn", "♑", "Earth", "Cardinal", "Mastery, steadfast discipline, spiritual mountain"),
    AQUARIUS("Aquarius", "♒", "Air", "Fixed", "Cosmic consciousness, flashes of revelation, liberation"),
    PISCES("Pisces", "♓", "Water", "Mutable", "Unitive awareness, boundless compassion, oceanic peace")
}

enum class Planet(
    val planetName: String,
    val symbol: String,
    val title: String,
    val dayOfWeek: String,
    val metal: String,
    val color: String,
    val meditativeFocus: String
) {
    SATURN("Saturn", "♄", "Lord of Time & Silence", "Saturday", "Lead", "Deep Indigo", "Stillness, boundary transcendence, deep discipline & karma dissolution"),
    JUPITER("Jupiter", "♃", "Lord of Wisdom & Grace", "Thursday", "Tin", "Royal Blue", "Expansion, spiritual grace, benevolent revelations & higher philosophy"),
    MARS("Mars", "♂", "Lord of Vital Energy", "Tuesday", "Iron", "Crimson", "Spiritual courage, burning impurities, decisive focus & inner strength"),
    SUN("Sun", "☉", "Solar Center of Consciousness", "Sunday", "Gold", "Solar Amber", "Radiant Self, pure awareness, illuminated clarity & vital life force"),
    VENUS("Venus", "♀", "Lady of Harmony & Devotion", "Friday", "Copper", "Emerald Green", "Bhakti devotion, inner beauty, heart awakening & divine peace"),
    MERCURY("Mercury", "☿", "Messenger of Higher Mind", "Wednesday", "Quicksilver", "Sky Cyan", "Mental synthesis, symbolic insight, mantra chanting & breath control"),
    MOON("Moon", "☽", "Goddess of Receptivity", "Monday", "Silver", "Luminous Pearl", "Intuition, subconscious unveiling, receptive meditation & astral stillness")
}

enum class Tattwa(
    val tattwaName: String,
    val sanskritName: String,
    val element: String,
    val symbolShape: String,
    val symbolGlyph: String,
    val colorHex: Color,
    val meditationQuality: String,
    val pranayamaSuggestion: String
) {
    AKASHA(
        "Akasha",
        "आकाश",
        "Spirit / Ether",
        "Egg (Ovoid)",
        "⬭",
        AkashaViolet,
        "Transcendence, space between thoughts, pure void, direct spiritual revelation.",
        "Maha Mudra & Silent witnessing (Sakshi Bhava). Hold awareness in Ajna / Crown."
    ),
    VAYU(
        "Vayu",
        "वायु",
        "Air",
        "Circle",
        "⚪",
        VayuSkyBlue,
        "Movement of thoughts, subtle breath (Prana), intellectual clarity, inspiring visions.",
        "Nadi Shodhana (Alternate nostril breathing) or gentle rhythmic Pranayama."
    ),
    TEJAS(
        "Tejas",
        "तेजस्",
        "Fire",
        "Upward Triangle",
        "▲",
        TejasFire,
        "Luminosity, spiritual intensity, burning egoic resistance, sudden flash of insight.",
        "Kapalabhati, Surya Bhedana, or Trataka (candle gazing meditation)."
    ),
    APAS(
        "Apas",
        "आपस्",
        "Water",
        "Horizontal Crescent",
        "☽",
        ApasWaterSilver,
        "Fluid awareness, intuitive receptivity, emotional healing, gentle devotional flow.",
        "Chandra Bhedana or continuous, cooling, fluid tidal breathing."
    ),
    PRITHVI(
        "Prithvi",
        "पृथिवी",
        "Earth",
        "Yellow Square",
        "■",
        PrithviEarthGold,
        "Physical grounding, steady root (Muladhara), integration of revelations, unwavering calm.",
        "Box Breathing (Sama Vritti 4-4-4-4) and Body Scan meditation."
    )
}

data class PlanetaryHourInfo(
    val hourNumber: Int, // 1..12
    val isDayHour: Boolean,
    val rulingPlanet: Planet,
    val dayRulerPlanet: Planet,
    val startTimeFormatted: String,
    val endTimeFormatted: String,
    val remainingMinutes: Int,
    val progressFraction: Float
)

data class TattwaInfo(
    val currentTattwa: Tattwa,
    val subTattwa: Tattwa,
    val startMinute: Int,
    val endMinute: Int,
    val remainingMinutes: Int,
    val progressFraction: Float
)

data class MoonPhaseData(
    val phaseType: MoonPhaseType,
    val illuminationPercent: Int,
    val ageDays: Double,
    val phaseAngleDegrees: Double,
    val isWaxing: Boolean,
    val glyph: String,
    val title: String,
    val description: String,
    val moonSign: ZodiacSign? = null,
    val moonSignDegree: Int? = null
)

data class SunSignData(
    val sign: ZodiacSign,
    val degree: Int,
    val exactEclipticLongitude: Double,
    val element: String,
    val modality: String,
    val keywords: String,
    val glyph: String
)

data class SolarTimes(
    val sunrise: Calendar,
    val sunset: Calendar,
    val solarNoon: Calendar,
    val nextSunrise: Calendar,
    val sunriseFormatted: String,
    val sunsetFormatted: String,
    val isPolarDay: Boolean = false,
    val isPolarNight: Boolean = false
)

data class PlanetaryHourSlot(
    val hourNumber: Int, // 1..12
    val isDayHour: Boolean,
    val rulingPlanet: Planet,
    val startTime: Calendar,
    val endTime: Calendar,
    val startTimeFormatted: String,
    val endTimeFormatted: String,
    val isCurrent: Boolean = false
)

data class CelestialSnapshot(
    val timestamp: Long,
    val moonPhase: MoonPhaseType,
    val moonIllumination: Int, // percentage 0..100
    val moonAgeDays: Double,
    val moonSign: ZodiacSign,
    val moonSignDegree: Int,
    val sunSign: ZodiacSign,
    val sunSignDegree: Int,
    val planetaryHour: PlanetaryHourInfo,
    val tattwaInfo: TattwaInfo,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val sunriseFormatted: String,
    val sunsetFormatted: String
)

data class CelestialCalculationResult(
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val moonPhase: MoonPhaseData,
    val sunSign: SunSignData,
    val currentPlanetaryHour: PlanetaryHourInfo,
    val planetaryHourSchedule: List<PlanetaryHourSlot>,
    val solarTimes: SolarTimes,
    val tattwaInfo: TattwaInfo,
    val snapshot: CelestialSnapshot
)



