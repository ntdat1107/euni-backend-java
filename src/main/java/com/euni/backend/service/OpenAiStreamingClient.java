package com.euni.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiStreamingClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public void streamChatCompletion(String baseUrl, String apiKey, String model, Double temperature,
                                     Integer maxTokens, String promptText, SseEmitter emitter, String traceId) {
        String targetUrl = resolveTargetUrl(baseUrl);
        log.info("[AI-TRACE][{}] Khởi tạo OpenAI-compatible request | URL: {}, Model: {}", traceId, targetUrl, model);

        try {
            Map<String, Object> bodyMap = Map.of(
                    "model", model != null && !model.isBlank() ? model : "gpt-4o-mini",
                    "messages", List.of(Map.of("role", "user", "content", promptText)),
                    "temperature", temperature != null ? temperature : 0.5,
                    "max_tokens", maxTokens != null ? maxTokens : 4096,
                    "stream", true
            );
            String jsonPayload = objectMapper.writeValueAsString(bodyMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(90))
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                handleError(response, emitter, traceId);
                return;
            }

            processStream(response.body(), emitter, traceId);
            emitter.complete();
        } catch (Exception e) {
            log.error("[AI-TRACE][{}] OpenAI streaming failed: {}", traceId, e.getMessage());
            try { emitter.completeWithError(e); } catch (Exception ignored) {}
        }
    }

    private void processStream(InputStream is, SseEmitter emitter, String traceId) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data: ")) continue;
                String data = line.substring(6).trim();
                if ("[DONE]".equalsIgnoreCase(data)) break;

                try {
                    JsonNode root = objectMapper.readTree(data);
                    JsonNode choices = root.path("choices");
                    if (choices.isArray() && !choices.isEmpty()) {
                        String delta = choices.get(0).path("delta").path("content").asText("");
                        if (!delta.isEmpty()) emitter.send(delta);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private void handleError(HttpResponse<InputStream> response, SseEmitter emitter, String traceId) throws Exception {
        String errBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
        log.error("[AI-TRACE][{}] Provider error HTTP {}: {}", traceId, response.statusCode(), errBody);
        emitter.send(SseEmitter.event().data("<p><strong>Lỗi Nhà cung cấp AI (HTTP " + response.statusCode() + "):</strong> " + errBody + "</p>"));
        emitter.complete();
    }

    private String resolveTargetUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) return "https://api.openai.com/v1/chat/completions";
        String trimmed = baseUrl.trim();
        return trimmed.endsWith("/") ? trimmed + "chat/completions" : trimmed + "/chat/completions";
    }
}
