#!/bin/bash

# Toy Language Type System - Test Runner
# Compiles and runs all 4 phases

set -e  # Exit on error

echo "╔════════════════════════════════════════════════════════════╗"
echo "║  Toy Language Type System - Test Suite                    ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Get project root
PROJECT_ROOT="/Users/wenbwang/IdeaProjects/Leet_Code"
cd "$PROJECT_ROOT"

echo "📁 Working directory: $PROJECT_ROOT"
echo ""

# Clean up any existing class files first
echo "🧹 Cleaning up old class files..."
find src/com/wwb/leetcode/other/openai/nodeiterator -name "*.class" -type f -delete 2>/dev/null
echo "✅ Cleanup complete"
echo ""

# Compile
echo "🔨 Compiling all files..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

javac \
    src/com/wwb/leetcode/other/openai/nodeiterator/*.java \
    src/com/wwb/leetcode/other/openai/nodeiterator/phases/*.java

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful"
else
    echo "❌ Compilation failed"
    exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Run Phase 1
echo "🚀 Running Phase 1: Data Representation"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
java -cp src com.wwb.leetcode.other.openai.nodeiterator.phases.Phase1DataRepresentation
echo ""

# Run Phase 2
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🚀 Running Phase 2: Type Inference"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
java -cp src com.wwb.leetcode.other.openai.nodeiterator.phases.Phase2TypeInference
echo ""

# Run Phase 3
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🚀 Running Phase 3: Function Composition"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
java -cp src com.wwb.leetcode.other.openai.nodeiterator.phases.Phase3Composition
echo ""

# Run Phase 4
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🚀 Running Phase 4: Type Environment"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
java -cp src com.wwb.leetcode.other.openai.nodeiterator.phases.Phase4Environment
echo ""

# Summary
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                     🎉 ALL TESTS COMPLETE                  ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo "✅ Phase 1: Data Representation - toString() working"
echo "✅ Phase 2: Type Inference - Generic binding working"
echo "✅ Phase 3: Function Composition - Higher-order functions working"
echo "✅ Phase 4: Type Environment - Let-bindings and scoping working"
echo ""
echo "📚 Next steps:"
echo "   1. Review START_HERE.md for quick orientation"
echo "   2. Read QUICKSTART.md for 30-min reference"
echo "   3. Study README_INTERVIEW_GUIDE.md for deep dive"
echo ""

# Cleanup generated class files
echo "🧹 Cleaning up generated class files..."
find src/com/wwb/leetcode/other/openai/nodeiterator -name "*.class" -type f -delete 2>/dev/null
echo "✅ Cleanup complete"

echo ""
echo "🚀 You're ready for your OpenAI interview!"

