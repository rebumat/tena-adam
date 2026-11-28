package et.ethiohealth.app

/** App language. Order matters: it matches the translation lists in [I18n]. */
enum class Lang(val code: String, val label: String) {
    EN("en", "English"),
    AM("am", "አማርኛ"),
    OM("om", "Afaan Oromoo"),
    TI("ti", "ትግርኛ");

    companion object {
        fun fromCode(code: String): Lang = entries.firstOrNull { it.code == code } ?: EN
    }
}

enum class Specialty(val en: String, val am: String, val om: String, val ti: String) {
    CARDIOLOGY("Cardiology", "የልብ ሕክምና", "Yaala Onnee", "ሕክምና ልቢ"),
    ENDOCRINOLOGY("Endocrinology (Diabetes)", "የስኳርና ሆርሞን ሕክምና", "Yaala Sukkaaraa fi Hormoonii", "ሕክምና ሽኮርን ሆርሞንን"),
    ONCOLOGY("Oncology (Cancer)", "የካንሰር ሕክምና", "Yaala Kaansarii", "ሕክምና መንሽሮ"),
    NEPHROLOGY("Nephrology (Kidney)", "የኩላሊት ሕክምና", "Yaala Kalee", "ሕክምና ኩሊት"),
    OBSTETRICS("Obstetrics & Gynecology", "የማህፀንና ጽንስ ሕክምና", "Yaala Ulfaa fi Da'umsaa", "ሕክምና ጥንስን ሕርስን"),
    PEDIATRICS("Pediatrics (Children)", "የሕፃናት ሕክምና", "Yaala Daa'immanii", "ሕክምና ህጻናት"),
    OPHTHALMOLOGY("Ophthalmology (Eye)", "የዓይን ሕክምና", "Yaala Ijaa", "ሕክምና ዓይኒ"),
    ORTHOPEDICS("Orthopedics (Bone)", "የአጥንት ሕክምና", "Yaala Lafee", "ሕክምና ዓጽሚ"),
    NEUROLOGY("Neurology", "የነርቭ ሕክምና", "Yaala Narvii", "ሕክምና ነርቪ"),
    PSYCHIATRY("Psychiatry (Mental health)", "የአእምሮ ጤና ሕክምና", "Fayyaa Sammuu", "ጥዕና ኣእምሮ"),
    DERMATOLOGY("Dermatology (Skin)", "የቆዳ ሕክምና", "Yaala Gogaa", "ሕክምና ቆርበት"),
    INTERNAL("Internal Medicine", "የውስጥ ደዌ ሕክምና", "Yaala Keessoo", "ሕክምና ውሽጣዊ"),
    SURGERY("General Surgery", "ቀዶ ሕክምና", "Baqaqsanii Yaaluu", "መጥባሕቲ");

    fun label(lang: Lang): String = when (lang) {
        Lang.EN -> en
        Lang.AM -> am
        Lang.OM -> om
        Lang.TI -> ti
    }
}

/** Ethiopian health-system facility tiers (health post → health center → hospital). */
enum class FacilityTier(val key: String) {
    HEALTH_POST("tier_health_post"),
    HEALTH_CENTER("tier_health_center"),
    PRIMARY_HOSPITAL("tier_primary"),
    GENERAL_HOSPITAL("tier_general"),
    REFERRAL_HOSPITAL("tier_referral");

    fun label(lang: Lang): String = I18n.get(key, lang)
}

data class Hospital(
    val id: String,
    val name: String,
    val area: String,
    val lat: Double,
    val lon: Double,
    val phone: String,
    val phoneNote: String,
    val specialties: Set<Specialty>,
    val services: List<String>,
    val emergency24h: Boolean,
    val fictional: Boolean = false,
    val tier: FacilityTier = FacilityTier.GENERAL_HOSPITAL,
    /** Demo bed numbers (labelled as demo in the UI). 0 = unknown. */
    val bedsAvailable: Int = 0,
    val bedsTotal: Int = 0
)

data class Doctor(
    val id: String,
    val name: String,
    val specialty: Specialty,
    val hospital: String,
    val languages: String,
    val online: Boolean
)

data class Pharmacy(
    val name: String,
    val area: String,
    val phone: String,
    val delivery: Boolean,
    /** medicine name (lowercase) -> (in stock, price in ETB, 0 when out of stock) */
    val stock: Map<String, Pair<Boolean, Int>>
)

/** A health-education article. Titles/bodies are translated where provided; English is the fallback. */
data class Article(
    val title: String,
    val tag: String,
    val body: String,
    val titleAm: String = "",
    val titleOm: String = "",
    val titleTi: String = "",
    val bodyAm: String = "",
    val bodyOm: String = "",
    val bodyTi: String = ""
) {
    fun title(lang: Lang): String = when (lang) {
        Lang.AM -> titleAm.ifEmpty { title }
        Lang.OM -> titleOm.ifEmpty { title }
        Lang.TI -> titleTi.ifEmpty { title }
        else -> title
    }

    fun body(lang: Lang): String = when (lang) {
        Lang.AM -> bodyAm.ifEmpty { body }
        Lang.OM -> bodyOm.ifEmpty { body }
        Lang.TI -> bodyTi.ifEmpty { body }
        else -> body
    }
}

data class Vaccine(
    val weeksAfterBirth: Int,
    val label: String,
    val labelAm: String,
    val labelOm: String,
    val labelTi: String,
    val vaccines: String
) {
    fun label(lang: Lang): String = when (lang) {
        Lang.AM -> labelAm
        Lang.OM -> labelOm
        Lang.TI -> labelTi
        else -> label
    }
}

data class Msg(val fromMe: Boolean, val text: String)

/** v1.3.0 live data. All of these come from free, no-key public APIs;
 *  when the network fails the app falls back to the offline demo data. */
data class LiveFacility(
    val id: String,
    val name: String,
    val type: String,
    val lat: Double,
    val lon: Double,
    val distanceKm: Double,
    val area: String = ""
)

data class MedicineInfo(
    val genericName: String,
    val brandName: String,
    val purpose: String,
    val warnings: String,
    val dosage: String = ""
)

data class AirWeather(
    val aqi: Int,
    val pm25: Double,
    val temperature: Double,
    val humidity: Int,
    val windKmh: Double,
    val weatherCode: Int,
    val isLive: Boolean = true
) {
    /** 0 = good, 1 = moderate, 2 = unhealthy (int, not a string key). */
    fun aqiCategory(): Int = if (aqi <= 50) 0 else if (aqi <= 100) 1 else 2
}

data class Tile(val emoji: String, val key: String, val color: Long, val route: Route)

/** A common laboratory test, with a one-line explanation in all 4 languages. */
data class LabTest(
    val nameEn: String, val nameAm: String, val nameOm: String, val nameTi: String,
    val purposeEn: String, val purposeAm: String, val purposeOm: String, val purposeTi: String
) {
    fun name(lang: Lang): String = when (lang) {
        Lang.AM -> nameAm
        Lang.OM -> nameOm
        Lang.TI -> nameTi
        else -> nameEn
    }

    fun purpose(lang: Lang): String = when (lang) {
        Lang.AM -> purposeAm
        Lang.OM -> purposeOm
        Lang.TI -> purposeTi
        else -> purposeEn
    }
}

/** A short information item (used for health campaigns), in all 4 languages. */
data class InfoItem(
    val titleEn: String, val titleAm: String, val titleOm: String, val titleTi: String,
    val bodyEn: String, val bodyAm: String, val bodyOm: String, val bodyTi: String
) {
    fun title(lang: Lang): String = when (lang) {
        Lang.AM -> titleAm
        Lang.OM -> titleOm
        Lang.TI -> titleTi
        else -> titleEn
    }

    fun body(lang: Lang): String = when (lang) {
        Lang.AM -> bodyAm
        Lang.OM -> bodyOm
        Lang.TI -> bodyTi
        else -> bodyEn
    }
}
