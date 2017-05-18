package com.wwb.leetcode.tags.string;

/**
 * Given a list of positive integers, the adjacent integers will perform the float division. For example, [2,3,4] -> 2 / 3 / 4.
 *
 * However, you can add any number of parenthesis at any position to change the priority of operations.
 * You should find out how to add parenthesis to get the maximum result, and return the corresponding expression in string format.
 * Your expression should NOT contain redundant parenthesis.
 *
 * Example:
 * Input: [1000,100,10,2]
 * Output: "1000/(100/10/2)"
 * Explanation:
 * 1000/(100/10/2) = 1000/((100/10)/2) = 200
 * However, the bold parenthesis in "1000/((100/10)/2)" are redundant,
 * since they don't influence the operation priority. So you should return "1000/(100/10/2)".
 *
 * Other cases:
 * 1000/(100/10)/2 = 50
 * 1000/(100/(10/2)) = 50
 * 1000/100/10/2 = 0.5
 * 1000/100/(10/2) = 2
 * Note:
 *
 * The length of the input array is [1, 10].
 * Elements in the given array will be in range [2, 1000].
 * There is only one optimal division for each test case.
 */
public class No553 {
    public String optimalDivision(int[] nums) {
        if (nums == null || nums.length == 0) {
            return "";
        }

        if (nums.length == 1) {
            return String.valueOf(nums[0]);
        }

        if (nums.length == 2) {
            return String.valueOf(nums[0]) + "/" + String.valueOf(nums[1]);
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(nums[0]).append("/(").append(nums[1]);

        for(int i = 2; i < nums.length; i++) {
            stringBuilder.append("/").append(nums[i]);
        }

        stringBuilder.append(")");


        return stringBuilder.toString();
    }
}
