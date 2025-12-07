package com.wwb.leetcode.other.openai.gpucredit.solution;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * PHASE 2: Multi-Tenant GPU Credit System with Grant IDs (12 minutes)
 *
 * PROBLEM STATEMENT:
 * Extend Phase 1 to support multiple users/organizations. Each has separate credit pools.
 * Credits are organized by "grant IDs" (e.g., purchase receipts).
 *
 * CLARIFYING QUESTIONS TO ASK:
 * 1. "Are grant IDs unique globally or per-user?"
 *    → Per-user (same grantId can exist for different users)
 * 2. "Should credits from different grants pool together for consumption?"
 *    → Yes, user sees unified balance, but can query per-grant
 * 3. "What if a user doesn't exist?"
 *    → Return 0 for balance, false for use
 * 4. "Should I track which grants were consumed from?"
 *    → Not required, but nice to have for auditing
 * 5. "Can grants have different properties (priority, etc.)?"
 *    → Not in this phase, but Phase 3 will add tiers
 *
 * KEY CHANGES FROM PHASE 1:
 * - Add userId parameter to all methods
 * - Add grantId to track purchase source
 * - Use Map<userId, PriorityQueue<Token>> for isolation
 * - Add getGrantBalance() for per-grant queries
 *
 * TIME COMPLEXITY:
 * - addCredit: O(log n) where n = tokens per user
 * - useCredit: O(k log n) where k = tokens touched per user
 * - getBalance: O(n) where n = tokens per user
 * - getGrantBalance: O(n) to filter by grantId
 *
 * SPACE COMPLEXITY: O(U * T) where U = users, T = avg tokens per user
 *
 * EXTENSIONS FOR LATER PHASES:
 * - Phase 3: Add TokenState enum, reservation logic
 * - Phase 4: Add per-user rate limiting
 */
public class Phase2GPUCredit {

    // userId → PriorityQueue of credits sorted by expiration
    private Map<String, PriorityQueue<Phase2CreditToken>> creditsByUser;

    public Phase2GPUCredit() {
        this.creditsByUser = new HashMap<>();
    }

    /**
     * Add credits for a specific user and grant
     * @param userId User identifier
     * @param grantId Grant identifier (e.g., purchase receipt ID)
     * @param amount Number of credits
     * @param timestamp When credits are added
     * @param expiration Duration until expiration
     *
     * Time: O(log n) where n = tokens for this user
     */
    public void addCredit(String userId, String grantId, int amount, int timestamp, int expiration) {
        validateUserId(userId);
        validateGrantId(grantId);
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive: " + amount);
        }
        if (expiration <= 0) {
            throw new IllegalArgumentException("Expiration must be positive: " + expiration);
        }

        Phase2CreditToken token = new Phase2CreditToken(grantId, amount, timestamp, expiration);

        creditsByUser
            .computeIfAbsent(userId, k -> new PriorityQueue<>(
                Comparator.comparingInt(c -> c.startTime + c.expiration)
            ))
            .offer(token);
    }

    /**
     * Use credits for a user across all their grants
     * Credits expiring soonest are consumed first, regardless of grant
     *
     * @param userId User identifier
     * @param amount Credits to consume
     * @param timestamp When consumption occurs
     * @return true if successful, false if insufficient
     *
     * Time: O(k log n) where k = tokens touched, n = total tokens for user
     */
    public boolean useCredit(String userId, int amount, int timestamp) {
        validateUserId(userId);
        if (amount <= 0) {
            return false;
        }

        PriorityQueue<Phase2CreditToken> userCredits = creditsByUser.get(userId);
        if (userCredits == null) {
            return false;
        }

        // Check if sufficient credits available
//        int available = getBalance(userId, timestamp);
//        if (available < amount) {
//            return false;
//        }

        int remaining = amount;
        int size = userCredits.size();

        for (int i = 0; i < size && remaining > 0; i++) {
            Phase2CreditToken token = userCredits.poll();

            int availableInToken = token.getAvailableBalance(timestamp);
            if (availableInToken > 0) {
                int toUse = Math.min(availableInToken, remaining);
                token.used += toUse;
                remaining -= toUse;
            }

            // Add back if still has balance or not yet expired
            if (token.getAvailableBalance(timestamp) > 0 || !token.isExpired(timestamp)) {
                userCredits.offer(token);
            }
        }

        return remaining == 0;
    }

    /**
     * Get total available credits for a user across all grants
     *
     * Time: O(n) where n = tokens for this user
     */
    public int getBalance(String userId, int timestamp) {
        validateUserId(userId);

        PriorityQueue<Phase2CreditToken> userCredits = creditsByUser.get(userId);
        if (userCredits == null) {
            return 0;
        }

        int total = 0;
        for (Phase2CreditToken token : userCredits) {
            total += token.getAvailableBalance(timestamp);
        }
        return total;
    }

    /**
     * Get available credits for a specific grant
     * Useful for auditing or displaying purchase-specific balances
     *
     * @param userId User identifier
     * @param grantId Grant identifier
     * @param timestamp Query time
     * @return Available credits from this specific grant
     *
     * Time: O(n) where n = tokens for this user
     */
    public int getGrantBalance(String userId, String grantId, int timestamp) {
        validateUserId(userId);
        validateGrantId(grantId);

        PriorityQueue<Phase2CreditToken> userCredits = creditsByUser.get(userId);
        if (userCredits == null) {
            return 0;
        }

        int total = 0;
        for (Phase2CreditToken token : userCredits) {
            if (token.grantId.equals(grantId)) {
                total += token.getAvailableBalance(timestamp);
            }
        }
        return total;
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }
    }

    private void validateGrantId(String grantId) {
        if (grantId == null || grantId.isEmpty()) {
            throw new IllegalArgumentException("GrantId cannot be null or empty");
        }
    }

    /**
     * Credit token with grant tracking
     *
     * DESIGN NOTE:
     * Added grantId field to track purchase source.
     * This enables per-grant balance queries and auditing.
     * Structure is ready to extend with state/tier in Phase 3.
     */
    static class Phase2CreditToken {
        final String grantId;    // NEW: Track which grant this came from
        final int amount;
        final int startTime;
        final int expiration;
        int used;

        Phase2CreditToken(String grantId, int amount, int startTime, int expiration) {
            this.grantId = grantId;
            this.amount = amount;
            this.startTime = startTime;
            this.expiration = expiration;
            this.used = 0;
        }

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
        System.out.println("=== Phase 2: Multi-Tenant GPU Credit Tests ===\n");

        Phase2GPUCredit gpu = new Phase2GPUCredit();

        // Test 1: Multi-user isolation
        gpu.addCredit("user1", "grant-a", 100, 0, 100);
        gpu.addCredit("user2", "grant-c", 200, 0, 150);
        assert gpu.getBalance("user1", 20) == 100 : "Test 1a failed";
        assert gpu.getBalance("user2", 20) == 200 : "Test 1b failed";
        System.out.println("✓ Test 1: Multi-user isolation");

        // Test 2: Multiple grants per user
        gpu.addCredit("user1", "grant-b", 50, 10, 80);
        assert gpu.getBalance("user1", 20) == 150 : "Test 2a failed";
        assert gpu.getGrantBalance("user1", "grant-a", 20) == 100 : "Test 2b failed";
        assert gpu.getGrantBalance("user1", "grant-b", 20) == 50 : "Test 2c failed";
        System.out.println("✓ Test 2: Multiple grants per user");

        // Test 3: Cross-grant consumption (FIFO by expiration)
        gpu.useCredit("user1", 120, 30);
        assert gpu.getGrantBalance("user1", "grant-b", 35) == 0 : "Test 3a failed (grant-b expires at 90, used first)";
        assert gpu.getGrantBalance("user1", "grant-a", 35) == 30 : "Test 3b failed (70 used from grant-a)";
        System.out.println("✓ Test 3: Cross-grant consumption");

        // Test 4: User doesn't exist
        assert gpu.getBalance("user3", 40) == 0 : "Test 4a failed";
        assert !gpu.useCredit("user3", 10, 40) : "Test 4b failed";
        System.out.println("✓ Test 4: Non-existent user");

        // Test 5: Grant expiration
        gpu = new Phase2GPUCredit();
        gpu.addCredit("user1", "g1", 50, 0, 50);
        gpu.addCredit("user1", "g2", 100, 10, 100);
        assert gpu.getBalance("user1", 40) == 150 : "Test 5a failed";
        assert gpu.getBalance("user1", 50) == 100 : "Test 5b failed (g1 expired)";
        assert gpu.getGrantBalance("user1", "g1", 50) == 0 : "Test 5c failed";
        assert gpu.getGrantBalance("user1", "g2", 50) == 100 : "Test 5d failed";
        System.out.println("✓ Test 5: Grant expiration");

        // Test 6: Insufficient credits for user
        gpu = new Phase2GPUCredit();
        gpu.addCredit("user1", "g1", 50, 0, 100);
        gpu.addCredit("user2", "g2", 100, 0, 100);
        assert !gpu.useCredit("user1", 100, 20) : "Test 6a failed";
        assert gpu.getBalance("user1", 25) == 50 : "Test 6b failed (unchanged)";
        System.out.println("✓ Test 6: Insufficient credits per user");

        System.out.println("\n✅ All Phase 2 tests passed!");
    }
}

