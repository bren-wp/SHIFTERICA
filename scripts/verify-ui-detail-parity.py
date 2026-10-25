#!/usr/bin/env python3
"""Strict UI source-parity and usability gates for both separately installed apps.

This proves shared presentation source and launcher resources are identical;
it does not assert image pixels match across different OS rendering engines.
Actual iOS simulator screenshots are an independent required CI job.
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

root = Path(__file__).resolve().parent.parent
a = root / "android/app/src/main/java/hr/raspored/app/ui"
b = root / "kalendar/android/app/src/main/java/hr/raspored/kalendar/ui"
i = root / "ios/Raspored"
j = root / "kalendar/ios/RasporedApp"
issues: list[str] = []
report: dict[str, object] = {"android_shared": [], "ios_shared": [],
                             "documented_intentional_differences": [],
                             "errors": issues}
# Payroll inputs and the legacy archive intentionally differ in the
# independent application. Never replace them with the original payslip UI.
intentional_android = {
    "SettingsSheet.kt", "SummaryScreen.kt", "MonthInsights.kt",
    "PayrollEstimateCard.kt", "AccountingComponents.kt",
}
intentional_ios = {
    "RasporedApp.swift", "RootView.swift", "SettingsView.swift",
    "SummaryView.swift", "MonthInsightsView.swift", "PayrollEstimateCard.swift",
    "MonthlyAccountingStore.swift", "PayrollEstimator.swift", "PayrollRules.swift",
}
def check_equal(first: Path, second: Path, android: bool, label: str) -> None:
    if not first.exists() or not second.exists():
        issues.append(f"{label}: missing shared visual file: {first} / {second}")
        return
    left = first.read_text(encoding="utf-8")
    right = second.read_text(encoding="utf-8")
    if android:
        left = left.replace("hr.raspored.app", "hr.raspored.kalendar")
    if left != right:
        issues.append(f"{label}: exact shared presentation drift: {first.name}")
    else:
        report[label].append(first.name)

# Include ALL matching presentation files except explicitly different
# accounting/archive screens, not just a hand-picked set of four pages.
for first in sorted(a.glob("*.kt")):
    if first.name not in intentional_android and first.name != "ConfirmedNetEditor.kt":
        check_equal(first, b / first.name, True, "android_shared")
for first in sorted(i.glob("*.swift")):
    if first.name in intentional_ios or first.name == "ConfirmedNetEditor.swift":
        continue
    counterpart = j / first.name
    if counterpart.exists():
        check_equal(first, counterpart, False, "ios_shared")

for name in ("AppHeader.kt", "MonthCalendar.kt", "MonthControls.kt",
             "SearchSheet.kt", "SettingsComponents.kt", "ShiftManagerSheet.kt",
             "NewShiftSheet.kt", "NewShiftComponents.kt", "YearScreen.kt",
             "SupportDialog.kt", "SplashView.kt"):
    if name not in report["android_shared"]:
        issues.append("Core Android UI component not identical: " + name)
for name in ("Theme.swift", "MonthCalendarComponents.swift",
             "MonthControls.swift", "NewShiftView.swift", "SearchView.swift",
             "ShiftManagerView.swift", "YearOverviewView.swift",
             "BuiltInShiftColorView.swift", "DocumentationMode.swift"):
    if name not in report["ios_shared"]:
        issues.append("Core iOS UI component not identical: " + name)

for rel in ("res/drawable/app_icon.xml",
            "res/drawable/app_icon_monochrome.xml",
            "res/mipmap-anydpi-v26/ic_launcher.xml",
            "res/values/colors.xml"):
    left = (root / "android/app/src/main" / rel).read_bytes()
    right = (root / "kalendar/android/app/src/main" / rel).read_bytes()
    if left != right:
        issues.append("Launcher icon resources drifted: " + rel)

def must_contain(path: Path, markers: tuple[str, ...]) -> None:
    contents = path.read_text(encoding="utf-8")
    for marker in markers:
        if marker not in contents:
            issues.append(f"Missing responsive/accessible behavior: {path}: {marker}")

for folder in (a, b):
    must_contain(folder / "MonthControls.kt",
                 ("LazyColumn(", "heightIn(max = 560.dp)", "contentDescription =",
                  "this.selected = selected", "items(gridRows,"))
    must_contain(folder / "SettingsComponents.kt",
                 (".toggleable(", "role = Role.Switch", "onCheckedChange = null",
                  "heightIn(min = 48.dp)", "if (onClick != null)"))
    must_contain(folder / "SearchSheet.kt",
                 ('"Očisti pretragu"', ".imePadding()", "shiftsByCode[code]"))
for folder in (i, j):
    must_contain(folder / "MonthControls.swift",
                 ('"Odabrano"', ".accessibilityLabel("))
    must_contain(folder / "SettingsView.swift",
                 ("Toggle(isOn: value)", ".accessibilityHint(subtitle)",
                  ".frame(minHeight: 48)"))
    must_contain(folder / "SearchView.swift",
                 ('"Očisti pretragu"', "LazyVStack(", ".autocorrectionDisabled()"))
    must_contain(folder / "DocumentationMode.swift",
                 ('case search', 'case dayPicker = "day-picker"'))
    must_contain(folder / "MonthCalendarComponents.swift",
                 ("CalendarGridDates.build(", "showOutsideDays: settings.showOutsideDays"))
    must_contain(folder / "CalendarGridDates.swift",
                 ("return (0..<cellCount).map { index -> Date? in",))
    grid = (folder / "CalendarGridDates.swift").read_text(encoding="utf-8")
    if ".compactMap" in grid:
        issues.append(f"Month grid must preserve nil placeholder cells: {folder}")

workflow = (root / ".github/workflows/ci.yml").read_text()
if "  ios_calendar:\n" not in workflow or "needs: [android, ios, ios_calendar]" not in workflow:
    issues.append("Independent calendar QA must run on its own mandatory macOS runner")
for flag in ("--qa-calendar-shifts", "--qa-calendar-new-shift",
             "--qa-calendar-search", "--qa-calendar-day-picker",
             "--qa-calendar-splash"):
    if flag not in workflow:
        issues.append("Missing required real-device screenshot route: " + flag)
# Must keep genuine user-specific calculation data isolated.
for path in (
    b / "CalculationProfileEditor.kt",
    j / "CalculationProfileEditor.swift",
    j / "LegacyArchiveSheet.swift",
):
    if not path.exists():
        issues.append("User-entered calculation/archive screen missing: " + str(path))

report["documented_intentional_differences"] = [
    "User-editable coefficient and monthly base instead of private payslip rows",
    "Previous standalone calendar archived without lossy shift conversion",
    "Independent bundle IDs and DEBUG-only simulator QA route switches",
]
output = root / "build/reports/ui-source-parity.json"
output.parent.mkdir(parents=True, exist_ok=True)
output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n",
                  encoding="utf-8")
print(f"Exact Android shared UI: {len(report['android_shared'])} files")
print(f"Exact iOS shared UI: {len(report['ios_shared'])} files")
if issues:
    for item in issues:
        print("ERROR:", item, file=sys.stderr)
    sys.exit(1)
print("All shared UI sources, interaction targets and screenshots QA routes: OK")
