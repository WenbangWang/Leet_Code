# Versioned KV Store - OpenAI Interview Question

## 📋 Overview

A **3-phase progressive coding interview question** (45-60 minutes) exploring versioned key-value storage, concurrency, and persistence.

## 🎯 Interview Structure

```
Phase 1 (15-20 min): Basic Versioned KV Store
Phase 2 (12-15 min): Multi-Threading
Phase 3 (15 min):    Persistence
```

---

## Phase 1: Basic Versioned KV Store

**Goal:** In-memory store that maintains version history and supports time-travel queries.

###API:
```java
void put(String key, String value, long timestamp);
String get(String key, long timestamp);  // Value at or before timestamp
String get(String key);                   // Latest value
```

### Example:
```java
store.put("user:1", "Alice", 100);
store.put("user:1", "Bob", 200);

store.get("user:1", 150);  // → "Alice"
store.get("user:1", 250);  // → "Bob"
store.get("user:1");        // → "Bob"
```

### Key Insights:
- **Data Structure:** `Map<String, NavigableMap<Long, String>>`
  - Outer: `HashMap` for key lookup O(1)
  - Inner: `TreeMap` for sorted timestamps O(log n)
- **Time-Travel:** `TreeMap.floorEntry(timestamp)` finds "at or before"
- **Handles:** Out-of-order inserts, duplicate timestamps, missing keys

### Complexity:
- `put()`: O(log V) where V = versions per key
- `get(key, ts)`: O(log V)
- `get(key)`: O(log V)
- Space: O(K × V) where K = keys

---

## Phase 2: Multi-Threading

**Goal:** Make Phase 1 thread-safe with good concurrency.

### Two Approaches:

#### **2A: Lock-Based (Fine-Grained)**
```java
Map<String, VersionHistory> store = new ConcurrentHashMap<>();

class VersionHistory {
    NavigableMap<Long, String> versions = new TreeMap<>();
    ReadWriteLock lock = new ReentrantReadWriteLock();
}
```
- ✅ Per-key locking (operations on different keys don't block)
- ✅ Multiple readers per key
- ❌ More complex code

#### **2B: Lock-Free (Simple)**
```java
Map<String, NavigableMap<Long, String>> store = new ConcurrentHashMap<>();
// Inner map: ConcurrentSkipListMap (lock-free!)
```
- ✅ Simpler code (no explicit locks)
- ✅ Better high-contention performance
- ❌ ~2x memory vs TreeMap

### Interview Discussion:
- **Which is better?** Lock-free for production (simpler, faster)
- **Why show lock-based?** Demonstrates understanding of locking patterns
- **Trade-off:** Code complexity vs memory

---

## Phase 3: Persistence

**Goal:** Serialize/deserialize with thread safety.

### File Format:
```
[entryCount (4 bytes)][entry1][entry2]...[entryN]

Each entry:
[keyLength (4)][keyBytes][timestamp (8)][valueLength (4)][valueBytes]
```

### Two Versions:

#### **3A: Simple (Single File)**
```java
ByteArrayOutputStream baos = new ByteArrayOutputStream();
DataOutputStream dos = new DataOutputStream(baos);

// Write count header
dos.writeInt(totalEntries);

// Write entries
for (entry : entries) {
    dos.writeInt(key.length);
    dos.write(keyBytes);
    dos.writeLong(timestamp);
    dos.writeInt(value.length);
    dos.write(valueBytes);
}

fileSystem.writeFile("store.dat", baos.toByteArray());
```

**Deserialize:**
```java
DataInputStream dis = ...;
int count = dis.readInt();
for (int i = 0; i < count; i++) {  // Bounded loop!
    // Read entry...
}
```

#### **3B: Multi-File (With 4KB Limit)**
- Splits across multiple files at 4KB boundaries
- Tracks metadata (offset of each entry)
- Enables random access during deserialization
- First 4 bytes of file0 = total entry count

### Thread Safety:
```java
ReadWriteLock serializationLock;

put/get: readLock (allow concurrent operations)
serialize/deserialize: writeLock (blocks everything)
```

### Key Points:
- **Entry count header:** Avoids `while(true)` + `EOFException`
- **Standard pattern:** Similar to Protocol Buffers, Thrift
- **Atomic deserialization:** Build new store, swap atomically

---

## 📚 Documentation

- **INTERVIEW_GUIDE.md**: Timing strategy, evaluation rubric, common mistakes
- **PHASE2_COMPARISON.md**: Lock-based vs lock-free deep dive
- **PHASE3_GUIDE.md**: Serialization approaches and simplification rationale

---

## 🚀 Running Tests

```bash
cd src/com/wwb/leetcode/other/openai/versionedkvstore
bash run_tests.sh
```

Tests verify:
- ✅ Phase 1: Time-travel, out-of-order timestamps, edge cases
- ✅ Phase 2A: Lock-based concurrency, no deadlocks
- ✅ Phase 2B: Lock-free consistency
- ✅ Phase 3A: Simple serialization round-trip
- ✅ Phase 3B: Multi-file splitting, thread-safe I/O

---

## 💡 Interview Tips

### Timing Strategy:
1. **Phase 1 (18 min):** Implement carefully, test edge cases
2. **Phase 2 (12 min):** Start with lock-free (simpler), mention lock-based
3. **Phase 3 (15 min):** Start with simple version, discuss multi-file if time

### Success Signals:
- Asks clarifying questions before coding
- Chooses `TreeMap` immediately (shows data structure knowledge)
- Tests edge cases (null, out-of-order, boundaries)
- Discusses trade-offs proactively
- Connects to real systems (Git, etcd, DynamoDB)

### Common Mistakes:
- Using `List` instead of `TreeMap` (O(n) queries)
- Not handling out-of-order timestamps
- Forgetting input validation
- Ignoring thread safety
- Over-complicating serialization

---

## 🔑 Key Takeaways

**Phase 1:** `TreeMap.floorEntry()` is the key insight  
**Phase 2:** Lock-free (`ConcurrentSkipListMap`) is production-preferred  
**Phase 3:** Entry count header > `while(true)` + `EOFException`

**Real-World Systems:**
- Git: Version control with history
- etcd/Consul: Distributed KV with versioning
- DynamoDB: Point-in-time recovery
- S3: Object versioning

---

## 📁 File Structure

```
Phase1VersionedKVStore.java        - Basic in-memory
Phase2VersionedKVStore.java        - Lock-based multi-threading
Phase2VersionedKVStore_LockFree.java  - Lock-free multi-threading
Phase3_SimpleVersion.java          - Simple serialization
Phase3VersionedKVStore.java        - Multi-file serialization
```

**Supporting:**
- `FileSystem.java` / `InMemoryFileSystem.java` - Storage abstraction
- `FileMeta.java` / `VersionEntry.java` - Data structures
- `run_tests.sh` - Compile and test all phases

Good luck with your OpenAI interview! 🎯
