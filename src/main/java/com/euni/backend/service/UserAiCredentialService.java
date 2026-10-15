package com.euni.backend.service;

import com.euni.backend.dto.UserAiCredentialDto;
import com.euni.backend.dto.request.TestConnectionRequest;
import com.euni.backend.dto.request.UserAiCredentialRequest;
import com.euni.backend.dto.response.TestConnectionResponse;
import com.euni.backend.entity.User;
import com.euni.backend.entity.UserAiCredential;
import com.euni.backend.entity.enums.AiProvider;
import com.euni.backend.repository.UserAiCredentialRepository;
import com.euni.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAiCredentialService {

    private final UserAiCredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final CryptoService cryptoService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Transactional(readOnly = true)
    public List<UserAiCredentialDto> getCredentialsByUsername(String username) {
        User user = getUserByUsername(username);
        return credentialRepository.findByUserIdAndDeletedFalse(user.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public UserAiCredentialDto createCredential(String username, UserAiCredentialRequest req) {
        if (req.getApiKey() == null || req.getApiKey().trim().isEmpty()) {
            throw new IllegalArgumentException("API Key không được để trống");
        }
        User user = getUserByUsername(username);

        CryptoService.EncryptedResult encrypted = cryptoService.encrypt(req.getApiKey().trim());

        UserAiCredential credential = UserAiCredential.builder()
                .user(user)
                .provider(req.getProvider())
                .keyAlias(req.getKeyAlias().trim())
                .encryptedApiKey(encrypted.ciphertext())
                .encryptionIv(encrypted.iv())
                .baseUrl(req.getBaseUrl() != null && !req.getBaseUrl().trim().isEmpty() ? req.getBaseUrl().trim() : null)
                .active(req.getActive() != null ? req.getActive() : true)
                .build();

        UserAiCredential saved = credentialRepository.save(credential);
        return toDto(saved);
    }

    @Transactional
    public UserAiCredentialDto updateCredential(String username, Long id, UserAiCredentialRequest req) {
        User user = getUserByUsername(username);
        UserAiCredential credential = credentialRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình API Key"));

        credential.setKeyAlias(req.getKeyAlias().trim());
        if (req.getProvider() != null) {
            credential.setProvider(req.getProvider());
        }
        if (req.getBaseUrl() != null) {
            credential.setBaseUrl(req.getBaseUrl().trim().isEmpty() ? null : req.getBaseUrl().trim());
        }
        if (req.getActive() != null) {
            credential.setActive(req.getActive());
        }

        if (req.getApiKey() != null && !req.getApiKey().trim().isEmpty()) {
            CryptoService.EncryptedResult encrypted = cryptoService.encrypt(req.getApiKey().trim());
            credential.setEncryptedApiKey(encrypted.ciphertext());
            credential.setEncryptionIv(encrypted.iv());
        }

        UserAiCredential saved = credentialRepository.save(credential);
        return toDto(saved);
    }

    @Transactional
    public void deleteCredential(String username, Long id) {
        User user = getUserByUsername(username);
        UserAiCredential credential = credentialRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình API Key"));
        credential.setDeleted(true);
        credentialRepository.save(credential);
    }

    public TestConnectionResponse testConnection(String username, TestConnectionRequest req) {
        String effectiveApiKey = req.getApiKey();
        String effectiveBaseUrl = req.getBaseUrl();
        AiProvider provider = req.getProvider();

        if ((effectiveApiKey == null || effectiveApiKey.trim().isEmpty()) && req.getCredentialId() != null) {
            User user = getUserByUsername(username);
            UserAiCredential credential = credentialRepository.findByIdAndUserId(req.getCredentialId(), user.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình lưu trữ"));
            effectiveApiKey = cryptoService.decrypt(credential.getEncryptedApiKey(), credential.getEncryptionIv());
            if (provider == null) {
                provider = credential.getProvider();
            }
            if (effectiveBaseUrl == null) {
                effectiveBaseUrl = credential.getBaseUrl();
            }
        }

        if (effectiveApiKey == null || effectiveApiKey.trim().isEmpty()) {
            return TestConnectionResponse.builder()
                    .success(false)
                    .message("Thiếu API Key để kiểm tra kết nối")
                    .build();
        }

        long start = System.currentTimeMillis();
        try {
            if (provider == null) {
                provider = AiProvider.GEMINI;
            }

            return switch (provider) {
                case GEMINI -> testGemini(effectiveApiKey.trim(), start);
                case OPENAI, DEEPSEEK, CUSTOM -> testOpenAiCompatible(effectiveApiKey.trim(), effectiveBaseUrl, provider, start);
                case ANTHROPIC -> testAnthropic(effectiveApiKey.trim(), start);
            };
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            log.error("Test connection failed for provider {}: {}", provider, e.getMessage());
            return TestConnectionResponse.builder()
                    .success(false)
                    .latencyMs(latency)
                    .message("Kết nối thất bại: " + e.getMessage())
                    .build();
        }
    }

    private TestConnectionResponse testGemini(String apiKey, long startTime) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        long latency = System.currentTimeMillis() - startTime;

        if (response.statusCode() == 200) {
            List<String> models = new ArrayList<>();
            try {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode modelsArr = root.path("models");
                if (modelsArr.isArray()) {
                    for (JsonNode m : modelsArr) {
                        String name = m.path("name").asText("").replace("models/", "");
                        if (name.contains("gemini")) {
                            models.add(name);
                        }
                    }
                }
            } catch (Exception ignored) {}

            return TestConnectionResponse.builder()
                    .success(true)
                    .latencyMs(latency)
                    .message("Kết nối Google Gemini thành công!")
                    .availableModels(models)
                    .build();
        } else {
            return TestConnectionResponse.builder()
                    .success(false)
                    .latencyMs(latency)
                    .message("Gemini phản hồi HTTP " + response.statusCode() + ": " + sanitizeErrorMessage(response.body()))
                    .build();
        }
    }

    private TestConnectionResponse testOpenAiCompatible(String apiKey, String baseUrl, AiProvider provider, long startTime) throws Exception {
        String targetUrl = (baseUrl != null && !baseUrl.trim().isEmpty())
                ? (baseUrl.endsWith("/") ? baseUrl + "models" : baseUrl + "/models")
                : (provider == AiProvider.DEEPSEEK ? "https://api.deepseek.com/v1/models" : "https://api.openai.com/v1/models");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        long latency = System.currentTimeMillis() - startTime;

        if (response.statusCode() == 200) {
            List<String> models = new ArrayList<>();
            try {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode dataArr = root.path("data");
                if (dataArr.isArray()) {
                    for (JsonNode m : dataArr) {
                        models.add(m.path("id").asText(""));
                    }
                }
            } catch (Exception ignored) {}

            return TestConnectionResponse.builder()
                    .success(true)
                    .latencyMs(latency)
                    .message("Kết nối " + provider.name() + " thành công!")
                    .availableModels(models)
                    .build();
        } else {
            return TestConnectionResponse.builder()
                    .success(false)
                    .latencyMs(latency)
                    .message(provider.name() + " phản hồi HTTP " + response.statusCode() + ": " + sanitizeErrorMessage(response.body()))
                    .build();
        }
    }

    private TestConnectionResponse testAnthropic(String apiKey, long startTime) throws Exception {
        String url = "https://api.anthropic.com/v1/models";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        long latency = System.currentTimeMillis() - startTime;

        if (response.statusCode() == 200) {
            List<String> models = new ArrayList<>();
            try {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode dataArr = root.path("data");
                if (dataArr.isArray()) {
                    for (JsonNode m : dataArr) {
                        models.add(m.path("id").asText(""));
                    }
                }
            } catch (Exception ignored) {}

            return TestConnectionResponse.builder()
                    .success(true)
                    .latencyMs(latency)
                    .message("Kết nối Anthropic Claude thành công!")
                    .availableModels(models)
                    .build();
        } else {
            return TestConnectionResponse.builder()
                    .success(false)
                    .latencyMs(latency)
                    .message("Anthropic phản hồi HTTP " + response.statusCode() + ": " + sanitizeErrorMessage(response.body()))
                    .build();
        }
    }

    private String sanitizeErrorMessage(String body) {
        if (body == null || body.trim().isEmpty()) return "Unknown provider error";
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.has("error")) {
                JsonNode err = root.path("error");
                if (err.has("message")) return err.path("message").asText();
            }
        } catch (Exception ignored) {}
        return body.length() > 200 ? body.substring(0, 200) + "..." : body;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại: " + username));
    }

    public UserAiCredentialDto toDto(UserAiCredential c) {
        String decrypted = null;
        try {
            decrypted = cryptoService.decrypt(c.getEncryptedApiKey(), c.getEncryptionIv());
        } catch (Exception ignored) {}

        return UserAiCredentialDto.builder()
                .id(c.getId())
                .provider(c.getProvider())
                .keyAlias(c.getKeyAlias())
                .maskedKey(decrypted != null ? CryptoService.maskApiKey(decrypted) : "********")
                .baseUrl(c.getBaseUrl())
                .active(c.isActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
