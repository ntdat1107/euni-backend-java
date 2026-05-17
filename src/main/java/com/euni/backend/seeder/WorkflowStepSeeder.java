package com.euni.backend.seeder;

import com.euni.backend.entity.WorkflowStepDefinition;
import com.euni.backend.repository.WorkflowStepDefinitionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(3)
@ConditionalOnProperty(name = "app.seeder.workflow-step.enabled", havingValue = "true")
public class WorkflowStepSeeder implements CommandLineRunner {

    private final WorkflowStepDefinitionRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting Workflow Step Definition seeding...");

        String workflowType = "SURVEY_CREATE";

        // Step 1: Khảo sát nhu cầu (From Image 1) - User specified Step 1 is Upload
        seedStep(workflowType, "S1_KHAO_SAT_NHU_CAU", "Khảo sát nhu cầu", "UPLOAD", 
                List.of("Khảo sát", "Tổng hợp kết quả khảo sát"));
        
        // Step 2-9: Combining data from Image 1 (Documents) and Image 2 (Codes and Types)
        seedStep(workflowType, "S2_PLO", "Xây dựng mục tiêu & chuẩn đầu ra (PLO)", "FORM", 
                List.of("Chuẩn đầu ra", "Tờ trình"));
        
        seedStep(workflowType, "S3_XacDinhCauTruc_KhoiLuongKienThuc", "Xác định cấu trúc, khối lượng kiến thức", "FORM", 
                List.of("Qđ tổ soạn thảo", "Xác định cấu trúc, KL kiến thức"));
        
        seedStep(workflowType, "S4_DoiChieuSoSanh", "Đối chiếu, so sánh chương trình đào tạo trong và ngoài nước", "UPLOAD", 
                List.of("So sánh đối chiếu CTĐT"));
        
        seedStep(workflowType, "S5_CLO", "Xây dựng đề cương học phần (CLO)", "FORM", 
                List.of("Đề cương chi tiết", "Biên bản HT - BM", "Biên bản HT - KH"));
        
        seedStep(workflowType, "S6_MATRIX", "Xây dựng ma trận phát triển kiến thức, kỹ năng", "FORM", 
                List.of("Ma trận chuẩn đầu ra, phát triển kiến thức, kỹ năng"));
        
        seedStep(workflowType, "S7_REVIEW_PHASE_1", "Tổ chức hội thảo lấy ý kiến", "UPLOAD", 
                List.of("Phiếu TĐG điều kiện mở ngành", "Phiếu nhận xét đk mở ngành"));
        
        seedStep(workflowType, "S8_REVIEW_PHASE_2", "Hội đồng Khoa chuyên môn thẩm định", "UPLOAD", 
                List.of("BB hội đồng thẩm định", "Phiếu thẩm định", "QĐ thành lập HĐ thẩm định", "Biên bản kiểm tra điều kiện thực tế"));
        
        seedStep(workflowType, "S9_SUBMIT", "Nộp chương trình đào tạo hoàn chỉnh trình HĐKH ĐT Trường", "UPLOAD", 
                List.of("Chương trình đào tạo", "Chuẩn đầu ra", "Đề cương chi tiết"));

        log.info("Workflow Step Definition seeding completed.");
    }

    private void seedStep(String workflowType, String stepCode, String stepName, String type, List<String> docs) {
        repository.findByStepCodeAndDeletedFalse(stepCode).orElseGet(() -> {
            try {
                WorkflowStepDefinition step = WorkflowStepDefinition.builder()
                        .workflowType(workflowType)
                        .stepCode(stepCode)
                        .stepName(stepName)
                        .type(type)
                        .requiredDocuments(objectMapper.writeValueAsString(docs))
                        .build();
                return repository.save(step);
            } catch (JsonProcessingException e) {
                log.error("Error seeding step: {}", stepCode, e);
                return null;
            }
        });
    }
}
