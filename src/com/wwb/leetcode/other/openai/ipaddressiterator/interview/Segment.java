package com.wwb.leetcode.other.openai.ipaddressiterator.interview;

import com.wwb.leetcode.other.openai.ipaddressiterator.IpHelper;

/**
 * Represents a contiguous range of IP addresses [start, end] inclusive.
 * Used for memory-efficient storage of IP ranges.
 */
class Segment implements Comparable<Segment> {
    final long start;  // inclusive
    final long end;    // inclusive

    Segment(long start, long end) {
        if (start > end) {
            throw new IllegalArgumentException("Start must be <= end");
        }
        this.start = start;
        this.end = end;
    }

    /**
     * Number of IPs in this segment
     */
    long size() {
        return end - start + 1;
    }

    /**
     * Check if this segment contains the given IP
     */
    boolean contains(long ip) {
        return ip >= start && ip <= end;
    }

    /**
     * Check if this segment overlaps with another
     */
    boolean overlaps(Segment other) {
        return this.start <= other.end && other.start <= this.end;
    }

    /**
     * Check if this segment can be merged with another (overlapping or adjacent)
     */
    boolean canMergeWith(Segment other) {
        // Adjacent segments can also be merged
        return this.start <= other.end + 1 && other.start <= this.end + 1;
    }

    /**
     * Merge this segment with another (assumes they overlap or are adjacent)
     */
    Segment merge(Segment other) {
        return new Segment(
            Math.min(this.start, other.start),
            Math.max(this.end, other.end)
        );
    }

    @Override
    public int compareTo(Segment other) {
        int cmp = Long.compare(this.start, other.start);
        return cmp != 0 ? cmp : Long.compare(this.end, other.end);
    }

    @Override
    public String toString() {
        return String.format("[%s - %s] (%d IPs)",
            IpHelper.longToIp(start),
            IpHelper.longToIp(end),
            size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Segment)) return false;
        Segment segment = (Segment) o;
        return start == segment.start && end == segment.end;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(start) * 31 + Long.hashCode(end);
    }
}

