package com.wwb.leetcode.other.anthropic.webcrawler;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConcurrentScheduler {
    private final BlockingQueue<String> urls;
    private final Set<String> visited;
    private final WebCrawler crawler;
    private final AsyncWebCrawler asyncWebCrawler;
    private final ExecutorService executor;
    private final CompletionService<Void> completionService;
    private final int numWorkers = 10;

    public ConcurrentScheduler() {
        this.urls = new LinkedBlockingQueue<>();
        this.visited = new HashSet<>();
        this.crawler = new WebCrawler(this.urls);
        this.asyncWebCrawler = new AsyncWebCrawler();
        this.executor = Executors.newFixedThreadPool(numWorkers);
        this.completionService = new ExecutorCompletionService<>(executor);
    }

    public void run() throws InterruptedException, ExecutionException {
        int tasksSubmitted = 0;
        int tasksCompleted = 0;

        String seed = "https://andyljones.com";
        this.visited.add(seed);
        submitCrawl(seed);
        tasksSubmitted++;

        while (tasksCompleted < tasksSubmitted) {
            Future<Void> future = completionService.take(); // wait for a task to complete
            future.get(); // propagate exceptions if any
            tasksCompleted++;

            // TODO check max depth and max page crawled here by comparing with size of visited
            // max depth can be done by changing the string url into a class which has url and depth

            // Submit any new URLs discovered
            while (!urls.isEmpty()) {
                String url = urls.poll(1, TimeUnit.SECONDS);
                if (url != null && visited.add(url)) { // add returns true if it was not already present
                    submitCrawl(url);
                    tasksSubmitted++;
                }
            }
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);

        System.out.println("Number of pages visited: " + visited.size());
    }

    public void runAsync() throws InterruptedException {
        this.crawl("https://andyljones.com", 0).join();
        System.out.println("Number of pages visited: " + visited.size());

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
    }

    private CompletableFuture<Void> crawl(String url, int depth) {
        // TODO check max depth and check max page crawled here by comparing with size of visited
        if (!this.visited.add(url)) {
            return CompletableFuture.completedFuture(null);
        }

//        int count = visitedCount.incrementAndGet();
//        if (count > MAX_PAGES) {
//            return CompletableFuture.completedFuture(null);
//        }

        return this.asyncWebCrawler.crawl(url).thenComposeAsync(urls -> {
            List<CompletableFuture<Void>> futures = urls.stream()
                .map(u -> crawl(u, depth + 1))
                .toList();
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }, this.executor);
    }

    private void submitCrawl(String url) {
        completionService.submit(() -> {
            try {
                crawler.crawl(url);
            } catch (IOException  e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
