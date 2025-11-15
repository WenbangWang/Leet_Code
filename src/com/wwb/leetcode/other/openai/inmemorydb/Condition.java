package com.wwb.leetcode.other.openai.inmemorydb;

import java.util.Objects;

@FunctionalInterface
public interface Condition {
    boolean test(Row row);

    // Static factory methods for convenience
    static Condition eq(String column, Object value) {
        return row -> Objects.equals(row.getValue(column), value);
    }

    static Condition neq(String column, Object value) {
        return row -> !Objects.equals(row.getValue(column), value);
    }

    static Condition gt(String column, Comparable<?> value) {
        return row -> {
            Object cell = row.getValue(column);
            if (cell instanceof Comparable<?>) {
                return ((Comparable) cell).compareTo(value) > 0;
            }
            return false;
        };
    }

    static Condition lt(String column, Comparable<?> value) {
        return row -> {
            Object cell = row.getValue(column);
            if (cell instanceof Comparable<?>) {
                return ((Comparable) cell).compareTo(value) < 0;
            }
            return false;
        };
    }

    default Condition and(Condition other) {
        return row -> this.test(row) && other.test(row);
    }

    default Condition or(Condition other) {
        return row -> this.test(row) || other.test(row);
    }
}
