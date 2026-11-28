package et.ethiohealth.app

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

// ---------------------------------------------------------------------------
// NEARBY MAP — list of facilities by area with approx/demo distances.
// (A real map needs the Google Maps SDK + API key; this list is the
// offline demo version — distances are from the demo location.)
// ---------------------------------------------------------------------------

@Composable
fun NearbyMapScreen(lang: Lang, go: (Route) -> Unit) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    val context = LocalContext.current
    ScrollScreen {
        Text(tr("nearby_title"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Text(tr("nearby_note"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(12.dp))
        val sorted = Data.hospitals.sortedBy { Data.distanceKm(it.lat, it.lon) }
        sorted.forEach { h ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { go(Route.HospitalDetail(h.id)) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🗺️", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(h.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkTeal)
                        Text(h.area, fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.7f))
                        Text(
                            "%.1f km · ${h.tier.label(lang)}".format(Data.distanceKm(h.lat, h.lon)),
                            fontSize = 12.sp, color = Teal, fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedButton(onClick = { openMap(context, h.lat, h.lon, h.name) }) {
                        Text(tr("view_map"), fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// HOSPITAL BEDS — demo bed availability per hospital (marked demo/verify).
// ---------------------------------------------------------------------------

@Composable
fun HospitalBedsScreen(lang: Lang, go: (Route) -> Unit) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    ScrollScreen {
        Text(tr("hospital_beds"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Text(tr("beds_note"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(12.dp))
        Data.hospitals.forEach { h ->
            val (avail, total) = Data.bedsFor(h)
            Card(
                modifier = Modifier.fillMaxWidth().clickable { go(Route.HospitalDetail(h.id)) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🛏️", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(h.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkTeal)
                        Text(h.area, fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.7f))
                    }
                    Text(
                        "$avail / $total",
                        fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        color = if (avail > 0) LeafGreen else EthRed
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// RATINGS & REVIEWS — star ratings + reviews per hospital/doctor, saved
// on the phone. Ratings start empty (no invented review counts).
// ---------------------------------------------------------------------------

@Composable
fun RatingsScreen(lang: Lang, store: Store, go: (Route) -> Unit) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    var selected by remember { mutableStateOf<String?>(null) }
    var stars by remember { mutableStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    val ratings = store.list(Store.KEY_RATINGS)
    fun ratingsFor(target: String) = ratings.filter { it["target"] == target }
    fun avgStars(target: String): String {
        val list = ratingsFor(target)
        if (list.isEmpty()) return ""
        val avg = list.mapNotNull { it["stars"]?.toIntOrNull() }.average()
        return "★ %.1f (%d)".format(avg, list.size)
    }

    ScrollScreen {
        Text(tr("ratings_reviews"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Spacer(modifier = Modifier.height(8.dp))

        // Hospitals.
        Data.hospitals.forEach { h ->
            val target = "hospital:${h.id}"
            RatingRow(
                lang = lang,
                title = h.name,
                subtitle = avgStars(target).ifBlank { tr("no_reviews") },
                reviews = ratingsFor(target),
                selected = selected == target,
                onSelect = { selected = target; saved = false }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        // Doctors.
        Data.doctors.forEach { d ->
            val target = "doctor:${d.id}"
            RatingRow(
                lang = lang,
                title = d.name,
                subtitle = avgStars(target).ifBlank { tr("no_reviews") },
                reviews = ratingsFor(target),
                selected = selected == target,
                onSelect = { selected = target; saved = false }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Add-rating form for the selected target.
        if (selected != null) {
            Spacer(modifier = Modifier.height(4.dp))
            InfoCard {
                Text(tr("your_rating"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { n ->
                        Text(
                            text = if (n <= stars) "★" else "☆",
                            fontSize = 30.sp,
                            color = EthYellow,
                            modifier = Modifier.clickable { stars = n }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text(tr("review_hint"), fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        store.add(
                            Store.KEY_RATINGS,
                            mapOf(
                                "target" to (selected ?: ""),
                                "stars" to stars.toString(),
                                "text" to reviewText.trim(),
                                "date" to LocalDate.now().format(DATE_FMT)
                            )
                        )
                        reviewText = ""
                        saved = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal)
                ) { Text(tr("save")) }
                if (saved) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("✅ ${tr("added")}", color = LeafGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RatingRow(
    lang: Lang,
    title: String,
    subtitle: String,
    reviews: List<Map<String, String>>,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkTeal)
            Text(subtitle, fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.7f))
            reviews.forEach { r ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "★".repeat(r["stars"]?.toIntOrNull() ?: 0) + " " + (r["text"] ?: ""),
                    fontSize = 13.sp, color = DarkTeal
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// LAB TESTS — common tests with a one-line explanation.
// ---------------------------------------------------------------------------

@Composable
fun LabTestsScreen(lang: Lang) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    ScrollScreen {
        Text(tr("lab_tests"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Text(tr("lab_note"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(12.dp))
        Data.labTests.forEach { t ->
            InfoCard {
                Text(t.name(lang), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkTeal)
                Spacer(modifier = Modifier.height(4.dp))
                Text(t.purpose(lang), fontSize = 14.sp, color = DarkTeal)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// BLOOD BANK — who can donate, where (National Blood Bank Service,
// Tikur Anbessa blood bank + regional blood banks). Demo/verify label.
// ---------------------------------------------------------------------------

@Composable
fun BloodBankScreen(lang: Lang) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    ScrollScreen {
        Text(tr("blood_title"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Text(tr("blood_note"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(12.dp))
        InfoCard {
            Text(tr("blood_who"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
            Text(tr("blood_who_body"), fontSize = 14.sp, color = DarkTeal)
        }
        Spacer(modifier = Modifier.height(10.dp))
        InfoCard {
            Text(tr("blood_where"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
            Text(tr("blood_where_body"), fontSize = 14.sp, color = DarkTeal)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(tr("donate_blood"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EthRed)
        Text(tr("find_blood"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
    }
}

// ---------------------------------------------------------------------------
// CBHI — Community-Based Health Insurance.
// No invented prices: costs vary by region, ask at the woreda health office.
// ---------------------------------------------------------------------------

@Composable
fun InsuranceScreen(lang: Lang) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    ScrollScreen {
        Text(tr("cbhi_title"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Spacer(modifier = Modifier.height(12.dp))
        listOf(
            "cbhi_what" to "cbhi_what_body",
            "cbhi_who" to "cbhi_who_body",
            "cbhi_covers" to "cbhi_covers_body",
            "cbhi_how" to "cbhi_how_body",
            "cbhi_cost" to "cbhi_cost_body"
        ).forEach { (titleKey, bodyKey) ->
            InfoCard {
                Text(tr(titleKey), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
                Text(tr(bodyKey), fontSize = 14.sp, color = DarkTeal)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// PAYMENTS — information only. Telebirr, CBE Birr, cash at hospital.
// NO real payment processing, NO keys, NO invented prices.
// ---------------------------------------------------------------------------

@Composable
fun PaymentsScreen(lang: Lang) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    ScrollScreen {
        Text(tr("pay_title"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Text(tr("pay_intro"), fontSize = 14.sp, color = DarkTeal)
        Spacer(modifier = Modifier.height(12.dp))
        listOf("pay_telebirr", "pay_cbe", "pay_cash", "pay_cbhi").forEach { key ->
            InfoCard {
                Text(tr(key), fontSize = 14.sp, color = DarkTeal)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Text(tr("pay_note"), fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.6f))
    }
}

// ---------------------------------------------------------------------------
// FIRST AID — step-by-step guides (bleeding, burns, choking, fainting)
// with "call 907" warnings.
// ---------------------------------------------------------------------------

@Composable
fun FirstAidScreen(lang: Lang) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    val context = LocalContext.current
    ScrollScreen {
        Text(tr("first_aid"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth().clickable { dial(context, "907") },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EthRed)
        ) {
            Text(
                "🚨 ${tr("fa_warning")} — 907",
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Data.firstAidGuides.forEach { (emoji, titleKey, bodyKey) ->
            InfoCard {
                Text("$emoji ${tr(titleKey)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
                Spacer(modifier = Modifier.height(6.dp))
                tr(bodyKey).split("\n").forEach { line ->
                    if (line.isNotBlank()) {
                        Text(line, fontSize = 14.sp, color = DarkTeal)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}


// ---------------------------------------------------------------------------
// MEDICINE REMINDERS — add medicine + time, saved on the phone.
// ---------------------------------------------------------------------------

@Composable
fun MedicineRemindersScreen(lang: Lang, store: Store) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    var items by remember { mutableStateOf(store.list(Store.KEY_REMINDERS)) }
    var med by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    ScrollScreen {
        Text(tr("medicine_reminders"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Text(tr("reminders_note"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = med, onValueChange = { med = it },
            label = { Text(tr("search_medicine")) },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = time, onValueChange = { time = it },
            label = { Text("${tr("reminder_time")} (HH:MM)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (med.isBlank() || time.isBlank()) return@Button
                store.add(Store.KEY_REMINDERS, mapOf("medicine" to med.trim(), "time" to time.trim()))
                items = store.list(Store.KEY_REMINDERS)
                med = ""
                time = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = Teal)
        ) { Text(tr("add")) }
        Spacer(modifier = Modifier.height(12.dp))
        if (items.isEmpty()) {
            Text(tr("no_data"), color = DarkTeal.copy(alpha = 0.7f), fontSize = 14.sp)
        }
        items.forEach { m ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⏰ ${m["medicine"] ?: ""} — ${m["time"] ?: ""}", fontSize = 14.sp, color = DarkTeal, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = {
                    store.remove(Store.KEY_REMINDERS, m["id"] ?: "")
                    items = store.list(Store.KEY_REMINDERS)
                }) { Text(tr("delete"), fontSize = 12.sp) }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// BP & SUGAR TRACKER — log blood pressure AND blood sugar, latest first.
// ---------------------------------------------------------------------------

@Composable
fun BpSugarScreen(lang: Lang, store: Store, go: (Route) -> Unit = {}) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    var sys by remember { mutableStateOf("") }
    var dia by remember { mutableStateOf("") }
    var bpDate by remember { mutableStateOf(LocalDate.now().format(DATE_FMT)) }
    var sugar by remember { mutableStateOf("") }
    var sugarDate by remember { mutableStateOf(LocalDate.now().format(DATE_FMT)) }
    var bpItems by remember { mutableStateOf(store.list(Store.KEY_BP_LOG)) }
    var sugarItems by remember { mutableStateOf(store.list(Store.KEY_SUGAR_LOG)) }

    ScrollScreen {
        Text(tr("bp_title"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Text(tr("bp_note"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = { go(Route.BpChart) }) { Text(tr("chart_title"), fontSize = 13.sp) }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = sys, onValueChange = { sys = it }, label = { Text(tr("systolic")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = dia, onValueChange = { dia = it }, label = { Text(tr("diastolic")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = bpDate, onValueChange = { bpDate = it }, label = { Text("${tr("reading_date")} (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (sys.isBlank() || dia.isBlank()) return@Button
                store.add(Store.KEY_BP_LOG, mapOf("systolic" to sys.trim(), "diastolic" to dia.trim(), "date" to bpDate.trim()))
                bpItems = store.list(Store.KEY_BP_LOG)
                sys = ""
                dia = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = Teal)
        ) { Text(tr("save")) }
        Spacer(modifier = Modifier.height(8.dp))
        bpItems.reversed().forEach { m ->
            Text("${m["systolic"] ?: ""}/${m["diastolic"] ?: ""} mmHg · ${m["date"] ?: ""}", fontSize = 14.sp, color = DarkTeal)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(tr("sugar_title"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Text(tr("bp_sugar_note"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = sugar, onValueChange = { sugar = it }, label = { Text(tr("sugar_value")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = sugarDate, onValueChange = { sugarDate = it }, label = { Text("${tr("reading_date")} (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (sugar.isBlank()) return@Button
                store.add(Store.KEY_SUGAR_LOG, mapOf("value" to sugar.trim(), "date" to sugarDate.trim()))
                sugarItems = store.list(Store.KEY_SUGAR_LOG)
                sugar = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = Teal)
        ) { Text(tr("save")) }
        Spacer(modifier = Modifier.height(8.dp))
        sugarItems.reversed().forEach { m ->
            Text("${m["value"] ?: ""} mg/dL · ${m["date"] ?: ""}", fontSize = 14.sp, color = DarkTeal)
        }
    }
}

// ---------------------------------------------------------------------------
// HEALTH CALCULATORS — BMI + links to pregnancy week & vaccine schedule.
// ---------------------------------------------------------------------------

@Composable
fun HealthCalculatorsScreen(lang: Lang, store: Store, go: (Route) -> Unit) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    ScrollScreen {
        Text(tr("bmi_title"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Text(tr("bmi_calc_note"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text(tr("weight_kg")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text(tr("height_cm")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val w = weight.toDoubleOrNull()
                val hCm = height.toDoubleOrNull()
                if (w == null || hCm == null || hCm <= 0) {
                    result = null
                    return@Button
                }
                val hM = hCm / 100.0
                val bmi = w / (hM * hM)
                val category = when {
                    bmi < 18.5 -> tr("bmi_under")
                    bmi < 25.0 -> tr("bmi_normal")
                    bmi < 30.0 -> tr("bmi_over")
                    else -> tr("bmi_obese")
                }
                result = "${tr("bmi_result")}: %.1f — %s".format(bmi, category)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Teal)
        ) { Text(tr("save")) }
        if (result != null) {
            Spacer(modifier = Modifier.height(12.dp))
            InfoCard {
                Text(result ?: "", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        TileCard(
            emoji = "🤰",
            title = tr("pregnancy_week"),
            subtitle = tr("maternal"),
            onClick = { go(Route.Maternal) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        TileCard(
            emoji = "💉",
            title = tr("vaccine_schedule"),
            subtitle = tr("maternal"),
            onClick = { go(Route.Maternal) }
        )
    }
}

// ---------------------------------------------------------------------------
// MENTAL WELLBEING — breathing exercise (timed inhale/hold/exhale),
// stress tips, "if you feel unsafe, call 907 or 911" note.
// ---------------------------------------------------------------------------

@Composable
fun MentalWellbeingScreen(lang: Lang) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    val context = LocalContext.current
    var breathing by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf("breathe_in") }

    androidx.compose.runtime.LaunchedEffect(breathing) {
        if (!breathing) return@LaunchedEffect
        while (true) {
            phase = "breathe_in"
            kotlinx.coroutines.delay(4000)
            phase = "breathe_hold"
            kotlinx.coroutines.delay(4000)
            phase = "breathe_out"
            kotlinx.coroutines.delay(4000)
        }
    }

    ScrollScreen {
        Text(tr("mental_wellbeing"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Spacer(modifier = Modifier.height(12.dp))
        InfoCard {
            Text(tr("breathe_title"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (breathing) tr(phase) else "🌬️",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Teal,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { breathing = !breathing },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) { Text(if (breathing) tr("breathe_stop") else tr("breathe_start")) }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(tr("stress_tips"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
        listOf("stress_tip_1", "stress_tip_2", "stress_tip_3").forEach { key ->
            Text("• ${tr(key)}", fontSize = 14.sp, color = DarkTeal)
            Spacer(modifier = Modifier.height(4.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth().clickable { dial(context, "907") },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EthRed)
        ) {
            Text(
                "🚨 ${tr("mental_unsafe")}",
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// PROFILE & FAMILY — user profile + family members, saved on the phone.
// ---------------------------------------------------------------------------

@Composable
fun ProfileScreen(
    lang: Lang,
    store: Store,
    go: (Route) -> Unit,
    darkMode: Boolean,
    onDarkMode: (Boolean) -> Unit
) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    var name by remember { mutableStateOf(store.getString(Store.KEY_PROFILE_NAME, "Rebuma")) }
    var phone by remember { mutableStateOf(store.getString(Store.KEY_PROFILE_PHONE, "")) }
    var memberName by remember { mutableStateOf("") }
    var memberRelation by remember { mutableStateOf("") }
    var members by remember { mutableStateOf(store.list(Store.KEY_FAMILY)) }
    var saved by remember { mutableStateOf(false) }

    ScrollScreen {
        Text(tr("profile_title"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        // v1.4.0: Settings — dark mode toggle (persisted in Store).
        Text(tr("settings_title"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🌙", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(tr("dark_mode"), fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Switch(checked = darkMode, onCheckedChange = onDarkMode)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(tr("your_name")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(tr("phone_number")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                store.putString(Store.KEY_PROFILE_NAME, name.trim())
                store.putString(Store.KEY_PROFILE_PHONE, phone.trim())
                saved = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = Teal)
        ) { Text(tr("save")) }
        if (saved) Text("✅ ${tr("added")}", color = LeafGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(20.dp))
        Text(tr("family_title"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = memberName, onValueChange = { memberName = it }, label = { Text(tr("member_name")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = memberRelation, onValueChange = { memberRelation = it }, label = { Text(tr("member_relation")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (memberName.isBlank()) return@Button
                store.add(
                    Store.KEY_FAMILY,
                    mapOf("name" to memberName.trim(), "relation" to memberRelation.trim())
                )
                members = store.list(Store.KEY_FAMILY)
                memberName = ""
                memberRelation = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = Teal)
        ) { Text(tr("add_member")) }
        Spacer(modifier = Modifier.height(8.dp))
        if (members.isEmpty()) {
            Text(tr("no_data"), color = DarkTeal.copy(alpha = 0.7f), fontSize = 14.sp)
        }
        members.forEach { m ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("👨‍👩‍👧 ${m["name"] ?: ""} (${m["relation"] ?: ""})", fontSize = 14.sp, color = DarkTeal, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = {
                    store.remove(Store.KEY_FAMILY, m["id"] ?: "")
                    members = store.list(Store.KEY_FAMILY)
                }) { Text(tr("delete"), fontSize = 12.sp) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // v1.4.0: quick links to the new features.
        TileCard(
            emoji = "📅",
            title = tr("appt_calendar"),
            subtitle = null,
            onClick = { go(Route.ApptCalendar) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        TileCard(
            emoji = "💉",
            title = tr("vac_reminders"),
            subtitle = null,
            onClick = { go(Route.VacReminders) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        TileCard(
            emoji = "🩺",
            title = tr("symptom_checker"),
            subtitle = null,
            onClick = { go(Route.SymptomChecker) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        TileCard(
            emoji = "🚨",
            title = tr("sos"),
            subtitle = null,
            onClick = { go(Route.Sos) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        TileCard(
            emoji = "ℹ️",
            title = tr("about"),
            subtitle = null,
            onClick = { go(Route.About) }
        )
    }
}

// ---------------------------------------------------------------------------
// HEALTH NEWS & CAMPAIGNS — general info, no fake dates.
// ---------------------------------------------------------------------------

@Composable
fun HealthNewsScreen(lang: Lang) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    ScrollScreen {
        Text(tr("health_news"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Text(tr("news_note"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(12.dp))
        Data.campaigns.forEach { c ->
            InfoCard {
                Text(c.title(lang), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
                Spacer(modifier = Modifier.height(4.dp))
                Text(c.body(lang), fontSize = 14.sp, color = DarkTeal)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// FUTURE AI FEATURES — planned features, marked "coming in a later version".
// ---------------------------------------------------------------------------

@Composable
fun FutureAiScreen(lang: Lang) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    ScrollScreen {
        Text(tr("future_ai"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Spacer(modifier = Modifier.height(8.dp))
        Chip(tr("coming_soon"), container = Color(0xFFFFF5B8), content = Color(0xFF6B5B00))
        Spacer(modifier = Modifier.height(12.dp))
        InfoCard {
            Text(tr("futureai_body"), fontSize = 14.sp, color = DarkTeal)
        }
    }
}

// ---------------------------------------------------------------------------
// AI HEALTH ASSISTANT — offline rule-based replies by default.
// Optional online mode: paste your own free API key in Settings below.
// ---------------------------------------------------------------------------

@Composable
fun AiAssistantScreen(lang: Lang, store: Store) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    var messages by remember { mutableStateOf(listOf(Msg(false, tr("assistant_greeting")))) }
    var input by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf(store.getString(Store.KEY_AI_PROVIDER, "offline")) }
    var apiKey by remember { mutableStateOf(store.getString(Store.KEY_AI_KEY, "")) }
    var keySaved by remember { mutableStateOf(false) }
    var isThinking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val assistant: AiAssistant = remember(provider, apiKey) {
        if (provider != "offline" && apiKey.isNotBlank()) {
            OnlineAiAssistant(provider, apiKey)
        } else {
            OfflineAssistant()
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tr("assistant_title"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal, modifier = Modifier.weight(1f))
            Chip(
                text = if (provider != "offline" && apiKey.isNotBlank()) tr("ai_online") else tr("assistant_offline"),
                container = Color(0xFFE8F5E9), content = DarkTeal
            )
        }
        Text(tr("assistant_hint"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { m ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (m.fromMe) Arrangement.End else Arrangement.Start
                ) {
                    Text(
                        text = m.text,
                        modifier = Modifier
                            .background(if (m.fromMe) Teal else Color.White, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        color = if (m.fromMe) Color.White else DarkTeal,
                        fontSize = 14.sp
                    )
                }
            }
        }
        if (isThinking) Text(tr("ai_thinking"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text(tr("assistant_hint"), fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val text = input.trim()
                    if (text.isEmpty() || isThinking) return@Button
                    messages = messages + Msg(true, text)
                    input = ""
                    isThinking = true
                    scope.launch {
                        val reply = withContext(Dispatchers.IO) { assistant.answer(text, lang, store) }
                        messages = messages + Msg(false, reply)
                        isThinking = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) { Text(tr("send")) }
        }

        // Optional online mode (free API key, user's own).
        Spacer(modifier = Modifier.height(16.dp))
        InfoCard {
            Text(tr("ai_settings"), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkTeal)
            Spacer(modifier = Modifier.height(4.dp))
            Text(tr("ai_provider"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("offline", "gemini", "groq").forEach { p ->
                    val selected = provider == p
                    Text(
                        text = p.replaceFirstChar { it.uppercase() },
                        modifier = Modifier
                            .background(if (selected) Teal else Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                            .clickable {
                                provider = p
                                store.putString(Store.KEY_AI_PROVIDER, p)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        color = if (selected) Color.White else DarkTeal,
                        fontSize = 13.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
            if (provider != "offline") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text(tr("ai_key_label")) },
                    placeholder = { Text(tr("ai_key_hint"), fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        store.putString(Store.KEY_AI_KEY, apiKey.trim())
                        keySaved = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal)
                ) { Text(tr("save")) }
                if (keySaved) Text("✅ ${tr("added")}", color = LeafGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (apiKey.isBlank()) Text(tr("ai_not_connected"), fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.6f))
            }
        }
    }
}
