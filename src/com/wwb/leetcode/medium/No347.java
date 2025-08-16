package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Given an integer array nums and an integer k, return the k most frequent elements. You may return the answer in any order.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: nums = [1,1,1,2,2,3], k = 2
 * Output: [1,2]
 * Example 2:
 * <p>
 * Input: nums = [1], k = 1
 * Output: [1]
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 1 <= nums.length <= 10^5
 * -10^4 <= nums[i] <= 10^4
 * k is in the range [1, the number of unique elements in the array].
 * It is guaranteed that the answer is unique.
 * <p>
 * <p>
 * Follow up: Your algorithm's time complexity must be better than O(n log n), where n is the array's size.
 */
public class No347 {
    public int[] topKFrequent(int[] nums, int k) {
        return solution1(nums, k);
    }

    // quick select
    private int[] solution1(int[] nums, int k) {
        Map<Integer, Integer> numberToFrequency = new HashMap<>();

        for (int num : nums) {
            numberToFrequency.put(num, numberToFrequency.getOrDefault(num, 0) + 1);
        }

        List<Pair> pairs = numberToFrequency.entrySet().stream().map(entry -> new Pair(
            entry.getKey(),
            entry.getValue()
        )).collect(Collectors.toCollection(ArrayList::new));

        int start = 0;
        int end = pairs.size() - 1;
        int targetIndex = pairs.size() - k;

        while (start < end) {
            int partitionIndex = partition(pairs, start, end);

            if (partitionIndex == targetIndex) {
                break;
            }

            if (partitionIndex > targetIndex) {
                end = partitionIndex - 1;
            } else {
                start = partitionIndex + 1;
            }
        }

        return IntStream
            .range(targetIndex, pairs.size())
            .mapToObj(i -> pairs.get(i).number)
            .mapToInt(Integer::valueOf)
            .toArray();
    }

    // bucket sort
    private int[] solution2(int[] nums, int k) {
        Map<Integer, Integer> numberToFrequency = new HashMap<>();

        for (int num : nums) {
            numberToFrequency.put(num, numberToFrequency.getOrDefault(num, 0) + 1);
        }

        // first level index represents the frequency, value represents numbers with the same frequency.
        List<Integer>[] buckets = new List[nums.length + 1];

        for (Map.Entry<Integer, Integer> entry: numberToFrequency.entrySet()) {
            int number = entry.getKey();
            int frequency = entry.getValue();

            if (buckets[frequency] == null) {
                buckets[frequency] = new ArrayList<>();
            }

            buckets[frequency].add(number);
        }

        List<Integer> result = new ArrayList<>(nums.length);

        for (int i = buckets.length - 1; i >= 0 && k > 0; i--) {
            if (buckets[i] != null) {
                for (int j = 0; j < buckets[i].size() && k > 0; j++, k--) {
                    result.add(buckets[i].get(j));
                }
            }
        }

        return result.stream().mapToInt(Integer::valueOf).toArray();
    }

    private int partition(List<Pair> pairs, int start, int end) {
        int pivotIndex = start;

        while (start <= end) {
            while (start <= end && pairs.get(start).frequency <= pairs.get(pivotIndex).frequency) {
                start++;
            }

            while (start <= end && pairs.get(end).frequency > pairs.get(pivotIndex).frequency) {
                end--;
            }

            if (start > end) {
                break;
            }

            swap(pairs, start, end);
        }

        swap(pairs, pivotIndex, end);

        return end;
    }

    private void swap(List<Pair> pairs, int left, int right) {
        Pair temp = pairs.get(left);
        pairs.set(left, pairs.get(right));
        pairs.set(right, temp);
    }

    private static class Pair {
        int number;
        int frequency;

        Pair(int number, int frequency) {
            this.number = number;
            this.frequency = frequency;
        }
    }
}
