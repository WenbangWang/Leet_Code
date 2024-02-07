package com.wwb.leetcode.other;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StandardCache<K, V extends StandardCache.Rankable> {

    private DataSource<K, V> ds;
    private Rankable ranker;
    private int capacity;
    private Map<K, Data<K, V>> cache;
    private TreeMap<Long, LinkedList<K>> ranks;
    private ReentrantReadWriteLock lock;
    private ReentrantReadWriteLock.ReadLock rLock;
    private ReentrantReadWriteLock.WriteLock wLock;

    /**
     * Constructor with a data source (assumed to be slow) and a cache size
     * @param ds the persistent layer of the the cache
     * @param capacity the number of entries that the cache can hold
     */
    public StandardCache(DataSource<K, V> ds, Rankable ranker, int capacity) {
        // Your code here
        this.ds = ds;
        this.ranker = ranker;
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.ranks = new TreeMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.rLock = this.lock.readLock();
        this.wLock = this.lock.writeLock();
    }

    /**
     * Gets some data. If possible, retrieves it from cache to be fast. If the data is not cached,
     * retrieves it from the data source. If the cache is full, attempt to cache the returned data,
     * evicting the V with lowest rank among the ones that it has available
     * If there is a tie, the cache may choose any V with lowest rank to evict.
     * @param key the key of the cache entry being queried
     * @return the Rankable value of the cache entry
     */
    public V get(K key) {
        this.rLock.lock();

        try {
            if (this.cache.containsKey(key)) {
                return this.cache.get(key).value;
            }
        } finally {
            this.rLock.unlock();
        }

        this.wLock.lock();

        try {
            // Read the cache again to avoid double writing from other thread.
            if (this.cache.containsKey(key)) {
                return this.cache.get(key).value;
            }

            evictLowestRankedData();

            V value = this.ds.get(key);
            Data<K, V> data = new Data<>(value.getRank(), key, value);
            this.cache.put(key, data);
            this.ranks.putIfAbsent(data.rank, new LinkedList<>());
            this.ranks.get(data.rank).add(key);

            return value;
        } finally {
            this.wLock.unlock();
        }
    }

    private void evictLowestRankedData() {
        if (this.cache.size() == this.capacity) {
            long evictedKeyRank = this.ranks.firstKey();
            LinkedList<K> lowestRankKeys = this.ranks.get(evictedKeyRank);
            lowestRankKeys.removeFirst();
            if (lowestRankKeys.isEmpty()) {
                this.ranks.remove(evictedKeyRank);
            }
        }
    }


    private static class Data<K, V> {
        long rank;
        K key;
        V value;

        public Data(long rank, K key, V value){
            this.rank = rank;
            this.key=key;
            this.value=value;
        }
    }

    /*
     * For reference, relevant interfaces: Rankable and DataSource.
     */

    public interface Rankable {
        /**
         * Returns the Rank of this object, using some algorithm and potentially
         * the internal state of the Rankable.
         */
        long getRank();
    }

    /*
     * Remember that this is slow, relatively speaking
     */
    public interface DataSource<K, V> {
        V get (K key);
    }
}
