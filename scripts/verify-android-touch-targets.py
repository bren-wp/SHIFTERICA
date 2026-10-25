#!/usr/bin/env python3
"""Guard the 48dp minimum for the named interactive Android controls.

The seven-column calendar cells are a separate density-constrained navigation
surface, not independently proven to be 48dp by these checks.
"""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent.parent
CASES = {
    "ui/MonthCalendar.kt": (
        ".heightIn(min = 48.dp)",
        "modifier = Modifier.size(48.dp).semantics { contentDescription = label }",
    ),
    "ui/MonthControls.kt": (
        ".height(48.dp)",
        "modifier = Modifier.size(48.dp)",
    ),
    "ui/SearchSheet.kt": (
        "onClick = onDismiss,",
        "modifier = Modifier.size(48.dp)",
    ),
    "ui/YearScreen.kt": (
        "onClick = { onYearChange(year - 1) },\n                    modifier = Modifier.size(48.dp)",
        "onClick = { onYearChange(year + 1) },\n                    modifier = Modifier.size(48.dp)",
    ),
}

errors = []
for root in (
    ROOT / "android/app/src/main/java/hr/raspored/app",
    ROOT / "kalendar/android/app/src/main/java/hr/raspored/kalendar",
):
    for filename, expected in CASES.items():
        path = root / filename
        if not path.exists():
            errors.append(f"Missing screen: {path}")
            continue
        source = path.read_text(encoding="utf-8")
        for fragment in expected:
            if fragment not in source:
                errors.append(f"Missing minimum Android touch area in {path}: {fragment}")
if errors:
    print("\n".join("ERROR: " + error for error in errors), file=sys.stderr)
    sys.exit(1)
print("48dp interactive controls in both Android apps: OK")
