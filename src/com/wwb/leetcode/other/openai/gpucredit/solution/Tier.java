package com.wwb.leetcode.other.openai.gpucredit.solution;

/**
 * Tier enum for credit priority
 * 
 * Used in Phase 3 to support priority-based credit consumption.
 * Lower priority value means higher priority (consumed first).
 * 
 * USAGE:
 * - Premium users get Tier.PREMIUM credits
 * - Standard users get Tier.STANDARD credits
 * - When consuming, PREMIUM credits are used before STANDARD
 * 
 * INTERVIEW DISCUSSION:
 * Q: "Why enum instead of String?"
 * A: 
 *   - Type safety: Compile-time checking
 *   - No typos: Can't use "Premium" vs "premium"
 *   - IDE autocomplete
 *   - Can add methods (e.g., isHigherThan())
 * 
 * Q: "How does priority work with PriorityQueue?"
 * A: Token's compareTo() first compares tier.priority, then expiration.
 *    This ensures PREMIUM tokens are polled before STANDARD.
 * 
 * EXTENSION:
 * Easy to add more tiers: ENTERPRISE(0), PREMIUM(1), STANDARD(2)
 */
public enum Tier {
    PREMIUM(0),    // Highest priority - consumed first
    STANDARD(1);   // Lower priority - consumed after premium
    
    private final int priority;
    
    Tier(int priority) {
        this.priority = priority;
    }
    
    public int getPriority() {
        return priority;
    }
    
    /**
     * Check if this tier has higher priority than another
     */
    public boolean isHigherThan(Tier other) {
        return this.priority < other.priority;
    }
    
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}

