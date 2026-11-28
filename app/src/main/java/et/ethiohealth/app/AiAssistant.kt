package et.ethiohealth.app

/**
 * AI Health Assistant.
 *
 * Default: OFFLINE rule-based replies — no internet, no API key, no network
 * dependency. The assistant never diagnoses: it gives general information,
 * points to the right screen (hospital finder, pharmacy, CBHI, blood bank...),
 * and detects emergencies ("call 907").
 *
 * Optional ONLINE mode (v1.3.0, wired): if the user pastes his own
 * free API key in Settings, OnlineAiAssistant calls a free-tier LLM
 * via Retrofit (see RealtimeRepository.geminiAnswer / groqAnswer).
 * Supported free tiers to document in README:
 *   - Google Gemini API free tier (model name is a constant below).
 *   - Groq free tier (model name is a constant below).
 * Do NOT invent key values, endpoints or prices. On any failure the
 * assistant falls back to the offline rules, so the app never breaks.
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
 * v1.3.0 online mode — wired via Retrofit in [RealtimeRepository].
 *
 * The user pastes his own free API key in Assistant → Online AI settings;
 * the key is stored on the phone only and never hardcoded. Gemini uses
 * [GEMINI_MODEL], Groq uses [GROQ_MODEL]. Every online call has a timeout;
 * on any failure [answer] falls back to [OfflineAssistant], and the reply
 * always ends with the "does not diagnose" disclaimer.
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
        if (apiKey.isBlank()) return offline.answer(message, lang, store)
        // The chat screen calls this from a coroutine on Dispatchers.IO, so
        // blocking here does not freeze the UI. Any failure falls back.
        val online: String? = try {
            kotlinx.coroutines.runBlocking {
                when (provider) {
                    "groq" -> RealtimeRepository.groqAnswer(apiKey, message, lang)
                    else -> RealtimeRepository.geminiAnswer(apiKey, message, lang)
                }
            }
        } catch (_: Exception) {
            null
        }
        val reply = online?.trim().orEmpty()
        if (reply.isEmpty()) return offline.answer(message, lang, store)
        val disclaimer = I18n.get("disclaimer", lang)
        return if (reply.contains(disclaimer)) reply else "$reply\n\n$disclaimer"
    }
}
