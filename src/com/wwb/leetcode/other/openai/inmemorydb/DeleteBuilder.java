package com.wwb.leetcode.other.openai.inmemorydb;

public class DeleteBuilder {
    private final DatabaseEngine engine;
    private Condition condition;
    private final String tableName;

    DeleteBuilder(DatabaseEngine engine, String tableName) {
        this.engine = engine;
        this.tableName = tableName;
    }

    public DeleteBuilder where(Condition condition) {
        this.condition = condition;
        return this;
    }

    public int execute() {
        Table table = engine.getTable(tableName);

        // need to check if condition exist or not
        return table.delete(this.condition);
    }
}
