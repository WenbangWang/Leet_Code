package com.wwb.leetcode.other.openai.cd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TrieNode {
    private String dir;
    private Map<String, TrieNode> children;
    private List<String> pathTokens;

    public TrieNode(String dir) {
        this.dir = dir;
        this.children = new HashMap<>();
        this.pathTokens = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TrieNode node)) {
            return false;
        }
        return Objects.equals(dir, node.dir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dir);
    }

    public static TrieNode buildTrie(Map<String, String> symlinks) {
        TrieNode root = new TrieNode("");

        for (Map.Entry<String, String> entry : symlinks.entrySet()) {
            String symlinkPath = entry.getKey();
            String path = entry.getValue();

            String[] tokens = symlinkPath.split("/");
            TrieNode current = root;

            for (String token : tokens) {
                // leading slash
                if (token.isEmpty()) {
                    continue;
                }
                current.children.putIfAbsent(token, new TrieNode(token));

                current = current.children.get(token);
            }

            current.pathTokens = Arrays.stream(path.split("/")).filter(x -> !x.isEmpty()).toList();
        }

        return root;
    }

    public static List<String> symlinkToPath(List<String> pathTokens, TrieNode root) {
        TrieNode current = root;
        TrieNode linkedNode = null;
        int index = -1;

        for (int i = 0; i < pathTokens.size(); i++) {
            String token = pathTokens.get(i);
            if (!current.children.containsKey(token)) {
                break;
            }

            current = current.children.get(token);

            if (!current.pathTokens.isEmpty()) {
                linkedNode = current;
                index = i;
            }
        }

        // no symlink
        if (linkedNode == null) {
            return null;
        }

        // /dir -> / could also return empty list but it is valid expansion
        List<String> suffix;

        if (index == pathTokens.size() - 1) {
            suffix = Collections.emptyList();
        } else {
            suffix = pathTokens.subList(index + 1, pathTokens.size());
        }

        List<String> result = new ArrayList<>(linkedNode.pathTokens);
        result.addAll(suffix);

        return result;
    }
}
