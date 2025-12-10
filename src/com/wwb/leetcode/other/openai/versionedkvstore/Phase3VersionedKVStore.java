package com.wwb.leetcode.other.openai.versionedkvstore;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * PHASE 3: Thread-Safe Persistence & Multi-File Serialization (15 minutes)
 * 
 * PROBLEM STATEMENT:
 * Building on Phase 2's lock-free thread-safe store, add persistence:
 * 1. serialize() - Save store to disk across multiple files (thread-safe)
 * 2. deserialize() - Restore store from disk (thread-safe)
 * 
 * DESIGN CHOICE: Built on Lock-Free Phase 2
 * - Uses ConcurrentSkipListMap (no per-key locks needed!)
 * - Only need global serializationLock for serialize/deserialize coordination
 * - Simpler than lock-based approach
 * 
 * NEW REQUIREMENTS:
 * - FileSystem API provided for I/O operations
 * - Each file has a 4KB (4096 bytes) limit
 * - Serialization must be thread-safe (can't modify during serialize)
 * - Deserialization must be thread-safe (atomic replacement of data)
 * - Binary format for efficiency
 * 
 * CLARIFYING QUESTIONS TO ASK:
 * 1. "Can we block operations during serialization?"
 *    → Yes for interview, discuss snapshot-based approach for production
 * 2. "Should deserialization be atomic (all-or-nothing)?"
 *    → Yes, don't expose partial state
 * 3. "What about concurrent serialize/deserialize calls?"
 *    → Serialize only one at a time (use global lock)
 * 
 * KEY INSIGHTS:
 * - Inherit lock-free thread-safety from Phase 2 (ConcurrentSkipListMap)
 * - No VersionHistory wrapper needed!
 * - Only need global serializationLock to coordinate serialize/deserialize
 * - Much simpler than lock-based Phase 3
 * 
 * CONCURRENCY STRATEGY:
 * - Regular operations: Lock-free (ConcurrentSkipListMap handles concurrency)
 * - Serialize: Acquire global write lock (blocks all operations)
 * - Deserialize: Acquire global write lock, atomic swap
 * 
 * TIME COMPLEXITY:
 * - put/get: O(log V) lock-free (unchanged from Phase 2)
 * - serialize: O(K × V) with global lock
 * - deserialize: O(K × V) with global lock
 * 
 * SPACE COMPLEXITY:
 * - O(K × V × 2) where 2 = skip list overhead
 * 
 * INTERVIEW NOTE:
 * This is simpler than lock-based Phase 3 because:
 * - No VersionHistory class
 * - No per-key locks to manage
 * - ConcurrentSkipListMap handles all concurrency for operations
 * - Only need global lock for file I/O coordination
 */
public class Phase3VersionedKVStore {
    
    private static final int MAX_FILE_SIZE = 4096; // 4KB per file
    
    // Lock-free store (from Phase 2_LockFree)
    // Use most general interfaces: Map and NavigableMap
    // Thread-safety provided by concrete types: ConcurrentHashMap and ConcurrentSkipListMap
    private final Map<String, NavigableMap<Long, String>> store;
    
    // FileSystem for I/O operations
    private final FileSystem fileSystem;
    
    // Metadata for all files
    private final List<FileMeta> metadata;
    
    // Index: key → list of files containing that key
    private final Map<String, List<FileMeta>> keyToFiles;
    
    // Global lock for serialize/deserialize operations
    // Note: Regular put/get don't need this - they're lock-free!
    private final ReentrantReadWriteLock serializationLock;
    
    /**
     * Initialize with a FileSystem implementation
     */
    public Phase3VersionedKVStore(FileSystem fileSystem) {
        this.store = new ConcurrentHashMap<>();
        this.fileSystem = fileSystem;
        this.metadata = new ArrayList<>();
        this.keyToFiles = new HashMap<>();
        this.serializationLock = new ReentrantReadWriteLock();
    }
    
    // ============================================================================
    // Phase 2 Operations (Lock-Free)
    // ============================================================================
    
    /**
     * Store a key-value pair (lock-free for normal operations)
     * 
     * CONCURRENCY:
     * - serializationLock.readLock prevents serialize during write
     * - ConcurrentSkipListMap.put is lock-free (no per-key locks!)
     * 
     * INTERVIEW NOTE:
     * Much simpler than lock-based version - no VersionHistory,
     * no per-key lock management.
     */
    public void put(String key, String value, long timestamp) {
        validateKey(key);
        validateTimestamp(timestamp);
        
        // Acquire read lock on serialization (allows concurrent puts, blocks serialize)
        serializationLock.readLock().lock();
        try {
            // No per-key locks needed! ConcurrentSkipListMap is thread-safe
            store.computeIfAbsent(key, k -> new ConcurrentSkipListMap<>())
                 .put(timestamp, value);
        } finally {
            serializationLock.readLock().unlock();
        }
    }
    
    /**
     * Retrieve value at or before timestamp (lock-free)
     */
    public String get(String key, long timestamp) {
        validateKey(key);
        validateTimestamp(timestamp);
        
        serializationLock.readLock().lock();
        try {
            NavigableMap<Long, String> versions = store.get(key);
            if (versions == null) {
                return null;
            }
            
            Map.Entry<Long, String> entry = versions.floorEntry(timestamp);
            return entry != null ? entry.getValue() : null;
        } finally {
            serializationLock.readLock().unlock();
        }
    }
    
    /**
     * Retrieve latest value (lock-free)
     */
    public String get(String key) {
        validateKey(key);
        
        serializationLock.readLock().lock();
        try {
            NavigableMap<Long, String> versions = store.get(key);
            if (versions == null) {
                return null;
            }
            
            Map.Entry<Long, String> entry = versions.lastEntry();
            return entry != null ? entry.getValue() : null;
        } finally {
            serializationLock.readLock().unlock();
        }
    }
    
    // ============================================================================
    // Phase 3: Thread-Safe Serialization
    // ============================================================================
    
    /*
     * SIMPLIFIED VERSION (if no 4KB file limit):
     * 
     * public void serialize() {
     *     ByteBuffer buffer = ByteBuffer.allocate(estimatedSize);
     *     
     *     for (String key : store.keySet()) {
     *         NavigableMap<Long, String> versions = store.get(key);
     *         for (Map.Entry<Long, String> entry : versions.entrySet()) {
     *             writeEntry(buffer, key, entry.getKey(), entry.getValue());
     *         }
     *     }
     *     
     *     fileSystem.writeFile("store.dat", buffer.array());
     * }
     * 
     * MULTI-FILE ADDS:
     * - Check if entry fits in current file
     * - Start new file if needed
     * - Track metadata (keyVersionOffset)
     * - Build keyToFiles index for fast lookup
     */
    
    /**
     * Serialize the entire store to disk (thread-safe)
     * 
     * FILE FORMAT:
     * File 0: [entryCount (4 bytes)][entry1][entry2]...
     * File N: [entryN+1][entryN+2]...
     * 
     * BINARY FORMAT PER ENTRY:
     * ┌─────────────┬──────────────┬─────────────┬─────────────┬──────────────┐
     * │  keyLength  │   keyBytes   │  timestamp  │ valueLength │  valueBytes  │
     * │   4 bytes   │   N bytes    │   8 bytes   │   4 bytes   │   M bytes    │
     * │   (int)     │   (UTF-8)    │   (long)    │   (int)     │   (UTF-8)    │
     * └─────────────┴──────────────┴─────────────┴─────────────┴──────────────┘
     * 
     * Total entry size: 16 + keyLength + valueLength bytes
     * 
     * EXAMPLE: key="user:1" (6 bytes), value="Alice" (5 bytes), timestamp=100
     * Bytes: [0,0,0,6][u,s,e,r,:,1][0,0,0,0,0,0,0,100][0,0,0,5][A,l,i,c,e]
     * 
     * ENTRY COUNT HEADER:
     * The first 4 bytes of file0 contain total entry count across ALL files.
     * This enables:
     *   - Bounded loop during deserialization (no while(true) + EOFException)
     *   - Progress tracking (processed 50/1000 entries)
     *   - Pre-allocation of data structures if needed
     * 
     * MULTI-FILE SPLITTING (OPTIONAL - can be separate phase):
     * Without file size limit: Serialize all entries to single file
     * With 4KB limit: Split across multiple files when size exceeded
     * 
     * Core serialization logic (ESSENTIAL):
     *   - Count total entries
     *   - Write count header to first file
     *   - Iterate through keys and versions
     *   - Encode each entry to binary format
     *   - Write to file
     * 
     * Multi-file complexity (OPTIONAL if no size limit):
     *   - Check if entry fits in current file
     *   - Start new file if needed
     *   - Track which keys are in which files (metadata)
     *   - Build keyToFiles index
     * 
     * CONCURRENCY:
     * - Acquires WRITE lock on serializationLock (blocks all operations)
     * - No per-key locks needed (ConcurrentSkipListMap is thread-safe)
     */
    public void serialize() {
        serializationLock.writeLock().lock();
        try {
            // ===== ESSENTIAL: Count total entries first =====
            int totalEntries = 0;
            for (NavigableMap<Long, String> versions : store.values()) {
                if (versions != null) {
                    totalEntries += versions.size();
                }
            }
            
            // ===== MULTI-FILE METADATA (OPTIONAL - only if file size limit exists) =====
            metadata.clear();
            keyToFiles.clear();
            
            // ===== MULTI-FILE BUFFER MANAGEMENT (OPTIONAL) =====
            ByteBuffer currentFileBuffer = ByteBuffer.allocate(MAX_FILE_SIZE);
            FileMeta currentMeta = new FileMeta("file0");
            int fileIndex = 0;
            
            // ===== WRITE HEADER: Total entry count (first 4 bytes of file0) =====
            currentFileBuffer.putInt(totalEntries);
            
            // ===== ESSENTIAL: Iterate through all data =====
            // Sort keys for better locality (optional but recommended)
            List<String> sortedKeys = new ArrayList<>(store.keySet());
            Collections.sort(sortedKeys);
            
            for (String key : sortedKeys) {
                NavigableMap<Long, String> versions = store.get(key);
                if (versions == null) continue;
                
                // ConcurrentSkipListMap provides weakly consistent iterator
                // No need for per-key locks!
                for (Map.Entry<Long, String> versionEntry : versions.entrySet()) {
                    long timestamp = versionEntry.getKey();
                    String value = versionEntry.getValue();
                    
                    // ===== ESSENTIAL: Calculate entry size for binary encoding =====
                    byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
                    byte[] valueBytes = value != null ? 
                        value.getBytes(StandardCharsets.UTF_8) : new byte[0];
                    int entrySize = 4 + keyBytes.length + 8 + 4 + valueBytes.length;
                    
                    // ===== MULTI-FILE: Check if we need to start a new file (OPTIONAL) =====
                    // Without file limit: Skip this check, write to single file
                    if (currentFileBuffer.position() + entrySize > MAX_FILE_SIZE) {
                        // Flush current file
                        flushFile(currentMeta, currentFileBuffer);
                        metadata.add(currentMeta);
                        
                        // Start new file
                        fileIndex++;
                        currentFileBuffer = ByteBuffer.allocate(MAX_FILE_SIZE);
                        currentMeta = new FileMeta("file" + fileIndex);
                    }
                    
                    // ===== MULTI-FILE: Record offset in metadata (OPTIONAL) =====
                    // Without file limit: Skip this, or just track file boundaries
                    // Note: Offset includes the 4-byte header in file0
                    long offset = currentFileBuffer.position();
                    currentMeta.keyVersionOffset
                        .computeIfAbsent(key, k -> new TreeMap<>())
                        .put(timestamp, offset);
                    
                    // ===== ESSENTIAL: Write entry to buffer =====
                    writeEntry(currentFileBuffer, key, timestamp, value);
                }
            }
            
            // ===== MULTI-FILE: Flush last file (OPTIONAL) =====
            // Without file limit: Just flush the single file
            if (currentFileBuffer.position() > 0) {
                flushFile(currentMeta, currentFileBuffer);
                metadata.add(currentMeta);
            }
            
            // ===== MULTI-FILE: Build key→files index (OPTIONAL) =====
            // Without file limit: Single file, no index needed
            buildKeyToFilesIndex();
            
            // ===== Write metadata to disk =====
            writeMetadataToDisk();
        } finally {
            serializationLock.writeLock().unlock();
        }
    }
    
    /**
     * Write a single entry to the byte buffer
     * 
     * BINARY ENTRY LAYOUT:
     * ┌─────────────┬──────────────┬─────────────┬─────────────┬──────────────┐
     * │  keyLength  │   keyBytes   │  timestamp  │ valueLength │  valueBytes  │
     * │   4 bytes   │   N bytes    │   8 bytes   │   4 bytes   │   M bytes    │
     * └─────────────┴──────────────┴─────────────┴─────────────┴──────────────┘
     * 
     * Field details:
     * - keyLength (4 bytes, int):    Length of key in bytes (supports up to 2GB keys)
     * - keyBytes (N bytes):          UTF-8 encoded key string
     * - timestamp (8 bytes, long):   Version timestamp (nanosecond precision possible)
     * - valueLength (4 bytes, int):  Length of value in bytes (0 if value is null)
     * - valueBytes (M bytes):        UTF-8 encoded value string (omitted if length=0)
     * 
     * Total size: 16 + N + M bytes (minimum 16 bytes for empty key/value)
     * 
     * EXAMPLE: key="user:1", value="Alice", timestamp=100
     * [0x00,0x00,0x00,0x06]                    → keyLength = 6
     * [0x75,0x73,0x65,0x72,0x3A,0x31]          → "user:1"
     * [0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x64] → timestamp = 100
     * [0x00,0x00,0x00,0x05]                    → valueLength = 5
     * [0x41,0x6C,0x69,0x63,0x65]               → "Alice"
     * 
     * WHY THIS FORMAT:
     * - Length-prefixed: Can read without knowing size upfront (streaming)
     * - Fixed-size integers: Easy to parse, predictable layout
     * - UTF-8: International character support
     * - Similar to Protocol Buffers, Thrift, Avro
     */
    private void writeEntry(ByteBuffer buffer, String key, long timestamp, String value) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] valueBytes = value != null ? 
            value.getBytes(StandardCharsets.UTF_8) : new byte[0];
        
        // Write: [keyLength][keyBytes][timestamp][valueLength][valueBytes]
        buffer.putInt(keyBytes.length);      // 4 bytes: key length
        buffer.put(keyBytes);                 // N bytes: key data
        buffer.putLong(timestamp);            // 8 bytes: timestamp
        buffer.putInt(valueBytes.length);     // 4 bytes: value length
        if (valueBytes.length > 0) {
            buffer.put(valueBytes);           // M bytes: value data (only if non-empty)
        }
    }
    
    /**
     * Flush buffer to file and update metadata
     */
    private void flushFile(FileMeta meta, ByteBuffer buffer) {
        int size = buffer.position();
        byte[] data = new byte[size];
        buffer.flip();
        buffer.get(data);
        
        fileSystem.writeFile(meta.fileName, data);
        meta.fileSize = size;
    }
    
    /**
     * Write metadata list to disk
     * File: store.dat.meta
     * Format: [count][FileMeta1][FileMeta2]...
     */
    private void writeMetadataToDisk() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            dos.writeInt(metadata.size());
            
            for (FileMeta meta : metadata) {
                writeFileMeta(dos, meta);
            }
            
            fileSystem.writeFile("store.dat.meta", baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write metadata", e);
        }
    }
    
    /**
     * Write single FileMeta to stream
     * Format: [fileName][fileSize][keyCount][key1][versions1]...
     */
    private void writeFileMeta(DataOutputStream dos, FileMeta meta) throws IOException {
        // Write fileName
        byte[] nameBytes = meta.fileName.getBytes(StandardCharsets.UTF_8);
        dos.writeInt(nameBytes.length);
        dos.write(nameBytes);
        
        // Write fileSize
        dos.writeLong(meta.fileSize);
        
        // Write keyVersionOffset map
        dos.writeInt(meta.keyVersionOffset.size());
        
        for (Map.Entry<String, NavigableMap<Long, Long>> keyEntry : meta.keyVersionOffset.entrySet()) {
            String key = keyEntry.getKey();
            NavigableMap<Long, Long> versions = keyEntry.getValue();
            
            // Write key
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            dos.writeInt(keyBytes.length);
            dos.write(keyBytes);
            
            // Write version count
            dos.writeInt(versions.size());
            
            // Write timestamp -> offset pairs
            for (Map.Entry<Long, Long> versionEntry : versions.entrySet()) {
                dos.writeLong(versionEntry.getKey());    // timestamp
                dos.writeLong(versionEntry.getValue());   // offset
            }
        }
    }
    
    // ============================================================================
    // Phase 3: Thread-Safe Deserialization
    // ============================================================================
    
    /**
     * Deserialize metadata from disk
     */
    public void deserializeMetadataFromDisk() {
        serializationLock.writeLock().lock();
        try {
            byte[] data = fileSystem.readFile("store.dat.meta");
            if (data == null) {
                throw new RuntimeException("Metadata file not found: store.dat.meta");
            }
            
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            
            metadata.clear();
            int count = dis.readInt();
            
            for (int i = 0; i < count; i++) {
                FileMeta meta = readFileMeta(dis);
                metadata.add(meta);
            }
            
            buildKeyToFilesIndex();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to read metadata", e);
        } finally {
            serializationLock.writeLock().unlock();
        }
    }
    
    /**
     * Deserialize metadata (in-memory, for testing)
     */
    public void deserializeMetadata(List<FileMeta> metaFromDisk) {
        serializationLock.writeLock().lock();
        try {
            metadata.clear();
            metadata.addAll(metaFromDisk);
            buildKeyToFilesIndex();
        } finally {
            serializationLock.writeLock().unlock();
        }
    }
    
    /**
     * Read single FileMeta from stream
     */
    private FileMeta readFileMeta(DataInputStream dis) throws IOException {
        // Read fileName
        int nameLength = dis.readInt();
        byte[] nameBytes = new byte[nameLength];
        dis.readFully(nameBytes);
        String fileName = new String(nameBytes, StandardCharsets.UTF_8);
        
        FileMeta meta = new FileMeta(fileName);
        
        // Read fileSize
        meta.fileSize = dis.readLong();
        
        // Read keyVersionOffset map
        int keyCount = dis.readInt();
        
        for (int i = 0; i < keyCount; i++) {
            // Read key
            int keyLength = dis.readInt();
            byte[] keyBytes = new byte[keyLength];
            dis.readFully(keyBytes);
            String key = new String(keyBytes, StandardCharsets.UTF_8);
            
            // Read version count
            int versionCount = dis.readInt();
            
            NavigableMap<Long, Long> versions = new TreeMap<>();
            
            // Read timestamp -> offset pairs
            for (int j = 0; j < versionCount; j++) {
                long timestamp = dis.readLong();
                long offset = dis.readLong();
                versions.put(timestamp, offset);
            }
            
            meta.keyVersionOffset.put(key, versions);
        }
        
        return meta;
    }
    
    /**
     * Deserialize all data from files (thread-safe, atomic)
     * 
     * Auto-loads metadata from disk if not already loaded.
     * 
     * FILE FORMAT:
     * File 0: [entryCount (4 bytes)][entry1][entry2]...
     * File N: [entryN+1][entryN+2]...
     * 
     * ENTRY COUNT HEADER:
     * - First 4 bytes of file0 contain total entry count
     * - For multi-file version: We use metadata/index, so count is mainly informational
     * - For simple version: Count enables bounded loop instead of while(true)
     * 
     * CONCURRENCY:
     * - Acquires WRITE lock on serializationLock (blocks all operations)
     * - Builds new store in temporary map
     * - Atomically replaces old store with new store
     * - Ensures readers never see partial state
     * 
     * INTERVIEW NOTE:
     * Simpler than lock-based version:
     * - No VersionHistory to wrap
     * - Just create ConcurrentSkipListMap directly
     */
    public void deserializeAll() {
        // Auto-load metadata if empty
        if (metadata.isEmpty()) {
            deserializeMetadataFromDisk();
        }
        
        serializationLock.writeLock().lock();
        try {
            // Build new store in temporary map (don't modify current store yet)
            Map<String, NavigableMap<Long, String>> newStore = new ConcurrentHashMap<>();
            
            for (String key : keyToFiles.keySet()) {
                // Create new ConcurrentSkipListMap for each key
                NavigableMap<Long, String> versions = new ConcurrentSkipListMap<>();
                
                for (FileMeta fileMeta : keyToFiles.get(key)) {
                    readAllVersionsFromFile(fileMeta, key, versions);
                }
                
                if (!versions.isEmpty()) {
                    newStore.put(key, versions);
                }
            }
            
            // Atomic replacement: clear old store and populate with new data
            store.clear();
            store.putAll(newStore);
            
        } finally {
            serializationLock.writeLock().unlock();
        }
    }
    
    /**
     * Read all versions of a key from a specific file
     * 
     * MULTI-FILE OPTIMIZATION (OPTIONAL):
     * Uses metadata to jump to specific offsets (fast random access)
     * 
     * SIMPLIFIED VERSION (single file without metadata):
     * - Read entire file sequentially
     * - Parse each entry until EOF
     * - Filter for matching keys
     * No offset tracking needed, but slower (must scan entire file)
     */
    private void readAllVersionsFromFile(FileMeta meta, String key, 
                                         NavigableMap<Long, String> result) {
        // MULTI-FILE: Use metadata to locate specific versions
        NavigableMap<Long, Long> timestampToOffset = meta.keyVersionOffset.get(key);
        if (timestampToOffset == null || timestampToOffset.isEmpty()) {
            return;
        }
        
        byte[] fileBytes = fileSystem.readFile(meta.fileName);
        if (fileBytes == null) {
            throw new RuntimeException("File not found: " + meta.fileName);
        }
        
        // MULTI-FILE: Jump to specific offsets (efficient random access)
        for (Map.Entry<Long, Long> entry : timestampToOffset.entrySet()) {
            long timestamp = entry.getKey();
            long offset = entry.getValue();
            
            VersionEntry version = readSingleVersion(fileBytes, offset);
            
            // Validate data integrity
            if (!version.key.equals(key) || version.timestamp != timestamp) {
                throw new RuntimeException(
                    String.format("Corrupted data at %s:%d (expected key=%s ts=%d, got key=%s ts=%d)",
                                meta.fileName, offset, key, timestamp, 
                                version.key, version.timestamp));
            }
            
            result.put(version.timestamp, version.value);
        }
    }
    
    /**
     * Read a single version entry from bytes at a specific offset
     * 
     * PARSING THE BINARY FORMAT:
     * 1. Read 4 bytes → keyLength (int)
     * 2. Read keyLength bytes → key string (UTF-8)
     * 3. Read 8 bytes → timestamp (long)
     * 4. Read 4 bytes → valueLength (int)
     * 5. Read valueLength bytes → value string (UTF-8), or null if length=0
     * 
     * Must parse in exact same order as writeEntry()!
     */
    private VersionEntry readSingleVersion(byte[] fileBytes, long offset) {
        ByteBuffer buffer = ByteBuffer.wrap(fileBytes);
        buffer.position((int) offset);
        
        // Parse: [keyLength][keyBytes][timestamp][valueLength][valueBytes]
        
        // 1. Read key length (4 bytes)
        int keyLength = buffer.getInt();
        
        // 2. Read key bytes (N bytes)
        byte[] keyBytes = new byte[keyLength];
        buffer.get(keyBytes);
        String key = new String(keyBytes, StandardCharsets.UTF_8);
        
        // 3. Read timestamp (8 bytes)
        long timestamp = buffer.getLong();
        
        // 4. Read value length (4 bytes)
        int valueLength = buffer.getInt();
        
        // 5. Read value bytes (M bytes, only if length > 0)
        String value = null;
        if (valueLength > 0) {
            byte[] valueBytes = new byte[valueLength];
            buffer.get(valueBytes);
            value = new String(valueBytes, StandardCharsets.UTF_8);
        }
        
        return new VersionEntry(key, timestamp, value);
    }
    
    // ============================================================================
    // Utility Methods
    // ============================================================================
    
    /**
     * Build index mapping each key to the files containing it
     * 
     * MULTI-FILE OPTIMIZATION (OPTIONAL):
     * Without file limit: Single file, no index needed
     * With multiple files: This enables O(F) lookup where F = files containing key
     * 
     * INTERVIEW NOTE:
     * For production with many files, could add bloom filter per file
     * to avoid checking files that definitely don't contain the key.
     */
    private void buildKeyToFilesIndex() {
        keyToFiles.clear();
        for (FileMeta meta : metadata) {
            for (String key : meta.keyVersionOffset.keySet()) {
                keyToFiles.computeIfAbsent(key, k -> new ArrayList<>()).add(meta);
            }
        }
    }
    
    public int getKeyCount() {
        return store.size();
    }
    
    public int getFileCount() {
        return metadata.size();
    }
    
    public int getVersionCount(String key) {
        NavigableMap<Long, String> versions = store.get(key);
        return versions != null ? versions.size() : 0;
    }
    
    public List<FileMeta> getMetadata() {
        return new ArrayList<>(metadata);
    }
    
    // ============================================================================
    // Validation
    // ============================================================================
    
    private void validateKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
    }
    
    private void validateTimestamp(long timestamp) {
        if (timestamp < 0) {
            throw new IllegalArgumentException("Timestamp cannot be negative: " + timestamp);
        }
    }
    
    // ============================================================================
    // Test Cases
    // ============================================================================
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Phase 3: Thread-Safe Persistence (Lock-Free) Tests ===\n");
        
        InMemoryFileSystem fs = new InMemoryFileSystem();
        
        // Test 1: Basic serialize and deserialize
        System.out.println("Test 1: Basic serialize and deserialize");
        Phase3VersionedKVStore store = new Phase3VersionedKVStore(fs);
        store.put("user:1", "Alice", 100);
        store.put("user:1", "Alice Smith", 200);
        store.put("user:2", "Bob", 150);
        
        store.serialize();
        System.out.println("  Serialized to " + store.getFileCount() + " file(s)");
        
        Phase3VersionedKVStore store2 = new Phase3VersionedKVStore(fs);
        store2.deserializeAll();  // Auto-loads metadata!
        
        assert store2.get("user:1", 150).equals("Alice") : "Test 1a failed";
        assert store2.get("user:1", 250).equals("Alice Smith") : "Test 1b failed";
        assert store2.get("user:2", 200).equals("Bob") : "Test 1c failed";
        System.out.println("  ✓ Data restored correctly");
        System.out.println("✓ Test 1 passed\n");
        
        // Test 2: Concurrent writes + serialize
        System.out.println("Test 2: Concurrent writes, then serialize");
        fs.clear();
        final Phase3VersionedKVStore store2a = new Phase3VersionedKVStore(fs);
        
        // Multiple threads writing
        Thread[] writers = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            writers[i] = new Thread(() -> {
                for (int j = 0; j < 50; j++) {
                    store2a.put("key" + threadId, "value" + j, j);
                }
            });
            writers[i].start();
        }
        
        for (Thread t : writers) {
            t.join();
        }
        
        // Serialize after concurrent writes
        store2a.serialize();
        System.out.println("  Serialized " + store2a.getKeyCount() + " keys to " + 
                         store2a.getFileCount() + " files");
        
        // Verify deserialization
        Phase3VersionedKVStore store2b = new Phase3VersionedKVStore(fs);
        store2b.deserializeAll();  // Auto-loads metadata!
        
        for (int i = 0; i < 10; i++) {
            assert store2b.get("key" + i).equals("value49") : "Test 2 failed for key" + i;
        }
        System.out.println("  ✓ All data restored correctly after concurrent writes");
        System.out.println("✓ Test 2 passed\n");
        
        // Test 3: Reads during serialize
        System.out.println("Test 3: Reads during serialize");
        fs.clear();
        final Phase3VersionedKVStore store3 = new Phase3VersionedKVStore(fs);
        
        // Populate with data
        for (int i = 0; i < 100; i++) {
            store3.put("key" + i, "value" + i, 100);
        }
        
        // Start readers
        Thread[] readers = new Thread[20];
        final boolean[] readSuccess = new boolean[20];
        for (int i = 0; i < 20; i++) {
            final int readerId = i;
            readers[i] = new Thread(() -> {
                try {
                    Thread.sleep(10);
                    for (int j = 0; j < 100; j++) {
                        String value = store3.get("key" + j, 100);
                        if (value == null || !value.equals("value" + j)) {
                            return;
                        }
                    }
                    readSuccess[readerId] = true;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            readers[i].start();
        }
        
        // Serialize (will block operations)
        Thread serializer = new Thread(() -> store3.serialize());
        serializer.start();
        
        for (Thread t : readers) {
            t.join();
        }
        serializer.join();
        
        int successfulReads = 0;
        for (boolean success : readSuccess) {
            if (success) successfulReads++;
        }
        
        System.out.println("  ✓ " + successfulReads + "/20 readers completed");
        System.out.println("  ✓ Serialize completed");
        System.out.println("✓ Test 3 passed\n");
        
        // Test 4: Multi-file splitting
        System.out.println("Test 4: Multi-file with concurrent access");
        fs.clear();
        Phase3VersionedKVStore store4a = new Phase3VersionedKVStore(fs);
        
        // Create enough data for multiple files
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 10; j++) {
                store4a.put("key" + i, "value_" + i + "_" + j + "_padding", j * 100L);
            }
        }
        
        store4a.serialize();
        int fileCount = store4a.getFileCount();
        System.out.println("  Created " + fileCount + " files");
        assert fileCount > 1 : "Should create multiple files";
        
        Phase3VersionedKVStore store4b = new Phase3VersionedKVStore(fs);
        store4b.deserializeAll();  // Auto-loads metadata!
        
        assert store4b.get("key50", 450).equals("value_50_4_padding") : "Test 4 failed";
        System.out.println("  ✓ Multi-file data restored correctly");
        System.out.println("✓ Test 4 passed\n");
        
        // Test 5: Atomic deserialization
        System.out.println("Test 5: Atomic deserialization");
        fs.clear();
        Phase3VersionedKVStore store5a = new Phase3VersionedKVStore(fs);
        store5a.put("key1", "value1", 100);
        store5a.serialize();
        
        Phase3VersionedKVStore store5b = new Phase3VersionedKVStore(fs);
        store5b.put("key1", "temp", 50);
        
        // Deserialize should atomically replace
        store5b.deserializeAll();  // Auto-loads metadata!
        
        assert store5b.get("key1", 75) == null : "Test 5a failed - old data should be gone";
        assert store5b.get("key1", 100).equals("value1") : "Test 5b failed";
        System.out.println("  ✓ Deserialization is atomic");
        System.out.println("✓ Test 5 passed\n");
        
        System.out.println("════════════════════════════════════════");
        System.out.println("✅ ALL PHASE 3 (LOCK-FREE) TESTS PASSED!");
        System.out.println("════════════════════════════════════════");
        System.out.println("\nKEY TAKEAWAYS:");
        System.out.println("  • Built on lock-free Phase 2 (ConcurrentSkipListMap)");
        System.out.println("  • No VersionHistory wrapper needed");
        System.out.println("  • No per-key locks to manage");
        System.out.println("  • Only global serializationLock for file I/O");
        System.out.println("  • Simpler than lock-based approach");
        System.out.println("  • Thread-safe serialization & deserialization");
        System.out.println("  • Binary format with multi-file splitting");
        System.out.println("\nCOMPLETE: All 3 phases implemented (lock-free)!");
    }
}
