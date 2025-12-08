package com.wwb.leetcode.other.openai.ipaddressiterator.interview;

import com.wwb.leetcode.other.openai.ipaddressiterator.IpHelper;

/**
 * Utility class for working with IP ranges and CIDR notation.
 * Provides methods to parse CIDR blocks and convert to segments.
 */
class IPRange {
    
    /**
     * Parse a CIDR block (e.g., "192.168.1.0/24") into a Segment
     */
    static Segment fromCIDR(String cidr) {
        if (!cidr.contains("/")) {
            // Single IP address
            long ip = IpHelper.ipToLong(cidr);
            return new Segment(ip, ip);
        }

        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid CIDR format: " + cidr);
        }

        long ip = IpHelper.ipToLong(parts[0]);
        int prefix = Integer.parseInt(parts[1]);

        if (prefix < 0 || prefix > 32) {
            throw new IllegalArgumentException("CIDR prefix must be 0-32: " + prefix);
        }

        // Calculate subnet mask
        // -1L has all 64 bits set to 1
        // Shift left by (32 - prefix) to create mask
        // AND with 0xffffffffL to keep only lower 32 bits
        long mask = prefix == 0 ? 0 : (-1L << (32 - prefix)) & 0xffffffffL;

        long start = ip & mask;
        long size = 1L << (32 - prefix);  // 2^(32 - prefix)
        long end = start + size - 1;

        return new Segment(start, end);
    }

    /**
     * Parse an IP range (e.g., "192.168.1.0-192.168.1.255") into a Segment
     */
    static Segment fromRange(String startIp, String endIp) {
        long start = IpHelper.ipToLong(startIp);
        long end = IpHelper.ipToLong(endIp);
        return new Segment(start, end);
    }

    /**
     * Convert a segment back to the smallest CIDR blocks that cover it.
     * A single segment may require multiple CIDR blocks to represent exactly.
     * 
     * For example: 192.168.1.1-192.168.1.5 requires multiple CIDRs:
     * - 192.168.1.1/32
     * - 192.168.1.2/31
     * - 192.168.1.4/31
     */
    static String[] toCIDRs(Segment segment) {
        // This is an advanced algorithm - mentioned for discussion
        // Not required for basic implementation
        throw new UnsupportedOperationException("CIDR conversion not implemented");
    }
}

