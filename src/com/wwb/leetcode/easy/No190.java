package com.wwb.leetcode.easy;

import java.util.HashMap;
import java.util.Map;

/**
 * Reverse bits of a given 32 bits unsigned integer.
 *
 * For example, given input 43261596 (represented in binary as 00000010100101000001111010011100),
 * return 964176192 (represented in binary as 00111001011110000010100101000000).
 *
 * Follow up:
 * If this function is called many times, how would you optimize it?
 */
public class No190 {

    private final Map<Byte, Integer> cache = new HashMap<>();

    public int reverseBits(int n) {
        return solution1(n);
    }

    private int solution1(int n) {
        int result = 0;

        for(int i = 0; i < 32; i++) {
            result += n & 1;
            //unsigned shift
            n >>>= 1;
            //shift all other than the last digit
            if(i < 31) {
                result <<= 1;
            }
        }

        return result;
    }

    private int solution2(int n) {
        byte[] bytes = new byte[4];
        int result = 0;

        for(int i = 0; i < 4; i++) {
            bytes[i] = (byte) ((n >>> 8 * i) & 0xFF);
        }

        for(int i = 0; i < 4; i++) {
            result += reverseByte(bytes[i]);
            if(i < 4 - 1) {
                result <<= 8;
            }
        }

        return result;
    }

    private int reverseByte(byte b) {
        Integer result = cache.get(b);

        if(result != null) {
            return result;
        }

        result = 0;
        for(int i = 0; i < 8; i++) {
            result += ((b >>> i) & 1);
            if(i < 7) {
                result <<= 1;
            }
        }

        cache.put(b, result);
        return result;
    }
}