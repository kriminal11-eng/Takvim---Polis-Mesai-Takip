package com.polis.mesaitakvim.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.polis.mesaitakvim.AgendaItem
import com.polis.mesaitakvim.Category
import com.polis.mesaitakvim.ReminderOptions
import com.polis.mesaitakvim.ShiftCalculator
import com.polis.mesaitakvim.ShiftType
import com.polis.mesaitakvim.reminderLabel
import java.time.LocalDate
import java.time.LocalTime

fun pickDate(ctx: Context, initial: LocalDate, onPicked: (LocalDate) -> Unit) {
    DatePickerDialog(
        ctx,
        { _, y, m, d -> onPicked(LocalDate.of(y, m + 1, d)) },
        initial.year, initial.monthValue - 1, initial.dayOfMonth
    ).apply { datePicker.firstDayOfWeek = java.util.Calendar.MONDAY }.show()
}

fun pickTime(ctx: Context, initial: LocalTime, onPicked: (LocalTime) -> Unit) {
    TimePickerDialog(
        ctx,
        { _, h, m -> onPicked(LocalTime.of(h, m)) },
        initial.hour, initial.minute, true
    ).show()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemEditorDialog(
    initial: AgendaItem?,
    defaultDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (AgendaItem) -> Unit,
    onDelete: (Int) -> Unit
) {
    val ctx = LocalContext.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: Category.NOTE) }
    var date by remember { mutableStateOf(initial?.date ?: defaultDate) }
    var time by remember { mutableStateOf(initial?.time) }
    var reminder by remember { mutableStateOf(initial?.reminderMinutes) }
    var reminderMenu by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(0.94f)
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Text(
                    if (initial == null) "Yeni kayıt" else "Kaydı düzenle",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Başlık") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                Text("Tür", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Category.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = {
                                category = cat
                                if (cat == Category.REMINDER && reminder == null) reminder = 0
                            },
                            label = { Text(cat.label) },
                            leadingIcon = {
                                Box(Modifier.size(12.dp).background(Color(cat.color), CircleShape))
                            }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))

                // Tarih
                OutlinedButton(onClick = { pickDate(ctx, date) { date = it } }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(date.format(LongDateFmt))
                }
                Spacer(Modifier.height(8.dp))

                // Saat
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = time != null,
                        onCheckedChange = { on ->
                            time = if (on) (time ?: LocalTime.now().withSecond(0).withNano(0).plusHours(1).withMinute(0)) else null
                        }
                    )
                    Spacer(Modifier.width(10.dp))
                    if (time != null) {
                        OutlinedButton(onClick = { pickTime(ctx, time ?: LocalTime.of(9, 0)) { time = it } }) {
                            Icon(Icons.Filled.Schedule, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(ShiftCalculator.fmt(time!!))
                        }
                    } else {
                        Text("Tüm gün (saat yok)", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(8.dp))

                // Hatırlatma
                Box {
                    OutlinedButton(onClick = { reminderMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Notifications, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Uyarı: " + reminderLabel(reminder))
                    }
                    DropdownMenu(expanded = reminderMenu, onDismissRequest = { reminderMenu = false }) {
                        ReminderOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { reminder = value; reminderMenu = false }
                            )
                        }
                    }
                }
                if (reminder != null && time == null) {
                    Text(
                        "Saat seçilmediği için uyarı 09:00 baz alınarak verilir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Not / açıklama") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (initial != null) {
                        TextButton(onClick = { confirmDelete = true }) {
                            Text("Sil", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("İptal") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        enabled = title.isNotBlank() || note.isNotBlank(),
                        onClick = {
                            onSave(
                                AgendaItem(
                                    id = initial?.id ?: 0,
                                    date = date,
                                    time = time,
                                    title = title.trim(),
                                    note = note.trim(),
                                    category = category,
                                    reminderMinutes = reminder,
                                    done = initial?.done ?: false
                                )
                            )
                        }
                    ) { Text("Kaydet") }
                }
            }
        }
    }

    if (confirmDelete && initial != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Silinsin mi?") },
            text = { Text("\"${initial.title.ifBlank { initial.category.label }}\" kaydı silinecek.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete(initial.id) }) {
                    Text("Sil", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Vazgeç") } }
        )
    }
}

@Composable
fun ShiftOverrideDialog(
    date: LocalDate,
    current: ShiftType?,
    auto: ShiftType,
    onDismiss: () -> Unit,
    onSelect: (ShiftType?) -> Unit
) {
    val options: List<Pair<ShiftType?, String>> = listOf(null to "Otomatik (döngüye göre: ${auto.label})") +
        ShiftType.entries.map { it to it.label }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mesaiyi değiştir") },
        text = {
            Column {
                Text(date.format(LongDateFmt), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                options.forEach { (type, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(type) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = current == type, onClick = { onSelect(type) })
                        Text(label)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Kapat") } }
    )
}
