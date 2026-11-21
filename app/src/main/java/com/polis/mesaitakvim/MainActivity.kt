package com.polis.mesaitakvim

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.polis.mesaitakvim.ui.AppState
import com.polis.mesaitakvim.ui.CalendarScreen
import com.polis.mesaitakvim.ui.MesaiTheme
import com.polis.mesaitakvim.ui.SettingsScreen

class MainActivity : ComponentActivity() {

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            ReminderScheduler.rescheduleAll(this)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Notifications.createChannels(this)
        if (Build.VERSION.SDK_INT >= 33 && !Notifications.canNotify(this)) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            MesaiTheme {
                val state = remember { AppState(applicationContext) }
                var showSettings by rememberSaveable { mutableStateOf(false) }
                BackHandler(enabled = showSettings) { showSettings = false }
                if (showSettings) {
                    SettingsScreen(state, onBack = { showSettings = false })
                } else {
                    CalendarScreen(state, onOpenSettings = { showSettings = true })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ReminderScheduler.rescheduleAll(this)
    }
}
