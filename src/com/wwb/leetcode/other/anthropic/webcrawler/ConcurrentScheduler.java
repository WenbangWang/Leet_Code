package com.wwb.leetcode.other.anthropic.webcrawler;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
        this.visited = ConcurrentHashMap.newKeySet();
        this.crawler = new WebCrawler(this.urls);
        this.asyncWebCrawler = new AsyncWebCrawler();
        this.executor = Executors.newFixedThreadPool(numWorkers);
        this.completionService = new ExecutorCompletionService<>(executor);
    }

    public void run() throws InterruptedException, ExecutionException {
        int tasksSubmitted = 0;
        int tasksCompleted = 0;

        String seed = "https://andyljones.com";
        visited.add(seed);
        submitCrawl(seed);
        tasksSubmitted++;

        while (tasksCompleted < tasksSubmitted) {
            completionService.take().get(); // wait for a task to complete, propagate exceptions
            tasksCompleted++;

            String url;
            while ((url = urls.poll()) != null) {
                if (visited.add(url)) { // add returns true if it was not already present
                    submitCrawl(url);
                    tasksSubmitted++;
                }
            }
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);

        System.out.println("Number of pages visited: " + visited.size());
    }

    public void runBfsLevelBarrier() throws InterruptedException, ExecutionException {
        List<String> currentLevel = List.of("https://andyljones.com");
        visited.add(currentLevel.get(0));

        while (!currentLevel.isEmpty()) {
            for (String url : currentLevel) {
                submitCrawl(url);
            }
            for (int i = 0; i < currentLevel.size(); i++) {
                completionService.take().get(); // wait out this whole level before moving on
            }

            List<String> nextLevel = new java.util.ArrayList<>();
            String url;
            while ((url = urls.poll()) != null) {
                if (visited.add(url)) { // add returns true if it was not already present
                    nextLevel.add(url);
                }
            }
            currentLevel = nextLevel;
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
