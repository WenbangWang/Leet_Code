package com.wwb.leetcode.medium;

import java.util.Stack;

/**
 * Implement a basic calculator to evaluate a simple expression string.
 * <p>
 * The expression string contains only non-negative integers, +, -, *, / operators and empty spaces .
 * The integer division should truncate toward zero.
 * <p>
 * You may assume that the given expression is always valid.
 * <p>
 * Some examples:
 * "3+2*2" = 7
 * " 3/2 " = 1
 * " 3+5 / 2 " = 5
 */
public class No227 {
    public int calculate(String s) {
        return solution2(s);
    }

    private int solution1(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }

        char lastSign = '+';
        Stack<Integer> stack = new Stack<>();
        int number = 0;

        for (int i = 0, length = s.length(); i < length; i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c)) {
                number = 10 * number + (c - '0');
            }

            if (!Character.isDigit(c) && c != ' ' || i == length - 1) {
                if (lastSign == '-') {
                    stack.push(-number);
                } else if (lastSign == '+') {
                    stack.push(number);
                } else if (lastSign == '*') {
                    stack.push(stack.pop() * number);
                } else if (lastSign == '/') {
                    stack.push(stack.pop() / number);
                }

                lastSign = c;
                number = 0;
            }
        }

        return stack.stream().reduce(Integer::sum).orElse(0);
    }

    private int solution2(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }

        char lastSign = '+';
        int number = 0;
        int sum = 0;
        int lastNumber = 0;

        for (int i = 0, length = s.length(); i < length; i++) {
            char c = s.charAt(i);

            if (Character.isDigit(c)) {
                number = number * 10 + (c - '0');
            }

            if (!Character.isDigit(c) && c != ' ' || i == length - 1) {
                if (lastSign == '+') {
                    sum += lastNumber;
                    lastNumber = number;
                } else if (lastSign == '-') {
                    sum += lastNumber;
                    lastNumber = -number;
                } else if (lastSign == '*') {
                    lastNumber *= number;
                } else if (lastSign == '/') {
                    lastNumber /= number;
                }

                lastSign = c;
                number = 0;
            }
        }

        sum += lastNumber;
        return sum;
    }
}
