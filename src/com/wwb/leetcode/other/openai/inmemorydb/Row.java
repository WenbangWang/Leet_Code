package com.wwb.leetcode.other.openai.inmemorydb;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Row {
    private final Map<String, Object> data;

    public Row() {
        this.data = new HashMap<>();
    }

    public void setValue(String column, Object value) {
        data.put(column, value);
    }

    public Object getValue(String column) {
        return data.get(column);
    }

    public Map<String, Object> getAll() {
        return Collections.unmodifiableMap(data);
    }
}
