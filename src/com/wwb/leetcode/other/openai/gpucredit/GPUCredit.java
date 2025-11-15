package com.wwb.leetcode.other.openai.gpucredit;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class GPUCredit {
    private Map<String, PriorityQueue<CreditToken>> credits;

    public GPUCredit() {
        credits = new HashMap<>();
    }

    public void addCredit(String creditId, int amount, int timestamp, int expiration) {
        CreditToken credit = new CreditToken(amount, timestamp, expiration);
        credits.computeIfAbsent(
            creditId,
            k -> new PriorityQueue<>(Comparator.comparingInt(c -> c.startTime + c.expiration))
        ).offer(credit);
    }

    public Integer getBalance(String creditId, int timestamp) {
        PriorityQueue<CreditToken> pq = credits.get(creditId);
        if (pq == null) {
            return null;
        }

        int total = 0;
        for (CreditToken c : pq) {
            total += c.getBalance(timestamp);
        }
        return total > 0 ? total : null;
    }

    public void useCredit(String creditId, int timestamp, int amount) {
        PriorityQueue<CreditToken> pq = credits.get(creditId);
        if (pq == null || amount <= 0) {
            return;
        }

        int remaining = amount;
        int size = pq.size();

        for (int i = 0; i < size && remaining > 0; i++) {
            CreditToken c = pq.poll();
            int balance = c.getBalance(timestamp);
            if (balance > 0) {
                int toUse = Math.min(balance, remaining);
                c.used += toUse;
                remaining -= toUse;
            }

            if (c.getBalance(timestamp) != 0) {
                pq.add(c);
            }
        }
//        List<CreditToken> temp = new ArrayList<>(); // temporarily hold credits while processing

//
//        while (!pq.isEmpty() && remaining > 0) {
//            CreditToken c = pq.poll();
//            int balance = c.getBalance(timestamp);
//            if (balance > 0) {
//                int toUse = Math.min(balance, remaining);
//                c.used += toUse;
//                remaining -= toUse;
//            }
//            temp.add(c); // put it back after using
//        }

//        // put all credits back into the priority queue
//        pq.addAll(temp);
    }
}
