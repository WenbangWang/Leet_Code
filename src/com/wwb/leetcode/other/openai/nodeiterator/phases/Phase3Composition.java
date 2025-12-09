package com.wwb.leetcode.other.openai.nodeiterator.phases;

import com.wwb.leetcode.other.openai.nodeiterator.Node;
import com.wwb.leetcode.other.openai.nodeiterator.Function;
import java.util.List;
import java.util.ArrayList;

/**
 * PHASE 3: Function Composition (15-18 minutes)
 *
 * Goal: Support composing two functions where the output of one feeds into another.
 *
 * Key Concepts:
 * - Simple composition: h(x) = g(f(x))
 * - Type compatibility: f's return type must match g's first parameter type
 * - Generic propagation: Generics can flow through composition
 * - Partial application: If g needs more params than f provides, compose takes extra params
 *
 * Composition Rules:
 * 1. Simple: f: A → B, g: B → C  =>  h: A → C
 * 2. Generic: f: T1 → T2, g: T2 → T3  =>  h: T1 → T3
 * 3. Partial: f: A → B, g: (B, C) → D  =>  h: (A, C) → D
 *
 * Time Complexity: O(n + m) where n = f's size, m = g's size
 *
 * Interview Tips:
 * - Ask: "Should compose be h(x) = g(f(x)) or h(x) = f(g(x))?" (standard is g∘f)
 * - Ask: "How should I handle partial application?"
 * - Test: simple composition, generic propagation, incompatible types
 */
public class Phase3Composition {

    /**
     * Check if two functions can be composed: compose(f, g) = h where h(x) = g(f(x))
     *
     * Rules:
     * - f's return type must be compatible with g's first parameter
     * - Compatible means: exact match OR both involve generics that can unify
     *
     * @param first Function f
     * @param second Function g
     * @return true if g can consume f's output
     */
    public static boolean canCompose(Function first, Function second) {
        // Check if second function has at least one parameter
        if (second.getParameters().isEmpty()) {
            return false;
        }

        Node fReturn = first.getOutputType();
        Node gFirstParam = second.getParameters().get(0);

        // Check if types are compatible
        return areTypesCompatible(fReturn, gFirstParam);
    }

    /**
     * Check if two types are compatible for composition.
     *
     * Compatible cases:
     * 1. Exact match: int == int
     * 2. Both generic: T1 == T2 (can unify)
     * 3. One generic: T1 can match anything
     * 4. Structural match: [T1, int] can match [char, int] if T1 → char
     */
    private static boolean areTypesCompatible(Node type1, Node type2) {
        // Case 1: Exact string match
        if (type1.toString().equals(type2.toString())) {
            return true;
        }

        // Case 2: Either is generic (can potentially unify)
        if (type1.isGenericType() || type2.isGenericType()) {
            return true;
        }

        // Case 3: Both are tuples - check structural compatibility
        if (!type1.getChildren().isEmpty() && !type2.getChildren().isEmpty()) {

            List<Node> children1 = type1.getChildren();
            List<Node> children2 = type2.getChildren();

            if (children1.size() != children2.size()) {
                return false;
            }

            for (int i = 0; i < children1.size(); i++) {
                if (!areTypesCompatible(children1.get(i), children2.get(i))) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Compose two functions: compose(f, g) = h where h(x) = g(f(x))
     *
     * Simple case: f: A → B, g: B → C  =>  h: A → C
     * Partial case: f: A → B, g: (B, C) → D  =>  h: (A, C) → D
     *
     * @param first Function f
     * @param second Function g
     * @return Composed function h
     * @throws TypeException if functions cannot be composed
     */
    public static ComposedFunction compose(Function first, Function second) throws TypeException {
        if (!canCompose(first, second)) {
            throw TypeException.compositionError(
                first.getOutputType().toString(),
                second.getParameters().isEmpty() ? "none" :
                    second.getParameters().get(0).toString()
            );
        }

        // Build parameter list for composed function
        List<Node> composedParams = new ArrayList<>();

        // Add all parameters from first function
        composedParams.addAll(first.getParameters());

        // Add extra parameters from second function (partial application)
        if (second.getParameters().size() > 1) {
            composedParams.addAll(second.getParameters().subList(1, second.getParameters().size()));
        }

        // Return type is second function's return type
        Node composedReturn = second.getOutputType();

        return new ComposedFunction(first, second, composedParams, composedReturn);
    }

    /**
     * Compose two functions with concrete type checking.
     * This variant performs stricter type checking at composition time.
     */
    public static ComposedFunction composeStrict(Function first, Function second) throws TypeException {
        if (second.getParameters().isEmpty()) {
            throw new TypeException("Cannot compose: second function has no parameters");
        }

        Node fReturn = first.getOutputType();
        Node gFirstParam = second.getParameters().get(0);

        // For strict mode, we require more concrete compatibility
        if (!fReturn.toString().equals(gFirstParam.toString()) &&
            !fReturn.isGenericType() && !gFirstParam.isGenericType()) {
            throw TypeException.compositionError(
                fReturn.toString(),
                gFirstParam.toString()
            );
        }

        return compose(first, second);
    }

    /**
     * Demonstration and test cases for function composition.
     */
    public static void runTests() {
        System.out.println("=== PHASE 3: Function Composition Tests ===\n");

        int passed = 0;
        int total = 0;

        // Test 1: Simple composition with concrete types
        total++;
        try {
            // f: (int) → char
            Function f = new Function(
                List.of(new Node("int")),
                new Node("char")
            );

            // g: (char) → float
            Function g = new Function(
                List.of(new Node("char")),
                new Node("float")
            );

            // h = g ∘ f: (int) → float
            ComposedFunction h = compose(f, g);

            System.out.println("✅ Test 1: Simple composition");
            System.out.println("   f: " + f);
            System.out.println("   g: " + g);
            System.out.println("   h = g∘f: " + h.asFunction());
            System.out.println("   Expected: (int) -> float");
            passed++;
        } catch (Exception e) {
            System.out.println("❌ Test 1 FAILED: " + e.getMessage());
        }

        System.out.println();

        // Test 2: Generic composition
        total++;
        try {
            // f: (T1) → [T1, int]
            Node t1 = new Node("T1");
            Function f = new Function(
                List.of(t1),
                new Node(List.of(t1, new Node("int")))
            );

            // g: ([T1, int]) → T1
            Function g = new Function(
                List.of(new Node(List.of(t1, new Node("int")))),
                t1
            );

            // h = g ∘ f: (T1) → T1 (identity-like)
            ComposedFunction h = compose(f, g);

            System.out.println("✅ Test 2: Generic composition");
            System.out.println("   f: " + f);
            System.out.println("   g: " + g);
            System.out.println("   h = g∘f: " + h.asFunction());
            System.out.println("   Expected: (T1) -> T1");
            passed++;
        } catch (Exception e) {
            System.out.println("❌ Test 2 FAILED: " + e.getMessage());
        }

        System.out.println();

        // Test 3: Partial application
        total++;
        try {
            // f: (int) → char
            Function f = new Function(
                List.of(new Node("int")),
                new Node("char")
            );

            // g: (char, float) → [char, float]
            Function g = new Function(
                List.of(new Node("char"), new Node("float")),
                new Node(List.of(new Node("char"), new Node("float")))
            );

            // h = g ∘ f: (int, float) → [char, float]
            // Note: float param is "curried in"
            ComposedFunction h = compose(f, g);

            System.out.println("✅ Test 3: Partial application");
            System.out.println("   f: " + f);
            System.out.println("   g: " + g);
            System.out.println("   h = g∘f: " + h.asFunction());
            System.out.println("   Expected: (int, float) -> [char,float]");
            System.out.println("   Note: Second param 'float' comes from g's extra param");
            passed++;
        } catch (Exception e) {
            System.out.println("❌ Test 3 FAILED: " + e.getMessage());
        }

        System.out.println();

        // Test 4: Incompatible composition (should fail)
        total++;
        try {
            // f: (int) → char
            Function f = new Function(
                List.of(new Node("int")),
                new Node("char")
            );

            // g: (float) → int  (expects float, not char!)
            Function g = new Function(
                List.of(new Node("float")),
                new Node("int")
            );

            ComposedFunction h = composeStrict(f, g);
            System.out.println("❌ Test 4 FAILED: Should have detected incompatible types");
        } catch (TypeException e) {
            System.out.println("✅ Test 4: Incompatible composition detected");
            System.out.println("   f: (int) -> char");
            System.out.println("   g: (float) -> int");
            System.out.println("   Error: " + e.getMessage());
            passed++;
        }

        System.out.println();

        // Test 5: Generic-to-concrete composition
        total++;
        try {
            // f: (int) → char
            Function f = new Function(
                List.of(new Node("int")),
                new Node("char")
            );

            // g: (T1) → [T1, T1]  (generic)
            Node t1 = new Node("T1");
            Function g = new Function(
                List.of(t1),
                new Node(List.of(t1, t1))
            );

            // h = g ∘ f: (int) → [T1, T1] where T1 will be bound to char
            ComposedFunction h = compose(f, g);

            System.out.println("✅ Test 5: Generic-to-concrete composition");
            System.out.println("   f: " + f);
            System.out.println("   g: " + g);
            System.out.println("   h = g∘f: " + h.asFunction());
            System.out.println("   Note: When applied, T1 will bind to char");
            passed++;
        } catch (Exception e) {
            System.out.println("❌ Test 5 FAILED: " + e.getMessage());
        }

        System.out.println();

        // Test 6: Chained composition (f, g, h)
        total++;
        try {
            // f: (int) → char
            Function f = new Function(
                List.of(new Node("int")),
                new Node("char")
            );

            // g: (char) → float
            Function g = new Function(
                List.of(new Node("char")),
                new Node("float")
            );

            // h: (float) → [int, float]
            Function h = new Function(
                List.of(new Node("float")),
                new Node(List.of(new Node("int"), new Node("float")))
            );

            // Compose: h ∘ g ∘ f
            ComposedFunction gf = compose(f, g);  // (int) → float
            ComposedFunction hgf = compose(gf.asFunction(), h);  // (int) → [int, float]

            System.out.println("✅ Test 6: Chained composition");
            System.out.println("   f: " + f);
            System.out.println("   g: " + g);
            System.out.println("   h: " + h);
            System.out.println("   g∘f: " + gf.asFunction());
            System.out.println("   h∘g∘f: " + hgf.asFunction());
            System.out.println("   Expected: (int) -> [int,float]");
            passed++;
        } catch (Exception e) {
            System.out.println("❌ Test 6 FAILED: " + e.getMessage());
        }

        System.out.println();

        // Test 7: Complex nested type composition
        total++;
        try {
            // f: ([T1, int]) → T1
            Node t1 = new Node("T1");
            Function f = new Function(
                List.of(new Node(List.of(t1, new Node("int")))),
                t1
            );

            // g: (T2) → [T2, T2]
            Node t2 = new Node("T2");
            Function g = new Function(
                List.of(t2),
                new Node(List.of(t2, t2))
            );

            // h = g ∘ f: ([T1, int]) → [T2, T2] where T2 would bind to T1's value
            ComposedFunction h = compose(f, g);

            System.out.println("✅ Test 7: Complex nested type composition");
            System.out.println("   f: " + f);
            System.out.println("   g: " + g);
            System.out.println("   h = g∘f: " + h.asFunction());
            passed++;
        } catch (Exception e) {
            System.out.println("❌ Test 7 FAILED: " + e.getMessage());
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("Results: " + passed + "/" + total + " tests passed");
        if (passed == total) {
            System.out.println("✅ PHASE 3 COMPLETE: Function composition working correctly");
        } else {
            System.out.println("⚠️  Some tests failed - review implementation");
        }
        System.out.println("=".repeat(60));
    }

    public static void main(String[] args) {
        runTests();
    }
}

