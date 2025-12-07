package com.wwb.leetcode.other.anthropic.webcrawler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Configuration for web crawler authentication.
 * Immutable configuration object for passing cookies and headers to HTTP requests.
 */
public final class CrawlerConfig {
    private final Optional<Map<String, String>> cookies;

    /**
     * Create config with cookies.
     * @param cookies Map of cookie name to cookie value. Can be null or empty.
     */
    public CrawlerConfig(Map<String, String> cookies) {
        this.cookies = (cookies == null || cookies.isEmpty())
            ? Optional.empty()
            : Optional.of(new HashMap<>(cookies)); // Defensive copy
    }

    /**
     * Create empty config (no authentication).
     */
    public CrawlerConfig() {
        this.cookies = Optional.empty();
    }

    /**
     * Get cookies as optional.
     */
    public Optional<Map<String, String>> getCookies() {
        return cookies;
    }

    /**
     * Format cookies as HTTP Cookie header value.
     * @return Cookie header string like "key1=value1; key2=value2" or empty string if no cookies
     */
    public String formatCookieHeader() {
        if (!cookies.isPresent()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        cookies.get().forEach((key, value) -> {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(key).append("=").append(value);
        });
        return sb.toString();
    }
}

