# Changelog — Tena Adam (ጤናአዳም)

## v1.1.1 — 2026-11-26 (launcher icon)

- Launcher icon changed to the **full Option 3 logo** (leaf + heartbeat +
-   ጤናአዳም + cross, on white) at the user's request — the v1.1.0 icon was the
-     simplified leaf + heartbeat + cross without the name. versionCode 3.
- - Nothing else changed.

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
- Option 3 logo (leaf + heartbeat + ጤናአዳም + cross) as About-screen logo; bright-green → deep-teal brand theme. (The v1.1.0 launcher icon was a simplified leaf + heartbeat + cross; v1.1.1 uses the full logo.)

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
- **Built.** Built via GitHub Actions (release v1.1.0, APK verified: package et.ethiohealth.app, versionCode 2, versionName 1.1.0, 16.9 MB). Delivered to the user 2026-11-26; installed on his phone.
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
