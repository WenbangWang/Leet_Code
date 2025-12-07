package com.wwb.leetcode.other.openai.gpucredit.solution;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * PHASE 1: Basic GPU Credit Pool with Expiration (15 minutes)
 * 
 * PROBLEM STATEMENT:
 * Implement a GPU credit system where users purchase credits that expire after a certain time.
 * When consuming credits, use the ones expiring soonest first (FIFO by expiration).
 * 
 * CLARIFYING QUESTIONS TO ASK:
 * 1. "Can credits have a start time different from when they're added?" 
 *    → Yes, credits become active at startTime
 * 2. "Should useCredit return false or throw exception if insufficient?" 
 *    → Return false (graceful degradation)
 * 3. "What happens if we query balance at a timestamp before credits are active?" 
 *    → Return 0 (credits not yet active)
 * 4. "Can amount/timestamp be negative?" 
 *    → No, validate inputs
 * 5. "Should we clean up expired credits eagerly or lazily?" 
 *    → Lazy deletion during operations (better performance)
 * 
 * KEY INSIGHTS:
 * - Use PriorityQueue to maintain FIFO order by expiration time
 * - Each credit token tracks: amount, startTime, expirationTime, used
 * - Credits can be partially consumed
 * 
 * TIME COMPLEXITY:
 * - addCredit: O(log n) where n = number of credit tokens
 * - useCredit: O(k log n) where k = number of tokens touched
 * - getBalance: O(n) to iterate and filter valid credits
 * 
 * SPACE COMPLEXITY: O(n) where n = number of credit tokens
 * 
 * EXTENSIONS FOR LATER PHASES:
 * - Phase 2: Add userId/grantId for multi-tenancy
 * - Phase 3: Add reservation state to tokens
 * - Phase 4: Add rate limiting per user
 */
public class Phase1GPUCredit {
    
    // PriorityQueue sorted by expiration time (earliest first)
    private PriorityQueue<Phase1CreditToken> credits;
    
    public Phase1GPUCredit() {
        // Sort by expiration time to implement FIFO expiration policy
        this.credits = new PriorityQueue<>(
            Comparator.comparingInt(c -> c.startTime + c.expiration)
        );
    }
    
    /**
     * Add credits to the pool
     * @param amount Number of credits to add (must be positive)
     * @param timestamp When credits are added
     * @param expiration Duration until credits expire (from startTime)
     * 
     * Time: O(log n)
     */
    public void addCredit(int amount, int timestamp, int expiration) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive: " + amount);
        }
        if (expiration <= 0) {
            throw new IllegalArgumentException("Expiration must be positive: " + expiration);
        }
        
        Phase1CreditToken token = new Phase1CreditToken(amount, timestamp, expiration);
        credits.offer(token);
    }
    
    /**
     * Consume credits, using the ones expiring soonest first
     * @param amount Number of credits to use
     * @param timestamp When credits are being used
     * @return true if successful, false if insufficient credits
     * 
     * Time: O(k log n) where k = number of tokens touched
     * 
     * IMPLEMENTATION NOTE:
     * We need to poll tokens, try to use them, and add them back.
     * This is because PriorityQueue doesn't support in-place updates.
     */
    public boolean useCredit(int amount, int timestamp) {
        if (amount <= 0) {
            return false;
        }
        
        // First check if we have enough credits
        int available = getBalance(timestamp);
        if (available < amount) {
            return false;
        }
        
        int remaining = amount;
        int size = credits.size();
        
        // Poll credits and consume them
        for (int i = 0; i < size && remaining > 0; i++) {
            Phase1CreditToken token = credits.poll();
            
            int availableInToken = token.getAvailableBalance(timestamp);
            if (availableInToken > 0) {
                int toUse = Math.min(availableInToken, remaining);
                token.used += toUse;
                remaining -= toUse;
            }
            
            // Add back if not fully consumed or expired
            if (token.getAvailableBalance(timestamp) > 0 || !token.isExpired(timestamp)) {
                credits.offer(token);
            }
            // Else: token is fully consumed, let it be garbage collected
        }
        
        return remaining == 0;
    }
    
    /**
     * Get total available credits at a given timestamp
     * @param timestamp Query time
     * @return Total available credits (excluding expired and used)
     * 
     * Time: O(n) where n = number of tokens
     * 
     * OPTIMIZATION OPPORTUNITY:
     * If getBalance is called frequently, consider caching the sum
     * and invalidating on addCredit/useCredit. Trade-off: O(1) query vs O(n) updates.
     */
    public int getBalance(int timestamp) {
        int total = 0;
        for (Phase1CreditToken token : credits) {
            total += token.getAvailableBalance(timestamp);
        }
        return total;
    }
    
    /**
     * Inner class representing a single credit token
     * 
     * DESIGN NOTE:
     * We use a separate class to encapsulate credit logic.
     * This makes it easier to extend in later phases (add grantId, state, etc.)
     */
    static class Phase1CreditToken {
        final int amount;        // Original amount
        final int startTime;     // When credit becomes active
        final int expiration;    // Duration until expiration (from startTime)
        int used;                // Amount consumed so far
        
        Phase1CreditToken(int amount, int startTime, int expiration) {
            this.amount = amount;
            this.startTime = startTime;
            this.expiration = expiration;
            this.used = 0;
        }
        
        /**
         * Get available balance at a specific timestamp
         * Returns 0 if:
         * - Credit not yet active (timestamp < startTime)
         * - Credit expired (timestamp >= startTime + expiration)
         * - Credit fully consumed (used >= amount)
         */
        int getAvailableBalance(int timestamp) {
            if (timestamp < startTime || isExpired(timestamp)) {
                return 0;
            }
            return Math.max(0, amount - used);
        }
        
        boolean isExpired(int timestamp) {
            return timestamp >= startTime + expiration;
        }
    }
    
    // ============ TEST CASES ============
    
    public static void main(String[] args) {
        System.out.println("=== Phase 1: Basic GPU Credit Tests ===\n");
        
        // Test 1: Basic add and balance
        Phase1GPUCredit gpu = new Phase1GPUCredit();
        gpu.addCredit(100, 0, 50);
        assert gpu.getBalance(10) == 100 : "Test 1 failed";
        System.out.println("✓ Test 1: Basic add and balance");
        
        // Test 2: Use credits
        gpu.useCredit(30, 20);
        assert gpu.getBalance(25) == 70 : "Test 2 failed";
        System.out.println("✓ Test 2: Use credits");
        
        // Test 3: Credits expire
        assert gpu.getBalance(50) == 0 : "Test 3 failed";
        System.out.println("✓ Test 3: Credits expire");
        
        // Test 4: Insufficient credits
        gpu = new Phase1GPUCredit();
        gpu.addCredit(50, 0, 100);
        assert !gpu.useCredit(100, 10) : "Test 4 failed";
        assert gpu.getBalance(10) == 50 : "Test 4 failed";
        System.out.println("✓ Test 4: Insufficient credits");
        
        // Test 5: Multiple grants, FIFO by expiration
        gpu = new Phase1GPUCredit();
        gpu.addCredit(50, 10, 40);   // expires at t=50
        gpu.addCredit(80, 20, 100);  // expires at t=120
        assert gpu.getBalance(30) == 130 : "Test 5a failed";
        gpu.useCredit(70, 35);  // should use all of first grant, 20 from second
        assert gpu.getBalance(40) == 60 : "Test 5b failed";
        System.out.println("✓ Test 5: FIFO expiration ordering");
        
        // Test 6: Credits not yet active
        gpu = new Phase1GPUCredit();
        gpu.addCredit(100, 50, 100);
        assert gpu.getBalance(30) == 0 : "Test 6 failed";
        assert gpu.getBalance(50) == 100 : "Test 6 failed";
        System.out.println("✓ Test 6: Credits not yet active");
        
        System.out.println("\n✅ All Phase 1 tests passed!");
    }
}

