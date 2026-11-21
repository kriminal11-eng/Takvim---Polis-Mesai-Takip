package com.polis.mesaitakvim.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polis.mesaitakvim.AgendaItem
import com.polis.mesaitakvim.AppSettings
import com.polis.mesaitakvim.Category
import com.polis.mesaitakvim.ShiftCalculator
import com.polis.mesaitakvim.ShiftType
import com.polis.mesaitakvim.reminderLabel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

val TR: Locale = Locale("tr", "TR")
val MonthFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", TR)
val LongDateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, EEEE", TR)
val ShortDateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM EEE", TR)
private val WeekDays = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

fun shiftColor(type: ShiftType, s: AppSettings): Color = when (type) {
    ShiftType.DAY -> Color(s.dayColor)
    ShiftType.NIGHT -> Color(s.nightColor)
    ShiftType.LEAVE -> LeaveColor
    ShiftType.REST -> Color.Transparent
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(state: AppState, onOpenSettings: () -> Unit) {
    val today = remember { LocalDate.now() }
    var month by rememberSaveable { mutableStateOf(YearMonth.now()) }
    var selected by rememberSaveable { mutableStateOf(today) }
    var editing by remember { mutableStateOf<AgendaItem?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var showShiftDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mesai Takvimi", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { month = YearMonth.from(today); selected = today }) {
                        Icon(Icons.Filled.Today, contentDescription = "Bugün")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Ayarlar")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = null; showEditor = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Not / Hatırlatma") }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp)
        ) {
            // Ay başlığı
            Row(
                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { month = month.minusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Önceki ay")
                }
                Text(
                    month.format(MonthFmt).replaceFirstChar { it.titlecase(TR) },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { month = month.plusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Sonraki ay")
                }
            }

            // Gün adları
            Row(Modifier.fillMaxWidth()) {
                WeekDays.forEachIndexed { i, d ->
                    Text(
                        d,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (i >= 5) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(4.dp))

            // Ay ızgarası (sağa/sola kaydırarak ay değiştirme)
            Column(
                Modifier.pointerInput(Unit) {
                    var total = 0f
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (total > 120f) month = month.minusMonths(1)
                            else if (total < -120f) month = month.plusMonths(1)
                            total = 0f
                        },
                        onDragCancel = { total = 0f },
                        onHorizontalDrag = { _, delta -> total += delta }
                    )
                }
            ) {
                val first = month.atDay(1)
                val offset = first.dayOfWeek.value - 1
                val rows = (offset + month.lengthOfMonth() + 6) / 7
                val gridStart = first.minusDays(offset.toLong())
                for (r in 0 until rows) {
                    Row(Modifier.fillMaxWidth()) {
                        for (c in 0 until 7) {
                            val date = gridStart.plusDays((r * 7 + c).toLong())
                            DayCell(
                                date = date,
                                inMonth = YearMonth.from(date) == month,
                                isToday = date == today,
                                isSelected = date == selected,
                                shift = state.shiftOn(date),
                                settings = state.settings,
                                dots = state.itemsOn(date).filter { !it.done }.map { it.category }.distinct(),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selected = date
                                    if (YearMonth.from(date) != month) month = YearMonth.from(date)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Legend(state.settings)
            Spacer(Modifier.height(8.dp))
            MonthSummaryCard(month, state)
            Spacer(Modifier.height(8.dp))
            DayPanel(
                date = selected,
                state = state,
                onChangeShift = { showShiftDialog = true },
                onEdit = { editing = it; showEditor = true }
            )
            Spacer(Modifier.height(8.dp))
            UpcomingCard(state, onOpen = { item ->
                selected = item.date
                month = YearMonth.from(item.date)
                editing = item
                showEditor = true
            })
            Spacer(Modifier.height(96.dp))
        }
    }

    if (showEditor) {
        ItemEditorDialog(
            initial = editing,
            defaultDate = selected,
            onDismiss = { showEditor = false },
            onSave = { item ->
                val toSave = if (item.id == 0) item.copy(id = state.newId()) else item
                state.upsert(toSave)
                selected = toSave.date
                month = YearMonth.from(toSave.date)
                showEditor = false
            },
            onDelete = { id -> state.delete(id); showEditor = false }
        )
    }

    if (showShiftDialog) {
        ShiftOverrideDialog(
            date = selected,
            current = state.overrides[selected],
            auto = ShiftCalculator.baseShift(selected, state.settings.cycleStart),
            onDismiss = { showShiftDialog = false },
            onSelect = { type -> state.setOverride(selected, type); showShiftDialog = false }
        )
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    shift: ShiftType,
    settings: AppSettings,
    dots: List<Category>,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val base = shiftColor(shift, settings)
    val alpha = if (inMonth) 1f else 0.35f
    val bg = if (base == Color.Transparent) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f * alpha)
    else base.copy(alpha = alpha)
    val fg = if (base == Color.Transparent) MaterialTheme.colorScheme.onSurface.copy(alpha = if (inMonth) 1f else 0.4f)
    else textOn(base).copy(alpha = if (inMonth) 1f else 0.5f)
    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier
            .aspectRatio(0.78f)
            .padding(2.dp)
            .clip(shape)
            .background(bg)
            .border(
                width = if (isSelected) 2.5.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(3.dp)
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (isToday) {
                Box(
                    Modifier.size(24.dp).background(MaterialTheme.colorScheme.secondary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${date.dayOfMonth}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    "${date.dayOfMonth}",
                    color = fg,
                    fontSize = 14.sp,
                    fontWeight = if (shift == ShiftType.DAY || shift == ShiftType.NIGHT) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.height(24.dp).padding(top = 2.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            val icon = when (shift) {
                ShiftType.DAY -> Icons.Filled.WbSunny
                ShiftType.NIGHT -> Icons.Filled.Bedtime
                ShiftType.LEAVE -> Icons.Filled.EventBusy
                ShiftType.REST -> null
            }
            if (icon != null) Icon(icon, contentDescription = shift.label, tint = fg, modifier = Modifier.size(14.dp))
            Spacer(Modifier.weight(1f))
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.height(8.dp)
            ) {
                dots.take(4).forEach { cat ->
                    Box(
                        Modifier
                            .size(7.dp)
                            .background(Color(cat.color), CircleShape)
                            .border(0.5.dp, Color.White, CircleShape)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Legend(s: AppSettings) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Mesai renkleri", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LegendBox(Color(s.dayColor), "Gündüz  ${ShiftCalculator.fmt(s.dayStart)}–${ShiftCalculator.fmt(s.dayEnd)}")
                LegendBox(Color(s.nightColor), "Gece  ${ShiftCalculator.fmt(s.nightStart)}–${ShiftCalculator.fmt(s.nightEnd)}")
                LegendBox(LeaveColor, "İzin / Rapor")
                LegendBox(Color.Transparent, "Dinlenme")
            }
            Spacer(Modifier.height(10.dp))
            Text("Ajanda renkleri", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Category.entries.forEach { cat ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(12.dp).background(Color(cat.color), CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text(cat.label, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendBox(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(width = 22.dp, height = 16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MonthSummaryCard(month: YearMonth, state: AppState) {
    val sum = ShiftCalculator.monthSummary(month, state.settings, state.overrides)
    val hours = sum.totalMinutes / 60
    val mins = sum.totalMinutes % 60
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${month.format(MonthFmt).replaceFirstChar { it.titlecase(TR) }} özeti",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${sum.dayCount} gündüz • ${sum.nightCount} gece" +
                        if (sum.leaveCount > 0) " • ${sum.leaveCount} izin" else "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                if (mins == 0L) "$hours saat" else "$hours sa $mins dk",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DayPanel(
    date: LocalDate,
    state: AppState,
    onChangeShift: () -> Unit,
    onEdit: (AgendaItem) -> Unit
) {
    val s = state.settings
    val shift = state.shiftOn(date)
    val prevShift = state.shiftOn(date.minusDays(1))
    val overridden = state.overrides.containsKey(date)
    val items = state.itemsOn(date)

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(
                date.format(LongDateFmt),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val c = shiftColor(shift, s)
                Box(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (c == Color.Transparent) MaterialTheme.colorScheme.surfaceVariant else c)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    val text = when (shift) {
                        ShiftType.DAY -> "Gündüz mesaisi • ${ShiftCalculator.hoursText(shift, s)}"
                        ShiftType.NIGHT -> "Gece mesaisi • ${ShiftCalculator.hoursText(shift, s)}"
                        ShiftType.REST -> "Dinlenme günü"
                        ShiftType.LEAVE -> "İzin / Rapor"
                    }
                    Text(
                        text,
                        color = if (c == Color.Transparent) MaterialTheme.colorScheme.onSurface else textOn(c),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onChangeShift) { Text("Değiştir") }
            }
            if (prevShift == ShiftType.NIGHT) {
                Text(
                    "🌙 Önceki geceden ${ShiftCalculator.fmt(s.nightEnd)}'e kadar mesai devam ediyor",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (overridden) {
                Text(
                    "Bu gün elle değiştirildi (otomatik: ${ShiftCalculator.baseShift(date, s.cycleStart).label})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(6.dp))

            if (items.isEmpty()) {
                Text(
                    "Bu gün için kayıt yok. Aşağıdaki butonla not, görev veya hatırlatma ekleyebilirsiniz.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                items.forEach { item ->
                    ItemRow(item, onClick = { onEdit(item) }, onToggle = { state.toggleDone(item.id) })
                }
            }
        }
    }
}

@Composable
private fun ItemRow(item: AgendaItem, onClick: () -> Unit, onToggle: () -> Unit, showDate: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(5.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(item.category.color))
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                item.title.ifBlank { item.category.label },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                textDecoration = if (item.done) TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val meta = buildString {
                if (showDate) append(item.date.format(ShortDateFmt)).append(" • ")
                append(item.time?.let { ShiftCalculator.fmt(it) } ?: "Tüm gün")
                append(" • ").append(item.category.label)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (item.reminderMinutes != null && !item.done) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Filled.NotificationsActive,
                        contentDescription = reminderLabel(item.reminderMinutes),
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        reminderLabel(item.reminderMinutes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            if (item.note.isNotBlank()) {
                Text(
                    item.note,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (item.category == Category.TASK) {
            Checkbox(checked = item.done, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
private fun UpcomingCard(state: AppState, onOpen: (AgendaItem) -> Unit) {
    val upcoming = state.upcoming(6)
    if (upcoming.isEmpty()) return
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Yaklaşanlar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            upcoming.forEach { item ->
                ItemRow(item, onClick = { onOpen(item) }, onToggle = { state.toggleDone(item.id) }, showDate = true)
            }
        }
    }
}
