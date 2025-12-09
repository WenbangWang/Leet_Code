package com.wwb.leetcode.other.openai.nodeiterator.phases;

import com.wwb.leetcode.other.openai.nodeiterator.Node;
import com.wwb.leetcode.other.openai.nodeiterator.Function;
import java.util.List;

/**
 * PHASE 1: Data Representation & toString (12 minutes)
 * 
 * Goal: Represent a toy language's type system with primitives, generics, and tuples.
 * 
 * Key Concepts:
 * - Primitives: int, float, char (concrete types)
 * - Generics: T, T1, T2, S, etc. (type variables)
 * - Tuples: [int, char], [[T1, float], T2] (composite types)
 * - Functions: (params) -> returnType
 * 
 * This phase uses the existing Node and Function classes.
 * 
 * Time Complexity:
 * - toString(): O(n) where n is the number of nodes in the tree
 * 
 * Interview Tips:
 * - Ask: "Should I use brackets or parentheses for tuples?"
 * - Ask: "Are generic names case-sensitive?"
 * - Test: nested tuples, empty tuples, multiple parameters
 */
public class Phase1DataRepresentation {
    
    /**
     * Demonstrates Node usage for representing types.
     * 
     * Examples:
     * - new Node("int")           → "int"
     * - new Node(List.of(...))    → "[int,char]"
     */
    public static void demonstrateNodes() {
        System.out.println("=== PHASE 1: Node Representation ===\n");
        
        // Primitives
        Node intNode = new Node("int");
        Node floatNode = new Node("float");
        Node charNode = new Node("char");
        
        System.out.println("Primitives:");
        System.out.println("  int:   " + intNode);
        System.out.println("  float: " + floatNode);
        System.out.println("  char:  " + charNode);
        
        // Generics
        Node t1 = new Node("T1");
        Node t2 = new Node("T2");
        
        System.out.println("\nGenerics:");
        System.out.println("  T1: " + t1);
        System.out.println("  T2: " + t2);
        
        // Simple tuple
        Node tuple1 = new Node(List.of(intNode, charNode));
        System.out.println("\nSimple tuple:");
        System.out.println("  [int, char]: " + tuple1);
        
        // Nested tuple
        Node innerTuple = new Node(List.of(t1, floatNode));
        Node nestedTuple = new Node(List.of(innerTuple, t2));
        System.out.println("\nNested tuple:");
        System.out.println("  [[T1, float], T2]: " + nestedTuple);
        
        // Complex nested structure
        Node complex = new Node(List.of(
            intNode,
            new Node(List.of(charNode, t1)),
            new Node(List.of(floatNode, new Node(List.of(t2, intNode))))
        ));
        System.out.println("\nComplex structure:");
        System.out.println("  " + complex);
    }
    
    /**
     * Demonstrates Function usage for representing function signatures.
     * 
     * Format: (param1, param2, ...) -> returnType
     */
    public static void demonstrateFunctions() {
        System.out.println("\n=== PHASE 1: Function Representation ===\n");
        
        Node intNode = new Node("int");
        Node charNode = new Node("char");
        Node floatNode = new Node("float");
        Node t1 = new Node("T1");
        Node t2 = new Node("T2");
        
        // Simple function: (int, char) -> float
        Function func1 = new Function(
            List.of(intNode, charNode),
            floatNode
        );
        System.out.println("Simple function:");
        System.out.println("  " + func1);
        
        // Generic function: (T1, T2) -> [T1, T2]
        Function func2 = new Function(
            List.of(t1, t2),
            new Node(List.of(t1, t2))
        );
        System.out.println("\nGeneric function:");
        System.out.println("  " + func2);
        
        // Higher-order function: (T1, (T1) -> T2) -> T2
        Function innerFunc = new Function(List.of(t1), t2);
        // Note: We can't directly represent function types in Node,
        // but we can document the signature
        System.out.println("\nHigher-order signature (conceptual):");
        System.out.println("  (T1, (T1) -> T2) -> T2");
        
        // Complex: ([int, T1], T2, int) -> [[T2, float], T1]
        Function func3 = new Function(
            List.of(
                new Node(List.of(intNode, t1)),
                t2,
                intNode
            ),
            new Node(List.of(
                new Node(List.of(t2, floatNode)),
                t1
            ))
        );
        System.out.println("\nComplex function:");
        System.out.println("  " + func3);
    }
    
    /**
     * Main test driver for Phase 1.
     */
    public static void main(String[] args) {
        demonstrateNodes();
        System.out.println("\n" + "=".repeat(60) + "\n");
        demonstrateFunctions();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ PHASE 1 COMPLETE: Data structures and toString implemented");
        System.out.println("=".repeat(60));
    }
}

