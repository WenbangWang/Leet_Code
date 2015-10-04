package com.wwb.leetcode.utils;

import com.wwb.leetcode.easy.*;
import com.wwb.leetcode.medium.*;
import com.wwb.leetcode.hard.*;

import java.util.HashSet;
import java.util.Set;

public class main {

    public static void main(String[] args) {
        int[] array = {5,9,3,2,1,0,2,3,3,1,0,0};
        No45 no45 = new No45();

        System.out.println(no45.jump(array));
    }
}