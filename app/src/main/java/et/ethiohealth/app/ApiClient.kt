package et.ethiohealth.app

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * v1.3.0 — free, no-key public APIs. Every call has a timeout and is wrapped
 * in try/catch by [RealtimeRepository]; on any failure the app falls back to
 * offline demo data or the last cached result. No API key is hardcoded here.
 * The only keys in the app are the user's own Gemini/Groq keys, pasted in
 * AI settings and stored on the phone (see [OnlineAiAssistant]).
 */
private fun httpClient(): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .writeTimeout(15, TimeUnit.SECONDS)
    .build()

private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
    .baseUrl(baseUrl)
    .client(httpClient())
    .addConverterFactory(GsonConverterFactory.create())
    .build()

// ---------------------------------------------------------------------------
// Overpass API (OpenStreetMap) — nearby hospitals, pharmacies, clinics.
// ---------------------------------------------------------------------------

data class OverpassResponse(val elements: List<OverpassElement> = emptyList())
data class OverpassElement(
    val type: String = "",
    val id: Long = 0,
    val lat: Double? = null,
    val lon: Double? = null,
    val center: OverpassCenter? = null,
    val tags: Map<String, String> = emptyMap()
)
data class OverpassCenter(val lat: Double = 0.0, val lon: Double = 0.0)

interface OverpassApi {
    @FormUrlEncoded
    @POST("interpreter")
    suspend fun query(@Field("data") data: String): OverpassResponse
}

// ---------------------------------------------------------------------------
// Nominatim (OpenStreetMap) — place search / reverse geocoding.
// Nominatim requires a descriptive User-Agent; it is passed per call.
// ---------------------------------------------------------------------------

data class NominatimPlace(
    @SerializedName("display_name") val displayName: String = "",
    val lat: String = "",
    val lon: String = ""
)

interface NominatimApi {
    @GET("search")
    suspend fun search(
        @Query("q") q: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5,
        @Header("User-Agent") userAgent: String = ApiClient.USER_AGENT
    ): List<NominatimPlace>

    @GET("reverse")
    suspend fun reverse(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json",
        @Header("User-Agent") userAgent: String = ApiClient.USER_AGENT
    ): NominatimPlace
}

// ---------------------------------------------------------------------------
// openFDA — medicine label information (generic name, purpose, warnings).
// ---------------------------------------------------------------------------

data class OpenFdaResponse(val results: List<OpenFdaLabel> = emptyList())
data class OpenFdaLabel(
    @SerializedName("openfda") val openFda: OpenFdaFields? = null,
    val purpose: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    @SerializedName("dosage_and_administration") val dosage: List<String> = emptyList()
)
data class OpenFdaFields(
    @SerializedName("generic_name") val genericName: List<String> = emptyList(),
    @SerializedName("brand_name") val brandName: List<String> = emptyList()
)

interface OpenFdaApi {
    @GET("drug/label.json")
    suspend fun search(
        @Query("search") search: String,
        @Query("limit") limit: Int = 3
    ): OpenFdaResponse
}

// ---------------------------------------------------------------------------
// Open-Meteo — air quality + weather, both free and keyless.
// ---------------------------------------------------------------------------

data class AirQualityResponse(
    val current: AirQualityCurrent? = null,
    val hourly: AirQualityHourly? = null
)
data class AirQualityCurrent(
    @SerializedName("pm2_5") val pm25: Double? = null,
    @SerializedName("us_aqi") val usAqi: Int? = null
)
data class AirQualityHourly(
    @SerializedName("pm2_5") val pm25: List<Double> = emptyList(),
    @SerializedName("us_aqi") val usAqi: List<Int> = emptyList()
)

data class WeatherResponse(val current: WeatherCurrent? = null)
data class WeatherCurrent(
    @SerializedName("temperature_2m") val temperature: Double? = null,
    @SerializedName("relative_humidity_2m") val humidity: Int? = null,
    @SerializedName("wind_speed_10m") val windKmh: Double? = null,
    @SerializedName("weather_code") val weatherCode: Int? = null
)

interface OpenMeteoAirApi {
    @GET("v1/air-quality")
    suspend fun get(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "pm2_5,us_aqi",
        @Query("hourly") hourly: String = "pm2_5,us_aqi"
    ): AirQualityResponse
}

interface OpenMeteoWeatherApi {
    @GET("v1/forecast")
    suspend fun get(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code",
        @Query("timezone") timezone: String = "Africa/Addis_Ababa"
    ): WeatherResponse
}

// ---------------------------------------------------------------------------
// Gemini + Groq — online AI, only with the user's own key (never hardcoded).
// ---------------------------------------------------------------------------

data class GeminiRequest(val contents: List<GeminiContent>)
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiPart(val text: String)
data class GeminiResponse(val candidates: List<GeminiCandidate> = emptyList())
data class GeminiCandidate(val content: GeminiContent? = null)

data class GroqRequest(val model: String, val messages: List<GroqMessage>)
data class GroqMessage(val role: String, val content: String)
data class GroqResponse(val choices: List<GroqChoice> = emptyList())
data class GroqChoice(val message: GroqMessage? = null)

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generate(
        @Path("model") model: String,
        @Query("key") key: String,
        @Body body: GeminiRequest
    ): GeminiResponse
}

interface GroqApi {
    @POST("openai/v1/chat/completions")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Body body: GroqRequest
    ): GroqResponse
}

object ApiClient {
    const val USER_AGENT = "TenaAdam/1.3.0 (et.ethiohealth.app; contact via GitHub rebumat/tena-adam)"
    const val ADDIS_LAT = 9.0054
    const val ADDIS_LON = 38.7636

    val overpass: OverpassApi by lazy {
        retrofit("https://overpass-api.de/api/").create(OverpassApi::class.java)
    }
    val nominatim: NominatimApi by lazy {
        retrofit("https://nominatim.openstreetmap.org/").create(NominatimApi::class.java)
    }
    val openFda: OpenFdaApi by lazy {
        retrofit("https://api.fda.gov/").create(OpenFdaApi::class.java)
    }
    val airQuality: OpenMeteoAirApi by lazy {
        retrofit("https://air-quality-api.open-meteo.com/").create(OpenMeteoAirApi::class.java)
    }
    val weather: OpenMeteoWeatherApi by lazy {
        retrofit("https://api.open-meteo.com/").create(OpenMeteoWeatherApi::class.java)
    }
    val gemini: GeminiApi by lazy {
        retrofit("https://generativelanguage.googleapis.com/").create(GeminiApi::class.java)
    }
    val groq: GroqApi by lazy {
        retrofit("https://api.groq.com/").create(GroqApi::class.java)
    }

    /** Overpass QL: hospitals, pharmacies, clinics and doctors within [radiusM]. */
    fun overpassQuery(lat: Double, lon: Double, radiusM: Int = 5000): String =
        "[out:json][timeout:15];" +
            "(node[\"amenity\"~\"hospital|pharmacy|clinic|doctors\"](around:$radiusM,$lat,$lon);" +
            "way[\"amenity\"~\"hospital|pharmacy|clinic\"](around:$radiusM,$lat,$lon););" +
            "out center 30;"
}
