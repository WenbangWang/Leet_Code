# design — Navigation Index

## Sections at a glance

<!-- nav:sections -->
| Section | Level | Line |
|---------|-------|------|
| # Twitter-like Feed / Timeline System | H1 | 1 |
| ## 1) Clarification Questions (FR → NFR) | H2 | 9 |
| ## 2) Functional Requirements (FR) | H2 | 41 |
| ## 3) Non-Functional Requirements (NFR) | H2 | 60 |
| ## 4) Back-of-the-Envelope (BOTE) Calculations | H2 | 91 |
| ## 5) Core Data Entities | H2 | 122 |
| ## 6) System Interfaces | H2 | 167 |
| ## 7) Simple Design (Single Server, Pull-Only) | H2 | 197 |
| ## 8) Enriched Design | H2 | 220 |
| ## 9) Deep Dives (Problem → Solutions → Preferred → Trade-offs) | H2 | 285 |
| ### 9.1 Fan-out-on-Write vs. Fan-out-on-Read vs. Hybrid (the Celebrity Problem) | H3 | 287 |
| ### 9.2 Timeline Storage & Cursor-Based Pagination | H3 | 322 |
| ### 9.3 Ranking / Relevance | H3 | 351 |
| ### 9.4 Cache Invalidation / Staleness on Delete or Edit | H3 | 380 |
| ### 9.5 Read/Write Amplification Trade-offs at Scale | H3 | 421 |
| ### 9.6 Failure Modes | H3 | 454 |
| ### 9.7 Authorization & Visibility (Private Accounts) | H3 | 504 |
<!-- /nav:sections -->

## Where is X defined?

| Concept | Section | Notes |
|---------|---------|-------|

## Edit-coupling map

| When you change… | Also update… |
|-----------------|-------------|
