package com.wwb.leetcode.other.openai.nodeiterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Node {
    private static final Set<String> TYPE_LIST = Set.of("str", "int", "float");

    // these two are mutually exclusive
    private String baseGeneric;       // Base type name
    private List<Node> children; // Children nodes if not base type

    // Constructor for base type
    public Node(String type) {
        this.baseGeneric = type;
        this.children = new ArrayList<>();
    }

    // Constructor for children nodes
    public Node(List<Node> children) {
        this.baseGeneric = null;
        this.children = children;
    }

    public Object getContent() {
        if (baseGeneric != null) return baseGeneric;
        return children;
    }

    public boolean isBaseGenericType() {
        return baseGeneric != null && !TYPE_LIST.contains(baseGeneric);
    }

    public boolean isGenericType() {
        if (isBaseGenericType()) return true;
        if (children != null) {
            for (Node child : children) {
                if (child.isGenericType()) return true;
            }
        }
        return false;
    }

    public Node cloneNode() {
        if (baseGeneric != null) return new Node(baseGeneric);
        List<Node> clonedChildren = new ArrayList<>();
        for (Node child : children) {
            clonedChildren.add(child.cloneNode());
        }
        return new Node(clonedChildren);
    }

    @Override
    public String toString() {
        if (baseGeneric != null) return baseGeneric;
        List<String> childStrs = new ArrayList<>();
        for (Node child : children) {
            childStrs.add(child.toString());
        }
        return "[" + String.join(",", childStrs) + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node)) return false;
        return this.toString().equals(obj.toString());
    }

    public String getBaseGeneric() {
        return baseGeneric;
    }

    public List<Node> getChildren() {
        return children;
    }
}
