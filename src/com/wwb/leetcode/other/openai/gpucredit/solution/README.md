# GPU Credit System - OpenAI Interview Question

**Progressive 4-phase implementation for OpenAI coding interviews**

[![Tests](https://img.shields.io/badge/tests-26%20passing-brightgreen)]() [![Java](https://img.shields.io/badge/java-11%2B-blue)]()

---

## 🚀 Quick Start

```bash
cd /Users/wenbwang/IdeaProjects/Leet_Code/src/com/wwb/leetcode/other/openai/gpucredit/solution
./run_tests.sh
```

**Expected:** All 26 tests pass ✅

---

## 📦 Package Structure

```
gpucredit/
├── GPUCredit.java              # (original implementation)
├── CreditToken.java            # (original token)
└── solution/                   # 👈 All new code here
    ├── Phase1GPUCredit.java    # Basic credit pool (15 min)
    ├── Phase2GPUCredit.java    # Multi-tenant (12 min)
    ├── Phase3GPUCredit.java    # Reservations & tiers (18 min)
    ├── Phase3CreditToken.java  # Enhanced token
    ├── Phase4GPUCredit.java    # Rate limiting (bonus)
    ├── TokenState.java         # State machine enum
    ├── Reservation.java        # Reservation tracking
    ├── package-info.java       # Package documentation
    ├── run_tests.sh            # Test runner
    └── README.md               # This file
```

---

## 🎯 The 4 Phases

### **Phase 1: Basic Credit Pool** (15 minutes)
**Problem:** Credits expire after a certain time. Consume oldest-expiring first.

**API:**
```java
void addCredit(int amount, int timestamp, int expiration)
boolean useCredit(int amount, int timestamp)
int getBalance(int timestamp)
```

**Key Questions to Ask:**
- "Can credits have a start time different from when they're added?"
- "Should useCredit return false or throw exception if insufficient?"
- "What happens if we query balance before credits are active?"

**Complexity:** O(log n) add, O(k log n) use, O(n) balance

---

### **Phase 2: Multi-Tenant** (12 minutes)
**Extension:** Support multiple users with separate credit pools. Track grants (purchase receipts).

**New API:**
```java
void addCredit(userId, grantId, amount, timestamp, expiration)
boolean useCredit(userId, amount, timestamp)
int getBalance(userId, timestamp)
int getGrantBalance(userId, grantId, timestamp)
```

**Key Questions:**
- "Are grant IDs unique globally or per-user?"
- "Should credits from different grants pool together for consumption?"

**Design:** `Map<userId, PriorityQueue<Token>>` for user isolation

---

### **Phase 3: Reservations with Priority Tiers** (18 minutes)
**Extension:** Reserve credits for long-running jobs + priority tier support.

**New API:**
```java
enum Tier { PREMIUM, STANDARD }

void addCredit(userId, grantId, amount, timestamp, expiration, tier)
String reserveCredit(userId, amount, timestamp, tier) → reservationId
boolean commitReservation(userId, reservationId, timestamp)
void releaseReservation(userId, reservationId, timestamp)
int getAvailableBalance(userId, timestamp)  // Excludes reserved
```

**Key Concepts:**
- **Two-phase commit:** reserve → commit/release
- **State machine:** AVAILABLE → RESERVED → CONSUMED
- **Separate tracking:** `used` vs `reserved` fields
- **Tier enum:** Type-safe priority (PREMIUM before STANDARD)
- **PriorityQueue:** Composite sort (tier first, then expiration)

**Key Questions:**
- "Can a user have multiple active reservations?"
- "What if credits expire during reservation period?"
- "Does PREMIUM mean premium-only or premium-first?" → Premium-first
- "Why enum instead of String?" → Type safety, no typos

---

### **Phase 4: Rate Limiting** (10-15 minutes, Bonus)
**Extension:** Limit credits consumed per time window (e.g., 100/minute).

**New API:**
```java
void setRateLimit(userId, creditsPerMinute)
RateLimitStatus getRateLimitStatus(userId, timestamp)
// useCredit() now checks rate limits
```

**Algorithm:** Sliding window (60-second window)

**Key Questions:**
- "Fixed window or sliding window?"
- "Should reservations count toward rate limit?"

**Discussion:** Sliding window vs token bucket trade-offs

---

## 💡 Key Design Decisions

### 1. **Why nested maps for tiers in Phase 3?**
- Phase 1-2: `Map<userId, PriorityQueue<Token>>`
- Phase 3: `Map<userId, Map<tier, PriorityQueue<Token>>>`
- Each tier has its own PriorityQueue (PREMIUM and STANDARD separated)
- Token compareTo is simple (expiration only, no tier)
- Consumption logic explicitly checks PREMIUM queue first, then STANDARD
- More scalable for 3+ tiers (easy to add ENTERPRISE, BASIC, etc.)

### 2. **Why separate `used` and `reserved` fields?**
- Reserved credits are tentative (might be released)
- Enables accurate `getAvailableBalance()` calculation
- Supports two-phase commit pattern

### 3. **Why Tier enum as map key instead of embedded in token?**
- **Simpler token:** No tier field needed
- **Explicit separation:** PREMIUM and STANDARD in different queues
- **Simpler compareTo:** Just expiration, no composite sorting
- **Type safety:** Enum prevents invalid tiers
- **Scalable:** Easy to add more tiers (ENTERPRISE, BASIC, etc.)
- **Flexible consumption:** Easy to change tier priority strategy

### 4. **Why TokenState enum?**
- Makes state transitions explicit and validatable
- Prevents invalid operations (e.g., using consumed credits)
- Easier debugging and auditing
- Could extend with more states (PENDING, EXPIRED)

### 5. **Why Sliding Window for rate limiting?**
- More accurate than fixed window (no burst at boundaries)
- Trade-off: O(w) space vs O(1) for token bucket
- Better for interview discussion

---

## 📊 Complexity Summary

| Operation | Phase 1-2 | Phase 3 | Phase 4 |
|-----------|-----------|---------|---------|
| addCredit | O(log n) | O(log n) | O(log n) |
| useCredit | O(k log n) | O(k log n) | O(k log n + w) |
| reserveCredit | - | O(k log n) | Same |
| commitReservation | - | O(k) | O(k + w) |
| getBalance | O(n) | O(n) | O(n) |

**Where:** n = tokens, k = tokens touched, w = events in rate limit window

---

## 🎤 Interview Strategy

### Time Allocation (50 minutes):
- **Phase 1** (15 min): Core algorithm, basic tests
- **Phase 2** (12 min): Extend structure, test isolation
- **Phase 3** (18 min): Design discussion, implement reservations
- **Phase 4** (5-10 min): Algorithm comparison, discuss trade-offs

### Critical Questions to Ask:

**Phase 1:**
- "Should I return null or throw exception for errors?"
- "Can credits be active before they're added?"

**Phase 2:**
- "How should I handle non-existent users?"
- "Should I track which grants were consumed from?"

**Phase 3:**
- "What happens if credits expire while reserved?"
- "Should reservations have a timeout?"
- "Can reservations fail if insufficient credits at commit time?"

**Phase 4:**
- "What time window for rate limiting?"
- "Should I use sliding window or token bucket?"

---

## 🔥 Interview Tips

### Do's:
- ✅ Ask clarifying questions BEFORE coding
- ✅ Start with simplest solution (Phase 1)
- ✅ Think out loud
- ✅ Test with examples as you go
- ✅ State time/space complexity
- ✅ Discuss trade-offs when extending
- ✅ Mention production concerns (caching, distributed systems)

### Don'ts:
- ❌ Jump straight to Phase 3 complexity
- ❌ Forget edge cases (null, negative, expired)
- ❌ Ignore state transitions in Phase 3
- ❌ Miss the reservation rollback on insufficient credits

---

## 🎓 What This Tests

### Technical Skills:
- **Data Structures:** PriorityQueue, HashMap, List, Enum
- **Algorithms:** FIFO by expiration, sliding window (with PriorityQueue for out-of-order events)
- **State Management:** State machines, lifecycle
- **System Design:** Multi-tenancy, quotas, rate limiting

### Interview Skills:
- **Progressive thinking:** Simple → complex
- **Clarifying questions:** What to ask at each phase
- **Complexity analysis:** Big-O for operations
- **Trade-off discussion:** Algorithm selection rationale

---

## 📝 Practice Drills

### Drill 1: Speed Implementation (15 min)
```
Set timer for 15 minutes
Implement Phase 1 from scratch (don't look)
Compare with solution
Note what you missed
```

### Drill 2: Whiteboard Design (20 min)
```
Design Phase 3 on whiteboard (no coding)
Draw state transition diagram:
  AVAILABLE → RESERVED → CONSUMED (commit)
           ↓          ↘
      CONSUMED      AVAILABLE (release)
Explain two-phase commit pattern
Discuss edge cases
```

### Drill 3: Mock Interview (45 min)
```
Have someone ask you the question
Work through all phases progressively
Practice thinking out loud
Get feedback on communication
```

---

## 🔗 Related Concepts

This question connects to:
- **Billing Systems:** AWS credits, Azure credits
- **Resource Quotas:** Kubernetes resource limits
- **Rate Limiting:** API throttling, circuit breakers
- **State Machines:** Order processing, workflows
- **Transactions:** Two-phase commit, SAGA pattern

---

## 🏃 Running Tests

### All phases:
```bash
./run_tests.sh
```

### Individual phase:
```bash
cd /Users/wenbwang/IdeaProjects/Leet_Code

# Compile all
/usr/bin/javac src/com/wwb/leetcode/other/openai/gpucredit/solution/*.java

# Run individually
/usr/bin/java -cp src com.wwb.leetcode.other.openai.gpucredit.solution.Phase1GPUCredit
/usr/bin/java -cp src com.wwb.leetcode.other.openai.gpucredit.solution.Phase2GPUCredit
/usr/bin/java -cp src com.wwb.leetcode.other.openai.gpucredit.solution.Phase3GPUCredit
/usr/bin/java -cp src com.wwb.leetcode.other.openai.gpucredit.solution.Phase4GPUCredit
```

---

## ✅ Pre-Interview Checklist

### Study (2-3 hours):
- [ ] Read this README fully
- [ ] Study Phase1GPUCredit.java code + comments
- [ ] Study Phase2GPUCredit.java extensions
- [ ] Study Phase3GPUCredit.java + supporting classes
- [ ] Understand TokenState transitions
- [ ] Review Phase4GPUCredit.java rate limiting

### Practice (2 hours):
- [ ] Run `./run_tests.sh` and verify all pass
- [ ] Implement Phase 1 from scratch (15 min timed)
- [ ] Whiteboard Phase 3 design (no coding)
- [ ] Practice explaining two-phase commit
- [ ] Review clarifying questions for each phase

### Interview Day:
- [ ] Remember: start simple (Phase 1)
- [ ] Ask clarifying questions first
- [ ] Think out loud while coding
- [ ] Test with examples
- [ ] State complexity for each operation
- [ ] Discuss trade-offs when extending

---

## 🎯 Success Criteria

**Junior (Phase 1-2):**
- Implement basic credit pool
- Add multi-tenant support
- Handle basic edge cases
- Explain time complexity

**Mid (Phase 1-3):**
- Implement reservation system
- Explain two-phase commit
- Design state machine
- Implement tier priority with composite compareTo
- Discuss `used` vs `reserved` fields

**Senior (All Phases):**
- Implement rate limiting
- Compare algorithms (sliding window vs token bucket)
- Explain tier enum benefits
- Discuss distributed systems (Redis)
- Mention production concerns (caching, monitoring)

---

## 📖 Code Organization

### Phase 1-2 Implementation:
```java
// Single PriorityQueue sorted by expiration
Map<String, PriorityQueue<CreditToken>> creditsByUser;

// compareTo: simple
return Integer.compare(this.getExpirationTime(), other.getExpirationTime());
```

### Phase 3 Nested Maps Design:
```java
// Nested maps: tier separation
Map<String, Map<Tier, PriorityQueue<Phase3CreditToken>>> creditsByUser;
//      └─user   └─tier  └─tokens sorted by expiration

// Consumption strategy
if (requestedTier == PREMIUM) {
    // Use PREMIUM queue first, then STANDARD
    remaining = consumeFrom(queues.get(PREMIUM), remaining);
    if (remaining > 0) {
        remaining = consumeFrom(queues.get(STANDARD), remaining);
    }
} else {
    // STANDARD tier: use STANDARD queue only
    remaining = consumeFrom(queues.get(STANDARD), remaining);
}

// Token compareTo: simple (no tier needed)
return Integer.compare(this.getExpirationTime(), other.getExpirationTime());
```

### Phase 3 State Machine:
```java
enum TokenState {
    AVAILABLE,  // Can be reserved or used
    RESERVED,   // Locked for a job
    CONSUMED    // Used and gone
}

// Separate tracking
int used;      // Confirmed consumption
int reserved;  // Tentative (might be released)
```

### Phase 4 Sliding Window:
```java
Deque<UsageEvent> usageHistory;

// Cleanup old events
while (oldest.timestamp < timestamp - 60) {
    usageHistory.poll();
}

// Check if usage + amount <= limit
int currentUsage = sum(usageHistory);
return currentUsage + amount <= creditsPerMinute;
```

---

## ✨ What Makes This Excellent

1. **Real-world relevance:** OpenAI has actual GPU credit systems
2. **Natural progression:** Each phase builds cleanly on previous
3. **Multiple skills:** Data structures + algorithms + system design
4. **Rich discussion:** State machines, transactions, rate limiting
5. **Clear stopping points:** Can finish early and still succeed

---

## 🚀 You're Ready!

**Files:** 7 Java files in `solution/` package  
**Tests:** 26 passing test cases  
**Documentation:** This comprehensive README  
**Status:** ✅ Production-quality, interview-optimized  

**Next step:** Run `./run_tests.sh` then study Phase 1! 

Good luck with your OpenAI interview! 🍀
