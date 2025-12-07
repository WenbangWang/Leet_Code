package com.wwb.leetcode.other.openai.gpucredit.solution;

import java.util.*;

/**
 * PHASE 3: Reservations & Priority Tiers (18 minutes)
 * 
 * PROBLEM STATEMENT:
 * Support long-running GPU jobs that need to reserve credits upfront.
 * Add priority tiers: "premium" users get premium credits first.
 * 
 * NEW OPERATIONS:
 * - reserveCredit(): Lock credits for a job (don't consume yet)
 * - commitReservation(): Job completed, consume the reserved credits
 * - releaseReservation(): Job failed/canceled, return credits to pool
 * - getAvailableBalance(): Balance excluding reserved credits
 * 
 * CLARIFYING QUESTIONS TO ASK:
 * 1. "Can a user have multiple active reservations?" 
 *    → Yes, multiple jobs can run concurrently
 * 2. "What if credits expire during reservation period?" 
 *    → Allow reservation, but commit might fail if expired
 * 3. "Does 'premium' tier mean premium-only or premium-first?" 
 *    → Premium-first: use premium grants first, then standard
 * 4. "Should reservations have a timeout?" 
 *    → Nice to have for production, not required for interview
 * 5. "Can we over-reserve (reserve more than available)?" 
 *    → No, return null if insufficient
 * 6. "What if commit is called with expired reservation?" 
 *    → Check credits still valid, return false if expired
 * 
 * KEY INSIGHTS:
 * - Reservation is a two-phase commit: reserve → commit/release
 * - Need to track which tokens are affected by each reservation
 * - Priority tiers affect consumption order, not quota
 * - State transitions: AVAILABLE → RESERVED → CONSUMED or back to AVAILABLE
 * 
 * TIME COMPLEXITY:
 * - addCredit: O(log n) where n = tokens per user
 * - reserveCredit: O(k log n) where k = tokens touched
 * - commitReservation: O(k) where k = tokens in reservation
 * - releaseReservation: O(k) where k = tokens in reservation
 * - getAvailableBalance: O(n) to sum available credits
 * 
 * SPACE COMPLEXITY: O(U * (T + R)) where U = users, T = tokens, R = reservations
 * 
 * EXTENSIONS FOR PHASE 4:
 * - Add rate limiting (credits per time window)
 * - Add reservation timeout (auto-release)
 * - Add audit logging for compliance
 */
public class Phase3GPUCredit {
    
    public static final String TIER_PREMIUM = "premium";
    public static final String TIER_STANDARD = "standard";
    
    // userId → PriorityQueue of credits
    // We'll sort differently based on operation (reserve vs use)
    private Map<String, List<Phase3CreditToken>> creditsByUser;
    
    // userId → list of active reservations
    private Map<String, List<Reservation>> reservationsByUser;
    
    // reservationId → Reservation (for fast lookup)
    private Map<String, Reservation> reservationsById;
    
    public Phase3GPUCredit() {
        this.creditsByUser = new HashMap<>();
        this.reservationsByUser = new HashMap<>();
        this.reservationsById = new HashMap<>();
    }
    
    /**
     * Add credits with tier support
     * 
     * @param tier "premium" or "standard"
     * 
     * Time: O(1) - we use List instead of PriorityQueue for flexibility
     *              Will sort on-demand during operations
     */
    public void addCredit(String userId, String grantId, int amount, 
                         int timestamp, int expiration, String tier) {
        validateUserId(userId);
        validateGrantId(grantId);
        validateTier(tier);
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive: " + amount);
        }
        if (expiration <= 0) {
            throw new IllegalArgumentException("Expiration must be positive: " + expiration);
        }
        
        Phase3CreditToken token = new Phase3CreditToken(
            grantId, amount, timestamp, expiration, tier
        );
        
        creditsByUser
            .computeIfAbsent(userId, k -> new ArrayList<>())
            .add(token);
    }
    
    /**
     * Reserve credits for a long-running job
     * 
     * ALGORITHM:
     * 1. Filter tokens by tier (premium job → premium tokens only)
     * 2. Sort by expiration time (use soonest-expiring first)
     * 3. Mark tokens as reserved (don't remove from pool)
     * 4. Create Reservation object tracking affected tokens
     * 5. Return reservation ID
     * 
     * @param tier "premium" (premium credits only) or "standard" (any credits, premium first)
     * @return reservationId or null if insufficient credits
     * 
     * Time: O(n log n + k) where n = tokens to sort, k = tokens reserved
     */
    public String reserveCredit(String userId, int amount, int timestamp, String tier) {
        validateUserId(userId);
        validateTier(tier);
        if (amount <= 0) {
            return null;
        }
        
        List<Phase3CreditToken> userTokens = creditsByUser.get(userId);
        if (userTokens == null) {
            return null;
        }
        
        // Get credits sorted by priority and expiration
        List<Phase3CreditToken> sortedTokens = getSortedTokensForTier(userTokens, tier);
        
        // Try to reserve the amount
        Reservation reservation = new Reservation(userId, amount, timestamp, tier);
        int remaining = amount;
        
        for (Phase3CreditToken token : sortedTokens) {
            if (remaining <= 0) break;
            
            int available = token.getAvailableBalance(timestamp);
            if (available > 0) {
                int toReserve = Math.min(available, remaining);
                
                if (token.reserve(toReserve, timestamp)) {
                    reservation.addAffectedToken(token, toReserve);
                    remaining -= toReserve;
                }
            }
        }
        
        if (remaining > 0) {
            // Insufficient credits - rollback reservations
            rollbackReservation(reservation);
            return null;
        }
        
        // Success - store reservation
        reservationsByUser
            .computeIfAbsent(userId, k -> new ArrayList<>())
            .add(reservation);
        reservationsById.put(reservation.getId(), reservation);
        
        return reservation.getId();
    }
    
    /**
     * Commit a reservation (job completed successfully)
     * Converts reserved credits to used credits
     * 
     * @return true if successful, false if reservation not found or credits expired
     * 
     * Time: O(k) where k = tokens in reservation
     */
    public boolean commitReservation(String userId, String reservationId, int timestamp) {
        validateUserId(userId);
        
        Reservation reservation = reservationsById.get(reservationId);
        if (reservation == null || !reservation.getUserId().equals(userId)) {
            return false;
        }
        
        // Check if any reserved credits have expired
        for (Reservation.TokenReservation tr : reservation.getAffectedTokens()) {
            if (tr.getToken().isExpired(timestamp)) {
                // Could either fail or skip expired tokens
                // For interview, let's fail for safety
                return false;
            }
        }
        
        // Commit all reserved amounts
        for (Reservation.TokenReservation tr : reservation.getAffectedTokens()) {
            tr.getToken().commit(tr.getReservedAmount());
        }
        
        // Remove reservation
        removeReservation(reservation);
        
        return true;
    }
    
    /**
     * Release a reservation (job failed/canceled)
     * Returns reserved credits back to available pool
     * 
     * Time: O(k) where k = tokens in reservation
     */
    public void releaseReservation(String userId, String reservationId, int timestamp) {
        validateUserId(userId);
        
        Reservation reservation = reservationsById.get(reservationId);
        if (reservation == null || !reservation.getUserId().equals(userId)) {
            return;
        }
        
        // Release all reserved amounts back to available
        for (Reservation.TokenReservation tr : reservation.getAffectedTokens()) {
            tr.getToken().release(tr.getReservedAmount());
        }
        
        // Remove reservation
        removeReservation(reservation);
    }
    
    /**
     * Get total balance (including reserved credits)
     * Useful for showing user's total credit balance
     * 
     * Time: O(n) where n = tokens for user
     */
    public int getBalance(String userId, int timestamp) {
        validateUserId(userId);
        
        List<Phase3CreditToken> userTokens = creditsByUser.get(userId);
        if (userTokens == null) {
            return 0;
        }
        
        int total = 0;
        for (Phase3CreditToken token : userTokens) {
            total += token.getTotalBalance(timestamp);
        }
        return total;
    }
    
    /**
     * Get available balance (excluding reserved credits)
     * This is what user can actually use immediately
     * 
     * Time: O(n) where n = tokens for user
     */
    public int getAvailableBalance(String userId, int timestamp) {
        validateUserId(userId);
        
        List<Phase3CreditToken> userTokens = creditsByUser.get(userId);
        if (userTokens == null) {
            return 0;
        }
        
        int total = 0;
        for (Phase3CreditToken token : userTokens) {
            total += token.getAvailableBalance(timestamp);
        }
        return total;
    }
    
    /**
     * Direct credit usage (without reservation)
     * Uses tier-priority: premium credits first for premium tier
     * 
     * Time: O(n log n + k) where n = tokens, k = tokens used
     */
    public boolean useCredit(String userId, int amount, int timestamp, String tier) {
        validateUserId(userId);
        validateTier(tier);
        if (amount <= 0) {
            return false;
        }
        
        List<Phase3CreditToken> userTokens = creditsByUser.get(userId);
        if (userTokens == null) {
            return false;
        }
        
        if (getAvailableBalance(userId, timestamp) < amount) {
            return false;
        }
        
        // Get sorted tokens by tier priority
        List<Phase3CreditToken> sortedTokens = getSortedTokensForTier(userTokens, tier);
        
        int remaining = amount;
        for (Phase3CreditToken token : sortedTokens) {
            if (remaining <= 0) break;
            
            int available = token.getAvailableBalance(timestamp);
            if (available > 0) {
                int toUse = Math.min(available, remaining);
                token.used += toUse;
                remaining -= toUse;
                
                if (token.used >= token.amount) {
                    token.state = TokenState.CONSUMED;
                }
            }
        }
        
        return remaining == 0;
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Get tokens sorted by tier priority and expiration
     * 
     * SORTING STRATEGY:
     * - Premium tier: premium tokens first, then standard (both sorted by expiration)
     * - Standard tier: all tokens sorted by expiration (no tier preference)
     * 
     * Time: O(n log n)
     */
    private List<Phase3CreditToken> getSortedTokensForTier(
            List<Phase3CreditToken> tokens, String tier) {
        
        List<Phase3CreditToken> sorted = new ArrayList<>(tokens);
        
        if (TIER_PREMIUM.equals(tier)) {
            // Premium jobs: premium credits first, then standard
            sorted.sort(Comparator
                .comparing((Phase3CreditToken t) -> !TIER_PREMIUM.equals(t.getTier()))
                .thenComparingInt(Phase3CreditToken::getExpirationTime)
            );
        } else {
            // Standard jobs: all credits by expiration only
            sorted.sort(Comparator.comparingInt(Phase3CreditToken::getExpirationTime));
        }
        
        return sorted;
    }
    
    private void rollbackReservation(Reservation reservation) {
        for (Reservation.TokenReservation tr : reservation.getAffectedTokens()) {
            tr.getToken().release(tr.getReservedAmount());
        }
    }
    
    private void removeReservation(Reservation reservation) {
        List<Reservation> userReservations = reservationsByUser.get(reservation.getUserId());
        if (userReservations != null) {
            userReservations.remove(reservation);
        }
        reservationsById.remove(reservation.getId());
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
    
    private void validateTier(String tier) {
        if (!TIER_PREMIUM.equals(tier) && !TIER_STANDARD.equals(tier)) {
            throw new IllegalArgumentException(
                "Tier must be 'premium' or 'standard': " + tier
            );
        }
    }
    
    // ============ TEST CASES ============
    
    public static void main(String[] args) {
        System.out.println("=== Phase 3: Reservations & Priority Tiers Tests ===\n");
        
        Phase3GPUCredit gpu = new Phase3GPUCredit();
        
        // Test 1: Basic reservation and commit
        gpu.addCredit("user1", "g1", 100, 0, 200, TIER_PREMIUM);
        assert gpu.getBalance("user1", 20) == 100 : "Test 1a failed";
        assert gpu.getAvailableBalance("user1", 20) == 100 : "Test 1b failed";
        
        String resId = gpu.reserveCredit("user1", 80, 30, TIER_PREMIUM);
        assert resId != null : "Test 1c failed";
        assert gpu.getBalance("user1", 35) == 100 : "Test 1d failed (total unchanged)";
        assert gpu.getAvailableBalance("user1", 35) == 20 : "Test 1e failed (80 reserved)";
        System.out.println("✓ Test 1: Basic reservation");
        
        // Test 2: Commit reservation
        assert gpu.commitReservation("user1", resId, 50) : "Test 2a failed";
        assert gpu.getBalance("user1", 55) == 20 : "Test 2b failed";
        assert gpu.getAvailableBalance("user1", 55) == 20 : "Test 2c failed";
        System.out.println("✓ Test 2: Commit reservation");
        
        // Test 3: Release reservation
        gpu = new Phase3GPUCredit();
        gpu.addCredit("user1", "g1", 100, 0, 200, TIER_STANDARD);
        String resId2 = gpu.reserveCredit("user1", 50, 10, TIER_STANDARD);
        assert gpu.getAvailableBalance("user1", 15) == 50 : "Test 3a failed";
        gpu.releaseReservation("user1", resId2, 20);
        assert gpu.getAvailableBalance("user1", 25) == 100 : "Test 3b failed (released)";
        System.out.println("✓ Test 3: Release reservation");
        
        // Test 4: Insufficient credits for reservation
        gpu = new Phase3GPUCredit();
        gpu.addCredit("user1", "g1", 50, 0, 100, TIER_STANDARD);
        String resId3 = gpu.reserveCredit("user1", 100, 10, TIER_STANDARD);
        assert resId3 == null : "Test 4 failed (should fail)";
        assert gpu.getAvailableBalance("user1", 15) == 50 : "Test 4b failed (unchanged)";
        System.out.println("✓ Test 4: Insufficient credits for reservation");
        
        // Test 5: Priority tier consumption
        gpu = new Phase3GPUCredit();
        gpu.addCredit("user1", "premium-grant", 100, 0, 100, TIER_PREMIUM);
        gpu.addCredit("user1", "standard-grant", 100, 5, 100, TIER_STANDARD);
        
        String resId4 = gpu.reserveCredit("user1", 50, 10, TIER_PREMIUM);
        assert resId4 != null : "Test 5a failed";
        
        // Premium reservation should take from premium credits first
        assert gpu.getAvailableBalance("user1", 15) == 150 : "Test 5b failed";
        System.out.println("✓ Test 5: Priority tier consumption");
        
        // Test 6: Multiple concurrent reservations
        gpu = new Phase3GPUCredit();
        gpu.addCredit("user1", "g1", 200, 0, 200, TIER_STANDARD);
        String res1 = gpu.reserveCredit("user1", 50, 10, TIER_STANDARD);
        String res2 = gpu.reserveCredit("user1", 80, 20, TIER_STANDARD);
        assert res1 != null && res2 != null : "Test 6a failed";
        assert gpu.getAvailableBalance("user1", 25) == 70 : "Test 6b failed (200 - 50 - 80)";
        
        gpu.commitReservation("user1", res1, 30);
        assert gpu.getBalance("user1", 35) == 150 : "Test 6c failed";
        assert gpu.getAvailableBalance("user1", 35) == 70 : "Test 6d failed (still 80 reserved)";
        
        gpu.releaseReservation("user1", res2, 40);
        assert gpu.getAvailableBalance("user1", 45) == 150 : "Test 6e failed";
        System.out.println("✓ Test 6: Multiple concurrent reservations");
        
        // Test 7: Can't use reserved credits
        gpu = new Phase3GPUCredit();
        gpu.addCredit("user1", "g1", 100, 0, 200, TIER_STANDARD);
        gpu.reserveCredit("user1", 80, 10, TIER_STANDARD);
        assert !gpu.useCredit("user1", 50, 20, TIER_STANDARD) : "Test 7 failed (only 20 available)";
        System.out.println("✓ Test 7: Can't use reserved credits");
        
        // Test 8: Reservation spanning multiple tokens
        gpu = new Phase3GPUCredit();
        gpu.addCredit("user1", "g1", 50, 0, 50, TIER_STANDARD);
        gpu.addCredit("user1", "g2", 80, 10, 100, TIER_STANDARD);
        String resId5 = gpu.reserveCredit("user1", 100, 20, TIER_STANDARD);
        assert resId5 != null : "Test 8a failed";
        assert gpu.getAvailableBalance("user1", 25) == 30 : "Test 8b failed (130 - 100)";
        System.out.println("✓ Test 8: Reservation spanning multiple tokens");
        
        System.out.println("\n✅ All Phase 3 tests passed!");
    }
}

