package com.wwb.leetcode.other.openai.memoryallocator;

/**
 * Metadata about an allocation.
 * Returned by queries to show allocation details.
 */
public class AllocationInfo {
    private final String address;
    private final int size;
    private final long timestamp;
    private final String ownerId;
    
    public AllocationInfo(String address, int size, long timestamp, String ownerId) {
        this.address = address;
        this.size = size;
        this.timestamp = timestamp;
        this.ownerId = ownerId;
    }
    
    public String getAddress() {
        return address;
    }
    
    public int getSize() {
        return size;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getOwnerId() {
        return ownerId;
    }
    
    @Override
    public String toString() {
        return String.format("Allocation[addr=%s, size=%d, owner=%s, time=%d]",
                           address, size, ownerId, timestamp);
    }
}

