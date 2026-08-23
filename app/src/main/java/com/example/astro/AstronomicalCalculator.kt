package com.example.astro

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * AstronomicalCalculator is a dedicated utility object providing comprehensive mathematical algorithms
 * and helper methods for celestial calculations:
 *
 * 1. Solar & Lunar Position Math (Julian Dates, Ecliptic Longitudes, Declination, Right Ascension, Solar Zenith)
 * 2. Moon Phase & Illumination (Synodic cycle progression, Phase Type, Illumination %, Moon Sign & Degree)
 * 3. Solar Ephemeris & Geolocation (Sunrise, Sunset, Solar Noon, Twilight based on Latitude & Longitude)
 * 4. Planetary Hours (Chaldean Order day & night proportional hours anchored to local Sunrise & Sunset)
 * 5. Tropical Zodiac Sun Sign calculations (Zodiac Sign, Degree, Modality, Element)
 */
object AstronomicalCalculator {

    /** Default reference coordinates: Mount Shasta, CA */
    const val DEFAULT_LATITUDE = 41.3099
    const val DEFAULT_LONGITUDE = -122.3106
    const val DEFAULT_LOCATION_NAME = "Mount Shasta, CA"

    /** Reference epoch: Known New Moon on Jan 6, 2000 at 18:14 UTC */
    const val EPOCH_NEW_MOON_MS = 947182440000L
    /** Mean synodic month period in days (29d 12h 44m 2.8s) */
    const val SYNODIC_MONTH_DAYS = 29.530588853
    /** Milliseconds in a standard 24-hour day */
    const val MS_PER_DAY = 86400000.0
    /** J2000.0 epoch in Julian Days */
    const val J2000_EPOCH_JD = 2451545.0
    /** Standard civil atmospheric refraction zenith angle in degrees */
    const val OFFICIAL_ZENITH_DEGREES = 90.8333

    /**
     * Classical Chaldean sequence of planets in descending planetary sphere order:
     * Saturn (♄) -> Jupiter (♃) -> Mars (♂) -> Sun (☉) -> Venus (♀) -> Mercury (☿) -> Moon (☽)
     */
    val chaldeanOrder: List<Planet> = listOf(
        Planet.SATURN,
        Planet.JUPITER,
        Planet.MARS,
        Planet.SUN,
        Planet.VENUS,
        Planet.MERCURY,
        Planet.MOON
    )

    /**
     * Mapping of standard Gregorian day of the week to the governing planetary regent.
     */
    val dayRulers: Map<Int, Planet> = mapOf(
        Calendar.SUNDAY to Planet.SUN,
        Calendar.MONDAY to Planet.MOON,
        Calendar.TUESDAY to Planet.MARS,
        Calendar.WEDNESDAY to Planet.MERCURY,
        Calendar.THURSDAY to Planet.JUPITER,
        Calendar.FRIDAY to Planet.VENUS,
        Calendar.SATURDAY to Planet.SATURN
    )

    // ============================================================================================
    // 1. SOLAR & LUNAR POSITION MATHEMATICAL HELPERS
    // ============================================================================================

    /**
     * Converts a Unix timestamp in milliseconds to a Julian Day (JD) number.
     */
    fun toJulianDay(timestampMs: Long): Double {
        return (timestampMs / MS_PER_DAY) + 2440587.5
    }

    /**
     * Calculates the number of days elapsed since the J2000.0 epoch (Jan 1, 2000 12:00 UTC).
     */
    fun daysSinceJ2000(timestampMs: Long): Double {
        return toJulianDay(timestampMs) - J2000_EPOCH_JD
    }

    /**
     * Normalizes an angle in degrees into the [0.0, 360.0) range.
     */
    fun normalizeDegrees(degrees: Double): Double {
        var normalized = degrees % 360.0
        if (normalized < 0) normalized += 360.0
        return normalized
    }

    /**
     * Calculates the Sun's Mean Anomaly in degrees for a given day of the year estimate.
     */
    fun calculateSunMeanAnomaly(dayOfYearEstimate: Double): Double {
        return normalizeDegrees((0.9856 * dayOfYearEstimate) - 3.289)
    }

    /**
     * Calculates the Sun's True Tropical Ecliptic Longitude (λ) in degrees.
     */
    fun calculateSunEclipticLongitude(meanAnomalyDeg: Double): Double {
        val mRad = Math.toRadians(meanAnomalyDeg)
        val l = meanAnomalyDeg + (1.916 * sin(mRad)) + (0.020 * sin(2.0 * mRad)) + 282.634
        return normalizeDegrees(l)
    }

    /**
     * Calculates the Sun's Right Ascension (RA) in hours (0..24).
     */
    fun calculateSunRightAscensionHours(eclipticLongitudeDeg: Double): Double {
        val lRad = Math.toRadians(eclipticLongitudeDeg)
        var raDeg = Math.toDegrees(atan2(0.91764 * sin(lRad), cos(lRad)))
        raDeg = normalizeDegrees(raDeg)
        return raDeg / 15.0
    }

    /**
     * Calculates the Sun's Declination (δ) in radians.
     */
    fun calculateSunDeclinationRad(eclipticLongitudeDeg: Double): Double {
        val sinDec = 0.39782 * sin(Math.toRadians(eclipticLongitudeDeg))
        return asin(sinDec)
    }

    /**
     * Calculates the Moon's approximate mean tropical ecliptic longitude in degrees.
     */
    fun calculateMoonEclipticLongitude(timestampMs: Long): Double {
        val d = daysSinceJ2000(timestampMs)
        val l = 218.316 + (13.176396 * d)
        return normalizeDegrees(l)
    }

    // ============================================================================================
    // 2. MOON PHASE CALCULATIONS
    // ============================================================================================

    /**
     * Computes the current Moon Phase, Age, Illumination Percentage, and Zodiac Sign for a given timestamp.
     *
     * @param timestamp Epoch timestamp in milliseconds (defaults to current system time).
     * @return [MoonPhaseData] detailing phase type, illumination %, age in days, phase angle, and moon sign.
     */
    fun calculateMoonPhase(timestamp: Long = System.currentTimeMillis()): MoonPhaseData {
        val diffDays = (timestamp - EPOCH_NEW_MOON_MS) / MS_PER_DAY
        var moonAge = diffDays % SYNODIC_MONTH_DAYS
        if (moonAge < 0) moonAge += SYNODIC_MONTH_DAYS

        // Phase angle in radians (0 to 2*PI) and degrees (0° to 360°)
        val phaseAngleRad = (moonAge / SYNODIC_MONTH_DAYS) * 2.0 * PI
        val phaseAngleDeg = Math.toDegrees(phaseAngleRad)

        // Geometric illumination fraction: (1 - cos(θ)) / 2
        val illuminationFraction = (1.0 - cos(phaseAngleRad)) / 2.0
        val illuminationPercent = (illuminationFraction * 100).roundToInt().coerceIn(0, 100)
        val isWaxing = moonAge < (SYNODIC_MONTH_DAYS / 2.0)

        val phaseType = when {
            moonAge < 1.84 -> MoonPhaseType.NEW_MOON
            moonAge < 5.53 -> MoonPhaseType.WAXING_CRESCENT
            moonAge < 9.22 -> MoonPhaseType.FIRST_QUARTER
            moonAge < 12.91 -> MoonPhaseType.WAXING_GIBBOUS
            moonAge < 16.61 -> MoonPhaseType.FULL_MOON
            moonAge < 20.30 -> MoonPhaseType.WANING_GIBBOUS
            moonAge < 23.99 -> MoonPhaseType.LAST_QUARTER
            moonAge < 27.68 -> MoonPhaseType.WANING_CRESCENT
            else -> MoonPhaseType.NEW_MOON
        }

        val (moonSign, moonDegree) = calculateMoonSign(timestamp)

        return MoonPhaseData(
            phaseType = phaseType,
            illuminationPercent = illuminationPercent,
            ageDays = moonAge,
            phaseAngleDegrees = phaseAngleDeg,
            isWaxing = isWaxing,
            glyph = phaseType.glyph,
            title = phaseType.title,
            description = phaseType.description,
            moonSign = moonSign,
            moonSignDegree = moonDegree
        )
    }

    /**
     * Calculates the Moon's Tropical Zodiac Sign and Degree (0°..29°).
     */
    fun calculateMoonSign(timestamp: Long = System.currentTimeMillis()): Pair<ZodiacSign, Int> {
        val longitude = calculateMoonEclipticLongitude(timestamp)
        val signIndex = (longitude / 30.0).toInt() % 12
        val degree = (longitude % 30.0).toInt()

        val signs = ZodiacSign.values()
        val moonSign = if (signIndex in signs.indices) signs[signIndex] else ZodiacSign.ARIES
        return Pair(moonSign, degree)
    }

    // ============================================================================================
    // 3. SUN SIGN (ZODIAC) CALCULATIONS
    // ============================================================================================

    /**
     * Computes the Sun's Tropical Zodiac Sign, degree, element, and modality for a given [Calendar].
     */
    fun calculateSunSign(calendar: Calendar): SunSignData {
        val month = calendar.get(Calendar.MONTH) + 1 // 1..12
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val (sign, startDay) = when (month) {
            1 -> if (day <= 19) Pair(ZodiacSign.CAPRICORN, 22) else Pair(ZodiacSign.AQUARIUS, 20)
            2 -> if (day <= 18) Pair(ZodiacSign.AQUARIUS, 20) else Pair(ZodiacSign.PISCES, 19)
            3 -> if (day <= 20) Pair(ZodiacSign.PISCES, 19) else Pair(ZodiacSign.ARIES, 21)
            4 -> if (day <= 19) Pair(ZodiacSign.ARIES, 21) else Pair(ZodiacSign.TAURUS, 20)
            5 -> if (day <= 20) Pair(ZodiacSign.TAURUS, 20) else Pair(ZodiacSign.GEMINI, 21)
            6 -> if (day <= 20) Pair(ZodiacSign.GEMINI, 21) else Pair(ZodiacSign.CANSER, 21)
            7 -> if (day <= 22) Pair(ZodiacSign.CANSER, 21) else Pair(ZodiacSign.LEO, 23)
            8 -> if (day <= 22) Pair(ZodiacSign.LEO, 23) else Pair(ZodiacSign.VIRGO, 23)
            9 -> if (day <= 22) Pair(ZodiacSign.VIRGO, 23) else Pair(ZodiacSign.LIBRA, 23)
            10 -> if (day <= 22) Pair(ZodiacSign.LIBRA, 23) else Pair(ZodiacSign.SCORPIO, 23)
            11 -> if (day <= 21) Pair(ZodiacSign.SCORPIO, 23) else Pair(ZodiacSign.SAGITTARIUS, 22)
            12 -> if (day <= 21) Pair(ZodiacSign.SAGITTARIUS, 22) else Pair(ZodiacSign.CAPRICORN, 22)
            else -> Pair(ZodiacSign.LEO, 1)
        }

        val degree = ((day - startDay + 30) % 30).coerceIn(0, 29)
        val signIndex = ZodiacSign.values().indexOf(sign)
        val exactLongitude = (signIndex * 30.0) + degree

        return SunSignData(
            sign = sign,
            degree = degree,
            exactEclipticLongitude = exactLongitude,
            element = sign.element,
            modality = sign.modality,
            keywords = sign.keywords,
            glyph = sign.glyph
        )
    }

    /**
     * Overload for [calculateSunSign] accepting a timestamp in milliseconds.
     */
    fun calculateSunSign(timestamp: Long = System.currentTimeMillis()): SunSignData {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return calculateSunSign(cal)
    }

    // ============================================================================================
    // 4. SOLAR EPHEMERIS TIMES (SUNRISE & SUNSET BASED ON GEOLOCATION)
    // ============================================================================================

    /**
     * Calculates local Sunrise, Sunset, Solar Noon, and Next Sunrise for a given date and geolocation.
     *
     * @param calendar Target date
     * @param latitude Observer latitude in decimal degrees
     * @param longitude Observer longitude in decimal degrees
     */
    fun calculateSunTimes(
        calendar: Calendar,
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE
    ): SolarTimes {
        val sunrise = calculateSolarCrossingTime(calendar, latitude, longitude, isSunrise = true)
        val sunset = calculateSolarCrossingTime(calendar, latitude, longitude, isSunrise = false)

        if (sunset.timeInMillis <= sunrise.timeInMillis) {
            sunset.timeInMillis = sunrise.timeInMillis + (12 * 3600 * 1000L)
        }

        val tomorrow = (calendar.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val nextSunrise = calculateSolarCrossingTime(tomorrow, latitude, longitude, isSunrise = true)

        val noonMs = (sunrise.timeInMillis + sunset.timeInMillis) / 2
        val solarNoon = (calendar.clone() as Calendar).apply { timeInMillis = noonMs }

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        return SolarTimes(
            sunrise = sunrise,
            sunset = sunset,
            solarNoon = solarNoon,
            nextSunrise = nextSunrise,
            sunriseFormatted = timeFormat.format(sunrise.time),
            sunsetFormatted = timeFormat.format(sunset.time)
        )
    }

    private fun calculateSolarCrossingTime(
        cal: Calendar,
        lat: Double,
        lon: Double,
        isSunrise: Boolean
    ): Calendar {
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val zenith = OFFICIAL_ZENITH_DEGREES

        val lngHour = lon / 15.0
        val t = if (isSunrise) {
            dayOfYear + ((6.0 - lngHour) / 24.0)
        } else {
            dayOfYear + ((18.0 - lngHour) / 24.0)
        }

        val meanAnomaly = calculateSunMeanAnomaly(t)
        val eclipticLongitude = calculateSunEclipticLongitude(meanAnomaly)
        val rightAscensionHours = calculateSunRightAscensionHours(eclipticLongitude)
        val declinationRad = calculateSunDeclinationRad(eclipticLongitude)

        val cosDec = cos(declinationRad)
        val sinDec = sin(declinationRad)

        val cosH = (cos(Math.toRadians(zenith)) - (sinDec * sin(Math.toRadians(lat)))) /
                (cosDec * cos(Math.toRadians(lat)))

        val hDeg = Math.toDegrees(acos(cosH.coerceIn(-1.0, 1.0)))
        val hHours = hDeg / 15.0

        val localMeanTime = if (isSunrise) {
            (24.0 - hHours + rightAscensionHours - (0.06571 * t) - 6.622 + 48.0) % 24.0
        } else {
            (hHours + rightAscensionHours - (0.06571 * t) - 6.622 + 48.0) % 24.0
        }

        val timeZone = cal.timeZone
        val offsetHours = timeZone.getOffset(cal.timeInMillis) / 3600000.0
        val ut = (localMeanTime - lngHour + 48.0) % 24.0
        var localHours = (ut + offsetHours + 48.0) % 24.0

        if (isSunrise && localHours >= 12.0) {
            localHours = (localHours + 12.0) % 24.0
        } else if (!isSunrise && localHours < 12.0) {
            localHours += 12.0
        }

        val hour = localHours.toInt().coerceIn(0, 23)
        val minute = ((localHours - hour) * 60.0).roundToInt().coerceIn(0, 59)

        return (cal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    // ============================================================================================
    // 5. PLANETARY HOURS (CHALDEAN SYSTEM ANCHORED TO SUNRISE / SUNSET)
    // ============================================================================================

    /**
     * Calculates the currently active Planetary Hour based on Sunrise and Sunset times.
     *
     * @param now Current date and time
     * @param sunrise Today's local sunrise
     * @param sunset Today's local sunset
     * @param nextSunrise Next day's local sunrise
     */
    fun calculatePlanetaryHour(
        now: Calendar,
        sunrise: Calendar,
        sunset: Calendar,
        nextSunrise: Calendar
    ): PlanetaryHourInfo {
        val nowMs = now.timeInMillis
        val sunriseMs = sunrise.timeInMillis
        val sunsetMs = sunset.timeInMillis
        val nextSunriseMs = nextSunrise.timeInMillis

        val isDay = nowMs in sunriseMs until sunsetMs

        val dayOfWeekCal = if (nowMs < sunriseMs) {
            (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        } else {
            now
        }
        val dayOfWeek = dayOfWeekCal.get(Calendar.DAY_OF_WEEK)
        val dayRuler = dayRulers[dayOfWeek] ?: Planet.SUN
        val dayRulerIndexInChaldean = chaldeanOrder.indexOf(dayRuler)

        val hourLengthMs: Long
        val hourStartMs: Long
        val hourEndMs: Long
        val hourIndex: Int

        if (isDay) {
            val totalDayDurationMs = (sunsetMs - sunriseMs).coerceAtLeast(1)
            hourLengthMs = totalDayDurationMs / 12
            val elapsedMs = (nowMs - sunriseMs).coerceAtLeast(0)
            hourIndex = (elapsedMs / hourLengthMs).toInt().coerceIn(0, 11)
            hourStartMs = sunriseMs + (hourIndex * hourLengthMs)
            hourEndMs = hourStartMs + hourLengthMs
        } else {
            val totalNightDurationMs = (nextSunriseMs - sunsetMs).coerceAtLeast(1)
            hourLengthMs = totalNightDurationMs / 12

            if (nowMs >= sunsetMs) {
                val elapsedMs = nowMs - sunsetMs
                hourIndex = (elapsedMs / hourLengthMs).toInt().coerceIn(0, 11)
                hourStartMs = sunsetMs + (hourIndex * hourLengthMs)
                hourEndMs = hourStartMs + hourLengthMs
            } else {
                val prevSunset = (sunset.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis
                val nightDuration = (sunriseMs - prevSunset).coerceAtLeast(1)
                val prevHourLength = nightDuration / 12
                val elapsedMs = (nowMs - prevSunset).coerceAtLeast(0)
                hourIndex = (elapsedMs / prevHourLength).toInt().coerceIn(0, 11)
                hourStartMs = prevSunset + (hourIndex * prevHourLength)
                hourEndMs = hourStartMs + prevHourLength
            }
        }

        val totalOffsetFromSunrise = if (isDay) hourIndex else 12 + hourIndex
        val planetIndex = (dayRulerIndexInChaldean + totalOffsetFromSunrise) % chaldeanOrder.size
        val rulingPlanet = chaldeanOrder[planetIndex]

        val remainingMs = (hourEndMs - nowMs).coerceAtLeast(0)
        val remainingMinutes = (remainingMs / 60000).toInt()

        val progressFraction = if (hourLengthMs > 0) {
            ((nowMs - hourStartMs).toFloat() / hourLengthMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val startFormatted = timeFormat.format(Date(hourStartMs))
        val endFormatted = timeFormat.format(Date(hourEndMs))

        return PlanetaryHourInfo(
            hourNumber = hourIndex + 1,
            isDayHour = isDay,
            rulingPlanet = rulingPlanet,
            dayRulerPlanet = dayRuler,
            startTimeFormatted = startFormatted,
            endTimeFormatted = endFormatted,
            remainingMinutes = remainingMinutes,
            progressFraction = progressFraction
        )
    }

    /**
     * Calculates the active Planetary Hour directly from date and geolocation.
     */
    fun calculatePlanetaryHour(
        calendar: Calendar,
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE
    ): PlanetaryHourInfo {
        val solarTimes = calculateSunTimes(calendar, latitude, longitude)
        return calculatePlanetaryHour(calendar, solarTimes.sunrise, solarTimes.sunset, solarTimes.nextSunrise)
    }

    /**
     * Computes the complete 24 planetary hours schedule (12 day + 12 night) for a specific date and coordinates.
     */
    fun calculatePlanetaryHoursForDay(
        calendar: Calendar,
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE
    ): List<PlanetaryHourSlot> {
        val solarTimes = calculateSunTimes(calendar, latitude, longitude)
        val sunriseMs = solarTimes.sunrise.timeInMillis
        val sunsetMs = solarTimes.sunset.timeInMillis
        val nextSunriseMs = solarTimes.nextSunrise.timeInMillis

        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayRuler = dayRulers[dayOfWeek] ?: Planet.SUN
        val dayRulerIndex = chaldeanOrder.indexOf(dayRuler)

        val dayHourLengthMs = (sunsetMs - sunriseMs) / 12
        val nightHourLengthMs = (nextSunriseMs - sunsetMs) / 12

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val nowMs = calendar.timeInMillis
        val slots = mutableListOf<PlanetaryHourSlot>()

        // 12 Diurnal Hours
        for (i in 0 until 12) {
            val startMs = sunriseMs + (i * dayHourLengthMs)
            val endMs = startMs + dayHourLengthMs
            val planet = chaldeanOrder[(dayRulerIndex + i) % chaldeanOrder.size]

            val startCal = Calendar.getInstance().apply { timeInMillis = startMs }
            val endCal = Calendar.getInstance().apply { timeInMillis = endMs }
            val isCurrent = nowMs in startMs until endMs

            slots.add(
                PlanetaryHourSlot(
                    hourNumber = i + 1,
                    isDayHour = true,
                    rulingPlanet = planet,
                    startTime = startCal,
                    endTime = endCal,
                    startTimeFormatted = timeFormat.format(startCal.time),
                    endTimeFormatted = timeFormat.format(endCal.time),
                    isCurrent = isCurrent
                )
            )
        }

        // 12 Nocturnal Hours
        for (i in 0 until 12) {
            val startMs = sunsetMs + (i * nightHourLengthMs)
            val endMs = startMs + nightHourLengthMs
            val planet = chaldeanOrder[(dayRulerIndex + 12 + i) % chaldeanOrder.size]

            val startCal = Calendar.getInstance().apply { timeInMillis = startMs }
            val endCal = Calendar.getInstance().apply { timeInMillis = endMs }
            val isCurrent = nowMs in startMs until endMs

            slots.add(
                PlanetaryHourSlot(
                    hourNumber = i + 1,
                    isDayHour = false,
                    rulingPlanet = planet,
                    startTime = startCal,
                    endTime = endCal,
                    startTimeFormatted = timeFormat.format(startCal.time),
                    endTimeFormatted = timeFormat.format(endCal.time),
                    isCurrent = isCurrent
                )
            )
        }

        return slots
    }
}
