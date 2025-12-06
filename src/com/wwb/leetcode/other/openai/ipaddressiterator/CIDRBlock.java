package com.wwb.leetcode.other.openai.ipaddressiterator;

class CIDRBlock {
    long start;   // inclusive
    long end;     // exclusive

    CIDRBlock(String cidr) {
        String[] parts = cidr.split("/");
        long ip = IpHelper.ipToLong(parts[0]);
        int prefix = Integer.parseInt(parts[1]);

        // -1L all 1's in a long form (64 bit 1s)
        // move left by 32 - prefix makes the rightmost few bit zeros
        // 0xffffffffL is long form of 32 zeros and 32 ones
        // together will give the subnet mask
        long mask = prefix == 0 ? 0 : (-1L << (32 - prefix)) & 0xffffffffL;

        start = ip & mask;
        // 2^(32 - prefix)
        long size = 1L << (32 - prefix);
        end = start + size;
    }
}
