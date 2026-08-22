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
 * AstroCalculator is a comprehensive astronomical and astrological calculation utility.
 *
 * It provides precise mathematical algorithms to calculate:
 * 1. Moon Phases (synodic month progression, illumination percentage, phase type, moon sign)
 * 2. Planetary Hours (classical Chaldean order day and night hour rulers, proportional diurnal/nocturnal duration)
 * 3. Sun Signs (tropical zodiac ecliptic longitude, degrees, elements, modalities)
 * 4. Solar Times (sunrise, sunset, solar noon, dawn/dusk based on geographic coordinates)
 * 5. Tattwa Cycles (classical Vedic 24-minute elemental ether-to-earth rhythms)
 */
object AstroCalculator {

    /** Default location coordinates (Mount Shasta, CA) */
    const val DEFAULT_LATITUDE = 41.3099
    const val DEFAULT_LONGITUDE = -122.3106
    const val DEFAULT_LOCATION_NAME = "Mount Shasta, CA"

    /** Reference epoch: Known New Moon on Jan 6, 2000 at 18:14 UTC */
    private const val EPOCH_NEW_MOON_MS = 947182440000L
    /** Mean synodic month period in days */
    private const val SYNODIC_MONTH_DAYS = 29.530588853
    /** Milliseconds in a standard 24-hour day */
    private const val MS_PER_DAY = 86400000.0

    /**
     * Chaldean sequence of planetary rulers in descending order of classical cosmic sphere distance.
     * Saturn -> Jupiter -> Mars -> Sun -> Venus -> Mercury -> Moon
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
     * Day of the week to ruling planet mapping (Sunday = Sun, Monday = Moon, etc.)
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
    // MOON PHASE CALCULATIONS
    // ============================================================================================

    /**
     * Calculates the Moon Phase, Illumination, and Moon Age for a given epoch timestamp.
     *
     * @param timestamp Epoch time in milliseconds (defaults to current time)
     * @return [MoonPhaseData] detailing phase type, illumination %, age, phase angle, and glyph.
     */
    fun calculateMoonPhase(timestamp: Long = System.currentTimeMillis()): MoonPhaseData {
        val diffDays = (timestamp - EPOCH_NEW_MOON_MS) / MS_PER_DAY
        var moonAge = diffDays % SYNODIC_MONTH_DAYS
        if (moonAge < 0) moonAge += SYNODIC_MONTH_DAYS

        // Phase angle in radians (0 to 2*PI) and degrees (0° to 360°)
        val phaseAngleRad = (moonAge / SYNODIC_MONTH_DAYS) * 2.0 * PI
        val phaseAngleDeg = Math.toDegrees(phaseAngleRad)

        // Geometric illumination fraction: (1 - cos(angle)) / 2
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
     * Overload for [calculateMoonPhase] accepting a [Date].
     */
    fun calculateMoonPhase(date: Date): MoonPhaseData = calculateMoonPhase(date.time)

    /**
     * Overload for [calculateMoonPhase] accepting a [Calendar].
     */
    fun calculateMoonPhase(calendar: Calendar): MoonPhaseData = calculateMoonPhase(calendar.timeInMillis)

    /**
     * Calculates the Moon's Tropical Zodiac Sign and Degree (0°-29°) based on approximate lunar ecliptic longitude.
     */
    fun calculateMoonSign(timestamp: Long = System.currentTimeMillis()): Pair<ZodiacSign, Int> {
        val jd = (timestamp / MS_PER_DAY) + 2440587.5
        val d = jd - 2451545.0 // Days since J2000.0 epoch

        // Approximate mean longitude of the Moon in tropical ecliptic coordinates
        var l = 218.316 + 13.176396 * d
        l %= 360.0
        if (l < 0) l += 360.0

        val signIndex = (l / 30.0).toInt() % 12
        val degree = (l % 30.0).toInt()

        val signs = ZodiacSign.values()
        val moonSign = if (signIndex in signs.indices) signs[signIndex] else ZodiacSign.ARIES
        return Pair(moonSign, degree)
    }

    // ============================================================================================
    // SUN SIGN & SOLAR POSITION CALCULATIONS
    // ============================================================================================

    /**
     * Calculates the Tropical Sun Sign and Zodiac degree for a given [Calendar].
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
     * Overload for [calculateSunSign] accepting a [Date].
     */
    fun calculateSunSign(date: Date): SunSignData {
        val cal = Calendar.getInstance().apply { time = date }
        return calculateSunSign(cal)
    }

    /**
     * Overload for [calculateSunSign] accepting a timestamp in milliseconds.
     */
    fun calculateSunSign(timestamp: Long = System.currentTimeMillis()): SunSignData {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return calculateSunSign(cal)
    }

    // ============================================================================================
    // SOLAR TIMES (SUNRISE, SUNSET, SOLAR NOON)
    // ============================================================================================

    /**
     * Calculates Sunrise, Sunset, Solar Noon, and Next Sunrise for a given date and geolocation coordinates.
     * Uses official solar zenith (90°50' with atmospheric refraction).
     *
     * @param calendar Target date
     * @param latitude Geographic latitude in decimal degrees (-90.0 to 90.0)
     * @param longitude Geographic longitude in decimal degrees (-180.0 to 180.0)
     */
    fun calculateSunTimes(
        calendar: Calendar,
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE
    ): SolarTimes {
        val sunrise = getSolarTimeForDate(calendar, latitude, longitude, isSunrise = true)
        val sunset = getSolarTimeForDate(calendar, latitude, longitude, isSunrise = false)

        val tomorrow = (calendar.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val nextSunrise = getSolarTimeForDate(tomorrow, latitude, longitude, isSunrise = true)

        // Solar noon is halfway between sunrise and sunset
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

    /**
     * Overload for [calculateSunTimes] accepting a [Date].
     */
    fun calculateSunTimes(
        date: Date,
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE
    ): SolarTimes {
        val cal = Calendar.getInstance().apply { time = date }
        return calculateSunTimes(cal, latitude, longitude)
    }

    private fun getSolarTimeForDate(
        cal: Calendar,
        lat: Double,
        lon: Double,
        isSunrise: Boolean
    ): Calendar {
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val zenith = 90.8333 // Official civil zenith including atmospheric refraction

        // Longitude to hour value and estimate of the time
        val lngHour = lon / 15.0
        val t = if (isSunrise) {
            dayOfYear + ((6.0 - lngHour) / 24.0)
        } else {
            dayOfYear + ((18.0 - lngHour) / 24.0)
        }

        // Sun's mean anomaly
        val m = (0.9856 * t) - 3.289
        // Sun's true longitude
        var l = m + (1.916 * sin(Math.toRadians(m))) + (0.020 * sin(Math.toRadians(2 * m))) + 282.634
        l %= 360.0
        if (l < 0) l += 360.0

        // Right ascension
        var ra = Math.toDegrees(atan2(0.91764 * sin(Math.toRadians(l)), cos(Math.toRadians(l))))
        ra %= 360.0
        if (ra < 0) ra += 360.0

        // Quadrant adjustment
        val lQuadrant = floor(l / 90.0) * 90.0
        val raQuadrant = floor(ra / 90.0) * 90.0
        ra += (lQuadrant - raQuadrant)
        ra /= 15.0

        // Sun's declination
        val sinDec = 0.39782 * sin(Math.toRadians(l))
        val cosDec = cos(asin(sinDec))

        // Local hour angle
        val cosH = (cos(Math.toRadians(zenith)) - (sinDec * sin(Math.toRadians(lat)))) /
                (cosDec * cos(Math.toRadians(lat)))

        val h = if (cosH > 1.0) {
            0.0 // Polar night
        } else if (cosH < -1.0) {
            180.0 // Midnight sun
        } else {
            if (isSunrise) 360.0 - Math.toDegrees(acos(cosH)) else Math.toDegrees(acos(cosH))
        }
        val hHours = h / 15.0

        // Local mean time
        val localMeanTime = hHours + ra - (0.06571 * t) - 6.622
        // Universal Time
        var ut = localMeanTime - lngHour
        ut %= 24.0
        if (ut < 0) ut += 24.0

        // Convert UT to local timezone offset
        val timeZone = cal.timeZone
        val offsetHours = timeZone.getOffset(cal.timeInMillis) / 3600000.0
        var localHours = ut + offsetHours
        localHours %= 24.0
        if (localHours < 0) localHours += 24.0

        val hour = localHours.toInt()
        val minute = ((localHours - hour) * 60.0).roundToInt().coerceIn(0, 59)

        return (cal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    // ============================================================================================
    // PLANETARY HOURS CALCULATIONS (CHALDEAN SYSTEM)
    // ============================================================================================

    /**
     * Calculates the active Planetary Hour for a given date/time and geolocation.
     *
     * In the classical Chaldean system:
     * - The astrological day begins at local sunrise.
     * - The period from sunrise to sunset is divided into 12 equal diurnal hours.
     * - The period from sunset to next sunrise is divided into 12 equal nocturnal hours.
     * - The first hour of the day is ruled by the day's regent planet (Sunday=Sun, Monday=Moon, etc.).
     * - Subsequent hours cycle continuously through the descending Chaldean order:
     *   Saturn -> Jupiter -> Mars -> Sun -> Venus -> Mercury -> Moon -> Saturn...
     *
     * @param date Date and time of observation
     * @param latitude Observer latitude
     * @param longitude Observer longitude
     * @return [PlanetaryHourInfo] containing hour number, ruling planet, day ruler, and progress.
     */
    fun calculatePlanetaryHour(
        date: Date,
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE
    ): PlanetaryHourInfo {
        val cal = Calendar.getInstance().apply { time = date }
        return calculatePlanetaryHour(cal, latitude, longitude)
    }

    /**
     * Overload for [calculatePlanetaryHour] accepting a [Calendar].
     */
    fun calculatePlanetaryHour(
        calendar: Calendar,
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE
    ): PlanetaryHourInfo {
        val solarTimes = calculateSunTimes(calendar, latitude, longitude)
        return calculatePlanetaryHour(
            calendar,
            solarTimes.sunrise,
            solarTimes.sunset,
            solarTimes.nextSunrise
        )
    }

    /**
     * Calculates the planetary hour with pre-calculated sunrise/sunset anchors.
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

        // The astrological day begins at sunrise. If before today's sunrise, belongs to previous day's ruler.
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
        val hourIndex: Int // 0..11

        if (isDay) {
            val totalDayDurationMs = (sunsetMs - sunriseMs).coerceAtLeast(1)
            hourLengthMs = totalDayDurationMs / 12
            val elapsedMs = (nowMs - sunriseMs).coerceAtLeast(0)
            hourIndex = (elapsedMs / hourLengthMs).toInt().coerceIn(0, 11)
            hourStartMs = sunriseMs + (hourIndex * hourLengthMs)
            hourEndMs = hourStartMs + hourLengthMs
        } else {
            // Night hours
            val totalNightDurationMs = (nextSunriseMs - sunsetMs).coerceAtLeast(1)
            hourLengthMs = totalNightDurationMs / 12

            if (nowMs >= sunsetMs) {
                val elapsedMs = nowMs - sunsetMs
                hourIndex = (elapsedMs / hourLengthMs).toInt().coerceIn(0, 11)
                hourStartMs = sunsetMs + (hourIndex * hourLengthMs)
                hourEndMs = hourStartMs + hourLengthMs
            } else {
                // Before sunrise: belongs to the second half of the previous night's 12 hours
                val prevSunset = (sunset.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis
                val nightDuration = (sunriseMs - prevSunset).coerceAtLeast(1)
                val prevHourLength = nightDuration / 12
                val elapsedMs = (nowMs - prevSunset).coerceAtLeast(0)
                hourIndex = (elapsedMs / prevHourLength).toInt().coerceIn(0, 11)
                hourStartMs = prevSunset + (hourIndex * prevHourLength)
                hourEndMs = hourStartMs + prevHourLength
            }
        }

        // Planet ruling this hour: (DayRulerIndex + totalHoursSinceSunrise) % 7
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
     * Calculates the entire 24-hour planetary hour schedule (12 day hours + 12 night hours) for a given date.
     * Useful for scheduling meditation sessions, reflections, or viewing the daily celestial cycle.
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

        // 12 Day Hours
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

        // 12 Night Hours
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

    /**
     * Overload for [calculatePlanetaryHoursForDay] accepting a [Date].
     */
    fun calculatePlanetaryHoursForDay(
        date: Date,
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE
    ): List<PlanetaryHourSlot> {
        val cal = Calendar.getInstance().apply { time = date }
        return calculatePlanetaryHoursForDay(cal, latitude, longitude)
    }

    // ============================================================================================
    // TATTWAS (VEDIC 24-MINUTE ELEMENTAL CYCLES)
    // ============================================================================================

    /**
     * Tattwas: 5 elements cycling in 24-minute blocks every 2 hours from local sunrise.
     * Order: Akasha (0-24m), Vayu (24-48m), Tejas (48-72m), Apas (72-96m), Prithvi (96-120m).
     */
    fun calculateTattwa(now: Calendar, sunrise: Calendar): TattwaInfo {
        val nowMs = now.timeInMillis
        val sunriseMs = sunrise.timeInMillis

        var elapsedMs = (nowMs - sunriseMs)
        if (elapsedMs < 0) elapsedMs += 86400000L // previous day sunrise reference

        val elapsedMinutes = (elapsedMs / 60000.0)
        val cyclePositionMinutes = elapsedMinutes % 120.0 // 2-hour cycle = 120 mins

        val tattwas = listOf(
            Tattwa.AKASHA,
            Tattwa.VAYU,
            Tattwa.TEJAS,
            Tattwa.APAS,
            Tattwa.PRITHVI
        )

        val mainTattwaIndex = (cyclePositionMinutes / 24.0).toInt().coerceIn(0, 4)
        val currentTattwa = tattwas[mainTattwaIndex]

        val startMinute = mainTattwaIndex * 24
        val endMinute = startMinute + 24
        val minuteInCurrentTattwa = cyclePositionMinutes - startMinute
        val remainingMinutes = (24.0 - minuteInCurrentTattwa).roundToInt().coerceAtLeast(1)

        // Sub-tattwa: 24 mins / 5 = 4.8 mins each
        val subTattwaIndex = (minuteInCurrentTattwa / 4.8).toInt().coerceIn(0, 4)
        val subTattwa = tattwas[subTattwaIndex]

        val progressFraction = (minuteInCurrentTattwa / 24.0).toFloat().coerceIn(0f, 1f)

        return TattwaInfo(
            currentTattwa = currentTattwa,
            subTattwa = subTattwa,
            startMinute = startMinute,
            endMinute = endMinute,
            remainingMinutes = remainingMinutes,
            progressFraction = progressFraction
        )
    }

    /**
     * Overload for [calculateTattwa] calculating sunrise automatically from geolocation.
     */
    fun calculateTattwa(
        date: Date,
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE
    ): TattwaInfo {
        val cal = Calendar.getInstance().apply { time = date }
        val solarTimes = calculateSunTimes(cal, latitude, longitude)
        return calculateTattwa(cal, solarTimes.sunrise)
    }

    // ============================================================================================
    // COMPREHENSIVE SNAPSHOT & CELESTIAL DATA AGGREGATORS
    // ============================================================================================

    /**
     * Computes the complete [CelestialCalculationResult] encompassing Moon Phase, Sun Sign,
     * Planetary Hour, Full Daily Planetary Schedule, Solar Times, and Tattwas for any given date and location.
     */
    fun calculateCelestialData(
        date: Date = Date(),
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE,
        locationName: String = DEFAULT_LOCATION_NAME
    ): CelestialCalculationResult {
        val cal = Calendar.getInstance().apply { time = date }
        val timestamp = date.time

        val moonData = calculateMoonPhase(timestamp)
        val sunData = calculateSunSign(cal)
        val solarTimes = calculateSunTimes(cal, latitude, longitude)
        val planetaryHourInfo = calculatePlanetaryHour(cal, solarTimes.sunrise, solarTimes.sunset, solarTimes.nextSunrise)
        val schedule = calculatePlanetaryHoursForDay(cal, latitude, longitude)
        val tattwa = calculateTattwa(cal, solarTimes.sunrise)

        val snapshot = CelestialSnapshot(
            timestamp = timestamp,
            moonPhase = moonData.phaseType,
            moonIllumination = moonData.illuminationPercent,
            moonAgeDays = moonData.ageDays,
            moonSign = moonData.moonSign ?: ZodiacSign.ARIES,
            moonSignDegree = moonData.moonSignDegree ?: 0,
            sunSign = sunData.sign,
            sunSignDegree = sunData.degree,
            planetaryHour = planetaryHourInfo,
            tattwaInfo = tattwa,
            city = locationName,
            latitude = latitude,
            longitude = longitude,
            sunriseFormatted = solarTimes.sunriseFormatted,
            sunsetFormatted = solarTimes.sunsetFormatted
        )

        return CelestialCalculationResult(
            timestamp = timestamp,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            moonPhase = moonData,
            sunSign = sunData,
            currentPlanetaryHour = planetaryHourInfo,
            planetaryHourSchedule = schedule,
            solarTimes = solarTimes,
            tattwaInfo = tattwa,
            snapshot = snapshot
        )
    }

    /**
     * Computes the [CelestialSnapshot] for backwards compatibility and integration with ViewModels.
     */
    fun calculateSnapshot(
        timestamp: Long = System.currentTimeMillis(),
        city: String = DEFAULT_LOCATION_NAME,
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE
    ): CelestialSnapshot {
        val result = calculateCelestialData(
            date = Date(timestamp),
            latitude = latitude,
            longitude = longitude,
            locationName = city
        )
        return result.snapshot
    }
}
