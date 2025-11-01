package com.wwb.leetcode.other.anthropic.filededupe;

import java.io.File;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class DuplicateFinder {

    private static final int BLOCK_SIZE = 8192; // 8 KB

    private final ExecutorService executor;

    public DuplicateFinder() {
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    // Step 1: group files by size
    private Map<Long, List<File>> groupBySize(List<File> files) {
        return files.stream()
            .collect(Collectors.groupingBy(File::length));
    }

    private String getFileBlockHash(File file, long offset) throws Exception {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            // EOF
            if (offset >= raf.length()) {
                return null;
            }
            raf.seek(offset);
            byte[] buffer = new byte[BLOCK_SIZE];
            int read = raf.read(buffer);
            if (read == -1) {
                return null; // EOF
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(buffer, 0, read);
            return HexFormat.of().formatHex(digest.digest());
        }
    }

    // Step 3: find duplicates block by block
    public List<List<String>> findDuplicatesByBlock(List<File> files) throws Exception {
        Map<Long, List<File>> sizeGroups = groupBySize(files);
        List<List<File>> candidates = sizeGroups.values().stream().filter(fs -> fs.size() != 1).toList();

        // Compare block by block
        long blockIndex = 0;
        List<List<String>> result = new ArrayList<>();

        while (!candidates.isEmpty()) {
            long currentBlock = blockIndex;

            // Step 2: process each candidate group concurrently
            List<Future<List<List<List<File>>>>> futures = new ArrayList<>();
            for (List<File> group : candidates) {
                futures.add(executor.submit(() -> processGroup(group, currentBlock)));
            }

            // Collect results (next-level candidates)
            List<List<File>> nextCandidates = new ArrayList<>();
            for (Future<List<List<List<File>>>> f : futures) {
                List<List<List<File>>> groupResult = f.get();
                if (groupResult != null) {
                    nextCandidates.addAll(groupResult.get(0));
                    List<String> subResult = groupResult.get(1).stream().flatMap(List::stream).map(File::getPath).toList();
                    if (!subResult.isEmpty()) {
                        result.add(subResult);
                    }
                }
            }

            candidates = nextCandidates;
            blockIndex++;
        }

        this.executor.shutdown();
        return result;
    }

    private List<List<List<File>>> processGroup(List<File> group, long blockIndex) throws Exception {
        long offset = blockIndex * BLOCK_SIZE;
        String EOF = "EOFFFF";
        List<Future<Map<String, List<File>>>> futures = new ArrayList<>();

        for (File file : group) {
            futures.add(executor.submit(() -> {
                Map<String, List<File>> local = new HashMap<>();
                try {
                    String blockHash = getFileBlockHash(file, offset);
                    local.computeIfAbsent(blockHash == null ? EOF : blockHash, k -> new ArrayList<>()).add(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return local;
            }));
        }

        // Merge results into a single map (no contention, single-threaded merge)
        Map<String, List<File>> merged = new HashMap<>();
        for (Future<Map<String, List<File>>> f : futures) {
            Map<String, List<File>> local = f.get();
            for (var entry : local.entrySet()) {
                merged.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                    .addAll(entry.getValue());
            }
        }

        List<List<List<File>>> result = new ArrayList<>();
        // next candidate
        result.add(new ArrayList<>());
        // duplicate
        result.add(new ArrayList<>());

        for (var entry : merged.entrySet()) {
            if (entry.getValue().size() > 1) {
                List<List<File>> candidate = entry.getKey().equals(EOF) ? result.get(1) : result.get(0);

                candidate.add(entry.getValue());
            }
        }

        return result;
    }
}
