# ጤናአዳም — Tena Adam v1.4.0

**Your Health, One Platform** — a professional "super app for health care" for Ethiopia.

Tena Adam helps you find a hospital by disease or condition, talk to a doctor
(text chat), check pharmacies, call emergency numbers, keep health records,
follow mother & child care and vaccinations, and learn about health — all in
one app, in English, Amharic, Afaan Oromo or Tigrinya. It works **offline**
with demo data stored only on the phone, and **online** it adds live data
from free public APIs (see "New in v1.3.0" below) with offline fallback.

- Package: `et.ethiohealth.app` (same as v0.1.0, so existing installs update)
- Version: **1.4.0** (versionCode 6), minSdk 26 (Android 8.0+), targetSdk 34
- Stack: Kotlin 2.0.0, Jetpack Compose (BOM 2024.09.00), Material 3,
  Retrofit 2.11.0 + OkHttp + Gson, kotlinx-coroutines-android 1.8.1
- Built by: Rebuma Tamiru Roro

## New in v1.4.0 — more features, more attractive UI

- **Splash / onboarding** — logo, tagline and 3 swipeable cards (Find care,
  Talk to a doctor, Track your health) with Skip / Get started, shown once.
- **Home redesign** — gradient hero with greeting and search, quick-action
  chips (Find hospital, Doctor, Live nearby, SOS), a daily health tip that
  rotates through the education articles, and section tiles with soft tinted
  icons. Hospital and doctor cards now have initials avatars, rounder
  corners (20dp) and more elevation.
- **Dark mode** — a toggle in Profile & Family → Settings, saved on the
  phone, applied through a proper Material 3 light + dark colour scheme
  (same teal brand, dark teal surfaces in dark mode). Shared cards, the
  bottom bar and the top-level screens follow the scheme.
- **Bottom navigation** — proper icons + bold labels when selected, and the
  SOS item is red so it stands out. The top SOS pill now opens the new
  one-tap SOS screen.
- **Appointment calendar** — a month grid of saved appointments, tap a day
  to see its appointments, add / cancel, and a days-until countdown.
  Reminder choice: in-app upcoming list + a notification-permission
  request. No AlarmManager / WorkManager yet — that needs a receiver and
  notification channel, so it was deliberately left out to keep the CI
  build safe.
- **Vaccination reminders** — the EPI schedule from the child's birth date
  with due / overdue / done states; tap to mark a vaccine Done (saved on
  the phone). Enter the birth date in Mother & Child first.
- **Symptom checker (non-diagnostic)** — pick symptoms, get a suggested
  specialty and hospitals that can help. Emergency symptoms show the
  emergency banner and a call-907 button. The "does not diagnose"
  disclaimer is always shown.
- **Daily health tip** — on Home, rotating through the education articles
  by day of year; tap to open the full article.
- **One-tap SOS** — a big red SOS screen with 907 / 911 / 991 / 939 as
  large tap-to-call cards, plus Share my location using the last-known GPS
  location (permission requested first; Meskel Square demo location is
  shared — and labelled as such — when GPS is unavailable).

## New in v1.3.0 — live data, free no-key APIs

All of these work when the phone is online and fall back gracefully offline
(cached last-good result first, then the offline demo data). No API key is
needed for any of them; no account is created; no health record leaves the
phone. See the Privacy screen in the app for the full statement.

- **Live Nearby** — real nearby hospitals, pharmacies and clinics from the
  Overpass API (OpenStreetMap), sorted by distance from your GPS location
  (Meskel Square, Addis Ababa, is the fallback when GPS is unavailable).
  Each facility has Directions (Google Maps) and Share.
- **Medicine Info** — medicine label information from openFDA
  (`api.fda.gov/drug/label.json`): generic name, purpose and warnings.
- **Air Quality & Weather** — live Open-Meteo air quality (US AQI, PM2.5)
  and weather for Addis Ababa, with health advice by AQI band.
- **Favourites** — save favourite hospitals and doctors on the phone;
  also available from any hospital detail screen.
- **BP & Sugar chart** — a simple bar chart of your logged blood-pressure
  and blood-sugar readings (last 7), from the BP & Sugar Tracker screen.
- **Share / Directions** — share any hospital and open Google Maps
  directions from hospital detail and Live Nearby.
- **Online AI, wired** — the Gemini (`gemini-2.0-flash`) and Groq
  (`llama-3.3-70b-versatile`) integration in `AiAssistant.kt` is now
  implemented via Retrofit, using only the user's own key from AI settings.
  Any failure falls back to the offline assistant, and every online reply
  still ends with the "does not diagnose" disclaimer. No key is hardcoded.
- **Privacy screen** — states that location is used only on-device for
  nearby search and that no data is sent except to the named public APIs.
- **Professional polish** — loading / error / empty states on all new
  screens, Material 3 theme consistency, About screen updated to v1.3.0,
  INTERNET + location permissions in the manifest.
- **Static check fixed** — `check_static.py` used a hardcoded path to a
  different checkout and failed with `KeyError: 'I18n.kt'` from the repo
  root; it now resolves paths relative to the script itself.

## ⚠️ Honest note — read first

This is a **clean rewrite**. The original source code was lost, so v1.1.0 was
rewritten from the code recovered by decompiling the user's own v0.1.0 APK
(see `~/workspace/ethiohealth-source/decompiled/` — not included here).
The recovered-source reference project is kept untouched at
`~/workspace/ethiohealth-source/EthioHealth/`.

**This project has NOT been built yet.** No Gradle build was attempted in the
environment where it was written (no network for dependencies). Open it in
Android Studio and run the first build there — see "How to build" below.
A static self-check was run instead (`check_static.py`): every string key
used exists in `I18n.kt`, every drawable referenced exists in `res/`,
braces/parens balance, no references to decompiled-Java classes, every
route and screen is wired. The check passes — but it is not a compile.

**No v0.2 APK was available.** The user confirmed he does not have the v0.2
APK file. All v0.2 features in this release were built from his screenshots
of v0.2 running on his phone. If a v0.2 APK arrives later, reconcile the two
and bump the version.

## What the app does

### Kept from v0.1.0 (all screens, all strings)
- **Home** — hero card, search, AI assistant banner, tile grid
- **Find a Hospital** — type a disease/condition → specialty → recommended
  hospitals (13 specialties, 15 hospitals — see below)
- **Hospital detail + Request Appointment** — appointment saved on the phone
  (status "Requested")
- **Doctors** — 10 sample doctors, online/offline, text chat
- **Chat** — text replies, canned answers, emergency detection → "call 907";
  video/audio "coming in a later version"
- **Pharmacy** — medicine search, in/out of stock, sample ETB prices, delivery
- **Emergency** — dial **907** Red Cross ambulance, **911** national emergency,
  **991** police, **939** fire (these numbers are from the app's own data)
- **Health Records** — history, prescriptions, lab results, my appointments
- **Mother & Child** — pregnancy-week calculator, child vaccination schedule
- **Health Education** — 11 articles
- **Language** — English / አማርኛ / Afaan Oromoo / ትግርኛ
- Disclaimer (all 4 languages): *"Tena Adam recommends hospitals; it does
  not diagnose. Always consult a health professional."* (The brand name in
  the disclaimer was updated from "EthioHealth" to "Tena Adam" as part of
  the rename; the rest of the wording is unchanged.)

### From v0.2 (built from the user's screenshots)
Home sections **Find Care / Health Services / My Health / AI, Learn & Support**,
with bottom navigation **Home / Assistant / SOS / Profile & Family** and a
floating blue Assistant button. Every v0.2 tile is a real screen:
- **Nearby Map** — facilities by area with approx. distances (demo location)
- **Hospital Beds** — bed availability per hospital (demo numbers, labelled)
- **Ratings & Reviews** — star ratings + reviews per hospital/doctor, saved
  on the phone (starts empty — no invented review counts)
- **Lab Tests** — common tests with a one-line explanation
- **Blood Bank** — who can donate, where (National Blood Bank Service,
  Tikur Anbessa blood bank, regional blood banks)
- **Health Insurance (CBHI)** — see v1.1.0 section below
- **Payments** — information only: Telebirr, CBE Birr, cash at hospital.
  **No real payment processing, no keys, no prices.**
- **First Aid** — step-by-step guides (bleeding, burns, choking, fainting)
  with "call 907" warnings
- **Medicine Reminders** — add medicine + time, saved on the phone
- **BP & Sugar Tracker** — log blood pressure and blood sugar, latest first
- **Health Calculators** — BMI + links to pregnancy week / vaccine schedule
- **Mental Wellbeing** — timed breathing exercise (inhale / hold / exhale),
  stress tips, "if you feel unsafe, call 907 or 911"
- **Profile & Family** — profile + family members, saved on the phone
- **Health News & Campaigns** — vaccination campaigns, malaria prevention,
  HIV testing, maternal health — **no fake dates**: "check with your health
  center for dates"
- **Future AI Features** — planned features, marked "coming in a later version"
- **AI Health Assistant** — chat screen (see below)

### New in v1.1.0
- **Ethiopian health-system tiers** — every hospital has a facility type
  (Health Post / Health Center / Primary Hospital / General Hospital /
  Referral & Specialized Hospital), shown as a chip on cards and detail, with
  a tier filter on the Finder. Real hospitals tiered correctly (Tikur Anbessa,
  St. Paul's, Menelik II, ALERT, Amanuel = Referral & Specialized; Yekatit 12,
  Gandhi = General). A "How the Ethiopian health system works" screen explains
  the 3 tiers (health post → health center → hospital) and the Health
  Extension Program.
- **CBHI screen** — what Community-Based Health Insurance is, who can join
  (informal-sector households through their woreda/kebele), what it covers
  in general terms, how to enrol (woreda health office / kebele). **No
  invented prices** — costs vary by region, ask at the woreda health office.
- **Full EPI vaccination schedule** — BCG + OPV 0 at birth; Pentavalent +
  OPV + PCV + Rota at 6, 10, 14 weeks; Measles at 9 and 15 months. Due dates
  computed from the child's birth date, with Done / Upcoming states.
- **Health tools** — BMI calculator (kg + cm → BMI + category) and a
  blood-pressure log in Records (latest first).
- **Appointment reminders** — "Upcoming appointments" on Home with a
  days-until countdown (from appointments already saved).
- **Blood donation** — eligibility info inside Blood Bank + "Find blood
  donation" entry on Home.
- **More data** — 5 more hospitals (regional referral: Gondar, Jimma,
  Hawassa, Bahir Dar, Mekelle — clearly demo data, verify before use),
  4 more doctors (10 total), 4 more pharmacies (8 total: Addis Ketema,
  Kirkos, Yeka, Akaki), 4 more education articles (tuberculosis, HIV,
  nutrition, safe water — malaria prevention was already there).
- **Professional polish** — Option 3 logo (see below), bright-green → deep
  teal gradient, white cards, bilingual tile labels (current language +
  Amharic), "Demo data – verify before use" visible on Finder and Pharmacy.

## Logo (final — Option 3)

The user chose **Option 3** (leaf sprig + white heartbeat line + ጤናአዳም +
teal cross, green-to-teal gradient) and confirmed it twice. The canonical
file he sent is:
`~/workspace/user/files/Option_3_-_Heartbeat_Tena_Adam_logo__1__10_bere.webp`
(a second copy he re-sent, `...__1__14_3wvq.webp`, is byte-identical).

In this project:
- **Launcher icon**: `app/src/main/res/mipmap-*/ic_launcher.png` and
  `ic_launcher_round.png` (48/72/96/144/192 px) — resized from the square
  Option 3 app icon
  (`design/tena-adam-app-icon-option3.webp`, also in
  `~/workspace/tena-adam-design/media-generation-tena-adam-logo-option3-app-ico-*.webp`).
  The old adaptive-icon XML and its foreground/background vectors were
  removed (they referenced deleted files).
- **About screen / header logo**: `app/src/main/res/drawable/tena_adam_logo.png`
  (1024 px wide) — converted from the canonical logo file. The polished
  Option 3 version is kept as a design reference in `design/`.
- **Design references** (`design/`): `tena-adam-logo-final.webp` (canonical),
  `tena-adam-logo-option3-polished.webp`, `tena-adam-app-icon-option3.webp`,
  `home-screen-mockup.webp`, `hospital-ai-screen-mockup.webp`.
  The earlier leaf-heart logo and the dark-green/gold logo are **not** used
  and are not in this project.
- **Brand colours**: bright leaf green `#43A047` → deep teal `#01918E`
  gradient, white cards, dark teal text `#0B3D36`. Red (`#DA121A`) is used
  only for Emergency / Blood Bank. The green/yellow/red Ethiopian flag
  stripe from v0.1.0 is kept under the top bar.

## AI Health Assistant — free options

The assistant works **offline by default** (`AiAssistant.kt`,
`OfflineAssistant`): rule-based replies, keyword routing to the right
screen, emergency detection → "call 907". It never diagnoses. No internet,
no key, no network dependency.

**Online mode (v1.3.0, wired)** — in the app, go to Assistant → AI settings,
pick a provider (Gemini or Groq) and paste **your own free API key**. The
assistant calls the provider over Retrofit/OkHttp and falls back to the
offline assistant on any failure. Free tiers that fit:
- **Google Gemini API free tier** — model constant `GEMINI_MODEL =
  "gemini-2.0-flash"` in `AiAssistant.kt`
- **Groq free tier** — model constant `GROQ_MODEL =
  "llama-3.3-70b-versatile"` in `AiAssistant.kt`

No key values, endpoints or prices are invented or stored here. To finish
the integration (in Android Studio): add an HTTP client dependency in
`app/build.gradle.kts`, implement `OnlineAiAssistant.answer()` at the
marked TODO (POST the user message with the model name and the key as the
bearer token), keep the "does not diagnose" disclaimer in the reply, and
fall back to `OfflineAssistant` when the network call fails. Adding it must
not break the offline build.

## Data honesty

- Real hospitals use their real names and areas. Phone numbers come from
  the v0.1.0 data ("city directory"). New regional hospitals have **no
  phone** — the UI says "not in demo data – look up before calling".
- All doctors and pharmacies are labelled **"(sample)" / "Sample ..."** —
  fictional people and places. Never real doctors.
- Pharmacy ETB prices are sample prices from v0.1.0.
- Bed numbers are **demo estimates**, labelled as such.
- The app never claims to diagnose, treat, or connect to a live hospital
  system. It is an offline demo.
- Emergency numbers are exactly **907 / 911 / 991 / 939**.

## Translations

All user-visible strings live in `I18n.kt` (319 keys, 4 translations each:
English, Amharic, Afaan Oromo, Tigrinya — in that order). The v0.1.0
strings were recovered exactly; the new strings are best-effort drafts in
the same style. The v0.1.0 note still applies: *"Translations of main
labels are a best-effort draft; please have native speakers review them."*

## Bottom navigation — a choice to confirm

The bottom bar follows the **v0.2 screenshots**: Home / Assistant / SOS /
Profile & Family. The design mockup showed a different bar (Home /
Appointments / History / Profile). The v0.2 screenshots are the actual app,
so the v0.2 bar wins. The bar items live in one list (`BOTTOM_NAV` in
`MainActivity.kt`), so swapping is a one-line change if the user prefers
the mockup's bar.

## How to build (first time, in Android Studio)

1. Open this folder in Android Studio (Hedgehog or newer).
2. Let Gradle sync — the first sync downloads dependencies (needs internet
   once): Kotlin 2.0.0, AGP 8.5.2, Compose BOM 2024.09.00, Retrofit 2.11.0,
   OkHttp 4.12.0, Gson converter 2.11.0, kotlinx-coroutines-android 1.8.1.
3. Run on a device or emulator (Android 8.0 / API 26 and newer).
4. If the first build reports a missing resource, check `res/` — it was
   recovered from the APK and contains library leftovers (notification
   drawables, locale `values-*` folders) that are harmless but were not all
   hand-checked.

To run the static self-check again: `python3 check_static.py` (no build).

## Project structure

```
TenaAdam/
├── README.md / CHANGELOG.md / check_static.py
├── settings.gradle / build.gradle.kts / gradle.properties / gradlew
├── design/                  # logo + mockup references (Option 3)
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml        # versionCode 5, versionName 1.3.0
        ├── java/et/ethiohealth/app/
        │   ├── MainActivity.kt        # nav stack, scaffold, theme, bottom bar
        │   ├── Screens.kt             # Home + v0.1.0 screens + HealthSystem + About
        │   ├── NewScreens.kt          # all v0.2 / v1.1.0 screens
        │   ├── RealtimeScreens.kt     # v1.3.0 live screens + Favourites/Chart/Privacy
        │   ├── ApiClient.kt           # v1.3.0 Retrofit clients (Overpass, Nominatim,
        │   │                          #   openFDA, Open-Meteo, Gemini, Groq)
        │   ├── RealtimeRepository.kt  # v1.3.0 live fetch + cache + offline fallback
        │   ├── RealtimeViewModels.kt  # v1.3.0 view models for the live screens
        │   ├── Data.kt                # hospitals, doctors, pharmacies, articles…
        │   ├── I18n.kt                # all strings, 4 languages (319 keys)
        │   ├── AiAssistant.kt         # offline assistant + wired online mode
        │   ├── Models.kt              # Lang, Specialty, FacilityTier, models
        │   └── Store.kt               # on-phone SharedPreferences JSON store
        └── res/
            ├── mipmap-*/ic_launcher(.png)      # Option 3 icon
            ├── drawable/tena_adam_logo.png     # Option 3 logo (About screen)
            └── values/strings.xml              # app_name = ጤናአዳም
```
