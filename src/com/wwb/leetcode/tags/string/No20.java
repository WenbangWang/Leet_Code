package com.wwb.leetcode.tags.string;

import java.util.Stack;

/**
 * Given a string containing just the characters '(', ')', '{', '}', '[' and ']',
 * determine if the input string is valid.
 *
 * The brackets must close in the correct order, "()" and "()[]{}" are all valid but "(]" and "([)]" are not.
 */
public class No20 {

    public static void main(String[] args) {
        No20 no20 = new No20();

        System.out.println(no20.isValid("([])"));
    }

    public boolean isValid(String s) {
        if(s == null || s.length() == 0) {
            return true;
        }

        return solution1(s);
    }

    private boolean solution1(String s) {
        Stack<Character> leftStack = new Stack<>();
        Stack<Character> rightStack = new Stack<>();
        char[] parenArray = s.toCharArray();

        for(char paren : parenArray) {
            leftStack.push(paren);
        }

        while(!leftStack.isEmpty()) {
            char left = leftStack.pop();

            if(rightStack.isEmpty()) {
                rightStack.push(left);
            } else {
                char right = rightStack.peek();

                if(isAPair(left, right)) {
                    rightStack.pop();
                } else if (!areOnSameSide(left, right)){
                    return false;
                } else {
                    rightStack.push(left);
                }
            }
        }

        return leftStack.isEmpty() && rightStack.isEmpty();
    }

    private boolean solution2(String s) {
        Stack<Character> stack = new Stack<>();
        char[] parenArray = s.toCharArray();

        for(char paren : parenArray) {
            if(isLeftRoundParen(paren) || isLeftCurlParen(paren) || isLeftSquareParen(paren)) {
                stack.push(paren);
            } else if(isRightRoundParen(paren) && !stack.isEmpty() && isLeftRoundParen(stack.peek())) {
                stack.pop();
            } else if(isRightCurlParen(paren) && !stack.isEmpty() && isLeftCurlParen(stack.peek())) {
                stack.pop();
            } else if(isRightSquareParen(paren) && !stack.isEmpty() && isLeftSquareParen(stack.peek())) {
                stack.pop();
            } else {
                return false;
            }
        }

        return stack.isEmpty();
    }

    private boolean isLeftRoundParen(char c) {
        return c == '(';
    }

    private boolean isLeftCurlParen(char c) {
        return c == '{';
    }

    private boolean isLeftSquareParen(char c) {
        return c == '[';
    }

    private boolean isRightRoundParen(char c) {
        return c == ')';
    }

    private boolean isRightCurlParen(char c) {
        return c == '}';
    }

    private boolean isRightSquareParen(char c) {
        return c == ']';
    }

    private boolean isAPairOfRoundParen(char c1, char c2) {
        return (isLeftRoundParen(c1) && isRightRoundParen(c2)) || (isLeftRoundParen(c2) && isRightRoundParen(c1));
    }

    private boolean isAPairOfCurlParen(char c1, char c2) {
        return (isLeftCurlParen(c1) && isRightCurlParen(c2)) || (isLeftCurlParen(c2) && isRightCurlParen(c1));
    }

    private boolean isAPairOfSquareParen(char c1, char c2) {
        return (isLeftSquareParen(c1) && isRightSquareParen(c2)) || (isLeftSquareParen(c2) && isRightSquareParen(c1));
    }

    private boolean isAPair(char c1, char c2) {
        return isAPairOfRoundParen(c1, c2) || isAPairOfCurlParen(c1, c2) || isAPairOfSquareParen(c1, c2);
    }

    private boolean areOnSameSide(char c1, char c2) {
        return (isOnLeft(c1) && isOnLeft(c2)) || (isOnRight(c1) && isOnRight(c2));
    }

    private boolean isOnLeft(char c) {
        return isLeftRoundParen(c) || isLeftCurlParen(c) || isLeftSquareParen(c);
    }

    private boolean isOnRight(char c) {
        return isRightRoundParen(c) || isRightCurlParen(c) || isRightSquareParen(c);
    }
}