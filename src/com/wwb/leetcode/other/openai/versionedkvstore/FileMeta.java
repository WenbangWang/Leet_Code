package com.wwb.leetcode.other.openai.versionedkvstore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

/**
 * Metadata for a single file in the versioned KV store
 * 
 * DESIGN PATTERN (from No981.java):
 * Instead of loading entire files into memory, we maintain metadata that
 * tells us where each version is located. This enables:
 * 1. On-demand loading (lazy deserialization)
 * 2. Fast lookups (jump to byte offset directly)
 * 3. Memory efficiency (don't load unused keys)
 * 
 * INTERVIEW NOTE:
 * This is similar to how RocksDB and LevelDB maintain index files
 * (SSTable index) separate from data files.
 */
public class FileMeta implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * Name of the file (e.g., "file0", "file1")
     */
    public String fileName;
    
    /**
     * Size of the file in bytes
     */
    public long fileSize;
    
    /**
     * Index mapping: key → (timestamp → byte offset in file)
     * 
     * STRUCTURE:
     * Map<String, NavigableMap<Long, Long>>
     *   ↓        ↓                ↓      ↓
     *  key   timestamp       offset  sorted by timestamp
     * 
     * EXAMPLE:
     * {
     *   "user:1" → { 100 → 0, 200 → 45, 300 → 90 },
     *   "user:2" → { 150 → 135, 250 → 180 }
     * }
     * 
     * This means:
     * - "user:1" at timestamp 100 starts at byte 0
     * - "user:1" at timestamp 200 starts at byte 45
     * - etc.
     * 
     * Use NavigableMap interface (TreeMap is the concrete implementation)
     */
    public Map<String, NavigableMap<Long, Long>> keyVersionOffset;
    
    public FileMeta(String fileName) {
        this.fileName = fileName;
        this.fileSize = 0;
        this.keyVersionOffset = new HashMap<>();
    }
    
    /**
     * Get the offset for a specific key at a specific timestamp
     * @return offset in bytes, or null if not found
     */
    public Long getOffset(String key, long timestamp) {
        NavigableMap<Long, Long> versions = keyVersionOffset.get(key);
        if (versions == null) {
            return null;
        }
        return versions.get(timestamp);
    }
    
    /**
     * Get all timestamps for a key in this file
     * Return interface type NavigableMap
     */
    public NavigableMap<Long, Long> getVersions(String key) {
        return keyVersionOffset.get(key);
    }
    
    /**
     * Check if this file contains a specific key
     */
    public boolean containsKey(String key) {
        return keyVersionOffset.containsKey(key);
    }
    
    @Override
    public String toString() {
        return String.format("FileMeta{fileName='%s', size=%d, keys=%d}", 
                           fileName, fileSize, keyVersionOffset.size());
    }
}

