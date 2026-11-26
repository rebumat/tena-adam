package et.ethiohealth.app

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Demo data for Tena Adam v1.1.0, recovered from the v0.1.0 build and extended.
 *
 * Honesty rules for this file:
 * - Real hospitals use their real names and areas. Phone numbers come from the
 *   v0.1.0 data ("city directory"); new hospitals have NO phone — the UI says
 *   "not in demo data – look up before calling".
 * - Doctors and pharmacies are labelled "(sample)" / "Sample ..." — fictional.
 * - Bed numbers are DEMO estimates, labelled as such in the UI.
 * - Nothing here is a live system: no prices are real (pharmacy ETB prices are
 *   sample prices from v0.1.0), and the app never claims to diagnose or book
 *   for real.
 */
object Data {
    /** Approximate user location used for "km away" (Meskel Square area, Addis Ababa). */
    const val USER_LAT = 9.0108
    const val USER_LON = 38.7613

    val hospitals: List<Hospital> = listOf(
        Hospital(
            id = "tash", name = "Tikur Anbessa (Black Lion) Specialized Hospital",
            area = "Lideta, Addis Ababa", lat = 9.0205, lon = 38.7497,
            phone = "0115511211", phoneNote = "city directory",
            specialties = setOf(
                Specialty.CARDIOLOGY, Specialty.ONCOLOGY, Specialty.NEPHROLOGY,
                Specialty.PEDIATRICS, Specialty.NEUROLOGY, Specialty.ENDOCRINOLOGY,
                Specialty.INTERNAL, Specialty.SURGERY, Specialty.ORTHOPEDICS,
                Specialty.OBSTETRICS, Specialty.OPHTHALMOLOGY
            ),
            services = listOf(
                "Teaching hospital (AAU)", "Radiotherapy / oncology center",
                "Dialysis", "ICU", "Specialist clinics"
            ),
            emergency24h = true, fictional = false,
            tier = FacilityTier.REFERRAL_HOSPITAL, bedsAvailable = 42, bedsTotal = 120
        ),
        Hospital(
            id = "spaul", name = "St. Paul's Hospital Millennium Medical College",
            area = "Gulele, Addis Ababa", lat = 9.0498, lon = 38.7272,
            phone = "0112768303", phoneNote = "city directory",
            specialties = setOf(
                Specialty.NEPHROLOGY, Specialty.OBSTETRICS, Specialty.PEDIATRICS,
                Specialty.SURGERY, Specialty.INTERNAL, Specialty.CARDIOLOGY,
                Specialty.ONCOLOGY, Specialty.ORTHOPEDICS
            ),
            services = listOf(
                "Kidney transplant program", "Maternal & neonatal care",
                "Dialysis", "Specialist clinics"
            ),
            emergency24h = true, fictional = false,
            tier = FacilityTier.REFERRAL_HOSPITAL, bedsAvailable = 35, bedsTotal = 100
        ),
        Hospital(
            id = "y12", name = "Yekatit 12 Hospital Medical College",
            area = "Arada (Sidist Kilo), Addis Ababa", lat = 9.0436, lon = 38.7598,
            phone = "", phoneNote = "not in demo data – look up before calling",
            specialties = setOf(
                Specialty.DERMATOLOGY, Specialty.SURGERY, Specialty.PEDIATRICS,
                Specialty.INTERNAL, Specialty.OBSTETRICS, Specialty.ORTHOPEDICS
            ),
            services = listOf("Burn care unit", "Plastic surgery", "Outpatient clinics"),
            emergency24h = true, fictional = false,
            tier = FacilityTier.GENERAL_HOSPITAL, bedsAvailable = 28, bedsTotal = 90
        ),
        Hospital(
            id = "alert", name = "ALERT Comprehensive Specialized Hospital",
            area = "Kolfe Keranio, Addis Ababa", lat = 8.9946, lon = 38.71,
            phone = "0113211335", phoneNote = "city directory",
            specialties = setOf(
                Specialty.DERMATOLOGY, Specialty.ORTHOPEDICS, Specialty.SURGERY,
                Specialty.OPHTHALMOLOGY, Specialty.INTERNAL
            ),
            services = listOf(
                "Dermatology & leprosy care", "Trauma / orthopedics",
                "Burn care", "Eye clinic"
            ),
            emergency24h = true, fictional = false,
            tier = FacilityTier.REFERRAL_HOSPITAL, bedsAvailable = 18, bedsTotal = 70
        ),
        Hospital(
            id = "men2", name = "Menelik II Comprehensive Specialized Hospital",
            area = "Arada/Yeka, Addis Ababa", lat = 9.0405, lon = 38.7746,
            phone = "0111234272", phoneNote = "city directory",
            specialties = setOf(
                Specialty.OPHTHALMOLOGY, Specialty.INTERNAL, Specialty.SURGERY,
                Specialty.ORTHOPEDICS, Specialty.NEUROLOGY
            ),
            services = listOf("Eye care", "Infectious disease care", "Outpatient clinics"),
            emergency24h = true, fictional = false,
            tier = FacilityTier.REFERRAL_HOSPITAL, bedsAvailable = 22, bedsTotal = 80
        ),
        Hospital(
            id = "amanuel", name = "Amanuel Mental Specialized Hospital",
            area = "Addis Ketema, Addis Ababa", lat = 9.0335, lon = 38.7346,
            phone = "0112131516", phoneNote = "city directory",
            specialties = setOf(Specialty.PSYCHIATRY, Specialty.NEUROLOGY),
            services = listOf("Psychiatric outpatient & inpatient care", "Counselling"),
            emergency24h = true, fictional = false,
            tier = FacilityTier.REFERRAL_HOSPITAL, bedsAvailable = 15, bedsTotal = 60
        ),
        Hospital(
            id = "gandhi", name = "Gandhi Memorial Hospital",
            area = "Kirkos, Addis Ababa", lat = 9.0226, lon = 38.7572,
            phone = "0115518185", phoneNote = "city directory",
            specialties = setOf(Specialty.OBSTETRICS, Specialty.PEDIATRICS),
            services = listOf("Maternity & delivery", "Antenatal care", "Neonatal care"),
            emergency24h = true, fictional = false,
            tier = FacilityTier.GENERAL_HOSPITAL, bedsAvailable = 12, bedsTotal = 45
        ),
        Hospital(
            id = "demo1", name = "Sample Bole Heart & Diabetes Clinic (fictional)",
            area = "Bole, Addis Ababa", lat = 8.995, lon = 38.79,
            phone = "0110000001", phoneNote = "fictional",
            specialties = setOf(
                Specialty.CARDIOLOGY, Specialty.ENDOCRINOLOGY, Specialty.INTERNAL
            ),
            services = listOf("ECG / Echo", "Diabetes clinic", "Online consultation"),
            emergency24h = false, fictional = true,
            tier = FacilityTier.HEALTH_CENTER, bedsAvailable = 6, bedsTotal = 10
        ),
        Hospital(
            id = "demo2", name = "Sample Kality Kidney & Dialysis Center (fictional)",
            area = "Akaki Kality, Addis Ababa", lat = 8.93, lon = 38.77,
            phone = "0110000002", phoneNote = "fictional",
            specialties = setOf(Specialty.NEPHROLOGY),
            services = listOf("Hemodialysis", "Nephrology clinic"),
            emergency24h = false, fictional = true,
            tier = FacilityTier.HEALTH_CENTER, bedsAvailable = 4, bedsTotal = 8
        ),
        Hospital(
            id = "demo3", name = "Sample Megenagna Eye & Child Clinic (fictional)",
            area = "Yeka, Addis Ababa", lat = 9.02, lon = 38.8,
            phone = "0110000003", phoneNote = "fictional",
            specialties = setOf(Specialty.OPHTHALMOLOGY, Specialty.PEDIATRICS),
            services = listOf("Eye exams", "Child wellness & vaccination"),
            emergency24h = false, fictional = true,
            tier = FacilityTier.HEALTH_CENTER, bedsAvailable = 5, bedsTotal = 10
        ),
        // Regional referral hospitals (v1.1.0). Real names; phones/services are
        // demo data — the UI says to verify before use.
        Hospital(
            id = "gondar", name = "University of Gondar Comprehensive Specialized Hospital",
            area = "Gondar, Amhara", lat = 12.6030, lon = 37.4670,
            phone = "", phoneNote = "not in demo data – look up before calling",
            specialties = setOf(
                Specialty.INTERNAL, Specialty.SURGERY, Specialty.OBSTETRICS,
                Specialty.PEDIATRICS, Specialty.OPHTHALMOLOGY
            ),
            services = listOf(
                "Referral & teaching hospital", "Emergency & surgery", "Maternal care"
            ),
            emergency24h = true, fictional = false,
            tier = FacilityTier.REFERRAL_HOSPITAL, bedsAvailable = 30, bedsTotal = 110
        ),
        Hospital(
            id = "jimma", name = "Jimma University Medical Center",
            area = "Jimma, Oromia", lat = 7.6740, lon = 36.8340,
            phone = "", phoneNote = "not in demo data – look up before calling",
            specialties = setOf(
                Specialty.INTERNAL, Specialty.SURGERY, Specialty.OBSTETRICS,
                Specialty.PEDIATRICS, Specialty.CARDIOLOGY
            ),
            services = listOf(
                "Referral & teaching hospital", "Emergency & surgery", "Specialist clinics"
            ),
            emergency24h = true, fictional = false,
            tier = FacilityTier.REFERRAL_HOSPITAL, bedsAvailable = 25, bedsTotal = 95
        ),
        Hospital(
            id = "hawassa", name = "Hawassa University Comprehensive Specialized Hospital",
            area = "Hawassa, Sidama", lat = 7.0500, lon = 38.4800,
            phone = "", phoneNote = "not in demo data – look up before calling",
            specialties = setOf(
                Specialty.INTERNAL, Specialty.OBSTETRICS, Specialty.PEDIATRICS,
                Specialty.SURGERY, Specialty.ORTHOPEDICS
            ),
            services = listOf(
                "Referral & teaching hospital", "Maternal & neonatal care", "Emergency care"
            ),
            emergency24h = true, fictional = false,
            tier = FacilityTier.REFERRAL_HOSPITAL, bedsAvailable = 20, bedsTotal = 85
        ),
        Hospital(
            id = "bahirdar", name = "Felege Hiwot Comprehensive Specialized Hospital",
            area = "Bahir Dar, Amhara", lat = 11.5950, lon = 37.3900,
            phone = "", phoneNote = "not in demo data – look up before calling",
            specialties = setOf(
                Specialty.INTERNAL, Specialty.SURGERY, Specialty.OBSTETRICS,
                Specialty.PEDIATRICS, Specialty.OPHTHALMOLOGY
            ),
            services = listOf("Referral hospital", "Emergency & surgery", "Eye clinic"),
            emergency24h = true, fictional = false,
            tier = FacilityTier.REFERRAL_HOSPITAL, bedsAvailable = 24, bedsTotal = 90
        ),
        Hospital(
            id = "mekelle", name = "Ayder Comprehensive Specialized Hospital",
            area = "Mekelle, Tigray", lat = 13.4900, lon = 39.4700,
            phone = "", phoneNote = "not in demo data – look up before calling",
            specialties = setOf(
                Specialty.INTERNAL, Specialty.SURGERY, Specialty.OBSTETRICS,
                Specialty.PEDIATRICS, Specialty.ONCOLOGY
            ),
            services = listOf(
                "Referral & teaching hospital", "Oncology care", "Emergency care"
            ),
            emergency24h = true, fictional = false,
            tier = FacilityTier.REFERRAL_HOSPITAL, bedsAvailable = 16, bedsTotal = 75
        )
    )

    /** Sample doctors — fictional people, labelled "(sample)". Never real doctors. */
    val doctors: List<Doctor> = listOf(
        Doctor("d1", "Dr. Abebe Kebede (sample)", Specialty.CARDIOLOGY, "Sample Bole Heart & Diabetes Clinic", "Amharic, English", true),
        Doctor("d2", "Dr. Chaltu Gemechu (sample)", Specialty.PEDIATRICS, "Sample Megenagna Eye & Child Clinic", "Afaan Oromoo, Amharic, English", true),
        Doctor("d3", "Dr. Hanna Tesfaye (sample)", Specialty.OBSTETRICS, "Sample Women's Clinic", "Amharic, Tigrinya, English", false),
        Doctor("d4", "Dr. Tolosa Bekele (sample)", Specialty.ENDOCRINOLOGY, "Sample Bole Heart & Diabetes Clinic", "Afaan Oromoo, English", true),
        Doctor("d5", "Dr. Selam Gebremariam (sample)", Specialty.PSYCHIATRY, "Sample Wellness Center", "Tigrinya, Amharic, English", true),
        Doctor("d6", "Dr. Yonas Alemu (sample)", Specialty.INTERNAL, "Sample Family Clinic", "Amharic, English", false),
        Doctor("d7", "Dr. Makeda Haile (sample)", Specialty.OBSTETRICS, "Sample Women's Clinic", "Amharic, English", true),
        Doctor("d8", "Dr. Dawit Teshome (sample)", Specialty.NEUROLOGY, "Sample Family Clinic", "Amharic, Tigrinya, English", false),
        Doctor("d9", "Dr. Tigist Alemayehu (sample)", Specialty.DERMATOLOGY, "Sample Megenagna Eye & Child Clinic", "Amharic, English", true),
        Doctor("d10", "Dr. Bekele Tadesse (sample)", Specialty.SURGERY, "Sample Bole Heart & Diabetes Clinic", "Afaan Oromoo, Amharic", false)
    )

    private fun stock(
        paracetamol: Pair<Boolean, Int>, amoxicillin: Pair<Boolean, Int>,
        metformin: Pair<Boolean, Int>, insulin: Pair<Boolean, Int>,
        ors: Pair<Boolean, Int>, amlodipine: Pair<Boolean, Int>,
        salbutamol: Pair<Boolean, Int>, iron: Pair<Boolean, Int>
    ): Map<String, Pair<Boolean, Int>> = mapOf(
        "paracetamol" to paracetamol,
        "amoxicillin" to amoxicillin,
        "metformin" to metformin,
        "insulin" to insulin,
        "ors" to ors,
        "amlodipine" to amlodipine,
        "salbutamol inhaler" to salbutamol,
        "iron folic acid" to iron
    )

    /** Sample pharmacies — fictional, with sample ETB prices from v0.1.0. */
    val pharmacies: List<Pharmacy> = listOf(
        Pharmacy("Sample Piassa Pharmacy", "Arada", "0110000101", true, stock(true to 45, true to 300, true to 180, false to 0, true to 25, true to 150, false to 0, true to 60)),
        Pharmacy("Sample Bole Medhanit Pharmacy", "Bole", "0110000102", true, stock(true to 50, false to 0, true to 190, true to 950, true to 30, true to 160, true to 420, true to 65)),
        Pharmacy("Sample Merkato Kenema Pharmacy", "Addis Ketema", "0110000103", false, stock(true to 40, true to 110, false to 0, true to 900, true to 20, false to 0, true to 400, true to 55)),
        Pharmacy("Sample Sarbet Pharmacy", "Kolfe Keranio", "0110000104", true, stock(true to 48, true to 125, true to 175, false to 0, false to 0, true to 155, true to 430, false to 0)),
        Pharmacy("Sample Kirkos Pharmacy", "Kirkos", "0110000105", true, stock(true to 46, true to 120, true to 185, false to 0, true to 28, true to 152, false to 0, true to 62)),
        Pharmacy("Sample Yeka Pharmacy", "Yeka", "0110000106", false, stock(true to 44, false to 0, true to 178, true to 920, true to 22, true to 148, true to 410, true to 58)),
        Pharmacy("Sample Akaki Pharmacy", "Akaki Kality", "0110000107", true, stock(true to 42, true to 115, false to 0, true to 880, true to 24, false to 0, true to 405, true to 60)),
        Pharmacy("Sample Addis Ketema Pharmacy", "Addis Ketema", "0110000108", false, stock(true to 47, true to 130, true to 182, false to 0, true to 26, true to 158, false to 0, true to 64))
    )

    /** Ethiopia routine EPI schedule (weeks after birth), from v0.1.0. */
    val vaccineSchedule: List<Vaccine> = listOf(
        Vaccine(0, "At birth", "በልደት ጊዜ", "Yeroo dhalootaa", "ኣብ እዋን ልደት", "BCG, OPV0 (polio)"),
        Vaccine(6, "6 weeks", "6 ሳምንታት", "Torban 6", "6 ሰሙናት", "Pentavalent 1, PCV 1, OPV 1, Rota 1"),
        Vaccine(10, "10 weeks", "10 ሳምንታት", "Torban 10", "10 ሰሙናት", "Pentavalent 2, PCV 2, OPV 2, Rota 2"),
        Vaccine(14, "14 weeks", "14 ሳምንታት", "Torban 14", "14 ሰሙናት", "Pentavalent 3, PCV 3, OPV 3, IPV"),
        Vaccine(39, "9 months", "9 ወራት", "Ji'a 9", "9 ኣዋርሕ", "Measles 1 (MCV1)"),
        Vaccine(65, "15 months", "15 ወራት", "Ji'a 15", "15 ኣዋርሕ", "Measles 2 (MCV2)")
    )

    val articles: List<Article> = listOf(
        Article(
            "Know your blood pressure", "Heart",
            "High blood pressure often has no symptoms. Check it at a health center at least once a year after age 30. Reduce salt, stay active, avoid tobacco and limit alcohol. If you are on medicine, take it every day even when you feel well.",
            titleAm = "የደም ግፊትዎን ይወቁ", titleOm = "Dhiibbaa dhiigaa kee beeki", titleTi = "ጸቕጢ ደምካ ፈልጥ"
        ),
        Article(
            "Living well with diabetes", "Diabetes",
            "Eat regular meals with vegetables, legumes and whole grains such as teff; limit sugary drinks. Walk 30 minutes most days. Check your feet daily and attend follow-up visits for blood sugar, eye and kidney checks.",
            titleAm = "ከስኳር በሽታ ጋር በጥሩ ሁኔታ መኖር", titleOm = "Dhukkuba sukkaaraa waliin gaarii jiraachuu", titleTi = "ምስ ሕማም ሽኮር ብጽቡቕ ምንባር"
        ),
        Article(
            "Preventing malaria", "Infection",
            "When travelling to lowland areas, sleep under an insecticide-treated bed net and seek testing quickly if you develop fever. Pregnant women and young children are at higher risk and should be seen early.",
            titleAm = "ወባን መከላከል", titleOm = "Busaa ittisu", titleTi = "ወባ ምክልኻል"
        ),
        Article(
            "Oral rehydration for diarrhoea", "Child health",
            "Give ORS (oral rehydration solution) and zinc to children with diarrhoea, and keep breastfeeding. Go to a health facility immediately if the child is very sleepy, cannot drink, has blood in stool or is vomiting everything.",
            titleAm = "ለተቅማጥ የአፍ መድኃኒት (ORS)", titleOm = "ORS akkaataa afaanii", titleTi = "ORS ብኣፍ"
        ),
        Article(
            "Antenatal care visits", "Pregnancy",
            "Start antenatal care early in pregnancy and attend all scheduled visits. Take iron-folic acid as advised, and deliver at a health facility. Danger signs such as bleeding, severe headache, blurred vision or fits need emergency care.",
            titleAm = "የእርግዝና ክትትል ጉብኝቶች", titleOm = "Daawwannaa ulfaa", titleTi = "ምብጻሕ ጥንሲ"
        ),
        Article(
            "Mental health matters", "Mental health",
            "Feeling sad, anxious or unable to sleep for weeks is common and treatable. Talk to someone you trust and seek help from a health professional. If someone is thinking about harming themselves, get help immediately.",
            titleAm = "የአእምሮ ጤና አስፈላጊ ነው", titleOm = "Fayyaan sammuu barbaachisaa dha", titleTi = "ጥዕና ኣእምሮ ኣገዳሲ እዩ"
        ),
        Article(
            "Hand washing saves lives", "Prevention",
            "Wash hands with soap and water before eating, before preparing food, after using the toilet and after caring for a sick person. This helps prevent diarrhoea, respiratory infections and other diseases.",
            titleAm = "እጅ መታጠብ ሕይወት ያድናል", titleOm = "Harka dhiqachuun lubbuu baraara", titleTi = "ኢድ ምሕጻብ ሂወት የድሕን"
        ),
        Article(
            "Tuberculosis: cough that does not go away", "Infection",
            "A cough lasting more than two weeks, night sweats, weight loss or coughing blood can be signs of tuberculosis (TB). TB is curable with free treatment at public health facilities. Go to a health center early for testing — do not wait.",
            titleAm = "ሳንባ ነቀርሳ (TB)፡ የማያልቅ ሳል",
            titleOm = "Bakteriyaa sombaa (TB): qufa hin dhaabbane",
            titleTi = "ሳንባ ነቀርሳ (TB)፡ ዘይሓልፍ ስዓል",
            bodyAm = "ከሁለት ሳምንት በላይ የሚቆይ ሳል፣ የሌሊት ላብ፣ የክብደት መቀነስ ወይም ደም ማሳል የሳንባ ነቀርሳ (TB) ምልክቶች ሊሆኑ ይችላሉ። TB በሕዝብ ጤና ተቋማት በነጻ ሕክምና ይድናል። ለመመርመር ቀደም ብለው ወደ ጤና ማዕከል ይሂዱ — አይጠብቁ።",
            bodyOm = "Qufni torban lama ol ture, dafqa halkanii, hir'ina ulfaatinaa ykn dhiiga qufaachuu mallattoo TB ta'uu danda'a. TB yaala bilisaa dhaabbilee fayyaa mootummaa keessatti fayyuu danda'a. Qorannoo dursee gara giddu-gala fayyaa deemi — hin eeggatin.",
            bodyTi = "ስዓል ካብ ክልተ ሰሙን ንላዕሊ ዝጸንሐ፣ ላብ ለይቲ፣ ምንካይ ክብደት ወይ ደም ምስዓል ምልክት ሳንባ ነቀርሳ (TB) ክኸውን ይኽእል። TB ኣብ ህዝባዊ ተቋማት ጥዕና ብነጻ ሕክምና ይሓዊ። ንምርመራ ቀዳማይ ናብ ማእከል ጥዕና ኪድ — ኣይትጽበ።"
        ),
        Article(
            "HIV testing and treatment", "Infection",
            "HIV testing is free and confidential at public health facilities. If the test is positive, treatment (ART) is also free and lets people live long, healthy lives. Knowing your status protects you and your family — get tested.",
            titleAm = "የኤችአይቪ ምርመራ እና ሕክምና",
            titleOm = "Qorannoo fi yaala HIV",
            titleTi = "ምርመራን ሕክምናን HIV",
            bodyAm = "የኤችአይቪ ምርመራ በሕዝብ ጤና ተቋማት ነጻ እና በሚስጥር ነው። ምርመራው አዎንታዊ ከሆነ ሕክምናው (ART) እንዲሁ ነጻ ነው እና ሰዎች ረጅምና ጤናማ ሕይወት እንዲኖሩ ያደርጋል። ሁኔታዎን ማወቅ እርስዎን እና ቤተሰብዎን ይጠብቃል — ይመረመሩ።",
            bodyOm = "Qorannoon HIV dhaabbilee fayyaa mootummaa keessatti bilisaa fi iccitii dha. Yoo qorannoon pozatiivii ta'e, yaalli (ART) bilisa dha, namoonni jireenya dheeraa fi fayya qabeessa jiraachuu danda'u. Haala kee beekuun si fi maatii kee tiksa — qorannoo godhadhu.",
            bodyTi = "ምርመራ HIV ኣብ ህዝባዊ ተቋማት ጥዕና ብነጻን ብምስጢርን እዩ። ምርመራ ኣዎንታዊ እንተኾይኑ ሕክምና (ART) እውን ብነጻ እዩ፣ ሰባት ነዊሕን ጥዑይን ሂወት ክነብሩ የገብር። ኩነታትካ ምፍላጥ ነብስኻን ስድራቤትካን ይከላኸል — ተመርመር።"
        ),
        Article(
            "Eating well with local foods", "Nutrition",
            "A healthy plate has vegetables and legumes such as lentils and chickpeas, whole grains such as teff and barley, and a little fruit. Limit sugary drinks, too much salt and fried foods. Breastfeed babies for the first six months.",
            titleAm = "በአካባቢ ምግቦች በጥሩ ሁኔታ መመገብ",
            titleOm = "Nyaata naannoo gaarii nyaachuu",
            titleTi = "ብኣካባቢ ምግቢ ብጽቡቕ ምምጋብ",
            bodyAm = "ጤናማ ምግብ አትክልቶች እና ጥራጥሬዎች እንደ ምስር እና ሽምብራ፣ ሙሉ እህሎች እንደ ጤፍ እና ገብስ እና ትንሽ ፍራፍሬ አለው። ጣፋጭ መጠጦችን፣ ከመጠን በላይ ጨው እና የተጠበሱ ምግቦችን ይገድቡ። ሕፃናትን ለመጀመሪያ ስድስት ወራት ጡት ያጥቡ።",
            bodyOm = "Gabateen fayya qabeessi kuduraafi baaqelaa akka misiraa fi shuumburaa, midhaan guutuu akka xaafii fi garbuu, fi firii xiqqoo qaba. Dhugaatii sukkaara qabu, soogidda baay'ee fi nyaata faranjii xiqqeessi. Daa'imman ji'a jaha jalqabaa harma hodhisi.",
            bodyTi = "ጥዑይ ምግቢ ኣትክልትን ጥራጥረን ከም ምስርን ሽምብራን፣ ምሉእ እህል ከም ጣፍን ስገምን፣ ትንሽ ፍረ ኣለዎ። ጣፋጭ መስተታት፣ ብዙሕ ጨውን ዝተጠብሰ ምግብን ደረት። ህጻናት ንቀዳማይ ሹዱሽ ኣዋርሕ ጡብ ኣጥብእ።"
        ),
        Article(
            "Safe drinking water", "Prevention",
            "Drink water from a safe source. If you are not sure the water is safe, boil it and let it cool, or treat it as your health extension worker advises. Store drinking water in a clean, covered container.",
            titleAm = "ደህንነቱ የተጠበቀ የመጠጥ ውሃ",
            titleOm = "Bishaan dhugaatii nageenya qabu",
            titleTi = "ድሕነት ዘለዎ ማይ መስተ",
            bodyAm = "ከደህንነቱ የተጠበቀ ምንጭ ውሃ ይጠጡ። ውሃው ደህንነቱ የተጠበቀ መሆኑን እርግጠኛ ካልሆኑ ያፍሱት እና እንዲቀዘቅዝ ያድርጉት፣ ወይም የጤና ኤክስቴንሽን ሰራተኛዎ እንደሚያሳይዎት ያዙት። የመጠጥ ውሃ በንጹሕ እና በተሸፈነ መያዣ ውስጥ ያስቀምጡ።",
            bodyOm = "Bishaan madda nageenya qabu irraa dhugi. Bishaanichi nageenya qabaachuu isaa yoo hin beekne, danfisi akka qabbanaa'u godhi, ykn akka hojjetaan fayyaa babal'inaa si gorseetti. Bishaan dhugaatii meeshaa qulqulluu fi uwwifame keessatti kuusi.",
            bodyTi = "ማይ ካብ ድሕነት ዘለዎ ምንጪ ስተ። ማይ ድሕነት ዘለዎ ምዃኑ እንተዘይተረጋጊጽካ ኣፍልጦ ኣቀዝቅዞ፣ ወይ ሰራሕተኛ ምዝርጋሕ ጥዕናኻ ከም ዝነገረካ ኣዚዮ። ማይ መስተ ኣብ ንጹሕን ዝተሸፈነን መያዣ ኣቐምጦ።"
        )
    )

    val labTests: List<LabTest> = listOf(
        LabTest(
            "Complete blood count (CBC)", "ሙሉ የደም ቆጠራ (CBC)", "Lakkaawwii dhiigaa guutuu (CBC)", "ምሉእ ቍጽሪ ደም (CBC)",
            "Checks red and white blood cells — used when you have fever, weakness or infection.",
            "የደም ሴሎችን ይመረምራል — ትኩሳት፣ ድካም ወይም ኢንፌክሽን ሲኖር ይጠቅማል።",
            "Seelii dhiigaa diimaa fi adii sakatta'a — yoo ho'a qaamaa, dadhabbii ykn infekshinii qabdu fayyada.",
            "ሴላት ደም ቀይሕን ጻዕዳን ይምርምር — ትኩሳት፣ ድኻም ወይ ኢንፌክሽን ምስ ሃለወ ይጠቅም።"
        ),
        LabTest(
            "Blood sugar (fasting / random)", "የደም ስኳር", "Sukkaara dhiigaa", "ሽኮር ደም",
            "Measures sugar in the blood — used to find or follow diabetes.",
            "የደም ስኳር ይለካል — ስኳር በሽታን ለማግኘት ወይም ለመከታተል ይጠቅማል።",
            "Sukkaara dhiigaa madaala — dhukkuba sukkaaraa argachuu ykn hordofuuf fayyada.",
            "ሽኮር ደም ይምድድ — ሕማም ሽኮር ንምርካብ ወይ ንምኽታት ይጠቅም።"
        ),
        LabTest(
            "Urine test", "የሽንት ምርመራ", "Qorannoo fincaanii", "ምርመራ ሽንቲ",
            "Checks for infection, sugar or protein in the urine.",
            "በሽንት ውስጥ ኢንፌክሽን፣ ስኳር ወይም ፕሮቲን መኖሩን ይመረምራል።",
            "Fincaan keessatti infekshinii, sukkaaraa ykn pirootiinii jiraachuu sakatta'a.",
            "ኣብ ሽንቲ ኢንፌክሽን፣ ሽኮር ወይ ፕሮቲን ምህላው ይምርምር።"
        ),
        LabTest(
            "HIV test", "የኤችአይቪ ምርመራ", "Qorannoo HIV", "ምርመራ HIV",
            "Free and confidential at public facilities. Knowing your status protects you and your family.",
            "በሕዝብ ተቋማት ነጻ እና በሚስጥር ነው። ሁኔታዎን ማወቅ እርስዎን እና ቤተሰብዎን ይጠብቃል።",
            "Dhaabbilee mootummaa keessatti bilisaa fi iccitii dha. Haala kee beekuun si fi maatii kee tiksa.",
            "ኣብ ህዝባዊ ተቋማት ብነጻን ብምስጢርን እዩ። ኩነታትካ ምፍላጥ ነብስኻን ስድራቤትካን ይከላኸል።"
        ),
        LabTest(
            "Malaria test (blood film / RDT)", "የወባ ምርመራ", "Qorannoo busaa", "ምርመራ ወባ",
            "Finds malaria parasites in the blood when you have fever.",
            "ትኩሳት ሲኖር በደም ውስጥ የወባ ጥገኛ መኖሩን ያገኛል።",
            "Yoo ho'a qaamaa qabdu, dhiiga keessatti maxxantuu busaa argata.",
            "ትኩሳት ምስ ሃለወ ኣብ ደም ጥገኛ ወባ ይረክብ።"
        ),
        LabTest(
            "TB test (sputum / GeneXpert)", "የTB ምርመራ", "Qorannoo TB", "ምርመራ TB",
            "Tests sputum for tuberculosis when a cough lasts more than two weeks.",
            "ሳል ከሁለት ሳምንት በላይ ሲቆይ አክታን ለሳንባ ነቀርሳ ይመረምራል።",
            "Qufni torban lama ol yoo ture, tuffii TB'f sakatta'a.",
            "ስዓል ካብ ክልተ ሰሙን ንላዕሊ ምስ ጸንሐ ኣክታ ንTB ይምርምር።"
        ),
        LabTest(
            "Pregnancy test (urine / blood)", "የእርግዝና ምርመራ", "Qorannoo ulfaa", "ምርመራ ጥንሲ",
            "Confirms pregnancy early so antenatal care can start on time.",
            "የእርግዝና ክትትል በጊዜ እንዲጀምር እርግዝናን ቀደም ብሎ ያረጋግጣል።",
            "Ulfi jalqaba mirkaneessa, akka kunuunsa ulfaa yeroon jalqabu.",
            "ጥንሲ ቀዳማይ የረጋግጽ፣ ንክትትል ጥንሲ ብግዜ ንምጅማር።"
        ),
        LabTest(
            "Liver & kidney function", "የጉበት እና የኩላሊት ተግባር", "Hojiin tiruu fi kalee", "ተግባር ከብድን ኩሊትን",
            "Blood tests that show how well the liver and kidneys are working.",
            "ጉበት እና ኩላሊት ምን ያህል በጥሩ ሁኔታ እንደሚሰሩ የሚያሳዩ የደም ምርመራዎች።",
            "Qorannoon dhiigaa tiruu fi kaleen akkamitti akka hojjetan agarsiisa.",
            "ምርመራታት ደም ከብድን ኩሊትን ብኸመይ ጽቡቕ ከም ዝሰርሑ የርእዩ።"
        )
    )

    /** Health campaigns. No fake dates — the UI says to check with your health center. */
    val campaigns: List<InfoItem> = listOf(
        InfoItem(
            "National immunization campaigns", "ብሔራዊ የክትባት ዘመቻዎች", "Duula talaallii biyyoolessaa", "ሃገራዊ ዘመቻታት ክታበት",
            "Vaccination campaigns for children happen across Ethiopia several times a year. Bring your child's vaccination card. Check with your health center for the current dates.",
            "ለልጆች የክትባት ዘመቻዎች በኢትዮጵያ ውስጥ በዓመት ብዙ ጊዜ ይከናወናሉ። የልጁን የክትባት ካርድ ይዘው ይሂዱ። ወቅታዊ ቀኖችን በጤና ማዕከልዎ ያረጋግጡ።",
            "Duulli talaallii daa'immanii Itoophiyaa keessatti waggaatti yeroo hedduu gaggeeffama. Kaardii talaallii daa'ima kee fudhadhu deemi. Guyyaa ammaa giddu-gala fayyaa kee irraa mirkaneessi.",
            "ዘመቻታት ክታበት ህጻናት ኣብ ኢትዮጵያ ኣብ ዓመት ብዙሕ ግዜ ይካየዱ። ካርድ ክታበት ህጻንካ ሒዝካ ኪድ። ሕጂ ዘሎ ዕለታት ኣብ ማእከል ጥዕናኻ ኣረጋግጽ።"
        ),
        InfoItem(
            "Malaria prevention week", "የወባ መከላከያ ሳምንት", "Torban ittisa busaa", "ሰሙን ምክልኻል ወባ",
            "Sleep under an insecticide-treated bed net, especially in lowland areas. If you develop fever after travel, get tested quickly at a health center.",
            "በተለይ በዝቅተኛ አካባቢዎች በመድኃኒት በተጠቀለለ የአልጋ መረብ ስር ይተኛሉ። ከጉዞ በኋላ ትኩሳት ካለብዎ በጤና ማዕከል በፍጥነት ይመረመሩ።",
            "Keessattuu naannoo gadi aanaa keessatti saaphana siree qoricha itti naqame jalatti rafi. Imala booda ho'a qaamaa yoo qabaatte, giddu-gala fayyaa irratti hatattamaan qorannoo godhadhu.",
            "ፍሉይ ኣብ ትሕት ዝበሉ ከባቢታት ኣብ ትሕቲ መረብ ዓራት መድሃኒት ዝተጠቕለለ ድቀስ። ድሕሪ ጉዕዞ ትኩሳት እንተሃልዩካ ኣብ ማእከል ጥዕና ብቕልጡፍ ተመርመር።"
        ),
        InfoItem(
            "Free HIV testing days", "ነጻ የኤችአይቪ ምርመራ ቀናት", "Guyyaa qorannoo HIV bilisaa", "ነጻ መዓልታት ምርመራ HIV",
            "Free and confidential HIV testing is offered at public health facilities. Knowing your status protects you and your family.",
            "ነጻ እና በሚስጥር የኤችአይቪ ምርመራ በሕዝብ ጤና ተቋማት ይሰጣል። ሁኔታዎን ማወቅ እርስዎን እና ቤተሰብዎን ይጠብቃል።",
            "Qorannoon HIV bilisaa fi iccitii dhaabbilee fayyaa mootummaa keessatti kenna. Haala kee beekuun si fi maatii kee tiksa.",
            "ነጻን ምስጢራዊን ምርመራ HIV ኣብ ህዝባዊ ተቋማት ጥዕና ይወሃብ። ኩነታትካ ምፍላጥ ነብስኻን ስድራቤትካን ይከላኸል።"
        ),
        InfoItem(
            "Maternal health month", "የእናቶች ጤና ወር", "Ji'a fayyaa haadholi", "ወርሒ ጥዕና ኣደታት",
            "Pregnant mothers are encouraged to start antenatal care early and deliver at a health facility. Danger signs such as bleeding or severe headache need emergency care.",
            "እርጉዝ እናቶች የእርግዝና ክትትል ቀደም ብለው እንዲጀምሩ እና በጤና ተቋም እንዲወልዱ ይበረታታሉ። እንደ ደም መፍሰስ ወይም ከባድ ራስ ምታት ያሉ የአደጋ ምልክቶች ድንገተኛ እንክብካቤ ያስፈልጋቸዋል።",
            "Haadholi ulfaa daawwannaa ulfaa jalqaba jalqabanii dhaabbata fayyaa keessatti akka da'an jajjabeessu. Mallattoon balaa akka dhiiga baay'ee ykn dhukkubbii mataa cimaa kunuunsa balaa tasaa barbaada.",
            "ጥንስቲ ኣደታት ክትትል ጥንሲ ቀዳማይ ንምጅማርን ኣብ ተቋም ጥዕና ንምሕራስን ይበረታተዓ። ከም ደም ምፍሳስ ወይ ከቢድ ሕማም ርእሲ ዝኣመሰሉ ምልክታት ሓደጋ ህጹጽ ክንክን የድልዮም።"
        )
    )

    /** First-aid guides: (emoji, title key, body key) — bodies live in I18n. */
    val firstAidGuides: List<Triple<String, String, String>> = listOf(
        Triple("🩸", "fa_bleeding", "fa_bleeding_body"),
        Triple("🔥", "fa_burns", "fa_burns_body"),
        Triple("😮‍💨", "fa_choking", "fa_choking_body"),
        Triple("😵", "fa_fainting", "fa_fainting_body")
    )

    private val conditionKeywords: List<Pair<List<String>, Specialty>> = listOf(
        listOf("heart", "cardiac", "hypertension", "blood pressure", "palpitation", "ልብ", "የደም ግፊት", "onnee", "dhiibbaa dhiigaa", "ልቢ") to Specialty.CARDIOLOGY,
        listOf("diabetes", "sugar", "thyroid", "hormone", "ስኳር", "ታይሮይድ", "sukkaara", "dhukkuba sukkaaraa", "ሽኮር") to Specialty.ENDOCRINOLOGY,
        listOf("cancer", "tumor", "tumour", "lump", "chemo", "ካንሰር", "እጢ", "kaansarii", "kansarii", "መንሽሮ") to Specialty.ONCOLOGY,
        listOf("kidney", "renal", "dialysis", "urine", "ኩላሊት", "ዲያሊሲስ", "kalee", "ኩሊት") to Specialty.NEPHROLOGY,
        listOf("pregnan", "antenatal", "delivery", "birth", "gyneco", "menstru", "እርግዝና", "ወሊድ", "ማህፀን", "ulfa", "da'umsa", "ጥንሲ", "ሕርሲ") to Specialty.OBSTETRICS,
        listOf("child", "baby", "infant", "fever", "ሕፃን", "ልጅ", "ትኩሳት", "daa'ima", "ho'a qaamaa", "ህጻን", "ረስኒ") to Specialty.PEDIATRICS,
        listOf("eye", "vision", "cataract", "glaucoma", "ዓይን", "አይን", "ija", "ijaa", "ዓይኒ") to Specialty.OPHTHALMOLOGY,
        listOf("bone", "fracture", "joint", "spine", "back pain", "አጥንት", "ስብራት", "lafee", "cabuu", "ዓጽሚ") to Specialty.ORTHOPEDICS,
        listOf("stroke", "epilep", "headache", "migraine", "nerve", "paralysis", "ነርቭ", "ራስ ምታት", "mataa dhukkubbii", "ርእሲ") to Specialty.NEUROLOGY,
        listOf("mental", "depress", "anxiety", "psychiat", "stress", "insomnia", "አእምሮ", "ጭንቀት", "sammuu", "dhiphina", "ኣእምሮ") to Specialty.PSYCHIATRY,
        listOf("skin", "rash", "leprosy", "eczema", "acne", "burn", "ቆዳ", "ሽፍታ", "gogaa", "ቆርበት") to Specialty.DERMATOLOGY,
        listOf("stomach", "liver", "asthma", "tuberculosis", "malaria", "cough", "ሆድ", "ጉበት", "ወባ", "garaa", "tiruu", "busaa", "ከብዲ") to Specialty.INTERNAL,
        listOf("surgery", "hernia", "appendic", "operation", "ቀዶ", "baqaqsuu", "መጥባሕቲ") to Specialty.SURGERY
    )

    private val emergencyKeywords: List<String> = listOf(
        "chest pain", "bleeding", "unconscious", "difficulty breathing", "can't breathe",
        "cannot breathe", "not breathing", "seizure", "convulsion", "poison",
        "severe burn", "choking", "stroke", "suicid", "accident",
        "የደረት ህመም", "ደም መፍሰስ", "ራሱን የሳተ", "ራስን መሳት", "መተንፈስ", "አደጋ",
        "dhukkubbii qomaa", "dhiiga", "of wallaal", "hafuura", "balaa",
        "ቃንዛ ኣፍልቢ", "ደም ምፍሳስ", "ምስትንፋስ", "ሓደጋ"
    )

    fun isEmergency(query: String): Boolean {
        val q = query.lowercase()
        return emergencyKeywords.any { q.contains(it) }
    }

    fun matchSpecialties(query: String): List<Specialty> {
        val q = query.lowercase().trim()
        if (q.isEmpty()) return emptyList()
        return conditionKeywords
            .filter { (words, _) -> words.any { q.contains(it) } }
            .map { it.second }
            .distinct()
    }

    /** Haversine distance (km) from the demo user location. */
    fun distanceKm(lat: Double, lon: Double): Double {
        val dLat = Math.toRadians(lat - USER_LAT)
        val dLon = Math.toRadians(lon - USER_LON)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(USER_LAT)) * cos(Math.toRadians(lat)) * sin(dLon / 2).pow(2)
        return 2 * 6371.0 * asin(sqrt(a))
    }

    fun recommend(specs: List<Specialty>): List<Hospital> =
        hospitals
            .filter { h -> specs.any { h.specialties.contains(it) } }
            .sortedWith(
                compareByDescending<Hospital> { h -> specs.count { h.specialties.contains(it) } }
                    .thenBy { distanceKm(it.lat, it.lon) }
            )

    fun medicineNames(): List<String> =
        pharmacies.flatMap { it.stock.keys }.distinct().sorted()

    fun hospitalById(id: String): Hospital? = hospitals.firstOrNull { it.id == id }

    fun doctorById(id: String): Doctor? = doctors.firstOrNull { it.id == id }

    /** Demo bed numbers, labelled as demo in the UI. */
    fun bedsFor(hospital: Hospital): Pair<Int, Int> =
        hospital.bedsAvailable to hospital.bedsTotal
}
