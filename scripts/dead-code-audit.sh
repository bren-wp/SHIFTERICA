#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

SOURCE_DIRS=(
  "android/app/src/main/java"
  "ios/Raspored"
  "kalendar/android/app/src/main/java"
  "kalendar/ios/RasporedApp"
)

fail=0

echo "== Unfinished production markers =="
if grep -R -nE 'TODO|FIXME|HACK|XXX' "${SOURCE_DIRS[@]}"; then
  echo "ERROR: produkcijski source sadrži nedovršene oznake."
  fail=1
fi

echo "== Oversized source files =="
while IFS= read -r file; do
  size="$(wc -c < "$file" | tr -d ' ')"
  limit=18000
  case "$file" in
    kalendar/*) limit=24000 ;;
  esac
  if [ "$size" -gt "$limit" ]; then
    echo "ERROR: $file ima $size B; razdijeli ga na manje cjeline."
    fail=1
  fi
done < <(find "${SOURCE_DIRS[@]}" -type f \( -name '*.kt' -o -name '*.swift' \) | sort)

echo "== Stale built-in shift times =="
if grep -R -nE '08:00[[:space:]]*[–-][[:space:]]*14:00|14:00[[:space:]]*[–-][[:space:]]*21:00|21:00[[:space:]]*[–-][[:space:]]*07:00' "${SOURCE_DIRS[@]}"; then
  echo "ERROR: pronađeno je zastarjelo vrijeme ugrađene smjene."
  fail=1
fi

echo "== Runtime remote visual dependencies =="
if grep -R -nE '(https?:)?//[^"[:space:]]+\.(png|jpe?g|webp|svg|gif)' "${SOURCE_DIRS[@]}"; then
  echo "ERROR: vizualni runtime asset ovisi o udaljenom URL-u."
  fail=1
fi

echo "== Duplicate app section declarations =="
main_section_count="$(grep -R -hE 'enum (class )?MainSection' "${SOURCE_DIRS[@]}" | wc -l | tr -d ' ')"
if [ "$main_section_count" -gt 4 ]; then
  echo "ERROR: pronađene su duplicirane MainSection deklaracije ($main_section_count)."
  fail=1
fi

echo "== Hidden immutable payroll profile =="
if grep -R -nE 'workSector|payrollCoefficient|Bod / koeficijent|Radno okruženje|Grad za obračun poreza|PayrollCoefficientPreset' \
  android/app/src/main/java/hr/raspored/app/ui \
  android/app/src/main/java/hr/raspored/app/data \
  ios/Raspored/SettingsView.swift \
  ios/Raspored/UISettingsStore.swift; then
  echo "ERROR: fiksni obračunski parametri ne smiju biti izloženi u korisničkim postavkama."
  fail=1
fi

for pattern in \
  'in 1..3 -> 1_004.87' \
  'in 4..7 -> 1_015.00' \
  'in 8..11 -> 1_025.00' \
  '12 -> 1_035.00'; do
  if ! grep -q "$pattern" android/app/src/main/java/hr/raspored/app/model/payroll/CroatianPayrollRules.kt; then
    echo "ERROR: Android nema očekivanu službenu osnovicu 2026: $pattern"
    fail=1
  fi
done

for pattern in \
  'case 1...3: return 1_004.87' \
  'case 4...7: return 1_015.00' \
  'case 8...11: return 1_025.00' \
  'case 12: return 1_035.00'; do
  if ! grep -q "$pattern" ios/Raspored/PayrollRules.swift; then
    echo "ERROR: iOS nema očekivanu službenu osnovicu 2026: $pattern"
    fail=1
  fi
done

echo "== Android permission and duplicate import audit =="
python3 - <<'PY'
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

android_namespace = "{http://schemas.android.com/apk/res/android}"
issues = []
for base in (Path("android"), Path("kalendar/android")):
    manifest = base / "app/src/main/AndroidManifest.xml"
    root = ET.parse(manifest).getroot()
    application = root.find("application")
    if application is None:
        issues.append(f"{manifest}: missing application element")
        continue
    for attribute, value in (("allowBackup", "false"), ("usesCleartextTraffic", "false")):
        if application.get(android_namespace + attribute) != value:
            issues.append(f"{manifest}: {attribute} must be false")
    if base == Path("kalendar/android"):
        permissions = {
            item.get(android_namespace + "name")
            for item in root.findall("uses-permission")
        }
        unexpected = permissions.intersection({
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.READ_CONTACTS",
            "android.permission.READ_CALENDAR",
            "android.permission.WRITE_CALENDAR",
        })
        if unexpected:
            issues.append(f"{manifest}: unexpected permissions: {sorted(unexpected)}")

for directory in (
    Path("android/app/src/main/java"),
    Path("ios/Raspored"),
    Path("kalendar/android/app/src/main/java"),
    Path("kalendar/ios/RasporedApp"),
):
    for source in directory.rglob("*"):
        if source.suffix not in (".kt", ".swift") or not source.is_file():
            continue
        imports = [
            line.strip() for line in source.read_text(encoding="utf-8").splitlines()
            if line.strip().startswith("import ")
        ]
        if len(imports) != len(set(imports)):
            issues.append(f"{source}: repeated import declarations")

if issues:
    for issue in issues:
        print("ERROR:", issue)
    sys.exit(1)
print("Android permission and import audit: OK")
PY

echo "== README local asset references =="
python3 - <<'PY'
from pathlib import Path
import re
import sys

readme = Path("README.md").read_text(encoding="utf-8")
missing = []
for value in re.findall(r'(?:src="|\]\()([^")]+)', readme):
    if value.startswith(("http://", "https://", "#", "mailto:")):
        continue
    path = Path(value)
    if path.suffix.lower() in {".png", ".jpg", ".jpeg", ".webp", ".svg", ".gif"} and not path.exists():
        missing.append(value)

if missing:
    for value in missing:
        print(f"ERROR: README referencira nepostojeći lokalni asset: {value}")
    sys.exit(1)
PY

if [ "$fail" -ne 0 ]; then
  exit 1
fi

echo "== Conservative unused source symbol inventory (Android and iOS) =="
python3 - <<'PY'
from pathlib import Path
import re

roots = (
    Path("android/app/src/main/java"),
    Path("ios/Raspored"),
    Path("kalendar/android/app/src/main/java"),
    Path("kalendar/ios/RasporedApp"),
)
symbol_pattern = re.compile(
    r"(?m)^\s*private\s+(?:(?:static|final|lazy|override)\s+)*"
    r"(?:fun|func|val|var|class|struct|object|enum)\s+"
    r"(?:[A-Za-z_]\w*\.)?([A-Za-z_]\w*)\b"
)
candidates = []
for root in roots:
    files = sorted(
        p for p in root.rglob("*")
        if p.is_file() and p.suffix in {".kt", ".swift"}
    )
    # Audit only inside the corresponding installed application. Names shared
    # by the four independent targets must not mask otherwise unused symbols.
    contents = {str(p): p.read_text(encoding="utf-8") for p in files}
    all_text = "\n".join(contents.values())
    for path, source in contents.items():
        for match in symbol_pattern.finditer(source):
            symbol = match.group(1)
            # Exactly one lexical occurrence is a review candidate, never
            # sufficient evidence that platform callbacks/reflection are dead.
            uses = len(re.findall(r"\b" + re.escape(symbol) + r"\b", all_text))
            if uses == 1:
                line = source.count("\n", 0, match.start()) + 1
                candidates.append((path, line, symbol))
    print(f"{root}: reviewed {len(files)} Kotlin/Swift source files")

for path, line, name in sorted(candidates):
    print(f"REVIEW CANDIDATE (do not delete automatically): {path}:{line} {name}")
print(f"Conservative unused-symbol candidates requiring inspection: {len(candidates)}")

# A previously silent 200-record cutoff in both calendars would hide user
# data; keep this regression check independent of the UI unit tests.
search_paths = (
    Path("kalendar/android/app/src/main/java/hr/raspored/kalendar/CalendarSearch.kt"),
    Path("kalendar/ios/RasporedApp/CalendarSearch.swift"),
)
for path in search_paths:
    source = path.read_text(encoding="utf-8")
    if re.search(r"(?:take|prefix)\s*\(\s*200\s*\)", source):
        raise SystemExit(f"ERROR: hidden 200-record calendar search cap in {path}")
print("Calendar search completeness audit: OK")
PY

echo "Dead-code / maintenance audit: OK"
