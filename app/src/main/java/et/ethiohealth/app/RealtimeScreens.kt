package et.ethiohealth.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

// ---------------------------------------------------------------------------
// v1.3.0 live screens. All of them show loading / error / offline states
// and fall back to demo or cached data — they never show a blank screen.
// ---------------------------------------------------------------------------

/** Open Google Maps directions to a place (no API key needed). */
fun openDirections(context: Context, lat: Double, lon: Double, label: String) {
    val uri = Uri.parse(
        "https://www.google.com/maps/dir/?api=1&destination=$lat,$lon"
    )
    context.startActivity(
        Intent(Intent.ACTION_VIEW, uri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    )
}

/** Share a facility as plain text. */
fun shareFacility(context: Context, name: String, area: String, lat: Double, lon: Double) {
    val text = "$name — $area\nhttps://www.google.com/maps/search/?api=1&query=$lat,$lon"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(
        Intent.createChooser(intent, "Share").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    )
}

/** Last-known GPS / network location, or null when unavailable or not permitted. */
fun getLastLocation(context: Context): Pair<Double, Double>? {
    return try {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gps = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val net = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val best = gps ?: net ?: return null
        best.latitude to best.longitude
    } catch (_: Exception) {
        null
    }
}

fun isFavourite(store: Store, refId: String): Boolean =
    store.list(Store.KEY_FAVOURITES).any { it["refId"] == refId }

fun toggleFavourite(store: Store, type: String, refId: String, name: String, area: String): Boolean {
    val existing = store.list(Store.KEY_FAVOURITES).firstOrNull { it["refId"] == refId }
    return if (existing != null) {
        store.remove(Store.KEY_FAVOURITES, existing["id"] ?: "")
        false
    } else {
        store.add(
            Store.KEY_FAVOURITES,
            mapOf("type" to type, "refId" to refId, "name" to name, "area" to area)
        )
        true
    }
}

// ---------------------------------------------------------------------------
// LIVE NEARBY — real Overpass facilities, GPS with Meskel Square fallback.
// ---------------------------------------------------------------------------

@Composable
fun LiveNearbyScreen(lang: Lang, store: Store) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    val context = LocalContext.current
    val vm: LiveNearbyViewModel = viewModel(factory = RealtimeViewModelFactory(store))
    var lat by remember { mutableStateOf(RealtimeRepository.FALLBACK_LAT) }
    var lon by remember { mutableStateOf(RealtimeRepository.FALLBACK_LON) }
    var usingDemoLocation by remember { mutableStateOf(true) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            getLastLocation(context)?.let { (la, lo) ->
                lat = la; lon = lo; usingDemoLocation = false
            }
        }
    }
    LaunchedEffect(lat, lon) { vm.load(lat, lon) }

    ScrollScreen {
        Text(tr("live_nearby"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        if (usingDemoLocation) {
            Text(tr("demo_location"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        } else if (vm.placeName != null) {
            Text(vm.placeName ?: "", fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }) { Text(tr("use_my_location"), fontSize = 12.sp) }
            OutlinedButton(onClick = { vm.load(lat, lon) }) { Text(tr("live_refresh"), fontSize = 12.sp) }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (vm.isLoading) {
            CircularProgressIndicator(color = Teal)
            Text(tr("live_loading"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        }
        if (vm.isOffline) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (vm.isCached) tr("cached_result") else tr("live_offline"),
                fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { vm.load(lat, lon) }) { Text(tr("retry"), fontSize = 12.sp) }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (!vm.isLoading && vm.facilities.isEmpty()) {
            Text(tr("empty"), fontSize = 14.sp, color = DarkTeal.copy(alpha = 0.7f))
        }
        vm.facilities.forEach { f ->
            InfoCard {
                Text(f.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkTeal)
                Text(f.type, fontSize = 12.sp, color = Teal, fontWeight = FontWeight.Bold)
                if (f.area.isNotBlank()) Text(f.area, fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.7f))
                Text("%.1f km".format(f.distanceKm), fontSize = 13.sp, color = DarkTeal)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { openDirections(context, f.lat, f.lon, f.name) }) {
                        Text(tr("directions"), fontSize = 12.sp)
                    }
                    OutlinedButton(onClick = { shareFacility(context, f.name, f.area, f.lat, f.lon) }) {
                        Text(tr("share"), fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Text(tr("live_source"), fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.6f))
    }
}

// ---------------------------------------------------------------------------
// MEDICINE INFO — openFDA drug labels, keyless.
// ---------------------------------------------------------------------------

@Composable
fun MedicineInfoScreen(lang: Lang, store: Store) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    val vm: MedicineInfoViewModel = viewModel(factory = RealtimeViewModelFactory(store))
    var query by remember { mutableStateOf("") }

    ScrollScreen {
        Text(tr("medicine_info"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            label = { Text(tr("medicine_search_hint")) },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { if (query.isNotBlank()) vm.search(query) },
            colors = ButtonDefaults.buttonColors(containerColor = Teal)
        ) { Text(tr("search")) }
        Spacer(modifier = Modifier.height(12.dp))
        if (vm.isLoading) {
            CircularProgressIndicator(color = Teal)
            Text(tr("live_loading"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        }
        if (vm.searched && !vm.isLoading && vm.results.isEmpty()) {
            Text(tr("medicine_no_result"), fontSize = 14.sp, color = DarkTeal)
        }
        vm.results.forEach { m ->
            InfoCard {
                Text(tr("medicine_generic"), fontSize = 12.sp, color = Teal, fontWeight = FontWeight.Bold)
                Text(m.genericName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
                if (m.brandName.isNotBlank()) Text(m.brandName, fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
                if (m.purpose.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(tr("medicine_purpose"), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkTeal)
                    Text(m.purpose, fontSize = 13.sp, color = DarkTeal)
                }
                if (m.warnings.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(tr("medicine_warnings"), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkTeal)
                    Text(m.warnings, fontSize = 13.sp, color = DarkTeal)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        if (vm.results.isNotEmpty()) {
            Text(tr("medicine_source"), fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.6f))
        }
    }
}

// ---------------------------------------------------------------------------
// AIR QUALITY & WEATHER — Open-Meteo for Addis Ababa, keyless.
// ---------------------------------------------------------------------------

@Composable
fun AirQualityScreen(lang: Lang, store: Store) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    val vm: AirWeatherViewModel = viewModel(factory = RealtimeViewModelFactory(store))
    LaunchedEffect(Unit) { vm.load() }

    ScrollScreen {
        Text(tr("air_weather"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = { vm.load() }) { Text(tr("live_refresh"), fontSize = 12.sp) }
        Spacer(modifier = Modifier.height(12.dp))
        if (vm.isLoading) {
            CircularProgressIndicator(color = Teal)
            Text(tr("live_loading"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
        }
        if (vm.isOffline) {
            Text(
                if (vm.isCached) tr("cached_result") else tr("live_offline"),
                fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        val data = vm.data
        if (data != null && (data.aqi >= 0 || !data.temperature.isNaN())) {
            InfoCard {
                Text(tr("air_quality"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
                if (data.aqi >= 0) {
                    Text("AQI: ${data.aqi}", fontSize = 15.sp, color = DarkTeal)
                    Text("PM2.5: ${"%.1f".format(data.pm25)}", fontSize = 14.sp, color = DarkTeal)
                    val adviceKey = when (data.aqiCategory()) {
                        0 -> "aqi_good"
                        1 -> "aqi_moderate"
                        else -> "aqi_unhealthy"
                    }
                    Text(tr(adviceKey), fontSize = 13.sp, color = DarkTeal)
                } else {
                    Text(tr("live_offline"), fontSize = 13.sp, color = DarkTeal.copy(alpha = 0.7f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            InfoCard {
                Text(tr("weather_now"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
                if (!data.temperature.isNaN()) {
                    Text("${tr("temperature")}: ${"%.1f".format(data.temperature)} °C", fontSize = 14.sp, color = DarkTeal)
                }
                if (data.humidity >= 0) {
                    Text("${tr("humidity")}: ${data.humidity}%", fontSize = 14.sp, color = DarkTeal)
                }
                if (!data.windKmh.isNaN()) {
                    Text("${tr("wind")}: ${"%.1f".format(data.windKmh)} km/h", fontSize = 14.sp, color = DarkTeal)
                }
            }
        } else if (!vm.isLoading) {
            Text(tr("live_error"), fontSize = 14.sp, color = DarkTeal)
        }
    }
}

// ---------------------------------------------------------------------------
// FAVOURITES — hospitals and doctors saved in Store.
// ---------------------------------------------------------------------------

@Composable
fun FavouritesScreen(lang: Lang, store: Store) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    var favs by remember { mutableStateOf(store.list(Store.KEY_FAVOURITES)) }

    fun refresh() { favs = store.list(Store.KEY_FAVOURITES) }

    ScrollScreen {
        Text(tr("favourites"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Spacer(modifier = Modifier.height(12.dp))
        if (favs.isEmpty()) {
            Text(tr("fav_empty"), fontSize = 14.sp, color = DarkTeal)
        }
        favs.forEach { m ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(m["name"] ?: "", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkTeal)
                    Text("${m["type"] ?: ""} · ${m["area"] ?: ""}", fontSize = 12.sp, color = DarkTeal.copy(alpha = 0.7f))
                }
                OutlinedButton(onClick = {
                    store.remove(Store.KEY_FAVOURITES, m["id"] ?: "")
                    refresh()
                }) { Text(tr("delete"), fontSize = 12.sp) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(tr("hospital"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
        Data.hospitals.forEach { h ->
            val saved = isFavourite(store, h.id)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(h.name, fontSize = 13.sp, color = DarkTeal, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = {
                    toggleFavourite(store, "hospital", h.id, h.name, h.area)
                    refresh()
                }) { Text(if (saved) tr("remove_favourite") else tr("add_favourite"), fontSize = 11.sp) }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(tr("doctor"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkTeal)
        Data.doctors.forEach { d ->
            val saved = isFavourite(store, d.id)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(d.name, fontSize = 13.sp, color = DarkTeal, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = {
                    toggleFavourite(store, "doctor", d.id, d.name, d.hospital)
                    refresh()
                }) { Text(if (saved) tr("remove_favourite") else tr("add_favourite"), fontSize = 11.sp) }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// BP & SUGAR CHART — simple bar chart of the logged readings (last 7).
// ---------------------------------------------------------------------------

@Composable
fun BpChartScreen(lang: Lang, store: Store) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    val bp = store.list(Store.KEY_BP_LOG).takeLast(7)
    val sugar = store.list(Store.KEY_SUGAR_LOG).takeLast(7)

    ScrollScreen {
        Text(tr("chart_title"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Spacer(modifier = Modifier.height(12.dp))
        if (bp.isEmpty() && sugar.isEmpty()) {
            Text(tr("chart_no_data"), fontSize = 14.sp, color = DarkTeal)
            return@ScrollScreen
        }
        if (bp.isNotEmpty()) {
            Text(tr("chart_bp"), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkTeal)
            BarChart(values = bp.mapNotNull { it["systolic"]?.toFloatOrNull() }, color = Teal)
            bp.reversed().forEach { m ->
                Text("${m["systolic"] ?: ""}/${m["diastolic"] ?: ""} mmHg · ${m["date"] ?: ""}", fontSize = 13.sp, color = DarkTeal)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (sugar.isNotEmpty()) {
            Text(tr("chart_sugar"), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkTeal)
            BarChart(values = sugar.mapNotNull { it["value"]?.toFloatOrNull() }, color = LeafGreen)
            sugar.reversed().forEach { m ->
                Text("${m["value"] ?: ""} mg/dL · ${m["date"] ?: ""}", fontSize = 13.sp, color = DarkTeal)
            }
        }
    }
}

@Composable
private fun BarChart(values: List<Float>, color: Color) {
    if (values.isEmpty()) return
    val max = (values.maxOrNull() ?: 0f).coerceAtLeast(1f)
    Canvas(
        modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp)
    ) {
        val barWidth = size.width / (values.size * 2f)
        values.forEachIndexed { i, v ->
            val barHeight = size.height * (v / max)
            drawRect(
                color = color,
                topLeft = Offset(i * 2f * barWidth + barWidth / 2f, size.height - barHeight),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// PRIVACY — what leaves the phone, and what does not.
// ---------------------------------------------------------------------------

@Composable
fun PrivacyScreen(lang: Lang) {
    val tr: (String) -> String = { I18n.get(it, lang) }
    ScrollScreen {
        Text(tr("privacy_title"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkTeal)
        Spacer(modifier = Modifier.height(12.dp))
        InfoCard { Text(tr("privacy_body"), fontSize = 14.sp, color = DarkTeal) }
        Spacer(modifier = Modifier.height(10.dp))
        InfoCard { Text(tr("privacy_location"), fontSize = 14.sp, color = DarkTeal) }
        Spacer(modifier = Modifier.height(10.dp))
        InfoCard { Text(tr("privacy_network"), fontSize = 14.sp, color = DarkTeal) }
        Spacer(modifier = Modifier.height(10.dp))
        InfoCard { Text(tr("privacy_ai"), fontSize = 14.sp, color = DarkTeal) }
    }
}
