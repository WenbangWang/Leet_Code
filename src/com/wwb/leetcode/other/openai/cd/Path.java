package com.wwb.leetcode.other.openai.cd;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class Path {
    public String cd(String currentDir, String targetDir) {
        final String HOME = "/home";

        // ----------------------------------------------------
        // 1. Real OS-style ~ expansion (only at beginning)
        // ----------------------------------------------------
        if (targetDir.startsWith("~")) {
            // "~"
            if (targetDir.equals("~")) {
                targetDir = HOME;
            }
            // "~/xxx"
            else if (targetDir.startsWith("~/")) {
                targetDir = HOME + targetDir.substring(1);  // remove only the "~"
            }
            // "~user" or "~user/xxx" – real shells resolve users
            // Here we do a simplified version: reject others unless you want full user lookup.
            // For now, treat "~something" literally except "~" and "~/".
            else {
                // do nothing – literal directory name "~xxx"
            }
        }


        String path;

        if (targetDir.startsWith("/")) {
            path = targetDir;                      // absolute
        } else {
            path = currentDir + "/" + targetDir;   // relative
        }

        Stack<String> stack = new Stack<>();
        Set<String> ignore = new HashSet<>(Arrays.asList("..", ".", ""));
        StringBuilder result = new StringBuilder();

        for (String dir : path.split("/")) {
            if (dir.equals("..")) {
                if (stack.isEmpty()) {
                    return null;
                }

                stack.pop();
            } else if (!ignore.contains(dir)) {
                stack.push(dir);
            }
        }

        return result.append("/").append(String.join("/", stack)).toString();
    }

    public String cd(String currentDir, String targetDir, Map<String, String> symlinks) {
        String path = cd(currentDir, targetDir);

        if (path == null) {
            return null;
        }

        Set<String> visited = new HashSet<>();
        TrieNode root = TrieNode.buildTrie(symlinks);
        List<String> pathTokens = Arrays.stream(path.split("/")).filter(x -> !x.isEmpty()).toList();

        int maxDepth = symlinks.size() + 1; // Anything beyond this is definitely a cycle

        for (int step = 0; step < maxDepth; step++) {
            String key = String.join("/", pathTokens);

            if (!visited.add(key)) {
                throw new RuntimeException("loop detected in symlink");
            }

            List<String> newPathTokens = TrieNode.symlinkToPath(pathTokens, root);

            if (newPathTokens == null) {
                break;
            }

            pathTokens = newPathTokens;
        }

        return "/" +  String.join("/", pathTokens);
    }
}
