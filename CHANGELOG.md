# Changelog — Tena Adam (ጤናአዳም)

## v1.4.0 — 2026-11-28 (more features, attractive UI)

- Version 1.4.0, versionCode 6.
- Splash / onboarding: logo, tagline, 3 swipeable cards (Find care, Talk
  to a doctor, Track your health), Skip / Get started, shown once
  (Store.KEY_ONBOARDING_DONE).
- Home redesign: gradient hero, quick-action chips (Find hospital, Doctor,
  Live nearby, SOS), daily health tip rotating through the education
  articles, rounder (20dp) elevated hospital/doctor cards with initials
  avatars.
- Dark mode: toggle in Profile → Settings, persisted in Store
  (Store.KEY_DARK_MODE), applied via Material 3 light + dark colour
  schemes (TenaLightScheme / TenaDarkScheme in MainActivity). Shared cards
  (TileCard, InfoCard), Home tiles, bottom bar and scaffold background
  follow the scheme; some older screens still use light cards.
- Bottom navigation: bold selected labels, SOS item red; top SOS pill and
  bottom SOS item now open the new one-tap SOS screen.
- Appointment calendar: month grid, tap a day, add / cancel, days-until
  countdown, upcoming list. Reminder: in-app list + notification
  permission request (POST_NOTIFICATIONS, Android 13+); no AlarmManager /
  WorkManager in this release (deliberate, to keep the CI build safe).
- Vaccination reminders: EPI schedule from the child's birth date with
  due / overdue / done states and Done marking saved on the phone
  (Store.KEY_VACCINE_DONE).
- Symptom checker (non-diagnostic): 12 symptoms → suggested specialty +
  recommended hospitals, emergency banner + call 907 for chest pain,
  difficulty breathing or heavy bleeding, disclaimer always shown.
- Daily health tip on Home (article of the day, tap to open).
- One-tap SOS screen: big 907 / 911 / 991 / 939 cards, Share my location
  via last-known GPS (permission first; Meskel Square demo location with
  a clear label when GPS is unavailable).
- 52 new I18n keys (4 languages each); About screen and Home footer now
  show v1.4.0. Workflow updated to tag v1.4.0 / artifact
  tena-adam-v1.4.0-apk (note: the workflow file on GitHub is still the
  old v1.2.0 version — the GitHub App lacks Workflows permission — so
  this local update needs a future manual push to take effect).
- Static check passes (see check_static.py run below).

## v1.3.0 — 2026-11-28 (live data, professional upgrade)

- Version 1.3.0, versionCode 5. Built via GitHub Actions (release v1.3.0).
- Live Nearby (Overpass API / OpenStreetMap): real nearby hospitals,
  pharmacies and clinics sorted by distance from GPS, Meskel Square
  (9.0054, 38.7636) fallback, cached last-good result, demo data offline.
  Directions (Google Maps) and Share on every facility and hospital detail.
- Medicine Info (openFDA drug labels, no key): generic name, purpose,
  warnings; empty state when nothing is found.
- Air Quality & Weather (Open-Meteo, no key): US AQI + PM2.5 and current
  weather for Addis Ababa, with health advice by AQI band; cached fallback.
- Favourites saved on the phone (hospitals + doctors), toggleable from
  hospital detail and from the Favourites screen.
- BP & Sugar chart: simple bar chart of the last 7 logged readings,
  opened from the BP & Sugar Tracker.
- Online AI wired: Gemini (gemini-2.0-flash) and Groq
  (llama-3.3-70b-versatile) via Retrofit, using only the user's own key
  from AI settings; fallback to the offline assistant on any failure;
  disclaimer kept on every online reply. No key hardcoded.
- Privacy screen: location used only on-device for nearby search; no data
  sent except to the named public APIs (OpenStreetMap, openFDA, Open-Meteo,
  and Gemini/Groq only when the user enables online AI with their own key).
- Permissions added: INTERNET, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION.
  Dependencies added: Retrofit 2.11.0, converter-gson 2.11.0, OkHttp 4.12.0,
  kotlinx-coroutines-android 1.8.1 (all compatible with Kotlin 2.0.0 /
  Compose BOM 2024.09.00 / AGP 8.5.2).
- Fixed: `check_static.py` failed with KeyError I18n.kt from the repo root
  (hardcoded path to another checkout); it now resolves paths relative to
  the script. Static check passes: 319 I18n keys, 37 routes, 38 screens.
- About screen now shows v1.3.0; Home footer and About use the v1.3.0
  prototype string.

## v1.2.0 — 2026-11-26 (professional UI redesign)

- Home screen redesigned to look more attractive and professional: hero card
  now shows the full Option 3 logo in a white chip above the greeting,
  search bar has a search icon and a matching-height Search button, AI
  Assistant banner uses a proper icon in a blue circle, section titles have
  a teal accent bar, tiles have soft tinted icon circles (cycling palette),
  and "No upcoming appointments" is a proper card instead of plain text.
- Top bar: SOS and language are now pill buttons (the old IconButton clipped
  the "🌐 EN" text — it was wider than 48dp). Bottom navigation and the
  floating Assistant button now use proper vector icons (Home, SmartToy, Sos,
  FamilyRestroom) instead of emoji. versionCode 4.
- Fixed: greeting said "Hello, Rebuma, Rebuma" — the I18n "hello" string
  already contained the name; it is now just "Hello" / "ሰላም" / "Akkam" /
  "ሰላም" and the name is added once.
- Fixed: long tile titles (e.g. "Ratings & Reviews") could clip mid-word;
  titles and subtitles now use ellipsis.

## v1.1.1 — 2026-11-26 (launcher icon)

- Launcher icon changed to the **full Option 3 logo** (leaf + heartbeat +
  ጤናአዳም + cross, on white) at the user's request — the v1.1.0 icon was the
  simplified leaf + heartbeat + cross without the name. versionCode 3.
- Nothing else changed.

## v1.1.0 — 2026-11-26 (clean rewrite; built via GitHub Actions, release v1.1.0)

Renamed from **EthioHealth** to **ጤናአዳም (Tena Adam)**. Package stays
`et.ethiohealth.app` so existing installs update. versionCode 2.

### Added — from v0.2 (built from the user's screenshots; no v0.2 APK was available)
- Home sections Find Care / Health Services / My Health / AI, Learn & Support
  with a 3-column bilingual tile grid; bottom navigation Home / Assistant /
  SOS / Profile & Family; floating blue Assistant button.
- Nearby Map (facilities by area, approx. distances, demo location)
- Hospital Beds (demo bed availability per hospital, labelled demo/verify)
- Ratings & Reviews (star ratings + reviews per hospital/doctor, saved on
  the phone; starts empty — no invented review counts)
- Lab Tests (8 common tests with a one-line explanation)
- Blood Bank (who can donate, where — National Blood Bank Service, Tikur
  Anbessa blood bank, regional blood banks)
- Payments (information only: Telebirr, CBE Birr, cash at hospital — no real
  payment processing, no keys, no prices)
- First Aid (bleeding, burns, choking, fainting — step-by-step, "call 907")
- Medicine Reminders (add medicine + time, saved on the phone)
- BP & Sugar Tracker (log blood pressure and blood sugar, latest first)
- Health Calculators (BMI + links to pregnancy week / vaccine schedule)
- Mental Wellbeing (timed breathing exercise, stress tips, "if you feel
  unsafe, call 907 or 911")
- Profile & Family (profile + family members, saved on the phone)
- Health News & Campaigns (vaccination campaigns, malaria prevention, HIV
  testing, maternal health — no fake dates)
- Future AI Features (planned features, "coming in a later version")
- AI Health Assistant chat screen (offline rule-based replies by default;
  optional online mode via the user's own free Gemini/Groq API key — see
  README and the TODO in `AiAssistant.kt`)

### Added — new in v1.1.0
- Ethiopian health-system tiers: facility type chip on every hospital card
  and detail, tier filter on the Finder, correct tiers for real hospitals
  (Tikur Anbessa, St. Paul's, Menelik II, ALERT, Amanuel = Referral &
  Specialized; Yekatit 12, Gandhi = General), "How the Ethiopian health
  system works" screen (3 tiers + Health Extension Program)
- CBHI screen (what it is, who can join, what it covers, how to enrol;
  no invented prices — costs vary by region)
- Full EPI vaccination schedule from the child's birth date (BCG + OPV 0 at
  birth; Pentavalent + OPV + PCV + Rota at 6/10/14 weeks; Measles at 9 and
  15 months) with Done / Upcoming states
- BMI calculator and blood-pressure log
- "Upcoming appointments" on Home with a days-until countdown
- Blood-donation eligibility info inside Blood Bank
- 5 more hospitals (Gondar, Jimma, Hawassa, Bahir Dar, Mekelle — demo data),
  4 more doctors (10 total), 4 more pharmacies (8 total: Addis Ketema,
  Kirkos, Yeka, Akaki), 4 more education articles (tuberculosis, HIV,
  nutrition, safe water)
- About screen (logo, version 1.1.0, "Prototype — offline demo",
  disclaimer, "Built by Rebuma Tamiru Roro")
- Option 3 logo (leaf + heartbeat + ጤናአዳም + cross) as About-screen logo;
  bright-green → deep-teal brand theme. (The v1.1.0 launcher icon was a
  simplified leaf + heartbeat + cross; v1.1.1 uses the full logo.)

### Changed
- Display name everywhere: ጤናአዳም (Tena Adam). The disclaimer now says
  "Tena Adam recommends hospitals…" (brand name updated; wording otherwise
  unchanged). Tagline stays "Your Health, One Platform".
- Old adaptive launcher icon (green/yellow/red stripes + white circle)
  removed; replaced by the Option 3 icon PNGs.
- Status-bar colour now deep teal `#01918E` (was `#078930`).

### Kept from v0.1.0
- Every screen and every I18n string (all 4 languages). All v0.1.0 features
  still work the same way: Finder, hospital detail, appointment requests,
  doctors + chat, pharmacy, emergency (907 / 911 / 991 / 939), records,
  Mother & Child, education, language picker.
- Data: 13 specialties, the same 7 real hospitals + 3 demo clinics,
  6 original doctors, 4 original pharmacies, 7 original articles,
  vaccine schedule, condition/emergency keyword matching, distance
  calculation from the Meskel Square demo location.

### Notes / verified
- **Built.** Built via GitHub Actions (release v1.1.0, APK verified:
  package et.ethiohealth.app, versionCode 2, versionName 1.1.0, 16.9 MB).
  Delivered to the user 2026-11-26; installed on his phone.
  Static self-check (`check_static.py`) passes: 273 I18n keys, all keys
  used are defined, all drawables referenced exist, braces/parens balance,
  no decompiled-Java references, all 31 routes and 32 screens wired.
- New Amharic / Afaan Oromo / Tigrinya translations are best-effort drafts;
  please have native speakers review them.
- The v0.2 features were built from screenshots, not from a v0.2 APK
  (the user confirmed he does not have the v0.2 APK file). If it arrives
  later, reconcile and bump the version.

## v0.2 — (on the user's phone; screenshots only)
- Renamed to ጤናአዳም / Tena Adam; bigger home screen with the four sections
  above; bottom navigation Home / Assistant / SOS / Profile & Family;
  floating Assistant button; footer "Prototype v0.2 – offline demo".
- Source code not available; APK not available.

## v0.1.0-prototype — baseline (recovered by decompiling the user's APK)
- Package `et.ethiohealth.app`, versionName 0.1.0-prototype, versionCode 1.
- Kotlin + Jetpack Compose, Material 3, offline demo, SharedPreferences
  store ("ethiohealth"). 4 languages (en/am/om/ti).
- Home, Find a Hospital by disease/condition, hospital detail + appointment
  request, doctors, pharmacy, emergency (907/911/991/939), health records,
  Mother & Child (pregnancy week, vaccine calculator), health education
  (7 articles), chat (text only), language picker.
- Tagline "Your Health, One Platform". Prototype badge
  "Prototype v0.1 – offline demo".
