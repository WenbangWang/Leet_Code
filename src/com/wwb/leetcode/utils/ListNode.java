package com.wwb.leetcode.utils;

public class ListNode {

    public int val;
    public ListNode next;

    public ListNode(int x) {
        val = x;
    }

    public static ListNode create(int[] nums) {
        ListNode root = new ListNode(nums[0]);
        ListNode current = root;

        for (int i = 1; i < nums.length; i++) {
            current.next = new ListNode(nums[i]);
            current = current.next;
        }

        return root;
    }
}
