package et.ethiohealth.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tena Adam v1.1.0 — entry point.
 *
 * Navigation is a simple in-memory back-stack (like v0.1.0). Bottom bar
 * follows the v0.2 screenshots: Home / Assistant / SOS / Profile & Family.
 * (The design mockup showed a different bar — Home / Appointments / History /
 * Profile — but the v0.2 screenshots are the actual app, so the v0.2 bar wins.
 * The bar items live in [BOTTOM_NAV] below, so swapping is a one-line change.)
 *
 * Brand colours (Option 3 logo, final): bright leaf green → deep teal
 * gradient, white cards, dark teal text. Red is used ONLY for
 * Emergency / Blood Bank. The green/yellow/red flag stripe from v0.1.0
 * is kept under the top bar.
 */
val EthGreen = Color(0xFF078930) // Ethiopian flag green (v0.1.0 primary)
val EthYellow = Color(0xFFFCDD09)
val EthRed = Color(0xFFDA121A)
val LeafGreen = Color(0xFF43A047) // bright leaf green (Option 3 logo)
val Teal = Color(0xFF01918E) // deep teal (Option 3 logo)
val DarkTeal = Color(0xFF0B3D36) // dark text on white cards
val LightBg = Color(0xFFF7F8F3)

sealed class Route {
    object Home : Route()
    data class Finder(val initial: String? = null) : Route()
    data class HospitalDetail(val id: String, val spec: Specialty? = null) : Route()
    data class Appointment(val id: String, val spec: Specialty? = null) : Route()
    object Doctors : Route()
    data class Chat(val doctorId: String) : Route()
    object Pharmacy : Route()
    object Emergency : Route()
    object Records : Route()
    object Maternal : Route()
    object Education : Route()
    data class Article(val index: Int) : Route()
    object Language : Route()
    object NearbyMap : Route()
    object Beds : Route()
    object Ratings : Route()
    object LabTests : Route()
    object BloodBank : Route()
    object Insurance : Route()
    object Payments : Route()
    object FirstAid : Route()
    object Reminders : Route()
    object BpSugar : Route()
    object Calculators : Route()
    object Mental : Route()
    object Profile : Route()
    object News : Route()
    object FutureAi : Route()
    object Assistant : Route()
    object About : Route()
    object HealthSystem : Route()
}

/** Bottom navigation items (icon emoji, label key, target route). */
private val BOTTOM_NAV = listOf(
    Triple("🏠", "home", Route.Home),
    Triple("🤖", "assistant", Route.Assistant),
    Triple("🚨", "sos", Route.Emergency),
    Triple("👨‍👩‍👧", "profile_family", Route.Profile)
)

fun titleKey(route: Route): String = when (route) {
    Route.Home -> "app_name"
    is Route.Finder -> "find_hospital"
    is Route.HospitalDetail -> "hospital"
    is Route.Appointment -> "appointment"
    Route.Doctors -> "doctors"
    is Route.Chat -> "chat"
    Route.Pharmacy -> "pharmacy"
    Route.Emergency -> "emergency"
    Route.Records -> "records"
    Route.Maternal -> "maternal"
    Route.Education -> "education"
    is Route.Article -> "education"
    Route.Language -> "language"
    Route.NearbyMap -> "nearby_map"
    Route.Beds -> "hospital_beds"
    Route.Ratings -> "ratings_reviews"
    Route.LabTests -> "lab_tests"
    Route.BloodBank -> "blood_bank"
    Route.Insurance -> "health_insurance"
    Route.Payments -> "payments"
    Route.FirstAid -> "first_aid"
    Route.Reminders -> "medicine_reminders"
    Route.BpSugar -> "bp_sugar"
    Route.Calculators -> "health_calculators"
    Route.Mental -> "mental_wellbeing"
    Route.Profile -> "profile_family"
    Route.News -> "health_news"
    Route.FutureAi -> "future_ai"
    Route.Assistant -> "assistant"
    Route.About -> "about"
    Route.HealthSystem -> "health_system"
}

/** Open the phone dialer with a number (never auto-dials). */
fun dial(context: Context, number: String) {
    context.startActivity(
        Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = Store(this)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Teal,
                    secondary = EthYellow,
                    tertiary = LightBg,
                    error = EthRed,
                    background = LightBg,
                    surface = Color.White
                )
            ) {
                TenaAdamApp(store)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenaAdamApp(store: Store) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var lang by remember { mutableStateOf(store.lang) }
    var stack by remember { mutableStateOf(listOf<Route>(Route.Home)) }
    val current = stack.last()
    val canPop = stack.size > 1
    val tr: (String) -> String = { I18n.get(it, lang) }

    fun go(route: Route) {
        stack = stack + route
    }
    fun pop() {
        if (stack.size > 1) stack = stack.dropLast(1)
    }
    fun goRoot(route: Route) {
        stack = listOf(route)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        if (current is Route.Home) {
                            Column {
                                Text("ጤናአዳም", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                Text("Tena Adam", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                            }
                        } else {
                            Text(tr(titleKey(current)), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                        }
                    },
                    navigationIcon = {
                        if (canPop) {
                            IconButton(onClick = { pop() }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        }
                    },
                    actions = {
                        // SOS shortcut (red) — always visible.
                        IconButton(onClick = { go(Route.Emergency) }) {
                            Text("🆘", fontSize = 22.sp)
                        }
                        // Language switcher — cycles en → am → om → ti.
                        IconButton(onClick = {
                            val next = Lang.values()[(lang.ordinal + 1) % Lang.values().size]
                            store.lang = next
                            lang = next
                        }) {
                            Text("🌐 " + lang.code.uppercase(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Teal)
                )
                FlagStripe()
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                BOTTOM_NAV.forEach { (emoji, labelKey, route) ->
                    val selected = when (route) {
                        Route.Home -> current is Route.Home
                        Route.Assistant -> current is Route.Assistant
                        Route.Emergency -> current is Route.Emergency
                        Route.Profile -> current is Route.Profile
                        else -> false
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (route is Route.Home) goRoot(Route.Home) else go(route)
                        },
                        icon = { Text(emoji, fontSize = 20.sp) },
                        label = { Text(tr(labelKey), fontSize = 11.sp, color = if (selected) Teal else DarkTeal) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (current !is Route.Assistant) {
                FloatingActionButton(
                    onClick = { go(Route.Assistant) },
                    containerColor = Color(0xFF1565C0)
                ) { Text("🤖", fontSize = 24.sp) }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .background(LightBg)
        ) {
            when (val r = current) {
                Route.Home -> HomeScreen(lang, store, ::go, ::goRoot)
                is Route.Finder -> FinderScreen(lang, store, r.initial, ::go, ::pop)
                is Route.HospitalDetail -> HospitalDetailScreen(lang, store, r.id, r.spec, ::go, ::pop)
                is Route.Appointment -> AppointmentScreen(lang, store, r.id, r.spec, ::pop)
                Route.Doctors -> DoctorsScreen(lang, store, ::go)
                is Route.Chat -> ChatScreen(lang, store, r.doctorId, ::pop)
                Route.Pharmacy -> PharmacyScreen(lang, store)
                Route.Emergency -> EmergencyScreen(lang, store)
                Route.Records -> RecordsScreen(lang, store)
                Route.Maternal -> MaternalScreen(lang, store, ::go)
                Route.Education -> EducationScreen(lang, ::go)
                is Route.Article -> ArticleScreen(lang, r.index)
                Route.Language -> LanguageScreen(lang) { newLang ->
                    store.lang = newLang
                    lang = newLang
                }
                Route.NearbyMap -> NearbyMapScreen(lang, ::go)
                Route.Beds -> HospitalBedsScreen(lang, ::go)
                Route.Ratings -> RatingsScreen(lang, store, ::go)
                Route.LabTests -> LabTestsScreen(lang)
                Route.BloodBank -> BloodBankScreen(lang)
                Route.Insurance -> InsuranceScreen(lang)
                Route.Payments -> PaymentsScreen(lang)
                Route.FirstAid -> FirstAidScreen(lang)
                Route.Reminders -> MedicineRemindersScreen(lang, store)
                Route.BpSugar -> BpSugarScreen(lang, store)
                Route.Calculators -> HealthCalculatorsScreen(lang, store, ::go)
                Route.Mental -> MentalWellbeingScreen(lang)
                Route.Profile -> ProfileScreen(lang, store, ::go)
                Route.News -> HealthNewsScreen(lang)
                Route.FutureAi -> FutureAiScreen(lang)
                Route.Assistant -> AiAssistantScreen(lang, store)
                Route.About -> AboutScreen(lang)
                Route.HealthSystem -> HealthSystemScreen(lang)
            }
        }
    }
}

/** Ethiopian flag stripe (green / yellow / red), 4dp, from v0.1.0. */
@Composable
fun FlagStripe() {
    Row(modifier = Modifier.fillMaxWidth().height(4.dp)) {
        Box(modifier = Modifier.weight(1f).height(4.dp).background(EthGreen))
        Box(modifier = Modifier.weight(1f).height(4.dp).background(EthYellow))
        Box(modifier = Modifier.weight(1f).height(4.dp).background(EthRed))
    }
}

/** "Demo data – verify before use" badge (pale yellow, from v0.1.0). */
@Composable
fun DemoBadge(lang: Lang) {
    Text(
        text = I18n.get("demo_data", lang),
        modifier = Modifier
            .background(Color(0xFFFFF5B8), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = Color(0xFF6B5B00),
        fontSize = 12.sp
    )
}

/** The "does not diagnose" disclaimer, shown on Home and in About. */
@Composable
fun DisclaimerText(lang: Lang) {
    Text(
        text = I18n.get("disclaimer", lang),
        color = DarkTeal.copy(alpha = 0.7f),
        fontSize = 12.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = DarkTeal,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

/** A white rounded card with an emoji icon, title and subtitle. */
@Composable
fun TileCard(
    emoji: String,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkTeal)
                if (subtitle != null) {
                    Text(subtitle, fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.65f))
                }
            }
        }
    }
}

/** Generic white information card. */
@Composable
fun InfoCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) { content() }
    }
}

/** Vertically scrollable screen container. */
@Composable
fun ScrollScreen(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) { content() }
}

/** Hero gradient used on Home (bright leaf green → deep teal). */
val heroGradient: Brush
    get() = Brush.horizontalGradient(listOf(LeafGreen, Teal))
