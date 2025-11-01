package com.wwb.leetcode.other.anthropic.inmemorydb;
import java.util.HashMap;
import java.util.Map;

public class InMemoryDB1 {
    // key -> record
    private final Map<String, Record> records = new HashMap<>();

    /**
     * SET <timestamp> <key> <field> <value>
     */
    public void set(String timestamp, String key, String field, int value) {
        records.computeIfAbsent(key, k -> new Record()).set(field, value);
    }

    /**
     * COMPARE_AND_SET <timestamp> <key> <field> <expectedValue> <newValue>
     */
    public boolean compareAndSet(String timestamp, String key, String field, int expectedValue, int newValue) {
        Record rec = records.get(key);
        if (rec == null) {
            return false;
        }
        return rec.compareAndSet(field, expectedValue, newValue);
    }

    /**
     * COMPARE_AND_DELETE <timestamp> <key> <field> <expectedValue>
     */
    public boolean compareAndDelete(String timestamp, String key, String field, int expectedValue) {
        Record rec = records.get(key);
        if (rec == null) {
            return false;
        }
        return rec.compareAndDelete(field, expectedValue);
    }

    /**
     * GET <timestamp> <key> <field>
     */
    public String get(String timestamp, String key, String field) {
        Record rec = records.get(key);
        if (rec == null) {
            return "";
        }
        Integer val = rec.get(field);
        return val == null ? "" : String.valueOf(val);
    }

    // ================== Inner Record Class ==================
    private static class Record {
        private final Map<String, Integer> fields = new HashMap<>();

        void set(String field, int value) {
            fields.put(field, value);
        }

        boolean compareAndSet(String field, int expectedValue, int newValue) {
            Integer current = fields.get(field);
            if (current != null && current == expectedValue) {
                fields.put(field, newValue);
                return true;
            }
            return false;
        }

        boolean compareAndDelete(String field, int expectedValue) {
            Integer current = fields.get(field);
            if (current != null && current == expectedValue) {
                fields.remove(field);
                return true;
            }
            return false;
        }

        Integer get(String field) {
            return fields.get(field);
        }
    }

    // ================== Demo ==================
    public static void main(String[] args) {
        InMemoryDB1 db = new InMemoryDB1();

        db.set("1", "user1", "balance", 100);
        System.out.println(db.get("2", "user1", "balance")); // 100

        System.out.println(db.compareAndSet("3", "user1", "balance", 100, 200)); // true
        System.out.println(db.get("4", "user1", "balance")); // 200

        System.out.println(db.compareAndDelete("5", "user1", "balance", 200)); // true
        System.out.println(db.get("6", "user1", "balance")); // ""

        System.out.println(db.compareAndSet("7", "user1", "balance", 300, 400)); // false
    }
}
