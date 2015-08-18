package com.wwb.leetcode.medium;

import java.util.Stack;

/**
 * Evaluate the value of an arithmetic expression in Reverse Polish Notation.
 *
 * Valid operators are +, -, *, /. Each operand may be an integer or another expression.
 *
 * Some examples:
 * ["2", "1", "+", "3", "*"] -> ((2 + 1) * 3) -> 9
 * ["4", "13", "5", "/", "+"] -> (4 + (13 / 5)) -> 6
 *
 */
public class No150 {

    public int evalRPN(String[] tokens) {
        int result = 0;
        Stack<Integer> operands = new Stack<>();

        for(String token : tokens) {
            try {
                int operand = Integer.parseInt(token);
                operands.push(operand);
            } catch (NumberFormatException e) {
                doCalculation(operands, token);
            }
        }

        return operands.pop();
    }

    private void doCalculation(Stack<Integer> operands, String operator) {
        int secondOperand = operands.pop();
        int firstOperand = operands.pop();
        int result;

        switch (operator) {
            case "+":
                result = firstOperand + secondOperand;
                break;
            case "-":
                result = firstOperand - secondOperand;
                break;
            case "*":
                result = firstOperand * secondOperand;
                break;
            case "/":
                result = firstOperand / secondOperand;
                break;
            default:
                throw new RuntimeException();
        }

        operands.push(result);
    }
}