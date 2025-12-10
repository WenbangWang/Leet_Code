# Cursor Configuration

This directory contains supporting documentation for Cursor IDE configuration.

## 📁 File Locations

```
/Leet_Code/
├── .cursorrules              ← Main cursor rules file (966 lines, in root)
└── .cursor/
    └── README.md             ← This documentation file
```

## 📄 Main Configuration File

**Location:** `.cursorrules` (project root)

**Size:** 966 lines (26KB)

**Structure:**
- Lines 1-431: RIPER-5 Enhanced Protocol (6 operational modes)
- Lines 432-966: LeetCode-specific development rules

**Why in root?**
- ✅ Cursor IDE automatically finds it
- ✅ Works immediately after `git clone` (no setup needed!)
- ✅ No symlinks required (portable across all systems)
- ✅ Tracked in git (team gets same rules)

## 📝 What Are These Rules?

### RIPER-5 Enhanced Protocol

A structured workflow system with 6 modes to prevent over-eager AI implementation:

1. **FAST-TRACK** 🚀 - Quick implementation for simple changes
2. **RESEARCH** 🔍 - Understanding and analysis (default mode)
3. **INNOVATE** 💡 - Brainstorming solution approaches
4. **PLAN** 📝 - Detailed specification before coding
5. **EXECUTE** ⚙️ - Implementation following the plan
6. **REVIEW** ✅ - Systematic validation

### LeetCode-Specific Rules

- Problem documentation templates
- Mandatory complexity analysis (Time & Space)
- Clean code standards (no unused code, imports, etc.)
- Algorithm explanation (WHY not WHAT)
- Edge case documentation
- Interview preparation guidelines

## 🔄 Updating Rules

To update the cursor rules:

```bash
# Edit the file
code .cursor/.cursorrules

# Or from project root
code .cursorrules

# Changes apply immediately (symlink ensures consistency)
```

## 📚 External Documentation

Detailed documentation is maintained separately at:
`~/Documents/cursor-rules/Leet_Code/`

**Files:**
- `README.md` - Setup and usage guide
- `RIPER5_QUICK_GUIDE.md` - Quick reference for RIPER-5 modes
- `SETUP_SUMMARY.md` - Complete setup history

**Why external docs?**
- Shared across different projects/machines
- Version-controlled separately
- Detailed guides that don't clutter project repo

## 🎯 Quick Start

When working with Cursor on this project:

1. **AI will always declare its mode:**
   ```
   [MODE: RESEARCH] [MODEL: Claude Sonnet 4.5]
   ```

2. **For simple LeetCode problems:**
   - AI suggests FAST-TRACK mode
   - Quick implementation with documentation

3. **For complex problems:**
   - Full RIPER-5 cycle: RESEARCH → INNOVATE → PLAN → EXECUTE → REVIEW
   - Systematic problem-solving
   - Complete documentation

## 🚀 Benefits

**Prevents:**
- ❌ Rushing into implementation
- ❌ Missing edge cases
- ❌ Incomplete documentation
- ❌ Unused code accumulation

**Ensures:**
- ✅ Systematic thinking (great for interviews!)
- ✅ Complete documentation (easy to review later)
- ✅ Clean, maintainable code
- ✅ Proper complexity analysis

## 📖 Learn More

For detailed RIPER-5 usage examples and best practices:

```bash
# View the quick guide
cat ~/Documents/cursor-rules/Leet_Code/RIPER5_QUICK_GUIDE.md

# Or open in editor
code ~/Documents/cursor-rules/Leet_Code/RIPER5_QUICK_GUIDE.md
```

---

**Last Updated:** Dec 10, 2025  
**Rules Version:** 966 lines (RIPER-5 + LeetCode rules)

