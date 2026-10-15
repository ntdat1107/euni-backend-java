package com.euni.backend.service;

import com.euni.backend.dto.request.AiGenerateRequest;
import com.euni.backend.entity.User;
import com.euni.backend.entity.UserAiCredential;
import com.euni.backend.entity.UserAiModelConfig;
import com.euni.backend.entity.enums.AiProvider;
import com.euni.backend.repository.UserAiCredentialRepository;
import com.euni.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAiService {

    @Value("${gemini.api-key:}")
    private String systemApiKey;

    @Value("${gemini.model:gemini-2.0-flash-lite}")
    private String systemModel;

    private final AiPromptResolver promptResolver;
    private final OpenAiStreamingClient openAiStreamingClient;
    private final UserRepository userRepository;
    private final UserAiCredentialRepository credentialRepository;
    private final UserAiModelConfigService modelConfigService;
    private final CryptoService cryptoService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final List<String> FALLBACK_MODELS = List.of(
            "gemini-2.0-flash-lite", "gemini-2.0-flash", "gemini-1.5-flash", "gemini-1.5-pro"
    );

    public record ResolvedConfig(AiProvider provider, String apiKey, String model, String baseUrl,
                                  Double temperature, Integer maxTokens) {}

    public void streamGenerate(AiGenerateRequest request, SseEmitter emitter) {
        streamGenerate(request, emitter, null);
    }

    public void streamGenerate(AiGenerateRequest request, SseEmitter emitter, String username) {
        long startTime = System.currentTimeMillis();
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        log.info("[AI-TRACE][{}] Yêu cầu AI | Type: {}, User: {}", traceId, request.getContentType(), username);

        ResolvedConfig config = resolveConfig(username, request.getContentType());
        if (config == null || config.apiKey() == null || config.apiKey().isBlank()) {
            log.warn("[AI-TRACE][{}] Không tìm thấy API Key (User hoặc System). Dùng mock fallback.", traceId);
            streamMockResponse(emitter);
            return;
        }

        String promptText = promptResolver.buildFullPrompt(request);

        if (config.provider() == AiProvider.OPENAI || config.provider() == AiProvider.DEEPSEEK || config.provider() == AiProvider.CUSTOM) {
            openAiStreamingClient.streamChatCompletion(config.baseUrl(), config.apiKey(), config.model(),
                    config.temperature(), config.maxTokens(), promptText, emitter, traceId);
            return;
        }

        executeGeminiStreaming(config, promptText, emitter, traceId, startTime);
    }

    private ResolvedConfig resolveConfig(String username, String contentType) {
        if (username != null && !username.isBlank()) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                Long userId = userOpt.get().getId();
                Optional<UserAiModelConfig> modelOpt = modelConfigService.getEffectiveConfig(userId, contentType);
                if (modelOpt.isPresent() && modelOpt.get().getCredential() != null && modelOpt.get().getCredential().isActive()) {
                    return toResolvedConfig(modelOpt.get().getCredential(), modelOpt.get().getModelName(),
                            modelOpt.get().getTemperature(), modelOpt.get().getMaxTokens());
                }
                List<UserAiCredential> activeCreds = credentialRepository.findActiveByUserId(userId);
                if (!activeCreds.isEmpty()) {
                    return toResolvedConfig(activeCreds.get(0), null, 0.5, 4096);
                }
            }
        }
        if (systemApiKey != null && !systemApiKey.isBlank()) {
            return new ResolvedConfig(AiProvider.GEMINI, systemApiKey.trim(), systemModel, null, 0.5, 4096);
        }
        return null;
    }

    private ResolvedConfig toResolvedConfig(UserAiCredential cred, String model, Double temp, Integer maxTokens) {
        String decryptedKey = cryptoService.decrypt(cred.getEncryptedApiKey(), cred.getEncryptionIv());
        String effectiveModel = model != null && !model.isBlank() ? model :
                (cred.getProvider() == AiProvider.GEMINI ? "gemini-2.0-flash-lite" : "gpt-4o-mini");
        return new ResolvedConfig(cred.getProvider(), decryptedKey, effectiveModel, cred.getBaseUrl(), temp, maxTokens);
    }

    private void executeGeminiStreaming(ResolvedConfig config, String promptText, SseEmitter emitter, String traceId, long startTime) {
        try {
            Map<String, Object> reqBody = Map.of(
                    "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", promptText)))),
                    "generationConfig", Map.of("temperature", config.temperature(), "maxOutputTokens", config.maxTokens())
            );
            String jsonPayload = objectMapper.writeValueAsString(reqBody);
            String targetModel = sanitizeModelName(config.model());

            HttpResponse<InputStream> response = executeGeminiRequest(targetModel, jsonPayload, config.apiKey(), traceId);
            if (response != null && response.statusCode() == 404) {
                response = retryFallbackModels(targetModel, jsonPayload, config.apiKey(), traceId);
            }

            if (response == null || response.statusCode() != 200) {
                int code = response != null ? response.statusCode() : 500;
                String err = readResponseBody(response);
                emitter.send(SseEmitter.event().data("<p><strong>Lỗi Gemini (HTTP " + code + "):</strong> " + err + "</p>"));
                emitter.complete();
                return;
            }

            streamGeminiChunks(response.body(), emitter, traceId, startTime);
            emitter.complete();
        } catch (Exception e) {
            log.error("[AI-TRACE][{}] Gemini streaming failed: {}", traceId, e.getMessage());
            try { emitter.completeWithError(e); } catch (Exception ignored) {}
        }
    }

    private HttpResponse<InputStream> executeGeminiRequest(String model, String payload, String key, String traceId) throws Exception {
        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:streamGenerateContent?alt=sse&key=%s",
                model, key.trim());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(60))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
    }

    private HttpResponse<InputStream> retryFallbackModels(String originalModel, String payload, String key, String traceId) {
        for (String fallback : FALLBACK_MODELS) {
            if (fallback.equalsIgnoreCase(originalModel)) continue;
            try {
                HttpResponse<InputStream> res = executeGeminiRequest(fallback, payload, key, traceId);
                if (res != null && res.statusCode() == 200) return res;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void streamGeminiChunks(InputStream is, SseEmitter emitter, String traceId, long startTime) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data: ")) continue;
                String data = line.substring(6).trim();
                if ("[DONE]".equalsIgnoreCase(data)) break;
                try {
                    JsonNode root = objectMapper.readTree(data);
                    JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
                    if (parts.isArray() && !parts.isEmpty()) {
                        String chunk = parts.get(0).path("text").asText("");
                        if (!chunk.isEmpty()) emitter.send(chunk);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private String sanitizeModelName(String raw) {
        if (raw == null || raw.isBlank()) return "gemini-2.0-flash-lite";
        String t = raw.trim();
        return t.startsWith("models/") ? t.substring(7) : t;
    }

    private String readResponseBody(HttpResponse<InputStream> res) {
        if (res == null || res.body() == null) return "Unknown";
        try (InputStream is = res.body()) { return new String(is.readAllBytes(), StandardCharsets.UTF_8); }
        catch (Exception e) { return "Error reading body"; }
    }

    private void streamMockResponse(SseEmitter emitter) {
        try {
            String mock = "<p><strong>Kết quả giả lập:</strong> Bạn chưa thiết lập API Key cá nhân và hệ thống chưa có default key. Vui lòng vào Cài đặt để thêm API Key.</p>";
            emitter.send(mock);
            emitter.complete();
        } catch (Exception e) {
            try { emitter.completeWithError(e); } catch (Exception ignored) {}
        }
    }
}
