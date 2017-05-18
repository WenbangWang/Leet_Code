package com.wwb.leetcode.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RoundRobinTournament {
    static List<List<Integer>> arrange(int numberOfTeams) {
        List<Integer> teams = IntStream.range(2, numberOfTeams + 1).boxed().collect(Collectors.toList());
        List<List<Integer>> arrange = new ArrayList<>(numberOfTeams);

        for (int i = 0; i < numberOfTeams; i++) {
            arrange.add(new ArrayList<>());
        }

        int numberOfDays = numberOfTeams - 1;
        int halfSize = numberOfTeams / 2;
        int firstTeam = 1;
        int numberOfRemainingTeams = teams.size();

        for (int day = 0; day < numberOfDays; day++) {
            int nextMatchIndex = day;
            arrange.get(firstTeam - 1).add(teams.get(nextMatchIndex));
            arrange.get(teams.get(nextMatchIndex) - 1).add(firstTeam);
            System.out.println(firstTeam + " : " + teams.get(nextMatchIndex));

            for (int runner = 1; runner < halfSize; runner++) {
                int head = (day + runner) % numberOfRemainingTeams;
                int tail = (day + numberOfDays - runner) % numberOfRemainingTeams;

                arrange.get(teams.get(head) - 1).add(teams.get(tail));
                arrange.get(teams.get(tail) - 1).add(teams.get(head));
                System.out.println(teams.get(head) + " : " + teams.get(tail));
            }
        }

        return arrange;
    }
}
