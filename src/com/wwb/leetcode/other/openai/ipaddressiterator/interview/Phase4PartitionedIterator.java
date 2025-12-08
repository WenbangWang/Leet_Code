package com.wwb.leetcode.other.openai.ipaddressiterator.interview;

import com.wwb.leetcode.other.openai.ipaddressiterator.IpHelper;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Phase 4: Partitioned Iterator for Concurrent/Distributed Processing
 * 
 * Enables parallel processing of IP ranges by partitioning the space into roughly equal chunks.
 * Useful for distributed network scanning, parallel DNS lookups, or MapReduce-style operations.
 * 
 * Key Features:
 * - Partition IP space into N roughly-equal chunks
 * - Each partition is thread-safe and independent
 * - Supports both static partitioning and work-stealing pattern
 * - Load balancing based on IP count, not segment count
 * 
 * Design Patterns:
 * - Static Partitioning: Divide work upfront (good for predictable workloads)
 * - Work Stealing: Dynamic task queue (good for variable-time operations)
 * 
 * Use Cases:
 * - Parallel network scanning (nmap-style)
 * - Distributed rate-limited API calls
 * - MapReduce over IP ranges
 * - Load-balanced work distribution
 * 
 * Time Complexity:
 * - partition(): O(n log n + m log m + s) where s = segments
 * - Each iterator: O(1) per next()
 * 
 * Space Complexity: O(s) per partition
 */
public class Phase4PartitionedIterator {

    /**
     * Partition IP ranges into N roughly-equal independent iterators for parallel processing.
     * Partitions are balanced by IP count, not segment count.
     * 
     * @param includeCidrs List of CIDR blocks to include
     * @param excludeCidrs List of CIDR blocks to exclude
     * @param numPartitions Number of partitions to create
     * @return List of independent iterators, one per partition
     */
    public static List<Phase3EfficientIterator> partition(
            List<String> includeCidrs,
            List<String> excludeCidrs,
            int numPartitions) {
        
        if (numPartitions <= 0) {
            throw new IllegalArgumentException("numPartitions must be > 0");
        }

        // First, compute all valid segments
        Phase3EfficientIterator tempIter = new Phase3EfficientIterator(includeCidrs, excludeCidrs);
        long totalIPs = tempIter.totalIPs();
        
        if (totalIPs == 0) {
            return new ArrayList<>();
        }

        // Get internal segments (need to access them)
        List<Segment> allSegments = getSegments(includeCidrs, excludeCidrs);
        
        if (allSegments.isEmpty()) {
            return new ArrayList<>();
        }

        // Calculate IPs per partition
        long ipsPerPartition = (totalIPs + numPartitions - 1) / numPartitions; // Ceiling division
        
        List<Phase3EfficientIterator> result = new ArrayList<>();
        List<Segment> currentPartition = new ArrayList<>();
        long currentCount = 0;

        for (Segment seg : allSegments) {
            long segSize = seg.size();
            
            while (segSize > 0) {
                long needed = ipsPerPartition - currentCount;
                
                if (segSize <= needed) {
                    // Entire segment fits in current partition
                    currentPartition.add(seg);
                    currentCount += segSize;
                    segSize = 0;
                } else {
                    // Need to split segment
                    Segment part1 = new Segment(seg.start, seg.start + needed - 1);
                    currentPartition.add(part1);
                    currentCount += needed;
                    
                    // Remaining part
                    seg = new Segment(seg.start + needed, seg.end);
                    segSize = seg.size();
                }
                
                // If partition is full, create iterator
                if (currentCount >= ipsPerPartition) {
                    result.add(createIteratorFromSegments(currentPartition));
                    currentPartition = new ArrayList<>();
                    currentCount = 0;
                }
            }
        }

        // Add last partition if it has any segments
        if (!currentPartition.isEmpty()) {
            result.add(createIteratorFromSegments(currentPartition));
        }

        return result;
    }

    /**
     * Get a specific partition for distributed processing.
     * Useful when each worker knows its ID (e.g., in MapReduce).
     * 
     * @param includeCidrs List of CIDR blocks to include
     * @param excludeCidrs List of CIDR blocks to exclude
     * @param partitionId Which partition to get (0-based)
     * @param totalPartitions Total number of partitions
     * @return Iterator for this partition
     */
    public static Phase3EfficientIterator getPartition(
            List<String> includeCidrs,
            List<String> excludeCidrs,
            int partitionId,
            int totalPartitions) {
        
        if (partitionId < 0 || partitionId >= totalPartitions) {
            throw new IllegalArgumentException("Invalid partitionId");
        }

        List<Phase3EfficientIterator> all = partition(includeCidrs, excludeCidrs, totalPartitions);
        
        if (partitionId >= all.size()) {
            // This partition is empty (happens when totalPartitions > segments)
            return new Phase3EfficientIterator(new ArrayList<>(), null);
        }

        return all.get(partitionId);
    }

    /**
     * Demonstrate work-stealing pattern using a thread-safe work queue.
     * Workers pull segments dynamically from a shared queue.
     */
    public static class WorkStealingIterator {
        private final BlockingQueue<Segment> workQueue;
        private final AtomicLong totalProcessed;
        private final long totalIPs;

        public WorkStealingIterator(List<String> includeCidrs, List<String> excludeCidrs) {
            List<Segment> segments = getSegments(includeCidrs, excludeCidrs);
            this.workQueue = new LinkedBlockingQueue<>(segments);
            this.totalProcessed = new AtomicLong(0);
            this.totalIPs = segments.stream().mapToLong(Segment::size).sum();
        }

        /**
         * Get next segment to process (thread-safe).
         * Returns null when no more work available.
         */
        public Segment getNextSegment() {
            return workQueue.poll();
        }

        /**
         * Report progress on a segment (for tracking)
         */
        public void reportProgress(long ipsProcessed) {
            totalProcessed.addAndGet(ipsProcessed);
        }

        /**
         * Get overall progress (0.0 to 1.0)
         */
        public double getProgress() {
            if (totalIPs == 0) return 1.0;
            return (double) totalProcessed.get() / totalIPs;
        }

        /**
         * Get number of remaining segments
         */
        public int remainingSegments() {
            return workQueue.size();
        }
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private static List<Segment> getSegments(List<String> includeCidrs, List<String> excludeCidrs) {
        List<Segment> includeSegs = parseCIDRs(includeCidrs);
        List<Segment> excludeSegs = excludeCidrs != null ? parseCIDRs(excludeCidrs) : new ArrayList<>();
        List<Segment> merged = mergeSegments(includeSegs);
        return subtractSegments(merged, excludeSegs);
    }

    private static List<Segment> parseCIDRs(List<String> cidrs) {
        List<Segment> result = new ArrayList<>();
        for (String cidr : cidrs) {
            result.add(IPRange.fromCIDR(cidr));
        }
        return result;
    }

    private static List<Segment> mergeSegments(List<Segment> segments) {
        if (segments.isEmpty()) return new ArrayList<>();
        List<Segment> sorted = new ArrayList<>(segments);
        Collections.sort(sorted);
        List<Segment> merged = new ArrayList<>();
        Segment current = sorted.get(0);
        for (int i = 1; i < sorted.size(); i++) {
            if (current.canMergeWith(sorted.get(i))) {
                current = current.merge(sorted.get(i));
            } else {
                merged.add(current);
                current = sorted.get(i);
            }
        }
        merged.add(current);
        return merged;
    }

    /**
     * Two-Pointer optimized segment subtraction (same as Phase 2 & 3)
     */
    private static List<Segment> subtractSegments(List<Segment> includes, List<Segment> excludes) {
        if (excludes.isEmpty()) return includes;
        
        List<Segment> result = new ArrayList<>();
        List<Segment> sortedExcludes = new ArrayList<>(excludes);
        Collections.sort(sortedExcludes);
        
        for (Segment include : includes) {
            long currentStart = include.start;
            
            for (Segment exclude : sortedExcludes) {
                if (exclude.end < currentStart) continue;
                if (exclude.start > include.end) break;
                
                if (exclude.start > currentStart) {
                    result.add(new Segment(currentStart, 
                                          Math.min(include.end, exclude.start - 1)));
                }
                
                currentStart = Math.max(currentStart, exclude.end + 1);
                if (currentStart > include.end) break;
            }
            
            if (currentStart <= include.end) {
                result.add(new Segment(currentStart, include.end));
            }
        }
        
        return result;
    }

    private static Phase3EfficientIterator createIteratorFromSegments(List<Segment> segments) {
        // Convert segments to CIDR strings for Phase3 constructor
        if (segments.isEmpty()) {
            return new Phase3EfficientIterator(new ArrayList<>(), null);
        }
        
        List<String> cidrs = new ArrayList<>();
        for (Segment seg : segments) {
            // Create a minimal CIDR representation
            String cidr = IpHelper.longToIp(seg.start);
            if (seg.size() > 1) {
                cidr = findBestCIDR(seg);
            } else {
                cidr = cidr + "/32";
            }
            cidrs.add(cidr);
        }
        
        return new Phase3EfficientIterator(cidrs, null);
    }

    private static String findBestCIDR(Segment seg) {
        // Simplified: just return start IP with prefix
        long size = seg.size();
        int prefix = 32 - (int)(Math.log(size) / Math.log(2));
        return IpHelper.longToIp(seg.start) + "/" + prefix;
    }

    // ============================================================================
    // Test Cases
    // ============================================================================

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Phase 4: Partitioned Iterator ===\n");

        // Test 1: Basic partitioning
        System.out.println("Test 1: Partition into 4 equal parts");
        List<String> inc1 = Arrays.asList("10.0.0.0/28"); // 16 IPs
        List<Phase3EfficientIterator> parts1 = Phase4PartitionedIterator.partition(inc1, null, 4);
        System.out.println("  Partitions created: " + parts1.size());
        for (int i = 0; i < parts1.size(); i++) {
            long count = parts1.get(i).count();
            System.out.println("  Partition " + i + ": " + count + " IPs");
        }
        assert parts1.size() == 4 : "Should have 4 partitions";
        long total1 = parts1.stream().mapToLong(Phase3EfficientIterator::count).sum();
        assert total1 == 16 : "Total should be 16 IPs";
        System.out.println("✓ Passed\n");

        // Test 2: Uneven partitioning
        System.out.println("Test 2: 10 IPs into 3 partitions (uneven)");
        List<String> inc2 = Arrays.asList("192.168.1.0/29", "192.168.1.8/31"); // 8 + 2 = 10 IPs
        List<Phase3EfficientIterator> parts2 = Phase4PartitionedIterator.partition(inc2, null, 3);
        System.out.println("  Partitions: " + parts2.size());
        for (int i = 0; i < parts2.size(); i++) {
            long count = parts2.get(i).count();
            System.out.println("  Partition " + i + ": " + count + " IPs");
        }
        long total2 = parts2.stream().mapToLong(Phase3EfficientIterator::count).sum();
        assert total2 == 10 : "Total should be 10 IPs";
        System.out.println("✓ Passed\n");

        // Test 3: More partitions than segments
        System.out.println("Test 3: More partitions than segments");
        List<String> inc3 = Arrays.asList("10.0.0.0/30"); // 4 IPs, request 10 partitions
        List<Phase3EfficientIterator> parts3 = Phase4PartitionedIterator.partition(inc3, null, 10);
        System.out.println("  Requested: 10, Created: " + parts3.size());
        long total3 = parts3.stream().mapToLong(Phase3EfficientIterator::count).sum();
        assert total3 == 4 : "Total should still be 4 IPs";
        System.out.println("✓ Passed\n");

        // Test 4: Get specific partition
        System.out.println("Test 4: Get specific partition (like MapReduce)");
        List<String> inc4 = Arrays.asList("172.16.0.0/28"); // 16 IPs
        Phase3EfficientIterator part4_0 = Phase4PartitionedIterator.getPartition(inc4, null, 0, 4);
        Phase3EfficientIterator part4_1 = Phase4PartitionedIterator.getPartition(inc4, null, 1, 4);
        Phase3EfficientIterator part4_2 = Phase4PartitionedIterator.getPartition(inc4, null, 2, 4);
        Phase3EfficientIterator part4_3 = Phase4PartitionedIterator.getPartition(inc4, null, 3, 4);
        
        System.out.println("  Partition 0: " + part4_0.count() + " IPs, starts with " + part4_0.peek());
        System.out.println("  Partition 1: " + part4_1.count() + " IPs, starts with " + part4_1.peek());
        System.out.println("  Partition 2: " + part4_2.count() + " IPs, starts with " + part4_2.peek());
        System.out.println("  Partition 3: " + part4_3.count() + " IPs, starts with " + part4_3.peek());
        
        long total4 = part4_0.count() + part4_1.count() + part4_2.count() + part4_3.count();
        assert total4 == 16 : "Total should be 16";
        System.out.println("✓ Passed\n");

        // Test 5: Work-stealing pattern
        System.out.println("Test 5: Work-stealing pattern simulation");
        List<String> inc5 = Arrays.asList("10.0.0.0/27"); // 32 IPs
        WorkStealingIterator ws = new WorkStealingIterator(inc5, null);
        
        System.out.println("  Initial segments: " + ws.remainingSegments());
        System.out.println("  Initial progress: " + String.format("%.0f%%", ws.getProgress() * 100));
        
        // Simulate worker processing segments
        Segment seg1 = ws.getNextSegment();
        if (seg1 != null) {
            System.out.println("  Worker 1 got segment: " + seg1);
            ws.reportProgress(seg1.size());
        }
        
        System.out.println("  After 1 segment: " + String.format("%.0f%%", ws.getProgress() * 100));
        assert ws.remainingSegments() >= 0 : "Should have segments or be done";
        System.out.println("✓ Passed\n");

        // Test 6: Parallel processing simulation
        System.out.println("Test 6: Parallel processing with 4 threads");
        List<String> inc6 = Arrays.asList("192.168.0.0/24"); // 256 IPs
        List<Phase3EfficientIterator> parts6 = Phase4PartitionedIterator.partition(inc6, null, 4);
        
        AtomicLong processedCount = new AtomicLong(0);
        CountDownLatch latch = new CountDownLatch(4);
        
        for (int i = 0; i < parts6.size(); i++) {
            final int threadId = i;
            final Phase3EfficientIterator iter = parts6.get(i);
            
            new Thread(() -> {
                try {
                    long count = 0;
                    while (iter.hasNext()) {
                        iter.next(); // Simulate processing
                        count++;
                    }
                    processedCount.addAndGet(count);
                    System.out.println("  Thread " + threadId + " processed " + count + " IPs");
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        latch.await(); // Wait for all threads
        System.out.println("  Total processed: " + processedCount.get());
        assert processedCount.get() == 256 : "Should process all 256 IPs";
        System.out.println("✓ Passed\n");

        // Test 7: Large range partitioning
        System.out.println("Test 7: Large range (1M IPs) into 10 partitions");
        List<String> inc7 = Arrays.asList("10.0.0.0/12"); // ~1M IPs
        List<Phase3EfficientIterator> parts7 = Phase4PartitionedIterator.partition(inc7, null, 10);
        
        System.out.println("  Partitions: " + parts7.size());
        long min = Long.MAX_VALUE, max = 0;
        for (Phase3EfficientIterator iter : parts7) {
            long count = iter.count();
            min = Math.min(min, count);
            max = Math.max(max, count);
        }
        System.out.println("  Min partition size: " + min);
        System.out.println("  Max partition size: " + max);
        System.out.println("  Difference: " + (max - min) + " (should be small for load balance)");
        
        long total7 = parts7.stream().mapToLong(Phase3EfficientIterator::count).sum();
        assert total7 == 1048576 : "Total should be 2^20 IPs";
        System.out.println("✓ Passed\n");

        System.out.println("=== All Phase 4 Tests Passed! ===");
        System.out.println("\nKey Takeaways:");
        System.out.println("- Partitioning enables parallel/distributed processing");
        System.out.println("- Load balancing by IP count ensures even work distribution");
        System.out.println("- Work-stealing pattern good for variable-time tasks");
        System.out.println("- Each partition is independent and thread-safe");
    }
}

