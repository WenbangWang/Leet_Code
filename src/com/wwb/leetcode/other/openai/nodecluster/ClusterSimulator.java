package com.wwb.leetcode.other.openai.nodecluster;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClusterSimulator {

    // Simple in-memory node registry to simulate network
    static Map<Integer, Node> nodeRegistry = new HashMap<>();

    public static void main(String[] args) {
        // 1. Build tree structure
        // Example tree:
        //        1
        //      / | \
        //     2  3  4
        //    / \
        //   5   6

        Node n5 = createNode(5, 2, Collections.emptyList());
        Node n6 = createNode(6, 2, Collections.emptyList());
        Node n2 = createNode(2, 1, Arrays.asList(5, 6));
        Node n3 = createNode(3, 1, Collections.emptyList());
        Node n4 = createNode(4, 1, Collections.emptyList());
        Node n1 = createNode(1, null, Arrays.asList(2, 3, 4)); // root


        // 3. Test COUNT
        System.out.println("=== COUNT TEST ===");
        n1.receiveMessage(null, "COUNT:" + UUID.randomUUID());

        // 4. Test TOPO
        System.out.println("\n=== TOPOLOGY TEST ===");
        n1.receiveMessage(null, "TOPO:" + UUID.randomUUID());
    }

    private static SimNode createNode(int id, Integer parentId, List<Integer> children) {
        SimNode node = new SimNode(id, parentId, children, nodeRegistry);
        nodeRegistry.put(id, node);
        return node;
    }

    private static class SimNode extends Node {
        private final Map<Integer, Node> registry;

        public SimNode(int id, Integer parent, List<Integer> children, Map<Integer, Node> registry) {
            super(id, parent, children);
            this.registry = registry;
        }

        @Override
        public void sendMessage(int toNodeId, String message) {
            Node target = registry.get(toNodeId);
            if (target != null) {
                target.receiveMessage(this.getId(), message);
            }
        }
    }
}
