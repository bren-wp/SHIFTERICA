#!/usr/bin/env python3
"""Launcher UI, optimization and obsolete runtime source regression gates."""
from pathlib import Path
from xml.etree import ElementTree as ET
import re
import sys

ROOT = Path(__file__).resolve().parent.parent
ANDROID_NS = "{http://schemas.android.com/apk/res/android}"
issues = []
old_android = (
    "CalendarScreen.kt", "CalendarSettings.kt", "CalendarSplash.kt",
    "CalendarSummary.kt", "CalendarRecordsSheet.kt", "CalendarInsights.kt",
    "CalendarTiles.kt", "CalendarParity.kt",
)
old_ios = (
    "CalendarHome.swift", "CalendarEditor.swift", "CalendarParityViews.swift",
    "CalendarSplash.swift", "CalendarSummary.swift",
    "CalendarRecordsSheet.swift", "CalendarSettings.swift",
)

def expect(flag: bool, message: str) -> None:
    if not flag:
        issues.append(message)

for base in ("android", "kalendar/android"):
    root = ROOT / base / "app/src/main"
    manifest = ET.parse(root / "AndroidManifest.xml")
    application = manifest.find("./application")
    expect(application is not None, f"{base}: missing application")
    if application is not None:
        for key in ("icon", "roundIcon"):
            expect(application.get(ANDROID_NS + key) == "@mipmap/ic_launcher",
                   f"{base}: {key} is not the adaptive icon")
    adaptive = ET.parse(root / "res/mipmap-anydpi-v26/ic_launcher.xml").getroot()
    expect(adaptive.tag == "adaptive-icon", f"{base}: invalid adaptive icon root")
    for tag, expected in (
        ("background", "@color/launcher_icon_background"),
        ("foreground", "@drawable/app_icon"),
        ("monochrome", "@drawable/app_icon_monochrome"),
    ):
        child = adaptive.find(tag)
        expect(child is not None and child.get(ANDROID_NS + "drawable") == expected,
               f"{base}: missing {tag} launcher icon layer")
    vector = ET.parse(root / "res/drawable/app_icon.xml").getroot()
    expect(vector.tag == "vector", f"{base}: icon must be vector")
    expect(any(float(g.get(ANDROID_NS + "scaleX", "1")) >= 1.15
               for g in vector.iter("group")),
           f"{base}: small legacy launcher icon motif returned")
    expect(ET.parse(root / "res/drawable/app_icon_monochrome.xml")
           .getroot().tag == "vector", f"{base}: themed mark is invalid")
    colors = (root / "res/values/colors.xml").read_text()
    expect('name="launcher_icon_background">#090F1B' in colors,
           f"{base}: missing full-bleed dark background")
    gradle = (ROOT / base / "app/build.gradle.kts").read_text()
    expect("isMinifyEnabled = true" in gradle and
           "isShrinkResources = true" in gradle,
           f"{base}: release build is no longer size-optimized")

for path in old_android:
    expect(not (ROOT / "kalendar/android/app/src/main/java/hr/raspored/kalendar" / path).exists(),
           f"Dead Android calendar UI reintroduced: {path}")
for path in old_ios:
    expect(not (ROOT / "kalendar/ios/RasporedApp" / path).exists(),
           f"Dead iOS calendar UI reintroduced: {path}")
for path in ("CalendarStore.kt", "CalendarEntry.kt", "CalendarBackup.kt",
             "CalendarShiftPresets.kt", "CalendarLegacyFormatting.kt"):
    expect((ROOT / "kalendar/android/app/src/main/java/hr/raspored/kalendar" / path).is_file(),
           f"Legacy Android event data model lost: {path}")
for path in ("CalendarStore.swift", "CalendarEntry.swift", "CalendarBackup.swift"):
    expect((ROOT / "kalendar/ios/RasporedApp" / path).is_file(),
           f"Legacy iOS event data model lost: {path}")

svg = ET.parse(ROOT / "docs/assets/app-icon.svg").getroot()
expect(any("scale(1.17)" in x.get("transform", "") for x in svg.iter()),
       "Public SVG launcher motif was not resized")
icon_generator = (ROOT / "ios/generate_app_icon.swift").read_text()
expect("ctx.scaleBy(x: 1.17, y: 1.17)" in icon_generator,
       "Both iOS generated icons must use the enlarged artwork")
for path in (
    "android/app/src/main/java/hr/raspored/app/ui/YearScreen.kt",
    "kalendar/android/app/src/main/java/hr/raspored/kalendar/ui/YearScreen.kt",
):
    expect("GridCells.Adaptive(" in (ROOT / path).read_text(),
           f"{path}: fixed year month grid is not compact-phone friendly")
for path in (
    "ios/Raspored/YearOverviewView.swift",
    "kalendar/ios/RasporedApp/YearOverviewView.swift",
):
    expect("GridItem(.adaptive(" in (ROOT / path).read_text(),
           f"{path}: fixed year grid is not compact-phone friendly")

if issues:
    print("\n".join("ERROR: " + x for x in issues), file=sys.stderr)
    sys.exit(1)
print("Four app icons, adaptive year grids, shrunk releases, legacy source inventory: OK")
