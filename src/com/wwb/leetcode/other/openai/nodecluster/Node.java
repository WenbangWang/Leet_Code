package com.wwb.leetcode.other.openai.nodecluster;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Node {
    private final int id;
    private final Integer parent;
    // can be a set for optimizing
    private final List<Integer> children;

    private final Set<String> seenRequests;
    // For COUNT responses
    private Map<Integer, Integer> nodeToCount;

    // For TOPO responses
    private Map<Integer, String> nodeToTopo;

    public Node(int id, Integer parent, List<Integer> children) {
        this.id = id;
        this.parent = parent;
        this.children = children;
        this.seenRequests = new HashSet<>();
        this.nodeToCount = new HashMap<>();
        this.nodeToTopo = new HashMap<>();
    }

    public void sendMessage(int toNodeId, String message) {
        // Provided by system
    }

    public void receiveMessage(Integer fromNodeId, String msg) {
        String[] parts = msg.split(":");

        String type = parts[0];
        String reqId = parts[1];

        // Only COUNT and TOPO trigger new-tree-work at this node
        if (type.equals("COUNT") || type.equals("TOPO")) {
            if (!seenRequests.add(reqId)) {
                return;
            }
        }

        if (type.equals("COUNT")) {
            handleCount(fromNodeId, reqId);
        } else if (type.equals("COUNT_RESP")) {
            handleCountResp(fromNodeId, reqId, Integer.parseInt(parts[2]));
        } else if (type.equals("TOPO")) {
            handleTopo(fromNodeId, reqId);
        } else if (type.equals("TOPO_RESP")) {
            handleTopoResp(fromNodeId, reqId, parts[2]);
        }
    }

    private void handleCount(Integer fromNodeId, String reqId) {
        // Initial external root trigger (fromNodeId == null) is allowed
        if (fromNodeId != null && !fromNodeId.equals(this.parent)) {
            return;
        }

        // Fresh reqId → reset state
        nodeToCount = new HashMap<>();

        // Single-node tree
        if (parent == null && children.isEmpty()) {
            System.out.println("Total machines = 1");
            return;
        }

        // Leaf
        if (children.isEmpty()) {
            sendMessage(parent, "COUNT_RESP:" + reqId + ":1");
            return;
        }

        // Forward to children
        for (int child : children) {
            sendMessage(child, "COUNT:" + reqId);
        }
    }

    private void handleCountResp(Integer fromNodeId, String reqId, int childCount) {

        // Response must come from a child
        if (fromNodeId == null || !children.contains(fromNodeId)) {
            return;
        }

        // Record once
        nodeToCount.putIfAbsent(fromNodeId, childCount);

        if (nodeToCount.size() == children.size()) {
            int total = 1 + nodeToCount.values().stream().mapToInt(Integer::intValue).sum();

            if (parent == null) {
                System.out.println("Total machines = " + total);
            } else {
                sendMessage(parent, "COUNT_RESP:" + reqId + ":" + total);
            }
        }
    }

        /* ===========================================================
       ================== TOPOLOGY HANDLING =======================
       =========================================================== */

    private void handleTopo(Integer fromNodeId, String reqId) {
        // Sender must be parent unless root
        if (fromNodeId != null && !fromNodeId.equals(this.parent)) {
            return;
        }

        // New request → reset state
        nodeToTopo = new HashMap<>();

        // Single node tree
        if (parent == null && children.isEmpty()) {
            System.out.println("Topology = " + id);
            return;
        }

        // Leaf
        if (children.isEmpty()) {
            sendMessage(parent, "TOPO_RESP:" + reqId + ":" + id);
            return;
        }

        // Forward TOPO to children
        for (int child : children) {
            sendMessage(child, "TOPO:" + reqId);
        }
    }

    private void handleTopoResp(Integer fromNodeId, String reqId, String subtree) {
        // Must come from a child
        if (fromNodeId == null || !children.contains(fromNodeId)) {
            return;
        }

        nodeToTopo.putIfAbsent(fromNodeId, subtree);

        if (nodeToTopo.size() == children.size()) {

            // Build topology string: id(child1,child2,...)

            String result = id +
                "(" +
                String.join(", ", nodeToTopo.values().stream().toList()) +
                ")";

            if (parent == null) {
                System.out.println("Topology = " + result);
            } else {
                sendMessage(parent, "TOPO_RESP:" + reqId + ":" + result);
            }
        }
    }

    public int getId() {
        return id;
    }

    public Integer getParent() {
        return parent;
    }

    public List<Integer> getChildren() {
        return children;
    }
}
