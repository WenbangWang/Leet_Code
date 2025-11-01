package com.wwb.leetcode.other.anthropic.stacktrace;

public class ActiveFrame {
    String name;
    int consecutiveCount;
    boolean started;

    ActiveFrame(String name) {
        this.name = name;
        this.consecutiveCount = 1;
        this.started = false;
    }
}
