package com.wwb.leetcode.other.openai.inmemorydb;

public class Demo {
    public static void main(String[] args) {
        DatabaseEngine db = new DatabaseEngine();

        db.createTable("users")
            .column("id", DataType.INT)
            .column("name", DataType.STRING)
            .column("age", DataType.INT)
            .build();

        db.insertInto("users").values(3, "Charlie", 30).execute();
        db.insertInto("users").values(1, "Alice", 25).execute();
        db.insertInto("users").values(2, "Bob", 28).execute();

        System.out.println("=== All Users ===");
        db.select("*").from("users").execute().print();

        System.out.println("\n=== WHERE id = 2 ===");
        db.select("*")
            .from("users")
            .where(Condition.eq("id", 2))
            .execute()
            .print();

        System.out.println("\n=== WHERE id > 1 AND name != 'Charlie' ===");
        db.select("*")
            .from("users")
            .where(Condition.gt("id", 1).and(Condition.neq("name", "Charlie")))
            .execute()
            .print();

        System.out.println("=== Order by name ASC ===");
        db.select("*")
            .from("users")
            .orderBy("name", Order.ASC)
            .execute()
            .print();

        System.out.println("\n=== Order by age DESC, then name ASC ===");
        db.select("*")
            .from("users")
            .orderBy("age", Order.DESC)
            .orderBy("name", Order.ASC)
            .execute()
            .print();
    }
}
