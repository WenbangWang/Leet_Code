package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.List;

/**
 * Design an algorithm to encode a list of strings to a string.
 * The encoded string is then sent over the network and is decoded back to the original list of strings.
 *
 * Machine 1 (sender) has the function:
 *
 * string encode(vector<string> strs) {
 *   // ... your code
 *   return encoded_string;
 * }
 * Machine 2 (receiver) has the function:
 * vector<string> decode(string s) {
 *   //... your code
 *   return strs;
 * }
 * So Machine 1 does:
 *
 * string encoded_string = encode(strs);
 * and Machine 2 does:
 *
 * vector<string> strs2 = decode(encoded_string);
 * strs2 in Machine 2 should be the same as strs in Machine 1.
 *
 * Implement the encode and decode methods.
 *
 * You are not allowed to solve the problem using any serialize methods (such as eval).
 *
 *
 *
 * Example 1:
 *
 * Input: dummy_input = ["Hello","World"]
 * Output: ["Hello","World"]
 * Explanation:
 * Machine 1:
 * Codec encoder = new Codec();
 * String msg = encoder.encode(strs);
 * Machine 1 ---msg---> Machine 2
 *
 * Machine 2:
 * Codec decoder = new Codec();
 * String[] strs = decoder.decode(msg);
 * Example 2:
 *
 * Input: dummy_input = [""]
 * Output: [""]
 *
 *
 * Constraints:
 *
 * 1 <= strs.length <= 200
 * 0 <= strs[i].length <= 200
 * strs[i] contains any possible characters out of 256 valid ASCII characters.
 *
 *
 * Follow up: Could you write a generalized algorithm to work on any possible set of characters?
 */
public class No271 {
    public class Codec {
        // Encodes a list of strings to a single string.
        public String encode(List<String> strs) {
            StringBuilder sb = new StringBuilder();
            for(String s: strs) {
                sb.append(intToString(s.length()));
                sb.append(s);
            }
            return sb.toString();
        }

        // Decodes a single string to a list of strings.
        public List<String> decode(String s) {
            int i = 0, n = s.length();
            List<String> output = new ArrayList<>();
            while (i < n) {
                int length = stringToInt(s.substring(i, i + 2));
                i += 2;
                output.add(s.substring(i, i + length));
                i += length;
            }
            return output;
        }

        // Encodes string length to bytes string
        private String intToString(int x) {
            char[] bytes = new char[2];
            int mask = (1 << 16) - 1;

            for(int i = 1; i >= 0; i--) {
                bytes[1 - i] = (char) (x >> (i * 16) & mask);
            }
            return new String(bytes);
        }

        // Decodes bytes string to integer
        private int stringToInt(String bytesStr) {
            int result = 0;

            for(int i = 0; i < bytesStr.length(); i++) {
                result = (result << 16) + (int) bytesStr.charAt(i);
            }
            return result;
        }
    }
}
