package com.wwb.leetcode.experience.linkedin.roller_coaster_agent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Entry point for the Roller Coaster Throughput problem.
 *
 * Normal usage (from project root):
 *   javac src/com/wwb/leetcode/experience/linkedin/roller_coaster_agent/*.java
 *   java -cp src com.wwb.leetcode.experience.linkedin.roller_coaster_agent.RollerCoasterAgent <input_file>
 *
 * Test harness:
 *   java -cp src com.wwb.leetcode.experience.linkedin.roller_coaster_agent.RollerCoasterAgent --test
 *
 * Regenerate expected outputs after intentional changes:
 *   java -cp src com.wwb.leetcode.experience.linkedin.roller_coaster_agent.RollerCoasterAgent --test --regen
 */
public class RollerCoasterAgent {

    private static final int DEFAULT_LOOKAHEAD = 4;
    private static final int MAX_LOOKAHEAD = 10;

    private static final String TESTS_DIR =
            "src/com/wwb/leetcode/experience/linkedin/roller_coaster_agent/tests";

    // -------------------------------------------------------------------------
    // Test scenarios
    // -------------------------------------------------------------------------

    private static class TestScenario {
        final String file;
        final String part1Description;
        final String part2Description;

        TestScenario(String file, String part1Description, String part2Description) {
            this.file = file;
            this.part1Description = part1Description;
            this.part2Description = part2Description;
        }
    }

    private static final List<TestScenario> SCENARIOS = Arrays.asList(
        new TestScenario(
            "01_perfect_packing",
            "Part 1: Groups are exact multiples of seatsPerRow so every train is filled with no " +
            "gaps. Verifies that 100% utilization is reported and actual throughput equals max throughput.",
            "Part 2: All lookahead values (0–10) yield identical results (2 runs, 0 empty seats, " +
            "100% utilization) because the packing is optimal regardless of lookahead."
        ),
        new TestScenario(
            "02_lookahead_matters",
            "Part 1: Two large groups (size=5) nearly fill the 6-seat train; lookahead=4 picks up " +
            "the trailing size=1 groups to complete each train. Reports 2 runs, 100% utilization.",
            "Part 2: lookahead=0: after seating g1(5), only g2(5) is considered and doesn't fit, " +
            "so train1 closes with 1 empty. g3(1) then joins train2 alongside g2(5). g4(1) is " +
            "stranded alone on train3 (5 empty). Total: 3 runs, 6 empty, 66.7% util. " +
            "lookahead>=1: g3 and g4 are picked up mid-fill to complete trains 1 and 2 (2 runs, " +
            "0 empty, 100% util). Best lookahead reported as 1."
        ),
        new TestScenario(
            "03_row_alignment",
            "Part 1: A group of 2 leaves a partial row; the next group of 5 must advance to the " +
            "next row boundary (placing mid-row would span 3 rows; aligned spans 2). Verifies the " +
            "row-alignment heuristic fires and the resulting empty seats are counted correctly.",
            "Part 2: Row alignment is a fixed heuristic, not lookahead-sensitive. All lookahead " +
            "values produce the same runs and empty seat count, so utilization is flat across 0–10."
        ),
        new TestScenario(
            "04_multi_train_ceil",
            "Part 1: numTrains=2. Groups fill 3 individual train dispatches, so Total runs = " +
            "ceil(3/2) = 2 (not floor). Verifies ceiling division in the runs calculation.",
            "Part 2: With 2 trains the batch ceiling behavior is consistent across all lookahead " +
            "values for this input. Verifies Part 2 respects the same numTrains divisor."
        ),
        new TestScenario(
            "05_single_group_exact_fit",
            "Part 1: One group whose size equals the full train capacity (6 seats). Verifies the " +
            "simulator terminates in exactly 1 dispatch, 0 empty seats, and 100% utilization.",
            "Part 2: A single perfectly fitting group is lookahead-agnostic — all lookahead values " +
            "report 1 run, 0 empty seats, 100% utilization."
        ),
        new TestScenario(
            "06_multi_ride",
            "Part 1: Two rides in one file separated by a blank line. Verifies that Part 1 output " +
            "is emitted for each ride independently with correct per-ride metrics.",
            "Part 2: Verifies that the lookahead table is emitted separately for each ride and that " +
            "each ride's best lookahead is computed independently."
        )
    );

    // -------------------------------------------------------------------------
    // Main
    // -------------------------------------------------------------------------

    public static void main(String[] args) throws IOException {
        if (args.length >= 1 && args[0].equals("--test")) {
            boolean regen = args.length >= 2 && args[1].equals("--regen");
            runTests(regen);
            return;
        }
        if (args.length < 1) {
            System.out.println("Usage: RollerCoasterAgent <input_file>");
            System.out.println("       RollerCoasterAgent --test [--regen]");
            return;
        }
        List<Ride> rides = RideParser.parse(args[0]);
        runPart1(rides);
        runPart2(rides);
    }

    // -------------------------------------------------------------------------
    // Simulation output
    // -------------------------------------------------------------------------

    static void runPart1(List<Ride> rides) {
        for (Ride ride : rides) {
            new Simulator(ride, DEFAULT_LOOKAHEAD).run().printPart1();
        }
    }

    static void runPart2(List<Ride> rides) {
        System.out.println("=== Part 2: Empty Seat Analysis with Variable Lookahead ===");
        System.out.println();
        for (Ride ride : rides) {
            printLookaheadTable(ride);
        }
    }

    private static void printLookaheadTable(Ride ride) {
        System.out.printf("Ride: %s%n", ride.name);
        System.out.println("Lookahead | Runs | Total Riders | Empty Seats | Utilization");
        System.out.println("----------|------|--------------|-------------|------------");

        int bestLookahead = 0;
        double bestUtilization = -1;

        for (int la = 0; la <= MAX_LOOKAHEAD; la++) {
            SimulationResult result = new Simulator(ride, la).run();
            result.printPart2Row();
            if (result.utilization > bestUtilization) {
                bestUtilization = result.utilization;
                bestLookahead = la;
            }
        }

        System.out.println();
        System.out.printf(Locale.US, "Best lookahead: %d (utilization: %.1f%%)%n%n",
                bestLookahead, bestUtilization * 100);
    }

    // -------------------------------------------------------------------------
    // Test harness
    // -------------------------------------------------------------------------

    private static void runTests(boolean regen) throws IOException {
        int pass = 0;
        int fail = 0;

        for (TestScenario scenario : SCENARIOS) {
            String inputPath = TESTS_DIR + "/" + scenario.file + ".txt";
            List<Ride> rides = RideParser.parse(inputPath);

            String part1Actual = capture(() -> runPart1(rides));
            String part2Actual = capture(() -> runPart2(rides));

            String part1ExpectedPath = TESTS_DIR + "/expected/" + scenario.file + "_part1.txt";
            String part2ExpectedPath = TESTS_DIR + "/expected/" + scenario.file + "_part2.txt";

            if (regen) {
                Files.write(Paths.get(part1ExpectedPath), part1Actual.getBytes());
                Files.write(Paths.get(part2ExpectedPath), part2Actual.getBytes());
                System.out.printf("  REGEN  %s%n", scenario.file);
                continue;
            }

            boolean part1Pass = check(scenario, "Part 1", scenario.part1Description,
                                      part1Actual, part1ExpectedPath);
            boolean part2Pass = check(scenario, "Part 2", scenario.part2Description,
                                      part2Actual, part2ExpectedPath);

            if (part1Pass && part2Pass) pass++;
            else fail++;

            System.out.println();
        }

        if (!regen) {
            System.out.printf("Results: %d passed, %d failed%n", pass, fail);
            if (fail > 0) System.exit(1);
        }
    }

    /**
     * Compares actual output against the expected file for one part of one scenario.
     * Prints PASS/FAIL, the description, and any differing lines on failure.
     */
    private static boolean check(TestScenario scenario, String part, String description,
                                  String actual, String expectedPath) throws IOException {
        String expected = new String(Files.readAllBytes(Paths.get(expectedPath)));
        boolean passed = actual.equals(expected);

        System.out.printf("  %s   %s [%s]%n", passed ? "PASS" : "FAIL", scenario.file, part);
        System.out.printf("         %s%n", description);

        if (!passed) {
            String[] expLines = expected.split("\n", -1);
            String[] actLines = actual.split("\n", -1);
            int maxLines = Math.max(expLines.length, actLines.length);
            for (int i = 0; i < maxLines; i++) {
                String exp = i < expLines.length ? expLines[i] : "<missing>";
                String act = i < actLines.length ? actLines[i] : "<missing>";
                if (!exp.equals(act)) {
                    System.out.printf("         line %d expected: %s%n", i + 1, exp);
                    System.out.printf("         line %d   actual: %s%n", i + 1, act);
                }
            }
        }

        return passed;
    }

    @FunctionalInterface
    private interface OutputAction {
        void run() throws IOException;
    }

    /** Captures System.out produced by the given action into a String. */
    private static String capture(OutputAction action) throws IOException {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            action.run();
        } finally {
            System.setOut(originalOut);
        }
        return buffer.toString();
    }
}
