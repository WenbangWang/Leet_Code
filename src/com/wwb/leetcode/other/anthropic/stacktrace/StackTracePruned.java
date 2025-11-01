package com.wwb.leetcode.other.anthropic.stacktrace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StackTracePruned {

    public static List<Event> convertToTracePruned(List<Sample> samples, int N) {
        List<Event> events = new ArrayList<>();
        if (samples.isEmpty()) return events;

        List<ActiveFrame> prevStack = new ArrayList<>();

        for (Sample s : samples) {
            List<String> curStackNames = s.stack;

            // Find longest common prefix (LCP)
            int lcp = 0;
            while (lcp < prevStack.size() && lcp < curStackNames.size()
                && prevStack.get(lcp).name.equals(curStackNames.get(lcp))) {
                lcp++;
            }

            // End frames that disappeared
            for (int j = prevStack.size() - 1; j >= lcp; j--) {
                ActiveFrame frame = prevStack.get(j);
                if (frame.started) {
                    events.add(new Event("end", s.ts, frame.name));
                }
            }

            // Build new stack frames
            List<ActiveFrame> newStack = new ArrayList<>();
            // Copy LCP frames and increment consecutive count
            for (int i = 0; i < lcp; i++) {
                ActiveFrame frame = prevStack.get(i);
                frame.consecutiveCount++;
                // Emit start event if threshold reached
                if (!frame.started && frame.consecutiveCount >= N) {
                    events.add(new Event("start", s.ts, frame.name));
                    frame.started = true;
                }
                newStack.add(frame);
            }

            // Add new frames beyond LCP
            for (int i = lcp; i < curStackNames.size(); i++) {
                ActiveFrame frame = new ActiveFrame(curStackNames.get(i));
                // needed when N == 1
                if (frame.consecutiveCount >= N) {
                    events.add(new Event("start", s.ts, frame.name));
                    frame.started = true;
                }
                newStack.add(frame);
            }

            prevStack = newStack;
        }

        return events;
    }

    /**
     *             // Compute Longest Common Suffix (LCS)
     *             int lcs = 0;
     *             while (lcs < prevFrames.size() && lcs < currStack.size() &&
     *                    prevFrames.get(prevFrames.size() - 1 - lcs).name
     *                    .equals(currStack.get(currStack.size() - 1 - lcs).name)) {
     *                 lcs++;
     *             }
     *
     *             // End frames before LCS in prevFrames
     *             for (int i = 0; i < prevFrames.size() - lcs; i++) {
     *                 ActiveFrame f = prevFrames.get(i);
     *                 if (f.started) {
     *                     events.add(new Event("end", s.ts, f.name));
     *                 }
     *             }
     *
     *             // Build current ActiveFrame stack
     *             List<ActiveFrame> currFrames = new ArrayList<>();
     *
     *             // 1️⃣ Frames before LCS → new frames
     *             for (int i = 0; i < currStack.size() - lcs; i++) {
     *                 currFrames.add(new ActiveFrame(currStack.get(i), s.ts));
     *             }
     *
     *             // 2️⃣ Frames in LCS → continue previous frames
     *             for (int i = prevFrames.size() - lcs; i < prevFrames.size(); i++) {
     *                 ActiveFrame f = prevFrames.get(i);
     *                 f.consecutiveCount++;
     *                 currFrames.add(f);
     *             }
     *
     *             // Start frames before LCS in currFrames if consecutiveCount >= N
     *             for (int i = 0; i < currFrames.size() - lcs; i++) {
     *                 ActiveFrame f = currFrames.get(i);
     *                 if (!f.started && f.consecutiveCount >= N) {
     *                     events.add(new Event("start", f.firstSeen, f.name));
     *                     f.started = true;
     *                 }
     *             }
     *
     * Prefix你应该好理解，就是每次你不是得先看到 start再找对应得end么，这其实就是从前往后找，就叫prefix，所以反过来从end找start就是postfix。
     *
     * Prefix vs Postfix 在这个题里的区别
     * Prefix 做法
     * 每次采样得到一个调用栈 A → B → C → D。
     * 如果你用 prefix（从栈顶/最外层开始比较），那么两次采样只要前半段一样，就会被当作“重复”。
     * 问题是：在长调用链里，上层函数往往变化不大，真正频繁切换的恰恰是栈底的函数。
     * 所以 prefix 比较可能会过度合并，把本来不同的执行路径当成一样，从而丢失了细粒度信息。
     *
     * Postfix 做法
     * Postfix 从 栈底（leaf function）往上比，重点关注最深层的调用序列。
     * 这样可以更准确地区分不同的热点函数。
     * 比如：
     * main → handler → parse → foo
     * main → handler → parse → bar
     * 如果用 prefix，它们的前 3 层都一样，会被当作同一个路径。
     * 但用 postfix，从 leaf 开始比，foo vs bar 能清晰区分。
     *
     * 总结
     * Prefix：容易“糊”在一起，分辨率差（适合做高层归并）。
     * Postfix：能更精准捕捉到 leaf 层函数的差异（热点分析更有价值）。
     * 在 profiling 场景下，我们通常更关心底层函数的性能瓶颈，所以 postfix 消噪优于 prefix。
     * | Scenario                                     | Prefix (LCP)                                                | Suffix (LCS)                                         |
     * | -------------------------------------------- | ----------------------------------------------------------- | ---------------------------------------------------- |
     * | **Normal stack changes (leaf changing)**     | ✅ Great (minimal churn)                                     | ❌ Overly sensitive, resets too often                 |
     * | **Tail recursion**                           | ⚠️ Can cause minor misalignment, since top of stack repeats | ✅ Handles naturally (bottom stays same)              |
     * | **Async / context switching**                | ❌ May merge unrelated stacks that share roots               | ✅ Keeps leaf-level continuity for short-lived frames |
     * | **Performance tracing (main thread-heavy)**  | ✅ More stable                                               | ❌ Too noisy                                          |
     * | **Low-level profilers (e.g., leaf samples)** | ⚠️ Can look weird (no leaf stability)                       | ✅ Matches actual execution near CPU                  |
     * @param args
     */

    public static void main(String[] args) {
        int N = 2;

        List<Sample> samples = Arrays.asList(
            // main appears 3 times, leaf appears 1 time only
            new Sample(0.0, Arrays.asList("main")),
            new Sample(1.0, Arrays.asList("main", "leaf")),
            new Sample(2.0, Arrays.asList("main"))
        );

        List<Event> events = convertToTracePruned(samples, N);

        System.out.println("=== Pruned trace events (N=" + N + ") ===");
        for (Event e : events) {
            System.out.println(e.kind + " " + e.ts + " " + e.name);
        }

        // Expected output:
        // start 2.0 main
        // Explanation:
        // - "main" appears in 2 consecutive samples (0.0 and 1.0) → start event emitted at 2.0
        // - "leaf" appears only 1 sample → ignored
    }
}
