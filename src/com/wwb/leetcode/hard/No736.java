package com.wwb.leetcode.hard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * You are given a string expression representing a Lisp-like expression to return the integer value of.
 *
 * The syntax for these expressions is given as follows.
 *
 * An expression is either an integer, let expression, add expression, mult expression, or an assigned variable.
 * Expressions always evaluate to a single integer.
 * (An integer could be positive or negative.)
 * A let expression takes the form "(let v1 e1 v2 e2 ... vn en expr)", where let is always the string "let",
 * then there are one or more pairs of alternating variables and expressions,
 * meaning that the first variable v1 is assigned the value of the expression e1,
 * the second variable v2 is assigned the value of the expression e2, and so on sequentially;
 * and then the value of this let expression is the value of the expression expr.
 * An add expression takes the form "(add e1 e2)" where add is always the string "add",
 * there are always two expressions e1, e2 and the result is the addition of the evaluation of e1 and the evaluation of e2.
 * A mult expression takes the form "(mult e1 e2)" where mult is always the string "mult",
 * there are always two expressions e1, e2 and the result is the multiplication of the evaluation of e1 and the evaluation of e2.
 * For this question, we will use a smaller subset of variable names. A variable starts with a lowercase letter,
 * then zero or more lowercase letters or digits. Additionally, for your convenience,
 * the names "add", "let", and "mult" are protected and will never be used as variable names.
 * Finally, there is the concept of scope. When an expression of a variable name is evaluated,
 * within the context of that evaluation, the innermost scope (in terms of parentheses) is checked first for the value of that variable,
 * and then outer scopes are checked sequentially. It is guaranteed that every expression is legal.
 * Please see the examples for more details on the scope.
 *
 *
 * Example 1:
 *
 * Input: expression = "(let x 2 (mult x (let x 3 y 4 (add x y))))"
 * Output: 14
 * Explanation: In the expression (add x y), when checking for the value of the variable x,
 * we check from the innermost scope to the outermost in the context of the variable we are trying to evaluate.
 * Since x = 3 is found first, the value of x is 3.
 * Example 2:
 *
 * Input: expression = "(let x 3 x 2 x)"
 * Output: 2
 * Explanation: Assignment in let statements is processed sequentially.
 * Example 3:
 *
 * Input: expression = "(let x 1 y 2 x (add x y) (add x y))"
 * Output: 5
 * Explanation: The first (add x y) evaluates as 3, and is assigned to x.
 * The second (add x y) evaluates as 3+2 = 5.
 *
 *
 * Constraints:
 *
 * 1 <= expression.length <= 2000
 * There are no leading or trailing spaces in expression.
 * All tokens are separated by a single space in expression.
 * The answer and all intermediate calculations of that answer are guaranteed to fit in a 32-bit integer.
 * The expression is guaranteed to be legal and evaluate to an integer.
 */
public class No736 {
    static final String LET = "let";
    static final String MULT = "mult";
    static final String ADD = "add";
    static final String[] TOKENS = new String[]{
        "\\(",
        "\\)",
        LET,
        MULT,
        ADD,
        // var
        "[a-z][a-z0-9]*",
        // number
        "\\d+|-\\d+"
    };


    public int evaluate(String expression) {
        final Pattern PATTERN = Pattern.compile(String.join("|", TOKENS));
        Matcher matcher = PATTERN.matcher(expression);
        List<String> tokens = new ArrayList<>();

        while(matcher.find()) {
            tokens.add(matcher.group());
        }

        return new Parser(tokens).parse().evaluate(new HashMap<>());
    }

    interface Evaluator {
        int evaluate(Map<String, Integer> variableAssignment);
    }

    static class NumberEvaluator implements Evaluator {
        private int value;

        NumberEvaluator(String expression) {
            this.value = Integer.parseInt(expression);
        }

        @Override
        public int evaluate(Map<String, Integer> variableAssignment) {
            return this.value;
        }
    }

    static class VariableEvaluator implements Evaluator {
        String variable;

        VariableEvaluator(String variable) {
            this.variable = variable;
        }

        @Override
        public int evaluate(Map<String, Integer> variableAssignment) {
            return variableAssignment.get(this.variable);
        }
    }

    static class AddEvaluator implements Evaluator {
        private Evaluator a;
        private Evaluator b;

        AddEvaluator(Evaluator a, Evaluator b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public int evaluate(Map<String, Integer> variableAssignment) {
            return this.a.evaluate(variableAssignment) + this.b.evaluate(variableAssignment);
        }
    }

    static class MultEvaluator implements Evaluator {
        private Evaluator a;
        private Evaluator b;

        MultEvaluator(Evaluator a, Evaluator b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public int evaluate(Map<String, Integer> variableAssignment) {
            return this.a.evaluate(variableAssignment) * this.b.evaluate(variableAssignment);
        }
    }

    static class LetEvaluator implements Evaluator {
        private Map<VariableEvaluator, Evaluator> variableExpressions;
        private Evaluator childExpression;

        LetEvaluator(Map<VariableEvaluator, Evaluator> variableExpressions, Evaluator childExpression) {
            this.variableExpressions = variableExpressions;
            this.childExpression = childExpression;
        }

        @Override
        public int evaluate(Map<String, Integer> variableAssignment) {
            // deep copy
            Map<String, Integer> newVariableAssignment = new HashMap<>(variableAssignment);

            variableExpressions.forEach((k, v) -> newVariableAssignment.put(k.variable, v.evaluate(newVariableAssignment)));


            return this.childExpression.evaluate(newVariableAssignment);
        }
    }

    static class Parser {
        private List<String> tokens;
        private int index;

        Parser(List<String> tokens) {
            this.tokens = tokens;
            this.index = 0;
        }

        Evaluator parse() {
            String current = this.read();

            if (current.equals("(")) {
                current = this.read(1);

                return switch (current) {
                    case LET -> this.parseLet();
                    case MULT -> this.parseMult();
                    case ADD -> this.parseAdd();
                    default -> {
                        System.out.println("Invalid expression: " + current);
                        yield null;
                    }
                };

            } else if (Character.isAlphabetic(current.charAt(0))) {
                return new VariableEvaluator(this.consume());
            } else {
                return new NumberEvaluator(this.consume());
            }
        }

        private String read(int offset) {
            return this.tokens.get(this.index + offset);
        }

        private String read() {
            return this.read(0);
        }

        private String consume() {
            String token = this.read();
            this.index++;

            return token;
        }

        private Evaluator parseMult() {
            // consume (
            this.consume();
            // consume mult
            this.consume();

            Evaluator a = this.parse();
            Evaluator b = this.parse();

            // consume )
            this.consume();

            return new MultEvaluator(a, b);
        }

        private Evaluator parseAdd() {
            // consume (
            this.consume();
            // consume add
            this.consume();

            Evaluator a = this.parse();
            Evaluator b = this.parse();

            // consume )
            this.consume();

            return new AddEvaluator(a, b);
        }

        private Map<VariableEvaluator, Evaluator> parseVariableExpressions() {
            Map<VariableEvaluator, Evaluator> variableExpressions = new LinkedHashMap<>();


            // only parse variable->expression pairs
            // not include the very last expression "x" like "let x 3 x"
            while (!this.read().equals("(") && this.index + 1 < this.tokens.size() && !this.read(1).equals(")")) {
                variableExpressions.put(new VariableEvaluator(this.consume()), this.parse());
            }

            return variableExpressions;
        }

        private Evaluator parseLet() {
            // consume (
            this.consume();
            // consume let
            this.consume();

            Evaluator evaluator =  new LetEvaluator(this.parseVariableExpressions(), this.parse());

            // consume )
            this.consume();

            return evaluator;
        }
    }
}
