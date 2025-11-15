package com.wwb.leetcode.other.openai.inmemorydb;

import java.util.Comparator;

public enum DataType {
    INT {
        @Override
        public Comparator<Object> comparator() {
            return Comparator.comparingInt(o -> (Integer) o);
        }
    },
    STRING {
        @Override
        public Comparator<Object> comparator() {
            return Comparator.comparing(o -> (String) o, Comparator.nullsFirst(String::compareTo));
        }
    },
    DOUBLE {
        @Override
        public Comparator<Object> comparator() {
            return Comparator.comparingDouble(o -> (Double) o);
        }
    },
    BOOLEAN {
        @Override
        public Comparator<Object> comparator() {
            return Comparator.comparing(o -> (Boolean) o, Comparator.nullsFirst(Boolean::compareTo));
        }
    };

    public abstract Comparator<Object> comparator();
}
