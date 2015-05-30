package com.wwb.leetcode.easy;

import java.util.ArrayList;
import java.util.List;

/**
 * Given an index k, return the kth row of the Pascal's triangle.
 *
 * For example, given k = 3,
 * Return [1,3,3,1].
 *
 * Note:
 * Could you optimize your algorithm to use only O(k) extra space?
 */
public class No119 {

    public List<Integer> getRow(int rowIndex) {
        List<Integer> list = new ArrayList<>();

        return solution1(list, rowIndex);
//        return solution2(list, rowIndex, 0);
    }

    private List<Integer> solution1(List<Integer> list, int rowIndex) {
        for(int i = 0; i <= rowIndex; i++) {
             list.add(0, 1);

             for(int j = 1; j < list.size() - 1; j++) {
                 list.set(j, list.get(j) + list.get(j + 1));
             }
        }

        return list;
    }

    private List<Integer> solution2(List<Integer> list, int rowIndex, int currentRow) {
        if(list.size() - 1 == rowIndex) {
            return list;
        }
        List<Integer> row = new ArrayList<>();

        for(int i = 0; i <= currentRow; i++) {
            if(i == 0 || i == currentRow) {
                row.add(1);
            } else {
                row.add(list.get(i - 1) + list.get(i));
            }
        }

        return solution2(row, rowIndex, currentRow + 1);
    }
}