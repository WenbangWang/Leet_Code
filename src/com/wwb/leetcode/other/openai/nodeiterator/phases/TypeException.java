package com.wwb.leetcode.other.openai.nodeiterator.phases;

/**
 * Custom exception for type-related errors in the toy language.
 * Provides descriptive error messages for type mismatches, conflicts, and other type errors.
 */
public class TypeException extends Exception {
    
    public TypeException(String message) {
        super(message);
    }
    
    public TypeException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Factory methods for common error scenarios
    
    public static TypeException typeMismatch(String expected, String actual) {
        return new TypeException(
            String.format("Type mismatch: expected %s but got %s", expected, actual)
        );
    }
    
    public static TypeException typeConflict(String generic, String type1, String type2) {
        return new TypeException(
            String.format("Type conflict: generic %s bound to both %s and %s", 
                generic, type1, type2)
        );
    }
    
    public static TypeException arityMismatch(int expected, int actual) {
        return new TypeException(
            String.format("Arity mismatch: expected %d arguments but got %d", 
                expected, actual)
        );
    }
    
    public static TypeException compositionError(String returnType, String paramType) {
        return new TypeException(
            String.format("Cannot compose: first function returns %s but second expects %s", 
                returnType, paramType)
        );
    }
    
    public static TypeException unboundVariable(String varName) {
        return new TypeException(
            String.format("Unbound variable: %s", varName)
        );
    }
    
    public static TypeException unboundFunction(String funcName) {
        return new TypeException(
            String.format("Unbound function: %s", funcName)
        );
    }
}

