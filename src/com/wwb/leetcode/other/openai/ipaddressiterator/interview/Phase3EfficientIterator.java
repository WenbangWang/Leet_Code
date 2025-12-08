package com.wwb.leetcode.other.openai.ipaddressiterator.interview;

import com.wwb.leetcode.other.openai.ipaddressiterator.IpHelper;
import java.util.*;

/**
 * Phase 3: Memory-Efficient Iterator with Advanced Operations
 * 
 * Extends Phase 2 with memory optimization for massive IP ranges (e.g., entire IPv4 space)
 * and adds advanced operations: skip(n), peek(), count()
 * 
 * Key Optimization:
 * - Instead of storing every IP, store compressed segments
 * - Track current position as (segmentIndex, offsetInSegment)
 * - Memory: O(s) where s = number of segments, NOT O(total IPs)
 * 
 * For example:
 * - Range 0.0.0.0/0 (4.3 billion IPs) = 1 segment = O(1) space!
 * - vs naive approach: 4.3 billion String objects = ~34 GB memory
 * 
 * New Operations:
 * - skip(n): Skip next n IPs in O(s) time (vs iterating n times)
 * - peek(): See next IP without consuming it in O(1) time
 * - count(): Get remaining IP count in O(s) time (vs iterating all)
 * 
 * Time Complexity:
 * - Construction: O(n log n + m log m)
 * - hasNext(): O(1)
 * - next(): O(1) amortized
 * - skip(n): O(s) worst case, O(1) amortized
 * - peek(): O(1)
 * - count(): O(s)
 * 
 * Space Complexity: O(s) where s = number of segments
 */
public class Phase3EfficientIterator implements Iterator<String> {
    private final List<Segment> segments;
    private int currentSegmentIndex;
    private long currentOffset; // Offset within current segment

    /**
     * Create a memory-efficient iterator over multiple IP ranges with exclusions
     * 
     * @param includeCidrs List of CIDR blocks to include
     * @param excludeCidrs List of CIDR blocks to exclude
     */
    public Phase3EfficientIterator(List<String> includeCidrs, List<String> excludeCidrs) {
        if (includeCidrs == null || includeCidrs.isEmpty()) {
            this.segments = new ArrayList<>();
        } else {
            List<Segment> includeSegments = parseCIDRs(includeCidrs);
            List<Segment> excludeSegments = excludeCidrs != null ? parseCIDRs(excludeCidrs) : new ArrayList<>();
            List<Segment> mergedIncludes = mergeSegments(includeSegments);
            this.segments = subtractSegments(mergedIncludes, excludeSegments);
        }

        this.currentSegmentIndex = 0;
        this.currentOffset = 0;
    }

    // ============================================================================
    // Core Iterator Methods
    // ============================================================================

    @Override
    public boolean hasNext() {
        return currentSegmentIndex < segments.size();
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more IP addresses");
        }

        Segment current = segments.get(currentSegmentIndex);
        String result = IpHelper.longToIp(current.start + currentOffset);

        // Advance position
        currentOffset++;
        if (currentOffset >= current.size()) {
            currentSegmentIndex++;
            currentOffset = 0;
        }

        return result;
    }

    // ============================================================================
    // Advanced Operations (New in Phase 3)
    // ============================================================================

    /**
     * Skip the next n IP addresses without returning them.
     * Much faster than calling next() n times for large n.
     * 
     * Time: O(s) worst case where s = segments to skip through
     * Space: O(1)
     * 
     * @param n Number of IPs to skip
     */
    public void skip(long n) {
        if (n <= 0) return;

        while (n > 0 && currentSegmentIndex < segments.size()) {
            Segment current = segments.get(currentSegmentIndex);
            long remainingInSegment = current.size() - currentOffset;

            if (n < remainingInSegment) {
                // Skip within current segment
                currentOffset += n;
                break;
            } else {
                // Skip entire remaining part of current segment
                n -= remainingInSegment;
                currentSegmentIndex++;
                currentOffset = 0;
            }
        }
    }

    /**
     * Peek at the next IP address without consuming it.
     * 
     * Time: O(1)
     * Space: O(1)
     * 
     * @return Next IP address, or null if no more IPs
     */
    public String peek() {
        if (!hasNext()) {
            return null;
        }

        Segment current = segments.get(currentSegmentIndex);
        return IpHelper.longToIp(current.start + currentOffset);
    }

    /**
     * Count remaining IP addresses without iterating through them.
     * 
     * Time: O(s) where s = remaining segments
     * Space: O(1)
     * 
     * @return Number of remaining IP addresses
     */
    public long count() {
        long total = 0;

        for (int i = currentSegmentIndex; i < segments.size(); i++) {
            Segment seg = segments.get(i);
            if (i == currentSegmentIndex) {
                // Partial segment
                total += seg.size() - currentOffset;
            } else {
                // Full segment
                total += seg.size();
            }
        }

        return total;
    }

    /**
     * Reset iterator to beginning
     */
    public void reset() {
        currentSegmentIndex = 0;
        currentOffset = 0;
    }

    /**
     * Get current position as a percentage (0.0 to 1.0)
     * Useful for progress tracking
     */
    public double progress() {
        long total = totalIPs();
        if (total == 0) return 1.0;
        
        long consumed = total - count();
        return (double) consumed / total;
    }

    // ============================================================================
    // Helper Methods (same as Phase 2)
    // ============================================================================

    private List<Segment> parseCIDRs(List<String> cidrs) {
        List<Segment> result = new ArrayList<>();
        for (String cidr : cidrs) {
            result.add(IPRange.fromCIDR(cidr));
        }
        return result;
    }

    private List<Segment> mergeSegments(List<Segment> segments) {
        if (segments.isEmpty()) return new ArrayList<>();

        List<Segment> sorted = new ArrayList<>(segments);
        Collections.sort(sorted);

        List<Segment> merged = new ArrayList<>();
        Segment current = sorted.get(0);

        for (int i = 1; i < sorted.size(); i++) {
            Segment next = sorted.get(i);
            if (current.canMergeWith(next)) {
                current = current.merge(next);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);

        return merged;
    }

    /**
     * Two-Pointer optimized segment subtraction
     * Same algorithm as Phase 2, but noted here for Phase 3
     */
    private List<Segment> subtractSegments(List<Segment> includes, List<Segment> excludes) {
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

    public long totalIPs() {
        long total = 0;
        for (Segment seg : segments) {
            total += seg.size();
        }
        return total;
    }

    public int segmentCount() {
        return segments.size();
    }

    // ============================================================================
    // Test Cases
    // ============================================================================

    public static void main(String[] args) {
        System.out.println("=== Phase 3: Memory-Efficient Iterator ===\n");

        // Test 1: Basic skip operation
        System.out.println("Test 1: Skip operation");
        List<String> inc1 = Arrays.asList("10.0.0.0/28"); // 16 IPs
        Phase3EfficientIterator iter1 = new Phase3EfficientIterator(inc1, null);
        System.out.println("  First IP: " + iter1.peek());
        iter1.skip(5);
        System.out.println("  After skip(5): " + iter1.peek());
        assert iter1.peek().equals("10.0.0.5") : "Should be at 10.0.0.5";
        System.out.println("  Remaining: " + iter1.count());
        assert iter1.count() == 11 : "Should have 11 remaining (16 - 5)";
        System.out.println("✓ Passed\n");

        // Test 2: Peek doesn't consume
        System.out.println("Test 2: Peek doesn't consume");
        List<String> inc2 = Arrays.asList("192.168.1.0/30"); // 4 IPs
        Phase3EfficientIterator iter2 = new Phase3EfficientIterator(inc2, null);
        String first = iter2.peek();
        String second = iter2.peek();
        assert first.equals(second) : "Peek should return same value";
        String actual = iter2.next();
        assert first.equals(actual) : "Next should return what peek showed";
        System.out.println("  Peek and next consistent: " + first);
        System.out.println("✓ Passed\n");

        // Test 3: Count operation
        System.out.println("Test 3: Count operation");
        List<String> inc3 = Arrays.asList("192.168.1.0/24"); // 256 IPs
        Phase3EfficientIterator iter3 = new Phase3EfficientIterator(inc3, null);
        System.out.println("  Initial count: " + iter3.count());
        assert iter3.count() == 256 : "Should have 256 IPs";
        iter3.next(); iter3.next(); iter3.next();
        System.out.println("  After 3 iterations: " + iter3.count());
        assert iter3.count() == 253 : "Should have 253 remaining";
        System.out.println("✓ Passed\n");

        // Test 4: Skip across multiple segments
        System.out.println("Test 4: Skip across segments");
        List<String> inc4 = Arrays.asList("10.0.0.0/30", "10.0.1.0/30"); // 4 + 4 = 8 IPs
        List<String> exc4 = Arrays.asList("10.0.0.2/31"); // Exclude 2 from first segment
        Phase3EfficientIterator iter4 = new Phase3EfficientIterator(inc4, exc4);
        System.out.println("  Total IPs: " + iter4.totalIPs());
        System.out.println("  Segments: " + iter4.segmentCount());
        assert iter4.totalIPs() == 6 : "Should have 6 IPs total";
        
        iter4.skip(3); // Skip through first segment into second
        System.out.println("  After skip(3): " + iter4.peek());
        System.out.println("  Remaining: " + iter4.count());
        assert iter4.count() == 3 : "Should have 3 remaining";
        System.out.println("✓ Passed\n");

        // Test 5: Memory efficiency demonstration
        System.out.println("Test 5: Memory efficiency - Large range");
        List<String> inc5 = Arrays.asList("10.0.0.0/8"); // 16,777,216 IPs!
        Phase3EfficientIterator iter5 = new Phase3EfficientIterator(inc5, null);
        System.out.println("  Total IPs: " + iter5.count() + " (16M+)");
        System.out.println("  Segments: " + iter5.segmentCount());
        System.out.println("  Memory: O(" + iter5.segmentCount() + ") segments, NOT O(16M) IPs!");
        assert iter5.segmentCount() == 1 : "Should be just 1 segment";
        
        // Can efficiently skip millions
        iter5.skip(1_000_000);
        System.out.println("  After skip(1M): " + iter5.peek());
        System.out.println("  Remaining: " + iter5.count());
        assert iter5.count() == 16_777_216 - 1_000_000 : "Count should be accurate";
        System.out.println("✓ Passed\n");

        // Test 6: Progress tracking
        System.out.println("Test 6: Progress tracking");
        List<String> inc6 = Arrays.asList("192.168.1.0/28"); // 16 IPs
        Phase3EfficientIterator iter6 = new Phase3EfficientIterator(inc6, null);
        System.out.println("  Initial progress: " + String.format("%.0f%%", iter6.progress() * 100));
        iter6.skip(8);
        System.out.println("  After skip(8): " + String.format("%.0f%%", iter6.progress() * 100));
        assert Math.abs(iter6.progress() - 0.5) < 0.01 : "Should be at 50%";
        iter6.skip(8);
        System.out.println("  After skip(8) more: " + String.format("%.0f%%", iter6.progress() * 100));
        assert Math.abs(iter6.progress() - 1.0) < 0.01 : "Should be at 100%";
        System.out.println("✓ Passed\n");

        // Test 7: Reset functionality
        System.out.println("Test 7: Reset functionality");
        List<String> inc7 = Arrays.asList("10.0.0.0/30");
        Phase3EfficientIterator iter7 = new Phase3EfficientIterator(inc7, null);
        String first7 = iter7.next();
        iter7.next();
        iter7.next();
        System.out.println("  After consuming 3 IPs, remaining: " + iter7.count());
        iter7.reset();
        System.out.println("  After reset, remaining: " + iter7.count());
        assert iter7.count() == 4 : "Should be back to 4";
        String afterReset = iter7.next();
        assert first7.equals(afterReset) : "Should restart from beginning";
        System.out.println("✓ Passed\n");

        // Test 8: Edge case - skip more than available
        System.out.println("Test 8: Skip more than available");
        List<String> inc8 = Arrays.asList("10.0.0.0/30"); // 4 IPs
        Phase3EfficientIterator iter8 = new Phase3EfficientIterator(inc8, null);
        iter8.skip(100); // Try to skip more than available
        System.out.println("  After skip(100) with only 4 IPs:");
        System.out.println("  HasNext: " + iter8.hasNext());
        assert !iter8.hasNext() : "Should have no more IPs";
        assert iter8.count() == 0 : "Count should be 0";
        System.out.println("✓ Passed\n");

        System.out.println("=== All Phase 3 Tests Passed! ===");
        System.out.println("\nKey Takeaway: Memory usage is O(segments), NOT O(total IPs)!");
        System.out.println("This allows iteration over billions of IPs with minimal memory.");
    }
}

