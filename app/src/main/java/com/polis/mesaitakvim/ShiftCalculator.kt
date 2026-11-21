package com.polis.mesaitakvim

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * "Çakma 12/36" döngüsü (9 gün):
 *  0,1,2 -> gündüz (3 gün üst üste)
 *  3,5,7 -> gece (gün aşırı)
 *  4,6,8 -> gece çıkışı / dinlenme
 * Örn: 20-21-22 Kasım gündüz, 23-25-27 Kasım gece, 29 Kasım yeni döngü.
 */
object ShiftCalculator {
    const val CYCLE_LENGTH = 9

    fun baseShift(date: LocalDate, cycleStart: LocalDate): ShiftType {
        val diff = ChronoUnit.DAYS.between(cycleStart, date)
        return when (Math.floorMod(diff, CYCLE_LENGTH.toLong()).toInt()) {
            0, 1, 2 -> ShiftType.DAY
            3, 5, 7 -> ShiftType.NIGHT
            else -> ShiftType.REST
        }
    }

    fun shiftFor(
        date: LocalDate,
        settings: AppSettings,
        overrides: Map<LocalDate, ShiftType>
    ): ShiftType = overrides[date] ?: baseShift(date, settings.cycleStart)

    fun hoursText(type: ShiftType, s: AppSettings): String = when (type) {
        ShiftType.DAY -> "${fmt(s.dayStart)} – ${fmt(s.dayEnd)}"
        ShiftType.NIGHT -> "${fmt(s.nightStart)} – ${fmt(s.nightEnd)}"
        else -> ""
    }

    /** Vardiya süresi (dakika) */
    fun durationMinutes(type: ShiftType, s: AppSettings): Long = when (type) {
        ShiftType.DAY -> span(s.dayStart, s.dayEnd)
        ShiftType.NIGHT -> span(s.nightStart, s.nightEnd)
        else -> 0
    }

    /** Mesainin başlangıç zamanı */
    fun startOf(date: LocalDate, type: ShiftType, s: AppSettings): LocalDateTime? = when (type) {
        ShiftType.DAY -> date.atTime(s.dayStart)
        ShiftType.NIGHT -> date.atTime(s.nightStart)
        else -> null
    }

    data class MonthSummary(val dayCount: Int, val nightCount: Int, val leaveCount: Int, val totalMinutes: Long)

    fun monthSummary(month: YearMonth, s: AppSettings, overrides: Map<LocalDate, ShiftType>): MonthSummary {
        var d = 0
        var n = 0
        var l = 0
        var minutes = 0L
        for (day in 1..month.lengthOfMonth()) {
            val type = shiftFor(month.atDay(day), s, overrides)
            when (type) {
                ShiftType.DAY -> d++
                ShiftType.NIGHT -> n++
                ShiftType.LEAVE -> l++
                ShiftType.REST -> {}
            }
            minutes += durationMinutes(type, s)
        }
        return MonthSummary(d, n, l, minutes)
    }

    private fun span(start: LocalTime, end: LocalTime): Long {
        val m = Duration.between(start, end).toMinutes()
        return if (m <= 0) m + 24 * 60 else m
    }

    fun fmt(t: LocalTime): String = "%02d:%02d".format(t.hour, t.minute)
}
