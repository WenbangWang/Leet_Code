package com.wwb.leetcode.other.openai.versionedkvstore;

import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of FileSystem for testing
 * 
 * INTERVIEW NOTE:
 * This allows us to test serialization logic without actual disk I/O.
 * In production, you'd use real file operations (FileOutputStream, etc.)
 */
public class InMemoryFileSystem implements FileSystem {
    private final Map<String, byte[]> files = new HashMap<>();
    
    @Override
    public void writeFile(String filename, byte[] data) {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        files.put(filename, data.clone()); // Defensive copy
    }
    
    @Override
    public byte[] readFile(String filename) {
        byte[] data = files.get(filename);
        return data != null ? data.clone() : null; // Defensive copy
    }
    
    @Override
    public boolean fileExists(String filename) {
        return files.containsKey(filename);
    }
    
    /**
     * Get number of files (useful for testing)
     */
    public int getFileCount() {
        return files.size();
    }
    
    /**
     * Clear all files (useful for testing)
     */
    public void clear() {
        files.clear();
    }
}

