package com.wwb.leetcode.tags.greedy;

/**
 * There are N children standing in a line. Each child is assigned a rating value.
 *
 * You are giving candies to these children subjected to the following requirements:
 *
 * Each child must have at least one candy.
 * Children with a higher rating get more candies than their neighbors.
 * What is the minimum candies you must give?
 */
public class No135 {

    public int candy(int[] ratings) {
        if(ratings == null || ratings.length == 0) {
            return 0;
        }

        int result = 1;
        int pre = 1;
        int countDown = 0;

        for(int i = 1; i < ratings.length; i++) {
            if(ratings[i] >= ratings[i - 1]) {
                if(countDown > 0) {
                    result += countDown * (1 + countDown) / 2;
                    if(countDown >= pre) {
                        result += countDown - pre + 1;
                    }
                    pre = 1;
                    countDown = 0;
                }

                pre = ratings[i] == ratings[i - 1] ? 1 : pre + 1;
                result += pre;
            } else {
                countDown++;
            }
        }

        if(countDown > 0) {
            result += countDown * (1 + countDown) / 2;
            if(countDown >= pre) {
                result += countDown - pre + 1;
            }
        }

        return result;
    }
}
