https://www.hellointerview.com/learn/system-design/problem-breakdowns/leetcode

# System Design: LeetCode

## 1. Clarification Questions (Ordered by FR → NFR)

### Functional Requirements (FR)

1. What types of problems are supported? (Coding, database, shell, etc.)
2. Do we need multiple programming languages support? Which ones?
3. Should users submit code and get **real-time feedback**? Or **batch compilation** is acceptable?
4. Should we support **leaderboards and contests**?
5. Should users be able to **view solutions**, **discuss problems**, or **track progress**?

### Non-Functional Requirements (NFR)

1. Expected number of users (concurrent / monthly active)?
2. Expected number of problem submissions per second?
3. SLA for submission feedback (e.g., <2s)?
4. Fault tolerance for code execution sandbox?
5. Data retention: track all submissions, history, discussions?

---

## 2. Functional Requirements (FR)

* Users can **register/login** and manage profiles.
* Users can **browse problems** by difficulty, topic, and tags.
* Users can **submit solutions** in multiple programming languages.
* Users get **automatic evaluation** of submissions (pass/fail, runtime, memory).
* Users can **see statistics**: attempts, success rates, badges.
* Users can participate in **contests** (time-limited competitions).
* **Discussion and solution sharing** for community engagement.

---

## 3. Non-Functional Requirements (NFR)

* **High availability:** 99.9% uptime.
* **Low latency:** Submission evaluation <2s (for simple problems).
* **Scalable:** Millions of users, high submission throughput.
* **Secure:** Code execution sandboxed to avoid malicious submissions.
* **Durable:** Track submission history, contest results, and analytics.
* **Extensible:** Easily add new languages or problem types.

---

## 4. Back-of-Envelope Calculation

* Users: 50M total, 1M daily active users.
* Submission rate: \~100k submissions/sec at peak during contests.
* Storage estimate:

    * Average code submission: 10KB
    * 100k submissions/sec → 1GB/sec → 86TB/day → use compression and retention policy.
* Problem metadata: \~50k problems → negligible storage (\~10MB).
* Real-time leaderboard requires fast updates → consider **Redis** or **in-memory DB**.

---

## 5. Core Data Entities

| Entity     | Attributes                                                                    | Notes                                 |
| ---------- | ----------------------------------------------------------------------------- | ------------------------------------- |
| User       | id, username, email, password\_hash, rating, solved\_problems                 | Track progress, contest participation |
| Problem    | id, title, description, difficulty, tags, test\_cases, constraints            | Include multiple languages support    |
| Submission | id, user\_id, problem\_id, code, language, status, runtime, memory, timestamp | Link to evaluation service            |
| Contest    | id, title, start\_time, end\_time, problems\[], participants\[]               | Leaderboard, rankings                 |
| Discussion | id, problem\_id, user\_id, content, timestamp                                 | Optional solution discussion          |

---

## 6. System Interfaces

**APIs**:

```
POST /api/register
POST /api/login
GET  /api/problems?difficulty=easy&tags=array
POST /api/problems/{id}/submit
GET  /api/problems/{id}/submissions?user_id=123
GET  /api/contests
POST /api/contests/{id}/participate
GET  /api/leaderboard?contest_id=456
POST /api/problems/{id}/discussion
```

**Internal services**:

* **Submission Service:** Handles code submission, queues for evaluation.
* **Evaluation Service:** Executes code in sandbox, returns status, runtime, memory.
* **Leaderboard Service:** Updates contest rankings in real-time.
* **Recommendation Service:** Suggests problems based on skill and history.

---

## 7. Simple Design (ASCII Diagram)

```
          +--------------------+
          |     Client App     |
          +--------------------+
                    |
                    v
          +--------------------+
          |   API Gateway      |
          +--------------------+
                    |
     +--------------+--------------+
     |                             |
+----------+                 +----------+
| User DB  |                 | Problem  |
+----------+                 |  DB      |
                             +----------+
                    |
           +------------------+
           | Submission Queue |
           +------------------+
                    |
           +------------------+
           | Evaluation Service|
           +------------------+
                    |
           +------------------+
           | Results DB / Cache|
           +------------------+
```

---

## 8. Enriched Design (ASCII Diagram)

```
          +--------------------+
          |     Client App     |
          +--------------------+
                    |
                    v
          +--------------------+
          |   API Gateway      |
          +--------------------+
        /      |        |        \
       v       v        v         v
+--------+ +--------+ +--------+ +--------+
| UserDB | |Problem | |Contest | |Cache/  |
|        | |DB      | |DB      | |Redis   |
+--------+ +--------+ +--------+ +--------+
                    |
            +-------------------+
            | Submission Queue  |
            +-------------------+
                    |
            +-------------------+
            | Evaluation Cluster|
            +-------------------+
          /       |       |        \
         v        v       v         v
   Sandboxed   Sandboxed  Sandboxed  Sandboxed
    Worker      Worker     Worker     Worker
          \       |       |        /
           +-------------------+
           | Result DB / Cache |
           +-------------------+
                    |
           +-------------------+
           | Leaderboard &     |
           | Recommendation    |
           +-------------------+
```

**Enhancements**:

* **Evaluation cluster**: horizontally scalable workers for different languages.
* **Cache / Redis**: fast retrieval for leaderboard, recent submissions.
* **Leaderboard & Recommendation**: decoupled services for flexibility.
* **Sandboxed execution**: isolates user code, prevents security risks.

---

## 9. Possible Deep Dives (Expanded)

### 9.1 Code Execution Sandbox

* **Problem:** Users can submit arbitrary code → security risk.
* **Solution:**

    * Run each submission in a container (Docker / Firecracker).
    * Limit CPU, memory, network access.
    * Timeouts to prevent infinite loops.

### 9.2 High Throughput Submission Handling

* **Problem:** Peaks in submissions during contests.
* **Solution:**

    * Use **message queue** (Kafka / RabbitMQ) to decouple submission from evaluation.
    * Worker pool dynamically scales based on queue depth.
    * Use **sharding** by language or problem ID for parallel processing.

### 9.3 Leaderboard Updates

* **Problem:** Real-time ranking for contests.
* **Solution:**

    * Use **Redis sorted sets** to maintain rankings efficiently.
    * Periodically persist to SQL for durability.

### 9.4 Multi-Language Support

* **Problem:** Problems may support multiple languages.
* **Solution:**

    * Maintain per-language compilation and runtime environments.
    * Normalize evaluation outputs (status, runtime, memory).

### 9.5 Caching & Performance

* **Problem:** Frequent reads for problems, leaderboards.
* **Solution:**

    * Cache problems, top submissions, leaderboard snapshots in **Redis**.
    * Cache invalidation strategies: TTL, pub/sub for updates.

### 9.6 Scalability & Fault Tolerance

* **Problem:** Millions of users, geographically distributed.
* **Solution:**

    * Use **CDN** for problem assets.
    * Deploy multiple **evaluation clusters per region**.
    * Database replication and sharding.
    * Failover for evaluation workers.
