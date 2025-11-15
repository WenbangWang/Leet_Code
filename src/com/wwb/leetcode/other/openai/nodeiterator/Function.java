package com.wwb.leetcode.other.openai.nodeiterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Function {
    private List<Node> parameters;
    private Node outputType;

    public Function(List<Node> parameters, Node outputType) {
        this.parameters = parameters;
        this.outputType = outputType;
    }

    @Override
    public String toString() {
        List<String> paramStrs = new ArrayList<>();
        for (Node param : parameters) {
            paramStrs.add(param.toString());
        }
        return "(" + String.join(",", paramStrs) + ") -> " + outputType.toString();
    }

    public List<Node> getParameters() {
        return parameters;
    }

    public Node getOutputType() {
        return outputType;
    }

    public Node getReturnType(List<Node> args) throws Exception {
        if (args.size() != this.getParameters().size()) {
            throw new Exception("Illegal Arguments");
        }
        Map<String, Node> bindingMap = new HashMap<>();
        List<Node> funcParams = this.getParameters();
        for (int i = 0; i < args.size(); i++) {
            binding(funcParams.get(i), args.get(i), bindingMap);
        }
        return replaceInvocationArguments(this.getOutputType(), bindingMap);
    }

    private static Node replaceInvocationArguments(Node node, Map<String, Node> bindingMap) {
        if (!node.isGenericType()) {
            return node.cloneNode();
        }
        if (node.getChildren().isEmpty()) {
            return bindingMap.get(node.getBaseGeneric()).cloneNode();
        }
        List<Node> replacedChildren = new ArrayList<>();
        for (Node child : node.getChildren()) {
            replacedChildren.add(replaceInvocationArguments(child, bindingMap));
        }
        return new Node(replacedChildren);
    }

    // Bind function parameter nodes to invocation argument nodes
    private static void binding(Node funcNode, Node argNode, Map<String, Node> bindingMap) throws Exception {
        if (funcNode.isBaseGenericType()) {
            String key = funcNode.getBaseGeneric();
            if (bindingMap.containsKey(key)) {
                if (!bindingMap.get(key).equals(argNode)) {
                    throw new Exception("Invocation argument type mismatched on " + funcNode + " and " + argNode);
                }
            } else {
                bindingMap.put(key, argNode);
            }
        } else if (funcNode.equals(argNode)) {
            return;
        } else if (funcNode.getBaseGeneric() == null && argNode.getBaseGeneric() == null) {
            List<Node> funcChildren = funcNode.getChildren();
            List<Node> argChildren = argNode.getChildren();
            for (int i = 0; i < funcChildren.size(); i++) {
                binding(funcChildren.get(i), argChildren.get(i), bindingMap);
            }
        } else {
            throw new Exception("Mismatch parameter on " + funcNode + " and " + argNode);
        }
    }
}
