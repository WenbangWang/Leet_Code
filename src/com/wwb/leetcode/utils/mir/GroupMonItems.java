package com.wwb.leetcode.utils.mir;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupMonItems {
    public static void main(String[] args) throws IOException {
        Path inputDir = Paths.get("input");
        Path outputDir = Paths.get("output");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir, "*.txt")) {
            for (Path filePath : stream) {
                convert(filePath, outputDir);
            }
        }
    }

    private static void convert(Path inputFile, Path outputDir) throws IOException {
        List<String> output = convert(groupMonItems(inputFile));
        Files.write(outputDir.resolve(inputFile.getFileName()), output);
    }

    private static List<String> convert(Map<String, List<MonItem>> probabilityToMonItems) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<MonItem>> entry : probabilityToMonItems.entrySet()) {
            String prob = entry.getKey();
            List<MonItem> items = entry.getValue();

            result.add("#CHILD " + prob + " RANDOM");
            result.add("(");
            for (MonItem item : items) {
                result.add("  1/1 " + item.item);
            }
            result.add(")");
        }

        return result;
    }

    private static Map<String, List<MonItem>> groupMonItems(Path inputFile) throws IOException {
        Map<String, List<MonItem>> result = new HashMap<>();
        List<String> lines = Files.readAllLines(inputFile);

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            MonItem monItem = parseLine(line, inputFile.toString());

            result.putIfAbsent(monItem.probability, new ArrayList<>());

            result.get(monItem.probability).add(monItem);
        }

        return result;
    }

    private static MonItem parseLine(String s, String filename) {
        String[] parts = s.split("\\s");

        if (parts.length != 2) {
            throw new RuntimeException(String.format("Line needs to have expected format. Current line: %s in file %s", s, filename));
        }

        return new MonItem(parts[1], parts[0]);
    }

    private static class MonItem {
        MonItem(String item, String probability) {
            this.item = item;
            this.probability = probability;
        }

        String item;
        String probability;
    }
}
