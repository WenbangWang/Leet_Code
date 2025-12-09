package com.wwb.leetcode.other.openai.nodeiterator.phases;

import com.wwb.leetcode.other.openai.nodeiterator.Node;
import com.wwb.leetcode.other.openai.nodeiterator.Function;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a composed function that is the result of composing two functions.
 * 
 * If h = compose(f, g), then h(x) = g(f(x))
 * 
 * This class maintains references to the original functions to support
 * type inference with generic propagation.
 */
public class ComposedFunction {
    private final Function first;   // f
    private final Function second;  // g
    private final List<Node> parameters;  // Combined parameters
    private final Node returnType;        // Final return type
    
    /**
     * Creates a composed function.
     * 
     * @param first The first function to apply (f)
     * @param second The second function to apply (g)
     * @param parameters The combined parameter list
     * @param returnType The final return type
     */
    public ComposedFunction(Function first, Function second, 
                           List<Node> parameters, Node returnType) {
        this.first = first;
        this.second = second;
        this.parameters = parameters;
        this.returnType = returnType;
    }
    
    /**
     * Get the composed function as a regular Function object.
     */
    public Function asFunction() {
        return new Function(parameters, returnType);
    }
    
    /**
     * Infer the concrete return type given actual arguments.
     * 
     * Algorithm:
     * 1. Apply args to first function → get intermediate type
     * 2. Apply intermediate type to second function → get final type
     * 3. Handle generic propagation across both functions
     */
    public Node inferReturnType(List<Node> args) throws Exception {
        // Step 1: Infer intermediate type from first function
        Node intermediateType = first.getReturnType(args);
        
        // Step 2: Apply intermediate type to second function
        // The second function's first parameter should match the intermediate type
        List<Node> secondArgs = new ArrayList<>();
        secondArgs.add(intermediateType);
        
        // Add any extra arguments for partial application
        int extraArgsStart = first.getParameters().size();
        for (int i = extraArgsStart; i < args.size(); i++) {
            secondArgs.add(args.get(i));
        }
        
        // Step 3: Infer final type
        return second.getReturnType(secondArgs);
    }
    
    public Function getFirst() {
        return first;
    }
    
    public Function getSecond() {
        return second;
    }
    
    public List<Node> getParameters() {
        return parameters;
    }
    
    public Node getReturnType() {
        return returnType;
    }
    
    @Override
    public String toString() {
        return "compose(" + first + ", " + second + ") = " + asFunction();
    }
}

