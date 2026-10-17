#!/usr/bin/env python3
"""Whole-target conservative dead-code/UX audit for both Android and iOS apps.

Static lexical candidates are *not* proof of dead code: launchers, JSON models,
SwiftUI property wrappers, reflection, and platform callbacks can be referenced
without a plain source-level call. Compiler, lint and runtime QA remain required.
"""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REPORT = ROOT / "build/reports/full-source-audit.json"
TARGETS = {
    "raspored-android": ("android/app/src/main/java", ("android/app/src/test",)),
    "raspored-ios": ("ios/Raspored", ("ios/tests",)),
    "kalendar-android": (
        "kalendar/android/app/src/main/java", ("kalendar/android/app/src/test",)
    ),
    "kalendar-ios": ("kalendar/ios/RasporedApp", ("kalendar/ios/tests",)),
}
UNREACHABLE_SCREEN_FILES = {
    "kalendar-android": (
        "hr/raspored/kalendar/CalendarScreen.kt",
        "hr/raspored/kalendar/CalendarSettings.kt",
        "hr/raspored/kalendar/CalendarSplash.kt",
        "hr/raspored/kalendar/CalendarSummary.kt",
        "hr/raspored/kalendar/CalendarRecordsSheet.kt",
        "hr/raspored/kalendar/CalendarInsights.kt",
        "hr/raspored/kalendar/CalendarTiles.kt",
        "hr/raspored/kalendar/CalendarParity.kt",
    ),
    "kalendar-ios": (
        "CalendarHome.swift", "CalendarEditor.swift",
        "CalendarParityViews.swift", "CalendarSplash.swift",
        "CalendarSummary.swift", "CalendarRecordsSheet.swift",
        "CalendarSettings.swift",
    ),
}
TYPE_DECLARATION = re.compile(
    r"(?m)^(?:@\w+(?:\([^)]*\))?\s+)*"
    r"(?:(?:public|private|internal|fileprivate|open|final|data|sealed|"
    r"abstract|indirect)\s+)*"
    r"(?:enum\s+class|class|struct|object|interface|protocol|"
    r"typealias|enum)\s+([A-Z]\w*)\b"
)
KOTLIN_IMPORT = re.compile(r"(?m)^\s*import\s+([\w.]+)(?:\s+as\s+(\w+))?\s*$")
PRIVATE_SYMBOL = re.compile(
    r"(?m)^\s*(?:private|fileprivate)\s+"
    r"(?:(?:static|final|lazy|override)\s+)*"
    r"(?:fun|func|val|var|class|struct|object|enum)\s+(\w+)\b"
)
ROOTS = [
    "android/app/src/main/java/hr/raspored/app/ui/MonthCalendar.kt",
    "kalendar/android/app/src/main/java/hr/raspored/kalendar/ui/MonthCalendar.kt",
    "ios/Raspored/MonthCalendarComponents.swift",
    "kalendar/ios/RasporedApp/MonthCalendarComponents.swift",
]
issues: list[str] = []
report: dict[str, object] = {"method": "target-scoped lexical review and regression checks",
                             "targets": {}, "blocking_issues": issues}


def source_files(directory: str) -> list[Path]:
    return sorted(p for p in (ROOT / directory).rglob("*")
                  if p.is_file() and p.suffix in (".kt", ".swift"))


for target, (directory, test_directories) in TARGETS.items():
    src_files = source_files(directory)
    test_files = [p for path in test_directories for p in source_files(path)]
    files = {str(p.relative_to(ROOT)): p.read_text(encoding="utf-8")
             for p in src_files}
    tests = "\n".join(p.read_text(encoding="utf-8") for p in test_files)
    all_code = "\n".join(files.values()) + "\n" + tests
    referenced = {}
    types = []
    private_candidates = []
    import_candidates = []
    force_unwrap = []
    for name, code in files.items():
        if name.endswith(".kt"):
            body = KOTLIN_IMPORT.sub("", code)
            for match in KOTLIN_IMPORT.finditer(code):
                qualified, alias = match.groups()
                imported = alias or qualified.rsplit(".", 1)[-1]
                if imported == "*" or imported == "_":
                    continue
                if not re.search(r"\b" + re.escape(imported) + r"\b", body):
                    import_candidates.append(f"{name}:{code.count(chr(10), 0, match.start()) + 1}: {imported}")
            if "!!" in code:
                force_unwrap.append(name)
        elif re.search(r"\b(?:try|as)!\b", code):
            force_unwrap.append(name)

        for regex, collection in (
            (TYPE_DECLARATION, types), (PRIVATE_SYMBOL, private_candidates)
        ):
            for match in regex.finditer(code):
                symbol = match.group(1)
                if symbol not in referenced:
                    referenced[symbol] = len(re.findall(
                        r"\b" + re.escape(symbol) + r"\b", all_code
                    ))
                collection.append((name, symbol))
    orphan_types = [f"{path}: {symbol}" for path, symbol in types
                    if referenced[symbol] == 1]
    private_candidates = [f"{path}: {symbol}" for path, symbol in private_candidates
                          if referenced[symbol] == 1]
    for relative in UNREACHABLE_SCREEN_FILES.get(target, ()):
        if (ROOT / directory / relative).exists():
            issues.append(f"Retired UI screen returned: {directory}/{relative}")

    report["targets"][target] = {
        "source_files": len(files), "test_files": len(test_files),
        "source_lines": sum(len(s.splitlines()) for s in files.values()),
        "declared_types": len(types),
        "type_review_candidates": orphan_types,
        "private_symbol_review_candidates": private_candidates,
        "unused_import_review_candidates": import_candidates,
        "force_unwrap_review_files": force_unwrap,
    }
    print(f"{target}: {len(files)} source + {len(test_files)} test files, "
          f"{len(types)} types, {len(orphan_types)} type candidates, "
          f"{len(import_candidates)} import candidates", flush=True)
    for warning in (orphan_types + private_candidates + import_candidates)[:12]:
        print("  REVIEW (never auto-delete):", warning)

# The real month navigation must expose Today consistently on all four targets.
for name in ROOTS:
    source = (ROOT / name).read_text(encoding="utf-8")
    if '"Danas"' not in source:
        issues.append(f"Missing one-tap current month return: {name}")
    if name.endswith(".swift") and ".accessibilityLabel(" not in source:
        issues.append(f"iOS day cells lack accessibility labels: {name}")
    if name.endswith(".kt") and "contentDescription = accessibilityLabel" not in source:
        issues.append(f"Android day cells lack accessibility labels: {name}")

for name in (
    "android/app/src/main/java/hr/raspored/app/ui/MonthControls.kt",
    "kalendar/android/app/src/main/java/hr/raspored/kalendar/ui/MonthControls.kt",
    "ios/Raspored/MonthControls.swift",
    "kalendar/ios/RasporedApp/MonthControls.swift",
):
    source = (ROOT / name).read_text()
    if "UPRAVLJANJE SMJENAMA" not in source:
        issues.append(f"Ambiguous shift-management action returned: {name}")

for name in (
    "kalendar/android/app/src/main/java/hr/raspored/kalendar/CalendarStore.kt",
    "kalendar/android/app/src/main/java/hr/raspored/kalendar/CalendarBackup.kt",
    "kalendar/ios/RasporedApp/CalendarStore.swift",
    "kalendar/ios/RasporedApp/CalendarBackup.swift",
):
    if not (ROOT / name).is_file():
        issues.append(f"User archive compatibility file missing: {name}")

REPORT.parent.mkdir(parents=True, exist_ok=True)
REPORT.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n",
                  encoding="utf-8")
if issues:
    for issue in issues:
        print("ERROR:", issue, file=sys.stderr)
    sys.exit(1)
print("All four app targets audited; detailed JSON:", REPORT.relative_to(ROOT))
