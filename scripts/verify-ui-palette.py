#!/usr/bin/env python3
"""Zero-dependency Android/iOS color parity and accessibility regression gate."""
import json
import re
import sys
from pathlib import Path
from xml.etree import ElementTree

ROOT = Path(__file__).resolve().parent.parent
ANDROID = (ROOT / "android/app/src/main/java/hr/raspored/app/ui/RasporedTheme.kt").read_text()
IOS = (ROOT / "ios/Raspored/Theme.swift").read_text()
SHIFT_ANDROID = (ROOT / "android/app/src/main/java/hr/raspored/app/model/ShiftModel.kt").read_text()
SHIFT_IOS = (ROOT / "ios/Raspored/ShiftModel.swift").read_text()

TOKENS = (
    "Bg", "Bg2", "Card", "Card2", "Stroke", "Text", "Muted",
    "Accent", "Accent2", "Weekend", "Danger", "Night", "Day",
    "Annual", "Morning", "Afternoon", "Sick", "Empty", "WeekendEmpty",
)
SHIFTS = ("night", "day", "annual", "morning", "afternoon", "sick")


def color_in(source: str, expression: str) -> str:
    hit = re.search(expression, source)
    if not hit:
        raise ValueError("Missing color source: " + expression)
    return hit.group(1).upper()[-6:]


def android(token: str) -> str:
    return color_in(ANDROID, r"\bval\s+" + token + r"\s*=\s*Color\(0x([A-Fa-f0-9]{8})\)")


def ios(token: str) -> str:
    name = token[0].lower() + token[1:]
    return color_in(IOS, r"\bstatic\s+let\s+" + name + r"\s*=\s*Color\(hex:\s*0x([A-Fa-f0-9]{6})\)")


def linear(channel: int) -> float:
    value = channel / 255
    return value / 12.92 if value <= 0.04045 else ((value + 0.055) / 1.055) ** 2.4


def luminance(hex_rgb: str) -> float:
    values = [linear(int(hex_rgb[pos:pos + 2], 16)) for pos in (0, 2, 4)]
    return sum(a * b for a, b in zip(values, (0.2126, 0.7152, 0.0722)))


def contrast(foreground: str, background: str) -> float:
    lighter, darker = sorted((luminance(foreground), luminance(background)), reverse=True)
    return (lighter + 0.05) / (darker + 0.05)


def check(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    colors = {}
    for key in TOKENS:
        a = android(key)
        b = ios(key)
        check(a == b, f"{key} Android #{a} != iOS #{b}")
        colors[key] = a
    for key in SHIFTS:
        kotlin = color_in(
            SHIFT_ANDROID,
            r"\bval\s+" + key + r"\s*=\s*ShiftType\([^\n]*?color\s*=\s*Color\(0x([A-Fa-f0-9]{8})\)"
        )
        swift = color_in(
            SHIFT_IOS,
            r"\bstatic\s+let\s+" + key + r"\s*=\s*ShiftTypeDef\([^\n]*?backgroundHex:\s*0x([A-Fa-f0-9]{6})"
        )
        check(kotlin == swift, f"Default shift {key}: Android #{kotlin} != iOS #{swift}")

    for label, fg, bg, minimum in (
        ("primary text on surface", colors["Text"], colors["Card"], 7.0),
        ("muted text on surface", colors["Muted"], colors["Card"], 4.5),
        ("primary text on background", colors["Text"], colors["Bg"], 7.0),
        ("accent icon on surface", colors["Accent"], colors["Card"], 3.0),
        ("muted navigation icon", colors["Muted"], colors["Card2"], 3.0),
        ("dark action icon on mint", "081A25", colors["Accent"], 4.5),
    ):
        ratio = contrast(fg, bg)
        check(ratio >= minimum, f"{label} contrast {ratio:.2f}:1 below {minimum}:1")
        print(f"{label}: {ratio:.2f}:1")

    asset = json.loads(
        (ROOT / "ios/Raspored/Assets.xcassets/AccentColor.colorset/Contents.json").read_text()
    )
    components = asset["colors"][0]["color"]["components"]
    expected = [int(colors["Accent"][i:i + 2], 16) / 255 for i in (0, 2, 4)]
    observed = [float(components[k]) for k in ("red", "green", "blue")]
    check(all(abs(a - b) < 0.002 for a, b in zip(expected, observed)),
          "iOS asset catalog accent differs from Android and iOS theme")

    for path in (
        "docs/assets/raspored-logo.svg", "docs/assets/app-icon.svg",
        "docs/assets/ui-showcase.svg", "android/app/src/main/res/drawable/app_icon.xml",
        "android/app/src/main/res/values/colors.xml",
    ):
        ElementTree.parse(ROOT / path)

    print("UI palette parity, icon vector XML and contrast: OK")


if __name__ == "__main__":
    try:
        main()
    except (AssertionError, ValueError, OSError, KeyError, ElementTree.ParseError) as exc:
        print("UI theme regression: " + str(exc), file=sys.stderr)
        sys.exit(1)
