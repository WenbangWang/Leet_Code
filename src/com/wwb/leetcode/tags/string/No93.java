package com.wwb.leetcode.tags.string;

import java.util.ArrayList;
import java.util.List;

/**
 * Given a string containing only digits, restore it by returning all possible valid IP address combinations.
 *
 * For example:
 * Given "25525511135",
 *
 * return ["255.255.11.135", "255.255.111.35"]. (Order does not matter)
 */
public class No93 {

    public List<String> restoreIpAddresses(String s) {
        List<String> result = new ArrayList<>();

        if(s == null || s.length() < 4 || s.length() > 12) {
            return result;
        }

        int length = s.length();

        for(int i = 1; i < 4 && i < length - 2; i++) {
            for(int j = i + 1; j < i + 4 && j < length - 1; j++) {
                for(int k = j + 1; k < j + 4 && k < length; k++) {
                    String firstDomain = s.substring(0, i);
                    String secondDomain = s.substring(i, j);
                    String thirdDomain = s.substring(j, k);
                    String forthDomain = s.substring(k, length);

                    if(isValid(firstDomain) && isValid(secondDomain) && isValid(thirdDomain) && isValid(forthDomain)) {
                        result.add(firstDomain + "." + secondDomain + "." + thirdDomain + "." + forthDomain);
                    }
                }
            }
        }

        return result;
    }

    private boolean isValid(String s) {
        return !(s == null || s.length() == 0 || s.length() > 3 || (s.charAt(0) == '0' && s.length() > 1) || Integer.parseInt(s) > 255);
    }
}