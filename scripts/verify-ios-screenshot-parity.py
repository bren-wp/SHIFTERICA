#!/usr/bin/env python3
"""Compare genuine iOS Simulator screenshots of presentation-shared screens.

Only screens whose UX, seeded data and layout must be identical are compared.
The system status bar is intentionally excluded: carrier, battery and clock
are outside application control. Different accounting/archive screens remain
outside this comparison by design.
"""
from __future__ import annotations

import argparse
import sys
from pathlib import Path

from PIL import Image, ImageChops, ImageStat

SHARED_SCREENS = (
    "splash.png",
    "search.png",
    "shifts.png",
    "new-shift.png",
    "day-picker.png",
)


def compare_images(reference: Path, candidate: Path, status_bar_pixels: int = 120) -> tuple[float, float]:
    with Image.open(reference) as ref_file, Image.open(candidate) as cand_file:
        ref = ref_file.convert("RGB")
        cand = cand_file.convert("RGB")
    if ref.size != cand.size:
        raise ValueError(f"mismatched screenshot dimensions: {ref.size} != {cand.size}")
    width, height = ref.size
    if width <= 0 or height <= status_bar_pixels:
        raise ValueError(f"invalid image size/crop: {width}x{height}, top={status_bar_pixels}")
    # Blank SpringBoard/transition frames are valid PNG files but NOT real
    # application screenshots. This final gate also catches any screenshot
    # that slipped through the macOS capture-and-retry validation.
    for label, screenshot in (("Raspored", ref), ("Raspored_app", cand)):
        tiny = screenshot.resize((64, 64), Image.Resampling.BILINEAR)
        body = tiny.crop((4, 8, 60, 60))
        pixels = list(body.getdata())
        white_count = sum(
            1 for red, green, blue in pixels
            if red >= 245 and green >= 245 and blue >= 245
        )
        white_fraction = white_count / len(pixels)
        if white_fraction > 0.80:
            raise ValueError(
                f"{label}: invalid blank white screenshot "
                f"({white_fraction:.1%} nearly white)"
            )
    crop = (0, status_bar_pixels, width, height)
    difference = ImageChops.difference(ref.crop(crop), cand.crop(crop))
    max_channel = ImageChops.lighter(
        ImageChops.lighter(difference.getchannel("R"), difference.getchannel("G")),
        difference.getchannel("B"),
    )
    histogram = max_channel.histogram()
    different_pixels = sum(histogram[13:])
    pixels = width * (height - status_bar_pixels)
    mean_absolute_error = sum(ImageStat.Stat(difference).mean) / 3.0
    return different_pixels / pixels, mean_absolute_error


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--raspored", type=Path, required=True)
    parser.add_argument("--kalendar", type=Path, required=True)
    args = parser.parse_args()
    failures = []
    for name in SHARED_SCREENS:
        a, b = args.raspored / name, args.kalendar / name
        if not a.is_file() or not b.is_file():
            failures.append(f"{name}: missing real simulator screenshot: {a} / {b}")
            continue
        try:
            fraction, mean = compare_images(a, b)
        except (OSError, ValueError) as error:
            failures.append(f"{name}: {error}")
            continue
        print(f"{name}: divergent pixels >12/channel: {fraction:.4%}, mean absolute RGB error: {mean:.4f}")
        # An allowance for non-deterministic compositor output is small enough
        # to reject real shifts in layout, colors, icon geometry, and type.
        if fraction > 0.005 or mean > 0.5:
            failures.append(f"{name}: shared iOS visual presentation differs unexpectedly")
    if failures:
        print("\n".join("FAIL: " + message for message in failures), file=sys.stderr)
        return 1
    print("Five shared real-device screenshot comparisons: OK")
    return 0


if __name__ == "__main__":
    sys.exit(main())
