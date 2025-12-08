package com.wwb.leetcode.other.openai.ipaddressiterator.interview;

import com.wwb.leetcode.other.openai.ipaddressiterator.IpHelper;
import java.util.*;

/**
 * Phase 2: Multi-Range Iterator with Exclusions
 * 
 * Iterates over multiple IP ranges (CIDR blocks) while excluding certain ranges.
 * Handles overlapping ranges by merging them and subtracting exclusions.
 * 
 * Key Concepts:
 * - Interval merging algorithm
 * - Subtraction of overlapping intervals
 * - CIDR notation parsing
 * - Efficient segment-based iteration
 * 
 * Algorithm:
 * 1. Parse all included CIDR blocks into segments
 * 2. Parse all excluded CIDR blocks into segments
 * 3. Merge overlapping included segments
 * 4. Subtract excluded segments from included segments
 * 5. Iterate through resulting segments
 * 
 * Time Complexity:
 * - Construction: O(n log n + m log m) where n=includes, m=excludes
 * - hasNext(): O(1)
 * - next(): O(1) amortized
 * 
 * Space Complexity: O(n + m) for storing segments
 * 
 * Example Usage:
 *   List<String> includes = Arrays.asList("192.168.1.0/24", "10.0.0.0/28");
 *   List<String> excludes = Arrays.asList("192.168.1.128/25");
 *   Phase2MultiRangeIterator iter = new Phase2MultiRangeIterator(includes, excludes);
 *   while (iter.hasNext()) {
 *       System.out.println(iter.next());
 *   }
 */
public class Phase2MultiRangeIterator implements Iterator<String> {
    private final List<Segment> segments;
    private int currentSegmentIndex;
    private long currentIp;

    /**
     * Create an iterator over multiple IP ranges with exclusions
     * 
     * @param includeCidrs List of CIDR blocks to include (e.g., ["192.168.1.0/24", "10.0.0.0/28"])
     * @param excludeCidrs List of CIDR blocks to exclude (e.g., ["192.168.1.128/25"])
     */
    public Phase2MultiRangeIterator(List<String> includeCidrs, List<String> excludeCidrs) {
        if (includeCidrs == null || includeCidrs.isEmpty()) {
            this.segments = new ArrayList<>();
        } else {
            // Step 1: Parse all CIDR blocks into segments
            List<Segment> includeSegments = parseCIDRs(includeCidrs);
            List<Segment> excludeSegments = excludeCidrs != null ? parseCIDRs(excludeCidrs) : new ArrayList<>();

            // Step 2: Merge overlapping include segments
            List<Segment> mergedIncludes = mergeSegments(includeSegments);

            // Step 3: Subtract excluded segments
            this.segments = subtractSegments(mergedIncludes, excludeSegments);
        }

        this.currentSegmentIndex = 0;
        this.currentIp = segments.isEmpty() ? 0 : segments.get(0).start;
    }

    /**
     * Parse list of CIDR blocks into segments
     */
    private List<Segment> parseCIDRs(List<String> cidrs) {
        List<Segment> result = new ArrayList<>();
        for (String cidr : cidrs) {
            result.add(IPRange.fromCIDR(cidr));
        }
        return result;
    }

    /**
     * Merge overlapping or adjacent segments
     * Input must be sorted by start IP
     */
    private List<Segment> mergeSegments(List<Segment> segments) {
        if (segments.isEmpty()) {
            return new ArrayList<>();
        }

        // Sort by start IP
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
     * Subtract excluded segments from included segments using Two-Pointer approach
     * 
     * ALGORITHM: Two-Pointer Optimization
     * Instead of creating intermediate lists for each exclusion, we:
     * 1. Sort excludes once by start position
     * 2. For each include segment, scan through excludes in order
     * 3. Track current position and output gaps between excludes
     * 
     * Time Complexity: O(n log n + m log m + n×m)
     *   - Better than naive O(n × m × k) where k = pieces created
     *   - Avoids creating intermediate lists
     * 
     * Space Complexity: O(n + m) for sorted lists
     * 
     * Visual Example:
     *   Include:  [████████████████████]  1 ─────────── 20
     *   Excludes:      [████]  [████]     5-8, 12-15
     *   
     *   Process:
     *     Start at 1
     *     Before exclude[5-8]:   output [1-4]
     *     Move to 9
     *     Before exclude[12-15]: output [9-11]
     *     Move to 16
     *     After all excludes:    output [16-20]
     *   
     *   Result: [1-4], [9-11], [16-20]
     */
    private List<Segment> subtractSegments(List<Segment> includes, List<Segment> excludes) {
        if (excludes.isEmpty()) {
            return includes;
        }

        List<Segment> result = new ArrayList<>();
        
        // Sort excludes once for efficient processing
        List<Segment> sortedExcludes = new ArrayList<>(excludes);
        Collections.sort(sortedExcludes);

        for (Segment include : includes) {
            long currentStart = include.start;
            
            for (Segment exclude : sortedExcludes) {
                // Early exit: exclude is completely before current position
                if (exclude.end < currentStart) {
                    continue; // Skip this exclude
                }
                
                // Early exit: exclude is completely after this include
                if (exclude.start > include.end) {
                    break; // No more excludes will overlap
                }
                
                // If there's a gap before the exclude, output it
                if (exclude.start > currentStart) {
                    result.add(new Segment(currentStart, 
                                          Math.min(include.end, exclude.start - 1)));
                }
                
                // Move current position past the exclude
                currentStart = Math.max(currentStart, exclude.end + 1);
                
                // Early exit: we've passed the include's end
                if (currentStart > include.end) {
                    break;
                }
            }
            
            // Add remaining part if any
            if (currentStart <= include.end) {
                result.add(new Segment(currentStart, include.end));
            }
        }

        return result;
    }

    @Override
    public boolean hasNext() {
        return currentSegmentIndex < segments.size() && 
               currentIp <= segments.get(currentSegmentIndex).end;
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more IP addresses");
        }

        String result = IpHelper.longToIp(currentIp);
        currentIp++;

        // Move to next segment if current is exhausted
        if (currentIp > segments.get(currentSegmentIndex).end) {
            currentSegmentIndex++;
            if (currentSegmentIndex < segments.size()) {
                currentIp = segments.get(currentSegmentIndex).start;
            }
        }

        return result;
    }

    /**
     * Get total number of IPs across all segments
     */
    public long totalIPs() {
        long total = 0;
        for (Segment seg : segments) {
            total += seg.size();
        }
        return total;
    }

    /**
     * Get number of segments (for debugging/analysis)
     */
    public int segmentCount() {
        return segments.size();
    }

    // ============================================================================
    // Test Cases
    // ============================================================================

    public static void main(String[] args) {
        System.out.println("=== Phase 2: Multi-Range Iterator with Exclusions ===\n");

        // Test 1: Single CIDR block, no exclusions
        System.out.println("Test 1: Single CIDR block (192.168.1.0/30 = 4 IPs)");
        List<String> inc1 = Arrays.asList("192.168.1.0/30");
        Phase2MultiRangeIterator iter1 = new Phase2MultiRangeIterator(inc1, null);
        System.out.println("  Total IPs: " + iter1.totalIPs());
        System.out.println("  Segments: " + iter1.segmentCount());
        assert iter1.totalIPs() == 4 : "Expected 4 IPs";
        int count = 0;
        while (iter1.hasNext() && count < 5) {
            System.out.println("    " + iter1.next());
            count++;
        }
        System.out.println("✓ Passed\n");

        // Test 2: Multiple non-overlapping ranges
        System.out.println("Test 2: Two non-overlapping ranges");
        List<String> inc2 = Arrays.asList("192.168.1.0/30", "10.0.0.0/30");
        Phase2MultiRangeIterator iter2 = new Phase2MultiRangeIterator(inc2, null);
        System.out.println("  Total IPs: " + iter2.totalIPs());
        System.out.println("  Segments: " + iter2.segmentCount());
        assert iter2.totalIPs() == 8 : "Expected 8 IPs";
        assert iter2.segmentCount() == 2 : "Expected 2 segments";
        System.out.println("✓ Passed\n");

        // Test 3: Overlapping ranges (should merge)
        System.out.println("Test 3: Overlapping ranges (should merge)");
        List<String> inc3 = Arrays.asList("192.168.1.0/25", "192.168.1.128/25");
        Phase2MultiRangeIterator iter3 = new Phase2MultiRangeIterator(inc3, null);
        System.out.println("  Total IPs: " + iter3.totalIPs());
        System.out.println("  Segments: " + iter3.segmentCount());
        assert iter3.totalIPs() == 256 : "Expected 256 IPs (full /24)";
        assert iter3.segmentCount() == 1 : "Expected 1 merged segment";
        System.out.println("✓ Passed\n");

        // Test 4: Simple exclusion
        System.out.println("Test 4: Exclusion from middle");
        List<String> inc4 = Arrays.asList("192.168.1.0/24");
        List<String> exc4 = Arrays.asList("192.168.1.128/25");
        Phase2MultiRangeIterator iter4 = new Phase2MultiRangeIterator(inc4, exc4);
        System.out.println("  Total IPs: " + iter4.totalIPs());
        System.out.println("  Segments: " + iter4.segmentCount());
        assert iter4.totalIPs() == 128 : "Expected 128 IPs (256 - 128)";
        assert iter4.segmentCount() == 1 : "Expected 1 segment";
        System.out.println("✓ Passed\n");

        // Test 5: Exclusion creates holes
        System.out.println("Test 5: Exclusion creating holes");
        List<String> inc5 = Arrays.asList("10.0.0.0/28"); // 16 IPs
        List<String> exc5 = Arrays.asList("10.0.0.4/30");  // Exclude 4 IPs in middle
        Phase2MultiRangeIterator iter5 = new Phase2MultiRangeIterator(inc5, exc5);
        System.out.println("  Total IPs: " + iter5.totalIPs());
        System.out.println("  Segments: " + iter5.segmentCount());
        assert iter5.totalIPs() == 12 : "Expected 12 IPs (16 - 4)";
        assert iter5.segmentCount() == 2 : "Expected 2 segments (hole in middle)";
        
        // Print all IPs to verify
        System.out.println("  IPs:");
        while (iter5.hasNext()) {
            System.out.println("    " + iter5.next());
        }
        System.out.println("✓ Passed\n");

        // Test 6: Complex scenario
        System.out.println("Test 6: Complex multi-range with exclusions");
        List<String> inc6 = Arrays.asList("192.168.1.0/24", "192.168.2.0/24", "10.0.0.0/28");
        List<String> exc6 = Arrays.asList("192.168.1.0/25", "10.0.0.8/29");
        Phase2MultiRangeIterator iter6 = new Phase2MultiRangeIterator(inc6, exc6);
        System.out.println("  Total IPs: " + iter6.totalIPs());
        System.out.println("  Segments: " + iter6.segmentCount());
        // 128 (from 192.168.1.128/25) + 256 (from 192.168.2.0/24) + 8 (from 10.0.0.0/29 after exclusion) = 392
        assert iter6.totalIPs() == 392 : "Expected 392 IPs";
        System.out.println("✓ Passed\n");

        // Test 7: Empty result (all excluded)
        System.out.println("Test 7: All IPs excluded");
        List<String> inc7 = Arrays.asList("192.168.1.0/24");
        List<String> exc7 = Arrays.asList("192.168.1.0/24");
        Phase2MultiRangeIterator iter7 = new Phase2MultiRangeIterator(inc7, exc7);
        System.out.println("  Total IPs: " + iter7.totalIPs());
        assert iter7.totalIPs() == 0 : "Expected 0 IPs";
        assert !iter7.hasNext() : "Should have no elements";
        System.out.println("✓ Passed\n");

        // Test 8: Verify actual iteration with exclusion
        System.out.println("Test 8: Verify iteration skips excluded IPs");
        List<String> inc8 = Arrays.asList("10.0.0.0/30"); // 10.0.0.0 - 10.0.0.3
        List<String> exc8 = Arrays.asList("10.0.0.2/32"); // Exclude just 10.0.0.2
        Phase2MultiRangeIterator iter8 = new Phase2MultiRangeIterator(inc8, exc8);
        List<String> result8 = new ArrayList<>();
        while (iter8.hasNext()) {
            result8.add(iter8.next());
        }
        System.out.println("  IPs: " + result8);
        assert result8.size() == 3 : "Expected 3 IPs";
        assert result8.contains("10.0.0.0") : "Should contain 10.0.0.0";
        assert result8.contains("10.0.0.1") : "Should contain 10.0.0.1";
        assert !result8.contains("10.0.0.2") : "Should NOT contain 10.0.0.2";
        assert result8.contains("10.0.0.3") : "Should contain 10.0.0.3";
        System.out.println("✓ Passed\n");

        System.out.println("=== All Phase 2 Tests Passed! ===");
    }
}

