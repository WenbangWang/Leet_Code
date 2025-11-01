# System Design Cheat Sheet (FRs + NFRs)

## 1. Functional Requirements (What the system does)
- **Core Features:** main capabilities (e.g., post, click, search).
- **Users & Roles:** types of users, permissions.
- **Data:** storage, retrieval, processing.
- **APIs / Interfaces:** endpoints or pipelines.
- **Business Logic:** rules, constraints, validation.
- **Error Handling:** retries, duplicates, failure responses.

**Quick questions to clarify:**
- Who uses the system and what can they do?
- What data is critical?
- What are the edge cases / failure scenarios?

---

## 2. Non-Functional Requirements (How the system behaves)
- **Scalability:** handle growth (vertical / horizontal).
- **Performance / Latency:** response times, throughput.
- **Availability:** uptime %, fault tolerance.
- **Reliability / Durability:** prevent data loss, recovery.
- **Consistency:** strong vs eventual consistency.
- **Security:** auth, encryption, privacy.
- **Maintainability / Extensibility:** easy to update / extend.
- **Cost Efficiency:** compute, storage, network budgets.

**Quick questions to clarify:**
- Expected users / traffic?
- Latency and throughput requirements?
- How critical is uptime?
- Any regulatory or compliance constraints?

---

## Tips for Interviews
1. **Clarify FRs first** → show understanding of core features.
2. **Discuss NFRs next** → demonstrate awareness of real-world trade-offs.
3. **Use numbers/examples** → e.g., 10k QPS, <100ms latency, 99.99% uptime.
4. **Think trade-offs** → consistency vs availability, cost vs performance.
