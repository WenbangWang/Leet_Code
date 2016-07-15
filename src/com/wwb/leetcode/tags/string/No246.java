package com.wwb.leetcode.tags.string;

import java.util.HashMap;
import java.util.Map;

/**
 * A strobogrammatic number is a number that looks the same when rotated 180 degrees (looked at upside down).
 *
 * Write a function to determine if a number is strobogrammatic. The number is represented as a string.
 *
 * For example, the numbers "69", "88", and "818" are all strobogrammatic.
 */
public class No246 {

    public boolean isStrobogrammatic(String num) {
        if(num == null || num.isEmpty()) {
            return false;
        }

        Map<Character, Character> map = new HashMap<>();

        map.put('0', '0');
        map.put('1', '1');
        map.put('6', '9');
        map.put('8', '8');
        map.put('9', '6');

        int left = 0;
        int right = num.length() - 1;

        while(left <= right) {
            char first = num.charAt(left);
            char last = num.charAt(right);

            if(!map.containsKey(first) || !map.containsKey(last) || first != map.get(last)) {
                return false;
            }

            left++;
            right--;
        }

        return true;
    }
}
