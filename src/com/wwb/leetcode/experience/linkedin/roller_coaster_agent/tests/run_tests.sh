#!/usr/bin/env bash
# Test harness for RollerCoasterAgent.
# Runs each scenario in tests/ against its expected output in tests/expected/.
#
# Usage (from project root):
#   bash src/com/wwb/leetcode/experience/linkedin/roller_coaster_agent/tests/run_tests.sh
#
# To regenerate expected outputs after intentional changes:
#   bash run_tests.sh --regen

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../../../../.." && pwd)"
SRC_DIR="$PROJECT_ROOT/src"
PACKAGE="com.wwb.leetcode.experience.linkedin.roller_coaster_agent"
MAIN_CLASS="$PACKAGE.RollerCoasterAgent"
TESTS_DIR="$SCRIPT_DIR"
EXPECTED_DIR="$SCRIPT_DIR/expected"

REGEN=false
if [[ "${1:-}" == "--regen" ]]; then
  REGEN=true
fi

# ── Compile ────────────────────────────────────────────────────────────────────
echo "Compiling..."
javac -d "$SRC_DIR" "$SRC_DIR/${PACKAGE//.//}"/*.java
echo "Compiled OK"
echo ""

# ── Run tests ─────────────────────────────────────────────────────────────────
PASS=0
FAIL=0
ERRORS=()

for input in "$TESTS_DIR"/[0-9]*.txt; do
  name=$(basename "$input" .txt)
  expected="$EXPECTED_DIR/${name}.txt"
  actual=$(java -cp "$SRC_DIR" "$MAIN_CLASS" "$input" 2>&1)

  if [[ "$REGEN" == true ]]; then
    echo "$actual" > "$expected"
    echo "  REGEN  $name"
    continue
  fi

  if [[ ! -f "$expected" ]]; then
    echo "  MISS   $name  (no expected file — run with --regen to create)"
    ERRORS+=("$name: missing expected file")
    FAIL=$(( FAIL + 1 ))
    continue
  fi

  if diff <(echo "$actual") <(cat "$expected") > /dev/null 2>&1; then
    echo "  PASS   $name"
    PASS=$(( PASS + 1 ))
  else
    echo "  FAIL   $name"
    echo "    diff (actual vs expected):"
    diff <(echo "$actual") <(cat "$expected") | sed 's/^/      /' || true
    ERRORS+=("$name")
    FAIL=$(( FAIL + 1 ))
  fi
done

# ── Summary ───────────────────────────────────────────────────────────────────
echo ""
echo "Results: $PASS passed, $FAIL failed"

if [[ $FAIL -gt 0 ]]; then
  exit 1
fi
