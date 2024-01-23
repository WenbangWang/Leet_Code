package com.wwb.leetcode.tags.array;

import java.util.HashMap;
import java.util.Map;

/**
 * At a lemonade stand, each lemonade costs $5. Customers are standing in a queue to buy
 * from you and order one at a time (in the order specified by bills).
 * Each customer will only buy one lemonade and pay with either a $5, $10, or $20 bill.
 * You must provide the correct change to each customer so that the net transaction is that the customer pays $5.
 *
 * Note that you do not have any change in hand at first.
 *
 * Given an integer array bills where bills[i] is the bill the ith customer pays,
 * return true if you can provide every customer with the correct change, or false otherwise.
 *
 *
 *
 * Example 1:
 *
 * Input: bills = [5,5,5,10,20]
 * Output: true
 * Explanation:
 * From the first 3 customers, we collect three $5 bills in order.
 * From the fourth customer, we collect a $10 bill and give back a $5.
 * From the fifth customer, we give a $10 bill and a $5 bill.
 * Since all customers got correct change, we output true.
 * Example 2:
 *
 * Input: bills = [5,5,10,10,20]
 * Output: false
 * Explanation:
 * From the first two customers in order, we collect two $5 bills.
 * For the next two customers in order, we collect a $10 bill and give back a $5 bill.
 * For the last customer, we can not give the change of $15 back because we only have two $10 bills.
 * Since not every customer received the correct change, the answer is false.
 *
 *
 * Constraints:
 *
 * 1 <= bills.length <= 10^5
 * bills[i] is either 5, 10, or 20.
 */
public class No860 {
    public boolean lemonadeChange(int[] bills) {
        return solution1(bills);
    }

    private boolean solution1(int[] bills) {
        Map<Integer, Integer> hand = new HashMap<>(Map.of(5, 0, 10, 0, 20, 0));

        return canComplete(bills, 0, hand);
    }

    private boolean solution2(int[] biils) {
        int fives = 0;
        int tens = 0;

        for (int bill : biils) {
            if (bill == 5) {
                fives++;
            } else if (bill == 10) {
                tens++;
                fives--;
            } else if (tens > 0) { // Given 20, change to ONE 10 and ONE 5 is always a better option
                tens--;
                fives--;
            } else { // 20
                fives -= 3;
            }

            if (fives < 0) {
                return false;
            }
        }

        return true;
    }

    private boolean canComplete(int[] bills, int index, Map<Integer, Integer> hand) {
        if (index == bills.length) {
            return true;
        }
        int bill = bills[index];
        switch (bill) {
            case 5:
                hand.compute(5, (k, v) -> v + 1);
                return canComplete(bills, index + 1, hand);
            case 10:
                if (hand.get(5) == 0) {
                    return false;
                }

                hand.compute(5, (k, v) -> v - 1);
                hand.compute(10, (k, v) -> v + 1);

                return canComplete(bills, index + 1, hand);
            case 20:
                boolean result = false;

                if (hand.get(5) >= 3) {
                    Map<Integer, Integer> newHand = new HashMap<>(hand);
                    newHand.compute(5, (k, v) -> v - 3);
                    result = canComplete(bills, index + 1, newHand);
                }

                if (hand.get(5) >= 1 && hand.get(10) >= 1) {
                    Map<Integer, Integer> newHand = new HashMap<>(hand);
                    newHand.compute(5, (k, v) -> v - 1);
                    newHand.compute(10, (k, v) -> v - 1);
                    result = result || canComplete(bills, index + 1, newHand);
                }

                return result;

        }

        return false;
    }
}
