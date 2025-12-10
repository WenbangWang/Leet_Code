#!/bin/bash

# Versioned KV Store - Test Runner
# Compiles, runs all phase tests, and cleans up generated class files

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Get the script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../../../.." && pwd)"
KVSTORE_DIR="$SCRIPT_DIR"

echo -e "${BLUE}════════════════════════════════════════${NC}"
echo -e "${BLUE}  Versioned KV Store - Test Suite${NC}"
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
    echo -e "${RED}Error: Java not found. Please install Java or set JAVA_HOME.${NC}"
    echo "Try: export JAVA_HOME=\$(/usr/libexec/java_home)"
    exit 1
fi

echo -e "${GREEN}Using: $JAVA${NC}"
$JAVA -version 2>&1 | head -1
echo ""

# Change to project root for running tests with -cp src
cd "$PROJECT_ROOT"

# Step 1: Clean up any existing class files (both locations)
echo -e "${YELLOW}[1/3] Cleaning up old class files...${NC}"
find "$KVSTORE_DIR" -name "*.class" -type f -delete 2>/dev/null || true
find "$PROJECT_ROOT/com/wwb/leetcode/other/openai/versionedkvstore" -name "*.class" -type f -delete 2>/dev/null || true
echo -e "${GREEN}✓ Cleanup complete${NC}"
echo ""

# Step 2: Compile all Java files
echo -e "${YELLOW}[2/3] Compiling Java files...${NC}"

# Compile supporting classes first (order matters!)
# Using -d to place .class files in proper package structure under src/
$JAVAC "$KVSTORE_DIR/FileSystem.java"
$JAVAC -cp "$PROJECT_ROOT/src" "$KVSTORE_DIR/InMemoryFileSystem.java"
$JAVAC -cp "$PROJECT_ROOT/src" "$KVSTORE_DIR/VersionEntry.java"
$JAVAC -cp "$PROJECT_ROOT/src" "$KVSTORE_DIR/FileMeta.java"

# Compile Phase 1
$JAVAC -cp "$PROJECT_ROOT/src" "$KVSTORE_DIR/Phase1VersionedKVStore.java"

# Compile Phase 2 (multi-threading - lock-based)
$JAVAC -cp "$PROJECT_ROOT/src" "$KVSTORE_DIR/Phase2VersionedKVStore.java"

# Compile Phase 2 Alternative (multi-threading - lock-free)
$JAVAC -cp "$PROJECT_ROOT/src" "$KVSTORE_DIR/Phase2VersionedKVStore_LockFree.java"

# Compile Phase 3 Simple (basic serialization without multi-file)
$JAVAC -cp "$PROJECT_ROOT/src" "$KVSTORE_DIR/Phase3_SimpleVersion.java"

# Compile Phase 3 (persistence with thread-safety - needs all dependencies)
$JAVAC -cp "$PROJECT_ROOT/src" "$KVSTORE_DIR/Phase3VersionedKVStore.java"

echo -e "${GREEN}✓ Compilation successful${NC}"
echo ""

# Step 3: Run tests for all phases
echo -e "${YELLOW}[3/3] Running tests...${NC}"
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Phase 1: Basic Versioned KV Store${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
$JAVA -cp src com.wwb.leetcode.other.openai.versionedkvstore.Phase1VersionedKVStore
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Phase 2: Multi-Threaded (Lock-Based)${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
$JAVA -cp src com.wwb.leetcode.other.openai.versionedkvstore.Phase2VersionedKVStore
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Phase 2 Alternative: Multi-Threaded (Lock-Free)${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
$JAVA -cp src com.wwb.leetcode.other.openai.versionedkvstore.Phase2VersionedKVStore_LockFree
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Phase 3A: Simple Serialization (Single File)${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
$JAVA -cp src com.wwb.leetcode.other.openai.versionedkvstore.Phase3_SimpleVersion
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Phase 3B: Thread-Safe Persistence (Multi-File)${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
$JAVA -cp src com.wwb.leetcode.other.openai.versionedkvstore.Phase3VersionedKVStore
echo ""

# Clean up generated class files (both locations)
echo -e "${YELLOW}Cleaning up generated class files...${NC}"
find "$KVSTORE_DIR" -name "*.class" -type f -delete 2>/dev/null || true
find "$PROJECT_ROOT/com/wwb/leetcode/other/openai/versionedkvstore" -name "*.class" -type f -delete 2>/dev/null || true
echo -e "${GREEN}✓ Cleanup complete${NC}"
echo ""

# Summary
echo -e "${GREEN}════════════════════════════════════════${NC}"
echo -e "${GREEN}  🎉 ALL TESTS COMPLETE!${NC}"
echo -e "${GREEN}════════════════════════════════════════${NC}"
echo ""
echo "Implementation Summary:"
echo ""
echo "Phase 1: Basic Versioned KV Store (15-20 min)"
echo "  ✓ TreeMap-based versioning (O(log n) queries)"
echo "  ✓ floorEntry() for time-travel semantics"
echo "  ✓ Out-of-order timestamp handling"
echo "  ✓ Independent version history per key"
echo ""
echo "Phase 2: Multi-Threaded Consistency (12-15 min)"
echo "  Version A (Lock-Based):"
echo "    ✓ ConcurrentHashMap + per-key ReadWriteLock"
echo "    ✓ Fine-grained concurrency (scales with cores)"
echo "    ✓ Multiple readers per key (non-blocking)"
echo "  Version B (Lock-Free):"
echo "    ✓ ConcurrentSkipListMap (no explicit locks)"
echo "    ✓ Simpler code, better high-contention throughput"
echo "    ✓ ~2x memory overhead vs TreeMap"
echo ""
echo "Phase 3: Thread-Safe Persistence (15 min)"
echo "  Phase 3A (Simple):"
echo "    ✓ ByteArrayOutputStream (no custom classes)"
echo "    ✓ Entry count header (no while(true) or EOFException)"
echo "    ✓ Bounded for-loop on deserialize"
echo "    ✓ Binary encoding (length-prefixed format)"
echo "  Phase 3B (Multi-File):"
echo "    ✓ Built on lock-free Phase 2 (ConcurrentSkipListMap)"
echo "    ✓ Entry count header (first 4 bytes of file0)"
echo "    ✓ Thread-safe serialization (global lock only)"
echo "    ✓ Multi-file splitting (4KB max per file)"
echo "    ✓ Offset tracking and metadata (random access)"
echo ""
echo "Read: README.md for overview"
echo "Read: INTERVIEW_GUIDE.md for interview strategy"
echo "Read: PHASE2_COMPARISON.md for lock-based vs lock-free"
echo "Read: PHASE3_GUIDE.md for serialization deep-dive"
echo "Read: PHASE3_OPTIONAL_FEATURES.md for Phase 3A vs 3B breakdown"
echo ""
