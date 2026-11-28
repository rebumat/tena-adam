#!/usr/bin/env python3
"""Static self-check for the TenaAdam v1.1.0 Kotlin project.
Checks (no Gradle build is attempted — no network in this VM):
  1. Every I18n key used in Kotlin files exists in I18n.kt (and vice versa:
     list unused keys for review).
  2. Every R.drawable / R.color reference exists in res/.
  3. Braces / parens / brackets balance per Kotlin file.
  4. No references to decompiled-Java classes (Composer, ComposableLambda...).
  5. Every Route.X referenced exists in the Route sealed class.
  6. Every Store.KEY_* used exists in Store.kt constants.
  7. Every screen function called from MainActivity exists.
  8. I18n map: no duplicate keys, every entry has exactly 4 translations.
Exit code 0 = all hard checks pass.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
KT_DIR = ROOT / "app/src/main/java/et/ethiohealth/app"
RES = ROOT / "app/src/main/res"

errors, warnings = [], []

kt_files = sorted(KT_DIR.glob("*.kt"))
src = {f.name: f.read_text(encoding="utf-8") for f in kt_files}

# --- 1. I18n keys ---------------------------------------------------------
i18n = src["I18n.kt"]
defined = re.findall(r'"([a-z0-9_]+)"\s+to\s+listOf\(', i18n)
if len(defined) != len(set(defined)):
    dupes = sorted({k for k in defined if defined.count(k) > 1})
    errors.append(f"I18n duplicate keys: {dupes}")
defined_set = set(defined)

# every entry has exactly 4 translations: count top-level listOf entries
for m in re.finditer(r'"([a-z0-9_]+)"\s+to\s+listOf\((.*?)\),\n', i18n, re.S):
    key, body = m.group(1), m.group(2)
    # count top-level string literals (naive: count of '", "' separators + 1)
    n = body.count('", "') + 1
    if n != 4:
        errors.append(f"I18n key {key}: {n} translations (expected 4)")

used = set()
for name, text in src.items():
    # tr("key") and I18n.get("key", ...) and t("key") (AiAssistant lambda)
    used |= set(re.findall(r'\btr\("([a-z0-9_]+)"\)', text))
    used |= set(re.findall(r'\bt\("([a-z0-9_]+)"\)', text))
    used |= set(re.findall(r'I18n\.get\("([a-z0-9_]+)"', text))
    # titleKey-style string returns in MainActivity: "..." -> "..."
    used |= set(re.findall(r'->\s*"([a-z0-9_]+)"', text))
# Keys used through variables (section lists, tier enum, label lists) — verified
# by hand against the code that builds those lists.
used |= {
    "find_care", "health_services", "my_health", "ai_learn_support",
    "sos", "home", "ambulance", "police", "fire", "national_emergency",
    "online_doctor",
    "tier_health_post", "tier_health_center", "tier_primary", "tier_general",
    "tier_referral",
    "cbhi_what", "cbhi_what_body", "cbhi_who", "cbhi_who_body",
    "cbhi_covers", "cbhi_covers_body", "cbhi_how", "cbhi_how_body",
    "cbhi_cost", "cbhi_cost_body",
    "pay_telebirr", "pay_cbe", "pay_cash", "pay_cbhi",
    "stress_tip_1", "stress_tip_2", "stress_tip_3",
}
# dynamic keys used via string interpolation / variables (checked explicitly)
for k in ["chat_reply_1", "chat_reply_2", "chat_reply_3", "chat_reply_4",
          "breathe_in", "breathe_hold", "breathe_out",
          "fa_bleeding", "fa_bleeding_body", "fa_burns", "fa_burns_body",
          "fa_choking", "fa_choking_body", "fa_fainting", "fa_fainting_body"]:
    used.add(k)
missing = sorted(used - defined_set)
if missing:
    errors.append(f"I18n keys used but NOT defined: {missing}")
unused = sorted(defined_set - used)
if unused:
    warnings.append(f"I18n keys defined but never used: {unused}")

# --- 2. R.drawable / R.color ---------------------------------------------
for name, text in src.items():
    for d in set(re.findall(r'R\.drawable\.([A-Za-z0-9_]+)', text)):
        if not (RES / "drawable" / f"{d}.png").exists() and \
           not (RES / "drawable" / f"{d}.xml").exists():
            errors.append(f"{name}: missing drawable {d}")
    for c in set(re.findall(r'R\.color\.([A-Za-z0-9_]+)', text)):
        colors = (RES / "values/colors.xml").read_text()
        if f'name="{c}"' not in colors:
            errors.append(f"{name}: missing color {c}")

# --- 3. brace / paren / bracket balance ----------------------------------
for name, text in src.items():
    # strip strings and comments roughly
    t = re.sub(r'"(?:\\.|[^"\\])*"', '""', text)
    t = re.sub(r'//.*', '', t)
    t = re.sub(r'/\*.*?\*/', '', t, flags=re.S)
    for o, c, label in [("{", "}", "braces"), ("(", ")", "parens"), ("[", "]", "brackets")]:
        if t.count(o) != t.count(c):
            errors.append(f"{name}: unbalanced {label}: {t.count(o)} '{o}' vs {t.count(c)} '{c}'")

# --- 4. no decompiled-Java references -------------------------------------
bad = ["Composer", "ComposableLambda", "ComposableSingletons", "SnapshotState",
       "RecomposeScopeImpl", "Intrinsics.check", "checkNotNull", "JavaToKotlin"]
for name, text in src.items():
    for b in bad:
        if b in text:
            errors.append(f"{name}: reference to decompiled-Java artifact '{b}'")

# --- 5. Route coverage ----------------------------------------------------
# Route subclasses are defined in MainActivity.kt as `object X : Route()` / `data class X`
defined_routes = set(re.findall(r'(?:object|data class)\s+(\w+)\s*(?:\(|:)', src["MainActivity.kt"]))
route_names = set()
for name, text in src.items():
    route_names |= set(re.findall(r'Route\.(\w+)', text))
# Route subclasses are defined in MainActivity.kt as `object X : Route()` / `data class X`
defined_routes = set(re.findall(r'(?:object|data class)\s+(\w+)\s*(?:\(|:)', src["MainActivity.kt"]))
missing_routes = sorted(route_names - defined_routes - {"Home"})
if missing_routes:
    errors.append(f"Route.X used but not defined: {missing_routes}")

# --- 6. Store keys ---------------------------------------------------------
store_consts = set(re.findall(r'const val (KEY_[A-Z_]+)', src["Store.kt"]))
for name, text in src.items():
    for k in set(re.findall(r'Store\.(KEY_[A-Z_]+)', text)):
        if k not in store_consts:
            errors.append(f"{name}: unknown Store key {k}")
    # raw string keys passed to store.list/add/remove/getString/putString
    for k in set(re.findall(r'store\.(?:list|add|remove|getString|putString)\("([a-z_]+)"', text)):
        errors.append(f"{name}: raw string store key '{k}' (use Store.KEY_* constant)")

# --- 7. screen functions called from MainActivity exist -------------------
main = src["MainActivity.kt"]
called = set(re.findall(r'(\w+Screen)\(', main))
defined_fns = set()
for name, text in src.items():
    defined_fns |= set(re.findall(r'fun\s+(\w+Screen)\s*\(', text))
missing_fns = sorted(called - defined_fns)
if missing_fns:
    errors.append(f"Screen functions called but not defined: {missing_fns}")

# --- 8. every Route case handled in MainActivity dispatch ------------------
for r in sorted(defined_routes):
    if f"Route.{r}" not in main and r != "Home":
        warnings.append(f"Route.{r} defined but not referenced in MainActivity dispatch")

print(f"Kotlin files: {len(kt_files)}")
print(f"I18n keys defined: {len(defined_set)}  |  keys used: {len(used)}")
print(f"Routes defined: {len(defined_routes)}  |  screens defined: {len(defined_fns)}")
print()
for w in warnings:
    print("WARN:", w)
if errors:
    print()
    for e in errors:
        print("FAIL:", e)
    sys.exit(1)
print("ALL HARD CHECKS PASSED")
