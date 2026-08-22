package com.example

import com.example.astro.AstroCalculator
import com.example.astro.MoonPhaseType
import com.example.astro.Planet
import com.example.astro.ZodiacSign
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class AstroCalculatorTest {

    @Test
    fun testMoonPhaseCalculation() {
        // Test Moon Phase for known timestamp
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.AUGUST, 22, 12, 0, 0)
        }
        val moonData = AstroCalculator.calculateMoonPhase(cal)

        assertNotNull(moonData)
        assertTrue(moonData.illuminationPercent in 0..100)
        assertTrue(moonData.ageDays in 0.0..30.0)
        assertNotNull(moonData.glyph)
        assertNotNull(moonData.title)
        assertNotNull(moonData.moonSign)
        assertTrue(moonData.moonSignDegree in 0..29)
    }

    @Test
    fun testSunSignCalculation() {
        // August 22 is Leo (transitioning to Virgo on Aug 23)
        val leoCal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 22, 12, 0, 0)
        }
        val leoData = AstroCalculator.calculateSunSign(leoCal)
        assertEquals(ZodiacSign.LEO, leoData.sign)
        assertEquals("Fire", leoData.element)
        assertEquals("Fixed", leoData.modality)
        assertEquals("♌", leoData.glyph)

        // March 25 is Aries
        val ariesCal = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 25, 12, 0, 0)
        }
        val ariesData = AstroCalculator.calculateSunSign(ariesCal)
        assertEquals(ZodiacSign.ARIES, ariesData.sign)
        assertEquals("Fire", ariesData.element)
    }

    @Test
    fun testSunTimesAndPlanetaryHours() {
        val lat = 37.7749 // San Francisco
        val lon = -122.4194
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 22, 12, 0, 0)
        }

        val solarTimes = AstroCalculator.calculateSunTimes(cal, lat, lon)
        assertNotNull(solarTimes.sunriseFormatted)
        assertNotNull(solarTimes.sunsetFormatted)
        assertTrue(solarTimes.sunset.timeInMillis > solarTimes.sunrise.timeInMillis)

        // Planetary Hour at solar noon should be a day hour
        val planetaryHour = AstroCalculator.calculatePlanetaryHour(cal, lat, lon)
        assertTrue(planetaryHour.hourNumber in 1..12)
        assertTrue(planetaryHour.isDayHour)
        assertNotNull(planetaryHour.rulingPlanet)
        assertNotNull(planetaryHour.dayRulerPlanet)

        // Full day schedule should have 24 hours (12 day + 12 night)
        val schedule = AstroCalculator.calculatePlanetaryHoursForDay(cal, lat, lon)
        assertEquals(24, schedule.size)
        assertEquals(12, schedule.filter { it.isDayHour }.size)
        assertEquals(12, schedule.filter { !it.isDayHour }.size)
    }

    @Test
    fun testTattwaCalculation() {
        val lat = 37.7749
        val lon = -122.4194
        val now = Date()
        val tattwa = AstroCalculator.calculateTattwa(now, lat, lon)

        assertNotNull(tattwa.currentTattwa)
        assertNotNull(tattwa.subTattwa)
        assertTrue(tattwa.remainingMinutes in 1..24)
        assertTrue(tattwa.progressFraction in 0f..1f)
    }

    @Test
    fun testCompleteCelestialData() {
        val result = AstroCalculator.calculateCelestialData(
            date = Date(),
            latitude = 41.3099,
            longitude = -122.3106,
            locationName = "Mount Shasta, CA"
        )

        assertNotNull(result.moonPhase)
        assertNotNull(result.sunSign)
        assertNotNull(result.currentPlanetaryHour)
        assertEquals(24, result.planetaryHourSchedule.size)
        assertNotNull(result.solarTimes)
        assertNotNull(result.tattwaInfo)
        assertNotNull(result.snapshot)
        assertEquals("Mount Shasta, CA", result.locationName)
    }
}
