package com.polis.mesaitakvim.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.polis.mesaitakvim.AgendaItem
import com.polis.mesaitakvim.AppSettings
import com.polis.mesaitakvim.ReminderScheduler
import com.polis.mesaitakvim.ShiftCalculator
import com.polis.mesaitakvim.ShiftType
import com.polis.mesaitakvim.Storage
import java.time.LocalDate

/** Uygulama durumunu tutar; her değişiklik kaydedilir ve alarmlar yeniden kurulur */
class AppState(private val ctx: Context) {
    var items by mutableStateOf(Storage.loadItems(ctx))
        private set
    var overrides by mutableStateOf(Storage.loadOverrides(ctx))
        private set
    var settings by mutableStateOf(Storage.loadSettings(ctx))
        private set

    fun newId(): Int = Storage.nextId(ctx)

    fun itemsOn(date: LocalDate): List<AgendaItem> =
        items.filter { it.date == date }
            .sortedWith(compareBy<AgendaItem> { it.time == null }.thenBy { it.time })

    fun upcoming(limit: Int = 5): List<AgendaItem> {
        val today = LocalDate.now()
        return items.filter { !it.done && !it.date.isBefore(today) }
            .sortedWith(compareBy<AgendaItem> { it.date }.thenBy { it.effectiveTime })
            .take(limit)
    }

    fun shiftOn(date: LocalDate): ShiftType = ShiftCalculator.shiftFor(date, settings, overrides)

    fun upsert(item: AgendaItem) {
        items = items.filter { it.id != item.id } + item
        Storage.saveItems(ctx, items)
        reschedule()
    }

    fun delete(id: Int) {
        items = items.filter { it.id != id }
        Storage.saveItems(ctx, items)
        reschedule()
    }

    fun toggleDone(id: Int) {
        items = items.map { if (it.id == id) it.copy(done = !it.done) else it }
        Storage.saveItems(ctx, items)
        reschedule()
    }

    /** type = null -> otomatik döngüye dön */
    fun setOverride(date: LocalDate, type: ShiftType?) {
        overrides = if (type == null) overrides - date else overrides + (date to type)
        Storage.saveOverrides(ctx, overrides)
        reschedule()
    }

    fun updateSettings(s: AppSettings) {
        settings = s
        Storage.saveSettings(ctx, s)
        reschedule()
    }

    private fun reschedule() = ReminderScheduler.rescheduleAll(ctx)
}
