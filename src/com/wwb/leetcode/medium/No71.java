package com.wwb.leetcode.medium;

import java.util.*;

/**
 * Given an absolute path for a file (Unix-style), simplify it.
 * <p>
 * For example,
 * path = "/home/", => "/home"
 * path = "/a/./b/../../c/", => "/c"
 */
public class No71 {

    public String simplifyPath(String path) {
        Stack<String> stack = new Stack<>();
        Set<String> map = new HashSet<>(Arrays.asList("..", ".", ""));
        StringBuilder result = new StringBuilder();

        for (String dir : path.split("/")) {
            if (dir.equals("..") && !stack.isEmpty()) {
                stack.pop();
            } else if (!map.contains(dir)) {
                stack.push(dir);
            }
        }

        return result.append("/").append(String.join("/", stack)).toString();
    }
}
