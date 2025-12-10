package com.wwb.leetcode.other.openai.cd;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * CD Command Implementation with Tilde Expansion and Symlink Resolution
 * 
 * Implements Unix-style path navigation with:
 * - Basic path normalization (., ..)
 * - Tilde expansion (~, ~/path)
 * - Symlink resolution with cycle detection
 * 
 * Time Complexity: O(n) for basic cd, O(k × n) for symlink resolution
 *   where n = path length, k = symlink resolution iterations
 * Space Complexity: O(n) for path processing stack and visited set
 */
public class Path {
    /**
     * Navigate to target directory with tilde expansion.
     * 
     * Time Complexity: O(n) where n = path length
     *   - Tilde expansion: O(1)
     *   - String concatenation: O(n)
     *   - split("/"): O(n)
     *   - Stack operations: O(d) where d = depth
     *   - String.join(): O(n)
     *   Total: O(n)
     * 
     * Space Complexity: O(n)
     *   - Stack: O(d) where d ≤ n
     *   - Result string: O(n)
     * 
     * @param currentDir current absolute directory path
     * @param targetDir target directory (may start with ~)
     * @return normalized absolute path, or null if invalid
     */
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

    /**
     * Navigate to target directory with symlink resolution.
     * 
     * Time Complexity: O(S + k × n) where:
     *   S = total characters in all symlinks (Trie build cost)
     *   k = symlink resolution iterations (≤ m+1 where m = number of symlinks)
     *   n = path length
     *   
     *   Breakdown:
     *   - cd() without symlinks: O(n)
     *   - buildTrie(): O(S) - one-time preprocessing
     *   - Per iteration: O(n) for path traversal and resolution
     *   Total: O(S + k×n)
     * 
     * Space Complexity: O(S + n) where:
     *   - Trie structure: O(S)
     *   - visited set: O(k) paths, each O(n) length → O(k×n)
     *   - pathTokens list: O(n)
     *   Total: O(S + k×n), typically O(S + n) as k is small
     * 
     * @param currentDir current absolute directory path
     * @param targetDir target directory
     * @param symlinks map of symlink paths to their targets
     * @return final absolute path after resolving all symlinks
     * @throws RuntimeException if symlink cycle detected
     */
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
