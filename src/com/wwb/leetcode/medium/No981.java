package com.wwb.leetcode.medium;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Design a time-based key-value data structure that can store multiple values for the same key at different time stamps and retrieve the key's value at a certain timestamp.
 *
 * Implement the TimeMap class:
 *
 * TimeMap() Initializes the object of the data structure.
 * void set(String key, String value, int timestamp) Stores the key key with the value value at the given time timestamp.
 * String get(String key, int timestamp) Returns a value such that set was called previously, with timestamp_prev <= timestamp. If there are multiple such values, it returns the value associated with the largest timestamp_prev. If there are no values, it returns "".
 *
 *
 * <Pre>
 * Example 1:
 *
 * Input
 * ["TimeMap", "set", "get", "get", "set", "get", "get"]
 * [[], ["foo", "bar", 1], ["foo", 1], ["foo", 3], ["foo", "bar2", 4], ["foo", 4], ["foo", 5]]
 * Output
 * [null, null, "bar", "bar", null, "bar2", "bar2"]
 *
 * Explanation
 * TimeMap timeMap = new TimeMap();
 * timeMap.set("foo", "bar", 1);  // store the key "foo" and value "bar" along with timestamp = 1.
 * timeMap.get("foo", 1);         // return "bar"
 * timeMap.get("foo", 3);         // return "bar", since there is no value corresponding to foo at timestamp 3 and timestamp 2, then the only value is at timestamp 1 is "bar".
 * timeMap.set("foo", "bar2", 4); // store the key "foo" and value "bar2" along with timestamp = 4.
 * timeMap.get("foo", 4);         // return "bar2"
 * timeMap.get("foo", 5);         // return "bar2"
 *
 *
 * Constraints:
 *
 * 1 <= key.length, value.length <= 100
 * key and value consist of lowercase English letters and digits.
 * 1 <= timestamp <= 107
 * All the timestamps timestamp of set are strictly increasing.
 * At most 2 * 105 calls will be made to set and get.
 * </Pre>
 */
public class No981 {
    private static class TimeMap {
        private static final int MAX_FILE_SIZE = 100; // simulate bytes

        private Map<String, NavigableMap<Long, String>> store;

        private final List<FileMeta> metadata;
        private final Map<String, List<FileMeta>> keyToFiles = new HashMap<>();

        public TimeMap() {
            // use ConcurrentHashMap for thread safety
            this.store = new HashMap<>();
            this.metadata = new ArrayList<>();
        }

        public void set(String key, String value, long timestamp) {
            // use ConcurrentNavigableMap for thread safety
            this.store.computeIfAbsent(key, k -> new TreeMap<>()).put(timestamp, value);
        }

        public String get(String key, long timestamp) {
            if (!this.store.containsKey(key)) {
                return "";
            }

            Long floorTimestamp = this.store.get(key).floorKey(timestamp);

            if (floorTimestamp == null) {
                return "";
            }

            return this.store.get(key).get(floorTimestamp);
        }

        // -------------------
        // On-demand get
        // -------------------
//        public String get(String key, long timestamp) {
//            List<FileMeta> files = keyToFiles.get(key);
//            if (files == null) return null;
//
//            for (FileMeta fm : files) {
//                VersionEntry ve = readFloorVersionFromFile(fm, key, timestamp);
//                if (ve != null) return ve.value;
//            }
//            return null;
//        }

        public void serialize() throws IOException {
            metadata.clear();
            keyToFiles.clear();

            ByteBuffer currentFileBuffer = ByteBuffer.allocate(MAX_FILE_SIZE);
            FileMeta currentMeta = new FileMeta("file0");
            int fileIndex = 0;

            for (Map.Entry<String, NavigableMap<Long, String>> entry : store.entrySet()) {
                String key = entry.getKey();
                NavigableMap<Long, String> versions = entry.getValue();

                for (Map.Entry<Long, String> ve : versions.entrySet()) {
                    byte[] keyBytes = key.getBytes();
                    byte[] valBytes = ve.getValue().getBytes();
                    int entrySize = 4 + keyBytes.length + 8 + 4 + valBytes.length;

                    if (currentFileBuffer.position() + entrySize > MAX_FILE_SIZE) {
                        // flush current file
                        writeFile(currentMeta.fileName, Arrays.copyOf(currentFileBuffer.array(), currentFileBuffer.position()));
                        currentMeta.fileSize = currentFileBuffer.position();
                        metadata.add(currentMeta);

                        // reset buffer and metadata
                        fileIndex++;
                        currentFileBuffer = ByteBuffer.allocate(MAX_FILE_SIZE);
                        currentMeta = new FileMeta("file" + fileIndex);
                    }

                    long offset = currentFileBuffer.position();
                    currentMeta.keyVersionOffset.computeIfAbsent(key, k -> new TreeMap<>())
                        .put(ve.getKey(), offset);

                    currentFileBuffer.putInt(keyBytes.length);
                    currentFileBuffer.put(keyBytes);
                    currentFileBuffer.putLong(ve.getKey());
                    currentFileBuffer.putInt(valBytes.length);
                    currentFileBuffer.put(valBytes);
                }
            }

            // flush last file
            if (currentFileBuffer.position() > 0) {
                writeFile(currentMeta.fileName, Arrays.copyOf(currentFileBuffer.array(), currentFileBuffer.position()));
                currentMeta.fileSize = currentFileBuffer.position();
                metadata.add(currentMeta);
            }

            // build key→files index
            for (FileMeta fm : metadata) {
                for (String key : fm.keyVersionOffset.keySet()) {
                    keyToFiles.computeIfAbsent(key, k -> new ArrayList<>()).add(fm);
                }
            }
        }


        public void deserializeMetadata(List<FileMeta> metaFromDisk) {
            metadata.clear();
            metadata.addAll(metaFromDisk);
            keyToFiles.clear();
            for (FileMeta fm : metadata) {
                for (String key : fm.keyVersionOffset.keySet()) {
                    keyToFiles.computeIfAbsent(key, k -> new ArrayList<>()).add(fm);
                }
            }
        }

        // -------------------
        // Low-level single version reader
        // -------------------
        private VersionEntry readSingleVersion(byte[] fileBytes, long offset) {
            ByteBuffer buf = ByteBuffer.wrap(fileBytes);
            buf.position((int) offset);

            int keyLen = buf.getInt();
            byte[] keyBytes = new byte[keyLen];
            buf.get(keyBytes);
            String key = new String(keyBytes);

            long ts = buf.getLong();
            int valLen = buf.getInt();
            byte[] valBytes = new byte[valLen];
            buf.get(valBytes);
            String value = new String(valBytes);

            return new VersionEntry(key, ts, value);
        }

        // -------------------
        // Read all versions from a single file
        // -------------------
        private void readAllVersionsFromFile(FileMeta fm, String key,
                                             NavigableMap<Long, String> result) {
            TreeMap<Long, Long> tsToOffset = fm.keyVersionOffset.get(key);
            if (tsToOffset == null || tsToOffset.isEmpty()) return;

            byte[] fileBytes = readFile(fm.fileName);

            for (Map.Entry<Long, Long> e : tsToOffset.entrySet()) {
                VersionEntry ve = readSingleVersion(fileBytes, e.getValue());
                if (!ve.key.equals(key) || ve.timestamp != e.getKey()) {
                    throw new RuntimeException("Corrupted data at offset " + e.getValue());
                }
                result.put(ve.timestamp, ve.value);
            }
        }

        // -------------------
        // Read largest version ≤ timestamp
        // -------------------
        private VersionEntry readFloorVersionFromFile(FileMeta fm, String key, long timestamp) {
            TreeMap<Long, Long> tsToOffset = fm.keyVersionOffset.get(key);
            if (tsToOffset == null || tsToOffset.isEmpty()) return null;

            Map.Entry<Long, Long> floorEntry = tsToOffset.floorEntry(timestamp);
            if (floorEntry == null) return null;

            byte[] fileBytes = readFile(fm.fileName);
            VersionEntry ve = readSingleVersion(fileBytes, floorEntry.getValue());
            if (!ve.key.equals(key) || ve.timestamp != floorEntry.getKey()) return null;
            return ve;
        }

        // -------------------
        // Full deserialize
        // -------------------
        public void deserializeAll() {
            store.clear();
            for (String key : keyToFiles.keySet()) {
                NavigableMap<Long, String> versions = new TreeMap<>();
                for (FileMeta fm : keyToFiles.get(key)) {
                    readAllVersionsFromFile(fm, key, versions);
                }
                store.put(key, versions);
            }
        }

        // -------------------
        // Placeholder file operations
        // -------------------
        private void writeFile(String fileName, byte[] data) {
            // assume this writes bytes to file system
        }

        private byte[] readFile(String fileName) {
            // assume this reads bytes from file system
            return new byte[0];
        }
    }

    public static class FileMeta implements Serializable {
        String fileName;
        long fileSize;
        Map<String, TreeMap<Long, Long>> keyVersionOffset = new HashMap<>();

        public FileMeta(String fileName) {
            this.fileName = fileName;
            this.fileSize = 0;
        }
    }


    private static class VersionEntry {
        String key;
        long timestamp;
        String value;

        VersionEntry(String key, long timestamp, String value) {
            this.key = key;
            this.timestamp = timestamp;
            this.value = value;
        }
    }

/**
 * Your TimeMap object will be instantiated and called as such:
 * TimeMap obj = new TimeMap();
 * obj.set(key,value,timestamp);
 * String param_2 = obj.get(key,timestamp);
 */
}
