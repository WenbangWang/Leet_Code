package com.wwb.leetcode.other.anthropic.stacktrace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StackTraceConverter {

    public static List<Event> convertToTrace(List<Sample> samples) {
        List<Event> events = new ArrayList<>();
        if (samples.isEmpty()) return events;

        List<String> prevStack = new ArrayList<>();

        for (Sample s : samples) {
            // uncomment if we want dedupe recursive calls
//            List<String> curStack = new ArrayList<>();
//            Set<String> seen = new HashSet<>();
//            for (String f : s.stack) {
//                if (!seen.contains(f)) {
//                    curStack.add(f);
//                    seen.add(f);
//                }
//            }
            List<String> curStack = s.stack;

            // Find longest common prefix (LCP)
            int lcp = 0;
            while (lcp < prevStack.size() && lcp < curStack.size()
                && prevStack.get(lcp).equals(curStack.get(lcp))) {
                lcp++;
            }

            // End functions that are no longer in the current stack
            // end in a reverse order
            for (int j = prevStack.size() - 1; j >= lcp; j--) {
                events.add(new Event("end", s.ts, prevStack.get(j)));
            }

            // Start new functions
            for (int j = lcp; j < curStack.size(); j++) {
                events.add(new Event("start", s.ts, curStack.get(j)));
            }

            prevStack = curStack;
        }

        // Note: unfinished functions in the last sample are left open
        return events;
    }

    public static void main(String[] args) {
        int N;

        // ===============================
        // Test Case 1: Single sample with duplicate "a"
        // Sample: ["a", "b", "a", "c"]
        // N = 1 (since only one sample)
        // Expected events:
        // start 0.0 a
        // start 0.0 b
        // start 0.0 c
        // Diagram:
        // 0.0 | a b c
        // ===============================
        N = 1;
        List<Sample> test1 = Arrays.asList(
            new Sample(0.0, Arrays.asList("a", "b", "a", "c"))
        );

        // ===============================
        // Test Case 2: Simple leaf pruning
        // Samples: ["main"], ["main","leaf"], ["main"]
        // N = 2
        // Expected events:
        // start 2.0 main
        // Diagram:
        // 0.0 | main
        // 1.0 | main -> leaf (leaf ignored)
        // 2.0 | main (main reached N)
        // ===============================
        N = 2;
        List<Sample> test2 = Arrays.asList(
            new Sample(0.0, Arrays.asList("main")),
            new Sample(1.0, Arrays.asList("main","leaf")),
            new Sample(2.0, Arrays.asList("main"))
        );

        // ===============================
        // Test Case 3: Nested recursion
        // Samples: ["main"], ["main","factorial"], ["main","factorial","factorial"], ["main","factorial"], ["main"]
        // N = 2
        // Expected events:
        // start 1.0 main
        // start 2.0 factorial
        // start 2.0 factorial (recursive)
        // end 3.0 factorial (inner)
        // end 4.0 factorial (outer)
        // Diagram:
        // 0.0 | main
        // 1.0 | main -> factorial
        // 2.0 | main -> factorial -> factorial
        // 3.0 | main -> factorial
        // 4.0 | main
        // ===============================
        List<Sample> test3 = Arrays.asList(
            new Sample(0.0, Arrays.asList("main")),
            new Sample(1.0, Arrays.asList("main", "factorial")),
            new Sample(2.0, Arrays.asList("main", "factorial", "factorial")),
            new Sample(3.0, Arrays.asList("main", "factorial")),
            new Sample(4.0, Arrays.asList("main"))
        );

        // ===============================
        // Test Case 4: Consecutive identical stacks
        // Samples: ["a","b","c"], ["a","b","c"], ["a","b"]
        // N = 2
        // Expected events:
        // start 2.0 a
        // start 2.0 b
        // start 2.0 c
        // end 3.0 c
        // Diagram:
        // 1.0 | a -> b -> c
        // 2.0 | a -> b -> c (N reached)
        // 3.0 | a -> b (c ended)
        // ===============================
        List<Sample> test4 = Arrays.asList(
            new Sample(1.0, Arrays.asList("a", "b", "c")),
            new Sample(2.0, Arrays.asList("a", "b", "c")),
            new Sample(3.0, Arrays.asList("a", "b"))
        );

        // ===============================
        // Test Case 5: Stack changes over time
        // Samples: ["a","b","c"], ["a","d","c"], ["a","d"], ["a"]
        // N = 2
        // Expected events:
        // start 1.0 a
        // start 2.0 b
        // start 2.0 c
        // end 2.0 b
        // start 2.0 d
        // end 3.0 c
        // end 3.0 d
        // end 4.0 a
        // Diagram:
        // 0.0 | a -> b -> c
        // 1.0 | a -> d -> c (b ended, d started)
        // 2.0 | a -> d (c ended)
        // 3.0 | a (d ended)
        // ===============================
        List<Sample> test5 = Arrays.asList(
            new Sample(0.0, Arrays.asList("a", "b", "c")),
            new Sample(1.0, Arrays.asList("a", "d", "c")),
            new Sample(2.0, Arrays.asList("a", "d")),
            new Sample(3.0, Arrays.asList("a"))
        );

        // ===============================
        // Test Case 6: Multiple short-lived leaves
        // Samples: ["main"], ["main","x"], ["main","x"], ["main","x"], ["main","y"], ["main","y"], ["main"]
        // N = 3
        // Expected events:
        // start 3.0 x
        // end 6.0 x
        // Diagram:
        // 0.0 | main
        // 1.0 | main -> x
        // 2.0 | main -> x
        // 3.0 | main -> x (N reached)
        // 4.0 | main -> y (ignored)
        // 5.0 | main -> y
        // 6.0 | main (x ends)
        // ===============================
        N = 3;
        List<Sample> test6 = Arrays.asList(
            new Sample(0.0, Arrays.asList("main")),
            new Sample(1.0, Arrays.asList("main","x")),
            new Sample(2.0, Arrays.asList("main","x")),
            new Sample(3.0, Arrays.asList("main","x")),
            new Sample(4.0, Arrays.asList("main","y")),
            new Sample(5.0, Arrays.asList("main","y")),
            new Sample(6.0, Arrays.asList("main"))
        );

        // ===============================
        // Test Case 7: Recursive call with short-lived leaf
        // Samples: ["a"], ["a","b"], ["a","b","a"], ["a","b"], ["a","c"], ["a"]
        // N = 2
        // Expected events:
        // start 1.0 a
        // start 2.0 b
        // start 2.0 a (recursive)
        // end 3.0 a
        // end 3.0 b
        // Diagram:
        // 0.0 | a
        // 1.0 | a -> b
        // 2.0 | a -> b -> a (recursive)
        // 3.0 | a -> b (inner a ended)
        // 4.0 | a -> c (ignored)
        // 5.0 | a
        // ===============================
        N = 2;
        List<Sample> test7 = Arrays.asList(
            new Sample(0.0, Arrays.asList("a")),
            new Sample(1.0, Arrays.asList("a","b")),
            new Sample(2.0, Arrays.asList("a","b","a")),
            new Sample(3.0, Arrays.asList("a","b")),
            new Sample(4.0, Arrays.asList("a","c")),
            new Sample(5.0, Arrays.asList("a"))
        );

        // Run all tests
        List<List<Sample>> tests = Arrays.asList(test1, test2, test3, test4, test5, test6, test7);

        int testNum = 1;
        for (List<Sample> t : tests) {
            System.out.println("=== Test case " + testNum + " ===");
            List<Event> events = convertToTrace(t);
            for (Event e : events) {
                System.out.println(e.kind + " " + e.ts + " " + e.name);
            }
            System.out.println();
            testNum++;
        }
    }

}
