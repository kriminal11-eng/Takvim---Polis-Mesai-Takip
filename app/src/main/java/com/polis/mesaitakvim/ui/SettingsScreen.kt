package com.polis.mesaitakvim.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.polis.mesaitakvim.AppSettings
import com.polis.mesaitakvim.ColorPalette
import com.polis.mesaitakvim.Notifications
import com.polis.mesaitakvim.ReminderScheduler
import com.polis.mesaitakvim.ShiftCalculator
import java.time.LocalTime

private val ShiftReminderOptions = listOf(
    15 to "15 dakika önce",
    30 to "30 dakika önce",
    60 to "1 saat önce",
    90 to "1,5 saat önce",
    120 to "2 saat önce",
    180 to "3 saat önce",
    720 to "12 saat önce"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(state: AppState, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val s = state.settings
    var refresh by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) {
        refresh++
        onPauseOrDispose { }
    }
    // refresh değiştiğinde izin durumları yeniden okunur
    val canNotify = remember(refresh) { Notifications.canNotify(ctx) }
    val exactOk = remember(refresh) { ReminderScheduler.canScheduleExact(ctx) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Section("Mesai döngüsü (Çakma 12/36)") {
                Text(
                    "Döngü 9 gündür: 3 gün üst üste gündüz, ardından gün aşırı 3 gece. " +
                        "Döngünün başladığı İLK GÜNDÜZ gününü seçin; takvim tüm ayları buna göre otomatik doldurur.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                SettingRow("İlk gündüz günü") {
                    OutlinedButton(onClick = {
                        pickDate(ctx, s.cycleStart) { state.updateSettings(s.copy(cycleStart = it)) }
                    }) { Text(s.cycleStart.format(LongDateFmt)) }
                }
                Spacer(Modifier.height(6.dp))
                CyclePreview(s)
            }

            Section("Mesai saatleri") {
                TimeRow("Gündüz başlangıç", s.dayStart) { state.updateSettings(s.copy(dayStart = it)) }
                TimeRow("Gündüz bitiş", s.dayEnd) { state.updateSettings(s.copy(dayEnd = it)) }
                TimeRow("Gece başlangıç", s.nightStart) { state.updateSettings(s.copy(nightStart = it)) }
                TimeRow("Gece bitiş (ertesi gün)", s.nightEnd) { state.updateSettings(s.copy(nightEnd = it)) }
            }

            Section("Mesai uyarısı") {
                SettingRow("Mesaiden önce bildirim gönder") {
                    Switch(
                        checked = s.shiftReminderEnabled,
                        onCheckedChange = { state.updateSettings(s.copy(shiftReminderEnabled = it)) }
                    )
                }
                if (s.shiftReminderEnabled) {
                    var menu by remember { mutableStateOf(false) }
                    SettingRow("Ne zaman?") {
                        Box {
                            OutlinedButton(onClick = { menu = true }) {
                                Text(
                                    ShiftReminderOptions.firstOrNull { it.first == s.shiftReminderMinutes }?.second
                                        ?: "${s.shiftReminderMinutes} dk önce"
                                )
                            }
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                ShiftReminderOptions.forEach { (v, label) ->
                                    DropdownMenuItem(text = { Text(label) }, onClick = {
                                        state.updateSettings(s.copy(shiftReminderMinutes = v)); menu = false
                                    })
                                }
                            }
                        }
                    }
                }
            }

            Section("Renkler") {
                Text("Gündüz mesaisi rengi", style = MaterialTheme.typography.bodyMedium)
                ColorPicker(s.dayColor) { state.updateSettings(s.copy(dayColor = it)) }
                Spacer(Modifier.height(8.dp))
                Text("Gece mesaisi rengi", style = MaterialTheme.typography.bodyMedium)
                ColorPicker(s.nightColor) { state.updateSettings(s.copy(nightColor = it)) }
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = {
                    val d = AppSettings()
                    state.updateSettings(s.copy(dayColor = d.dayColor, nightColor = d.nightColor))
                }) { Text("Varsayılan renkler (sarı gündüz, mavi gece)") }
            }

            Section("Bildirimler ve uyarılar") {
                SettingRow("Bildirim izni: " + if (canNotify) "Açık ✔" else "Kapalı ✖") {
                    if (!canNotify) {
                        TextButton(onClick = {
                            ctx.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }) { Text("Aç") }
                    }
                }
                SettingRow("Tam zamanında alarm: " + if (exactOk) "Açık ✔" else "Kapalı ✖") {
                    if (!exactOk && Build.VERSION.SDK_INT >= 31) {
                        TextButton(onClick = {
                            ctx.startActivity(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + ctx.packageName))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }) { Text("Aç") }
                    }
                }
                Text(
                    "İpucu: Bazı telefonlarda (Samsung, Xiaomi, Huawei vb.) pil tasarrufu bildirimleri geciktirebilir. " +
                        "Pil ayarlarından bu uygulamayı \"Kısıtlama yok\" olarak işaretleyin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        ctx.startActivity(
                            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }) { Text("Pil ayarları") }
                    OutlinedButton(onClick = {
                        Notifications.show(
                            ctx, 999_999, Notifications.CHANNEL_AGENDA,
                            "Test bildirimi", "Bildirimler çalışıyor ✔"
                        )
                    }) { Text("Test bildirimi") }
                }
            }
            CreditText()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SettingRow(label: String, trailing: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
private fun TimeRow(label: String, value: LocalTime, onChange: (LocalTime) -> Unit) {
    val ctx = LocalContext.current
    SettingRow(label) {
        OutlinedButton(onClick = { pickTime(ctx, value, onChange) }) { Text(ShiftCalculator.fmt(value)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPicker(selected: Long, onPick: (Long) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 6.dp)
    ) {
        ColorPalette.forEach { (name, value) ->
            val c = Color(value)
            Box(
                Modifier
                    .size(36.dp)
                    .background(c, CircleShape)
                    .border(
                        if (value == selected) 3.dp else 1.dp,
                        if (value == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        CircleShape
                    )
                    .clickable { onPick(value) },
                contentAlignment = Alignment.Center
            ) {
                if (value == selected) Icon(Icons.Filled.Check, contentDescription = name, tint = textOn(c))
            }
        }
    }
}

/** Seçilen başlangıçtan itibaren bir döngüyü gösterir */
@Composable
private fun CyclePreview(s: AppSettings) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
        for (i in 0 until ShiftCalculator.CYCLE_LENGTH) {
            val date = s.cycleStart.plusDays(i.toLong())
            val type = ShiftCalculator.baseShift(date, s.cycleStart)
            val base = shiftColor(type, s)
            val bg = if (base == Color.Transparent) MaterialTheme.colorScheme.surfaceVariant else base
            Box(
                Modifier
                    .weight(1f)
                    .height(34.dp)
                    .background(bg, androidx.compose.foundation.shape.RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${date.dayOfMonth}",
                    color = if (base == Color.Transparent) MaterialTheme.colorScheme.onSurface else textOn(base),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
