# design — Navigation Index

## Sections at a glance

<!-- nav:sections -->
| Section | Level | Line |
|---------|-------|------|
| # Zero-Downtime Database Migration | H1 | 1 |
| ## 1) Clarification Questions (FR → NFR) | H2 | 5 |
| ## 2) Functional Requirements (FR) | H2 | 34 |
| ## 2.5) Migration Granularity Decision | H2 | 46 |
| ## 3) Non-Functional Requirements (NFR) | H2 | 70 |
| ## 4) Back-of-the-Envelope (BOTE) Calculations | H2 | 82 |
| ## 5) Core Data Entities | H2 | 101 |
| ## 6) System Interfaces | H2 | 125 |
| ## 7) Simple Design (Single Table, Single Writer) | H2 | 150 |
| ## 8) Enriched Design | H2 | 168 |
| ## 9) Deep Dives (Problem → Solutions → Preferred → Trade-offs) | H2 | 244 |
| ### 9.1 Backfill Durability & Idempotency | H3 | 246 |
| ### 9.2 Dual-Write Consistency | H3 | 263 |
| ### 9.3 Verification Before Trust | H3 | 298 |
| ### 9.4 Cutover Ordering (Reads Before Writes) | H3 | 315 |
| ### 9.5 Rollback Window & the Point of No Return | H3 | 331 |
| ### 9.6 Schema-Change Lock Safety | H3 | 372 |
| ### 9.7 Reconciling Existence Mismatches (Present in One Store, Absent in the Other) | H3 | 395 |
| ### 9.8 Schema-Incompatible / Fan-Out Migrations (Table-Layer Path Only) | H3 | 510 |
<!-- /nav:sections -->

## Where is X defined?

| Concept | Section | Notes |
|---------|---------|-------|

## Edit-coupling map

| When you change… | Also update… |
|-----------------|-------------|
