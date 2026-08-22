package com.example.astro

import java.util.Calendar
import java.util.Date

/**
 * AstroCalculations facade delegating to the comprehensive [AstroCalculator] utility.
 */
object AstroCalculations {

    val chaldeanOrder: List<Planet> get() = AstroCalculator.chaldeanOrder
    val dayRulers: Map<Int, Planet> get() = AstroCalculator.dayRulers

    fun calculateSnapshot(
        timestamp: Long = System.currentTimeMillis(),
        city: String = AstroCalculator.DEFAULT_LOCATION_NAME,
        latitude: Double = AstroCalculator.DEFAULT_LATITUDE,
        longitude: Double = AstroCalculator.DEFAULT_LONGITUDE
    ): CelestialSnapshot = AstroCalculator.calculateSnapshot(timestamp, city, latitude, longitude)

    fun calculateMoonPhase(timestamp: Long): Triple<MoonPhaseType, Int, Double> {
        val data = AstroCalculator.calculateMoonPhase(timestamp)
        return Triple(data.phaseType, data.illuminationPercent, data.ageDays)
    }

    fun calculateMoonSign(timestamp: Long): Pair<ZodiacSign, Int> {
        return AstroCalculator.calculateMoonSign(timestamp)
    }

    fun calculateSunSign(calendar: Calendar): Pair<ZodiacSign, Int> {
        val data = AstroCalculator.calculateSunSign(calendar)
        return Pair(data.sign, data.degree)
    }

    fun calculateSunTimes(
        now: Calendar,
        latitude: Double,
        longitude: Double
    ): Triple<Calendar, Calendar, Calendar> {
        val solarTimes = AstroCalculator.calculateSunTimes(now, latitude, longitude)
        return Triple(solarTimes.sunrise, solarTimes.sunset, solarTimes.nextSunrise)
    }

    fun calculatePlanetaryHour(
        now: Calendar,
        sunrise: Calendar,
        sunset: Calendar,
        nextSunrise: Calendar
    ): PlanetaryHourInfo {
        return AstroCalculator.calculatePlanetaryHour(now, sunrise, sunset, nextSunrise)
    }

    fun calculateTattwa(now: Calendar, sunrise: Calendar): TattwaInfo {
        return AstroCalculator.calculateTattwa(now, sunrise)
    }
}
