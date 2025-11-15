package com.wwb.leetcode.hard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Your task is to design the basic function of Excel and implement the function of sum formula. Specifically, you need to implement the following functions:
 * <p>
 * Excel(int H, char W): This is the constructor. The inputs represents the height and width of the Excel form. H is a positive integer, range from 1 to 26. It represents the height. W is a character range from 'A' to 'Z'. It represents that the width is the number of characters from 'A' to W. The Excel form content is represented by a height * width 2D integer array C, it should be initialized to zero. You should assume that the first row of C starts from 1, and the first column of C starts from 'A'.
 * <p>
 * <p>
 * void Set(int row, char column, int val): Change the value at C(row, column) to be val.
 * int Get(int row, char column): Return the value at C(row, column).
 * int Sum(int row, char column, List of Strings : numbers): This function calculate and set the value at C(row, column), where the value should be the sum of cells represented by numbers. This function return the sum result at C(row, column). This sum formula should exist until this cell is overlapped by another value or another sum formula.
 * <p>
 * numbers is a list of strings that each string represent a cell or a range of cells. If the string represent a single cell, then it has the following format : ColRow. For example, "F7" represents the cell at (7, F).
 * <p>
 * If the string represent a range of cells, then it has the following format : ColRow1:ColRow2. The range will always be a rectangle, and ColRow1 represent the position of the top-left cell, and ColRow2 represents the position of the bottom-right cell.
 *
 * <pre>
 * Example 1:
 *
 * Excel(3,"C");
 * // construct a 3*3 2D array with all zero.
 * //   A B C
 * // 1 0 0 0
 * // 2 0 0 0
 * // 3 0 0 0
 *
 * Set(1, "A", 2);
 * // set C(1,"A") to be 2.
 * //   A B C
 * // 1 2 0 0
 * // 2 0 0 0
 * // 3 0 0 0
 *
 * Sum(3, "C", ["A1", "A1:B2"]);
 * // set C(3,"C") to be the sum of value at C(1,"A") and the values sum of the rectangle range whose top-left cell is C(1,"A") and bottom-right cell is C(2,"B"). Return 4.
 * //   A B C
 * // 1 2 0 0
 * // 2 0 0 0
 * // 3 0 0 4
 *
 * Set(2, "B", 2);
 * // set C(2,"B") to be 2. Note C(3, "C") should also be changed.
 * //   A B C
 * // 1 2 0 0
 * // 2 0 2 0
 * // 3 0 0 6
 *
 * Note:
 *
 * You could assume that there won't be any circular sum reference. For example, A1 = sum(B1) and B1 = sum(A1).
 * The test cases are using double-quotes to represent a character.
 * Please remember to RESET your class variables declared in class Excel, as static/class variables are persisted across multiple test cases. Please see here for more details.
 * Difficulty:
 * </pre>
 */
public class No631 {
    private static class Excel {
        private Cell[][] sheet;
        private int height;
        private int width;

        private static class Cell {
            int val;
            Map<String, Integer> formula;

            Cell() {
                val = 0;
                formula = new HashMap<>();
            }
        }

        public Excel(int height, char width) {
            this.height = height;
            this.width = width - 'A' + 1;
            sheet = new Cell[height][this.width];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < this.width; j++) {
                    sheet[i][j] = new Cell();
                }
            }
        }

        public void set(int row, char column, int val) {
            Cell cell = sheet[row - 1][column - 'A'];
            cell.val = val;
            cell.formula.clear(); // remove any previous formula
        }

        // O(N) in worst case (DFS over dependencies)
        public int get(int row, char column) {
            Cell cell = sheet[row - 1][column - 'A'];
            if (cell.formula.isEmpty()) {
                return cell.val;
            }
            return evaluate(cell.formula);
        }

        // O(K) where K = number of referenced cells
        public int sum(int row, char column, String[] numbers) {
            Cell cell = sheet[row - 1][column - 'A'];
            cell.formula = parseFormula(numbers);
            cell.val = evaluate(cell.formula);
            return cell.val;
        }

        private int evaluate(Map<String, Integer> formula) {
            int sum = 0;
            for (String key : formula.keySet()) {
                int count = formula.get(key);
                int[] pos = parseCell(key);
                sum += count * get(pos[0] + 1, (char) (pos[1] + 'A')); // recursive get
            }
            return sum;
        }

        private Map<String, Integer> parseFormula(String[] numbers) {
            Map<String, Integer> map = new HashMap<>();
            for (String str : numbers) {
                if (str.contains(":")) {
                    String[] parts = str.split(":");
                    int[] start = parseCell(parts[0]);
                    int[] end = parseCell(parts[1]);
                    for (int i = start[0]; i <= end[0]; i++) {
                        for (int j = start[1]; j <= end[1]; j++) {
                            String key = "" + (char) ('A' + j) + (i + 1);
                            map.put(key, map.getOrDefault(key, 0) + 1);
                        }
                    }
                } else {
                    map.put(str, map.getOrDefault(str, 0) + 1);
                }
            }
            return map;
        }

        private int[] parseCell(String s) {
            char col = s.charAt(0);
            int row = Integer.parseInt(s.substring(1));
            return new int[]{row - 1, col - 'A'};
        }
    }

    // with live update
    class Excel1 {
        private static class Cell {
            int r, c;
            int value = 0;
            Map<Cell, Integer> formula = null; // stores dependencies and their weights

            Cell(int r, int c) {
                this.r = r;
                this.c = c;
            }

            // equality based on position
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Cell)) return false;
                Cell other = (Cell) o;
                return r == other.r && c == other.c;
            }

            @Override
            public int hashCode() {
                return Objects.hash(r, c);
            }
        }

        private final int rows, cols;
        private final Cell[][] table;
        private final Map<Cell, Set<Cell>> dependents = new HashMap<>(); // reverse graph

        public Excel1(int H, char W) {
            this.rows = H;
            this.cols = W - 'A' + 1;
            this.table = new Cell[rows + 1][cols + 1];
            for (int r = 1; r <= rows; r++) {
                for (int c = 1; c <= cols; c++) {
                    table[r][c] = new Cell(r, c);
                }
            }
        }

        public void set(int r, char c, int v) {
            Cell cell = table[r][c - 'A' + 1];
            clearFormula(cell);
            cell.value = v;
            propagate(cell);
        }

        public int get(int r, char c) {
            return table[r][c - 'A' + 1].value;
        }

        public int sum(int r, char c, String[] strs) {
            Cell cell = table[r][c - 'A' + 1];
            clearFormula(cell);

            Map<Cell, Integer> formula = new HashMap<>();
            for (String s : strs) {
                if (s.contains(":")) {
                    String[] parts = s.split(":");
                    Cell start = parseCell(parts[0]);
                    Cell end = parseCell(parts[1]);
                    for (int i = start.r; i <= end.r; i++) {
                        for (int j = start.c; j <= end.c; j++) {
                            Cell dep = table[i][j];
                            formula.put(dep, formula.getOrDefault(dep, 0) + 1);
                        }
                    }
                } else {
                    Cell dep = parseCell(s);
                    formula.put(dep, formula.getOrDefault(dep, 0) + 1);
                }
            }

            cell.formula = formula;
            for (Cell dep : formula.keySet()) {
                dependents.computeIfAbsent(dep, k -> new HashSet<>()).add(cell);
            }

            cell.value = calculate(cell);
            propagate(cell);
            return cell.value;
        }

        private int calculate(Cell cell) {
            if (cell.formula == null) {
                return cell.value;
            }
            int sum = 0;
            for (Map.Entry<Cell, Integer> e : cell.formula.entrySet()) {
                sum += e.getKey().value * e.getValue();
            }
            return sum;
        }

        private void clearFormula(Cell cell) {
            if (cell.formula != null) {
                for (Cell dep : cell.formula.keySet()) {
                    Set<Cell> set = dependents.get(dep);
                    if (set != null) {
                        set.remove(cell);
                        if (set.isEmpty()) dependents.remove(dep);
                    }
                }
                cell.formula = null;
            }
        }

        private void propagate(Cell cell) {
            if (!dependents.containsKey(cell)) {
                return;
            }
            for (Cell dep : dependents.get(cell)) {
                dep.value = calculate(dep);
                propagate(dep);
            }
        }

        private Cell parseCell(String s) {
            int c = s.charAt(0) - 'A' + 1;
            int r = Integer.parseInt(s.substring(1));
            return table[r][c];
        }
    }
}
