package com.wwb.leetcode.other.anthropic.webcrawler;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        ConcurrentScheduler scheduler = new ConcurrentScheduler();
//        Scheduler scheduler = new Scheduler();
        scheduler.runAsync();
    }
}
