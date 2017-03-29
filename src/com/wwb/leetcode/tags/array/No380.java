package com.wwb.leetcode.tags.array;

import java.util.*;

/**
 * Design a data structure that supports all following operations in average O(1) time.
 *
 * insert(val): Inserts an item val to the set if not already present.
 * remove(val): Removes an item val from the set if present.
 * getRandom: Returns a random element from current set of elements. Each element must have the same probability of being returned.
 * Example:
 *
 * // Init an empty set.
 * RandomizedSet randomSet = new RandomizedSet();
 *
 * // Inserts 1 to the set. Returns true as 1 was inserted successfully.
 * randomSet.insert(1);
 *
 * // Returns false as 2 does not exist in the set.
 * randomSet.remove(2);
 *
 * // Inserts 2 to the set, returns true. Set now contains [1,2].
 * randomSet.insert(2);
 *
 * // getRandom should return either 1 or 2 randomly.
 * randomSet.getRandom();
 *
 * // Removes 1 from the set, returns true. Set now contains [2].
 * randomSet.remove(1);
 *
 * // 2 was already in the set, so return false.
 * randomSet.insert(2);
 *
 * // Since 2 is the only number in the set, getRandom always return 2.
 * randomSet.getRandom();
 */
public class No380 {
    public class RandomizedSet {
        private Map<Integer, Integer> map;
        private List<Integer> list;
        private Random random;


        /** Initialize your data structure here. */
        public RandomizedSet() {
            this.map = new HashMap<>();
            this.list = new ArrayList<>();
            this.random = new Random();
        }

        /** Inserts a value to the set. Returns true if the set did not already contain the specified element. */
        public boolean insert(int val) {
            if(this.has(val)) {
                return false;
            }

            int index = this.list.size();
            this.list.add(val);
            this.map.put(val, index);
            return true;
        }

        /** Removes a value from the set. Returns true if the set contained the specified element. */
        public boolean remove(int val) {
            if(!this.has(val)) {
                return false;
            }

            int index = this.map.get(val);

            if(index < this.list.size() - 1) {
                int lastNum = this.list.get(this.list.size() - 1);
                this.list.set(index, lastNum);
                this.map.put(lastNum, index);
            }

            this.list.remove(this.list.size() - 1);
            this.map.remove(val);
            return true;
        }

        public boolean has(int val) {
            return this.map.containsKey(val);
        }

        /** Get a random element from the set. */
        public int getRandom() {
            return this.list.get(this.random.nextInt(this.list.size()));
        }
    }

/**
 * Your RandomizedSet object will be instantiated and called as such:
 * RandomizedSet obj = new RandomizedSet();
 * boolean param_1 = obj.insert(val);
 * boolean param_2 = obj.remove(val);
 * int param_3 = obj.getRandom();
 */
}
