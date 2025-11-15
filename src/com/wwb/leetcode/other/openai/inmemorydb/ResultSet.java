package com.wwb.leetcode.other.openai.inmemorydb;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ResultSet {
    private final List<Map<String, Object>> records;

    public ResultSet(List<Map<String, Object>> records) {
        this.records = records;
    }

    public void print() {
        for (Map<String, Object> r : records) {
            System.out.println(r);
        }
    }

    public List<Map<String, Object>> getRecords() {
        return Collections.unmodifiableList(records);
    }
}
