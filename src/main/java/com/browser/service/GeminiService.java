package com.browser.service;

import com.browser.ai.NativeAiEngine;
import javafx.application.Platform;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GeminiService {
    private static final String FASTAPI_ENDPOINT = "http://127.0.0.1:8765/api/chat";
    private static final String FASTAPI_HEALTH = "http://127.0.0.1:8765/health";

    private final ConfigManager configManager;
    private final NativeAiEngine nativeAiEngine;
    private final HttpClient client;
    private boolean fastApiStarted = false;

    public GeminiService(ConfigManager configManager) {
        this.configManager = configManager;
        this.nativeAiEngine = new NativeAiEngine();
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        ensureFastApiServerRunning();
    }

    private synchronized void ensureFastApiServerRunning() {
        if (fastApiStarted) return;
        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest healthReq = HttpRequest.newBuilder()
                        .uri(URI.create(FASTAPI_HEALTH))
                        .timeout(Duration.ofSeconds(2))
                        .GET()
                        .build();
                HttpResponse<String> resp = client.send(healthReq, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    fastApiStarted = true;
                    return;
                }
            } catch (Exception ignored) {
                // Server not yet reachable, spawn background process
            }

            try {
                File script = new File("ai_service.py");
                if (script.exists()) {
                    ProcessBuilder pb = new ProcessBuilder("python", "ai_service.py");
                    pb.directory(new File("."));
                    pb.redirectErrorStream(true);
                    pb.start();
                    fastApiStarted = true;
                }
            } catch (Exception e) {
                System.err.println("Could not auto-start FastAPI service: " + e.getMessage());
            }
        });
    }

    public void ask(String prompt, String pageTitle, String pageUrl, String pageText,
                    BiConsumer<String, JSONObject> onSuccess,
                    Consumer<String> onError) {
        CompletableFuture.runAsync(() -> {
            // First attempt: FastAPI LLM Chatbot Service with Action Execution
            try {
                JSONObject payload = new JSONObject();
                payload.put("prompt", prompt);
                payload.put("page_title", pageTitle != null ? pageTitle : "");
                payload.put("page_url", pageUrl != null ? pageUrl : "");
                payload.put("page_text", pageText != null ? pageText : "");
                payload.put("model", "gpt-4o-mini");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(FASTAPI_ENDPOINT))
                        .timeout(Duration.ofSeconds(25))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() == 200) {
                    JSONObject resObj = new JSONObject(response.body());
                    String aiText = resObj.optString("response", "");
                    JSONObject action = resObj.optJSONObject("action");
                    if (!aiText.isBlank()) {
                        Platform.runLater(() -> onSuccess.accept(aiText, action));
                        return;
                    }
                }
            } catch (Exception ignored) {
                // FastAPI still booting or network timeout, seamless fallback
            }

            // Fallback: Autonomous Native Engine with Action Execution
            NativeAiEngine.AiExecutionResult fallback = nativeAiEngine.thinkAndExecute(prompt, pageTitle, pageUrl, pageText);
            Platform.runLater(() -> onSuccess.accept(fallback.response, fallback.action));
        });
    }

    public NativeAiEngine getNativeAiEngine() {
        return nativeAiEngine;
    }
}
