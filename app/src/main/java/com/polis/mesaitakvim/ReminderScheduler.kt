package com.polis.mesaitakvim

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Tüm hatırlatmaları AlarmManager ile planlar */
object ReminderScheduler {
    const val EXTRA_ID = "id"
    const val EXTRA_TITLE = "title"
    const val EXTRA_TEXT = "text"
    const val EXTRA_CHANNEL = "channel"

    private const val SHIFT_CODE_BASE = 1_000_000
    private const val SHIFT_DAYS_AHEAD = 45L

    private val dateFmt = DateTimeFormatter.ofPattern("d MMMM EEEE", Locale("tr"))

    fun canScheduleExact(ctx: Context): Boolean {
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return false
        return Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
    }

    fun rescheduleAll(ctx: Context) {
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return

        // Öncekileri iptal et
        Storage.loadScheduledCodes(ctx).forEach { code ->
            am.cancel(pending(ctx, code, null, null, null))
        }

        val now = LocalDateTime.now()
        val codes = mutableSetOf<Int>()

        // Ajanda kayıtları
        for (item in Storage.loadItems(ctx)) {
            val minutes = item.reminderMinutes ?: continue
            if (item.done) continue
            val eventAt = item.date.atTime(item.effectiveTime)
            val triggerAt = eventAt.minusMinutes(minutes.toLong())
            if (!triggerAt.isAfter(now)) continue
            val whenText = if (item.time != null) "${item.date.format(dateFmt)} • ${ShiftCalculator.fmt(item.time)}"
            else item.date.format(dateFmt)
            val text = buildString {
                append(item.category.label).append(" • ").append(whenText)
                if (item.note.isNotBlank()) append("\n").append(item.note)
            }
            set(am, ctx, item.id, triggerAt, item.title.ifBlank { item.category.label }, text, Notifications.CHANNEL_AGENDA)
            codes += item.id
        }

        // Mesai uyarıları
        val s = Storage.loadSettings(ctx)
        if (s.shiftReminderEnabled) {
            val overrides = Storage.loadOverrides(ctx)
            val today = LocalDate.now()
            for (i in 0..SHIFT_DAYS_AHEAD) {
                val date = today.plusDays(i)
                val type = ShiftCalculator.shiftFor(date, s, overrides)
                val start = ShiftCalculator.startOf(date, type, s) ?: continue
                val triggerAt = start.minusMinutes(s.shiftReminderMinutes.toLong())
                if (!triggerAt.isAfter(now)) continue
                val code = SHIFT_CODE_BASE + (date.toEpochDay() % 100_000).toInt()
                val title = if (type == ShiftType.DAY) "☀️ Gündüz mesaisi yaklaşıyor" else "🌙 Gece mesaisi yaklaşıyor"
                val text = "${date.format(dateFmt)} • ${ShiftCalculator.hoursText(type, s)}"
                set(am, ctx, code, triggerAt, title, text, Notifications.CHANNEL_SHIFT)
                codes += code
            }
        }

        Storage.saveScheduledCodes(ctx, codes)
    }

    private fun set(
        am: AlarmManager, ctx: Context, code: Int, at: LocalDateTime,
        title: String, text: String, channel: String
    ) {
        val millis = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pi = pending(ctx, code, title, text, channel)
        try {
            if (canScheduleExact(ctx)) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
            }
        } catch (e: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
        }
    }

    private fun pending(ctx: Context, code: Int, title: String?, text: String?, channel: String?): PendingIntent {
        val intent = Intent(ctx, ReminderReceiver::class.java).apply {
            action = "com.polis.mesaitakvim.REMINDER_$code"
            putExtra(EXTRA_ID, code)
            if (title != null) putExtra(EXTRA_TITLE, title)
            if (text != null) putExtra(EXTRA_TEXT, text)
            if (channel != null) putExtra(EXTRA_CHANNEL, channel)
        }
        return PendingIntent.getBroadcast(
            ctx, code, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
