package et.ethiohealth.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

// ---------------------------------------------------------------------------
// v1.4.0 screens: onboarding, symptom checker, appointment calendar,
// vaccination reminders, one-tap SOS. Everything works offline with the
// demo data; nothing here needs a network connection.
// ---------------------------------------------------------------------------

private fun parseDate14(s: String): LocalDate? = try {
    LocalDate.parse(s.trim(), DATE_FMT)
} catch (_: Exception) {
    null
}

/** Initials avatar: a soft-tinted circle with the first letters of a name. */
@Composable
fun AvatarCircle(name: String, sizeDp: Int = 48, tint: Color = Color(0xFFE0F2F1)) {
    val initials = name.trim().split(" ").filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.first().uppercase() }
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(tint),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initials.ifBlank { "?" },
            fontWeight = FontWeight.Bold,
            fontSize = (sizeDp / 3).sp,
            color = DarkTeal
        )
    }
}

// ---------------------------------------------------------------------------
// ONBOARDING — shown once (Store.KEY_ONBOARDING_DONE). Three cards:
// Find care / Talk to a doctor / Track your health. Swipe (drag) or tap Next.
// ---------------------------------------------------------------------------

@Composable
fun OnboardingScreen(lang: Lang, onDone: () -> Unit) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    var page by remember { mutableStateOf(0) }
    val emoji = when (page) {
        0 -> "🏥"
        1 -> "👩‍⚕️"
        else -> "📅"
    }
    val title = when (page) {
        0 -> tr("onboarding_1_title")
        1 -> tr("onboarding_2_title")
        else -> tr("onboarding_3_title")
    }
    val body = when (page) {
        0 -> tr("onboarding_1_body")
        1 -> tr("onboarding_2_body")
        else -> tr("onboarding_3_body")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(heroGradient)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.tena_adam_logo),
                contentDescription = "Tena Adam logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("ጤናአዳም", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 30.sp)
        Text(tr("tagline"), color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp)
        Spacer(modifier = Modifier.height(28.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(page) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, amount -> totalDrag += amount },
                        onDragEnd = {
                            if (totalDrag < -60f && page < 2) page += 1
                            if (totalDrag > 60f && page > 0) page -= 1
                            totalDrag = 0f
                        }
                    )
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(emoji, fontSize = 56.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DarkTeal, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(body, fontSize = 14.sp, color = DarkTeal.copy(alpha = 0.75f), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 0..2) {
                        Box(
                            modifier = Modifier
                                .size(width = if (i == page) 24.dp else 8.dp, height = 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (i == page) Teal else Color(0xFFBDBDBD))
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (page < 2) {
                OutlinedButton(
                    onClick = onDone,
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text(tr("onboarding_skip"), color = Color.White) }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }
            Button(
                onClick = { if (page < 2) page += 1 else onDone() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Teal),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    if (page < 2) tr("onboarding_next") else tr("onboarding_start"),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ---------------------------------------------------------------------------
// SYMPTOM CHECKER — non-diagnostic. Pick symptoms, get a suggested
// specialty + hospitals that can help. Always shows the disclaimer.
// Emergency symptoms show the emergency banner and a call-907 button.
// ---------------------------------------------------------------------------

private data class SymptomItem(val key: String, val spec: Specialty, val emergency: Boolean)

private val SYMPTOMS = listOf(
    SymptomItem("sym_fever", Specialty.PEDIATRICS, false),
    SymptomItem("sym_cough", Specialty.INTERNAL, false),
    SymptomItem("sym_headache", Specialty.NEUROLOGY, false),
    SymptomItem("sym_chest_pain", Specialty.CARDIOLOGY, true),
    SymptomItem("sym_breath", Specialty.INTERNAL, true),
    SymptomItem("sym_stomach", Specialty.INTERNAL, false),
    SymptomItem("sym_skin_rash", Specialty.DERMATOLOGY, false),
    SymptomItem("sym_sore_throat", Specialty.INTERNAL, false),
    SymptomItem("sym_bleeding", Specialty.SURGERY, true),
    SymptomItem("sym_dizziness", Specialty.NEUROLOGY, false),
    SymptomItem("sym_joint_pain", Specialty.ORTHOPEDICS, false),
    SymptomItem("sym_baby_fever", Specialty.PEDIATRICS, false)
)

@Composable
fun SymptomCheckerScreen(lang: Lang, store: Store, go: (Route) -> Unit) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    val context = LocalContext.current
    var selected by remember { mutableStateOf(setOf<String>()) }
    var checked by remember { mutableStateOf(false) }

    val specs = remember(selected) {
        SYMPTOMS.filter { selected.contains(it.key) }.map { it.spec }.distinct()
    }
    val hospitals = remember(specs) {
        if (specs.isEmpty()) emptyList() else Data.recommend(specs)
    }
    val hasEmergency = SYMPTOMS.any { selected.contains(it.key) && it.emergency }

    ScrollScreen {
        Text(tr("symptom_pick"), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        SYMPTOMS.forEach { item ->
            val isSelected = selected.contains(item.key)
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    selected = if (isSelected) selected - item.key else selected + item.key
                },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFFE0F2F1) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            selected = if (it) selected + item.key else selected - item.key
                        }
                    )
                    Text(
                        I18n.get(item.key, lang),
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (item.emergency) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = EthRed, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { checked = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Teal)
        ) { Text(tr("symptom_check")) }
        Spacer(modifier = Modifier.height(12.dp))

        if (checked) {
            if (selected.isEmpty()) {
                Text(tr("symptom_none"), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            } else {
                if (hasEmergency) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = EthRed)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(tr("emergency_banner"), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { dial(context, "907") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = EthRed),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("📞 907 — ${tr("ambulance")}", fontWeight = FontWeight.Bold) }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Text(tr("symptom_result"), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(6.dp))
                Text(tr("symptom_specialty"), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                specs.forEach { spec ->
                    Chip(spec.label(lang))
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(tr("symptom_hospitals"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                hospitals.take(5).forEach { h ->
                    HospitalCard(lang, h) { go(Route.HospitalDetail(h.id)) }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        DisclaimerText(lang)
    }
}

// ---------------------------------------------------------------------------
// APPOINTMENT CALENDAR — month grid of saved appointments, add / cancel,
// days-until countdown. Reminder choice (v1.4.0): in-app upcoming list +
// notification permission request. No AlarmManager / WorkManager yet —
// that needs a receiver and notification channel, so it is deliberately
// left out of this release to keep the CI build safe.
// ---------------------------------------------------------------------------

@Composable
fun AppointmentCalendarScreen(lang: Lang, store: Store, go: (Route) -> Unit) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    val context = LocalContext.current
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDay by remember { mutableStateOf(LocalDate.now()) }
    var appts by remember { mutableStateOf(store.list(Store.KEY_APPOINTMENTS)) }
    var hospName by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(selectedDay.format(DATE_FMT)) }
    var reason by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var notifState by remember { mutableStateOf("") }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifState = if (granted) tr("notif_allowed") else tr("appt_reminder_note")
    }

    fun apptDate(m: Map<String, String>): LocalDate? = parseDate14(m["date"] ?: "")
    val daysInMonth = month.lengthOfMonth()
    val firstWeekday = (month.atDay(1).dayOfWeek.value - 1) // Monday = 0
    val today = LocalDate.now()
    val selectedAppts = appts.filter { apptDate(it) == selectedDay }
    val upcoming = appts.mapNotNull { m ->
        val d = apptDate(m) ?: return@mapNotNull null
        if (d.isBefore(today)) null else m to d
    }.sortedBy { it.second }.take(3)

    ScrollScreen {
        // Month header with prev / next.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { month = month.minusMonths(1) }) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = { month = month.plusMonths(1) }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next month", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Weekday header (Mon first).
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                Text(d, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Calendar grid.
        val cells = mutableListOf<Int?>()
        repeat(firstWeekday) { cells.add(null) }
        for (d in 1..daysInMonth) cells.add(d)
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                week.forEach { day ->
                    if (day == null) {
                        Spacer(modifier = Modifier.width(40.dp).height(48.dp))
                    } else {
                        val date = month.atDay(day)
                        val hasAppt = appts.any { apptDate(it) == date }
                        val isSelected = date == selectedDay
                        val isToday = date == today
                        Column(
                            modifier = Modifier
                                .width(40.dp)
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isSelected -> Teal
                                        hasAppt -> Color(0xFFE0F2F1)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable {
                                    selectedDay = date
                                    dateText = date.format(DATE_FMT)
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                day.toString(),
                                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp,
                                color = when {
                                    isSelected -> Color.White
                                    isToday -> Teal
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (hasAppt) EthRed else Color.Transparent)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(tr("appt_pick_day"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "${tr("appt_calendar")} — ${selectedDay.format(DATE_FMT)}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (selectedAppts.isEmpty()) {
            Text(tr("appt_no_appts"), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
        selectedAppts.forEach { m ->
            val d = apptDate(m)
            val days = if (d != null) ChronoUnit.DAYS.between(today, d) else 0
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Teal, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(m["hospital"] ?: "", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("${m["date"] ?: ""} · $days ${tr("days_to_go")}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        if (!m["reason"].isNullOrBlank()) Text(m["reason"] ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    OutlinedButton(onClick = {
                        store.remove(Store.KEY_APPOINTMENTS, m["id"] ?: "")
                        appts = store.list(Store.KEY_APPOINTMENTS)
                    }) { Text(tr("cancel"), fontSize = 12.sp) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Upcoming (next 3) with countdown.
        if (upcoming.isNotEmpty()) {
            Text(tr("upcoming_appt"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            upcoming.forEach { (m, d) ->
                val days = ChronoUnit.DAYS.between(today, d)
                Text(
                    "• ${m["hospital"] ?: ""} — ${d.format(DATE_FMT)} · $days ${tr("days_to_go")}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        // Add an appointment.
        InfoCard {
            Text(tr("appt_add"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = hospName, onValueChange = { hospName = it }, label = { Text(tr("appt_hospital")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = dateText, onValueChange = { dateText = it }, label = { Text("${tr("date")} (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text(tr("reason")) }, modifier = Modifier.fillMaxWidth())
            if (error.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = EthRed, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    val d = parseDate14(dateText)
                    if (d == null || hospName.isBlank()) {
                        error = tr("invalid_date")
                        return@Button
                    }
                    error = ""
                    store.add(
                        Store.KEY_APPOINTMENTS,
                        mapOf(
                            "hospital" to hospName.trim(),
                            "hospital_id" to "",
                            "specialty" to "",
                            "name" to store.getString(Store.KEY_PROFILE_NAME, ""),
                            "phone" to store.getString(Store.KEY_PROFILE_PHONE, ""),
                            "date" to d.format(DATE_FMT),
                            "reason" to reason.trim(),
                            "status" to "Requested"
                        )
                    )
                    appts = store.list(Store.KEY_APPOINTMENTS)
                    selectedDay = d
                    hospName = ""
                    reason = ""
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) { Text(tr("appt_add")) }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(tr("appt_reminder_note"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(8.dp))
        if (Build.VERSION.SDK_INT >= 33) {
            OutlinedButton(onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }, modifier = Modifier.fillMaxWidth()) {
                Text(tr("allow_notifications"))
            }
        }
        if (notifState.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(notifState, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}

// ---------------------------------------------------------------------------
// VACCINATION REMINDERS — EPI schedule from the child's birth date, with
// Done marking stored on the phone (Store.KEY_VACCINE_DONE).
// ---------------------------------------------------------------------------

@Composable
fun VaccinationRemindersScreen(lang: Lang, store: Store, go: (Route) -> Unit) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    val dob = store.getString(Store.KEY_CHILD_DOB, "")
    val birth = parseDate14(dob)
    var doneKeys by remember {
        mutableStateOf(store.list(Store.KEY_VACCINE_DONE).mapNotNull { it["key"] }.toSet())
    }
    val today = LocalDate.now()

    fun vaccineKey(v: Vaccine): String = "${v.weeksAfterBirth}_${v.vaccines}"

    ScrollScreen {
        Text(tr("epi_note"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(12.dp))
        if (birth == null) {
            InfoCard {
                Text(tr("vac_no_dob"), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { go(Route.Maternal) },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal)
                ) { Text(tr("vac_open_maternal")) }
            }
            return@ScrollScreen
        }
        var allDone = true
        Data.vaccineSchedule.forEach { v ->
            val due = birth.plusWeeks(v.weeksAfterBirth.toLong())
            val key = vaccineKey(v)
            val isDone = doneKeys.contains(key)
            if (!isDone) allDone = false
            val overdue = !isDone && due.isBefore(today)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Vaccines,
                        contentDescription = null,
                        tint = if (isDone) LeafGreen else Teal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(v.label(lang), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(v.vaccines, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(
                            when {
                                isDone -> tr("done")
                                overdue -> "${tr("vac_overdue")} — ${due.format(DATE_FMT)}"
                                else -> "${tr("vac_due")} ${due.format(DATE_FMT)}"
                            },
                            fontSize = 12.sp,
                            color = if (overdue) EthRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = if (overdue) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    OutlinedButton(onClick = {
                        if (isDone) {
                            val row = store.list(Store.KEY_VACCINE_DONE).firstOrNull { it["key"] == key }
                            if (row != null) store.remove(Store.KEY_VACCINE_DONE, row["id"] ?: "")
                            doneKeys = doneKeys - key
                        } else {
                            store.add(Store.KEY_VACCINE_DONE, mapOf("key" to key))
                            doneKeys = doneKeys + key
                        }
                    }) {
                        if (isDone) {
                            Icon(Icons.Filled.Check, contentDescription = tr("done"), tint = LeafGreen, modifier = Modifier.size(18.dp))
                        } else {
                            Text(tr("vac_mark_done"), fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (allDone) {
            Text(tr("vac_all_done"), fontSize = 14.sp, color = LeafGreen, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------------------------------------------------------------------
// ONE-TAP SOS — big red screen: 907 / 911 / 991 / 939, plus share-my-location
// using the last-known GPS location (permission requested first). When GPS
// is unavailable it shares the Meskel Square demo location and says so.
// ---------------------------------------------------------------------------

@Composable
fun SosScreen(lang: Lang, store: Store) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    val context = LocalContext.current
    var locationMsg by remember { mutableStateOf("") }
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        val allowed = granted[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (!allowed) {
            locationMsg = tr("sos_location_none")
            return@rememberLauncherForActivityResult
        }
        val loc = getLastLocation(context)
        val text = if (loc != null) {
            "I need help. My location: https://www.google.com/maps/search/?api=1&query=${loc.first},${loc.second} (from the Tena Adam app)."
        } else {
            locationMsg = tr("sos_location_demo")
            "I need help. My approximate location is near Meskel Square, Addis Ababa (9.0108, 38.7613). This is a demo location from the Tena Adam app."
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(send, tr("share_location")))
    }

    ScrollScreen {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = EthRed),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🚨", fontSize = 48.sp)
                Text(tr("sos_title"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(tr("sos_subtitle"), color = Color.White.copy(alpha = 0.92f), fontSize = 14.sp, textAlign = TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        listOf(
            Triple("907", "ambulance", "🚑"),
            Triple("911", "national_emergency", "🚨"),
            Triple("991", "police", "👮"),
            Triple("939", "fire", "🔥")
        ).forEach { (number, labelKey, emoji) ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { dial(context, number) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(number, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = EthRed)
                        Text(tr(labelKey), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Icon(Icons.Filled.Phone, contentDescription = tr("call_now"), tint = Teal, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = {
                locationLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Teal),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(tr("sos_share_location"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                val loc = getLastLocation(context)
                val text = if (loc != null) {
                    "I need help. My location: https://www.google.com/maps/search/?api=1&query=${loc.first},${loc.second} (from the Tena Adam app)."
                } else {
                    locationMsg = tr("sos_location_demo")
                    "I need help. My approximate location is near Meskel Square, Addis Ababa (9.0108, 38.7613). This is a demo location from the Tena Adam app."
                }
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(
                    Intent.createChooser(send, tr("share_location")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(tr("share_location"))
        }
        if (locationMsg.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(locationMsg, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(tr("share_location_note"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
