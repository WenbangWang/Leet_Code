package com.wwb.leetcode.other.openai.gpucredit.solution;

/**
 * Represents the lifecycle state of a credit token
 * Used in Phase 3 to support reservation semantics
 *
 * STATE TRANSITIONS:
 *
 *     AVAILABLE
 *        |
 *        | reserve()
 *        ↓
 *     RESERVED ──────→ AVAILABLE (release())
 *        |
 *        | commit()
 *        ↓
 *     CONSUMED
 *
 * BUSINESS LOGIC:
 * - AVAILABLE: Credits can be reserved or directly consumed
 * - RESERVED: Credits are locked for a specific job, cannot be used by others
 * - CONSUMED: Credits have been used and are no longer available
 *
 * INTERVIEW DISCUSSION POINTS:
 * 1. "Why use state instead of just reserved/used counters?"
 *    → State makes transitions explicit and easier to validate
 *    → Prevents invalid operations (e.g., consuming reserved credits)
 *    → Makes auditing and debugging easier
 *
 * 2. "What about partial states (token is 50% reserved)?"
 *    → We handle this by tracking reserved/used amounts separately
 *    → State represents the predominant state of the token
 *
 * 3. "Could we add more states (e.g., EXPIRED)?"
 *    → Yes! Could add EXPIRED for better tracking
 *    → Could add PENDING for credits not yet active
 *    → Trade-off: more states = more complexity
 */
public enum TokenState {
    /**
     * Credits are available for reservation or immediate consumption
     * This is the initial state when credits are added
     */
    AVAILABLE,

    /**
     * Credits are reserved for a specific job but not yet consumed
     * They cannot be used by other operations
     * Can transition to CONSUMED (commit) or back to AVAILABLE (release)
     */
    RESERVED,

    /**
     * Credits have been consumed and are no longer available
     * This is a terminal state
     */
    CONSUMED;

    /**
     * Check if transition from this state to target state is valid
     *
     * Valid transitions:
     * - AVAILABLE → RESERVED
     * - AVAILABLE → CONSUMED (direct use without reservation)
     * - RESERVED → CONSUMED (commit reservation)
     * - RESERVED → AVAILABLE (release/cancel reservation)
     *
     * Invalid transitions:
     * - CONSUMED → anything (terminal state)
     * - Any other combination
     */
    public boolean canTransitionTo(TokenState target) {
      return switch (this) {
        case AVAILABLE -> target == RESERVED || target == CONSUMED;
        case RESERVED -> target == CONSUMED || target == AVAILABLE;
        case CONSUMED -> false; // Terminal state
        default -> false;
      };
    }

    /**
     * Get human-readable description of the state
     */
    public String getDescription() {
      return switch (this) {
        case AVAILABLE -> "Available for use or reservation";
        case RESERVED -> "Reserved for a specific job";
        case CONSUMED -> "Fully consumed";
        default -> "Unknown state";
      };
    }
}

