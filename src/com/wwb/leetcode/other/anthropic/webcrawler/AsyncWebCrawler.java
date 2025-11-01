package com.wwb.leetcode.other.anthropic.webcrawler;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsyncWebCrawler {
    private static final Pattern HREF = Pattern.compile("href\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

    public AsyncWebCrawler() {

    }

    public CompletableFuture<List<String>> crawl(String url) {
        if (url == null || url.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        HttpClient client = HttpClient.newHttpClient();

        // Build an HttpRequest object
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url)) // Specify the target URI
            .GET() // Set the request method to GET
            .build();

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        return response.thenApply(res -> {
            if (res.statusCode() != 200) {
                return Collections.emptyList();
            }

            URL u = null;

            try {
                u = new URL(url);
            } catch (MalformedURLException e) {
                return Collections.emptyList();
            }

            String host = u.getHost();
            List<String> urls = new ArrayList<>();
            Matcher m = HREF.matcher(res.body());

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

            return urls;
        });
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
}
