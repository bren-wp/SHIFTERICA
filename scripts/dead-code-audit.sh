#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

SOURCE_DIRS=(
  "android/app/src/main/java"
  "ios/Raspored"
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
  if [ "$size" -gt 18000 ]; then
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
if [ "$main_section_count" -gt 2 ]; then
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

echo "Dead-code / maintenance audit: OK"
