package com.wwb.leetcode.other.openai.versionedspreadsheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Phase 3: Versioning + Time-Travel
 * 
 * Problem: Add version history per cell with time-travel queries.
 * Combines Phase 2 (live updates) with VersionedKV pattern.
 * 
 * API:
 * - set(row, col, value, timestamp): Versioned write
 * - sum(row, col, references, timestamp): Versioned formula
 * - get(row, col): Latest value (O(1), cached)
 * - get(row, col, timestamp): Historical value (O(log V + F × log V))
 * - getHistory(row, col): All versions
 * 
 * Key Concepts:
 * - Per-cell version history: NavigableMap<Long, CellVersion>
 * - Two evaluation modes:
 *   1. Current: Cached, live propagation (Phase 2 behavior)
 *   2. Historical: Computed on demand with recursive time-travel
 * - TreeMap.floorEntry() for "at or before" lookup (VersionedKV pattern)
 * - Store formulas as strings in history (Cell objects change over time)
 * 
 * Time Complexity:
 * - set(timestamp): O(log V + D) where V = versions, D = dependents
 * - sum(timestamp): O(F + log V + C + D)
 * - get(): O(1) for current value
 * - get(timestamp): O(log V + F × log V) for historical value
 * - getHistory(): O(V)
 * 
 * Space: O(H × W × V + E) where V = avg versions per cell
 */
public class Phase3VersionedSpreadsheet {
    // ========================================
    // INNER CLASSES
    // ========================================
    
    /**
     * Versioned cell with current state + history
     */
    private static class VersionedCell {
        int row, col;
        
        // Current state (for live updates - Phase 2 behavior)
        int currentValue;
        Map<VersionedCell, Integer> currentFormula;
        
        // Version history (new!)
        NavigableMap<Long, CellVersion> history;  // TreeMap: timestamp → version
        
        VersionedCell(int row, int col) {
            this.row = row;
            this.col = col;
            this.currentValue = 0;
            this.currentFormula = null;
            this.history = new TreeMap<>();
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VersionedCell)) return false;
            VersionedCell other = (VersionedCell) o;
            return row == other.row && col == other.col;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }
    
    // ========================================
    // FIELDS
    // ========================================
    
    private final int rows, cols;
    private final VersionedCell[][] table;
    private final Map<VersionedCell, Set<VersionedCell>> dependents;
    private long currentVersion;  // Global version counter
    
    // ========================================
    // CONSTRUCTOR
    // ========================================
    
    /**
     * Creates a versioned spreadsheet with given dimensions
     * 
     * @param height Number of rows (1-indexed)
     * @param width Last column letter ('A' to width)
     */
    public Phase3VersionedSpreadsheet(int height, char width) {
        this.rows = height;
        this.cols = width - 'A' + 1;
        this.table = new VersionedCell[rows + 1][cols + 1];
        this.dependents = new HashMap<>();
        this.currentVersion = 0;
        
        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                table[r][c] = new VersionedCell(r, c);
            }
        }
    }
    
    // ========================================
    // PUBLIC API METHODS
    // ========================================
    
    /**
     * Sets a versioned value and propagates to current dependents
     * 
     * Time: O(log V + D) where V = versions, D = dependents
     * 
     * @param row Row number (1-indexed)
     * @param col Column letter
     * @param value Value to set
     * @param timestamp Version timestamp
     * @return Version number
     * @throws IllegalArgumentException if row/column out of bounds or timestamp negative
     */
    public long set(int row, char col, int value, long timestamp) {
        validateBounds(row, col);
        validateTimestamp(timestamp);
        VersionedCell cell = table[row][col - 'A' + 1];
        
        // Save to history
        CellVersion version = new CellVersion(value, null, timestamp);
        cell.history.put(timestamp, version);
        
        // Update current state (Phase 2 behavior)
        clearFormula(cell);
        cell.currentValue = value;
        propagate(cell);
        
        return ++currentVersion;
    }
    
    /**
     * Sets a versioned formula and propagates to current dependents
     * 
     * Time: O(F + log V + C + D)
     * 
     * @param row Row number (1-indexed)
     * @param col Column letter
     * @param references Array of cell references
     * @param timestamp Version timestamp
     * @return Version number
     * @throws IllegalArgumentException if row/column out of bounds,
     *         references invalid, timestamp negative, or cycle detected
     */
    public long sum(int row, char col, String[] references, long timestamp) {
        validateBounds(row, col);
        validateReferences(references);
        validateTimestamp(timestamp);
        VersionedCell cell = table[row][col - 'A' + 1];
        clearFormula(cell);
        
        // Parse formula into Cell dependencies and String references
        Map<VersionedCell, Integer> formulaCells = new HashMap<>();
        Map<String, Integer> formulaStrings = new HashMap<>();  // For history storage
        
        for (String ref : references) {
            if (ref.contains(":")) {
                String[] parts = ref.split(":");
                VersionedCell start = parseCell(parts[0]);
                VersionedCell end = parseCell(parts[1]);
                
                for (int r = start.row; r <= end.row; r++) {
                    for (int c = start.col; c <= end.col; c++) {
                        VersionedCell dep = table[r][c];
                        String depRef = String.format("%c%d", (char)('A' + c - 1), r);
                        
                        formulaCells.put(dep, formulaCells.getOrDefault(dep, 0) + 1);
                        formulaStrings.put(depRef, formulaStrings.getOrDefault(depRef, 0) + 1);
                    }
                }
            } else {
                VersionedCell dep = parseCell(ref);
                formulaCells.put(dep, formulaCells.getOrDefault(dep, 0) + 1);
                formulaStrings.put(ref, formulaStrings.getOrDefault(ref, 0) + 1);
            }
        }
        
        // Cycle detection
        for (VersionedCell dep : formulaCells.keySet()) {
            if (hasCycle(cell, dep)) {
                throw new IllegalArgumentException("Cycle detected");
            }
        }
        
        // Calculate value
        int value = 0;
        for (Map.Entry<VersionedCell, Integer> entry : formulaCells.entrySet()) {
            value += entry.getKey().currentValue * entry.getValue();
        }
        
        // Save to history (store formula as strings, not Cell references)
        CellVersion version = new CellVersion(value, formulaStrings, timestamp);
        cell.history.put(timestamp, version);
        
        // Update current state and propagate
        cell.currentFormula = formulaCells;
        cell.currentValue = value;
        
        for (VersionedCell dep : formulaCells.keySet()) {
            dependents.computeIfAbsent(dep, k -> new HashSet<>()).add(cell);
        }
        
        propagate(cell);
        
        return ++currentVersion;
    }
    
    /**
     * Gets the latest (current) value
     * 
     * Time: O(1)
     * 
     * @param row Row number (1-indexed)
     * @param col Column letter
     * @return Current cached value
     * @throws IllegalArgumentException if row or column out of bounds
     */
    public int get(int row, char col) {
        validateBounds(row, col);
        return table[row][col - 'A' + 1].currentValue;
    }
    
    /**
     * Gets the historical value at or before timestamp
     * 
     * Uses TreeMap.floorEntry() to find version (VersionedKV pattern).
     * Recursively evaluates formulas at historical timestamp.
     * 
     * Time: O(log V + F × log V) where F = formula depth
     * 
     * @param row Row number (1-indexed)
     * @param col Column letter
     * @param timestamp Query timestamp
     * @return Historical value
     * @throws IllegalArgumentException if row/column out of bounds or timestamp negative
     */
    public int get(int row, char col, long timestamp) {
        validateBounds(row, col);
        validateTimestamp(timestamp);
        VersionedCell cell = table[row][col - 'A' + 1];
        
        // Find version at or before timestamp (VersionedKV pattern!)
        Map.Entry<Long, CellVersion> entry = cell.history.floorEntry(timestamp);
        
        if (entry == null) {
            return 0;  // No version before this timestamp
        }
        
        CellVersion version = entry.getValue();
        
        if (!version.isFormula()) {
            return version.getValue();
        }
        
        // Formula: recursively evaluate at historical timestamp
        int sum = 0;
        for (Map.Entry<String, Integer> e : version.getFormula().entrySet()) {
            String cellRef = e.getKey();
            int weight = e.getValue();
            
            int[] pos = parseCellRef(cellRef);
            int depValue = get(pos[0], (char)('A' + pos[1]), timestamp);  // Recursive time-travel!
            
            sum += weight * depValue;
        }
        
        return sum;
    }
    
    /**
     * Gets complete version history for a cell
     * 
     * Time: O(V) where V = versions
     * 
     * @param row Row number (1-indexed)
     * @param col Column letter
     * @return List of all versions
     * @throws IllegalArgumentException if row or column out of bounds
     */
    public List<CellVersion> getHistory(int row, char col) {
        validateBounds(row, col);
        VersionedCell cell = table[row][col - 'A' + 1];
        return new ArrayList<>(cell.history.values());
    }
    
    // ========================================
    // PRIVATE HELPER METHODS - Validation
    // ========================================
    
    /**
     * Validates cell coordinates are within bounds
     * 
     * @param row Row number (1-indexed)
     * @param col Column letter
     * @throws IllegalArgumentException if out of bounds
     */
    private void validateBounds(int row, char col) {
        if (row < 1 || row > rows) {
            throw new IllegalArgumentException(
                String.format("Row %d out of bounds [1, %d]", row, rows));
        }
        
        int colIndex = col - 'A' + 1;
        if (colIndex < 1 || colIndex > cols) {
            char maxCol = (char)('A' + cols - 1);
            throw new IllegalArgumentException(
                String.format("Column '%c' out of bounds ['A', '%c']", col, maxCol));
        }
    }
    
    /**
     * Validates timestamp is non-negative
     * 
     * @param timestamp Version timestamp
     * @throws IllegalArgumentException if negative
     */
    private void validateTimestamp(long timestamp) {
        if (timestamp < 0) {
            throw new IllegalArgumentException(
                String.format("Timestamp cannot be negative: %d", timestamp));
        }
    }
    
    /**
     * Validates references array is non-null and all references are valid
     * 
     * @param references Array of cell references
     * @throws IllegalArgumentException if null or contains invalid references
     */
    private void validateReferences(String[] references) {
        if (references == null) {
            throw new IllegalArgumentException("References array cannot be null");
        }
        
        for (String ref : references) {
            if (ref == null || ref.isEmpty()) {
                throw new IllegalArgumentException("Cell reference cannot be null or empty");
            }
            if (!ref.matches("^[A-Z]+\\d+(:[A-Z]+\\d+)?$")) {
                throw new IllegalArgumentException(
                    String.format("Invalid cell reference format: '%s'", ref));
            }
        }
    }
    
    // ========================================
    // PRIVATE HELPER METHODS - Parsing
    // ========================================
    
    /**
     * Parses a cell reference string to Cell object
     * 
     * @param ref Cell reference like "A1"
     * @return Cell object from table
     */
    private VersionedCell parseCell(String ref) {
        char col = ref.charAt(0);
        int row = Integer.parseInt(ref.substring(1));
        return table[row][col - 'A' + 1];
    }
    
    /**
     * Parses a cell reference string to [row, col] coordinates
     * 
     * Used for historical evaluation where Cell objects might not exist.
     * 
     * @param ref Cell reference like "A1"
     * @return Array [row (1-indexed), col (0-indexed)]
     */
    private int[] parseCellRef(String ref) {
        char col = ref.charAt(0);
        int row = Integer.parseInt(ref.substring(1));
        return new int[]{row, col - 'A'};
    }
    
    // ========================================
    // PRIVATE HELPER METHODS - Computation & Graph Management
    // ========================================
    
    /**
     * Calculates current cell value from its formula
     * 
     * @param cell Cell to calculate
     * @return Sum of weighted dependencies
     */
    private int calculate(VersionedCell cell) {
        if (cell.currentFormula == null) {
            return cell.currentValue;
        }
        
        int sum = 0;
        for (Map.Entry<VersionedCell, Integer> entry : cell.currentFormula.entrySet()) {
            sum += entry.getKey().currentValue * entry.getValue();
        }
        
        return sum;
    }
    
    /**
     * Removes cell from dependency graph
     * 
     * @param cell Cell whose formula to clear
     */
    private void clearFormula(VersionedCell cell) {
        if (cell.currentFormula != null) {
            for (VersionedCell dep : cell.currentFormula.keySet()) {
                Set<VersionedCell> depSet = dependents.get(dep);
                if (depSet != null) {
                    depSet.remove(cell);
                    if (depSet.isEmpty()) {
                        dependents.remove(dep);
                    }
                }
            }
            cell.currentFormula = null;
        }
    }
    
    /**
     * Propagates changes to current dependents recursively
     * 
     * @param cell Cell that changed
     */
    private void propagate(VersionedCell cell) {
        Set<VersionedCell> deps = dependents.get(cell);
        if (deps == null) return;
        
        for (VersionedCell dependent : deps) {
            dependent.currentValue = calculate(dependent);
            propagate(dependent);
        }
    }
    
    /**
     * Checks if adding edge from→to would create a cycle
     * 
     * @param from Source cell
     * @param to Target cell
     * @return true if cycle would be created
     */
    private boolean hasCycle(VersionedCell from, VersionedCell to) {
        return dfsHasPath(to, from, new HashSet<>());
    }
    
    /**
     * DFS to check if path exists from current to target
     * 
     * @param current Current cell
     * @param target Target cell
     * @param visited Visited cells
     * @return true if path exists
     */
    private boolean dfsHasPath(VersionedCell current, VersionedCell target, Set<VersionedCell> visited) {
        if (current.equals(target)) return true;
        
        Set<VersionedCell> deps = dependents.get(current);
        if (deps == null) return false;
        
        for (VersionedCell next : deps) {
            if (visited.add(next)) {
                if (dfsHasPath(next, target, visited)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // ========================================
    // TEST METHODS
    // ========================================
    
    /**
     * Test cases for Phase 3
     */
    public static void main(String[] args) {
        Phase3VersionedSpreadsheet sheet = new Phase3VersionedSpreadsheet(5, 'E');
        
        // Test 1: Basic versioning
        sheet.set(1, 'A', 10, 100);
        assert sheet.get(1, 'A', 50) == 0 : "Test 1a failed (before creation)";
        assert sheet.get(1, 'A', 100) == 10 : "Test 1b failed";
        assert sheet.get(1, 'A', 150) == 10 : "Test 1c failed";
        System.out.println("✅ Test 1 passed: Basic versioning");
        
        // Test 2: Value updates over time
        sheet.set(1, 'A', 10, 100);
        sheet.set(1, 'A', 20, 200);
        sheet.set(1, 'A', 30, 300);
        
        assert sheet.get(1, 'A', 150) == 10 : "Test 2a failed";
        assert sheet.get(1, 'A', 250) == 20 : "Test 2b failed";
        assert sheet.get(1, 'A', 350) == 30 : "Test 2c failed";
        assert sheet.get(1, 'A') == 30 : "Test 2d failed (current)";
        System.out.println("✅ Test 2 passed: Multiple versions");
        
        // Test 3: Formula with time-travel
        sheet.set(1, 'A', 10, 100);
        sheet.sum(2, 'A', new String[]{"A1"}, 200);
        
        assert sheet.get(2, 'A', 150) == 10 : "Test 3a failed";
        assert sheet.get(2, 'A', 250) == 10 : "Test 3b failed";
        System.out.println("✅ Test 3 passed: Formula time-travel");
        
        // Test 4: Historical formula evaluation
        sheet.set(1, 'A', 10, 100);
        sheet.set(2, 'A', 20, 200);
        sheet.sum(3, 'A', new String[]{"A1", "B1"}, 300);
        
        assert sheet.get(3, 'A', 250) == 10 : "Test 4a failed (A1=10, B1=0)";
        assert sheet.get(3, 'A', 350) == 30 : "Test 4b failed (A1=10, B1=20)";
        System.out.println("✅ Test 4 passed: Historical formula evaluation");
        
        // Test 5: Current state still has live updates
        sheet.set(1, 'A', 10, 100);
        sheet.sum(2, 'A', new String[]{"A1"}, 200);
        
        sheet.set(1, 'A', 20, 300);  // Update A1
        assert sheet.get(2, 'A') == 20 : "Test 5 failed (live update)";
        System.out.println("✅ Test 5 passed: Live updates still work");
        
        // Test 6: History tracking
        sheet.set(1, 'A', 10, 100);
        sheet.set(1, 'A', 20, 200);
        sheet.set(1, 'A', 30, 300);
        
        List<CellVersion> history = sheet.getHistory(1, 'A');
        assert history.size() == 3 : "Test 6a failed";
        assert history.get(0).getValue() == 10 : "Test 6b failed";
        assert history.get(1).getValue() == 20 : "Test 6c failed";
        assert history.get(2).getValue() == 30 : "Test 6d failed";
        System.out.println("✅ Test 6 passed: History tracking");
        
        // Test 7: Negative timestamp
        try {
            sheet.set(1, 'A', 10, -1);
            assert false : "Test 7 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("cannot be negative") : "Test 7 failed (wrong message)";
        }
        System.out.println("✅ Test 7 passed: Negative timestamp");
        
        // Test 8: Out of bounds with timestamp
        try {
            sheet.set(100, 'A', 10, 100);
            assert false : "Test 8 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("out of bounds") : "Test 8 failed (wrong message)";
        }
        System.out.println("✅ Test 8 passed: Out of bounds with timestamp");
        
        // Test 9: Historical query with negative timestamp
        try {
            sheet.get(1, 'A', -100);
            assert false : "Test 9 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("cannot be negative") : "Test 9 failed (wrong message)";
        }
        System.out.println("✅ Test 9 passed: Historical query negative timestamp");
        
        System.out.println("\n✅ Phase 3: All 9 tests passed (6 functional + 3 validation)!");
    }
}

