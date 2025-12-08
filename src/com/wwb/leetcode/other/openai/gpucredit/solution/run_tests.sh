#!/bin/bash

# GPU Credit System - Test Runner
# Run from solution/ directory

set -e

GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../../../../.." && pwd)"
SRC_DIR="$PROJECT_ROOT/src"
SOLUTION_DIR="$SRC_DIR/com/wwb/leetcode/other/openai/gpucredit/solution"

echo -e "${BLUE}════════════════════════════════════════${NC}"
echo -e "${BLUE}  GPU Credit System - Test Runner${NC}"
echo -e "${BLUE}════════════════════════════════════════${NC}"
echo ""

# Find Java
JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home 2>/dev/null)}"
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA="$JAVA_HOME/bin/java"
    JAVAC="$JAVA_HOME/bin/javac"
elif [ -x "/usr/bin/java" ]; then
    JAVA="/usr/bin/java"
    JAVAC="/usr/bin/javac"
elif command -v java &> /dev/null; then
    JAVA="java"
    JAVAC="javac"
else
    echo "Error: Java not found. Please install Java or set JAVA_HOME."
    exit 1
fi

echo -e "${GREEN}Using: $JAVA${NC}"
echo -e "${BLUE}Location: solution/ package${NC}"
echo ""

cd "$PROJECT_ROOT"

# Clean up old class files (both in src/ and generated in project root)
echo -e "${BLUE}Cleaning up old .class files...${NC}"
find "$SOLUTION_DIR" -name "*.class" -delete 2>/dev/null || true
find "$PROJECT_ROOT/com/wwb/leetcode/other/openai/gpucredit" -name "*.class" -delete 2>/dev/null || true
echo -e "${GREEN}✓ Cleanup complete${NC}"
echo ""

# Compile all files
echo -e "${BLUE}Compiling...${NC}"
$JAVAC "$SOLUTION_DIR/Tier.java" \
       "$SOLUTION_DIR/TokenState.java" \
       "$SOLUTION_DIR/Phase1GPUCredit.java" \
       "$SOLUTION_DIR/Phase2GPUCredit.java" \
       "$SOLUTION_DIR/Reservation.java" \
       "$SOLUTION_DIR/Phase3CreditToken.java" \
       "$SOLUTION_DIR/Phase3GPUCredit.java" \
       "$SOLUTION_DIR/Phase4GPUCredit.java"
echo -e "${GREEN}✓ Compilation successful${NC}"
echo ""

# Run tests
echo -e "${BLUE}Phase 1: Basic Credit Pool${NC}"
$JAVA -cp src com.wwb.leetcode.other.openai.gpucredit.solution.Phase1GPUCredit
echo ""

echo -e "${BLUE}Phase 2: Multi-Tenant${NC}"
$JAVA -cp src com.wwb.leetcode.other.openai.gpucredit.solution.Phase2GPUCredit
echo ""

echo -e "${BLUE}Phase 3: Reservations with Tiers${NC}"
$JAVA -cp src com.wwb.leetcode.other.openai.gpucredit.solution.Phase3GPUCredit
echo ""

echo -e "${BLUE}Phase 4: Rate Limiting${NC}"
$JAVA -cp src com.wwb.leetcode.other.openai.gpucredit.solution.Phase4GPUCredit
echo ""

echo -e "${GREEN}════════════════════════════════════════${NC}"
echo -e "${GREEN}  ALL TESTS PASSED! 🎉${NC}"
echo -e "${GREEN}════════════════════════════════════════${NC}"
echo ""

# Clean up class files after tests (both locations)
echo -e "${BLUE}Cleaning up .class files...${NC}"
find "$SOLUTION_DIR" -name "*.class" -delete 2>/dev/null || true
find "$PROJECT_ROOT/com/wwb/leetcode/other/openai/gpucredit" -name "*.class" -delete 2>/dev/null || true
echo -e "${GREEN}✓ Cleanup complete${NC}"
echo ""

echo "Read: README.md for complete interview guide"
echo ""
