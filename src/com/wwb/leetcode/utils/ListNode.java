package com.wwb.leetcode.utils;

public class ListNode {

    public int val;
    public ListNode next;

    public ListNode(int val) {
        this(val, null);
    }

    public ListNode(int x, ListNode next) {
        this.val = x;
        this.next = next;
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
