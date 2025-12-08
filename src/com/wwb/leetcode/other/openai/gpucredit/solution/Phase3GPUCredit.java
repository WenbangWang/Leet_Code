package com.wwb.leetcode.other.openai.gpucredit.solution;

import java.util.*;

/**
 * PHASE 3: Credit Reservations with Priority Tiers (18 minutes)
 * 
 * PROBLEM STATEMENT:
 * Support long-running GPU jobs that need to reserve credits upfront.
 * Add priority tiers: PREMIUM credits are consumed before STANDARD.
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
 * 3. "Does PREMIUM tier mean premium-only or premium-first?" 
 *    → Premium-first: use PREMIUM credits first, then STANDARD
 * 4. "Should reservations have a timeout?" 
 *    → Nice to have for production, not required for interview
 * 5. "Can we over-reserve (reserve more than available)?" 
 *    → No, return null if insufficient
 * 
 * KEY INSIGHTS:
 * - Reservation is a two-phase commit: reserve → commit/release
 * - Nested map structure: Map<userId, Map<tier, PriorityQueue<token>>>
 * - Tokens don't store tier - tier determined by which queue they're in
 * - Simple compareTo: just expiration (no composite sorting needed)
 * - Consumption logic manually checks PREMIUM queue first, then STANDARD
 * 
 * TIME COMPLEXITY:
 * - addCredit: O(log n) where n = tokens per user per tier
 * - reserveCredit: O(k log n) where k = tokens touched
 * - commitReservation: O(k) where k = tokens in reservation
 * - releaseReservation: O(k) where k = tokens in reservation
 * - getAvailableBalance: O(n) to sum across all tiers
 * 
 * SPACE COMPLEXITY: O(U * 2 * T + R) where U = users, T = avg tokens per tier, R = reservations
 * 
 * DESIGN TRADE-OFFS:
 * - Nested maps vs Embedded tier in token:
 *   Nested: Simpler token, explicit separation, but more complex consumption
 *   Embedded: Complex token, automatic ordering, simpler consumption
 *   We chose nested for cleaner separation and token simplicity.
 */
public class Phase3GPUCredit {
    
    // userId → tier → PriorityQueue of credits (sorted by expiration only)
    private Map<String, Map<Tier, PriorityQueue<Phase3CreditToken>>> creditsByUser;
    
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
     * Add credits to user's pool with tier
     * 
     * Time: O(log n) where n = tokens for this user in this tier
     * 
     * INTERVIEW NOTE:
     * Using nested maps allows us to separate PREMIUM and STANDARD queues.
     * Each queue sorts by expiration only (simpler compareTo).
     */
    public void addCredit(String userId, String grantId, int amount, 
                         int timestamp, int expiration, Tier tier) {
        validateUserId(userId);
        validateGrantId(grantId);
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive: " + amount);
        }
        if (expiration <= 0) {
            throw new IllegalArgumentException("Expiration must be positive: " + expiration);
        }
        
        Phase3CreditToken token = new Phase3CreditToken(
            grantId, amount, timestamp, expiration
        );
        
        creditsByUser
            .computeIfAbsent(userId, k -> new HashMap<>())
            .computeIfAbsent(tier, k -> new PriorityQueue<>())
            .offer(token);
    }
    
    /**
     * Reserve credits for a long-running job
     * 
     * ALGORITHM:
     * 1. If PREMIUM tier: check PREMIUM queue first, then STANDARD
     * 2. If STANDARD tier: check STANDARD queue only
     * 3. Poll tokens, mark as reserved, add back to queue
     * 4. Create Reservation tracking affected tokens
     * 
     * @param tier Requested tier
     *             PREMIUM: uses PREMIUM credits first, then STANDARD if needed
     *             STANDARD: uses STANDARD credits only
     * @return reservationId or null if insufficient credits
     * 
     * Time: O(k log n) where k = tokens touched across tiers
     */
    public String reserveCredit(String userId, int amount, int timestamp, Tier tier) {
        validateUserId(userId);
        if (amount <= 0) {
            return null;
        }
        
        Map<Tier, PriorityQueue<Phase3CreditToken>> userTiers = creditsByUser.get(userId);
        if (userTiers == null) {
            return null;
        }
        
        Reservation reservation = new Reservation(userId, amount, timestamp, tier);
        int remaining = amount;
        
        if (tier == Tier.PREMIUM) {
            // PREMIUM jobs: use PREMIUM credits first, then STANDARD
            remaining = reserveFromQueue(userTiers.get(Tier.PREMIUM), remaining, timestamp, reservation);
            if (remaining > 0) {
                remaining = reserveFromQueue(userTiers.get(Tier.STANDARD), remaining, timestamp, reservation);
            }
        } else {
            // STANDARD jobs: use STANDARD credits only
            remaining = reserveFromQueue(userTiers.get(Tier.STANDARD), remaining, timestamp, reservation);
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
     * Helper method to reserve credits from a specific tier queue
     * 
     * @return remaining amount after reservation
     * 
     * Time: O(k log n) where k = tokens touched in this queue
     */
    private int reserveFromQueue(PriorityQueue<Phase3CreditToken> queue, 
                                 int remaining, int timestamp, Reservation reservation) {
        if (queue == null || remaining <= 0) {
            return remaining;
        }
        
        int size = queue.size();
        for (int i = 0; i < size && remaining > 0; i++) {
            Phase3CreditToken token = queue.poll();
            
            int available = token.getAvailableBalance(timestamp);
            if (available > 0) {
                int toReserve = Math.min(available, remaining);
                
                if (token.reserve(toReserve, timestamp)) {
                    reservation.addAffectedToken(token, toReserve);
                    remaining -= toReserve;
                }
            }
            
            // Always add back to maintain queue
            queue.offer(token);
        }
        
        return remaining;
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
                // Fail for safety - don't commit expired credits
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
     * Get total balance across all tiers (including reserved credits)
     * 
     * Time: O(n) where n = tokens across all tiers for user
     */
    public int getBalance(String userId, int timestamp) {
        validateUserId(userId);
        
        Map<Tier, PriorityQueue<Phase3CreditToken>> userTiers = creditsByUser.get(userId);
        if (userTiers == null) {
            return 0;
        }
        
        int total = 0;
        for (PriorityQueue<Phase3CreditToken> queue : userTiers.values()) {
            for (Phase3CreditToken token : queue) {
                total += token.getTotalBalance(timestamp);
            }
        }
        return total;
    }
    
    /**
     * Get available balance across all tiers (excluding reserved credits)
     * 
     * Time: O(n) where n = tokens across all tiers for user
     */
    public int getAvailableBalance(String userId, int timestamp) {
        validateUserId(userId);
        
        Map<Tier, PriorityQueue<Phase3CreditToken>> userTiers = creditsByUser.get(userId);
        if (userTiers == null) {
            return 0;
        }
        
        int total = 0;
        for (PriorityQueue<Phase3CreditToken> queue : userTiers.values()) {
            for (Phase3CreditToken token : queue) {
                total += token.getAvailableBalance(timestamp);
            }
        }
        return total;
    }
    
    /**
     * Direct credit usage (without reservation)
     * Uses tier priority: PREMIUM credits consumed before STANDARD
     * 
     * Time: O(k log n) where k = tokens used across tiers
     */
    public boolean useCredit(String userId, int amount, int timestamp) {
        validateUserId(userId);
        if (amount <= 0) {
            return false;
        }
        
        Map<Tier, PriorityQueue<Phase3CreditToken>> userTiers = creditsByUser.get(userId);
        if (userTiers == null) {
            return false;
        }
        
        if (getAvailableBalance(userId, timestamp) < amount) {
            return false;
        }
        
        int remaining = amount;
        
        // Use PREMIUM credits first, then STANDARD
        remaining = useFromQueue(userTiers.get(Tier.PREMIUM), remaining, timestamp);
        if (remaining > 0) {
            remaining = useFromQueue(userTiers.get(Tier.STANDARD), remaining, timestamp);
        }
        
        return remaining == 0;
    }
    
    /**
     * Helper method to use credits from a specific tier queue
     * 
     * @return remaining amount after usage
     * 
     * Time: O(k log n) where k = tokens used from this queue
     */
    private int useFromQueue(PriorityQueue<Phase3CreditToken> queue, 
                            int remaining, int timestamp) {
        if (queue == null || remaining <= 0) {
            return remaining;
        }
        
        int size = queue.size();
        for (int i = 0; i < size && remaining > 0; i++) {
            Phase3CreditToken token = queue.poll();
            
            int available = token.getAvailableBalance(timestamp);
            if (available > 0) {
                int toUse = Math.min(available, remaining);
                token.used += toUse;
                remaining -= toUse;
                
                if (token.used >= token.amount) {
                    token.state = TokenState.CONSUMED;
                }
            }
            
            // Add back if not fully consumed or expired
            if (token.getAvailableBalance(timestamp) > 0 || !token.isExpired(timestamp)) {
                queue.offer(token);
            }
        }
        
        return remaining;
    }
    
    // ========== HELPER METHODS ==========
    
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
    
    // ============ TEST CASES ============
    
    public static void main(String[] args) {
        System.out.println("=== Phase 3: Reservations with Tier Priority Tests ===\n");
        
        Phase3GPUCredit gpu = new Phase3GPUCredit();
        
        // Test 1: Basic reservation and commit
        gpu.addCredit("user1", "g1", 100, 0, 200, Tier.STANDARD);
        assert gpu.getBalance("user1", 20) == 100 : "Test 1a failed";
        assert gpu.getAvailableBalance("user1", 20) == 100 : "Test 1b failed";
        
        String resId = gpu.reserveCredit("user1", 80, 30, Tier.STANDARD);
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
        gpu.addCredit("user1", "g1", 100, 0, 200, Tier.STANDARD);
        String resId2 = gpu.reserveCredit("user1", 50, 10, Tier.STANDARD);
        assert gpu.getAvailableBalance("user1", 15) == 50 : "Test 3a failed";
        gpu.releaseReservation("user1", resId2, 20);
        assert gpu.getAvailableBalance("user1", 25) == 100 : "Test 3b failed (released)";
        System.out.println("✓ Test 3: Release reservation");
        
        // Test 4: Tier priority - PREMIUM consumed first
        gpu = new Phase3GPUCredit();
        gpu.addCredit("user1", "standard-grant", 100, 0, 200, Tier.STANDARD);
        gpu.addCredit("user1", "premium-grant", 50, 10, 150, Tier.PREMIUM);
        
        assert gpu.useCredit("user1", 40, 20) : "Test 4a failed";
        // Should use from PREMIUM queue first (via useFromQueue logic)
        assert gpu.getBalance("user1", 25) == 110 : "Test 4b failed";
        System.out.println("✓ Test 4: Tier priority consumption");
        
        // Test 5: PREMIUM reservation uses PREMIUM first, then STANDARD
        gpu = new Phase3GPUCredit();
        gpu.addCredit("user1", "premium", 30, 0, 100, Tier.PREMIUM);
        gpu.addCredit("user1", "standard", 100, 0, 100, Tier.STANDARD);
        
        String resId3 = gpu.reserveCredit("user1", 80, 10, Tier.PREMIUM);
        assert resId3 != null : "Test 5a failed";
        // 30 from PREMIUM queue, 50 from STANDARD queue
        assert gpu.getAvailableBalance("user1", 15) == 50 : "Test 5b failed";
        System.out.println("✓ Test 5: PREMIUM reservation spans tiers");
        
        // Test 6: STANDARD reservation uses STANDARD only
        gpu = new Phase3GPUCredit();
        gpu.addCredit("user1", "premium", 50, 0, 100, Tier.PREMIUM);
        gpu.addCredit("user1", "standard", 30, 0, 100, Tier.STANDARD);
        
        String resId4 = gpu.reserveCredit("user1", 40, 10, Tier.STANDARD);
        assert resId4 == null : "Test 6 failed (insufficient STANDARD credits)";
        System.out.println("✓ Test 6: STANDARD reservation respects tier limit");
        
        // Test 7: Multiple concurrent reservations
        gpu = new Phase3GPUCredit();
        gpu.addCredit("user1", "g1", 200, 0, 200, Tier.STANDARD);
        String res1 = gpu.reserveCredit("user1", 50, 10, Tier.STANDARD);
        String res2 = gpu.reserveCredit("user1", 80, 20, Tier.STANDARD);
        assert res1 != null && res2 != null : "Test 7a failed";
        assert gpu.getAvailableBalance("user1", 25) == 70 : "Test 7b failed";
        
        gpu.commitReservation("user1", res1, 30);
        assert gpu.getBalance("user1", 35) == 150 : "Test 7c failed";
        
        gpu.releaseReservation("user1", res2, 40);
        assert gpu.getAvailableBalance("user1", 45) == 150 : "Test 7d failed";
        System.out.println("✓ Test 7: Multiple concurrent reservations");
        
        // Test 8: Can't use reserved credits
        gpu = new Phase3GPUCredit();
        gpu.addCredit("user1", "g1", 100, 0, 200, Tier.STANDARD);
        gpu.reserveCredit("user1", 80, 10, Tier.STANDARD);
        assert !gpu.useCredit("user1", 50, 20) : "Test 8 failed (only 20 available)";
        System.out.println("✓ Test 8: Can't use reserved credits");
        
        System.out.println("\n✅ All Phase 3 tests passed!");
    }
}
