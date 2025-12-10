#!/bin/bash

# Memory Allocator Test Runner
# Compiles, runs all phase tests, and cleans up generated class files
# Pattern based on GPU Credit test runner

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get the script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../../../.." && pwd)"
ALLOCATOR_DIR="$SCRIPT_DIR"

# Change to project root for running tests with -cp src
cd "$PROJECT_ROOT"

echo -e "${BLUE}════════════════════════════════════════${NC}"
echo -e "${BLUE}  Memory Allocator Test Suite${NC}"
echo -e "${BLUE}════════════════════════════════════════${NC}"
echo ""

# Find Java (same pattern as GPU credit script)
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
    echo -e "${RED}Error: Java not found. Please install Java or set JAVA_HOME.${NC}"
    echo "Try: export JAVA_HOME=\$(/usr/libexec/java_home)"
    exit 1
fi

echo -e "${GREEN}Using: $JAVA${NC}"
$JAVA -version 2>&1 | head -1
echo ""

# Step 1: Clean up any existing class files (both locations)
echo -e "${YELLOW}[1/4] Cleaning up old class files...${NC}"
find "$ALLOCATOR_DIR" -name "*.class" -type f -delete 2>/dev/null || true
find "$PROJECT_ROOT/com/wwb/leetcode/other/openai/memoryallocator" -name "*.class" -type f -delete 2>/dev/null || true
echo -e "${GREEN}✓ Cleanup complete${NC}"
echo ""

# Step 2: Compile all Java files
echo -e "${YELLOW}[2/4] Compiling Java files...${NC}"
cd "$PROJECT_ROOT"

# Compile in the correct order (supporting classes first)
$JAVAC "$ALLOCATOR_DIR/Block.java" \
       "$ALLOCATOR_DIR/AllocationInfo.java" \
       "$ALLOCATOR_DIR/AllocationStrategy.java" \
       "$ALLOCATOR_DIR/FirstFitStrategy.java" \
       "$ALLOCATOR_DIR/BestFitStrategy.java" \
       "$ALLOCATOR_DIR/WorstFitStrategy.java" \
       "$ALLOCATOR_DIR/Phase1MemoryAllocator.java" \
       "$ALLOCATOR_DIR/Phase2MemoryAllocator.java" \
       "$ALLOCATOR_DIR/Phase3MemoryAllocator.java" \
       "$ALLOCATOR_DIR/Phase4MemoryAllocator.java"

echo -e "${GREEN}✓ Compilation successful${NC}"
echo ""

# Step 3: Run tests for all phases
echo -e "${YELLOW}[3/4] Running tests...${NC}"
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Phase 1: Basic Allocator (First-Fit + Best-Fit)${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
$JAVA -cp src com.wwb.leetcode.other.openai.memoryallocator.Phase1MemoryAllocator
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Phase 2: Ownership & Realloc (Strategy Pattern)${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
$JAVA -cp src com.wwb.leetcode.other.openai.memoryallocator.Phase2MemoryAllocator
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Phase 3: Memory Compaction ⭐ (THE KEY!)${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
$JAVA -cp src com.wwb.leetcode.other.openai.memoryallocator.Phase3MemoryAllocator
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Phase 4: Alignment & Auto-Compact${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
$JAVA -cp src com.wwb.leetcode.other.openai.memoryallocator.Phase4MemoryAllocator
echo ""

# Step 4: Clean up generated class files (both locations)
echo -e "${YELLOW}[4/4] Cleaning up generated class files...${NC}"
find "$ALLOCATOR_DIR" -name "*.class" -type f -delete 2>/dev/null || true
echo -e "${GREEN}✓ Cleanup complete${NC}"
echo ""

# Summary
echo -e "${GREEN}════════════════════════════════════════${NC}"
echo -e "${GREEN}  🎉 ALL TESTS COMPLETE!${NC}"
echo -e "${GREEN}════════════════════════════════════════${NC}"
echo ""
echo "Implementation uses LeetCode 2502 pattern throughout:"
echo "  ✓ Iterator pattern for malloc"
echo "  ✓ Direct block modification for free"
echo "  ✓ Object reuse (no unnecessary allocation)"
echo "  ✓ Strategy pattern for pluggable algorithms"
echo ""
echo "Read: START_HERE.md for interview preparation"
echo "Read: STRATEGY_PATTERN.md for design pattern details"
echo ""


