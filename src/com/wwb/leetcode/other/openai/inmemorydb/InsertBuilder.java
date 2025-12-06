package com.wwb.leetcode.other.openai.inmemorydb;

import com.wwb.leetcode.other.openai.inmemorydb.core.Column;
import com.wwb.leetcode.other.openai.inmemorydb.core.Row;
import com.wwb.leetcode.other.openai.inmemorydb.core.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InsertBuilder {
    private final DatabaseEngine engine;
    private final String tableName;
    private final List<Object> values;

    InsertBuilder(DatabaseEngine engine, String tableName) {
        this.engine = engine;
        this.tableName = tableName;
        this.values = new ArrayList<>();
    }

    public InsertBuilder values(Object... vals) {
        values.addAll(Arrays.asList(vals));
        return this;
    }

    public void execute() {
        Table table = engine.getTable(tableName);
        List<Column> columns = table.getColumns();

        if (columns.size() != values.size()) {
            throw new IllegalArgumentException("Value count doesn't match column count");
        }

        Row row = new Row();
        for (int i = 0; i < columns.size(); i++) {
            Column col = columns.get(i);
            Object val = values.get(i);

            if (!isTypeCompatible(col.getType(), val)) {
                throw new IllegalArgumentException(
                    "Invalid type for column " + col.getName() + ": expected " + col.getType() + ", got " + val.getClass());
            }

            row.setValue(col.getName(), val);
        }
        table.insert(row);
    }

    private boolean isTypeCompatible(DataType type, Object val) {
        if (val == null) return true;
        return switch (type) {
            case INT     -> val instanceof Integer;
            case STRING  -> val instanceof String;
            case DOUBLE  -> val instanceof Double || val instanceof Float;
            case BOOLEAN -> val instanceof Boolean;
        };
    }
}
