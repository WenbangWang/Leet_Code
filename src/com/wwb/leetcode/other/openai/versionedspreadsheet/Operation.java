package com.wwb.leetcode.other.openai.versionedspreadsheet;

import java.util.Arrays;

/**
 * Command pattern for undo/redo operations
 * 
 * Represents a single spreadsheet operation (SET or SUM) with enough information
 * to undo and redo it.
 * 
 * Used in Phase 4 to maintain operation history per user.
 */
public class Operation {
    public enum Type { SET, SUM }
    
    private final String userId;
    private final Type type;
    private final int row;
    private final char col;
    private final long timestamp;
    
    // For SET operations
    private final Integer oldValue;
    private final Integer newValue;
    
    // For SUM operations
    private final String[] oldFormula;
    private final String[] newFormula;
    
    /**
     * Private constructor - use factory methods
     */
    private Operation(String userId, Type type, int row, char col, long timestamp,
                      Integer oldValue, Integer newValue,
                      String[] oldFormula, String[] newFormula) {
        this.userId = userId;
        this.type = type;
        this.row = row;
        this.col = col;
        this.timestamp = timestamp;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.oldFormula = oldFormula;
        this.newFormula = newFormula;
    }
    
    /**
     * Creates a SET operation
     * 
     * @param userId User who performed the operation
     * @param row Row number
     * @param col Column letter
     * @param timestamp Operation timestamp
     * @param oldValue Previous value
     * @param newValue New value
     * @return Operation instance
     */
    public static Operation createSet(String userId, int row, char col, long timestamp,
                                      int oldValue, int newValue) {
        return new Operation(userId, Type.SET, row, col, timestamp,
                           oldValue, newValue, null, null);
    }
    
    /**
     * Creates a SUM operation
     * 
     * @param userId User who performed the operation
     * @param row Row number
     * @param col Column letter
     * @param timestamp Operation timestamp
     * @param oldFormula Previous formula (null if none)
     * @param newFormula New formula
     * @return Operation instance
     */
    public static Operation createSum(String userId, int row, char col, long timestamp,
                                      String[] oldFormula, String[] newFormula) {
        return new Operation(userId, Type.SUM, row, col, timestamp,
                           null, null, oldFormula, newFormula);
    }
    
    // Getters
    public String getUserId() { return userId; }
    public Type getType() { return type; }
    public int getRow() { return row; }
    public char getCol() { return col; }
    public long getTimestamp() { return timestamp; }
    public Integer getOldValue() { return oldValue; }
    public Integer getNewValue() { return newValue; }
    public String[] getOldFormula() { return oldFormula; }
    public String[] getNewFormula() { return newFormula; }
    
    @Override
    public String toString() {
        if (type == Type.SET) {
            return String.format("SET %c%d: %d → %d by %s at %d",
                col, row, oldValue, newValue, userId, timestamp);
        } else {
            return String.format("SUM %c%d: %s → %s by %s at %d",
                col, row,
                oldFormula == null ? "none" : Arrays.toString(oldFormula),
                Arrays.toString(newFormula),
                userId, timestamp);
        }
    }
}

