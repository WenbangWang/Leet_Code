package com.wwb.leetcode.other.openai.nodeiterator.phases;

import com.wwb.leetcode.other.openai.nodeiterator.Node;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents an expression in the toy language.
 * 
 * Expression types:
 * - LITERAL: A concrete value (10, 'a', [1, 2])
 * - VARIABLE: A variable reference (x, y)
 * - FUNCTION_CALL: Function application (f(x, y))
 * - LET_BINDING: let x = value in body
 * - TUPLE: Tuple construction ([x, y, z])
 * 
 * This is a simplified AST (Abstract Syntax Tree) for demonstration purposes.
 * In a real language implementation, you'd have more node types and better structure.
 */
public class Expression {
    
    public enum Type {
        LITERAL,        // Concrete value with a type
        VARIABLE,       // Variable reference
        FUNCTION_CALL,  // Function application
        LET_BINDING,    // Let-binding
        TUPLE           // Tuple construction
    }
    
    private final Type type;
    private final Node literalType;           // For LITERAL
    private final String variableName;        // For VARIABLE
    private final String functionName;        // For FUNCTION_CALL
    private final List<Expression> arguments; // For FUNCTION_CALL, TUPLE
    private final String letVarName;          // For LET_BINDING
    private final Expression letValue;        // For LET_BINDING
    private final Expression letBody;         // For LET_BINDING
    
    // Constructor for LITERAL
    private Expression(Node literalType) {
        this.type = Type.LITERAL;
        this.literalType = literalType;
        this.variableName = null;
        this.functionName = null;
        this.arguments = null;
        this.letVarName = null;
        this.letValue = null;
        this.letBody = null;
    }
    
    // Constructor for VARIABLE
    private Expression(String variableName, boolean isVariable) {
        this.type = Type.VARIABLE;
        this.literalType = null;
        this.variableName = variableName;
        this.functionName = null;
        this.arguments = null;
        this.letVarName = null;
        this.letValue = null;
        this.letBody = null;
    }
    
    // Constructor for FUNCTION_CALL
    private Expression(String functionName, List<Expression> arguments) {
        this.type = Type.FUNCTION_CALL;
        this.literalType = null;
        this.variableName = null;
        this.functionName = functionName;
        this.arguments = arguments;
        this.letVarName = null;
        this.letValue = null;
        this.letBody = null;
    }
    
    // Constructor for LET_BINDING
    private Expression(String letVarName, Expression letValue, Expression letBody) {
        this.type = Type.LET_BINDING;
        this.literalType = null;
        this.variableName = null;
        this.functionName = null;
        this.arguments = null;
        this.letVarName = letVarName;
        this.letValue = letValue;
        this.letBody = letBody;
    }
    
    // Constructor for TUPLE
    private Expression(List<Expression> elements, boolean isTuple) {
        this.type = Type.TUPLE;
        this.literalType = null;
        this.variableName = null;
        this.functionName = null;
        this.arguments = elements;
        this.letVarName = null;
        this.letValue = null;
        this.letBody = null;
    }
    
    // Factory methods for creating expressions
    
    public static Expression literal(Node type) {
        return new Expression(type);
    }
    
    public static Expression variable(String name) {
        return new Expression(name, true);
    }
    
    public static Expression functionCall(String functionName, List<Expression> arguments) {
        return new Expression(functionName, arguments);
    }
    
    public static Expression letBinding(String varName, Expression value, Expression body) {
        return new Expression(varName, value, body);
    }
    
    public static Expression tuple(List<Expression> elements) {
        return new Expression(elements, true);
    }
    
    // Getters
    
    public Type getType() {
        return type;
    }
    
    public Node getLiteralType() {
        return literalType;
    }
    
    public String getVariableName() {
        return variableName;
    }
    
    public String getFunctionName() {
        return functionName;
    }
    
    public List<Expression> getArguments() {
        return arguments;
    }
    
    public String getLetVarName() {
        return letVarName;
    }
    
    public Expression getLetValue() {
        return letValue;
    }
    
    public Expression getLetBody() {
        return letBody;
    }
    
    @Override
    public String toString() {
        switch (type) {
            case LITERAL:
                return literalType.toString();
            case VARIABLE:
                return variableName;
            case FUNCTION_CALL:
                List<String> argStrs = new ArrayList<>();
                for (Expression arg : arguments) {
                    argStrs.add(arg.toString());
                }
                return functionName + "(" + String.join(", ", argStrs) + ")";
            case LET_BINDING:
                return "let " + letVarName + " = " + letValue + " in " + letBody;
            case TUPLE:
                List<String> elemStrs = new ArrayList<>();
                for (Expression elem : arguments) {
                    elemStrs.add(elem.toString());
                }
                return "[" + String.join(", ", elemStrs) + "]";
            default:
                return "Unknown";
        }
    }
}

