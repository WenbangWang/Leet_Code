package com.wwb.leetcode.other.anthropic.filededupe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileDedupe {
    public static void main(String[] args) throws Exception {
        FileDedupe dedupe = new FileDedupe();

        System.out.println(dedupe.findDuplicates("C:\\Users\\Wenbang Wang\\AppData\\Roaming\\JetBrains\\IdeaIC2023.2\\workspace\\"));
    }

    public List<List<String>> findDuplicates(String rootDir) throws Exception {
        List<File> files = listFiles(new File(rootDir));

        DuplicateFinder finder = new DuplicateFinder();

        return finder.findDuplicatesByBlock(files);
    }

    private List<File> listFiles(File dir) {
        List<File> result = new ArrayList<>();

        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                result.addAll(listFiles(f));
            } else {
                result.add(f);
            }
        }

        return result;
    }

    private List<List<String>> findByFullFileHash(List<File> files) throws IOException {
        Map<String, List<String>> contentToPath = new HashMap<>();

        for (File file : files) {
            String content = Files.readString(file.toPath());
            contentToPath.putIfAbsent(content, new ArrayList<>());
            contentToPath.get(content).add(file.getPath());
        }

        return contentToPath.values().stream().filter(fs -> fs.size() != 1).toList();
    }

    private List<List<String>> findByFileSizeThenFileHash(List<File> files) throws IOException {
        Map<Long, List<File>> sizeToFile = new HashMap<>();

        for (File file : files) {
            long size = file.length();
            sizeToFile.putIfAbsent(size, new ArrayList<>());
            sizeToFile.get(size).add(file);
        }

        files = sizeToFile.values().stream().filter(fs -> fs.size() != 1).flatMap(List::stream).toList();

        return findByFullFileHash(files);
    }
}
