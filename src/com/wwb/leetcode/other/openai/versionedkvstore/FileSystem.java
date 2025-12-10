package com.wwb.leetcode.other.openai.versionedkvstore;

/**
 * Simple FileSystem interface for testing serialization
 * 
 * In a real interview, the interviewer would provide this interface.
 * It abstracts away actual file I/O for easier testing.
 * 
 * INTERVIEW NOTE:
 * This is a common pattern in system design interviews - abstract
 * the I/O layer so you can focus on the serialization logic.
 */
public interface FileSystem {
    /**
     * Write bytes to a file
     * @param filename The file name (e.g., "file0", "file1")
     * @param data The byte array to write
     */
    void writeFile(String filename, byte[] data);
    
    /**
     * Read bytes from a file
     * @param filename The file name
     * @return The byte array, or null if file doesn't exist
     */
    byte[] readFile(String filename);
    
    /**
     * Check if a file exists
     * @param filename The file name
     * @return true if file exists
     */
    boolean fileExists(String filename);
}

