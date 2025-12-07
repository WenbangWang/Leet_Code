package com.wwb.leetcode.other.openai.gpucredit.solution;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a credit reservation for a long-running GPU job
 * 
 * BUSINESS CONTEXT:
 * When OpenAI users start a training job, they need to reserve GPU credits upfront.
 * The job might run for hours/days. During this time:
 * - Credits are locked (can't be used elsewhere)
 * - If job completes → commit (actually consume credits)
 * - If job fails/cancels → release (return credits to available pool)
 * 
 * DESIGN DECISIONS:
 * 1. Why track affectedTokens?
 *    → Need to know which tokens to update when committing/releasing
 *    → A reservation might span multiple credit tokens
 * 
 * 2. Why store createdAt timestamp?
 *    → For auditing and debugging
 *    → Could implement timeout: auto-release after N hours
 * 
 * 3. Why immutable ID?
 *    → Prevents modification, acts as unique identifier
 *    → Using UUID ensures global uniqueness
 * 
 * INTERVIEW DISCUSSION:
 * Q: "What if credits expire during reservation?"
 * A: Two approaches:
 *    1. Prevent reservation if any credits would expire before expected completion
 *    2. Allow reservation but handle at commit time (fail if expired)
 *    Current implementation: Allow (more flexible)
 * 
 * Q: "Should reservations have a timeout?"
 * A: Yes, in production. Could add:
 *    - int timeoutDuration
 *    - Background thread to auto-release expired reservations
 *    - Or lazy cleanup during other operations
 */
public class Reservation {
    private final String id;
    private final String userId;
    private final int amount;
    private final int createdAt;
    private final String tier;
    
    // Track which tokens were affected by this reservation
    // Needed for commit/release operations
    private final List<TokenReservation> affectedTokens;
    
    public Reservation(String userId, int amount, int timestamp, String tier) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.amount = amount;
        this.createdAt = timestamp;
        this.tier = tier;
        this.affectedTokens = new ArrayList<>();
    }
    
    public String getId() {
        return id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public int getCreatedAt() {
        return createdAt;
    }
    
    public String getTier() {
        return tier;
    }
    
    public List<TokenReservation> getAffectedTokens() {
        return affectedTokens;
    }
    
    public void addAffectedToken(Phase3CreditToken token, int reservedAmount) {
        affectedTokens.add(new TokenReservation(token, reservedAmount));
    }
    
    /**
     * Links a reservation to a specific credit token
     * Tracks how much was reserved from each token
     */
    public static class TokenReservation {
        private final Phase3CreditToken token;
        private final int reservedAmount;
        
        public TokenReservation(Phase3CreditToken token, int reservedAmount) {
            this.token = token;
            this.reservedAmount = reservedAmount;
        }
        
        public Phase3CreditToken getToken() {
            return token;
        }
        
        public int getReservedAmount() {
            return reservedAmount;
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "Reservation{id='%s', userId='%s', amount=%d, tier='%s', tokens=%d}",
            id.substring(0, 8), userId, amount, tier, affectedTokens.size()
        );
    }
}

