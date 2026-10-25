#!/usr/bin/env python3
"""Exercise the actual inline macOS simulator recovery Python without Xcode.

A past release CI failure had an undefined `types` and `fresh` only when
CoreSimulator launch timed out. This regression exercises that exact branch.
"""
from __future__ import annotations

import ast
import json
import subprocess
import textwrap
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
WORKFLOW = ROOT / ".github/workflows/ci.yml"


def recovery_statement() -> ast.If:
    workflow = WORKFLOW.read_text(encoding="utf-8")
    job = workflow.split("  ios_calendar:\n", 1)[1].split("\n  ios_visual_parity:", 1)[0]
    step = job.split("      - name: Universal calendar simulator launch and real screenshots\n", 1)[1]
    source = step.split("          python3 - <<'PY'\n", 1)[1].split("\n          PY\n", 1)[0]
    python = textwrap.dedent(source)
    parsed = ast.parse(python)
    matches = [
        node for node in parsed.body
        if isinstance(node, ast.If)
        and isinstance(node.test, ast.UnaryOp)
        and isinstance(node.test.op, ast.Not)
        and isinstance(node.test.operand, ast.Call)
        and isinstance(node.test.operand.func, ast.Name)
        and node.test.operand.func.id == "prepare_and_launch"
    ]
    if len(matches) != 1:
        raise AssertionError("Expected a single checked initial boot/install/launch recovery")
    return matches[0]


class RecoveryRegression(unittest.TestCase):
    def run_recovery(self, fail_creation: bool) -> tuple[dict, list[tuple]]:
        statement = recovery_statement()
        invoked: list[tuple] = []

        def fake_run(*args: str, timeout: int = 30) -> str:
            invoked.append(args)
            if args[:5] == ("xcrun", "simctl", "list", "devicetypes", "-j"):
                return json.dumps({
                    "devicetypes": [
                        {"name": "iPhone 17 Pro", "identifier": "iPhone17Pro"},
                        {"name": "iPhone 17 Pro Max", "identifier": "Max"},
                    ]
                })
            if args[:3] == ("xcrun", "simctl", "create"):
                if fail_creation:
                    raise subprocess.TimeoutExpired(args, timeout)
                self.assertEqual(args[4], "iPhone17Pro")
                return "NEW-UDID\n"
            raise AssertionError(f"Unexpected simulator operation: {args}")

        def fake_prepare_and_launch(udid: str, state: str) -> bool:
            invoked.append(("prepare", udid, state))
            return udid == "NEW-UDID"

        env = {
            "udid": "OLD-UDID",
            "target": {"state": "Shutdown"},
            "devices": [{"udid": "OLD-UDID", "name": "iPhone 17 Pro", "state": "Shutdown"}],
            "runtime": {"identifier": "com.apple.CoreSimulator.SimRuntime.iOS-26-2"},
            "json": json,
            "run": fake_run,
            "subprocess": subprocess,
            "prepare_and_launch": fake_prepare_and_launch,
        }
        block = ast.fix_missing_locations(ast.Module(body=[statement], type_ignores=[]))
        program = compile(block, str(WORKFLOW), "exec")
        if fail_creation:
            with self.assertRaisesRegex(SystemExit, "screenshot QA remains mandatory"):
                exec(program, env)
        else:
            exec(program, env)
        return env, invoked

    def test_launch_timeout_recovers_on_fresh_exact_model(self) -> None:
        env, invoked = self.run_recovery(False)
        self.assertEqual(env["udid"], "NEW-UDID")
        self.assertEqual(env["fresh"], "NEW-UDID")
        self.assertEqual(env["chosen"]["name"], "iPhone 17 Pro")
        self.assertIn(("prepare", "NEW-UDID", "Shutdown"), invoked)

    def test_no_simulator_never_marks_visual_qa_passed(self) -> None:
        env, invoked = self.run_recovery(True)
        self.assertEqual(env["udid"], "OLD-UDID")
        self.assertNotIn(("prepare", "NEW-UDID", "Shutdown"), invoked)


if __name__ == "__main__":
    unittest.main()
