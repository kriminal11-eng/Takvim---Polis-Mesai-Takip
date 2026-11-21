package com.polis.mesaitakvim

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

/** Basit kalıcı depolama (SharedPreferences + JSON) */
object Storage {
    private const val PREFS = "mesai_takvim"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // ---------- Ajanda ----------
    fun loadItems(ctx: Context): List<AgendaItem> {
        val raw = prefs(ctx).getString("items", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.getJSONObject(i)
                try {
                    AgendaItem(
                        id = o.getInt("id"),
                        date = LocalDate.parse(o.getString("date")),
                        time = o.optString("time", "").takeIf { it.isNotEmpty() }?.let { LocalTime.parse(it) },
                        title = o.optString("title", ""),
                        note = o.optString("note", ""),
                        category = runCatching { Category.valueOf(o.optString("cat")) }.getOrDefault(Category.NOTE),
                        reminderMinutes = o.optInt("rem", -1).takeIf { it >= 0 },
                        done = o.optBoolean("done", false)
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveItems(ctx: Context, items: List<AgendaItem>) {
        val arr = JSONArray()
        items.forEach { it ->
            arr.put(JSONObject().apply {
                put("id", it.id)
                put("date", it.date.toString())
                put("time", it.time?.toString() ?: "")
                put("title", it.title)
                put("note", it.note)
                put("cat", it.category.name)
                put("rem", it.reminderMinutes ?: -1)
                put("done", it.done)
            })
        }
        prefs(ctx).edit().putString("items", arr.toString()).apply()
    }

    fun nextId(ctx: Context): Int {
        val p = prefs(ctx)
        val id = p.getInt("next_id", 1)
        p.edit().putInt("next_id", id + 1).apply()
        return id
    }

    // ---------- Mesai değişiklikleri (izin, rapor vb.) ----------
    fun loadOverrides(ctx: Context): Map<LocalDate, ShiftType> {
        val raw = prefs(ctx).getString("overrides", null) ?: return emptyMap()
        return try {
            val o = JSONObject(raw)
            o.keys().asSequence().mapNotNull { k ->
                runCatching { LocalDate.parse(k) to ShiftType.valueOf(o.getString(k)) }.getOrNull()
            }.toMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveOverrides(ctx: Context, map: Map<LocalDate, ShiftType>) {
        val o = JSONObject()
        map.forEach { (k, v) -> o.put(k.toString(), v.name) }
        prefs(ctx).edit().putString("overrides", o.toString()).apply()
    }

    // ---------- Ayarlar ----------
    fun loadSettings(ctx: Context): AppSettings {
        val p = prefs(ctx)
        val d = AppSettings()
        fun time(key: String, def: LocalTime) =
            p.getString(key, null)?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: def
        return AppSettings(
            cycleStart = p.getString("cycle_start", null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: d.cycleStart,
            dayStart = time("day_start", d.dayStart),
            dayEnd = time("day_end", d.dayEnd),
            nightStart = time("night_start", d.nightStart),
            nightEnd = time("night_end", d.nightEnd),
            shiftReminderEnabled = p.getBoolean("shift_rem_v2", d.shiftReminderEnabled),
            shiftReminderMinutes = p.getInt("shift_rem_min", d.shiftReminderMinutes),
            dayColor = p.getLong("day_color", d.dayColor),
            nightColor = p.getLong("night_color", d.nightColor)
        )
    }

    fun saveSettings(ctx: Context, s: AppSettings) {
        prefs(ctx).edit()
            .putString("cycle_start", s.cycleStart.toString())
            .putString("day_start", s.dayStart.toString())
            .putString("day_end", s.dayEnd.toString())
            .putString("night_start", s.nightStart.toString())
            .putString("night_end", s.nightEnd.toString())
            .putBoolean("shift_rem_v2", s.shiftReminderEnabled)
            .putInt("shift_rem_min", s.shiftReminderMinutes)
            .putLong("day_color", s.dayColor)
            .putLong("night_color", s.nightColor)
            .apply()
    }

    // ---------- Planlanmış alarm kodları ----------
    fun loadScheduledCodes(ctx: Context): Set<Int> =
        prefs(ctx).getStringSet("alarm_codes", emptySet())!!.mapNotNull { it.toIntOrNull() }.toSet()

    fun saveScheduledCodes(ctx: Context, codes: Set<Int>) {
        prefs(ctx).edit().putStringSet("alarm_codes", codes.map { it.toString() }.toSet()).apply()
    }
}
