package com.wwb.leetcode.other.openai.gpucredit;

/**
 * Credit Token - Represents a single credit purchase with expiration
 * 
 * Design:
 * - Tracks original amount, start time, expiration, and usage
 * - Implements Comparable for PriorityQueue ordering by expiration
 * - Immutable fields (amount, startTime, expiration) for safety
 * - Mutable 'used' field tracks consumption
 * 
 * Time Complexity:
 * - getBalance(): O(1)
 * - isExpired(): O(1)
 * - compareTo(): O(1)
 * 
 * Space Complexity: O(1) - fixed fields
 */
public class CreditToken implements Comparable<CreditToken> {
    final int amount;
    final int startTime;
    final int expiration;
    int used;

    CreditToken(int amount, int startTime, int expiration) {
        this.amount = amount;
        this.startTime = startTime;
        this.expiration = expiration;
        this.used = 0;
    }

    /**
     * Get available balance at a specific timestamp
     * 
     * Returns 0 if:
     * - Credit not yet active (timestamp < startTime)
     * - Credit expired (timestamp > startTime + expiration)
     * 
     * Time: O(1)
     * Space: O(1)
     * 
     * @param timestamp Query time
     * @return Available credits (amount - used), or 0 if invalid
     */
    int getBalance(int timestamp) {
        if (timestamp < startTime || isExpired(timestamp)) {
            return 0;
        }

        return amount - used;
    }

    /**
     * Check if credit has expired at given timestamp
     * 
     * Time: O(1)
     * 
     * @param timestamp Query time
     * @return true if expired, false otherwise
     */
    boolean isExpired(int timestamp) {
        return timestamp > startTime + expiration;
    }

    /**
     * Compare tokens by expiration time for PriorityQueue ordering
     * 
     * Tokens expiring sooner have higher priority (smaller value).
     * This ensures FIFO consumption by expiration.
     * 
     * Time: O(1)
     * 
     * @param o Other token to compare with
     * @return negative if this expires sooner, positive if later, 0 if same
     */
    @Override
    public int compareTo(CreditToken o) {
        return Integer.compare(this.startTime + this.expiration, o.startTime + o.expiration);
    }
}
