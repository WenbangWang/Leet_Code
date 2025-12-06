package com.wwb.leetcode.other.openai.gpucredit;

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

    int getBalance(int timestamp) {
        if (timestamp < startTime || isExpired(timestamp)) {
            return 0;
        }

        return amount - used;
    }

    boolean isExpired(int timestamp) {
        return timestamp > startTime + expiration;
    }

    @Override
    public int compareTo(CreditToken o) {
        return Integer.compare(this.startTime + this.expiration, o.startTime + o.expiration);
    }
}
