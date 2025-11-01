package com.wwb.leetcode.other.anthropic.stacktrace;

public class Event {
    String kind; // "start" or "end"
    double ts;
    String name;

    Event(String kind, double ts, String name) {
        this.kind = kind;
        this.ts = ts;
        this.name = name;
    }
}
