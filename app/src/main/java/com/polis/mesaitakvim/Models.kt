package com.polis.mesaitakvim

import java.time.LocalDate
import java.time.LocalTime

/** Bir günün mesai durumu */
enum class ShiftType(val label: String) {
    DAY("Gündüz"),
    NIGHT("Gece"),
    REST("Dinlenme"),
    LEAVE("İzin / Rapor")
}

/** Ajanda kayıt türleri ve renkleri */
enum class Category(val label: String, val color: Long) {
    NOTE("Not", 0xFF8E24AA),          // mor
    REMINDER("Hatırlatma", 0xFFE53935), // kırmızı
    TASK("Görev", 0xFF43A047),        // yeşil
    IMPORTANT("Önemli", 0xFFFB8C00),  // turuncu
    PERSONAL("Kişisel", 0xFF00897B)   // turkuaz
}

data class AgendaItem(
    val id: Int,
    val date: LocalDate,
    val time: LocalTime?,
    val title: String,
    val note: String,
    val category: Category,
    /** Kaç dakika önce hatırlatılacak; null = hatırlatma yok */
    val reminderMinutes: Int?,
    val done: Boolean = false
) {
    /** Hatırlatmanın baz alınacağı saat (saat girilmemişse 09:00) */
    val effectiveTime: LocalTime get() = time ?: LocalTime.of(9, 0)
}

data class AppSettings(
    /** Döngüdeki ilk gündüz mesaisinin tarihi */
    val cycleStart: LocalDate = LocalDate.of(2026, 11, 20),
    val dayStart: LocalTime = LocalTime.of(8, 0),
    val dayEnd: LocalTime = LocalTime.of(18, 0),
    val nightStart: LocalTime = LocalTime.of(18, 0),
    val nightEnd: LocalTime = LocalTime.of(8, 0),
    val shiftReminderEnabled: Boolean = true,
    val shiftReminderMinutes: Int = 60,
    val dayColor: Long = 0xFFFFD54F,   // sarı
    val nightColor: Long = 0xFF64B5F6  // mavi
)

val ColorPalette: List<Pair<String, Long>> = listOf(
    "Sarı" to 0xFFFFD54F,
    "Mavi" to 0xFF64B5F6,
    "Turuncu" to 0xFFFFB74D,
    "Lacivert" to 0xFF5C6BC0,
    "Yeşil" to 0xFF81C784,
    "Mor" to 0xFFBA68C8,
    "Kırmızı" to 0xFFE57373,
    "Turkuaz" to 0xFF4DD0E1
)

val ReminderOptions: List<Pair<Int?, String>> = listOf(
    null to "Hatırlatma yok",
    0 to "Tam zamanında",
    10 to "10 dakika önce",
    30 to "30 dakika önce",
    60 to "1 saat önce",
    120 to "2 saat önce",
    1440 to "1 gün önce"
)

fun reminderLabel(minutes: Int?): String =
    ReminderOptions.firstOrNull { it.first == minutes }?.second
        ?: if (minutes == null) "Hatırlatma yok" else "$minutes dakika önce"
