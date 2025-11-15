package com.wwb.leetcode.other.openai.inmemorydb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

public class SelectBuilder {
    private final DatabaseEngine engine;
    private final String[] columns;
    private final List<OrderClause> orderClauses;
    private String fromTable;
    private Condition condition;

    SelectBuilder(DatabaseEngine engine, String... columns) {
        this.engine = engine;
        this.columns = columns;
        this.orderClauses = new ArrayList<>();
    }

    public SelectBuilder from(String table) {
        this.fromTable = table;
        return this;
    }

    public SelectBuilder where(Condition condition) {
        this.condition = condition;
        return this;
    }

    public SelectBuilder orderBy(String column, Order order) {
        orderClauses.add(new OrderClause(engine.getTable(fromTable).getColumn(column), order));
        return this;
    }

    public ResultSet execute() {
        Table table = engine.getTable(fromTable);
        List<Row> rows = new ArrayList<>(table.getRows());

        // 1️⃣ Filter rows
        filter(rows);

        // 2️⃣ Sort rows
        sort(rows);

        // 3️⃣ Projection
        List<Map<String, Object>> result = new ArrayList<>();
        for (Row row : rows) {
            Map<String, Object> record = new LinkedHashMap<>();
            if (columns.length == 1 && columns[0].equals("*")) {
                record.putAll(row.getAll());
            } else {
                for (String col : columns) {
                    record.put(col, row.getValue(col));
                }
            }
            result.add(record);
        }

        return new ResultSet(result);
    }

    private void sort(List<Row> rows) {
        if (!orderClauses.isEmpty()) {
            rows.sort((r1, r2) -> {
                for (OrderClause oc : orderClauses) {
                    Object v1 = r1.getValue(oc.column().getName());
                    Object v2 = r2.getValue(oc.column().getName());
                    DataType type = oc.column().getType();
                    Comparator<Object> cmp = type.comparator();

                    int result = cmp.compare(v1, v2);

                    if (result == 0) {
                        continue;
                    }

                    return oc.order() == Order.ASC ? result : -result;
                }
                return 0;
            });
        }
    }

    private void filter(List<Row> rows) {
        if (condition != null) {
            rows.removeIf(row -> !condition.test(row));
        }
    }

    private static class OrderClause {
        private final Column column;
        private final Order order;

        OrderClause(Column column, Order order) {
            this.column = column;
            this.order = order;
        }

        public Column column() { return column; }
        public Order order() { return order; }
    }
}
