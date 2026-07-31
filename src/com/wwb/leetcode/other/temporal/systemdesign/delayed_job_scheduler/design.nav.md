# design — Navigation Index

## Sections at a glance

<!-- nav:sections -->
| Section | Level | Line |
|---------|-------|------|
| # Distributed Delayed Job / Scheduled Task Execution System | H1 | 1 |
| ## 1) Clarification Questions (FR → NFR) | H2 | 11 |
| ## 2) Functional Requirements (FR) | H2 | 52 |
| ## 3) Non-Functional Requirements (NFR) | H2 | 76 |
| ## 4) Back-of-the-Envelope (BOTE) Calculations | H2 | 103 |
| ## 5) Core Data Entities | H2 | 140 |
| ## 6) System Interfaces | H2 | 183 |
| ## 7) Simple Design (Single Server, Naive Polling) | H2 | 214 |
| ## 8) Enriched Design | H2 | 237 |
| ## 9) Deep Dives (Problem → Solutions → Preferred → Trade-offs) | H2 | 313 |
| ### 9.1 Timer Storage & Retrieval at Scale | H3 | 315 |
| ### 9.2 Precision vs. Scalability Trade-off | H3 | 371 |
| ### 9.3 Persistence & Durability on Scheduler Node Crash | H3 | 419 |
| ### 9.4 Distributed Ownership / Sharding of Timers | H3 | 462 |
| ### 9.5 Exactly-Once vs. At-Least-Once Firing Semantics | H3 | 498 |
| ### 9.6 Cancellation / Rescheduling Without Rescanning | H3 | 555 |
| ### 9.7 Operational Runbook Hooks | H3 | 591 |
<!-- /nav:sections -->

## Where is X defined?

| Concept | Section | Notes |
|---------|---------|-------|

## Edit-coupling map

| When you change… | Also update… |
|-----------------|-------------|
