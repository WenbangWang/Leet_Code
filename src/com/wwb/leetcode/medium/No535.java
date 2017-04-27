package com.wwb.leetcode.medium;

import java.util.HashMap;
import java.util.Map;

/**
 * TinyURL is a URL shortening service where you enter a URL such as https://leetcode.com/problems/design-tinyurl
 * and it returns a short URL such as http://tinyurl.com/4e9iAk.
 *
 * Design the encode and decode methods for the TinyURL service.
 * There is no restriction on how your encode/decode algorithm should work.
 * You just need to ensure that a URL can be encoded to a tiny URL and the tiny URL can be decoded to the original URL.
 */
public class No535 {
    public class Codec {

    private char[] map;
    private Map<String, String> longUrlToShortUrlMap;
    private Map<Long, String> idToLongUrlMap;
    private long id;

    public Codec() {
        this.map = new char[62];
        for(char c = 'a'; c <= 'z'; c++) {
            this.map[c - 'a'] = c;
        }

        for(char c = 'A'; c <= 'Z'; c++) {
            this.map[c - 'A' + 26] = c;
        }

        for(int i = 0; i <= 9; i++) {
            this.map[i + 52] = (char) i;
        }

        this.longUrlToShortUrlMap = new HashMap<>();
        this.idToLongUrlMap = new HashMap<>();

        this.id = 0;
    }

    // Encodes a URL to a shortened URL.
    public String encode(String longUrl) {
        if(this.longUrlToShortUrlMap.containsKey(longUrl)) {
            return this.longUrlToShortUrlMap.get(longUrl);
        }
        this.id++;
        long count = this.id;
        StringBuilder result = new StringBuilder();

        while(count > 0) {
            int index = (int) (count % 62);
            result.append(this.map[index]);
            count /= 62;
        }

        String shortUrl = result.reverse().toString();

        this.idToLongUrlMap.put(this.id, longUrl);
        this.longUrlToShortUrlMap.put(longUrl, shortUrl);

        return shortUrl;
    }

    // Decodes a shortened URL to its original URL.
    public String decode(String shortUrl) {
        long id = 0;

        for(char c : shortUrl.toCharArray()) {
            if(c >= 'a' && c <= 'z') {
                id = id * 62 + c - 'a';
            } else if(c >= 'A' && c <= 'Z') {
                id = id * 62 + c - 'A' + 26;
            } else if(c >= '0' && c <= '9') {
                id = id * 62 + c - '0' + 52;
            }
        }

        if(this.idToLongUrlMap.containsKey(id)) {
            return this.idToLongUrlMap.get(id);
        }

        return "";
    }
}
}
