package com.wwb.leetcode.other.openai.inmemorydb.core;

import com.wwb.leetcode.other.openai.inmemorydb.DataType;

public class Column {
    private final String name;
    private final DataType type;

    public Column(String name, DataType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public DataType getType() { return type; }
}
