package com.wwb.leetcode.other.anthropic.findmedian;

public class PivotCountResponse {
    public long less;
    public long equal;
    public long greater;

    public PivotCountResponse(long less, long equal, long greater) {
        this.less = less;
        this.equal = equal;
        this.greater = greater;
    }

    public void add(PivotCountResponse other) {
        this.less += other.less;
        this.equal += other.equal;
        this.greater += other.greater;
    }
}
