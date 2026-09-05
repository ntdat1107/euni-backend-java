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

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final List<String> FALLBACK_MODELS = List.of(
            "gemini-2.0-flash",
            "gemini-1.5-flash-latest",
            "gemini-2.5-flash",
            "gemini-1.5-flash-002"
    );

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

            String selectedModel = sanitizeModelName(model);
            HttpResponse<InputStream> response = executeGeminiRequest(selectedModel, jsonPayload);

            // If 404, attempt fallback models automatically
            if (response != null && response.statusCode() == 404) {
                log.warn("Model '{}' trả về 404 Not Found. Đang thử tự động chuyển đổi mô hình fallback...", selectedModel);
                for (String fallback : FALLBACK_MODELS) {
                    if (fallback.equalsIgnoreCase(selectedModel)) continue;
                    log.info("Thử kết nối Gemini với model fallback: {}", fallback);
                    response = executeGeminiRequest(fallback, jsonPayload);
                    if (response != null && response.statusCode() == 200) {
                        log.info("Kết nối thành công với Gemini model: {}", fallback);
                        break;
                    }
                }
            }

            if (response == null || response.statusCode() != 200) {
                int code = response != null ? response.statusCode() : 500;
                String errorBody = response != null ? new String(response.body().readAllBytes(), StandardCharsets.UTF_8) : "Unknown Error";
                log.error("Gemini API Error (HTTP {}): {}", code, errorBody);
                emitter.send(SseEmitter.event().data("<p><strong>Lỗi Gemini API (HTTP " + code + "):</strong> Không tìm thấy mô hình tương thích hoặc API key chưa đúng. Chi tiết: " + errorBody + "</p>"));
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

    private HttpResponse<InputStream> executeGeminiRequest(String targetModel, String jsonPayload) throws Exception {
        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:streamGenerateContent?alt=sse&key=%s",
                targetModel, apiKey.trim());

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(60))
                .build();

        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
    }

    private String sanitizeModelName(String rawModel) {
        if (rawModel == null || rawModel.trim().isEmpty()) {
            return "gemini-2.0-flash";
        }
        String trimmed = rawModel.trim();
        if (trimmed.startsWith("models/")) {
            return trimmed.substring(7);
        }
        return trimmed;
    }

    private String buildFullPrompt(AiGenerateRequest request) {
        String userPrompt = request.getPrompt() != null ? request.getPrompt() : "";
        String action = request.getAction() != null ? request.getAction() : "";
        String contentType = request.getContentType() != null ? request.getContentType().toUpperCase() : "GENERAL";

        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là Trợ lý AI Chuyên gia Giáo dục & Thiết kế Chương trình Đào tạo Đại học cho Hệ thống Quản lý eUni.\n");
        sb.append("Nhiệm vụ của bạn là tạo nội dung văn bản HTML phù hợp để chèn trực tiếp vào trình biên soạn văn bản (Rich Text Editor).\n");
        sb.append("QUY TẮC ĐỊNH DẠNG BẮT BUỘC:\n");
        sb.append("- CHỈ trả về đoạn mã HTML hợp lệ (sử dụng các thẻ <p>, <ul>, <li>, <strong>, <em>, <h4>, <table>, <thead>, <tbody>, <tr>, <th>, <td>).\n");
        sb.append("- KHÔNG bao bọc trong khối markdown ```html hoặc ```.\n");
        sb.append("- Ngôn từ chuẩn mực học thuật, rõ ràng, dễ đo lường và theo đúng chuẩn kiểm định giáo dục (AUN-QA / MOET / Outcome-Based Education).\n\n");

        switch (contentType) {
            case "CLO":
            case "OUTCOME":
                sb.append("--- HƯỚNG DẪN CHUYÊN MÔN: VIẾT CHUẨN ĐẦU RA HỌC PHẦN (CLO / Course Learning Outcomes) ---\n");
                sb.append("- Sử dụng thang đo tư duy Bloom (Bloom's Taxonomy) với các động từ hành động đo lường được (Ví dụ: Phân tích, Thiết kế, Đánh giá, Vận dụng, Giải quyết, Xây dựng).\n");
                sb.append("- Cấu trúc từng CLO phải gồm: [Động từ Bloom] + [Đối tượng kiến thức/kỹ năng] + [Bối cảnh/Điều kiện áp dụng].\n");
                sb.append("- Định dạng danh sách <ul><li><strong>CLO 1:</strong> ...</li><li><strong>CLO 2:</strong> ...</li></ul>.\n\n");
                break;
            case "PLO":
                sb.append("--- HƯỚNG DẪN CHUYÊN MÔN: CHUẨN ĐẦU RA CHƯƠNG TRÌNH ĐÀO TẠO (PLO) & CHỈ SỐ PI ---\n");
                sb.append("- Xây dựng chuẩn đầu ra tổng thể cấp chương trình đào tạo (Program Learning Outcomes - PLO) và chỉ số hiệu quả (Performance Indicators - PI).\n");
                sb.append("- Tập trung vào các mảng kiến thức chuyên sâu, kỹ năng nghề nghiệp, khả năng giải quyết vấn đề phức tạp, đạo đức nghề nghiệp và khả năng học tập suốt đời.\n");
                sb.append("- Định dạng cấu trúc: <ul><li><strong>PLO X:</strong> [Nội dung PLO]<ul><li><strong>PI X.1:</strong> [Chỉ số thành phần]</li></ul></li></ul>.\n\n");
                break;
            case "SYLLABUS":
            case "OUTLINE":
                sb.append("--- HƯỚNG DẪN CHUYÊN MÔN: ĐỀ CƯƠNG CHI TIẾT HỌC PHẦN (Syllabus) ---\n");
                sb.append("- Cấu trúc chương trình học hợp lý, chia theo các chương/tuần học logic từ cơ bản đến nâng cao.\n");
                sb.append("- Mối chương cần nêu rõ: Tên chương, Chủ đề lý thuyết, Bài tập/Thực hành và Yêu cầu sinh viên tự học.\n");
                sb.append("- Định dạng: Sử dụng <h4>Chương X: [Tên chương]</h4> kèm danh sách <ul><li>...</li></ul>.\n\n");
                break;
            case "COURSE_DESCRIPTION":
            case "DESCRIPTION":
                sb.append("--- HƯỚNG DẪN CHUYÊN MÔN: MÔ TẢ TỔNG QUAN HỌC PHẦN ---\n");
                sb.append("- Nêu vị trí môn học trong chương trình đào tạo, mục tiêu tổng quát, phương pháp tiếp cận và những giá trị cốt lõi sinh viên sẽ nhận được.\n");
                sb.append("- Văn phong trang trọng, súc tích, truyền cảm hứng và chuẩn hóa học thuật.\n\n");
                break;
            case "PREREQUISITES":
                sb.append("--- HƯỚNG DẪN CHUYÊN MÔN: YÊU CẦU MÔN HỌC TIÊN QUYẾT & KIẾN THỨC NỀN TẢNG ---\n");
                sb.append("- Liệt kê các môn học tiên quyết (Prerequisite), môn học song hành (Corequisite) và khung kiến thức bắt buộc sinh viên cần trang bị trước khi bắt đầu môn học.\n\n");
                break;
            case "ASSESSMENT":
                sb.append("--- HƯỚNG DẪN CHUYÊN MÔN: PHƯƠNG PHÁP & MA TRẬN ĐÁNH GIÁ HỌC PHẦN ---\n");
                sb.append("- Thiết kế phương pháp đánh giá đa dạng: Chuyên cần, Đánh giá quá trình (Bài tập/Lab/Đồ án), Thi kết thúc học phần.\n");
                sb.append("- Nếu được yêu cầu dạng bảng, hãy tạo <table> với các cột: Thành phần đánh giá, Hình thức đánh giá, Trọng số (%), Chuẩn đầu ra (CLO) tương ứng.\n\n");
                break;
            default:
                sb.append("--- HƯỚNG DẪN CHUYÊN MÔN: NỘI DUNG HỌC THUẬT GIÁO DỤC ---\n");
                sb.append("- Đảm bảo nội dung chuẩn hóa theo mô hình giáo dục đại học.\n\n");
                break;
        }

        if (!action.isEmpty()) {
            sb.append("Tác vụ cụ thể: ").append(action).append("\n");
        }
        sb.append("Yêu cầu của người dùng: ").append(userPrompt);

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
