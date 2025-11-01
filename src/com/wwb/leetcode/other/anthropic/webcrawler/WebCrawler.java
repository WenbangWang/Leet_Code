package com.wwb.leetcode.other.anthropic.webcrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebCrawler {
    private static final Pattern HREF = Pattern.compile("href\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

    private final Queue<String> urls;
    private final AtomicInteger numberOfHTMLs;

    public WebCrawler() {
        this(new ArrayDeque<>());
    }

    public WebCrawler(Queue<String> urls) {
        this.urls = urls;
        this.numberOfHTMLs = new AtomicInteger();
    }

    public void crawl(String url) throws IOException {
        if (url == null || url.isEmpty()) {
            return;
        }

        URL u = new URL(url);
        String host = u.getHost();

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) u.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "NaiveCrawler/1.0");

            int code = connection.getResponseCode();

            if (code != 200) {
                return;
            }

            String contentType = connection.getContentType();

            if (contentType == null || contentType.equalsIgnoreCase("text/html")) {
                return;
            }

            StringBuilder sb = new StringBuilder();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            String html = sb.toString();
            this.numberOfHTMLs.incrementAndGet();

            Matcher m = HREF.matcher(html);
            while (m.find()) {
                String href = m.group(1);
                try {
                    URL resolved = new URL(u, href);
                    if (!resolved.getHost().equalsIgnoreCase(host)) {
                        continue;
                    }
                    String parsedUrl = parseUrl(resolved);

                    if (parsedUrl != null) {
                        urls.add(parsedUrl);
                    }
                } catch (Exception ignored) {}
            }

            System.out.println(this.urls);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    private String parseUrl(URL url) {
        try {
            URI uri = url.toURI().normalize();
            String s = uri.toString();
            if (s.contains("#")) {
                s = s.substring(0, s.indexOf("#"));
            }

            return s;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    static class RobotsCache {
        private final Map<String, RobotsRules> cache = new HashMap<>();

        boolean isAllowed(String urlStr, String userAgent) {
            try {
                URL url = new URL(urlStr);
                String hostKey = url.getProtocol() + "://" + url.getHost();
                RobotsRules rules = cache.computeIfAbsent(hostKey, k -> fetch(hostKey));
                return rules.isAllowed(url.getPath());
            } catch (Exception e) {
                return true; // fail open
            }
        }

        private RobotsRules fetch(String hostBase) {
            Set<String> disallows = new HashSet<>();
            try {
                URL robotsUrl = new URL(hostBase + "/robots.txt");
                HttpURLConnection conn = (HttpURLConnection) robotsUrl.openConnection();
                conn.setRequestProperty("User-Agent", "NaiveCrawler/1.0");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);

                if (conn.getResponseCode() != 200) return new RobotsRules(disallows);

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                boolean inStar = false;
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    if (line.toLowerCase().startsWith("user-agent:")) {
                        String ua = line.substring(11).trim().toLowerCase();
                        inStar = ua.equals("*");
                    } else if (inStar && line.toLowerCase().startsWith("disallow:")) {
                        String path = line.substring(9).trim();
                        if (!path.isEmpty()) disallows.add(path);
                    }
                }
                br.close();
            } catch (IOException ignored) {}
            return new RobotsRules(disallows);
        }

        static class RobotsRules {
            private final Set<String> disallows;
            RobotsRules(Set<String> disallows) { this.disallows = disallows; }

            boolean isAllowed(String path) {
                for (String dis : disallows) {
                    if (path.startsWith(dis)) return false;
                }
                return true;
            }
        }
    }
}
