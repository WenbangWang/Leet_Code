package com.wwb.leetcode.other.openai.inmemorydb;

import com.wwb.leetcode.other.openai.inmemorydb.core.Column;
import com.wwb.leetcode.other.openai.inmemorydb.core.Table;

import java.util.ArrayList;
import java.util.List;

public class CreateTableBuilder {
    private final DatabaseEngine engine;
    private final String name;
    private final List<Column> columns;

    CreateTableBuilder(DatabaseEngine engine, String name) {
        this.engine = engine;
        this.name = name;
        this.columns = new ArrayList<>();
    }

    public CreateTableBuilder column(String name, DataType type) {
        columns.add(new Column(name, type));
        return this;
    }

    public void build() {
        Table table = new Table(name, columns);
        engine.registerTable(table);
    }
}
