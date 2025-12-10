package com.wwb.leetcode.other.openai.versionedkvstore;

/**
 * Represents a single version of a key-value pair read from disk
 * 
 * INTERVIEW NOTE:
 * This is a simple data class (DTO) for deserialization.
 * Could use Java 16+ records, but keeping compatible with older Java.
 */
public class VersionEntry {
    public final String key;
    public final long timestamp;
    public final String value;
    
    public VersionEntry(String key, long timestamp, String value) {
        this.key = key;
        this.timestamp = timestamp;
        this.value = value;
    }
    
    @Override
    public String toString() {
        return String.format("VersionEntry{key='%s', ts=%d, value='%s'}", 
                           key, timestamp, value);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionEntry that = (VersionEntry) o;
        return timestamp == that.timestamp &&
               key.equals(that.key) &&
               (value == null ? that.value == null : value.equals(that.value));
    }
    
    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + Long.hashCode(timestamp);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}

