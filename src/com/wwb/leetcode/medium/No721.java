package com.wwb.leetcode.medium;

import java.util.*;

/**
 * Given a list of accounts where each element accounts[i] is a list of strings, where the first element accounts[i][0] is a name, and the rest of the elements are emails representing emails of the account.
 * <p>
 * Now, we would like to merge these accounts. Two accounts definitely belong to the same person if there is some common email to both accounts. Note that even if two accounts have the same name, they may belong to different people as people could have the same name. A person can have any number of accounts initially, but all of their accounts definitely have the same name.
 * <p>
 * After merging the accounts, return the accounts in the following format: the first element of each account is the name, and the rest of the elements are emails in sorted order. The accounts themselves can be returned in any order.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: accounts = [["John","johnsmith@mail.com","john_newyork@mail.com"],["John","johnsmith@mail.com","john00@mail.com"],["Mary","mary@mail.com"],["John","johnnybravo@mail.com"]]
 * Output: [["John","john00@mail.com","john_newyork@mail.com","johnsmith@mail.com"],["Mary","mary@mail.com"],["John","johnnybravo@mail.com"]]
 * Explanation:
 * The first and second John's are the same person as they have the common email "johnsmith@mail.com".
 * The third John and Mary are different people as none of their email addresses are used by other accounts.
 * We could return these lists in any order, for example the answer [['Mary', 'mary@mail.com'], ['John', 'johnnybravo@mail.com'],
 * ['John', 'john00@mail.com', 'john_newyork@mail.com', 'johnsmith@mail.com']] would still be accepted.
 * Example 2:
 * <p>
 * Input: accounts = [["Gabe","Gabe0@m.co","Gabe3@m.co","Gabe1@m.co"],["Kevin","Kevin3@m.co","Kevin5@m.co","Kevin0@m.co"],["Ethan","Ethan5@m.co","Ethan4@m.co","Ethan0@m.co"],["Hanzo","Hanzo3@m.co","Hanzo1@m.co","Hanzo0@m.co"],["Fern","Fern5@m.co","Fern1@m.co","Fern0@m.co"]]
 * Output: [["Ethan","Ethan0@m.co","Ethan4@m.co","Ethan5@m.co"],["Gabe","Gabe0@m.co","Gabe1@m.co","Gabe3@m.co"],["Hanzo","Hanzo0@m.co","Hanzo1@m.co","Hanzo3@m.co"],["Kevin","Kevin0@m.co","Kevin3@m.co","Kevin5@m.co"],["Fern","Fern0@m.co","Fern1@m.co","Fern5@m.co"]]
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 1 <= accounts.length <= 1000
 * 2 <= accounts[i].length <= 10
 * 1 <= accounts[i][j].length <= 30
 * accounts[i][0] consists of English letters.
 * accounts[i][j] (for j > 0) is a valid email.
 */
public class No721 {
    public List<List<String>> accountsMerge(List<List<String>> accounts) {
        return solution1(accounts);
    }

    // DFS
    private List<List<String>> solution1(List<List<String>> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Set<String>> neighbors = new HashMap<>();

        for (var account : accounts) {
            var firstEmail = account.get(1);

            for (int i = 2; i < account.size(); i++) {
                var email = account.get(i);

                neighbors.putIfAbsent(firstEmail, new HashSet<>());
                neighbors.putIfAbsent(email, new HashSet<>());

                neighbors.get(firstEmail).add(email);
                neighbors.get(email).add(firstEmail);
            }
        }

        List<List<String>> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (var account : accounts) {
            String accountName = account.get(0);
            String accountFirstEmail = account.get(1);

            // If email is visited, then it's a part of different component
            // Hence perform DFS only if email is not visited yet
            if (!visited.contains(accountFirstEmail)) {
                List<String> mergedAccount = new ArrayList<>();
                // Adding account name at the 0th index
                mergedAccount.add(accountName);

                dfs(mergedAccount, accountFirstEmail, visited, neighbors);
                Collections.sort(mergedAccount.subList(1, mergedAccount.size()));
                result.add(mergedAccount);
            }
        }

        return result;
    }

    private List<List<String>> solution2(List<List<String>> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return Collections.emptyList();
        }

        // index is the accounts' index, value is the parent of the accounts.get(index)
        int[] parents = new int[accounts.size()];
        int[] size = new int[accounts.size()];

        // Make itself as its own parent
        for (int i = 0; i < accounts.size(); i++) {
            parents[i] = i;
        }
        Arrays.fill(size, 1);

        Map<String, Integer> emailToIndex = new HashMap<>();

        for (int i = 0; i < accounts.size(); i++) {
            for (int j = 1; j < accounts.get(i).size(); j++) {
                var email = accounts.get(i).get(j);

                if (emailToIndex.containsKey(email)) {
                    var lastIndex = emailToIndex.get(email);

                    var parent1 = find(parents, lastIndex);
                    var parent2 = find(parents, i);

                    if (size[parent1] > size[parent2]) {
                        size[parent1] += size[parent2];
                        parents[parent2] = parent1;
                    } else {
                        size[parent2] += size[parent1];
                        parents[parent1] = parent2;
                    }
                } else {
                    emailToIndex.put(email, i);
                }
            }
        }

        Map<Integer, Set<Integer>> parentToChildren = new HashMap<>();

        for (int i = 0; i < parents.length; i++) {
            int parent = find(parents, i);

            parentToChildren.putIfAbsent(parent, new HashSet<>());

            parentToChildren.get(parent).add(i);
        }

        List<List<String>> result = new ArrayList<>();

        for (var groups : parentToChildren.values()) {
            List<String> account = new ArrayList<>();
            Set<String> emails = new TreeSet<>();

            for (var index : groups) {
                List<String> currentAccount = accounts.get(index);

                emails.addAll(currentAccount.subList(1, currentAccount.size()));
            }

            // account name
            account.add(accounts.get(groups.iterator().next()).get(0));
            account.addAll(emails);

            result.add(account);
        }

        return result;
    }

    private int find(int[] parents, int index) {
        if (parents[index] == index) {
            return index;
        }

        parents[index] = find(parents, parents[index]);

        return parents[index];
    }

    private void dfs(
            List<String> mergedAccount,
            String email,
            Set<String> visited,
            Map<String, Set<String>> neighbors
    ) {
        visited.add(email);

        mergedAccount.add(email);

        for (var neighbor : neighbors.getOrDefault(email, Collections.emptySet())) {
            if (!visited.contains(neighbor)) {
                dfs(mergedAccount, neighbor, visited, neighbors);
            }
        }
    }
}
