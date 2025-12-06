package com.wwb.leetcode.other.openai.ipaddressiterator;

import java.util.Iterator;

public class IPForwardIterator implements Iterator<String> {
    private long start;
    private long end;

    public IPForwardIterator(String startIp, String endId) {
        this.start = IpHelper.ipToLong(startIp);
        this.end = IpHelper.ipToLong(endId);
    }

    @Override
    public boolean hasNext() {
        return start < end;
    }

    @Override
    public String next() {
        long next = this.start;
        this.start++;

        return IpHelper.longToIp(next);
    }
}
