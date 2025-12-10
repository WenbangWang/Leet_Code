package com.wwb.leetcode.other.openai.nodecluster;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Node Cluster - OpenAI Interview Question (3 Phases)
 * 
 * PROBLEM: Distributed tree aggregation using message passing
 * - Each node represents a machine in a cluster (tree structure)
 * - Nodes can only communicate with parent and children
 * - Implement COUNT (count total nodes) and TOPO (return tree structure)
 * - Handle network failures with retry (idempotency)
 * 
 * COMPLEXITY:
 * - Time: O(n) messages total, O(h) latency where h = tree height
 * - Space: O(d) per node where d = max(degree, concurrent_requests)
 * - Messages: 2n-1 for COUNT/TOPO (n requests down, n-1 responses up)
 * 
 * INTERVIEW TALKING POINTS:
 * 1. This is a distributed post-order traversal
 * 2. Pattern appears in MapReduce (aggregation phase)
 * 3. No global state - each node only knows parent/children
 * 4. Idempotency is critical for production systems
 */
public class Node {
    // Protocol message constants
    private static final String MSG_COUNT = "COUNT";
    private static final String MSG_COUNT_RESP = "COUNT_RESP";
    private static final String MSG_TOPO = "TOPO";
    private static final String MSG_TOPO_RESP = "TOPO_RESP";
    private static final String MSG_DELIMITER = ":";
    private static final String OUTPUT_TOTAL_MACHINES = "Total machines = ";
    private static final String OUTPUT_TOPOLOGY = "Topology = ";
    
    private final int id;
    private final Integer parent;  // null for root
    
    // OPTIMIZATION NOTE: Could use Set<Integer> for O(1) child lookup
    // List is fine for interview (small children count typically)
    private final List<Integer> children;

    // ==================== PHASE 3: IDEMPOTENCY ====================
    // TALKING POINT: "I use a Set to track request IDs I've already processed.
    // This ensures network retries don't cause double-counting."
    // 
    // KEY INSIGHT: Only deduplicate initial COUNT/TOPO requests, NOT responses
    // Why? Responses come from different children - they're all unique!
    private final Set<String> seenRequests;
    
    // ==================== PHASE 1: COUNT STATE ====================
    // Track partial counts from each child
    // Map<childId, subtreeCount>
    // TALKING POINT: "Once I've heard from all children, I sum and send to parent"
    private Map<Integer, Integer> nodeToCount;

    // ==================== PHASE 2: TOPO STATE ====================
    // Track partial topology strings from each child
    // Map<childId, subtreeTopologyString>
    // TALKING POINT: "Same aggregation pattern, just building strings instead of summing"
    private Map<Integer, String> nodeToTopo;

    public Node(int id, Integer parent, List<Integer> children) {
        this.id = id;
        this.parent = parent;
        this.children = children;
        this.seenRequests = new HashSet<>();
        this.nodeToCount = new HashMap<>();
        this.nodeToTopo = new HashMap<>();
    }

    /**
     * Provided by the system - DO NOT IMPLEMENT in interview
     * 
     * TALKING POINT: "This is an async message passing interface.
     * When I call sendMessage(childId, msg), the child's receiveMessage() 
     * will be invoked automatically."
     */
    public void sendMessage(int toNodeId, String message) {
        // Provided by system
    }

    /**
     * Main message handler - the heart of the distributed algorithm
     * 
     * MESSAGE PROTOCOL:
     * - "COUNT:reqId" - Request to count nodes
     * - "COUNT_RESP:reqId:count" - Response with subtree count
     * - "TOPO:reqId" - Request for topology
     * - "TOPO_RESP:reqId:structure" - Response with subtree structure
     * 
     * TALKING POINT: "I parse the message to determine type and route to the 
     * appropriate handler. This is a simple protocol but extensible."
     * 
     * @param fromNodeId - sender (null for external trigger at root)
     * @param msg - format "TYPE:reqId[:data]"
     */
    public void receiveMessage(Integer fromNodeId, String msg) {
        String[] parts = msg.split(MSG_DELIMITER);

        String type = parts[0];
        String reqId = parts[1];

        // ==================== PHASE 3: DEDUPLICATION ====================
        // CRITICAL: Only deduplicate initial requests, NOT responses!
        // 
        // TALKING POINT: "With network retries, I might receive the same 
        // COUNT request twice. I use seenRequests to process each reqId 
        // exactly once. Note that I don't deduplicate responses since each 
        // child's response is unique."
        // 
        // EDGE CASE: What if we need multiple concurrent COUNT requests?
        // FOLLOW-UP: Could use Map<reqId, state> instead of just Set<reqId>
        if (type.equals(MSG_COUNT) || type.equals(MSG_TOPO)) {
            if (!seenRequests.add(reqId)) {
                return;  // Already processed this request
            }
        }

        if (type.equals(MSG_COUNT)) {
            handleCount(fromNodeId, reqId);
        } else if (type.equals(MSG_COUNT_RESP)) {
            handleCountResp(fromNodeId, reqId, Integer.parseInt(parts[2]));
        } else if (type.equals(MSG_TOPO)) {
            handleTopo(fromNodeId, reqId);
        } else if (type.equals(MSG_TOPO_RESP)) {
            handleTopoResp(fromNodeId, reqId, parts[2]);
        }
    }

    /* ===========================================================
       ================== PHASE 1: COUNT HANDLING =================
       =========================================================== */
    
    /**
     * Handle COUNT request - count total nodes in subtree
     * 
     * ALGORITHM: Distributed post-order traversal
     * 1. Forward COUNT to all children
     * 2. Wait for COUNT_RESP from each child
     * 3. Sum all counts + 1 (self)
     * 4. Send result to parent (or print if root)
     * 
     * TALKING POINT: "This is post-order traversal in a distributed setting.
     * I can't recurse directly since I don't have references to child nodes,
     * so I use message passing. Each node waits for all children before 
     * aggregating - that's the post-order pattern."
     * 
     * TIME COMPLEXITY: O(n) total messages, O(h) latency
     * SPACE COMPLEXITY: O(d) where d = number of children
     * 
     * @param fromNodeId - should be parent (or null for root trigger)
     * @param reqId - unique request identifier for deduplication
     */
    private void handleCount(Integer fromNodeId, String reqId) {
        // SECURITY: Only accept COUNT from parent (or external if root)
        // TALKING POINT: "I validate the sender to prevent malicious children
        // from triggering spurious counts."
        if (fromNodeId != null && !fromNodeId.equals(this.parent)) {
            return;  // Ignore - not from parent
        }

        // Fresh request → reset state
        // TALKING POINT: "I reset the state for this new request. In a 
        // concurrent scenario, I'd use Map<reqId, Map<childId, count>>"
        nodeToCount = new HashMap<>();

        // EDGE CASE 1: Single-node tree (root with no children)
        if (parent == null && children.isEmpty()) {
            System.out.println(OUTPUT_TOTAL_MACHINES + "1");
            return;
        }

        // EDGE CASE 2: Leaf node
        // TALKING POINT: "Leaf nodes immediately respond with count=1"
        if (children.isEmpty()) {
            sendMessage(parent, MSG_COUNT_RESP + MSG_DELIMITER + reqId + MSG_DELIMITER + "1");
            return;
        }

        // Internal node: Forward COUNT to all children
        // TALKING POINT: "I broadcast to all children and wait for responses.
        // This is the 'fan-out' phase. The 'fan-in' happens in handleCountResp."
        for (int child : children) {
            sendMessage(child, MSG_COUNT + MSG_DELIMITER + reqId);
        }
    }

    /**
     * Handle COUNT_RESP from children - aggregate and propagate up
     * 
     * TALKING POINT: "This is the 'reduce' phase of MapReduce. I collect 
     * results from all children (fan-in), aggregate them, and send to parent.
     * I use putIfAbsent to handle duplicate responses gracefully."
     * 
     * OPTIMIZATION: Why putIfAbsent instead of put?
     * - Protects against processing duplicate responses from same child
     * - More defensive, better for production
     * 
     * @param fromNodeId - must be one of our children
     * @param reqId - request identifier
     * @param childCount - count from child's subtree
     */
    private void handleCountResp(Integer fromNodeId, String reqId, int childCount) {
        // SECURITY: Validate sender is actually our child
        // TALKING POINT: "I validate responses come from legitimate children,
        // preventing malicious nodes from corrupting our count."
        if (fromNodeId == null || !children.contains(fromNodeId)) {
            return;  // Ignore invalid response
        }

        // Record this child's count (once - putIfAbsent handles duplicates)
        nodeToCount.putIfAbsent(fromNodeId, childCount);

        // AGGREGATION: Wait until we've heard from ALL children
        // TALKING POINT: "This is the synchronization point. I wait for all
        // children before computing the final result. This ensures correctness."
        if (nodeToCount.size() == children.size()) {
            // Sum all children counts + 1 (for self)
            int total = 1 + nodeToCount.values().stream().mapToInt(Integer::intValue).sum();

            // EDGE CASE: Root node prints result
            if (parent == null) {
                System.out.println(OUTPUT_TOTAL_MACHINES + total);
            } else {
                // Propagate result up to parent
                sendMessage(parent, MSG_COUNT_RESP + MSG_DELIMITER + reqId + MSG_DELIMITER + total);
            }
            
            // Optional cleanup (not critical for interview)
            // nodeToCount.clear();
        }
    }

    /* ===========================================================
       =============== PHASE 2: TOPOLOGY HANDLING =================
       =========================================================== */
    
    /**
     * Handle TOPO request - build tree structure string
     * 
     * TALKING POINT: "Phase 2 uses the exact same message passing pattern
     * as COUNT. The only difference is we're building strings instead of
     * summing integers. This shows the power of the aggregation abstraction."
     * 
     * OUTPUT FORMAT: "id(child1,child2,...)"
     * Example: Tree with root 1, children 2,3,4 where 2 has children 5,6
     *          Result: "1(2(5,6),3,4)"
     * 
     * ALGORITHM: Same post-order traversal as COUNT
     * 1. Forward TOPO to all children
     * 2. Wait for TOPO_RESP from each child
     * 3. Build structure: id(child1_topo, child2_topo, ...)
     * 4. Send to parent (or print if root)
     * 
     * @param fromNodeId - should be parent (or null for root)
     * @param reqId - unique request identifier
     */
    private void handleTopo(Integer fromNodeId, String reqId) {
        // Sender must be parent (or null for external root trigger)
        if (fromNodeId != null && !fromNodeId.equals(this.parent)) {
            return;
        }

        // Fresh request → reset state
        nodeToTopo = new HashMap<>();

        // EDGE CASE 1: Single node tree
        if (parent == null && children.isEmpty()) {
            System.out.println(OUTPUT_TOPOLOGY + id);
            return;
        }

        // EDGE CASE 2: Leaf node - just return own ID
        if (children.isEmpty()) {
            sendMessage(parent, MSG_TOPO_RESP + MSG_DELIMITER + reqId + MSG_DELIMITER + id);
            return;
        }

        // Internal node: Forward to all children
        for (int child : children) {
            sendMessage(child, MSG_TOPO + MSG_DELIMITER + reqId);
        }
    }

    /**
     * Handle TOPO_RESP from children - aggregate topology strings
     * 
     * TALKING POINT: "This is identical to handleCountResp, but instead of
     * summing integers, I'm concatenating strings. The pattern is the same:
     * collect from all children, aggregate, send to parent."
     * 
     * STRING BUILDING: id(child1_topo, child2_topo, ...)
     * 
     * OPTIMIZATION OPPORTUNITY: If children order matters, could sort by ID
     * Current: Arbitrary order (HashMap iteration)
     * Better: String.join(", ", nodeToTopo.entrySet().stream()
     *                              .sorted(Map.Entry.comparingByKey())
     *                              .map(Map.Entry::getValue).toList())
     * 
     * @param fromNodeId - must be one of our children
     * @param reqId - request identifier
     * @param subtree - topology string from child's subtree
     */
    private void handleTopoResp(Integer fromNodeId, String reqId, String subtree) {
        // Validate sender is our child
        if (fromNodeId == null || !children.contains(fromNodeId)) {
            return;
        }

        // Record this child's topology (deduplicate with putIfAbsent)
        nodeToTopo.putIfAbsent(fromNodeId, subtree);

        // Wait for all children to respond
        if (nodeToTopo.size() == children.size()) {
            // Build topology string: id(child1, child2, ...)
            // TALKING POINT: "I concatenate all child topologies with my ID.
            // This builds the tree structure bottom-up."
            
            String result = id +
                "(" +
                String.join(", ", nodeToTopo.values().stream().toList()) +
                ")";

            if (parent == null) {
                System.out.println(OUTPUT_TOPOLOGY + result);
            } else {
                sendMessage(parent, MSG_TOPO_RESP + MSG_DELIMITER + reqId + MSG_DELIMITER + result);
            }
            
            // Optional cleanup
            // nodeToTopo.clear();
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
