package com.browser.service;

import javafx.application.Platform;
import org.json.JSONArray;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SearchSuggestionService {
    private final HttpClient client;
    private CompletableFuture<Void> currentTask;

    public SearchSuggestionService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public void fetchSuggestions(String query, Consumer<List<String>> callback) {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }

        if (query == null || query.trim().length() < 2) {
            callback.accept(new ArrayList<>());
            return;
        }

        String trimmed = query.trim();
        // If it looks like a direct URL or local path, don't query
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file://") || trimmed.contains(" ")) {
            // query still valid for multiple words
        }

        currentTask = CompletableFuture.runAsync(() -> {
            try {
                String encoded = URLEncoder.encode(trimmed, StandardCharsets.UTF_8);
                String url = "https://suggestqueries.google.com/complete/search?client=chrome&q=" + encoded;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(3))
                        .header("User-Agent", "Mozilla/5.0")
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() == 200) {
                    JSONArray arr = new JSONArray(response.body());
                    if (arr.length() >= 2) {
                        JSONArray suggestionsArr = arr.getJSONArray(1);
                        List<String> results = new ArrayList<>();
                        for (int i = 0; i < Math.min(suggestionsArr.length(), 6); i++) {
                            results.add(suggestionsArr.getString(i));
                        }
                        Platform.runLater(() -> callback.accept(results));
                        return;
                    }
                }
            } catch (Exception ignored) {
                // Ignore network errors or cancellation
            }
            Platform.runLater(() -> callback.accept(new ArrayList<>()));
        });
    }
}
