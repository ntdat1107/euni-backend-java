package com.euni.backend.service;

import com.euni.backend.dto.request.AiGenerateRequest;
import com.euni.backend.repository.AiPromptTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiPromptResolver {

    private final AiPromptTemplateRepository templateRepository;

    private static final Map<String, String> DEFAULT_PROMPTS = Map.of(
            "BASE_SYSTEM", """
                Bạn là Trợ lý AI Chuyên gia Giáo dục & Thiết kế Chương trình Đào tạo Đại học cho Hệ thống Quản lý eUni.
                Nhiệm vụ của bạn là tạo nội dung văn bản HTML phù hợp để chèn trực tiếp vào trình biên soạn văn bản (Rich Text Editor).
                QUY TẮC ĐỊNH DẠNG BẮT BUỘC:
                - CHỈ trả về đoạn mã HTML hợp lệ (sử dụng các thẻ <p>, <ul>, <li>, <strong>, <em>, <h4>, <table>, <thead>, <tbody>, <tr>, <th>, <td>).
                - KHÔNG bao bọc trong khối markdown ```html hoặc ```.
                - Ngôn từ chuẩn mực học thuật, rõ ràng, dễ đo lường và theo đúng chuẩn kiểm định giáo dục (AUN-QA / MOET / Outcome-Based Education).
                """,
            "PEO", """
                --- HƯỚNG DẪN CHUYÊN MÔN: MỤC TIÊU ĐÀO TẠO CHƯƠNG TRÌNH (PEO / Program Educational Objectives) ---
                - Cấu trúc đầu ra BẮT BUỘC bao gồm 2 phần rõ ràng theo đúng định dạng thẻ HTML sau:
                  1. Đoạn văn MỤC TIÊU CHUNG của chương trình đào tạo nằm trong thẻ <p>:
                     <p><strong>Mục tiêu chung:</strong> Chương trình đào tạo ngành [Tên ngành] nhằm đào tạo cử nhân/kỹ sư có kiến thức chuyên sâu...</p>
                  2. Thẻ <ul> chứa danh sách CÁC MỤC TIÊU CỤ THỂ (PEO 1, PEO 2, PEO 3,...):
                     <ul>
                       <li><strong>PEO 1:</strong> [Nội dung mục tiêu cụ thể 1]</li>
                       <li><strong>PEO 2:</strong> [Nội dung mục tiêu cụ thể 2]</li>
                       <li><strong>PEO 3:</strong> [Nội dung mục tiêu cụ thể 3]</li>
                     </ul>
                - Đảm bảo nội dung các PEO tập trung vào năng lực phát triển sự nghiệp dài hạn (3-5 năm sau tốt nghiệp), trình độ chuyên môn, kỹ năng quản lý và đạo đức nghề nghiệp.
                """,
            "CLO", """
                --- HƯỚNG DẪN CHUYÊN MÔN: VIẾT CHUẨN ĐẦU RA HỌC PHẦN (CLO / Course Learning Outcomes) ---
                - Sử dụng thang đo tư duy Bloom (Bloom's Taxonomy) với các động từ hành động đo lường được (Ví dụ: Phân tích, Thiết kế, Đánh giá, Vận dụng, Giải quyết, Xây dựng).
                - Cấu trúc từng CLO phải gồm: [Động từ Bloom] + [Đối tượng kiến thức/kỹ năng] + [Bối cảnh/Điều kiện áp dụng].
                - Định dạng danh sách <ul><li><strong>CLO 1:</strong> ...</li><li><strong>CLO 2:</strong> ...</li></ul>.
                """,
            "PLO", """
                --- HƯỚNG DẪN CHUYÊN MÔN: CHUẨN ĐẦU RA CHƯƠNG TRÌNH ĐÀO TẠO (PLO) & CHỈ SỐ PI ---
                - Xây dựng chuẩn đầu ra tổng thể cấp chương trình đào tạo (Program Learning Outcomes - PLO) và chỉ số hiệu quả (Performance Indicators - PI).
                - Tập trung vào các mảng kiến thức chuyên sâu, kỹ năng nghề nghiệp, khả năng giải quyết vấn đề phức tạp, đạo đức nghề nghiệp và khả năng học tập suốt đời.
                - Định dạng cấu trúc: <ul><li><strong>PLO X:</strong> [Nội dung PLO]<ul><li><strong>PI X.1:</strong> [Chỉ số thành phần]</li></ul></li></ul>.
                """,
            "SYLLABUS", """
                --- HƯỚNG DẪN CHUYÊN MÔN: ĐỀ CƯƠNG CHI TIẾT HỌC PHẦN (Syllabus) ---
                - Cấu trúc chương trình học hợp lý, chia theo các chương/tuần học logic từ cơ bản đến nâng cao.
                - Mối chương cần nêu rõ: Tên chương, Chủ đề lý thuyết, Bài tập/Thực hành và Yêu cầu sinh viên tự học.
                - Định dạng: Sử dụng <h4>Chương X: [Tên chương]</h4> kèm danh sách <ul><li>...</li></ul>.
                """,
            "COURSE_DESCRIPTION", """
                --- HƯỚNG DẪN CHUYÊN MÔN: MÔ TẢ TỔNG QUAN HỌC PHẦN ---
                - Nêu vị trí môn học trong chương trình đào tạo, mục tiêu tổng quát, phương pháp tiếp cận và những giá trị cốt lõi sinh viên sẽ nhận được.
                - Văn phong trang trọng, súc tích, truyền cảm hứng và chuẩn hóa học thuật.
                """,
            "PREREQUISITES", """
                --- HƯỚNG DẪN CHUYÊN MÔN: YÊU CẦU MÔN HỌC TIÊN QUYẾT & KIẾN THỨC NỀN TẢNG ---
                - Liệt kê các môn học tiên quyết (Prerequisite), môn học song hành (Corequisite) và khung kiến thức bắt buộc sinh viên cần trang bị trước khi bắt đầu môn học.
                """,
            "ASSESSMENT", """
                --- HƯỚNG DẪN CHUYÊN MÔN: PHƯƠNG PHÁP & MA TRẬN ĐÁNH GIÁ HỌC PHẦN ---
                - Thiết kế phương pháp đánh giá đa dạng: Chuyên cần, Đánh giá quá trình (Bài tập/Lab/Đồ án), Thi kết thúc học phần.
                - Nếu được yêu cầu dạng bảng, hãy tạo <table> với các cột: Thành phần đánh giá, Hình thức đánh giá, Trọng số (%), Chuẩn đầu ra (CLO) tương ứng.
                """,
            "DEFAULT", """
                --- HƯỚNG DẪN CHUYÊN MÔN: NỘI DUNG HỌC THUẬT GIÁO DỤC ---
                - Đảm bảo nội dung chuẩn hóa theo mô hình giáo dục đại học.
                """
    );

    public String buildFullPrompt(AiGenerateRequest request) {
        String userPrompt = request.getPrompt() != null ? request.getPrompt() : "";
        String action = request.getAction() != null ? request.getAction() : "";
        String promptCode = resolvePromptCode(request.getContentType());

        StringBuilder sb = new StringBuilder();
        sb.append(getPromptTemplateContent("BASE_SYSTEM")).append("\n\n");
        sb.append(getPromptTemplateContent(promptCode)).append("\n\n");

        if (request.getMetadata() != null && !request.getMetadata().isEmpty()) {
            sb.append("--- THÔNG TIN CHƯƠNG TRÌNH ĐÀO TẠO & NGỮ CẢNH HỆ THỐNG ---\n");
            request.getMetadata().forEach((k, v) -> {
                if (v != null && !v.toString().trim().isEmpty()) {
                    sb.append("- ").append(k).append(": ").append(v).append("\n");
                }
            });
            sb.append("\n");
        }

        if (!action.isEmpty()) {
            sb.append("Tác vụ cụ thể: ").append(action).append("\n");
        }
        sb.append("Yêu cầu của người dùng: ").append(userPrompt);
        return sb.toString();
    }

    private String resolvePromptCode(String contentType) {
        if (contentType == null) return "DEFAULT";
        return switch (contentType.toUpperCase()) {
            case "PEO", "PEOS", "MUC_TIEU" -> "PEO";
            case "CLO", "OUTCOME" -> "CLO";
            case "PLO" -> "PLO";
            case "SYLLABUS", "OUTLINE" -> "SYLLABUS";
            case "COURSE_DESCRIPTION", "DESCRIPTION" -> "COURSE_DESCRIPTION";
            case "PREREQUISITES" -> "PREREQUISITES";
            case "ASSESSMENT" -> "ASSESSMENT";
            default -> "DEFAULT";
        };
    }

    private String getPromptTemplateContent(String code) {
        String defaultFallback = DEFAULT_PROMPTS.getOrDefault(code, DEFAULT_PROMPTS.get("DEFAULT"));
        if (templateRepository != null) {
            try {
                var opt = templateRepository.findByCodeAndActiveTrueAndDeletedFalse(code.toUpperCase());
                if (opt.isPresent() && opt.get().getTemplateContent() != null && !opt.get().getTemplateContent().trim().isEmpty()) {
                    return opt.get().getTemplateContent().trim();
                }
            } catch (Exception e) {
                log.warn("[AI-PROMPT] Lỗi lấy prompt từ DB cho code='{}': {}", code, e.getMessage());
            }
        }
        return defaultFallback;
    }
}
