package et.ethiohealth.app

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Simple on-device store, unchanged from v0.1.0: everything is kept in
 * SharedPreferences ("ethiohealth") as JSON arrays of string maps.
 * Nothing leaves the phone. Not encrypted — see the Records screen note.
 */
class Store(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ethiohealth", Context.MODE_PRIVATE)

    fun list(key: String): List<Map<String, String>> {
        val arr = JSONArray(prefs.getString(key, "[]"))
        val out = ArrayList<Map<String, String>>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val map = LinkedHashMap<String, String>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = obj.getString(k)
            }
            out.add(map)
        }
        return out
    }

    fun add(key: String, item: Map<String, String>) {
        val arr = JSONArray(prefs.getString(key, "[]"))
        val obj = JSONObject(item + ("id" to System.currentTimeMillis().toString()))
        arr.put(obj)
        prefs.edit().putString(key, arr.toString()).apply()
    }

    fun remove(key: String, id: String) {
        val kept = list(key).filter { it["id"] != id }
        val arr = JSONArray()
        kept.forEach { arr.put(JSONObject(it)) }
        prefs.edit().putString(key, arr.toString()).apply()
    }

    fun getString(key: String, def: String = ""): String =
        prefs.getString(key, def) ?: def

    fun putString(key: String, v: String) {
        prefs.edit().putString(key, v).apply()
    }

    var lang: Lang
        get() = Lang.fromCode(getString(KEY_LANG, "en"))
        set(value) {
            putString(KEY_LANG, value.code)
        }

    companion object {
        const val KEY_LANG = "lang"
        const val KEY_APPOINTMENTS = "appointments"
        const val KEY_HISTORY = "history"
        const val KEY_PRESCRIPTIONS = "prescriptions"
        const val KEY_LABS = "labs"
        const val KEY_DUE_DATE = "due_date"
        const val KEY_CHILD_DOB = "child_dob"
        const val KEY_REMINDERS = "reminders"
        const val KEY_BP_LOG = "bp_log"
        const val KEY_SUGAR_LOG = "sugar_log"
        const val KEY_RATINGS = "ratings"
        const val KEY_FAMILY = "family"
        const val KEY_PROFILE_NAME = "profile_name"
        const val KEY_PROFILE_PHONE = "profile_phone"
        const val KEY_AI_PROVIDER = "ai_provider"
        const val KEY_AI_KEY = "ai_api_key"
        // v1.3.0: favourites + last-good live-API caches (offline fallback).
        const val KEY_FAVOURITES = "favourites"
        const val KEY_CACHE_NEARBY = "cache_nearby"
        const val KEY_CACHE_AIR = "cache_air"
    }
}
