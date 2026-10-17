#!/usr/bin/env python3
"""Runtime parity gate: the independent application uses actual original screens."""
from pathlib import Path
import sys
import re

original_android = Path("android/app/src/main/java/hr/raspored/app/ui")
calendar_android = Path("kalendar/android/app/src/main/java/hr/raspored/kalendar/ui")
original_ios = Path("ios/Raspored")
calendar_ios = Path("kalendar/ios/RasporedApp")
issues = []

android_core = (
    "RasporedApp.kt", "RasporedTheme.kt", "AppHeader.kt", "MonthCalendar.kt",
    "MonthControls.kt", "MonthScreen.kt", "YearScreen.kt", "SplashView.kt",
    "NewShiftSheet.kt", "SearchSheet.kt", "ShiftManagerSheet.kt", "SettingsSheet.kt"
)
ios_core = (
    "RootView.swift", "Theme.swift", "MonthCalendarComponents.swift",
    "MonthControls.swift", "MonthView.swift", "YearOverviewView.swift",
    "SearchView.swift", "SettingsView.swift", "ShiftManagerView.swift",
    "NewShiftView.swift", "WorkTime.swift", "ShiftReminders.swift"
)
for name in android_core:
    source = original_android / name
    clone = calendar_android / name
    if not source.is_file() or not clone.is_file():
        issues.append(f"Missing actual Android screen: {name}")
        continue
    # Only intentionally duplicated presentation code should remain truly identical.
    if name in ("AppHeader.kt", "MonthCalendar.kt", "YearScreen.kt", "RasporedTheme.kt"):
        expected = source.read_text().replace("hr.raspored.app", "hr.raspored.kalendar")
        if clone.read_text() != expected:
            issues.append(f"Android parity changed unexpectedly: {name}")

for name in ios_core:
    source = original_ios / name
    clone = calendar_ios / name
    if not source.is_file() or not clone.is_file():
        issues.append(f"Missing actual iOS screen: {name}")
        continue
    if name in ("MonthCalendarComponents.swift", "YearOverviewView.swift", "Theme.swift"):
        if source.read_text() != clone.read_text():
            issues.append(f"iOS parity changed unexpectedly: {name}")

required = (
    calendar_android / "CalculationProfileEditor.kt",
    calendar_ios / "CalculationProfileEditor.swift",
    Path("kalendar/android/app/src/main/java/hr/raspored/kalendar/model/payroll/PayrollEstimator.kt"),
    calendar_ios / "PayrollEstimator.swift"
)
for path in required:
    if not path.is_file():
        issues.append(f"Missing reusable calculation component: {path}")
android_manifest = Path("kalendar/android/app/src/main/AndroidManifest.xml").read_text()
gradle = Path("kalendar/android/app/build.gradle.kts").read_text()
ios_project = Path("kalendar/ios/project.yml").read_text()
if "hr.raspored.kalendar" not in gradle or "hr.raspored.kalendar" not in ios_project:
    issues.append("Independent app identifiers not preserved")
if "android.permission.INTERNET" in android_manifest:
    issues.append("Independent calendar unexpectedly requires Internet permission")
if "DEFAULT_COEFFICIENT" in (calendar_android.parent / "model/payroll/CroatianPayrollRules.kt").read_text():
    issues.append("A personal coefficient remains hard-coded")
if "defaultCoefficient" in (calendar_ios / "PayrollRules.swift").read_text():
    issues.append("An iOS personal coefficient remains hard-coded")
for path in (calendar_android / "MonthInsights.kt", calendar_ios / "MonthInsightsView.swift"):
    if "userCoefficient" not in path.read_text() or "userMonthlyBase" not in path.read_text():
        issues.append(f"Calculation profile not wired into {path}")
# Legacy universal-calendar event collections are archived, never discarded
# by replacing their storage with the original one-shift-per-date model.
android_settings = (calendar_android / "SettingsSheet.kt").read_text()
ios_settings = (calendar_ios / "SettingsView.swift").read_text()
for fragment in ("Prethodni događaji", "CalendarBackup.encode",
                 "CalendarBackup.decode", "mergeMissing"):
    if fragment not in android_settings:
        issues.append("Android legacy archive missing: " + fragment)
    if fragment not in ios_settings:
        issues.append("iOS legacy archive missing: " + fragment)
legacy_android = Path(
    "kalendar/android/app/src/main/java/hr/raspored/kalendar/CalendarStore.kt"
).read_text()
legacy_ios = (calendar_ios / "CalendarStore.swift").read_text()
if '"raspored.universal.v1"' not in legacy_android:
    issues.append("Android original universal event preference key was changed")
if '"hr.raspored.universal.calendar.v1"' not in legacy_ios:
    issues.append("iOS original universal event preference key was changed")
# Avoid silent truncation of preserved universal event records in archive UI.
for path in (
    calendar_android / "LegacyArchiveDialog.kt",
    calendar_ios / "LegacyArchiveSheet.swift",
):
    if not path.is_file():
        issues.append(f"Missing full archived event browser: {path}")
    else:
        ui = path.read_text()
        if "take(200)" in ui or "prefix(200)" in ui:
            issues.append(f"Archived events are truncated in {path}")
        if "search" not in ui.lower() and "query" not in ui.lower():
            issues.append(f"Archived events cannot be searched in {path}")
# All clone imports must remain isolated even when source components match.
for path in Path("kalendar").rglob("*"):
    if path.suffix in {".kt", ".swift"} and path.is_file():
        for index, line in enumerate(path.read_text().splitlines(), 1):
            if re.match(r"^\s*(import|package)\s+", line) and "hr.raspored.app." in line:
                issues.append(f"Original app dependency in {path}:{index}")
workflow = Path(".github/workflows/ci.yml").read_text()
for screen in ("calendar", "year", "summary", "settings"):
    if f'("{screen}",' not in workflow:
        issues.append(f"Missing real simulator screenshot: {screen}")
if issues:
    print("\n".join("ERROR: " + issue for issue in issues), file=sys.stderr)
    sys.exit(1)
print("Independent calendar original-screen parity, calculations and isolation: OK")
