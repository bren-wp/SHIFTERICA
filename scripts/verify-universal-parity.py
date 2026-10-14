#!/usr/bin/env python3
"""Guard original-calendar UI shell and standalone, payroll-free implementation."""
from pathlib import Path
import re
import sys

android = Path("kalendar/android/app/src/main/java/hr/raspored/kalendar")
ios = Path("kalendar/ios/RasporedApp")
checks = [
    (android / "CalendarParity.kt", ("Mjesec", "Godina", "Sažetak", "ParityHeader", "ParityBottomNav", "0xFF090F1B")),
    (android / "CalendarTiles.kt", ("PON", "UTO", "SUB", "NED", "RoundedCornerShape(13.dp)")),
    (android / "CalendarSplash.kt", ("Pametni planer smjena", "CalendarSplashMark")),
    (android / "CalendarScreen.kt", ("CalendarSection.MONTH", "CalendarSection.YEAR", "CalendarSection.SUMMARY")),
    (ios / "CalendarParityViews.swift", ("Mjesec", "Godina", "Sažetak", "CalendarParityHeader", "CalendarParityBottomNav", "CalendarParityMonthGrid")),
    (ios / "CalendarSplash.swift", ("Pametni planer smjena", "CalendarParityBrandMark")),
    (ios / "CalendarHome.swift", ("CalendarParityHeader", "CalendarParityBottomNav", "CalendarParityMonthGrid")),
]
issues = []
for path, needles in checks:
    if not path.exists():
        issues.append(f"{path}: missing parity component")
        continue
    source = path.read_text(encoding="utf-8")
    for needle in needles:
        if needle not in source:
            issues.append(f"{path}: missing anchor {needle}")

for directory in (android, ios):
    for path in directory.rglob("*"):
        if not path.is_file() or path.suffix not in (".kt", ".swift"):
            continue
        for lineno, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
            if re.match(r"^\s*(import|package)\s+", line) and (
                "hr.raspored.app." in line or
                any(token in line.lower() for token in ("payroll", "monthlyaccounting", "wage", "taxcalculator"))
            ):
                issues.append(f"{path}:{lineno}: forbidden payroll/original app dependency")

if "0xFF090F1B" not in Path("android/app/src/main/java/hr/raspored/app/ui/RasporedTheme.kt").read_text(encoding="utf-8"):
    issues.append("Original Android branding background is missing")
if issues:
    print("\n".join("ERROR: " + issue for issue in issues), file=sys.stderr)
    sys.exit(1)
print("Universal calendar visual-shell / payroll-isolation audit: OK")
