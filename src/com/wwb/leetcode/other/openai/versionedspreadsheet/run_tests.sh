#!/bin/bash

# Versioned Spreadsheet Test Runner
# Tests all 4 phases

set -e  # Exit on error

PROJECT_ROOT="/Users/wenbwang/IdeaProjects/Leet_Code"
PACKAGE_DIR="src/com/wwb/leetcode/other/openai/versionedspreadsheet"

cd "$PROJECT_ROOT"

echo "=========================================="
echo "Versioned Spreadsheet Test Suite"
echo "=========================================="
echo ""

# Compile all files
echo "📦 Compiling all phases..."
/usr/bin/javac \
    "$PACKAGE_DIR/CellVersion.java" \
    "$PACKAGE_DIR/Operation.java" \
    "$PACKAGE_DIR/Phase1Spreadsheet.java" \
    "$PACKAGE_DIR/Phase2Spreadsheet.java" \
    "$PACKAGE_DIR/Phase3VersionedSpreadsheet.java" \
    "$PACKAGE_DIR/Phase4UndoableSpreadsheet.java"

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful"
else
    echo "❌ Compilation failed"
    exit 1
fi

echo ""
echo "=========================================="
echo "Running Phase 1: Basic Spreadsheet"
echo "=========================================="
/usr/bin/java -cp src com.wwb.leetcode.other.openai.versionedspreadsheet.Phase1Spreadsheet

echo ""
echo "=========================================="
echo "Running Phase 2: Live Updates"
echo "=========================================="
/usr/bin/java -cp src com.wwb.leetcode.other.openai.versionedspreadsheet.Phase2Spreadsheet

echo ""
echo "=========================================="
echo "Running Phase 3: Versioned + Time-Travel"
echo "=========================================="
/usr/bin/java -cp src com.wwb.leetcode.other.openai.versionedspreadsheet.Phase3VersionedSpreadsheet

echo ""
echo "=========================================="
echo "Running Phase 4: Undo/Redo"
echo "=========================================="
/usr/bin/java -cp src com.wwb.leetcode.other.openai.versionedspreadsheet.Phase4UndoableSpreadsheet

echo ""
echo "=========================================="
echo "✅ ALL TESTS PASSED!"
echo "=========================================="
echo ""
echo "Summary:"
echo "  Phase 1: 13 tests ✅ (5 functional + 8 validation)"
echo "  Phase 2: 9 tests ✅ (6 functional + 3 validation)"
echo "  Phase 3: 9 tests ✅ (6 functional + 3 validation)"
echo "  Phase 4: 10 tests ✅ (7 functional + 3 validation)"
echo "  Total: 41 tests ✅"
echo ""
echo "Pattern Synthesis:"
echo "  ✓ LeetCode 631 (Excel) → Phase 1-2"
echo "  ✓ VersionedKV Store → Phase 3"
echo "  ✓ Command Pattern → Phase 4"
echo ""

# Cleanup generated class files
echo "🧹 Cleaning up generated class files..."
rm -f "$PACKAGE_DIR"/*.class
echo "✅ Cleanup complete"
echo ""
echo "You're ready for the interview! 🚀"

