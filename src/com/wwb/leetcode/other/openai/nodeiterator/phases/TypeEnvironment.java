package com.wwb.leetcode.other.openai.nodeiterator.phases;

import com.wwb.leetcode.other.openai.nodeiterator.Node;
import com.wwb.leetcode.other.openai.nodeiterator.Function;
import java.util.HashMap;
import java.util.Map;

/**
 * Type environment for managing variable and function bindings.
 * 
 * Supports:
 * - Variable bindings: x → int, y → char
 * - Function bindings: f → (int) → char
 * - Scoping: Nested environments for let-bindings
 * - Shadowing: Inner bindings override outer bindings
 * 
 * Example usage:
 * 
 * TypeEnvironment global = new TypeEnvironment();
 * global.bindVariable("x", new Node("int"));
 * 
 * TypeEnvironment local = global.createChild();
 * local.bindVariable("x", new Node("char"));  // Shadows global x
 * 
 * local.lookupVariable("x")  → char (local binding)
 * global.lookupVariable("x") → int  (global binding)
 */
public class TypeEnvironment {
    private final Map<String, Node> variables;
    private final Map<String, Function> functions;
    private final TypeEnvironment parent;  // For nested scopes
    
    /**
     * Create a root environment with no parent.
     */
    public TypeEnvironment() {
        this(null);
    }
    
    /**
     * Create a child environment with a parent scope.
     */
    public TypeEnvironment(TypeEnvironment parent) {
        this.variables = new HashMap<>();
        this.functions = new HashMap<>();
        this.parent = parent;
    }
    
    /**
     * Create a child environment for nested scopes.
     */
    public TypeEnvironment createChild() {
        return new TypeEnvironment(this);
    }
    
    /**
     * Bind a variable to a type in the current scope.
     * This shadows any parent binding with the same name.
     */
    public void bindVariable(String name, Node type) {
        variables.put(name, type);
    }
    
    /**
     * Look up a variable's type.
     * Searches current scope, then parent scopes.
     * 
     * @throws TypeException if variable is not bound
     */
    public Node lookupVariable(String name) throws TypeException {
        // Check current scope
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        
        // Check parent scopes
        if (parent != null) {
            return parent.lookupVariable(name);
        }
        
        // Not found
        throw TypeException.unboundVariable(name);
    }
    
    /**
     * Check if a variable is bound in this environment or any parent.
     */
    public boolean hasVariable(String name) {
        if (variables.containsKey(name)) {
            return true;
        }
        return parent != null && parent.hasVariable(name);
    }
    
    /**
     * Bind a function to a signature in the current scope.
     */
    public void bindFunction(String name, Function func) {
        functions.put(name, func);
    }
    
    /**
     * Look up a function's signature.
     * Searches current scope, then parent scopes.
     * 
     * @throws TypeException if function is not bound
     */
    public Function lookupFunction(String name) throws TypeException {
        // Check current scope
        if (functions.containsKey(name)) {
            return functions.get(name);
        }
        
        // Check parent scopes
        if (parent != null) {
            return parent.lookupFunction(name);
        }
        
        // Not found
        throw TypeException.unboundFunction(name);
    }
    
    /**
     * Check if a function is bound in this environment or any parent.
     */
    public boolean hasFunction(String name) {
        if (functions.containsKey(name)) {
            return true;
        }
        return parent != null && parent.hasFunction(name);
    }
    
    /**
     * Get the parent environment (for debugging).
     */
    public TypeEnvironment getParent() {
        return parent;
    }
    
    /**
     * Get all variables in the current scope (not including parents).
     */
    public Map<String, Node> getLocalVariables() {
        return new HashMap<>(variables);
    }
    
    /**
     * Get all functions in the current scope (not including parents).
     */
    public Map<String, Function> getLocalFunctions() {
        return new HashMap<>(functions);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TypeEnvironment{\n");
        
        // Variables
        if (!variables.isEmpty()) {
            sb.append("  Variables:\n");
            for (Map.Entry<String, Node> entry : variables.entrySet()) {
                sb.append("    ").append(entry.getKey())
                  .append(" : ").append(entry.getValue()).append("\n");
            }
        }
        
        // Functions
        if (!functions.isEmpty()) {
            sb.append("  Functions:\n");
            for (Map.Entry<String, Function> entry : functions.entrySet()) {
                sb.append("    ").append(entry.getKey())
                  .append(" : ").append(entry.getValue()).append("\n");
            }
        }
        
        // Parent
        if (parent != null) {
            sb.append("  Parent: [exists]\n");
        }
        
        sb.append("}");
        return sb.toString();
    }
}

