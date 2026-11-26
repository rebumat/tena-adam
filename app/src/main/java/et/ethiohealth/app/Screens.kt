package et.ethiohealth.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

private fun parseDate(s: String): LocalDate? = try {
    LocalDate.parse(s.trim(), DATE_FMT)
} catch (_: Exception) {
    null
}

fun openMap(context: Context, lat: Double, lon: Double, label: String) {
    val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon(${Uri.encode(label)})")
    context.startActivity(
        Intent(Intent.ACTION_VIEW, uri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    )
}

/** A small green/teal chip (used for tier, online status, etc.). */
@Composable
fun Chip(text: String, container: Color = Teal, content: Color = Color.White) {
    Text(
        text = text,
        modifier = Modifier
            .background(container, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = content,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
}

// ---------------------------------------------------------------------------
// HOME — v0.2 layout: gradient hero, search bar, AI banner, 4 sections of
// 3-column bilingual tiles, upcoming appointments, disclaimer, footer.
// ---------------------------------------------------------------------------

@Composable
fun HomeScreen(
    lang: Lang,
    store: Store,
    go: (Route) -> Unit,
    goRoot: (Route) -> Unit
) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    val name = store.getString(Store.KEY_PROFILE_NAME, "Rebuma").ifBlank { "Rebuma" }
    var query by remember { mutableStateOf("") }

    val sections = listOf(
        "find_care" to listOf(
            Triple("🏥", "find_hospital", Route.Finder()),
            Triple("👩‍⚕️", "online_doctor", Route.Doctors),
            Triple("💊", "pharmacy", Route.Pharmacy),
            Triple("🗺️", "nearby_map", Route.NearbyMap),
            Triple("🛏️", "hospital_beds", Route.Beds),
            Triple("⭐", "ratings_reviews", Route.Ratings)
        ),
        "health_services" to listOf(
            Triple("🧪", "lab_tests", Route.LabTests),
            Triple("🩸", "blood_bank", Route.BloodBank),
            Triple("🚑", "emergency", Route.Emergency),
            Triple("🛡️", "health_insurance", Route.Insurance),
            Triple("💳", "payments", Route.Payments),
            Triple("🩹", "first_aid", Route.FirstAid)
        ),
        "my_health" to listOf(
            Triple("📋", "records", Route.Records),
            Triple("⏰", "medicine_reminders", Route.Reminders),
            Triple("🩺", "bp_sugar", Route.BpSugar),
            Triple("🤰", "maternal", Route.Maternal),
            Triple("🧮", "health_calculators", Route.Calculators),
            Triple("🧘", "mental_wellbeing", Route.Mental),
            Triple("👨‍👩‍👧", "profile_family", Route.Profile)
        ),
        "ai_learn_support" to listOf(
            Triple("🤖", "assistant", Route.Assistant),
            Triple("🔮", "future_ai", Route.FutureAi),
            Triple("📚", "education", Route.Education),
            Triple("📰", "health_news", Route.News),
            Triple("🌐", "language", Route.Language),
            Triple("🏛️", "health_system", Route.HealthSystem),
            Triple("ℹ️", "about", Route.About)
        )
    )

    ScrollScreen {
        // Hero card (bright leaf green → deep teal gradient).
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .background(heroGradient)
                    .padding(20.dp)
            ) {
                Text("ጤናአዳም · Tena Adam", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${tr("hello")}, $name", color = Color.White, fontSize = 16.sp)
                Text(tr("tagline"), color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                Text(tr("how_help"), color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Search bar → Finder.
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(tr("search_hint"), fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { go(Route.Finder(query.ifBlank { null })) },
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) { Text(tr("search")) }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // AI Health Assistant banner.
        Card(
            modifier = Modifier.fillMaxWidth().clickable { go(Route.Assistant) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🤖", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(tr("assistant"), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkTeal)
                    Text(tr("ai_banner"), fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.7f))
                }
                Text("›", fontSize = 26.sp, color = DarkTeal)
            }
        }

        // Upcoming appointments (from saved appointments, with countdown).
        val upcoming = remember(lang) {
            val today = LocalDate.now()
            store.list(Store.KEY_APPOINTMENTS)
                .mapNotNull { m ->
                    val d = parseDate(m["date"] ?: "") ?: return@mapNotNull null
                    if (d.isBefore(today)) null else m to d
                }
                .sortedBy { it.second }
                .take(2)
        }
        if (upcoming.isNotEmpty()) {
            SectionTitle(tr("upcoming_appt"))
            upcoming.forEach { (m, d) ->
                val days = ChronoUnit.DAYS.between(LocalDate.now(), d)
                TileCard(
                    emoji = "📅",
                    title = m["hospital"] ?: tr("appointment"),
                    subtitle = "${m["date"]} · $days ${tr("days_to_go")}",
                    onClick = { go(Route.Records) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            Text(
                tr("no_upcoming"),
                fontSize = 12.sp,
                color = DarkTeal.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        // The four v0.2 sections.
        sections.forEach { (sectionKey, tiles) ->
            SectionTitle(tr(sectionKey))
            tiles.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { (emoji, key, route) ->
                        HomeTile(
                            emoji = emoji,
                            title = tr(key),
                            subtitle = if (lang == Lang.AM) I18n.get(key, Lang.EN) else I18n.get(key, Lang.AM),
                            modifier = Modifier.weight(1f),
                            onClick = { go(route) }
                        )
                    }
                    // Pad incomplete rows so tiles keep equal width.
                    repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }

        DisclaimerText(lang)
        Text(
            text = tr("prototype_v11"),
            color = DarkTeal.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun HomeTile(
    emoji: String,
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 26.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkTeal,
                textAlign = TextAlign.Center, maxLines = 2, minLines = 2
            )
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, fontSize = 10.sp, color = DarkTeal.copy(alpha = 0.6f), textAlign = TextAlign.Center, maxLines = 1)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// FINDER — search by disease/condition, tier filter, hospital cards.
// ---------------------------------------------------------------------------

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun FinderScreen(
    lang: Lang,
    store: Store,
    initial: String?,
    go: (Route) -> Unit,
    pop: () -> Unit
) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    var query by remember { mutableStateOf(initial ?: "") }
    var tier by remember { mutableStateOf<FacilityTier?>(null) }

    val results = remember(query, tier) {
        val q = query.trim()
        val base = if (q.isEmpty()) {
            Data.hospitals.sortedBy { Data.distanceKm(it.lat, it.lon) }
        } else {
            val specs = Data.matchSpecialties(q)
            if (specs.isNotEmpty()) Data.recommend(specs)
            else Data.hospitals.filter {
                it.name.contains(q, ignoreCase = true) || it.area.contains(q, ignoreCase = true)
            }.sortedBy { Data.distanceKm(it.lat, it.lon) }
        }
        if (tier == null) base else base.filter { it.tier == tier }
    }

    ScrollScreen {
        DemoBadge(lang)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(tr("search_hint"), fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Tier filter chips.
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val allSelected = tier == null
            Text(
                text = tr("tier_all"),
                modifier = Modifier
                    .background(if (allSelected) Teal else Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                    .clickable { tier = null }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = if (allSelected) Color.White else DarkTeal,
                fontSize = 13.sp, fontWeight = FontWeight.Bold
            )
            FacilityTier.values().forEach { t ->
                val selected = tier == t
                Text(
                    text = t.label(lang),
                    modifier = Modifier
                        .background(if (selected) Teal else Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                        .clickable { tier = t }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    color = if (selected) Color.White else DarkTeal,
                    fontSize = 13.sp, fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (results.isEmpty()) {
            Text(tr("no_sample_data"), color = DarkTeal.copy(alpha = 0.7f))
        }
        results.forEach { h ->
            HospitalCard(lang, h) { go(Route.HospitalDetail(h.id)) }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Link to the health-system explainer.
        Spacer(modifier = Modifier.height(8.dp))
        TileCard(
            emoji = "🏛️",
            title = tr("health_system"),
            subtitle = tr("hs_title"),
            onClick = { go(Route.HealthSystem) }
        )
    }
}

@Composable
fun HospitalCard(lang: Lang, h: Hospital, onClick: () -> Unit) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏥", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(h.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkTeal, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(h.area, fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(h.tier.label(lang))
                Chip(
                    text = "%.1f km".format(Data.distanceKm(h.lat, h.lon)),
                    container = Color(0xFFE8F5E9), content = DarkTeal
                )
                if (h.fictional) Chip(tr("fictional_sample"), container = Color(0xFFFFF5B8), content = Color(0xFF6B5B00))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                h.specialties.take(3).joinToString(", ") { it.label(lang) },
                fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.65f)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// HOSPITAL DETAIL
// ---------------------------------------------------------------------------

@Composable
fun HospitalDetailScreen(
    lang: Lang,
    store: Store,
    id: String,
    spec: Specialty?,
    go: (Route) -> Unit,
    pop: () -> Unit
) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    val context = LocalContext.current
    val h = Data.hospitalById(id)
    ScrollScreen {
        if (h == null) {
            Text(tr("no_sample_data"))
            return@ScrollScreen
        }
        Text(h.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DarkTeal)
        Text(h.area, fontSize = 14.sp, color = DarkTeal.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip(h.tier.label(lang))
            Chip(
                text = "%.1f km".format(Data.distanceKm(h.lat, h.lon)),
                container = Color(0xFFE8F5E9), content = DarkTeal
            )
            if (h.emergency24h) Chip(tr("emergency_24h"), container = EthRed)
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (h.fictional) DemoBadge(lang) else Text(tr("real_hospital_note"), fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(12.dp))

        SectionTitle(tr("specialty"))
        h.specialties.forEach { s ->
            Text("• ${s.label(lang)}", fontSize = 14.sp, color = DarkTeal)
        }
        SectionTitle(tr("services"))
        h.services.forEach { s ->
            Text("• $s", fontSize = 14.sp, color = DarkTeal)
        }
        SectionTitle(tr("beds"))
        val (avail, total) = Data.bedsFor(h)
        Text("${tr("beds_available")}: $avail / $total  (${tr("beds_note")})", fontSize = 14.sp, color = DarkTeal)

        Spacer(modifier = Modifier.height(16.dp))
        if (h.phone.isNotBlank()) {
            Button(
                onClick = { dial(context, h.phone) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) { Text("📞 ${tr("call")} ${h.phone}") }
            Text(tr("phone") + ": " + h.phoneNote, fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.6f))
        } else {
            Text(tr("phone") + ": " + h.phoneNote, fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { go(Route.Appointment(h.id, spec)) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LeafGreen)
        ) { Text(tr("book_appointment")) }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { openMap(context, h.lat, h.lon, h.name) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("🗺️ ${tr("view_map")}") }
    }
}

// ---------------------------------------------------------------------------
// APPOINTMENT — request saved on the phone (status "Requested").
// ---------------------------------------------------------------------------

@Composable
fun AppointmentScreen(lang: Lang, store: Store, id: String, spec: Specialty?, pop: () -> Unit) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    val h = Data.hospitalById(id)
    var name by remember { mutableStateOf(store.getString(Store.KEY_PROFILE_NAME, "Rebuma")) }
    var phone by remember { mutableStateOf(store.getString(Store.KEY_PROFILE_PHONE, "")) }
    var date by remember { mutableStateOf(LocalDate.now().plusDays(3).format(DATE_FMT)) }
    var reason by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    ScrollScreen {
        Text(h?.name ?: tr("hospital"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        if (spec != null) Text(spec.label(lang), fontSize = 14.sp, color = DarkTeal.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(12.dp))
        if (saved) {
            InfoCard {
                Text("✅ ${tr("added")}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
                Spacer(modifier = Modifier.height(6.dp))
                Text("${tr("status_label")}: ${tr("requested")}", fontSize = 14.sp, color = DarkTeal)
                Text(tr("appt_local_note"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = pop, colors = ButtonDefaults.buttonColors(containerColor = Teal)) {
                    Text(tr("back"))
                }
            }
            return@ScrollScreen
        }
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(tr("your_name")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(tr("phone_number")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("${tr("date")} (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text(tr("reason")) }, modifier = Modifier.fillMaxWidth())
        if (error.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = EthRed, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                val d = parseDate(date)
                if (d == null) {
                    error = tr("invalid_date")
                    return@Button
                }
                store.add(
                    Store.KEY_APPOINTMENTS,
                    mapOf(
                        "hospital" to (h?.name ?: ""),
                        "hospital_id" to id,
                        "specialty" to (spec?.name ?: ""),
                        "name" to name,
                        "phone" to phone,
                        "date" to d.format(DATE_FMT),
                        "reason" to reason,
                        "status" to "Requested"
                    )
                )
                saved = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Teal)
        ) { Text(tr("save")) }
        Spacer(modifier = Modifier.height(8.dp))
        Text(tr("appt_prototype_note"), fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.6f))
    }
}

// ---------------------------------------------------------------------------
// DOCTORS
// ---------------------------------------------------------------------------

@Composable
fun DoctorsScreen(lang: Lang, store: Store, go: (Route) -> Unit) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    ScrollScreen {
        Text(tr("fictional_sample"), fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(8.dp))
        Data.doctors.forEach { d ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { go(Route.Chat(d.id)) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👩‍⚕️", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(d.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkTeal, modifier = Modifier.weight(1f))
                        Chip(
                            text = if (d.online) tr("online") else tr("offline"),
                            container = if (d.online) LeafGreen else Color(0xFFE0E0E0),
                            content = if (d.online) Color.White else DarkTeal
                        )
                    }
                    Text(d.specialty.label(lang), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
                    Text(d.hospital, fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
                    Text(d.languages, fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.6f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// CHAT — text only; canned replies; emergency detection → "call 907".
// ---------------------------------------------------------------------------

@Composable
fun ChatScreen(lang: Lang, store: Store, doctorId: String, pop: () -> Unit) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    val doctor = Data.doctorById(doctorId)
    var messages by remember { mutableStateOf(listOf(Msg(false, tr("chat_hello")))) }
    var input by remember { mutableStateOf("") }
    var replyCount by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(doctor?.name ?: tr("chat"), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkTeal)
        if (doctor != null) Text(doctor.specialty.label(lang), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        Text(tr("chat_demo_note"), fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages) { m ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (m.fromMe) Arrangement.End else Arrangement.Start) {
                    Text(
                        text = m.text,
                        modifier = Modifier
                            .background(
                                if (m.fromMe) Teal else Color.White,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        color = if (m.fromMe) Color.White else DarkTeal,
                        fontSize = 14.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val text = input.trim()
                    if (text.isEmpty()) return@Button
                    val reply = if (Data.isEmergency(text)) {
                        tr("chat_emergency_reply")
                    } else {
                        replyCount += 1
                        tr("chat_reply_${((replyCount - 1) % 4) + 1}")
                    }
                    messages = messages + Msg(true, text) + Msg(false, reply)
                    input = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) { Text(tr("send")) }
        }
    }
}

// ---------------------------------------------------------------------------
// PHARMACY — medicine search, stock + sample prices, delivery flag.
// ---------------------------------------------------------------------------

@Composable
fun PharmacyScreen(lang: Lang, store: Store) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }

    ScrollScreen {
        DemoBadge(lang)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(tr("search_medicine"), fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        val q = query.trim().lowercase()
        Data.pharmacies.forEach { p ->
            val matched = if (q.isEmpty()) p.stock.keys.toList()
            else p.stock.keys.filter { it.contains(q) }
            if (matched.isEmpty()) return@forEach
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💊", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkTeal)
                            Text(p.area, fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
                        }
                        if (p.delivery) Chip(tr("delivery"), container = LeafGreen)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    matched.forEach { med ->
                        val (inStock, price) = p.stock[med]!!
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Text(med.replaceFirstChar { it.uppercase() }, fontSize = 14.sp, color = DarkTeal, modifier = Modifier.weight(1f))
                            Text(
                                if (inStock) "${tr("in_stock")} · $price ETB" else tr("out_of_stock"),
                                fontSize = 13.sp,
                                color = if (inStock) LeafGreen else EthRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(onClick = { dial(context, p.phone) }) {
                        Text("📞 ${tr("call")} ${p.phone}")
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// EMERGENCY — 907 / 911 / 991 / 939. Red accents only here (and Blood Bank).
// ---------------------------------------------------------------------------

@Composable
fun EmergencyScreen(lang: Lang, store: Store) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    val context = LocalContext.current
    ScrollScreen {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = EthRed)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🚨", fontSize = 40.sp)
                Text(tr("emergency"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(tr("emergency_numbers_note"), color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        listOf(
            Triple("907", "ambulance", "🚑"),
            Triple("911", "national_emergency", "🚨"),
            Triple("991", "police", "👮"),
            Triple("939", "fire", "🔥")
        ).forEach { (number, labelKey, emoji) ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { dial(context, number) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(number, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = EthRed)
                        Text(tr(labelKey), fontSize = 14.sp, color = DarkTeal)
                    }
                    Text(tr("call_now"), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Teal)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(tr("share_location_note"), fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "I need help. My approximate location is near Meskel Square, Addis Ababa (9.0108, 38.7613). This is a demo location from the Tena Adam app."
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(send, tr("share_location")))
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("📍 ${tr("share_location")}") }
    }
}


// ---------------------------------------------------------------------------
// RECORDS — history, prescriptions, lab results, my appointments.
// Everything is stored only on this phone (not encrypted — see note).
// ---------------------------------------------------------------------------

@Composable
fun RecordsScreen(lang: Lang, store: Store) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    ScrollScreen {
        Text(tr("records_lock"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        Text(tr("local_only"), fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.6f))
        SectionTitle(tr("history"))
        RecordSection(lang, store, Store.KEY_HISTORY)
        SectionTitle(tr("prescriptions"))
        RecordSection(lang, store, Store.KEY_PRESCRIPTIONS)
        SectionTitle(tr("labs"))
        RecordSection(lang, store, Store.KEY_LABS)
        SectionTitle(tr("my_appointments"))
        val appointments = store.list(Store.KEY_APPOINTMENTS)
        if (appointments.isEmpty()) {
            Text(tr("no_data"), color = DarkTeal.copy(alpha = 0.7f), fontSize = 14.sp)
        }
        appointments.forEach { m ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(m["hospital"] ?: "", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkTeal)
                        Text("${m["date"] ?: ""} · ${m["status"] ?: ""}", fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.7f))
                        if (!m["reason"].isNullOrBlank()) Text(m["reason"] ?: "", fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.6f))
                    }
                    OutlinedButton(onClick = { store.remove(Store.KEY_APPOINTMENTS, m["id"] ?: "") }) {
                        Text(tr("delete"), fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RecordSection(lang: Lang, store: Store, key: String) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    var items by remember(key) { mutableStateOf(store.list(key)) }
    var text by remember { mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                if (text.isBlank()) return@Button
                store.add(key, mapOf("text" to text.trim(), "date" to LocalDate.now().format(DATE_FMT)))
                items = store.list(key)
                text = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = Teal)
        ) { Text(tr("add")) }
    }
    Spacer(modifier = Modifier.height(8.dp))
    if (items.isEmpty()) {
        Text(tr("no_data"), color = DarkTeal.copy(alpha = 0.7f), fontSize = 14.sp)
    }
    items.forEach { m ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("• ${m["text"] ?: ""}", fontSize = 14.sp, color = DarkTeal, modifier = Modifier.weight(1f))
            Text(m["date"] ?: "", fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = {
                store.remove(key, m["id"] ?: "")
                items = store.list(key)
            }) { Text(tr("delete"), fontSize = 12.sp) }
        }
    }
}

// ---------------------------------------------------------------------------
// MATERNAL — pregnancy week calculator + full EPI schedule from child DOB.
// ---------------------------------------------------------------------------

@Composable
fun MaternalScreen(lang: Lang, store: Store, go: (Route) -> Unit) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    var dueDate by remember { mutableStateOf(store.getString(Store.KEY_DUE_DATE, "")) }
    var dob by remember { mutableStateOf(store.getString(Store.KEY_CHILD_DOB, "")) }
    var dueError by remember { mutableStateOf("") }
    var dobError by remember { mutableStateOf("") }

    ScrollScreen {
        // Pregnancy week.
        SectionTitle(tr("pregnancy_week"))
        OutlinedTextField(
            value = dueDate,
            onValueChange = { dueDate = it },
            label = { Text("${tr("due_date")} (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val d = parseDate(dueDate)
                if (d == null) {
                    dueError = tr("invalid_date")
                    return@Button
                }
                dueError = ""
                store.putString(Store.KEY_DUE_DATE, d.format(DATE_FMT))
            },
            colors = ButtonDefaults.buttonColors(containerColor = Teal)
        ) { Text(tr("save")) }
        if (dueError.isNotBlank()) Text(dueError, color = EthRed, fontSize = 13.sp)
        val due = parseDate(dueDate)
        if (due != null) {
            val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), due)
            val week = ((280 - daysUntil) / 7).coerceIn(0, 42)
            Spacer(modifier = Modifier.height(8.dp))
            InfoCard {
                Text("${tr("pregnancy_week")}: $week", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DarkTeal)
                Text("${tr("due_date")}: ${due.format(DATE_FMT)}", fontSize = 14.sp, color = DarkTeal)
            }
        }

        // Full EPI vaccination schedule.
        SectionTitle(tr("vaccine_schedule"))
        Text(tr("epi_note"), fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = dob,
            onValueChange = { dob = it },
            label = { Text("${tr("child_dob")} (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val d = parseDate(dob)
                if (d == null) {
                    dobError = tr("invalid_date")
                    return@Button
                }
                dobError = ""
                store.putString(Store.KEY_CHILD_DOB, d.format(DATE_FMT))
            },
            colors = ButtonDefaults.buttonColors(containerColor = Teal)
        ) { Text(tr("save")) }
        if (dobError.isNotBlank()) Text(dobError, color = EthRed, fontSize = 13.sp)
        val birth = parseDate(dob)
        Spacer(modifier = Modifier.height(8.dp))
        Data.vaccineSchedule.forEach { v ->
            val dueV = birth?.plusWeeks(v.weeksAfterBirth.toLong())
            val isDone = dueV != null && dueV.isBefore(LocalDate.now())
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(v.label(lang), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkTeal)
                        Text(v.vaccines, fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.7f))
                        if (dueV != null) {
                            Text(
                                if (isDone) "${tr("due_since")}: ${dueV.format(DATE_FMT)}" else dueV.format(DATE_FMT),
                                fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Chip(
                        text = when {
                            dueV == null -> tr("upcoming")
                            isDone -> tr("done")
                            else -> tr("upcoming")
                        },
                        container = if (isDone) LeafGreen else Color(0xFFFFF5B8),
                        content = if (isDone) Color.White else Color(0xFF6B5B00)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        TileCard(
            emoji = "🤰",
            title = tr("maternity_hospitals"),
            subtitle = null,
            onClick = { go(Route.Finder("pregnancy")) }
        )
    }
}

// ---------------------------------------------------------------------------
// EDUCATION + ARTICLE
// ---------------------------------------------------------------------------

@Composable
fun EducationScreen(lang: Lang, go: (Route) -> Unit) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    ScrollScreen {
        Text(tr("articles"), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkTeal)
        Spacer(modifier = Modifier.height(8.dp))
        Data.articles.forEachIndexed { i, a ->
            TileCard(
                emoji = "📚",
                title = a.title(lang),
                subtitle = a.tag,
                onClick = { go(Route.Article(i)) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun ArticleScreen(lang: Lang, index: Int) {
    val a = Data.articles.getOrNull(index)
    ScrollScreen {
        if (a == null) {
            Text(I18n.get("no_sample_data", lang))
            return@ScrollScreen
        }
        Text(a.title(lang), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DarkTeal)
        Spacer(modifier = Modifier.height(4.dp))
        Text(a.tag, fontSize = 13.sp, color = Teal, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(a.body(lang), fontSize = 15.sp, color = DarkTeal)
        Spacer(modifier = Modifier.height(16.dp))
        DisclaimerText(lang)
    }
}

// ---------------------------------------------------------------------------
// LANGUAGE
// ---------------------------------------------------------------------------

@Composable
fun LanguageScreen(lang: Lang, onSelect: (Lang) -> Unit) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    ScrollScreen {
        Text(tr("lang_note"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(12.dp))
        Lang.values().forEach { l ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(l) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (l == lang) Teal else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🌐", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        l.label, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        color = if (l == lang) Color.White else DarkTeal,
                        modifier = Modifier.weight(1f)
                    )
                    if (l == lang) Text("✓", fontSize = 20.sp, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// HEALTH SYSTEM — how the Ethiopian health system works (3 tiers + HEP).
// ---------------------------------------------------------------------------

@Composable
fun HealthSystemScreen(lang: Lang) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    ScrollScreen {
        Text(tr("hs_title"), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DarkTeal)
        Spacer(modifier = Modifier.height(8.dp))
        InfoCard {
            Text(tr("hs_tier1_title"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
            Text(tr("hs_tier1_body"), fontSize = 14.sp, color = DarkTeal)
        }
        Spacer(modifier = Modifier.height(10.dp))
        InfoCard {
            Text(tr("hs_tier2_title"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
            Text(tr("hs_tier2_body"), fontSize = 14.sp, color = DarkTeal)
        }
        Spacer(modifier = Modifier.height(10.dp))
        InfoCard {
            Text(tr("hs_tier3_title"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
            Text(tr("hs_tier3_body"), fontSize = 14.sp, color = DarkTeal)
        }
        Spacer(modifier = Modifier.height(10.dp))
        InfoCard {
            Text(tr("hep_title"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
            Text(tr("hep_body"), fontSize = 14.sp, color = DarkTeal)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(tr("tier"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
        FacilityTier.values().forEach { t ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Chip(t.label(lang))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ABOUT — logo (Option 3, final), version, disclaimer, built-by.
// ---------------------------------------------------------------------------

@Composable
fun AboutScreen(lang: Lang) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    ScrollScreen {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(R.drawable.tena_adam_logo),
            contentDescription = "Tena Adam logo",
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        Text("ጤናአዳም", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = DarkTeal, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text("Tena Adam", fontSize = 16.sp, color = DarkTeal.copy(alpha = 0.7f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Text(tr("tagline"), fontSize = 15.sp, color = DarkTeal, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        InfoCard {
            Text("${tr("about_version")}: 1.1.0", fontSize = 15.sp, color = DarkTeal)
            Text(tr("about_offline"), fontSize = 14.sp, color = DarkTeal)
            Spacer(modifier = Modifier.height(8.dp))
            Text(tr("about_built_by"), fontSize = 14.sp, color = DarkTeal)
        }
        Spacer(modifier = Modifier.height(12.dp))
        DisclaimerText(lang)
        Text(tr("prototype_v11"), color = DarkTeal.copy(alpha = 0.5f), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}
