#!/usr/bin/env python3
"""Regression gates for non-destructive shift imports and strict backup types.

This checks source-level ordering and schema guards. Runtime tests and builds
remain mandatory; these assertions alone do not prove transactionality.
"""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent.parent
errors = []

for base, ns in (
    ("android", "hr/raspored/app"),
    ("kalendar/android", "hr/raspored/kalendar"),
):
    path = ROOT / base / "app/src/main/java" / ns / "data/ShiftLibraryStore.kt"
    source = path.read_text(encoding="utf-8")
    body = source.split("    fun importJson(", 1)[1].split("    /** Refuse deletion", 1)[0]
    check = (
        body.find("staged += validatedCustomShift("),
        body.find("for (shift in staged) {"),
        body.find("if (staged.isNotEmpty()) persistCustom()"),
    )
    if -1 in check or check != tuple(sorted(check)):
        errors.append(f"{path}: imported shifts must all validate before the first write")
    if any(line.lstrip().startswith("save(") for line in body.splitlines()):
        errors.append(f"{path}: unvalidated per-item save restored")

    backup = ROOT / base / "app/src/main/java" / ns / "data/ScheduleBackup.kt"
    code = backup.read_text(encoding="utf-8")
    for marker in (
        'root.opt("format") == FORMAT && root.opt("version") == 1',
        "dates.requiredBackupText(key)",
        "item.requiredBackupText(\"code\")",
        "item.requiredBackupText(\"name\")",
        "item.requiredBackupInt(\"background\")",
        "item.requiredBackupInt(\"foreground\")",
        "value is String",
        "value is Int",
    ):
        if marker not in code:
            errors.append(f"{backup}: missing strict backup type guard: {marker}")

# Pixel parity is only meaningful when both macOS jobs use the exact same
# simulator model for the first capture and every fallback.
workflow = (ROOT / ".github/workflows/ci.yml").read_text(encoding="utf-8")
if 'preferred_names = ("iPhone 17 Pro",)' not in workflow:
    errors.append("Primary iOS screenshots are not locked to iPhone 17 Pro")
if 'd.get("isAvailable") and d.get("name") == "iPhone 17 Pro"' not in workflow:
    errors.append("Independent iOS screenshots are not locked to iPhone 17 Pro")
if 'if d.get("isAvailable") and "iPhone" in d.get("name","")' in workflow:
    errors.append("Arbitrary simulator selection can silently change image dimensions")
if 'if item.get("name") == "iPhone 16"' in workflow:
    errors.append("Screenshot recovery must preserve iPhone model/resolution")

if errors:
    print("\n".join("ERROR: " + error for error in errors), file=sys.stderr)
    sys.exit(1)
print("Non-destructive import staging and strict scalar schema guards: OK")
