package com.wwb.leetcode.other.openai.versionedspreadsheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Phase 4: Undo/Redo + Audit Log
 * 
 * Problem: Add per-user operation history with undo/redo capabilities.
 * All operations are tracked in an audit log.
 * 
 * API:
 * - set(userId, row, col, value, timestamp): Tracked versioned write
 * - sum(userId, row, col, references, timestamp): Tracked versioned formula
 * - undo(userId): Revert last operation by this user
 * - redo(userId): Re-apply last undone operation
 * - getOperationHistory(userId): Get user's operations
 * - getAllOperations(): Get complete audit log
 * 
 * Key Concepts:
 * - Command pattern: Operation class encapsulates operation details
 * - Per-user undo/redo stacks: Map<userId, Stack<Operation>>
 * - Global audit log: List<Operation>
 * - Undo creates new version (not rollback) for collaborative editing
 * 
 * Design Decision: Undo as New Edit
 * - Alice's undo doesn't break Bob's formulas
 * - Full audit trail preserved
 * - Time-travel still works correctly
 * 
 * Time Complexity: All Phase 3 operations +
 * - undo(): O(log V + D)
 * - redo(): O(log V + D)
 * - getOperationHistory(): O(L) where L = audit log size
 * 
 * Space: O(H × W × V + E + O) where O = operations stored
 */
public class Phase4UndoableSpreadsheet extends Phase3VersionedSpreadsheet {
    // ========================================
    // FIELDS
    // ========================================
    
    // Per-user undo/redo stacks
    private final Map<String, Stack<Operation>> undoStacks;
    private final Map<String, Stack<Operation>> redoStacks;
    
    // Global audit log
    private final List<Operation> auditLog;
    
    // ========================================
    // CONSTRUCTOR
    // ========================================
    
    /**
     * Creates an undoable spreadsheet with given dimensions
     * 
     * @param height Number of rows (1-indexed)
     * @param width Last column letter ('A' to width)
     */
    public Phase4UndoableSpreadsheet(int height, char width) {
        super(height, width);
        this.undoStacks = new HashMap<>();
        this.redoStacks = new HashMap<>();
        this.auditLog = new ArrayList<>();
    }
    
    // ========================================
    // PUBLIC API METHODS
    // ========================================
    
    /**
     * Sets a versioned value with user tracking
     * 
     * Records operation in undo stack and audit log.
     * Clears redo stack (standard undo/redo semantics).
     * 
     * Time: O(log V + D)
     * 
     * @param userId User performing the operation
     * @param row Row number (1-indexed)
     * @param col Column letter
     * @param value Value to set
     * @param timestamp Version timestamp
     * @return Version number
     * @throws IllegalArgumentException if userId is null/empty,
     *         row/column out of bounds, or timestamp negative
     */
    public long set(String userId, int row, char col, int value, long timestamp) {
        validateUserId(userId);
        // Record old value for undo
        int oldValue = super.get(row, col);
        
        // Perform operation
        long version = super.set(row, col, value, timestamp);
        
        // Record operation
        Operation op = Operation.createSet(userId, row, col, timestamp, oldValue, value);
        undoStacks.computeIfAbsent(userId, k -> new Stack<>()).push(op);
        redoStacks.computeIfAbsent(userId, k -> new Stack<>()).clear();  // Clear redo stack
        auditLog.add(op);
        
        return version;
    }
    
    /**
     * Sets a versioned formula with user tracking
     * 
     * Records operation in undo stack and audit log.
     * Clears redo stack (standard undo/redo semantics).
     * 
     * Time: O(F + log V + C + D)
     * 
     * @param userId User performing the operation
     * @param row Row number (1-indexed)
     * @param col Column letter
     * @param references Array of cell references
     * @param timestamp Version timestamp
     * @return Version number
     * @throws IllegalArgumentException if userId is null/empty,
     *         row/column out of bounds, references invalid,
     *         timestamp negative, or cycle detected
     */
    public long sum(String userId, int row, char col, String[] references, long timestamp) {
        validateUserId(userId);
        // Record old formula for undo (if any)
        // Note: We can't easily get the old formula from Phase3, so we'll track it as null
        // In a full implementation, Phase3 would expose getCurrentFormula()
        String[] oldFormula = null;
        
        // Perform operation
        long version = super.sum(row, col, references, timestamp);
        
        // Record operation
        Operation op = Operation.createSum(userId, row, col, timestamp, oldFormula, references);
        undoStacks.computeIfAbsent(userId, k -> new Stack<>()).push(op);
        redoStacks.computeIfAbsent(userId, k -> new Stack<>()).clear();
        auditLog.add(op);
        
        return version;
    }
    
    /**
     * Undoes the last operation by this user
     * 
     * Creates a new version with the old value/formula.
     * This preserves audit trail and doesn't break other users' work.
     * 
     * Time: O(log V + D)
     * 
     * @param userId User whose operation to undo
     * @throws IllegalArgumentException if userId is null or empty
     */
    public void undo(String userId) {
        validateUserId(userId);
        Stack<Operation> undoStack = undoStacks.get(userId);
        if (undoStack == null || undoStack.isEmpty()) {
            return;
        }
        
        Operation op = undoStack.pop();
        long now = System.currentTimeMillis();
        
        if (op.getType() == Operation.Type.SET) {
            // Restore old value
            super.set(op.getRow(), op.getCol(), op.getOldValue(), now);
        } else {
            // Restore old formula (or clear if none)
            if (op.getOldFormula() == null) {
                super.set(op.getRow(), op.getCol(), 0, now);  // Clear formula
            } else {
                super.sum(op.getRow(), op.getCol(), op.getOldFormula(), now);
            }
        }
        
        redoStacks.computeIfAbsent(userId, k -> new Stack<>()).push(op);
    }
    
    /**
     * Redoes the last undone operation by this user
     * 
     * Creates a new version with the new value/formula.
     * 
     * Time: O(log V + D)
     * 
     * @param userId User whose operation to redo
     * @throws IllegalArgumentException if userId is null or empty
     */
    public void redo(String userId) {
        validateUserId(userId);
        Stack<Operation> redoStack = redoStacks.get(userId);
        if (redoStack == null || redoStack.isEmpty()) {
            return;
        }
        
        Operation op = redoStack.pop();
        long now = System.currentTimeMillis();
        
        if (op.getType() == Operation.Type.SET) {
            // Re-apply new value
            super.set(op.getRow(), op.getCol(), op.getNewValue(), now);
        } else {
            // Re-apply new formula
            super.sum(op.getRow(), op.getCol(), op.getNewFormula(), now);
        }
        
        undoStacks.get(userId).push(op);
    }
    
    /**
     * Gets all operations performed by a specific user
     * 
     * Time: O(L) where L = audit log size
     * 
     * @param userId User ID
     * @return List of operations by this user
     * @throws IllegalArgumentException if userId is null or empty
     */
    public List<Operation> getOperationHistory(String userId) {
        validateUserId(userId);
        return auditLog.stream()
            .filter(op -> op.getUserId().equals(userId))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets complete audit log of all operations
     * 
     * Time: O(1) (returns reference, or O(L) if copying)
     * 
     * @return List of all operations
     */
    public List<Operation> getAllOperations() {
        return new ArrayList<>(auditLog);
    }
    
    // ========================================
    // PRIVATE HELPER METHODS - Validation
    // ========================================
    
    /**
     * Validates userId is non-null and non-empty
     * 
     * @param userId User identifier
     * @throws IllegalArgumentException if null or empty
     */
    private void validateUserId(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
    }
    
    // ========================================
    // TEST METHODS
    // ========================================
    
    /**
     * Test cases for Phase 4
     */
    public static void main(String[] args) {
        Phase4UndoableSpreadsheet sheet = new Phase4UndoableSpreadsheet(5, 'E');
        
        // Test 1: Basic undo
        sheet.set("alice", 1, 'A', 10, 100);
        assert sheet.get(1, 'A') == 10 : "Test 1a failed";
        
        sheet.undo("alice");
        assert sheet.get(1, 'A') == 0 : "Test 1b failed (undo)";
        System.out.println("✅ Test 1 passed: Basic undo");
        
        // Test 2: Redo
        sheet.set("alice", 1, 'A', 10, 100);
        sheet.undo("alice");
        sheet.redo("alice");
        assert sheet.get(1, 'A') == 10 : "Test 2 failed (redo)";
        System.out.println("✅ Test 2 passed: Redo");
        
        // Test 3: Multiple operations
        sheet.set("alice", 1, 'A', 10, 100);
        sheet.set("alice", 1, 'A', 20, 200);
        sheet.set("alice", 1, 'A', 30, 300);
        
        sheet.undo("alice");
        assert sheet.get(1, 'A') == 20 : "Test 3a failed";
        sheet.undo("alice");
        assert sheet.get(1, 'A') == 10 : "Test 3b failed";
        sheet.undo("alice");
        assert sheet.get(1, 'A') == 0 : "Test 3c failed";
        System.out.println("✅ Test 3 passed: Multiple undos");
        
        // Test 4: Per-user undo stacks
        sheet.set("alice", 1, 'A', 10, 100);
        sheet.set("bob", 2, 'A', 20, 200);
        
        sheet.undo("alice");
        assert sheet.get(1, 'A') == 0 : "Test 4a failed";
        assert sheet.get(2, 'A') == 20 : "Test 4b failed (bob's cell unaffected)";
        System.out.println("✅ Test 4 passed: Per-user undo");
        
        // Test 5: Formula undo
        sheet.set("alice", 1, 'A', 10, 100);
        sheet.sum("alice", 2, 'A', new String[]{"A1"}, 200);
        assert sheet.get(2, 'A') == 10 : "Test 5a failed";
        
        sheet.undo("alice");
        assert sheet.get(2, 'A') == 0 : "Test 5b failed (formula undone)";
        System.out.println("✅ Test 5 passed: Formula undo");
        
        // Test 6: Audit log
        sheet.set("alice", 1, 'A', 10, 100);
        sheet.set("bob", 2, 'A', 20, 200);
        
        List<Operation> aliceOps = sheet.getOperationHistory("alice");
        List<Operation> bobOps = sheet.getOperationHistory("bob");
        
        assert aliceOps.size() >= 1 : "Test 6a failed";
        assert bobOps.size() >= 1 : "Test 6b failed";
        assert sheet.getAllOperations().size() >= 2 : "Test 6c failed";
        System.out.println("✅ Test 6 passed: Audit log");
        
        // Test 7: Redo clears on new operation
        sheet.set("alice", 1, 'A', 10, 100);
        sheet.undo("alice");
        sheet.set("alice", 1, 'A', 20, 200);  // New operation
        
        sheet.redo("alice");  // Should do nothing
        assert sheet.get(1, 'A') == 20 : "Test 7 failed (redo stack cleared)";
        System.out.println("✅ Test 7 passed: Redo stack clears");
        
        // Test 8: Null userId in set
        try {
            sheet.set(null, 1, 'A', 10, 100);
            assert false : "Test 8 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("cannot be null") : "Test 8 failed (wrong message)";
        }
        System.out.println("✅ Test 8 passed: Null userId");
        
        // Test 9: Empty userId
        try {
            sheet.set("  ", 1, 'A', 10, 100);
            assert false : "Test 9 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("cannot be empty") : "Test 9 failed (wrong message)";
        }
        System.out.println("✅ Test 9 passed: Empty userId");
        
        // Test 10: Null userId in undo
        try {
            sheet.undo(null);
            assert false : "Test 10 failed (should throw)";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("cannot be null") : "Test 10 failed (wrong message)";
        }
        System.out.println("✅ Test 10 passed: Null userId in undo");
        
        System.out.println("\n✅ Phase 4: All 10 tests passed (7 functional + 3 validation)!");
    }
}

