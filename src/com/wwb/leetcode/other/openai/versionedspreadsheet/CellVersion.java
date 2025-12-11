package com.wwb.leetcode.other.openai.versionedspreadsheet;

import java.util.Map;

/**
 * Represents a historical version of a cell
 * 
 * Used in Phase 3+ to store version history with time-travel capabilities.
 * Each version captures either a direct value or a formula with its computed result.
 */
public class CellVersion {
    private final int value;
    private final Map<String, Integer> formula;  // Stored as string references ("A1" -> count)
    private final long timestamp;
    private final boolean isFormula;
    
    /**
     * Creates a cell version
     * 
     * @param value The computed value at this version
     * @param formula The formula (if any) as string references with weights
     * @param timestamp The timestamp of this version
     */
    public CellVersion(int value, Map<String, Integer> formula, long timestamp) {
        this.value = value;
        this.formula = formula;
        this.timestamp = timestamp;
        this.isFormula = (formula != null && !formula.isEmpty());
    }
    
    public int getValue() {
        return value;
    }
    
    public Map<String, Integer> getFormula() {
        return formula;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public boolean isFormula() {
        return isFormula;
    }
    
    @Override
    public String toString() {
        if (isFormula) {
            return String.format("CellVersion{value=%d, formula=%s, ts=%d}",
                value, formula, timestamp);
        }
        return String.format("CellVersion{value=%d, ts=%d}", value, timestamp);
    }
}

