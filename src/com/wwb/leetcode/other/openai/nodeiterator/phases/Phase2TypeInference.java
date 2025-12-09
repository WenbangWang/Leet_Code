package com.wwb.leetcode.other.openai.nodeiterator.phases;

import com.wwb.leetcode.other.openai.nodeiterator.Node;
import com.wwb.leetcode.other.openai.nodeiterator.Function;
import java.util.List;

/**
 * PHASE 2: Type Inference with Generic Binding (15 minutes)
 * 
 * Goal: Given a generic function and concrete argument types, infer the concrete return type.
 * 
 * Algorithm:
 * 1. Check arity (params.length == args.length)
 * 2. Build binding map by traversing params and args in parallel:
 *    - If param is generic (T1), bind it to the corresponding arg type
 *    - If param is concrete, check it matches arg (else error)
 *    - If param is tuple, recurse into children
 *    - Detect conflicts: T1 bound to both int and char
 * 3. Apply bindings to return type:
 *    - Replace generics with their bound types
 *    - Recurse into nested structures
 * 
 * Time Complexity: O(n + m) where n = params size, m = return type size
 * Space Complexity: O(g) where g = number of generic variables
 * 
 * Interview Tips:
 * - Ask: "Should I throw exception or return null on error?"
 * - Ask: "What if a generic appears in return but not in params?" (free variable)
 * - Test: simple binding, nested tuples, conflicts, arity mismatch
 * 
 * This phase uses the existing Function.getReturnType() method.
 */
public class Phase2TypeInference {
    
    /**
     * Wrapper around Function.getReturnType() with better error handling.
     */
    public static Node inferReturnType(Function function, List<Node> args) throws TypeException {
        try {
            return function.getReturnType(args);
        } catch (Exception e) {
            throw new TypeException("Type inference failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Test cases demonstrating type inference.
     */
    public static void runTests() {
        System.out.println("=== PHASE 2: Type Inference Tests ===\n");
        
        int passed = 0;
        int total = 0;
        
        // Test 1: Simple generic binding
        total++;
        try {
            // Function: (T1, T2) -> [T1, T2]
            Node t1 = new Node("T1");
            Node t2 = new Node("T2");
            Function func = new Function(
                List.of(t1, t2),
                new Node(List.of(t1, t2))
            );
            
            // Args: (int, char)
            Node intNode = new Node("int");
            Node charNode = new Node("char");
            
            Node result = inferReturnType(func, List.of(intNode, charNode));
            String expected = "[int,char]";
            
            if (result.toString().equals(expected)) {
                System.out.println("✅ Test 1: Simple binding");
                System.out.println("   Function: " + func);
                System.out.println("   Args:     (int, char)");
                System.out.println("   Result:   " + result);
                passed++;
            } else {
                System.out.println("❌ Test 1 FAILED: expected " + expected + " but got " + result);
            }
        } catch (Exception e) {
            System.out.println("❌ Test 1 FAILED: " + e.getMessage());
        }
        
        System.out.println();
        
        // Test 2: Repeated generic (consistency check)
        total++;
        try {
            // Function: (T1, T2, int, T1) -> [T1, T2]
            Node t1 = new Node("T1");
            Node t2 = new Node("T2");
            Node intNode = new Node("int");
            
            Function func = new Function(
                List.of(t1, t2, intNode, t1),
                new Node(List.of(t1, t2))
            );
            
            // Args: (int, char, int, int)
            Node result = inferReturnType(func, List.of(
                new Node("int"),
                new Node("char"),
                new Node("int"),
                new Node("int")
            ));
            
            String expected = "[int,char]";
            if (result.toString().equals(expected)) {
                System.out.println("✅ Test 2: Repeated generic (consistent)");
                System.out.println("   Function: " + func);
                System.out.println("   Args:     (int, char, int, int)");
                System.out.println("   Result:   " + result);
                passed++;
            } else {
                System.out.println("❌ Test 2 FAILED: expected " + expected + " but got " + result);
            }
        } catch (Exception e) {
            System.out.println("❌ Test 2 FAILED: " + e.getMessage());
        }
        
        System.out.println();
        
        // Test 3: Nested tuple with generics
        total++;
        try {
            // Function: ([[T1, float], T2]) -> [T2, T1]
            Node t1 = new Node("T1");
            Node t2 = new Node("T2");
            Node floatNode = new Node("float");
            
            Function func = new Function(
                List.of(new Node(List.of(
                    new Node(List.of(t1, floatNode)),
                    t2
                ))),
                new Node(List.of(t2, t1))
            );
            
            // Args: ([[int, float], char])
            Node result = inferReturnType(func, List.of(
                new Node(List.of(
                    new Node(List.of(new Node("int"), new Node("float"))),
                    new Node("char")
                ))
            ));
            
            String expected = "[char,int]";
            if (result.toString().equals(expected)) {
                System.out.println("✅ Test 3: Nested tuple binding");
                System.out.println("   Function: " + func);
                System.out.println("   Args:     ([[int, float], char])");
                System.out.println("   Result:   " + result);
                passed++;
            } else {
                System.out.println("❌ Test 3 FAILED: expected " + expected + " but got " + result);
            }
        } catch (Exception e) {
            System.out.println("❌ Test 3 FAILED: " + e.getMessage());
        }
        
        System.out.println();
        
        // Test 4: Type conflict (should fail)
        total++;
        try {
            // Function: (T1, T1) -> T1
            Node t1 = new Node("T1");
            Function func = new Function(
                List.of(t1, t1),
                t1
            );
            
            // Args: (int, char) - conflict!
            Node result = inferReturnType(func, List.of(
                new Node("int"),
                new Node("char")
            ));
            
            System.out.println("❌ Test 4 FAILED: Should have detected type conflict");
        } catch (TypeException e) {
            System.out.println("✅ Test 4: Type conflict detected");
            System.out.println("   Function: (T1, T1) -> T1");
            System.out.println("   Args:     (int, char)");
            System.out.println("   Error:    " + e.getMessage());
            passed++;
        }
        
        System.out.println();
        
        // Test 5: Type mismatch (should fail)
        total++;
        try {
            // Function: (int, char) -> float
            Node intNode = new Node("int");
            Node charNode = new Node("char");
            Node floatNode = new Node("float");
            
            Function func = new Function(
                List.of(intNode, charNode),
                floatNode
            );
            
            // Args: (int, int) - second should be char!
            Node result = inferReturnType(func, List.of(
                new Node("int"),
                new Node("int")
            ));
            
            System.out.println("❌ Test 5 FAILED: Should have detected type mismatch");
        } catch (TypeException e) {
            System.out.println("✅ Test 5: Type mismatch detected");
            System.out.println("   Function: (int, char) -> float");
            System.out.println("   Args:     (int, int)");
            System.out.println("   Error:    " + e.getMessage());
            passed++;
        }
        
        System.out.println();
        
        // Test 6: Arity mismatch (should fail)
        total++;
        try {
            // Function: (T1, T2) -> T1
            Node t1 = new Node("T1");
            Node t2 = new Node("T2");
            Function func = new Function(List.of(t1, t2), t1);
            
            // Args: (int) - missing second arg!
            Node result = inferReturnType(func, List.of(new Node("int")));
            
            System.out.println("❌ Test 6 FAILED: Should have detected arity mismatch");
        } catch (TypeException e) {
            System.out.println("✅ Test 6: Arity mismatch detected");
            System.out.println("   Function: (T1, T2) -> T1");
            System.out.println("   Args:     (int)");
            System.out.println("   Error:    " + e.getMessage());
            passed++;
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Results: " + passed + "/" + total + " tests passed");
        if (passed == total) {
            System.out.println("✅ PHASE 2 COMPLETE: Type inference working correctly");
        } else {
            System.out.println("⚠️  Some tests failed - review implementation");
        }
        System.out.println("=".repeat(60));
    }
    
    public static void main(String[] args) {
        runTests();
    }
}

