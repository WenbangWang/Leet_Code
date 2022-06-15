package com.wwb.leetcode.medium;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * You are given some lists of regions where the first region of each list includes all other regions in that list.
 *
 * Naturally, if a region x contains another region y then x is bigger than y. Also, by definition, a region x contains itself.
 *
 * Given two regions: region1 and region2, return the smallest region that contains both of them.
 *
 * If you are given regions r1, r2, and r3 such that r1 includes r3, it is guaranteed there is no r2 such that r2 includes r3.
 *
 * It is guaranteed the smallest region exists.
 *
 *
 *
 * Example 1:
 *
 * Input:
 * regions = [["Earth","North America","South America"],
 * ["North America","United States","Canada"],
 * ["United States","New York","Boston"],
 * ["Canada","Ontario","Quebec"],
 * ["South America","Brazil"]],
 * region1 = "Quebec",
 * region2 = "New York"
 * Output: "North America"
 * Example 2:
 *
 * Input: regions = [["Earth", "North America", "South America"],["North America", "United States", "Canada"],["United States", "New York", "Boston"],["Canada", "Ontario", "Quebec"],["South America", "Brazil"]], region1 = "Canada", region2 = "South America"
 * Output: "Earth"
 *
 *
 * Constraints:
 *
 * 2 <= regions.length <= 10^4
 * 2 <= regions[i].length <= 20
 * 1 <= regions[i][j].length, region1.length, region2.length <= 20
 * region1 != region2
 * regions[i][j], region1, and region2 consist of English letters.
 */
public class No1257 {
    public String findSmallestRegion(List<List<String>> regions, String region1, String region2) {
        Map<String, String> childToParent = new HashMap<>();

        for(List<String> rs: regions) {
            for(int i = 1; i < rs.size(); i++) {
                childToParent.put(rs.get(i), rs.get(0));
            }
        }

        String p1 = region1;
        String p2 = region2;

        while(!p1.equals(p2)) {
            p1 = childToParent.getOrDefault(p1, region2);
            p2 = childToParent.getOrDefault(p2, region1);
        }

        return p1;
    }
}
