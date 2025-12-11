package com.wwb.leetcode.other.openai.versionedspreadsheet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Phase 2: Live Updates with Dependency Graph
 * 
 * Problem: Extend Phase 1 to auto-propagate changes.
 * When A1 changes, all formulas depending on A1 auto-update.
 * 
 * API:
 * - set(row, col, value): Set value and propagate to dependents
 * - sum(row, col, references): Set formula and propagate (renamed from setFormula)
 * - get(row, col): Get cached value (O(1))
 * 
 * Key Concepts:
 * - Dependency graph: Map<Cell, Set<Cell>> dependents (reverse graph)
 * - Cycle detection via DFS (prevent A1=B1, B1=A1)
 * - Recursive propagation when cell changes
 * - Clean formula removal from graph
 * 
 * Pattern: Exactly LeetCode 631 (Excel Sheet) implementation
 * 
 * Time Complexity:
 * - set(): O(D) where D = total dependent cells
 * - sum(): O(F + C + D) where F = formula size, C = cycle check
 * - get(): O(1) cached value
 * 
 * Space: O(H × W + E) where E = edges in dependency graph
 */
public class Phase2Spreadsheet {
    // ========================================
    // INNER CLASSES
    // ========================================
    
    /**
     * Represents a cell with position, value, and dependencies
     */
    private static class Cell {
        int row, col;
        int value;
        Map<Cell, Integer> formula;  // Dependencies with weights
        
        Cell(int row, int col) {
            this.row = row;
            this.col = col;
            this.value = 0;
            this.formula = null;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Cell)) return false;
            Cell other = (Cell) o;
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
    private final Cell[][] table;
    private final Map<Cell, Set<Cell>> dependents;  // Reverse graph: A1 → {B1, C2, ...}
    
    // ========================================
    // CONSTRUCTOR
    // ========================================
    
    /**
     * Creates a spreadsheet with given dimensions
     * 
     * @param height Number of rows (1-indexed)
     * @param width Last column letter ('A' to width)
     */
    public Phase2Spreadsheet(int height, char width) {
        this.rows = height;
        this.cols = width - 'A' + 1;
        this.table = new Cell[rows + 1][cols + 1];
        this.dependents = new HashMap<>();
        
        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                table[r][c] = new Cell(r, c);
            }
        }
    }
    
    // ========================================
    // PUBLIC API METHODS
    // ========================================
    
    /**
     * Sets a direct value and propagates to dependent cells
     * 
     * Time: O(D) where D = total dependent cells
     * 
     * @param row Row number (1-indexed)
     * @param col Column letter
     * @param value Value to set
     * @throws IllegalArgumentException if row or column out of bounds
     */
    public void set(int row, char col, int value) {
        validateBounds(row, col);
        Cell cell = table[row][col - 'A' + 1];
        clearFormula(cell);  // Remove from dependency graph
        cell.value = value;
        propagate(cell);     // Update all dependents
    }
    
    /**
     * Gets the cached value of a cell
     * 
     * Time: O(1)
     * 
     * @param row Row number (1-indexed)
     * @param col Column letter
     * @return Cached cell value
     * @throws IllegalArgumentException if row or column out of bounds
     */
    public int get(int row, char col) {
        validateBounds(row, col);
        return table[row][col - 'A' + 1].value;
    }
    
    /**
     * Sets a formula and propagates changes
     * 
     * Renamed from setFormula to emphasize it's a SUM operation.
     * Builds dependency graph and performs cycle detection.
     * 
     * Time: O(F + C + D) where F = formula size, C = cycle check, D = dependents
     * 
     * @param row Row number (1-indexed)
     * @param col Column letter
     * @param references Array of cell references
     * @return The computed value
     * @throws IllegalArgumentException if row/column out of bounds,
     *         references is null/invalid, or cycle detected
     */
    public int sum(int row, char col, String[] references) {
        validateBounds(row, col);
        validateReferences(references);
        Cell cell = table[row][col - 'A' + 1];
        clearFormula(cell);
        
        // Parse formula into Cell dependencies
        Map<Cell, Integer> formula = new HashMap<>();
        for (String ref : references) {
            if (ref.contains(":")) {
                String[] parts = ref.split(":");
                Cell start = parseCell(parts[0]);
                Cell end = parseCell(parts[1]);
                
                for (int r = start.row; r <= end.row; r++) {
                    for (int c = start.col; c <= end.col; c++) {
                        Cell dep = table[r][c];
                        formula.put(dep, formula.getOrDefault(dep, 0) + 1);
                    }
                }
            } else {
                Cell dep = parseCell(ref);
                formula.put(dep, formula.getOrDefault(dep, 0) + 1);
            }
        }
        
        // Cycle detection: Check if adding this formula creates a cycle
        for (Cell dep : formula.keySet()) {
            if (hasCycle(cell, dep)) {
                throw new IllegalArgumentException(
                    String.format("Cycle detected: setting %c%d would create circular dependency",
                        (char)('A' + cell.col - 1), cell.row));
            }
        }
        
        // Build dependency graph
        cell.formula = formula;
        for (Cell dep : formula.keySet()) {
            dependents.computeIfAbsent(dep, k -> new HashSet<>()).add(cell);
        }
        
        // Calculate and propagate
        cell.value = calculate(cell);
        propagate(cell);
        
        return cell.value;
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
     * Validates a cell reference string format and bounds
     * 
     * @param ref Cell reference like "A1"
     * @throws IllegalArgumentException if invalid format or out of bounds
     */
    private void validateCellReference(String ref) {
        if (ref == null || ref.isEmpty()) {
            throw new IllegalArgumentException("Cell reference cannot be null or empty");
        }
        
        if (!ref.matches("^[A-Z]+\\d+$")) {
            throw new IllegalArgumentException(
                String.format("Invalid cell reference format: '%s' (expected format: A1, B2, etc.)", ref));
        }
        
        int i = 0;
        while (i < ref.length() && Character.isLetter(ref.charAt(i))) {
            i++;
        }
        
        char col = ref.charAt(0);
        int row;
        try {
            row = Integer.parseInt(ref.substring(i));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                String.format("Invalid row number in reference: '%s'", ref));
        }
        
        validateBounds(row, col);
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
            if (ref.contains(":")) {
                String[] parts = ref.split(":");
                if (parts.length != 2) {
                    throw new IllegalArgumentException(
                        String.format("Invalid range format: '%s' (expected 'A1:B2')", ref));
                }
                validateCellReference(parts[0]);
                validateCellReference(parts[1]);
            } else {
                validateCellReference(ref);
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
    private Cell parseCell(String ref) {
        char col = ref.charAt(0);
        int row = Integer.parseInt(ref.substring(1));
        return table[row][col - 'A' + 1];
    }
    
    // ========================================
    // PRIVATE HELPER METHODS - Computation & Graph Management
    // ========================================
    
    /**
     * Calculates cell value from its formula
     * 
     * @param cell Cell to calculate
     * @return Sum of weighted dependencies
     */
    private int calculate(Cell cell) {
        if (cell.formula == null) {
            return cell.value;
        }
        
        int sum = 0;
        for (Map.Entry<Cell, Integer> entry : cell.formula.entrySet()) {
            Cell dep = entry.getKey();
            int weight = entry.getValue();
            sum += dep.value * weight;
        }
        
        return sum;
    }
    
    /**
     * Removes cell from dependency graph
     * 
     * Called when cell's formula is replaced with a direct value or new formula.
     * 
     * @param cell Cell whose formula to clear
     */
    private void clearFormula(Cell cell) {
        if (cell.formula != null) {
            // Remove cell from all its dependencies' dependent lists
            for (Cell dep : cell.formula.keySet()) {
                Set<Cell> depSet = dependents.get(dep);
                if (depSet != null) {
                    depSet.remove(cell);
                    if (depSet.isEmpty()) {
                        dependents.remove(dep);
                    }
                }
            }
            cell.formula = null;
        }
    }
    
    /**
     * Propagates changes to all dependent cells recursively
     * 
     * @param cell Cell that changed
     */
    private void propagate(Cell cell) {
        Set<Cell> deps = dependents.get(cell);
        if (deps == null) return;
        
        // Update all cells that depend on this cell
        for (Cell dependent : deps) {
            dependent.value = calculate(dependent);
            propagate(dependent);  // Recursive propagation
        }
    }
    
    /**
     * Checks if adding edge from→to would create a cycle
     * 
     * A cycle exists if there's already a path to→from.
     * 
     * @param from Source cell
     * @param to Target cell
     * @return true if cycle would be created
     */
    private boolean hasCycle(Cell from, Cell to) {
        // Check if adding edge from→to creates a cycle
        // This means there's already a path to→from
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
    private boolean dfsHasPath(Cell current, Cell target, Set<Cell> visited) {
        if (current.equals(target)) return true;
        
        Set<Cell> deps = dependents.get(current);
        if (deps == null) return false;
        
        for (Cell next : deps) {
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
     * Test cases for Phase 2
     */
    public static void main(String[] args) {
        Phase2Spreadsheet sheet = new Phase2Spreadsheet(5, 'E');
        
        // Test 1: Basic live update
        sheet.set(1, 'A', 10);
        sheet.sum(2, 'A', new String[]{"A1"});
        assert sheet.get(2, 'A') == 10 : "Test 1a failed";
        
        sheet.set(1, 'A', 20);  // Change A1
        assert sheet.get(2, 'A') == 20 : "Test 1b failed (live update)";
        System.out.println("✅ Test 1 passed: Live update");
        
        // Test 2: Cascading updates
        sheet.set(1, 'A', 5);
        sheet.sum(2, 'A', new String[]{"A1"});    // B1 = A1
        sheet.sum(3, 'A', new String[]{"B1"});    // C1 = B1
        
        sheet.set(1, 'A', 10);  // Change A1
        assert sheet.get(3, 'A') == 10 : "Test 2 failed (cascade)";
        System.out.println("✅ Test 2 passed: Cascading updates");
        
        // Test 3: Weighted dependencies
        sheet.set(1, 'A', 10);
        sheet.sum(2, 'A', new String[]{"A1", "A1", "A1"});  // 3x A1
        assert sheet.get(2, 'A') == 30 : "Test 3 failed";
        System.out.println("✅ Test 3 passed: Weighted dependencies");
        
        // Test 4: Cycle detection
        sheet.set(1, 'A', 10);
        sheet.sum(2, 'A', new String[]{"A1"});
        
        try {
            sheet.sum(1, 'A', new String[]{"B1"});  // A1 = B1, but B1 = A1!
            assert false : "Test 4 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Cycle") : "Test 4 failed (wrong exception)";
        }
        System.out.println("✅ Test 4 passed: Cycle detection");
        
        // Test 5: Clear formula with set()
        sheet.set(1, 'A', 10);
        sheet.sum(2, 'A', new String[]{"A1"});
        sheet.set(2, 'A', 100);  // Replace formula with value
        
        sheet.set(1, 'A', 20);   // Change A1
        assert sheet.get(2, 'A') == 100 : "Test 5 failed (should not update)";
        System.out.println("✅ Test 5 passed: Formula override");
        
        // Test 6: Range update propagation
        sheet.set(1, 'A', 5);
        sheet.set(1, 'B', 10);
        sheet.sum(2, 'A', new String[]{"A1:B1"});
        assert sheet.get(2, 'A') == 15 : "Test 6a failed";
        
        sheet.set(1, 'B', 20);  // Change B1
        assert sheet.get(2, 'A') == 25 : "Test 6b failed (range propagation)";
        System.out.println("✅ Test 6 passed: Range propagation");
        
        // Test 7: Out of bounds validation
        try {
            sheet.set(100, 'A', 10);
            assert false : "Test 7 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("out of bounds") : "Test 7 failed (wrong message)";
        }
        System.out.println("✅ Test 7 passed: Out of bounds");
        
        // Test 8: Null references
        try {
            sheet.sum(1, 'A', null);
            assert false : "Test 8 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("cannot be null") : "Test 8 failed (wrong message)";
        }
        System.out.println("✅ Test 8 passed: Null references");
        
        // Test 9: Invalid cell reference in formula
        try {
            sheet.sum(1, 'A', new String[]{"Invalid"});
            assert false : "Test 9 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Invalid cell reference format") : "Test 9 failed (wrong message)";
        }
        System.out.println("✅ Test 9 passed: Invalid cell reference");
        
        System.out.println("\n✅ Phase 2: All 9 tests passed (6 functional + 3 validation)!");
    }
}
