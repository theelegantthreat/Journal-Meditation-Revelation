package com.example

import com.example.astro.AstronomicalCalculator
import com.example.astro.ZodiacSign
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class AstronomicalCalculatorTest {

    @Test
    fun testSolarPositionMathHelpers() {
        val jd = AstronomicalCalculator.toJulianDay(AstronomicalCalculator.EPOCH_NEW_MOON_MS)
        assertTrue(jd > 2450000.0)

        val normalized = AstronomicalCalculator.normalizeDegrees(400.0)
        assertEquals(40.0, normalized, 0.001)

        val meanAnomaly = AstronomicalCalculator.calculateSunMeanAnomaly(100.0)
        assertTrue(meanAnomaly in 0.0..360.0)

        val eclipticLongitude = AstronomicalCalculator.calculateSunEclipticLongitude(meanAnomaly)
        assertTrue(eclipticLongitude in 0.0..360.0)

        val raHours = AstronomicalCalculator.calculateSunRightAscensionHours(eclipticLongitude)
        assertTrue(raHours in 0.0..24.0)

        val declination = AstronomicalCalculator.calculateSunDeclinationRad(eclipticLongitude)
        assertTrue(Math.abs(declination) <= Math.toRadians(23.5))
    }

    @Test
    fun testMoonPhaseAndLunarPosition() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.AUGUST, 23, 10, 0, 0)
        }
        val moonData = AstronomicalCalculator.calculateMoonPhase(cal.timeInMillis)

        assertNotNull(moonData.phaseType)
        assertTrue(moonData.illuminationPercent in 0..100)
        assertTrue(moonData.ageDays in 0.0..30.0)
        assertNotNull(moonData.glyph)
        assertNotNull(moonData.title)
        assertNotNull(moonData.moonSign)
        assertTrue(moonData.moonSignDegree in 0..29)
    }

    @Test
    fun testSunSignCalculations() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 23, 12, 0, 0)
        }
        val sunSignData = AstronomicalCalculator.calculateSunSign(cal)
        assertEquals(ZodiacSign.VIRGO, sunSignData.sign)
        assertEquals("Earth", sunSignData.element)
        assertEquals("Mutable", sunSignData.modality)
        assertEquals("♍", sunSignData.glyph)
    }

    @Test
    fun testPlanetaryHoursFromSunTimes() {
        val lat = 41.3099 // Mount Shasta
        val lon = -122.3106
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 23, 12, 0, 0)
        }

        val solarTimes = AstronomicalCalculator.calculateSunTimes(cal, lat, lon)
        assertNotNull(solarTimes.sunriseFormatted)
        assertNotNull(solarTimes.sunsetFormatted)
        assertTrue(solarTimes.sunset.timeInMillis > solarTimes.sunrise.timeInMillis)

        val currentHour = AstronomicalCalculator.calculatePlanetaryHour(cal, lat, lon)
        assertTrue(currentHour.hourNumber in 1..12)
        assertNotNull(currentHour.rulingPlanet)
        assertNotNull(currentHour.dayRulerPlanet)

        val schedule = AstronomicalCalculator.calculatePlanetaryHoursForDay(cal, lat, lon)
        assertEquals(24, schedule.size)
        assertEquals(12, schedule.filter { it.isDayHour }.size)
        assertEquals(12, schedule.filter { !it.isDayHour }.size)
    }
}
