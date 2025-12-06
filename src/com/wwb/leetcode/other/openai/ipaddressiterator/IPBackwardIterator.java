package com.wwb.leetcode.other.openai.ipaddressiterator;

import java.util.Iterator;

public class IPBackwardIterator implements Iterator<String> {
    private long start;
    private long end;

    public IPBackwardIterator(String startIp, String endId) {
        this.start = IpHelper.ipToLong(startIp);
        this.end = IpHelper.ipToLong(endId);
    }

    @Override
    public boolean hasNext() {
        return this.start > this.end;
    }

    @Override
    public String next() {
        long pre = this.start;
        this.start--;

        return IpHelper.longToIp(pre);
    }
}
