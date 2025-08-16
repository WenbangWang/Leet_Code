package com.wwb.leetcode.medium;

import java.util.HashMap;
import java.util.Map;

/**
 * Given two sparse vectors, compute their dot product.
 * <p>
 * Implement class SparseVector:
 * <p>
 * SparseVector(nums) Initializes the object with the vector nums
 * dotProduct(vec) Compute the dot product between the instance of SparseVector and vec
 * A sparse vector is a vector that has mostly zero values, you should store the sparse vector efficiently and compute the dot product between two SparseVector.
 * <p>
 * Follow up: What if only one of the vectors is sparse?
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: nums1 = [1,0,0,2,3], nums2 = [0,3,0,4,0]
 * <p>
 * Output: 8
 * <p>
 * Explanation:
 * <p>
 * v1 = SparseVector(nums1) , v2 = SparseVector(nums2)
 * <p>
 * v1.dotProduct(v2) = 1*0 + 0*3 + 0*0 + 2*4 + 3*0 = 8
 * <p>
 * <p>
 * Example 2:
 * <p>
 * Input: nums1 = [0,1,0,0,0], nums2 = [0,0,0,0,2]
 * <p>
 * Output: 0
 * <p>
 * Explanation:
 * v1 = SparseVector(nums1) , v2 = SparseVector(nums2)
 * <p>
 * v1.dotProduct(v2) = 0*0 + 1*0 + 0*0 + 0*0 + 0*2 = 0
 * <p>
 * <p>
 * Example 3:
 * <p>
 * Input: nums1 = [0,1,0,0,2,0,0], nums2 = [1,0,0,0,3,0,4]
 * <p>
 * Output: 6
 * <p>
 * <p>
 * Constraints:
 * <p>
 * n == nums1.length == nums2.length
 * 1 <= n <= 10^5
 * 0 <= nums1[i], nums2[i] <= 100
 */
public class No1570 {
    class SparseVector {
        private Map<Integer, Integer> indexToNum;

        SparseVector(int[] nums) {
            this.indexToNum = new HashMap<>();

            for (int i = 0; i < nums.length; i++) {
                int num = nums[i];

                if (num != 0) {
                    this.indexToNum.put(i, num);
                }
            }
        }

        public int dotProduct(SparseVector vec) {
            Map<Integer, Integer> left = this.indexToNum;
            Map<Integer, Integer> right = vec.indexToNum;

            // always assume left is smaller
            if (left.size() > right.size()) {
                right = this.indexToNum;
                left = vec.indexToNum;
            }

            int result = 0;

            for (Map.Entry<Integer, Integer> entry : left.entrySet()) {
                result += entry.getValue() * right.getOrDefault(entry.getKey(), 0);
            }

            return result;
        }
    }
}
