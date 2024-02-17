package com.wwb.leetcode.medium;

/**
 * Follow up for "Search in Rotated Sorted Array":
 * What if duplicates are allowed?
 *
 * Would this affect the run-time complexity? How and why?
 *
 * Write a function to determine if a given target is in the array.
 */
public class No81 {

    public boolean search(int[] nums, int target) {
        int start = 0;
        int end = nums.length - 1;

        while(start <= end) {
            int mid = start + (end - start) / 2;

            if (nums[mid] == target) {
                return true;
            }

            if (nums[mid] < nums[start]) {
                if(target > nums[end] || target < nums[mid]) {
                    end = mid - 1;
                } else {
                    start = mid + 1;
                }
            } else if (nums[mid] > nums[start]) {
                if(target < nums[mid] && target >= nums[start]) {
                    end = mid - 1;
                } else {
                    start = mid + 1;
                }
            } else {
                if (nums[mid] == nums[end]) {
                    boolean allTheSame = true;

                    for (int i = 1; mid - i >= start && mid + i <= end; i++) {
                        if (nums[mid] != nums[mid - i]) {
                            end = mid - i;
                            allTheSame = false;
                            break;
                        } else if (nums[mid] != nums[mid + i]) {
                            start = mid + i;
                            allTheSame = false;
                            break;
                        }
                    }

                    if (allTheSame) {
                        return false;
                    }
                } else {
                    start = mid + 1;
                }
            }
        }

        return false;
    }

    private boolean search(int[] nums, int target, int start, int end) {
        if(start > end) {
            return false;
        }

        int mid = start + (end - start) / 2;

        if(nums[mid] == target) {
            return true;
        }

        if(nums[mid] < nums[start]) {
            if(target > nums[end] || target < nums[mid]) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
        } else if(nums[mid] > nums[start]) {
            if(target < nums[mid] && target >= nums[start]) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
        } else {
            if (nums[mid] == nums[end]) {
                boolean flag = true;

                for (int i = 1; mid - i >= start && mid + i <= end; i++) {
                    if (nums[mid] != nums[mid - i]) {
                        end = mid - i;
                        flag = false;
                        break;
                    } else if (nums[mid] != nums[mid + i]) {
                        start = mid + i;
                        flag = false;
                        break;
                    }
                }

                if (flag) {
                    return false;
                }
            } else {
                start = mid + 1;
            }
        }

        return search(nums, target, start, end);
    }
}
