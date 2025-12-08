package com.wwb.leetcode.other.openai.gpucredit.solution;

/**
 * Enhanced credit token with reservation support
 * Used in Phase 3
 * 
 * NEW FIELDS (vs Phase 2):
 * - reserved: Amount currently locked by reservations
 * - state: AVAILABLE, RESERVED, or CONSUMED
 * 
 * NOTE: No tier field - tier is managed via nested maps in Phase3GPUCredit
 * 
 * IMPORTANT INVARIANT:
 * used + reserved + available(timestamp) <= amount
 * 
 * Example:
 * - amount = 100
 * - used = 30
 * - reserved = 40
 * - available = 30 (can still reserve or use these)
 */
public class Phase3CreditToken implements Comparable<Phase3CreditToken> {
    final String grantId;
    final int amount;
    final int startTime;
    final int expiration;
    
    int used;               // Consumed and gone
    int reserved;           // NEW: Locked by reservations
    TokenState state;       // NEW: Lifecycle state
    
    public Phase3CreditToken(String grantId, int amount, int startTime, int expiration) {
        this.grantId = grantId;
        this.amount = amount;
        this.startTime = startTime;
        this.expiration = expiration;
        this.used = 0;
        this.reserved = 0;
        this.state = TokenState.AVAILABLE;
    }
    
    /**
     * Get balance available for immediate use (not reserved, not used, not expired)
     * 
     * INTERVIEW NOTE:
     * This is different from getAvailableBalance in Phase 1/2.
     * Now we must exclude reserved credits.
     */
    public int getAvailableBalance(int timestamp) {
        if (timestamp < startTime || isExpired(timestamp)) {
            return 0;
        }
        return Math.max(0, amount - used - reserved);
    }
    
    /**
     * Get total balance (used + reserved + available)
     * Useful for displaying total credits before expiration
     */
    public int getTotalBalance(int timestamp) {
        if (timestamp < startTime || isExpired(timestamp)) {
            return 0;
        }
        return Math.max(0, amount - used);
    }
    
    /**
     * Get how much is currently reserved (locked but not consumed)
     */
    public int getReservedBalance() {
        return reserved;
    }
    
    public boolean isExpired(int timestamp) {
        return timestamp >= startTime + expiration;
    }
    
    public int getExpirationTime() {
        return startTime + expiration;
    }
    
    public TokenState getState() {
        return state;
    }
    
    /**
     * Reserve credits from this token
     * @return true if successful
     */
    public boolean reserve(int amount, int timestamp) {
        if (getAvailableBalance(timestamp) < amount) {
            return false;
        }
        reserved += amount;
        
        // Update state if token becomes fully reserved
        if (getAvailableBalance(timestamp) == 0) {
            state = TokenState.RESERVED;
        }
        return true;
    }
    
    /**
     * Release reserved credits back to available pool
     */
    public void release(int amount) {
        reserved = Math.max(0, reserved - amount);
        
        // Update state back to available if we freed up credits
        if (reserved == 0 && used < this.amount) {
            state = TokenState.AVAILABLE;
        }
    }
    
    /**
     * Commit reserved credits to used (consume them)
     */
    public void commit(int amount) {
        int toCommit = Math.min(amount, reserved);
        reserved -= toCommit;
        used += toCommit;
        
        // Update state if fully consumed
        if (used >= this.amount) {
            state = TokenState.CONSUMED;
        } else if (reserved == 0) {
            state = TokenState.AVAILABLE;
        }
    }
    
    /**
     * Compare tokens for PriorityQueue ordering
     * 
     * CRITICAL: Sort by absolute expiration time (FIFO)
     * Use getExpirationTime() which returns startTime + expiration
     * 
     * Time Complexity: O(1)
     */
    @Override
    public int compareTo(Phase3CreditToken other) {
        return Integer.compare(this.getExpirationTime(), other.getExpirationTime());
    }
    
    @Override
    public String toString() {
        return String.format(
            "Token{grant='%s', amount=%d, used=%d, reserved=%d, state=%s, expires=%d}",
            grantId, amount, used, reserved, state, getExpirationTime()
        );
    }
}
