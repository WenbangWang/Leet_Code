# Versioned KV Store - Interview Guide

## 🎯 For Candidates

### Quick Start

This question tests your ability to:
1. Design efficient data structures (TreeMap)
2. Handle persistence and serialization
3. Reason about concurrency and consistency

### Time Management (60 minutes)

```
Phase 1 (15-20 min): ███████████████░░░░░ Basic Implementation
Phase 2 (15 min):    ███████████░░░░░░░░░ Serialization Design
Phase 3 (12-15 min): ██████████░░░░░░░░░░ Concurrency Discussion
Bonus (5-10 min):    ████░░░░░░░░░░░░░░░░ Extensions
```

### Phase-by-Phase Strategy

#### Phase 1: Basic Versioned KV Store

**🎯 Goal**: Working time-travel queries in 15 minutes

**Key Steps**:
1. Ask clarifying questions (2 min)
   - Out-of-order timestamps?
   - Same timestamp overwrites?
   - Input validation needed?
2. Choose data structure (1 min)
   - `Map<String, TreeMap<Long, String>>`
3. Implement put/get (8 min)
4. Test edge cases (4 min)

**🚨 Common Mistakes**:
- Using `List<Pair<Long, String>>` → O(n) queries
- Not handling out-of-order timestamps
- Forgetting `get(key)` without timestamp

**💡 Pro Tips**:
- Mention `TreeMap.floorEntry()` immediately
- Compare to Git commits or database MVCC
- Test: empty key, no history, exact timestamp

---

#### Phase 2: Persistence & Serialization

**🎯 Goal**: Design file format and metadata in 15 minutes

**Key Steps**:
1. Discuss trade-offs (3 min)
   - JSON vs binary
   - Eager vs lazy loading
   - Single vs multiple files
2. Design file format (5 min)
3. Design metadata structure (4 min)
4. Discuss query efficiency (3 min)

**🚨 Common Mistakes**:
- Using Java serialization (not portable, security risk)
- Ignoring 4KB file limit
- No plan for querying after deserialization

**💡 Pro Tips**:
- Start simple (length-prefixed format)
- Mention LSM-trees, SSTable format
- Discuss bloom filters for negative lookups
- Compare to RocksDB, LevelDB

**Example Format**:
```
[4 bytes: keyLength]
[keyLength bytes: key]
[8 bytes: timestamp]
[4 bytes: valueLength]
[valueLength bytes: value]
```

---

#### Phase 3: Multi-Threaded Consistency

**🎯 Goal**: Discuss concurrency strategies in 12 minutes

**Key Steps**:
1. Identify thread safety issues (3 min)
   - TreeMap is NOT thread-safe
   - Need atomicity per key
2. Propose solution (5 min)
   - Fine-grained locking
   - Copy-on-write
   - ConcurrentSkipListMap
3. Discuss trade-offs (4 min)
   - Lock granularity
   - Read/write ratio
   - Performance vs simplicity

**🚨 Common Mistakes**:
- `synchronized` on entire method (poor concurrency)
- Not locking TreeMap modifications
- Holding multiple key locks (deadlock risk)

**💡 Pro Tips**:
- Ask about read/write ratio
- Mention `ReentrantReadWriteLock`
- Discuss `ConcurrentSkipListMap` as TreeMap alternative
- Compare to Cassandra's MVCC approach

**Solution Template**:
```java
private final ConcurrentHashMap<String, Lock> keyLocks;
private final ConcurrentHashMap<String, TreeMap<Long, String>> store;

public void put(String key, String value, long ts) {
    Lock lock = keyLocks.computeIfAbsent(key, k -> new ReentrantLock());
    lock.lock();
    try {
        store.computeIfAbsent(key, k -> new TreeMap<>()).put(ts, value);
    } finally {
        lock.unlock();
    }
}
```

---

### Bonus Topics (If Time Permits)

#### Garbage Collection
**Question**: "How would you clean up old versions?"

**Key Points**:
- TTL-based expiration
- Keep last N versions
- Compaction strategies
- Trade-off: storage vs history

#### Range Queries
**Question**: "Support `getRange(startKey, endKey, timestamp)`"

**Key Points**:
- Need TreeMap for keys too (nested structure)
- Pagination for large results
- Consistent point-in-time view

#### Distributed Versioning
**Question**: "Scale across multiple machines"

**Key Points**:
- Consistent hashing for partitioning
- Version vectors for ordering
- Consensus (Raft/Paxos) for coordination

---

## 🎓 For Interviewers

### Evaluation Rubric

#### Strong Hire (4/4)
- ✅ Chooses TreeMap immediately (no prompting)
- ✅ Asks clarifying questions proactively
- ✅ Completes Phase 1 in 15 minutes with tests
- ✅ Designs elegant serialization format
- ✅ Discusses concurrency trade-offs fluently
- ✅ Connects to real systems (Git, etcd, DynamoDB)

#### Hire (3/4)
- ✅ Arrives at TreeMap with hints
- ✅ Completes Phase 1 in 20 minutes
- ✅ Reasonable serialization design
- ✅ Understands basic concurrency issues
- ✅ Tests some edge cases

#### No Hire (1-2/4)
- ❌ Uses List with linear search
- ❌ Can't complete Phase 1 in 25 minutes
- ❌ Suggests Java serialization
- ❌ Doesn't recognize thread safety issues
- ❌ No test cases or edge case handling

### Hints to Provide (If Stuck)

**Phase 1**:
- "What data structure provides O(log n) sorted lookups?"
- "How does Git track version history?"
- "What does 'at or before' mean for queries?"

**Phase 2**:
- "How would you split data across multiple files?"
- "What information would help you locate a key quickly?"
- "How does RocksDB organize its data on disk?"

**Phase 3**:
- "Is TreeMap thread-safe?"
- "What happens if two threads modify the same key?"
- "How would read-heavy vs write-heavy affect your design?"

### Time Extensions

If candidate finishes early (rare):
- Implement actual serialization code
- Add range query support
- Design compaction algorithm
- Discuss consistency models (linearizability vs eventual)

If candidate is slow:
- Skip implementation for Phase 2/3, focus on design
- Ask for pseudocode instead of full code
- Focus on one aspect (e.g., just serialization format)

---

## 📊 Comparison to Similar Questions

| Question | Similar Concepts | Key Differences |
|----------|-----------------|-----------------|
| LeetCode 981 | Time-based KV store | This adds persistence & concurrency |
| LRU Cache | Data structure design | This adds versioning dimension |
| Design URL Shortener | System design scale | This focuses on data structure depth |
| Git Internals | Versioning | This is coding vs architecture |

---

## 🔗 Real-World Connections

### Companies Using Similar Systems

**Git**
- Each commit is a versioned snapshot
- Uses content-addressable storage
- Efficient delta compression

**etcd / Consul**
- Distributed KV store with versioning
- MVCC for consistency
- Watch API for change notifications

**DynamoDB**
- Point-in-time recovery
- Item versioning
- Streams for change data capture

**S3**
- Object versioning
- Lifecycle policies for old versions
- Cross-region replication

**Cassandra**
- Timestamp-based MVCC
- Compaction strategies
- Tunable consistency

---

## 📚 Study Resources

### Before the Interview

1. **Data Structures**
   - TreeMap / TreeSet operations
   - NavigableMap API (floorEntry, ceilingEntry)
   - Concurrency collections (ConcurrentHashMap, ConcurrentSkipListMap)

2. **Serialization**
   - Binary encoding (ByteBuffer)
   - Length-prefixed formats
   - Protocol Buffers / Thrift basics

3. **Concurrency**
   - Lock types (ReentrantLock, ReadWriteLock)
   - Lock granularity trade-offs
   - Copy-on-write patterns

4. **System Design Patterns**
   - LSM-trees (RocksDB, LevelDB)
   - MVCC (PostgreSQL, Cassandra)
   - Write-ahead logs

### Practice Problems

- LeetCode 981: Time Based Key-Value Store
- LeetCode 146: LRU Cache
- LeetCode 380: Insert Delete GetRandom O(1)
- Design a File System with Versioning
- Implement Git-like Version Control

---

## 💬 Sample Interview Dialogue

**Interviewer**: "Let's design a versioned key-value store..."

**Candidate**: "Before I start, can I clarify a few things?"
- ✅ Good! Shows thoughtfulness

**Candidate**: "Are timestamps monotonically increasing?"
- ✅ Great question! Tests understanding of distributed systems

**Candidate**: "I'll use a HashMap of TreeMaps..."
- ✅ Perfect! Right data structure immediately

**Candidate**: "For persistence, we could use JSON but that's inefficient..."
- ✅ Excellent! Proactive trade-off discussion

**Candidate**: "TreeMap isn't thread-safe, so we need locks per key..."
- ✅ Outstanding! Shows concurrency awareness

---

## 🎬 Conclusion

This question effectively tests:
- ✅ Data structure knowledge (TreeMap)
- ✅ API design (clean, composable)
- ✅ Systems thinking (persistence, consistency)
- ✅ Trade-off reasoning (memory vs disk, locks vs lock-free)
- ✅ Real-world knowledge (Git, databases)

**Time to master**: 2-3 hours of practice
**Difficulty**: Medium to Hard
**Hit rate**: High at OpenAI, Stripe, Google, Amazon

