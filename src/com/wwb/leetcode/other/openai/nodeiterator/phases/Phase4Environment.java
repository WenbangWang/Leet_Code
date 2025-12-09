package com.wwb.leetcode.other.openai.nodeiterator.phases;

import com.wwb.leetcode.other.openai.nodeiterator.Node;
import com.wwb.leetcode.other.openai.nodeiterator.Function;
import java.util.List;
import java.util.ArrayList;

/**
 * PHASE 4: Type Environment & Variable Binding (12-15 minutes)
 * 
 * Goal: Support let-bindings and maintain a type environment for variables.
 * 
 * Key Concepts:
 * - Type environment: Symbol table mapping names to types
 * - Scoping: Nested environments for let-bindings
 * - Shadowing: Inner bindings override outer bindings
 * - Type checking: Verify expressions are well-typed
 * 
 * Examples:
 * 
 * let x = 10 in x                      → int
 * let x = 10 in let y = 'a' in [x, y]  → [int, char]
 * let f = (int) -> char in f(10)       → char
 * 
 * Time Complexity: O(n * d) where n = expression size, d = scope depth
 * Space Complexity: O(v + d) where v = variables, d = scope depth
 * 
 * Interview Tips:
 * - Ask: "Should I handle recursive let-bindings?"
 * - Ask: "What about mutual recursion?"
 * - Discuss: Shadowing vs error on redefinition
 * - Test: nested scopes, shadowing, unbound variables
 */
public class Phase4Environment {
    
    /**
     * Type check an expression in a given environment.
     * Returns the type of the expression.
     * 
     * @param expr The expression to type check
     * @param env The type environment
     * @return The type of the expression
     * @throws TypeException if the expression is ill-typed
     */
    public static Node typeCheck(Expression expr, TypeEnvironment env) throws TypeException {
        switch (expr.getType()) {
            case LITERAL:
                return typeCheckLiteral(expr, env);
            
            case VARIABLE:
                return typeCheckVariable(expr, env);
            
            case FUNCTION_CALL:
                return typeCheckFunctionCall(expr, env);
            
            case LET_BINDING:
                return typeCheckLetBinding(expr, env);
            
            case TUPLE:
                return typeCheckTuple(expr, env);
            
            default:
                throw new TypeException("Unknown expression type: " + expr.getType());
        }
    }
    
    /**
     * Type check a literal expression.
     * Literals have their type directly attached.
     */
    private static Node typeCheckLiteral(Expression expr, TypeEnvironment env) {
        return expr.getLiteralType();
    }
    
    /**
     * Type check a variable reference.
     * Look up the variable in the environment.
     */
    private static Node typeCheckVariable(Expression expr, TypeEnvironment env) throws TypeException {
        String varName = expr.getVariableName();
        return env.lookupVariable(varName);
    }
    
    /**
     * Type check a function call.
     * 
     * Algorithm:
     * 1. Look up function signature
     * 2. Type check all arguments
     * 3. Use type inference to get return type
     */
    private static Node typeCheckFunctionCall(Expression expr, TypeEnvironment env) throws TypeException {
        String funcName = expr.getFunctionName();
        List<Expression> argExprs = expr.getArguments();
        
        // Look up function
        Function func = env.lookupFunction(funcName);
        
        // Type check all arguments
        List<Node> argTypes = new ArrayList<>();
        for (Expression argExpr : argExprs) {
            argTypes.add(typeCheck(argExpr, env));
        }
        
        // Infer return type
        try {
            return func.getReturnType(argTypes);
        } catch (Exception e) {
            throw new TypeException(
                "Type error in function call " + funcName + ": " + e.getMessage(), e
            );
        }
    }
    
    /**
     * Type check a let-binding.
     * 
     * Algorithm:
     * 1. Type check the value expression
     * 2. Create a child environment
     * 3. Bind the variable to its type in the child environment
     * 4. Type check the body in the child environment
     * 
     * Example:
     * let x = 10 in x + 1
     * 
     * 1. Type of 10 is int
     * 2. Create child env
     * 3. Bind x → int in child
     * 4. Type check (x + 1) in child → int
     */
    private static Node typeCheckLetBinding(Expression expr, TypeEnvironment env) throws TypeException {
        String varName = expr.getLetVarName();
        Expression valueExpr = expr.getLetValue();
        Expression bodyExpr = expr.getLetBody();
        
        // Step 1: Type check the value
        Node valueType = typeCheck(valueExpr, env);
        
        // Step 2: Create child environment
        TypeEnvironment childEnv = env.createChild();
        
        // Step 3: Bind variable
        childEnv.bindVariable(varName, valueType);
        
        // Step 4: Type check body in child environment
        return typeCheck(bodyExpr, childEnv);
    }
    
    /**
     * Type check a tuple expression.
     * 
     * Algorithm:
     * 1. Type check each element
     * 2. Build a tuple type from element types
     */
    private static Node typeCheckTuple(Expression expr, TypeEnvironment env) throws TypeException {
        List<Expression> elements = expr.getArguments();
        List<Node> elementTypes = new ArrayList<>();
        
        for (Expression elem : elements) {
            elementTypes.add(typeCheck(elem, env));
        }
        
        return new Node(elementTypes);
    }
    
    /**
     * Demonstration and test cases for type environment and let-bindings.
     */
    public static void runTests() {
        System.out.println("=== PHASE 4: Type Environment Tests ===\n");
        
        int passed = 0;
        int total = 0;
        
        // Test 1: Simple let-binding
        total++;
        try {
            TypeEnvironment env = new TypeEnvironment();
            
            // let x = 10 in x
            Expression expr = Expression.letBinding(
                "x",
                Expression.literal(new Node("int")),
                Expression.variable("x")
            );
            
            Node result = typeCheck(expr, env);
            
            if (result.toString().equals("int")) {
                System.out.println("✅ Test 1: Simple let-binding");
                System.out.println("   Expression: " + expr);
                System.out.println("   Type: " + result);
                passed++;
            } else {
                System.out.println("❌ Test 1 FAILED: expected int but got " + result);
            }
        } catch (Exception e) {
            System.out.println("❌ Test 1 FAILED: " + e.getMessage());
        }
        
        System.out.println();
        
        // Test 2: Nested let-bindings
        total++;
        try {
            TypeEnvironment env = new TypeEnvironment();
            
            // let x = 10 in let y = 'a' in [x, y]
            Expression expr = Expression.letBinding(
                "x",
                Expression.literal(new Node("int")),
                Expression.letBinding(
                    "y",
                    Expression.literal(new Node("char")),
                    Expression.tuple(List.of(
                        Expression.variable("x"),
                        Expression.variable("y")
                    ))
                )
            );
            
            Node result = typeCheck(expr, env);
            
            if (result.toString().equals("[int,char]")) {
                System.out.println("✅ Test 2: Nested let-bindings");
                System.out.println("   Expression: " + expr);
                System.out.println("   Type: " + result);
                passed++;
            } else {
                System.out.println("❌ Test 2 FAILED: expected [int,char] but got " + result);
            }
        } catch (Exception e) {
            System.out.println("❌ Test 2 FAILED: " + e.getMessage());
        }
        
        System.out.println();
        
        // Test 3: Shadowing
        total++;
        try {
            TypeEnvironment env = new TypeEnvironment();
            
            // let x = 10 in let x = 'a' in x
            // Inner x shadows outer x
            Expression expr = Expression.letBinding(
                "x",
                Expression.literal(new Node("int")),
                Expression.letBinding(
                    "x",
                    Expression.literal(new Node("char")),
                    Expression.variable("x")
                )
            );
            
            Node result = typeCheck(expr, env);
            
            if (result.toString().equals("char")) {
                System.out.println("✅ Test 3: Shadowing");
                System.out.println("   Expression: " + expr);
                System.out.println("   Type: " + result + " (char shadows int)");
                passed++;
            } else {
                System.out.println("❌ Test 3 FAILED: expected char but got " + result);
            }
        } catch (Exception e) {
            System.out.println("❌ Test 3 FAILED: " + e.getMessage());
        }
        
        System.out.println();
        
        // Test 4: Function call with let-binding
        total++;
        try {
            TypeEnvironment env = new TypeEnvironment();
            
            // Define function: id: (T1) -> T1
            Node t1 = new Node("T1");
            Function idFunc = new Function(List.of(t1), t1);
            env.bindFunction("id", idFunc);
            
            // let x = 10 in id(x)
            Expression expr = Expression.letBinding(
                "x",
                Expression.literal(new Node("int")),
                Expression.functionCall("id", List.of(Expression.variable("x")))
            );
            
            Node result = typeCheck(expr, env);
            
            if (result.toString().equals("int")) {
                System.out.println("✅ Test 4: Function call with let-binding");
                System.out.println("   Expression: " + expr);
                System.out.println("   Type: " + result);
                passed++;
            } else {
                System.out.println("❌ Test 4 FAILED: expected int but got " + result);
            }
        } catch (Exception e) {
            System.out.println("❌ Test 4 FAILED: " + e.getMessage());
        }
        
        System.out.println();
        
        // Test 5: Unbound variable (should fail)
        total++;
        try {
            TypeEnvironment env = new TypeEnvironment();
            
            // Reference x without binding
            Expression expr = Expression.variable("x");
            
            Node result = typeCheck(expr, env);
            
            System.out.println("❌ Test 5 FAILED: Should have detected unbound variable");
        } catch (TypeException e) {
            System.out.println("✅ Test 5: Unbound variable detected");
            System.out.println("   Expression: x");
            System.out.println("   Error: " + e.getMessage());
            passed++;
        }
        
        System.out.println();
        
        // Test 6: Unbound function (should fail)
        total++;
        try {
            TypeEnvironment env = new TypeEnvironment();
            
            // Call undefined function
            Expression expr = Expression.functionCall("foo", List.of(
                Expression.literal(new Node("int"))
            ));
            
            Node result = typeCheck(expr, env);
            
            System.out.println("❌ Test 6 FAILED: Should have detected unbound function");
        } catch (TypeException e) {
            System.out.println("✅ Test 6: Unbound function detected");
            System.out.println("   Expression: foo(int)");
            System.out.println("   Error: " + e.getMessage());
            passed++;
        }
        
        System.out.println();
        
        // Test 7: Complex nested expression
        total++;
        try {
            TypeEnvironment env = new TypeEnvironment();
            
            // Define pair: (T1, T2) -> [T1, T2]
            Node t1 = new Node("T1");
            Node t2 = new Node("T2");
            Function pairFunc = new Function(
                List.of(t1, t2),
                new Node(List.of(t1, t2))
            );
            env.bindFunction("pair", pairFunc);
            
            // let x = 10 in let y = 'a' in pair(x, y)
            Expression expr = Expression.letBinding(
                "x",
                Expression.literal(new Node("int")),
                Expression.letBinding(
                    "y",
                    Expression.literal(new Node("char")),
                    Expression.functionCall("pair", List.of(
                        Expression.variable("x"),
                        Expression.variable("y")
                    ))
                )
            );
            
            Node result = typeCheck(expr, env);
            
            if (result.toString().equals("[int,char]")) {
                System.out.println("✅ Test 7: Complex nested expression");
                System.out.println("   Expression: " + expr);
                System.out.println("   Type: " + result);
                passed++;
            } else {
                System.out.println("❌ Test 7 FAILED: expected [int,char] but got " + result);
            }
        } catch (Exception e) {
            System.out.println("❌ Test 7 FAILED: " + e.getMessage());
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Results: " + passed + "/" + total + " tests passed");
        if (passed == total) {
            System.out.println("✅ PHASE 4 COMPLETE: Type environment and let-bindings working correctly");
        } else {
            System.out.println("⚠️  Some tests failed - review implementation");
        }
        System.out.println("=".repeat(60));
    }
    
    public static void main(String[] args) {
        runTests();
    }
}

