package com.wwb.leetcode.other.openai.versionedspreadsheet;

import java.util.HashMap;
import java.util.Map;

/**
 * Phase 1: Basic Spreadsheet with Lazy Evaluation
 * 
 * Problem: Implement a spreadsheet that stores values and formulas.
 * Formulas are evaluated lazily on get() calls.
 * 
 * API:
 * - set(row, col, value): Set a direct value
 * - setFormula(row, col, references): Set a formula (e.g., ["A1", "A2", "A1:B2"])
 * - get(row, col): Get value (evaluates formula if needed)
 * 
 * Key Concepts:
 * - Cell storage (2D array)
 * - Formula parsing (single cells and ranges)
 * - Weighted dependencies (if A1 appears 3x in formula, weight=3)
 * - Recursive lazy evaluation
 * 
 * Time Complexity:
 * - set(): O(1)
 * - setFormula(): O(F) where F = formula size
 * - get(): O(F × D) where D = formula depth
 * 
 * Space: O(H × W) for grid
 */
public class Phase1Spreadsheet {
    // ========================================
    // INNER CLASSES
    // ========================================
    
    private static class SimpleCell {
        int value;
        Map<String, Integer> formula;  // "A1" -> count (how many times referenced)
        
        SimpleCell() {
            value = 0;
            formula = new HashMap<>();
        }
    }
    
    // ========================================
    // FIELDS
    // ========================================
    
    private SimpleCell[][] sheet;
    private int width;
    private int height;
    
    // ========================================
    // CONSTRUCTOR
    // ========================================
    
    /**
     * Creates a spreadsheet with given dimensions
     * 
     * @param height Number of rows (1-indexed)
     * @param width Last column letter ('A' to width)
     */
    public Phase1Spreadsheet(int height, char width) {
        this.height = height;
        this.width = width - 'A' + 1;
        this.sheet = new SimpleCell[height + 1][this.width + 1];
        
        for (int i = 1; i <= height; i++) {
            for (int j = 1; j <= this.width; j++) {
                sheet[i][j] = new SimpleCell();
            }
        }
    }
    
    // ========================================
    // PUBLIC API METHODS
    // ========================================
    
    /**
     * Sets a direct value in a cell (clears any formula)
     * 
     * Time: O(1)
     * 
     * @param row Row number (1-indexed)
     * @param col Column letter
     * @param value Value to set
     * @throws IllegalArgumentException if row or column out of bounds
     */
    public void set(int row, char col, int value) {
        validateBounds(row, col);
        SimpleCell cell = sheet[row][col - 'A' + 1];
        cell.value = value;
        cell.formula.clear();  // Clear any previous formula
    }
    
    /**
     * Sets a formula in a cell
     * 
     * Supports:
     * - Single cells: "A1"
     * - Ranges: "A1:B2" (expands to A1, A2, B1, B2)
     * - Multiple references: ["A1", "A1"] means A1 * 2 (weight=2)
     * 
     * Time: O(F) where F = total cells in formula
     * 
     * @param row Row number (1-indexed)
     * @param col Column letter
     * @param references Array of cell references
     * @throws IllegalArgumentException if row/column out of bounds,
     *         references is null, or contains invalid cell references
     */
    public void setFormula(int row, char col, String[] references) {
        validateBounds(row, col);
        if (references == null) {
            throw new IllegalArgumentException("References array cannot be null");
        }
        SimpleCell cell = sheet[row][col - 'A' + 1];
        cell.formula = parseFormula(references);
        // Note: value is NOT calculated here (lazy evaluation)
    }
    
    /**
     * Gets the value of a cell
     * 
     * If cell has a formula, evaluates it recursively.
     * Otherwise returns the direct value.
     * 
     * Time: O(F × D) where D = max formula depth
     * 
     * @param row Row number (1-indexed)
     * @param col Column letter
     * @return Cell value
     * @throws IllegalArgumentException if row or column out of bounds
     */
    public int get(int row, char col) {
        validateBounds(row, col);
        SimpleCell cell = sheet[row][col - 'A' + 1];
        
        if (cell.formula.isEmpty()) {
            return cell.value;
        }
        
        // Lazy evaluation: compute on demand
        return evaluate(cell.formula);
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
        if (row < 1 || row > height) {
            throw new IllegalArgumentException(
                String.format("Row %d out of bounds [1, %d]", row, height));
        }
        
        int colIndex = col - 'A' + 1;
        if (colIndex < 1 || colIndex > width) {
            char maxCol = (char)('A' + width - 1);
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
        
        // Check format: must start with letter(s), followed by digits
        if (!ref.matches("^[A-Z]+\\d+$")) {
            throw new IllegalArgumentException(
                String.format("Invalid cell reference format: '%s' (expected format: A1, B2, etc.)", ref));
        }
        
        // Extract and validate row/col
        int i = 0;
        while (i < ref.length() && Character.isLetter(ref.charAt(i))) {
            i++;
        }
        
        if (i == 0 || i == ref.length()) {
            throw new IllegalArgumentException(
                String.format("Invalid cell reference format: '%s'", ref));
        }
        
        char col = ref.charAt(0);
        int row;
        try {
            row = Integer.parseInt(ref.substring(i));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                String.format("Invalid row number in reference: '%s'", ref));
        }
        
        // Validate bounds
        validateBounds(row, col);
    }
    
    /**
     * Validates a range format
     * 
     * @param parts Range parts from split (e.g., ["A1", "B2"])
     * @throws IllegalArgumentException if invalid range
     */
    private void validateRange(String[] parts) {
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                "Invalid range format (expected 'A1:B2')");
        }
        
        validateCellReference(parts[0]);
        validateCellReference(parts[1]);
        
        int[] start = parseCell(parts[0]);
        int[] end = parseCell(parts[1]);
        
        if (start[0] > end[0] || start[1] > end[1]) {
            throw new IllegalArgumentException(
                String.format("Invalid range '%s:%s': start must be <= end", parts[0], parts[1]));
        }
    }
    
    // ========================================
    // PRIVATE HELPER METHODS - Parsing
    // ========================================
    
    /**
     * Parses formula references into weighted dependencies
     * 
     * Expands ranges and counts occurrences.
     * Example: ["A1", "A1:A2", "A1"] -> {"A1": 3, "A2": 1}
     * 
     * @param references Array of cell references
     * @return Map of cell reference -> count
     */
    private Map<String, Integer> parseFormula(String[] references) {
        Map<String, Integer> formula = new HashMap<>();
        
        for (String ref : references) {
            if (ref.contains(":")) {
                // Range: "A1:B2"
                String[] parts = ref.split(":");
                validateRange(parts);
                int[] start = parseCell(parts[0]);
                int[] end = parseCell(parts[1]);
                
                for (int r = start[0]; r <= end[0]; r++) {
                    for (int c = start[1]; c <= end[1]; c++) {
                        String cellRef = String.format("%c%d", (char)('A' + c), r);
                        formula.put(cellRef, formula.getOrDefault(cellRef, 0) + 1);
                    }
                }
            } else {
                // Single cell: "A1"
                validateCellReference(ref);
                formula.put(ref, formula.getOrDefault(ref, 0) + 1);
            }
        }
        
        return formula;
    }
    
    /**
     * Parses a cell reference string to [row, col]
     * 
     * Example: "A1" -> [1, 0], "B3" -> [3, 1]
     * 
     * @param cellRef Cell reference like "A1"
     * @return Array [row (1-indexed), col (0-indexed)]
     */
    private int[] parseCell(String cellRef) {
        char col = cellRef.charAt(0);
        int row = Integer.parseInt(cellRef.substring(1));
        return new int[]{row, col - 'A'};
    }
    
    // ========================================
    // PRIVATE HELPER METHODS - Computation
    // ========================================
    
    /**
     * Evaluates a formula by summing weighted cell values
     * 
     * Recursively calls get() for each referenced cell.
     * 
     * @param formula Map of cell reference -> weight
     * @return Computed sum
     */
    private int evaluate(Map<String, Integer> formula) {
        int sum = 0;
        
        for (Map.Entry<String, Integer> entry : formula.entrySet()) {
            String cellRef = entry.getKey();
            int weight = entry.getValue();
            
            int[] pos = parseCell(cellRef);
            int cellValue = get(pos[0], (char)('A' + pos[1]));  // Recursive!
            
            sum += weight * cellValue;
        }
        
        return sum;
    }
    
    // ========================================
    // TEST METHODS
    // ========================================
    
    /**
     * Test cases for Phase 1
     */
    public static void main(String[] args) {
        Phase1Spreadsheet sheet = new Phase1Spreadsheet(5, 'E');
        
        // Test 1: Basic set/get
        sheet.set(1, 'A', 10);
        assert sheet.get(1, 'A') == 10 : "Test 1 failed";
        System.out.println("✅ Test 1 passed: Basic set/get");
        
        // Test 2: Simple formula
        sheet.set(1, 'A', 10);
        sheet.set(2, 'A', 20);
        sheet.setFormula(3, 'A', new String[]{"A1", "A2"});
        assert sheet.get(3, 'A') == 30 : "Test 2 failed";
        System.out.println("✅ Test 2 passed: Simple formula");
        
        // Test 3: Range formula
        sheet.set(1, 'A', 5);
        sheet.set(1, 'B', 10);
        sheet.set(2, 'A', 15);
        sheet.set(2, 'B', 20);
        sheet.setFormula(3, 'A', new String[]{"A1:B2"});
        assert sheet.get(3, 'A') == 50 : "Test 3 failed";
        System.out.println("✅ Test 3 passed: Range formula");
        
        // Test 4: Multiple references (weight)
        sheet.set(1, 'A', 10);
        sheet.setFormula(2, 'A', new String[]{"A1", "A1", "A1"});
        assert sheet.get(2, 'A') == 30 : "Test 4 failed";
        System.out.println("✅ Test 4 passed: Weighted references");
        
        // Test 5: Overwrite formula with value
        sheet.setFormula(2, 'A', new String[]{"A1", "A1"});
        sheet.set(2, 'A', 100);
        assert sheet.get(2, 'A') == 100 : "Test 5 failed";
        System.out.println("✅ Test 5 passed: Overwrite formula");
        
        // Test 6: Out of bounds row (high)
        try {
            sheet.set(1000, 'A', 10);
            assert false : "Test 6 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("out of bounds") : "Test 6 failed (wrong message)";
        }
        System.out.println("✅ Test 6 passed: Out of bounds row (high)");
        
        // Test 7: Out of bounds row (low)
        try {
            sheet.set(0, 'A', 10);
            assert false : "Test 7 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("out of bounds") : "Test 7 failed (wrong message)";
        }
        System.out.println("✅ Test 7 passed: Out of bounds row (low)");
        
        // Test 8: Out of bounds column
        try {
            sheet.set(1, 'Z', 10);  // Sheet is only A-E
            assert false : "Test 8 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("out of bounds") : "Test 8 failed (wrong message)";
        }
        System.out.println("✅ Test 8 passed: Out of bounds column");
        
        // Test 9: Null references array
        try {
            sheet.setFormula(1, 'A', null);
            assert false : "Test 9 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("cannot be null") : "Test 9 failed (wrong message)";
        }
        System.out.println("✅ Test 9 passed: Null references");
        
        // Test 10: Invalid cell reference format
        try {
            sheet.setFormula(1, 'A', new String[]{"1A"});
            assert false : "Test 10 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Invalid cell reference format") : "Test 10 failed (wrong message)";
        }
        System.out.println("✅ Test 10 passed: Invalid cell reference format");
        
        // Test 11: Cell reference out of bounds
        try {
            sheet.setFormula(1, 'A', new String[]{"Z999"});
            assert false : "Test 11 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("out of bounds") : "Test 11 failed (wrong message)";
        }
        System.out.println("✅ Test 11 passed: Cell reference out of bounds");
        
        // Test 12: Inverted range
        try {
            sheet.setFormula(1, 'A', new String[]{"B2:A1"});
            assert false : "Test 12 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("start must be <= end") : "Test 12 failed (wrong message)";
        }
        System.out.println("✅ Test 12 passed: Inverted range");
        
        // Test 13: Empty cell reference
        try {
            sheet.setFormula(1, 'A', new String[]{""});
            assert false : "Test 13 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("cannot be null or empty") : "Test 13 failed (wrong message)";
        }
        System.out.println("✅ Test 13 passed: Empty cell reference");
        
        System.out.println("\n✅ Phase 1: All 13 tests passed (5 functional + 8 validation)!");
    }
}
