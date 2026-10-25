#!/usr/bin/env python3
"""Regression for CoreSimulator white frame detection in screenshot parity."""
from __future__ import annotations

import importlib.util
from pathlib import Path
from tempfile import TemporaryDirectory
import unittest

from PIL import Image, ImageDraw

SCRIPT = Path(__file__).with_name("verify-ios-screenshot-parity.py")
spec = importlib.util.spec_from_file_location("visual_parity", SCRIPT)
assert spec is not None and spec.loader is not None
visual_parity = importlib.util.module_from_spec(spec)
spec.loader.exec_module(visual_parity)


class ScreenshotParityTests(unittest.TestCase):
    def test_matching_real_dark_app_screens_pass(self) -> None:
        with TemporaryDirectory() as folder:
            first, second = Path(folder) / "first.png", Path(folder) / "second.png"
            Image.new("RGB", (240, 320), (21, 35, 57)).save(first)
            Image.new("RGB", (240, 320), (21, 35, 57)).save(second)
            self.assertEqual(visual_parity.compare_images(first, second, 12), (0.0, 0.0))

    def test_white_frame_with_valid_status_bar_is_rejected(self) -> None:
        with TemporaryDirectory() as folder:
            original, bad = Path(folder) / "valid.png", Path(folder) / "blank.png"
            Image.new("RGB", (240, 320), (21, 35, 57)).save(original)
            image = Image.new("RGB", (240, 320), "white")
            ImageDraw.Draw(image).rectangle((0, 0, 239, 23), fill=(0, 0, 0))
            image.save(bad)
            with self.assertRaisesRegex(ValueError, "blank white screenshot"):
                visual_parity.compare_images(original, bad, 12)

    def test_real_differences_still_fail_pixel_comparison(self) -> None:
        with TemporaryDirectory() as folder:
            first, second = Path(folder) / "one.png", Path(folder) / "two.png"
            Image.new("RGB", (240, 320), (21, 35, 57)).save(first)
            Image.new("RGB", (240, 320), (45, 65, 85)).save(second)
            fraction, mean = visual_parity.compare_images(first, second, 12)
            self.assertGreater(fraction, 0.99)
            self.assertGreater(mean, 10)


if __name__ == "__main__":
    unittest.main()
