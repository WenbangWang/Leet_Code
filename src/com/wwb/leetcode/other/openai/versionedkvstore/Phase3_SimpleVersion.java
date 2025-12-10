package com.wwb.leetcode.other.openai.versionedkvstore;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Phase 3A: Simple Serialization (Single File, No 4KB Limit)
 * 
 * Simplified serialization using ByteArrayOutputStream + entry count header.
 * Compare to Phase3VersionedKVStore.java for multi-file version.
 */
public class Phase3_SimpleVersion {
    
    private final Map<String, NavigableMap<Long, String>> store;
    private final FileSystem fileSystem;
    private final ReentrantReadWriteLock serializationLock;
    
    public Phase3_SimpleVersion(FileSystem fileSystem) {
        this.store = new ConcurrentHashMap<>();
        this.fileSystem = fileSystem;
        this.serializationLock = new ReentrantReadWriteLock();
    }
    
    // Basic operations (same as Phase 2)
    
    public void put(String key, String value, long timestamp) {
        serializationLock.readLock().lock();
        try {
            store.computeIfAbsent(key, k -> new ConcurrentSkipListMap<>())
                 .put(timestamp, value);
        } finally {
            serializationLock.readLock().unlock();
        }
    }
    
    public String get(String key, long timestamp) {
        serializationLock.readLock().lock();
        try {
            NavigableMap<Long, String> versions = store.get(key);
            if (versions == null) return null;
            
            Map.Entry<Long, String> entry = versions.floorEntry(timestamp);
            return entry != null ? entry.getValue() : null;
        } finally {
            serializationLock.readLock().unlock();
        }
    }
    
    public String get(String key) {
        serializationLock.readLock().lock();
        try {
            NavigableMap<Long, String> versions = store.get(key);
            if (versions == null) return null;
            
            Map.Entry<Long, String> entry = versions.lastEntry();
            return entry != null ? entry.getValue() : null;
        } finally {
            serializationLock.readLock().unlock();
        }
    }
    
    // ============================================================================
    // Serialization
    // ============================================================================
    
    /**
     * Serialize to single file
     * Format: [count][entry1][entry2]...
     */
    public void serialize() {
        serializationLock.writeLock().lock();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            // Count total entries
            int totalEntries = 0;
            for (NavigableMap<Long, String> versions : store.values()) {
                if (versions != null) {
                    totalEntries += versions.size();
                }
            }
            
            // Write header
            dos.writeInt(totalEntries);
            
            // Write entries
            List<String> sortedKeys = new ArrayList<>(store.keySet());
            Collections.sort(sortedKeys);
            
            for (String key : sortedKeys) {
                NavigableMap<Long, String> versions = store.get(key);
                if (versions == null) continue;
                
                for (Map.Entry<Long, String> versionEntry : versions.entrySet()) {
                    writeEntry(dos, key, versionEntry.getKey(), versionEntry.getValue());
                }
            }
            
            fileSystem.writeFile("store.dat", baos.toByteArray());
            
        } catch (IOException e) {
            throw new RuntimeException("Serialization failed", e);
        } finally {
            serializationLock.writeLock().unlock();
        }
    }
    
    /**
     * Write entry: [keyLen][key][timestamp][valLen][value]
     */
    private void writeEntry(DataOutputStream dos, String key, long timestamp, String value) 
            throws IOException {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] valueBytes = value != null ? value.getBytes(StandardCharsets.UTF_8) : new byte[0];
        
        dos.writeInt(keyBytes.length);
        dos.write(keyBytes);
        dos.writeLong(timestamp);
        dos.writeInt(valueBytes.length);
        if (valueBytes.length > 0) {
            dos.write(valueBytes);
        }
    }
    
    // ============================================================================
    // Deserialization
    // ============================================================================
    
    /**
     * Deserialize from single file
     * Uses bounded loop (no while(true) or EOFException)
     */
    public void deserialize() {
        serializationLock.writeLock().lock();
        try {
            byte[] data = fileSystem.readFile("store.dat");
            if (data == null) return;
            
            Map<String, NavigableMap<Long, String>> newStore = new ConcurrentHashMap<>();
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            
            // Read header
            int totalEntries = dis.readInt();
            
            // Bounded loop!
            for (int i = 0; i < totalEntries; i++) {
                VersionEntry entry = readEntry(dis);
                newStore.computeIfAbsent(entry.key, k -> new ConcurrentSkipListMap<>())
                        .put(entry.timestamp, entry.value);
            }
            
            // Atomic replacement
            store.clear();
            store.putAll(newStore);
            
        } catch (IOException e) {
            throw new RuntimeException("Deserialization failed", e);
        } finally {
            serializationLock.writeLock().unlock();
        }
    }
    
    /**
     * Read entry from stream
     */
    private VersionEntry readEntry(DataInputStream dis) throws IOException {
        int keyLength = dis.readInt();
        byte[] keyBytes = new byte[keyLength];
        dis.readFully(keyBytes);
        String key = new String(keyBytes, StandardCharsets.UTF_8);
        
        long timestamp = dis.readLong();
        
        int valueLength = dis.readInt();
        String value = null;
        if (valueLength > 0) {
            byte[] valueBytes = new byte[valueLength];
            dis.readFully(valueBytes);
            value = new String(valueBytes, StandardCharsets.UTF_8);
        }
        
        return new VersionEntry(key, timestamp, value);
    }
    
    // ============================================================================
    // Test
    // ============================================================================
    
    public static void main(String[] args) {
        System.out.println("=== Phase 3A: Simple Serialization ===\n");
        
        InMemoryFileSystem fs = new InMemoryFileSystem();
        Phase3_SimpleVersion store = new Phase3_SimpleVersion(fs);
        
        store.put("user:1", "Alice", 100);
        store.put("user:1", "Bob", 200);
        store.put("user:2", "Charlie", 150);
        
        store.serialize();
        System.out.println("✓ Serialized (format: [count][entry1][entry2]...)");
        
        Phase3_SimpleVersion store2 = new Phase3_SimpleVersion(fs);
        store2.deserialize();
        
        assert store2.get("user:1", 150).equals("Alice");
        assert store2.get("user:1", 250).equals("Bob");
        assert store2.get("user:2", 200).equals("Charlie");
        
        System.out.println("✓ Deserialized successfully");
        System.out.println("\nKey features:");
        System.out.println("  • ByteArrayOutputStream (no custom classes)");
        System.out.println("  • Entry count header (bounded for-loop)");
        System.out.println("  • Binary encoding (length-prefixed)");
    }
}
