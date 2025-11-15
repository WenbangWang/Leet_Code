package com.wwb.leetcode.other.openai.nodeiterator;

import java.util.List;

public class TypeBinding {

    // Example usage
    public static void main(String[] args) throws Exception {
        Node genericT = new Node("T");
        Node listOfT = new Node(List.of(genericT));
        Function func = new Function(List.of(listOfT), listOfT);

        Node intNode = new Node("int");
        Node listOfInt = new Node(List.of(intNode));

        Node resultType = func.getReturnType(List.of(listOfInt));
        System.out.println("Result Type: " + resultType); // should print [int]


        Node node1 = new Node("T");
        Node node2 = new Node("float");
        Node node3 = new Node("T");
        Node node4 = new Node(List.of(node1, node2));
        Node node5 = new Node(List.of(node4, node3));

        System.out.println(node5);
        // Expected: [[T,float],T]


        Function func1 = new Function(
            List.of(
                node5,
                new Node("S")
            ),
            new Node(List.of(new Node("S"), new Node("T")))
        );

        System.out.println(func1);
        // Expected: ([[T,float],T],S) -> [S,T]


        Node node11 = new Node("str");
        Node node22 = new Node("float");
        Node node33 = new Node("str");
        Node node44 = new Node(List.of(node11, node22));
        Node node55 = new Node(List.of(node44, node33));


        Node arg2 = new Node(List.of(new Node("float"), new Node("int")));

        Node result = func1.getReturnType(
            List.of(node55, arg2)
        );

        System.out.println(result);
        // Expected: [ [float,int], str ]
    }
}
