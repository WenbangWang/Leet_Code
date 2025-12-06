package com.wwb.leetcode.other.openai.ipaddressiterator;

public class IpHelper {
    // ip address is unsigned int while integer is signed hence need to use long
    public static long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        long res = 0;
        for (String p : parts) {
            res = (res << 8) + Integer.parseInt(p);
        }
        return res;
    }

    public static String longToIp(long x) {
        return ((x >> 24) & 255) + "." +
            ((x >> 16) & 255) + "." +
            ((x >> 8) & 255) + "." +
            (x & 255);
    }
}
