package com.wwb.leetcode.tags.string;

import java.util.*;

/**
 * Given an absolute path for a file (Unix-style), simplify it.
 *
 * For example,
 * path = "/home/", => "/home"
 * path = "/a/./b/../../c/", => "/c"
 */
public class No71 {

    public String simplifyPath(String path) {
        Deque<String> queue = new ArrayDeque<>();
        Set<String> map = new HashSet<>(Arrays.asList("..", ".", ""));
        StringBuilder result = new StringBuilder();

        for(String dir : path.split("/")) {
            if(dir.equals("..") && !queue.isEmpty()) {
                queue.pop();
            } else if(!map.contains(dir)) {
                queue.push(dir);
            }
        }

        while(!queue.isEmpty()) {
            result.append("/").append(queue.pollLast());
        }

        return result.length() == 0 ? "/" : result.toString();
    }
}