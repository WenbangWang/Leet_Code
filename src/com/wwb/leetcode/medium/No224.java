package com.wwb.leetcode.medium;

import java.util.Stack;

/**
 * Implement a basic calculator to evaluate a simple expression string.
 *
 * The expression string may contain open ( and closing parentheses ),
 * the plus + or minus sign -, non-negative integers and empty spaces .
 *
 * You may assume that the given expression is always valid.
 *
 * Some examples:
 * "1 + 1" = 2
 * " 2-1 + 2 " = 3
 * "(1+(4+5+2)-3)+(6+8)" = 23
 * Note: Do not use the eval built-in library function.
 */
public class No224 {

    public int calculate(String s) {
        if(s == null || s.isEmpty()) {
            return 0;
        }
        int result = 0;
        int sign = 1;
        int number = 0;
        Stack<Integer> stack = new Stack<>();

        for(char c : s.toCharArray()) {
            if(Character.isDigit(c)) {
                number = 10 * number + (c - '0');
            } else if(c == '-') {
                result += sign * number;
                number = 0;
                sign = -1;
            } else if(c == '+') {
                result += sign * number;
                number = 0;
                sign = 1;
            } else if(c == '(') {
                stack.push(result);
                stack.push(sign);
                result = 0;
                sign = 1;
            } else if(c == ')') {
                result += sign * number;
                number = 0;
                result *= stack.pop(); // last sign
                result += stack.pop(); // last result
            }
        }

        return number != 0 ? result + sign * number : result;
    }
}
