package com.polis.mesaitakvim

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class ShiftCalculatorTest {
    private val s = AppSettings()

    private fun at(y: Int, m: Int, d: Int) = ShiftCalculator.baseShift(LocalDate.of(y, m, d), s.cycleStart)

    @Test
    fun ornekDongu() {
        // 20-21-22 Kasım gündüz
        listOf(20, 21, 22).forEach { assertEquals(ShiftType.DAY, at(2026, 11, it)) }
        // 23-25-27 Kasım gece
        listOf(23, 25, 27).forEach { assertEquals(ShiftType.NIGHT, at(2026, 11, it)) }
        // aradaki günler dinlenme
        listOf(24, 26, 28).forEach { assertEquals(ShiftType.REST, at(2026, 11, it)) }
        // 29-30 Kasım, 1 Aralık yeni gündüz döngüsü
        assertEquals(ShiftType.DAY, at(2026, 11, 29))
        assertEquals(ShiftType.DAY, at(2026, 11, 30))
        assertEquals(ShiftType.DAY, at(2026, 12, 1))
        assertEquals(ShiftType.NIGHT, at(2026, 12, 2))
        // geçmiş tarihler de doğru hesaplanmalı (11-12-13 Kasım gündüz)
        assertEquals(ShiftType.DAY, at(2026, 11, 11))
        assertEquals(ShiftType.NIGHT, at(2026, 11, 18))
        assertEquals(ShiftType.REST, at(2026, 11, 19))
    }

    @Test
    fun overrideOncelikli() {
        val d = LocalDate.of(2026, 11, 21)
        assertEquals(ShiftType.LEAVE, ShiftCalculator.shiftFor(d, s, mapOf(d to ShiftType.LEAVE)))
    }

    @Test
    fun sureler() {
        assertEquals(600L, ShiftCalculator.durationMinutes(ShiftType.DAY, s))
        assertEquals(840L, ShiftCalculator.durationMinutes(ShiftType.NIGHT, s))
        val sum = ShiftCalculator.monthSummary(YearMonth.of(2026, 11), s, emptyMap())
        assertEquals(sum.dayCount * 600L + sum.nightCount * 840L, sum.totalMinutes)
    }
}
