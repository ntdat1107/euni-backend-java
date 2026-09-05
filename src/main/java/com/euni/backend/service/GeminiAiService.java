package com.euni.backend.service;

import com.euni.backend.dto.request.AiGenerateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Slf4j
@Service
public class GeminiAiService {

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String[] MOCK_HTML_PARAGRAPHS = new String[]{
            "<p><strong>Chuẩn đầu ra (CLO) & Nội dung học phần:</strong> Môn học này trang bị cho người học kiến thức chuyên sâu về phân tích, thiết kế và phát triển phần mềm theo chuẩn chất lượng cao, tuân thủ theo các khung năng lực quốc tế và quy định kiểm định giáo dục hiện hành.</p>",
            "<p>Sau khi hoàn thành môn học, người học có khả năng:</p>",
            "<ul>\n  <li><strong>CLO 1:</strong> Phân tích nghiệp vụ phức tạp và chuyển đổi thành mô hình kiến trúc phần mềm (Architecture Patterns & DDD).</li>\n  <li><strong>CLO 2:</strong> Làm chủ các công cụ lập trình hiện đại, quy trình kiểm thử tự động (Automated Testing) và triển khai liên tục (CI/CD).</li>\n  <li><strong>CLO 3:</strong> Đánh giá, tối ưu hóa hiệu năng hệ thống và đảm bảo an toàn bảo mật phần mềm.</li>\n</ul>",
            "<p><em>Ghi chú: Dữ liệu được tổng hợp & đề xuất tự động bởi Trợ lý AI Assistant (Hệ thống eUni).</em></p>"
    };

    public void streamGenerate(AiGenerateRequest request, SseEmitter emitter) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("GEMINI_API_KEY chưa được cấu hình. Sử dụng dữ liệu giả lập (Mock fallback).");
            streamMockResponse(emitter);
            return;
        }

        try {
            String promptText = buildFullPrompt(request);

            Map<String, Object> textPart = Map.of("text", promptText);
            Map<String, Object> contentMap = Map.of("role", "user", "parts", List.of(textPart));
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(contentMap),
                    "generationConfig", Map.of(
                            "temperature", 0.7,
                            "maxOutputTokens", 2048
                    )
            );

            String jsonPayload = objectMapper.writeValueAsString(requestBody);
            String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:streamGenerateContent?alt=sse&key=%s",
                    model, apiKey.trim());

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<InputStream> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                log.error("Gemini API Error (HTTP {}): {}", response.statusCode(), errorBody);
                emitter.send(SseEmitter.event().data("<p><strong>Lỗi Gemini API (HTTP " + response.statusCode() + "):</strong> Vui lòng kiểm tra lại GEMINI_API_KEY hoặc thử lại sau.</p>"));
                emitter.complete();
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String dataJson = line.substring(6).trim();
                        if ("[DONE]".equalsIgnoreCase(dataJson)) {
                            break;
                        }

                        try {
                            JsonNode root = objectMapper.readTree(dataJson);
                            JsonNode candidates = root.path("candidates");
                            if (candidates.isArray() && !candidates.isEmpty()) {
                                JsonNode parts = candidates.get(0).path("content").path("parts");
                                if (parts.isArray() && !parts.isEmpty()) {
                                    String textChunk = parts.get(0).path("text").asText("");
                                    if (!textChunk.isEmpty()) {
                                        emitter.send(textChunk);
                                    }
                                }
                            }
                        } catch (Exception parseException) {
                            log.debug("Skip non-JSON or chunk parse error: {}", parseException.getMessage());
                        }
                    }
                }
            }

            emitter.complete();

        } catch (Exception e) {
            log.error("Lỗi khi gọi Gemini API stream", e);
            try {
                emitter.completeWithError(e);
            } catch (Exception ignored) {
            }
        }
    }

    private String buildFullPrompt(AiGenerateRequest request) {
        String userPrompt = request.getPrompt() != null ? request.getPrompt() : "";
        String action = request.getAction() != null ? request.getAction() : "";

        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là Trợ lý AI Chuyên gia Giáo dục & Thiết kế Chương trình Đào tạo Đại học cho Hệ thống Quản lý eUni.\n");
        sb.append("Nhiệm vụ của bạn là tạo nội dung văn bản HTML phù hợp để chèn vào trình biên soạn văn bản (Rich Text Editor).\n");
        sb.append("Định dạng đầu ra: CHỈ trả về đoạn mã HTML hợp lệ (dùng <p>, <ul>, <li>, <strong>, <em>), KHÔNG bọc trong markdown ```html.\n\n");

        if (!action.isEmpty()) {
            sb.append("Tác vụ cần thực hiện: ").append(action).append("\n");
        }
        sb.append("Yêu cầu nội dung: ").append(userPrompt);

        return sb.toString();
    }

    private void streamMockResponse(SseEmitter emitter) {
        try {
            String fullContent = String.join("\n", MOCK_HTML_PARAGRAPHS);
            int chunkSize = 15;

            for (int i = 0; i < fullContent.length(); i += chunkSize) {
                int end = Math.min(i + chunkSize, fullContent.length());
                String chunk = fullContent.substring(i, end);

                emitter.send(chunk);
                Thread.sleep(40);
            }

            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
}
