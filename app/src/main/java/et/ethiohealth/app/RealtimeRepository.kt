package et.ethiohealth.app

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * v1.3.0 — live data with graceful offline fallback.
 *
 * Every function returns a [LiveResult]: Live on success, Cached when the
 * network failed but a previous good result was saved, Demo when there is
 * no network and no cache (offline demo hospitals / empty medicine and
 * air-quality results with isLive = false). Nothing here throws to the UI.
 */
sealed class LiveResult<out T> {
    data class Live<T>(val data: T) : LiveResult<T>()
    data class Cached<T>(val data: T) : LiveResult<T>()
    data class Demo<T>(val data: T) : LiveResult<T>()
}

object RealtimeRepository {
    const val FALLBACK_LAT = 9.0054 // Meskel Square, Addis Ababa
    const val FALLBACK_LON = 38.7636

    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * 6371.0 * asin(sqrt(a))
    }

    /** Demo hospitals as live facilities, sorted by distance from [lat]/[lon]. */
    fun demoFacilities(lat: Double, lon: Double): List<LiveFacility> =
        Data.hospitals.map { h ->
            LiveFacility(
                id = "demo-${h.id}", name = h.name, type = h.tier.label(Lang.EN),
                lat = h.lat, lon = h.lon,
                distanceKm = haversineKm(lat, lon, h.lat, h.lon), area = h.area
            )
        }.sortedBy { it.distanceKm }

    private fun cacheNearby(store: Store, items: List<LiveFacility>) {
        val arr = JSONArray()
        items.forEach { f ->
            arr.put(JSONObject().apply {
                put("id", f.id); put("name", f.name); put("type", f.type)
                put("lat", f.lat); put("lon", f.lon)
                put("distanceKm", f.distanceKm); put("area", f.area)
            })
        }
        store.putString(Store.KEY_CACHE_NEARBY, arr.toString())
    }

    private fun readCachedNearby(store: Store, lat: Double, lon: Double): List<LiveFacility>? {
        return try {
            val raw = store.getString(Store.KEY_CACHE_NEARBY, "")
            if (raw.isBlank()) return null
            val arr = JSONArray(raw)
            val out = ArrayList<LiveFacility>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val fLat = o.optDouble("lat", 0.0)
                val fLon = o.optDouble("lon", 0.0)
                out.add(
                    LiveFacility(
                        id = o.optString("id"), name = o.optString("name"),
                        type = o.optString("type"), lat = fLat, lon = fLon,
                        distanceKm = haversineKm(lat, lon, fLat, fLon),
                        area = o.optString("area")
                    )
                )
            }
            if (out.isEmpty()) null else out.sortedBy { it.distanceKm }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun fetchNearby(lat: Double, lon: Double, store: Store): LiveResult<List<LiveFacility>> {
        return try {
            val response = ApiClient.overpass.query(ApiClient.overpassQuery(lat, lon))
            val items = response.elements.mapNotNull { el ->
                val eLat = el.lat ?: el.center?.lat ?: return@mapNotNull null
                val eLon = el.lon ?: el.center?.lon ?: return@mapNotNull null
                val name = el.tags["name"] ?: return@mapNotNull null
                val type = el.tags["amenity"] ?: "hospital"
                LiveFacility(
                    id = "osm-${el.type}-${el.id}", name = name, type = type,
                    lat = eLat, lon = eLon,
                    distanceKm = haversineKm(lat, lon, eLat, eLon),
                    area = el.tags["addr:suburb"] ?: el.tags["addr:city"] ?: ""
                )
            }.sortedBy { it.distanceKm }
            if (items.isEmpty()) {
                readCachedNearby(store, lat, lon)?.let { return LiveResult.Cached(it) }
                LiveResult.Demo(demoFacilities(lat, lon))
            } else {
                cacheNearby(store, items)
                LiveResult.Live(items)
            }
        } catch (_: Exception) {
            readCachedNearby(store, lat, lon)?.let { return LiveResult.Cached(it) }
            LiveResult.Demo(demoFacilities(lat, lon))
        }
    }

    suspend fun searchMedicine(name: String): LiveResult<List<MedicineInfo>> {
        val query = name.trim()
        if (query.isEmpty()) return LiveResult.Demo(emptyList())
        return try {
            // openFDA search syntax: openfda.generic_name:"paracetamol"
            val safe = query.replace("\"", "")
            val response = ApiClient.openFda.search("openfda.generic_name:\"$safe\"")
            val items = response.results.map { label ->
                MedicineInfo(
                    genericName = label.openFda?.genericName?.firstOrNull() ?: query,
                    brandName = label.openFda?.brandName?.firstOrNull() ?: "",
                    purpose = label.purpose.firstOrNull() ?: "",
                    warnings = label.warnings.firstOrNull() ?: "",
                    dosage = label.dosage.firstOrNull() ?: ""
                )
            }
            if (items.isEmpty()) LiveResult.Demo(emptyList()) else LiveResult.Live(items)
        } catch (_: Exception) {
            LiveResult.Demo(emptyList())
        }
    }

    suspend fun fetchAirWeather(
        lat: Double = ApiClient.ADDIS_LAT,
        lon: Double = ApiClient.ADDIS_LON,
        store: Store
    ): LiveResult<AirWeather> {
        return try {
            val air = ApiClient.airQuality.get(lat, lon)
            val weather = ApiClient.weather.get(lat, lon)
            val aqi = air.current?.usAqi
                ?: air.hourly?.usAqi?.firstOrNull()
                ?: -1
            val pm25 = air.current?.pm25
                ?: air.hourly?.pm25?.firstOrNull()
                ?: -1.0
            val result = AirWeather(
                aqi = aqi, pm25 = pm25,
                temperature = weather.current?.temperature ?: Double.NaN,
                humidity = weather.current?.humidity ?: -1,
                windKmh = weather.current?.windKmh ?: Double.NaN,
                weatherCode = weather.current?.weatherCode ?: -1,
                isLive = true
            )
            store.putString(
                Store.KEY_CACHE_AIR,
                JSONObject().apply {
                    put("aqi", result.aqi); put("pm25", result.pm25)
                    put("temperature", result.temperature); put("humidity", result.humidity)
                    put("windKmh", result.windKmh); put("weatherCode", result.weatherCode)
                }.toString()
            )
            LiveResult.Live(result)
        } catch (_: Exception) {
            val cached = try {
                val raw = store.getString(Store.KEY_CACHE_AIR, "")
                if (raw.isBlank()) null else JSONObject(raw).let {
                    AirWeather(
                        aqi = it.optInt("aqi", -1), pm25 = it.optDouble("pm25", -1.0),
                        temperature = it.optDouble("temperature", Double.NaN),
                        humidity = it.optInt("humidity", -1),
                        windKmh = it.optDouble("windKmh", Double.NaN),
                        weatherCode = it.optInt("weatherCode", -1), isLive = false
                    )
                }
            } catch (_: Exception) {
                null
            }
            if (cached != null) LiveResult.Cached(cached)
            else LiveResult.Demo(AirWeather(-1, -1.0, Double.NaN, -1, Double.NaN, -1, isLive = false))
        }
    }

    suspend fun reverseGeocode(lat: Double, lon: Double): String? {
        return try {
            ApiClient.nominatim.reverse(lat, lon).displayName
                .split(",").take(2).joinToString(",").ifBlank { null }
        } catch (_: Exception) {
            null
        }
    }

    // --- Online AI (Gemini / Groq), user's own key only. Returns null on failure. ---

    suspend fun geminiAnswer(apiKey: String, message: String, lang: Lang): String? {
        return try {
            val prompt = "You are the Tena Adam health assistant for Ethiopia. " +
                "Give general health information only, never diagnose or prescribe. " +
                "Keep the answer short. Answer in ${lang.code} when possible. " +
                "User question: $message"
            val response = ApiClient.gemini.generate(
                OnlineAiAssistant.GEMINI_MODEL, apiKey,
                GeminiRequest(listOf(GeminiContent(listOf(GeminiPart(prompt)))))
            )
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun groqAnswer(apiKey: String, message: String, lang: Lang): String? {
        return try {
            val prompt = "You are the Tena Adam health assistant for Ethiopia. " +
                "Give general health information only, never diagnose or prescribe. " +
                "Keep the answer short. Answer in ${lang.code} when possible. " +
                "User question: $message"
            val response = ApiClient.groq.chat(
                "Bearer $apiKey",
                GroqRequest(
                    OnlineAiAssistant.GROQ_MODEL,
                    listOf(GroqMessage("user", prompt))
                )
            )
            response.choices.firstOrNull()?.message?.content?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}
