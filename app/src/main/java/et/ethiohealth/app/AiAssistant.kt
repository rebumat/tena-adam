package et.ethiohealth.app

/**
 * AI Health Assistant.
 *
 * Default: OFFLINE rule-based replies — no internet, no API key, no network
 * dependency. The assistant never diagnoses: it gives general information,
 * points to the right screen (hospital finder, pharmacy, CBHI, blood bank...),
 * and detects emergencies ("call 907").
 *
 * Optional ONLINE mode (TODO, clearly marked): if the user pastes his own
 * free API key in Settings, OnlineAiAssistant can call a free-tier LLM.
 * Supported free tiers to document in README:
 *   - Google Gemini API free tier (model name is a constant below).
 *   - Groq free tier (model name is a constant below).
 * Do NOT invent key values, endpoints or prices. The integration is a stub:
 * it throws NotImplementedError with the exact file/method to fill in.
 * Adding it must NOT break the offline build — no new Gradle dependency
 * is added by this file.
 */
interface AiAssistant {
    /** Answer a user message, given the current language and store (for reminders etc.). */
    fun answer(message: String, lang: Lang, store: Store): String
}

class OfflineAssistant : AiAssistant {
    override fun answer(message: String, lang: Lang, store: Store): String {
        val q = message.trim().lowercase()
        val t: (String) -> String = { I18n.get(it, lang) }

        // 1) Emergency always wins.
        if (Data.isEmergency(q)) return t("asst_emergency")

        // 2) Quick keyword routing to the right part of the app.
        if (q.contains("hospital") || q.contains("clinic") || q.contains("doctor") ||
            q.contains("ሕክምና") || q.contains("ሆስፒታል") || q.contains("hospitalii") ||
            q.contains("ሆስፒታል") || Data.matchSpecialties(q).isNotEmpty()
        ) {
            val specs = Data.matchSpecialties(q)
            return if (specs.isEmpty()) t("asst_hospital")
            else t("asst_specialty") + " " +
                specs.joinToString(", ") { it.label(lang) } + ". " + t("asst_hospital")
        }
        if (q.contains("medicine") || q.contains("pharmacy") || q.contains("drug") ||
            q.contains("medhanit") || q.contains("መድኃኒት") || q.contains("farmasii") ||
            q.contains("መድሃኒት")
        ) return t("asst_pharmacy")
        if (q.contains("blood") || q.contains("donat") || q.contains("ደም") ||
            q.contains("dhiiga")
        ) return t("asst_blood")
        if (q.contains("insurance") || q.contains("cbhi") || q.contains("ኢንሹራንስ") ||
            q.contains("inshuraansii")
        ) return t("asst_insurance")
        if (q.contains("vaccine") || q.contains("vaccin") || q.contains("immun") ||
            q.contains("ክትባት") || q.contains("talaallii") || q.contains("ክታበት")
        ) return t("asst_vaccine")
        if (q.contains("reminder") || q.contains("remind") || q.contains("appointment") ||
            q.contains("ቀጠሮ") || q.contains("beeksisa") || q.contains("ቀጠሮ")
        ) return t("asst_reminder")
        if (q.contains("pregnan") || q.contains("mother") || q.contains("baby") ||
            q.contains("child") || q.contains("እርግዝና") || q.contains("እናት") ||
            q.contains("ulfa") || q.contains("haadha") || q.contains("ጥንሲ")
        ) return t("asst_maternal")
        if (q.contains("hello") || q.contains("hi") || q.contains("salam") ||
            q.contains("ሰላም") || q.contains("akkam") || q.contains("ሰላም")
        ) return t("asst_greeting")

        // 3) Fallback.
        return t("asst_default")
    }
}

/**
 * OPTIONAL online mode — TODO, not wired to any network call.
 *
 * To enable (in Android Studio, after the user pastes his own free key in
 * Settings → AI Assistant):
 *   1. Add the HTTP client dependency in app/build.gradle.kts (e.g. OkHttp).
 *   2. Implement [answer] below: POST the user message to the provider's
 *      chat endpoint with [model], using [apiKey] as the bearer token.
 *   3. Keep the disclaimer: the reply must still say it is general
 *      information, not a diagnosis.
 *   4. If the network call fails, fall back to [OfflineAssistant].
 *
 * Free tiers that fit (documented in README, no prices invented):
 *   - Google Gemini API free tier — model: "gemini-2.0-flash".
 *   - Groq free tier — model: "llama-3.3-70b-versatile".
 */
class OnlineAiAssistant(
    private val provider: String,
    private val apiKey: String,
    private val offline: AiAssistant = OfflineAssistant()
) : AiAssistant {

    companion object {
        /** Change these constants when the provider changes its model name. */
        const val GEMINI_MODEL = "gemini-2.0-flash"
        const val GROQ_MODEL = "llama-3.3-70b-versatile"
    }

    val model: String
        get() = when (provider) {
            "gemini" -> GEMINI_MODEL
            "groq" -> GROQ_MODEL
            else -> GEMINI_MODEL
        }

    override fun answer(message: String, lang: Lang, store: Store): String {
        // TODO: implement the online call (see class comment). Until then,
        // always fall back to the offline assistant so the app never breaks.
        return offline.answer(message, lang, store)
    }
}
