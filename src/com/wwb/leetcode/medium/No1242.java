package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Build a web crawler that performs breadth-first search (BFS) traversal starting from a seed URL, discovering and fetching all reachable pages within the same domain. Implement concurrent fetching using multithreading to improve crawling speed while maintaining thread safety for shared data structures like the URL queue and visited set. For example, starting from https://example.com, the crawler should visit https://example.com/about and https://example.com/contact but skip https://other.com.
 * <p>
 * Input:
 * <p>
 * start_url = "https://example.com"
 * max_threads = 4
 * <p>
 * Page structure:
 * example.com → [example.com/about, example.com/blog]
 * example.com/about → [example.com/contact]
 * example.com/blog → [example.com/about, external.com]
 * Output:
 * <p>
 * Crawled URLs (in BFS order):
 * 1. https://example.com
 * 2. https://example.com/about
 * 3. https://example.com/blog
 * 4. https://example.com/contact
 * <p>
 * Total pages crawled: 4
 * (external.com skipped - different domain)
 * <p>
 * Explanation: The crawler uses BFS to visit pages level-by-level, employs 4 threads for concurrent fetching, and only crawls URLs within the same domain while tracking visited pages to avoid duplicates.
 * <p>
 * Constraints:
 * <p>
 * Only crawl URLs within the same domain as the starting URL
 * Use BFS traversal order for discovering URLs
 * Implement thread-safe access to shared queue and visited set
 * Handle concurrent URL fetching with multiple threads
 * Avoid crawling the same URL twice
 */
public class No1242 {

    interface HtmlParser {
        List<String> getUrls(String url);
    }

    static class Solution {
        public List<String> crawl(String startUrl, HtmlParser htmlParser) {
            Set<String> visited = ConcurrentHashMap.newKeySet();
            String hostname = getHostname(startUrl);
            ExecutorService executor = Executors.newFixedThreadPool(4);

            visited.add(startUrl);
            List<String> frontier = new ArrayList<>(List.of(startUrl));

            while (!frontier.isEmpty()) {
                List<String> discovered = fetchAll(frontier, executor, htmlParser).join();

                List<String> nextFrontier = new ArrayList<>();
                for (String next : discovered) {
                    if (next.startsWith(hostname) && visited.add(next)) {
                        nextFrontier.add(next);
                    }
                }
                frontier = nextFrontier;
            }

            executor.shutdown();
            return new ArrayList<>(visited);
        }

        private CompletableFuture<List<String>> fetchAll(List<String> urls, ExecutorService executor,
                HtmlParser htmlParser) {
            List<CompletableFuture<List<String>>> fetches = new ArrayList<>();
            for (String url : urls) {
                fetches.add(CompletableFuture.supplyAsync(() -> htmlParser.getUrls(url), executor)
                        .orTimeout(5, TimeUnit.SECONDS)
                        .exceptionally(ex -> Collections.emptyList()));
            }

            return CompletableFuture.allOf(fetches.toArray(new CompletableFuture[0]))
                    .thenApply(v -> {
                        List<String> result = new ArrayList<>();
                        for (CompletableFuture<List<String>> fetch : fetches) {
                            result.addAll(fetch.getNow(Collections.emptyList()));
                        }
                        return result;
                    });
        }

        private String getHostname(String url) {
            int idx = url.indexOf('/', 7);
            return idx != -1 ? url.substring(0, idx) : url;
        }
    }
}
