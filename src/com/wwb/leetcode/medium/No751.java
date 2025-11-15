package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.List;

/**
 * Given a start IP address ip and a number of ips we need to cover n, return a representation of the range as a list (of smallest possible length) of CIDR blocks.
 * <p>
 * A CIDR block is a string consisting of an IP, followed by a slash, and then the prefix length. For example: "123.45.67.89/20". That prefix length "20" represents the number of common prefix bits in the specified range.
 *
 * <pre>
 * Example 1:
 *
 * Input: ip = "255.0.0.7", n = 10
 * Output: ["255.0.0.7/32","255.0.0.8/29","255.0.0.16/32"]
 * Explanation:
 * The initial ip address, when converted to binary, looks like this (spaces added for clarity):
 * 255.0.0.7 -> 11111111 00000000 00000000 00000111
 * The address "255.0.0.7/32" specifies all addresses with a common prefix of 32 bits to the given address,
 * ie. just this one address.
 *
 * The address "255.0.0.8/29" specifies all addresses with a common prefix of 29 bits to the given address:
 * 255.0.0.8 -> 11111111 00000000 00000000 00001000
 * Addresses with common prefix of 29 bits are:
 * 11111111 00000000 00000000 00001000
 * 11111111 00000000 00000000 00001001
 * 11111111 00000000 00000000 00001010
 * 11111111 00000000 00000000 00001011
 * 11111111 00000000 00000000 00001100
 * 11111111 00000000 00000000 00001101
 * 11111111 00000000 00000000 00001110
 * 11111111 00000000 00000000 00001111
 *
 * The address "255.0.0.16/32" specifies all addresses with a common prefix of 32 bits to the given address,
 * ie. just 11111111 00000000 00000000 00010000.
 *
 * In total, the answer specifies the range of 10 ips starting with the address 255.0.0.7 .
 *
 * There were other representations, such as:
 * ["255.0.0.7/32","255.0.0.8/30", "255.0.0.12/30", "255.0.0.16/32"],
 * but our answer was the shortest possible.
 *
 * Also note that a representation beginning with say, "255.0.0.7/30" would be incorrect,
 * because it includes addresses like 255.0.0.4 = 11111111 00000000 00000000 00000100
 * that are outside the specified range.
 * Note:
 *
 * ip will be a valid IPv4 address.
 * Every implied address ip + x (for x < n) will be a valid IPv4 address.
 * n will be an integer in the range [1, 1000].
 * </pre>
 */
public class No751 {
    // 第一問 - iterator from any ip address
    // 第二問 - reverse iterator from any ip address
    // 第三問 - forward/reverse iterator with cidr
    public List<String> ipToCIDR(String startIp, int n) {
        List<String> ans = new ArrayList<>();
        long ip = ipToLong(startIp);
        while (n > 0) {
            // isolates the rightmost 1-bit in x
            long lowbit = ip & -ip; // max aligned block size at this ip
            long blockSize = lowbit;
            // shrink until blockSize <= n
            while (blockSize > n) {
                blockSize >>= 1;
            }
            // blockSize = 2^(32 - prefix)
            // prefix = 32 - log2(blockSize)
            int prefix = 32 - (int)(Math.log(blockSize) / Math.log(2));
            ans.add(longToIp(ip) + "/" + prefix);
            ip += blockSize;
            n -= blockSize;
        }
        return ans;
    }

    // ip address is unsigned int while integer is signed hence need to use long
    private long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        long res = 0;
        for (String p : parts) {
            res = (res << 8) + Integer.parseInt(p);
        }
        return res;
    }

    private String longToIp(long x) {
        return ((x >> 24) & 255) + "." +
            ((x >> 16) & 255) + "." +
            ((x >> 8) & 255) + "." +
            (x & 255);
    }
}
