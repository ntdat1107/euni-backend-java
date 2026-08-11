package com.euni.backend.controller;

import com.euni.backend.dto.request.AiGenerateRequest;
import org.springframework.http.MediaType;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private static final String[] MOCK_HTML_PARAGRAPHS = new String[]{
            "<p><strong>Chuẩn đầu ra (CLO) & Nội dung học phần:</strong> Môn học này trang bị cho người học kiến thức chuyên sâu về phân tích, thiết kế và phát triển phần mềm theo chuẩn chất lượng cao, tuân thủ theo các khung năng lực quốc tế và quy định kiểm định giáo dục hiện hành.</p>",
            "<p>Sau khi hoàn thành môn học, người học có khả năng:</p>",
            "<ul>\n  <li><strong>CLO 1:</strong> Phân tích nghiệp vụ phức tạp và chuyển đổi thành mô hình kiến trúc phần mềm (Architecture Patterns & DDD).</li>\n  <li><strong>CLO 2:</strong> Làm chủ các công cụ lập trình hiện đại, quy trình kiểm thử tự động (Automated Testing) và triển khai liên tục (CI/CD).</li>\n  <li><strong>CLO 3:</strong> Đánh giá, tối ưu hóa hiệu năng hệ thống và đảm bảo an toàn bảo mật phần mềm.</li>\n</ul>",
            "<p><em>Ghi chú: Dữ liệu được tổng hợp & đề xuất tự động bởi Trợ lý AI Assistant (Hệ thống eUni).</em></p>"
    };

    @PostMapping(value = "/generate-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateStream(@RequestBody AiGenerateRequest request) {
        SseEmitter emitter = new SseEmitter(180000L);

        SecurityContext context = SecurityContextHolder.getContext();
        executor.execute(DelegatingSecurityContextRunnable.create(() -> {
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
        }, context));

        return emitter;
    }
}
