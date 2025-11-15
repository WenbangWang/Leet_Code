package com.wwb.leetcode.other.openai.inmemorydb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Table {
    private final String name;
    private final List<Column> columns;
    private final List<Row> rows;

    public Table(String name, List<Column> columns) {
        this.name = name;
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<Column> getColumns() { return Collections.unmodifiableList(columns); }

    // optimize to use set instead of list to store columns
    public Column getColumn(String column) {
        return getColumns().stream()
            .filter(c -> c.getName().equals(column))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown column: " + column));
    }

    public void insert(Row row) {
        rows.add(row);
    }

    public int delete(Condition condition) {
        int before = rows.size();
        rows.removeIf(condition::test);
        return before - rows.size(); // return number of deleted rows
    }

    public List<Row> getRows() {
        return Collections.unmodifiableList(rows);
    }
}
