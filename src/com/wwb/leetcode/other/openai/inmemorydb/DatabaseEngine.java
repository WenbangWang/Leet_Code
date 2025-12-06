package com.wwb.leetcode.other.openai.inmemorydb;

import com.wwb.leetcode.other.openai.inmemorydb.core.Table;

import java.util.HashMap;
import java.util.Map;

public class DatabaseEngine {
    private final Map<String, Table> tables;

    public DatabaseEngine() {
        this.tables = new HashMap<>();
    }

    // --- Create table ---
    public CreateTableBuilder createTable(String name) {
        return new CreateTableBuilder(this, name);
    }

    // --- Insert ---
    public InsertBuilder insertInto(String tableName) {
        return new InsertBuilder(this, tableName);
    }

    // --- Select ---
    public SelectBuilder select(String... columns) {
        return new SelectBuilder(this, columns);
    }

    public DeleteBuilder delete(String tableName) {
        return new DeleteBuilder(this, tableName);
    }

    // internal methods
    void registerTable(Table table) {
        tables.put(table.getName(), table);
    }

    Table getTable(String name) {
        Table table = tables.get(name);
        if (table == null)
            throw new IllegalArgumentException("Table not found: " + name);
        return table;
    }
}
