package com.polis.mesaitakvim

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(ReminderScheduler.EXTRA_ID, 0)
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: "Hatırlatma"
        val text = intent.getStringExtra(ReminderScheduler.EXTRA_TEXT) ?: ""
        val channel = intent.getStringExtra(ReminderScheduler.EXTRA_CHANNEL) ?: Notifications.CHANNEL_AGENDA
        Notifications.show(context, id, channel, title, text)
        // İleriye dönük mesai uyarılarını tazele
        ReminderScheduler.rescheduleAll(context)
    }
}

/** Telefon yeniden başlatıldığında / saat değiştiğinde alarmları yeniden kurar */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderScheduler.rescheduleAll(context)
    }
}
