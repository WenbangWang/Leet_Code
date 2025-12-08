package com.wwb.leetcode.other.openai.ipaddressiterator.interview;

import com.wwb.leetcode.other.openai.ipaddressiterator.IpHelper;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Phase 1: Basic IP Range Iterator
 * 
 * Implements iteration over a single range of IP addresses from startIp to endIp (inclusive).
 * 
 * Key Concepts:
 * - IP address representation (String vs long)
 * - Why use long instead of int (unsigned vs signed)
 * - Basic Iterator interface implementation
 * 
 * Time Complexity:
 * - Construction: O(1)
 * - hasNext(): O(1)
 * - next(): O(1)
 * 
 * Space Complexity: O(1)
 * 
 * Example Usage:
 *   Phase1IPRangeIterator iter = new Phase1IPRangeIterator("192.168.1.0", "192.168.1.3");
 *   while (iter.hasNext()) {
 *       System.out.println(iter.next());
 *   }
 *   // Output: 192.168.1.0, 192.168.1.1, 192.168.1.2, 192.168.1.3
 */
public class Phase1IPRangeIterator implements Iterator<String> {
    private long current;
    private final long end;

    /**
     * Create an iterator from startIp to endIp (inclusive)
     * 
     * @param startIp Starting IP address (e.g., "192.168.1.0")
     * @param endIp Ending IP address (e.g., "192.168.1.255")
     * @throws IllegalArgumentException if startIp > endIp
     */
    public Phase1IPRangeIterator(String startIp, String endIp) {
        this.current = IpHelper.ipToLong(startIp);
        this.end = IpHelper.ipToLong(endIp);

        if (this.current > this.end) {
            throw new IllegalArgumentException(
                String.format("Start IP (%s) must be <= end IP (%s)", startIp, endIp)
            );
        }
    }

    @Override
    public boolean hasNext() {
        return current <= end;
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more IP addresses in range");
        }
        return IpHelper.longToIp(current++);
    }

    /**
     * Get the total number of IPs in this range (including already consumed ones)
     */
    public long totalSize() {
        return end - current + 1;
    }

    /**
     * Get the number of remaining IPs
     */
    public long remaining() {
        return hasNext() ? end - current + 1 : 0;
    }

    // ============================================================================
    // Test Cases
    // ============================================================================

    public static void main(String[] args) {
        System.out.println("=== Phase 1: Basic IP Range Iterator ===\n");

        // Test 1: Basic iteration
        System.out.println("Test 1: Basic iteration (192.168.1.0 to 192.168.1.3)");
        Phase1IPRangeIterator iter1 = new Phase1IPRangeIterator("192.168.1.0", "192.168.1.3");
        int count = 0;
        while (iter1.hasNext()) {
            System.out.println("  " + iter1.next());
            count++;
        }
        System.out.println("  Total IPs: " + count);
        assert count == 4 : "Expected 4 IPs";
        System.out.println("✓ Passed\n");

        // Test 2: Single IP
        System.out.println("Test 2: Single IP (10.0.0.1)");
        Phase1IPRangeIterator iter2 = new Phase1IPRangeIterator("10.0.0.1", "10.0.0.1");
        count = 0;
        while (iter2.hasNext()) {
            System.out.println("  " + iter2.next());
            count++;
        }
        assert count == 1 : "Expected 1 IP";
        System.out.println("✓ Passed\n");

        // Test 3: Larger range (Class C subnet)
        System.out.println("Test 3: Larger range (192.168.1.0/24 = 256 IPs)");
        Phase1IPRangeIterator iter3 = new Phase1IPRangeIterator("192.168.1.0", "192.168.1.255");
        count = 0;
        String first = null, last = null;
        while (iter3.hasNext()) {
            String ip = iter3.next();
            if (first == null) first = ip;
            last = ip;
            count++;
        }
        System.out.println("  First: " + first);
        System.out.println("  Last: " + last);
        System.out.println("  Total: " + count);
        assert count == 256 : "Expected 256 IPs";
        assert first.equals("192.168.1.0") : "First IP should be 192.168.1.0";
        assert last.equals("192.168.1.255") : "Last IP should be 192.168.1.255";
        System.out.println("✓ Passed\n");

        // Test 4: Edge case - starting at 0.0.0.0
        System.out.println("Test 4: Edge case (0.0.0.0 to 0.0.0.2)");
        Phase1IPRangeIterator iter4 = new Phase1IPRangeIterator("0.0.0.0", "0.0.0.2");
        count = 0;
        while (iter4.hasNext()) {
            System.out.println("  " + iter4.next());
            count++;
        }
        assert count == 3 : "Expected 3 IPs";
        System.out.println("✓ Passed\n");

        // Test 5: Edge case - ending at 255.255.255.255
        System.out.println("Test 5: Edge case (255.255.255.253 to 255.255.255.255)");
        Phase1IPRangeIterator iter5 = new Phase1IPRangeIterator("255.255.255.253", "255.255.255.255");
        count = 0;
        while (iter5.hasNext()) {
            System.out.println("  " + iter5.next());
            count++;
        }
        assert count == 3 : "Expected 3 IPs";
        System.out.println("✓ Passed\n");

        // Test 6: Remaining count
        System.out.println("Test 6: Remaining count");
        Phase1IPRangeIterator iter6 = new Phase1IPRangeIterator("10.0.0.1", "10.0.0.5");
        System.out.println("  Initial remaining: " + iter6.remaining());
        assert iter6.remaining() == 5 : "Expected 5 remaining";
        iter6.next();
        iter6.next();
        System.out.println("  After 2 iterations: " + iter6.remaining());
        assert iter6.remaining() == 3 : "Expected 3 remaining";
        System.out.println("✓ Passed\n");

        // Test 7: Invalid range (start > end) - should throw exception
        System.out.println("Test 7: Invalid range (start > end)");
        try {
            new Phase1IPRangeIterator("192.168.1.10", "192.168.1.5");
            System.out.println("✗ Failed - should have thrown exception");
            assert false;
        } catch (IllegalArgumentException e) {
            System.out.println("  Correctly threw exception: " + e.getMessage());
            System.out.println("✓ Passed\n");
        }

        // Test 8: NoSuchElementException when iterating past end
        System.out.println("Test 8: NoSuchElementException when iterating past end");
        Phase1IPRangeIterator iter8 = new Phase1IPRangeIterator("10.0.0.1", "10.0.0.1");
        iter8.next(); // Consume the only element
        try {
            iter8.next(); // Should throw
            System.out.println("✗ Failed - should have thrown exception");
            assert false;
        } catch (NoSuchElementException e) {
            System.out.println("  Correctly threw exception: " + e.getMessage());
            System.out.println("✓ Passed\n");
        }

        System.out.println("=== All Phase 1 Tests Passed! ===");
    }
}

