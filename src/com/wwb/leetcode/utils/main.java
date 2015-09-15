package com.wwb.leetcode.utils;

import com.wwb.leetcode.easy.*;
import com.wwb.leetcode.medium.*;
import com.wwb.leetcode.hard.*;

import java.util.HashSet;
import java.util.Set;

public class main {

    public static void main(String[] args) {
        int n = 5;

        n &= -n;

        System.out.println(n);
    }
}