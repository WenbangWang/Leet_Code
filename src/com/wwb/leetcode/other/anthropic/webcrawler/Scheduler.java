package com.wwb.leetcode.other.anthropic.webcrawler;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class Scheduler {
    private final Queue<String> urls;
    private final Set<String> visited;
    private final WebCrawler crawler;
    private final AtomicInteger numberOfHTMLs;

    public Scheduler() {
        this.urls = new ArrayDeque<>();
        this.visited = new HashSet<>();
        this.crawler = new WebCrawler(this.urls);
        this.numberOfHTMLs = new AtomicInteger();
    }

    public void run() throws IOException {
        this.urls.offer("https://andyljones.com");

        while (!this.urls.isEmpty()) {
            String url = this.urls.poll();

            if (this.visited.add(url)) {
                this.crawler.crawl(url);
            }
        }
        System.out.println(String.format("Number of pages visited: '%d'", this.visited.size()));
    }

    public void runAsync() throws ExecutionException, InterruptedException {
        this.urls.offer("https://andyljones.com");
        AsyncWebCrawler crawler = new AsyncWebCrawler();

        while (!this.urls.isEmpty()) {
            this.numberOfHTMLs.incrementAndGet();
            String url = this.urls.poll();

            if (this.visited.add(url)) {
                this.urls.addAll(crawler.crawl(url).get());
            }
        }

        System.out.println(String.format("Number of pages visited: '%d'", this.visited.size()));
    }
}
