package com.wwb.leetcode.tags.string;

import java.util.Stack;

/**
 * Implement a basic calculator to evaluate a simple expression string.
 *
 * The expression string contains only non-negative integers, +, -, *, / operators and empty spaces .
 * The integer division should truncate toward zero.
 *
 * You may assume that the given expression is always valid.
 *
 * Some examples:
 * "3+2*2" = 7
 * " 3/2 " = 1
 * " 3+5 / 2 " = 5
 */
public class No227 {

    public int calculate(String s) {
        if(s == null || s.length() == 0) {
            return 0;
        }

        int result = 0;
        char lastSign = '+';
        Stack<Integer> stack = new Stack<>();
        int number = 0;

        for(int i = 0, length = s.length(); i < length; i++) {
            char c = s.charAt(i);
            if(Character.isDigit(c)) {
                number = 10 * number + (c - '0');
            }
            if(!Character.isDigit(c) && c != ' ' || i == length - 1) {
                if(lastSign == '-') {
                    stack.push(-number);
                } else if(lastSign == '+') {
                    stack.push(number);
                } else if(lastSign == '*') {
                    stack.push(stack.pop() * number);
                } else if(lastSign == '/') {
                    stack.push(stack.pop() / number);
                }

                lastSign = c;
                number = 0;
            }
        }

        for(int n : stack) {
            result += n;
        }

        return result;
    }
}