package com.wwb.leetcode.other.openai.memoryallocator;

/**
 * Represents a memory block (either free or allocated).
 * 
 * Core data structure for the memory allocator.
 * Used to track both allocated and free memory regions.
 */
public class Block implements Comparable<Block> {
    private long address;      // Starting address
    private int size;          // Size in bytes
    private String ownerId;    // Owner ID (null for free blocks)
    private long timestamp;    // Allocation timestamp
    
    public Block(long address, int size) {
        this(address, size, null, 0);
    }
    
    public Block(long address, int size, String ownerId, long timestamp) {
        this.address = address;
        this.size = size;
        this.ownerId = ownerId;
        this.timestamp = timestamp;
    }
    
    // Getters and setters
    public long getAddress() {
        return address;
    }
    
    public void setAddress(long address) {
        this.address = address;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public String getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public long getEndAddress() {
        return address + size - 1;
    }
    
    public boolean isFree() {
        return ownerId == null;
    }
    
    @Override
    public int compareTo(Block other) {
        return Long.compare(this.address, other.address);
    }
    
    @Override
    public String toString() {
        return String.format("Block[addr=0x%04X, size=%d, owner=%s]", 
                           address, size, ownerId == null ? "FREE" : ownerId);
    }
    
    /**
     * Creates a copy of this block
     */
    public Block copy() {
        return new Block(address, size, ownerId, timestamp);
    }
}

