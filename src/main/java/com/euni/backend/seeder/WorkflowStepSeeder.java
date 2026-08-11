package com.euni.backend.seeder;

import com.euni.backend.entity.WorkflowStepDefinition;
import com.euni.backend.entity.WorkflowTemplate;
import com.euni.backend.entity.enums.WorkflowStatus;
import com.euni.backend.repository.WorkflowStepDefinitionRepository;
import com.euni.backend.repository.WorkflowTemplateRepository;
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
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(3)
@ConditionalOnProperty(name = "app.seeder.workflow-step.enabled", havingValue = "true", matchIfMissing = true)
public class WorkflowStepSeeder implements CommandLineRunner {

    private final WorkflowStepDefinitionRepository repository;
    private final WorkflowTemplateRepository workflowTemplateRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting safe Workflow Step Definition & Template seeding (natural key checks)...");

        String workflowType = "SURVEY_CREATE";

        // 1. Seed Master Step Definitions S1 -> S9
        seedStep(workflowType, "S1_KHAO_SAT_NHU_CAU", "Khảo sát nhu cầu", "UPLOAD", 
                List.of("Khảo sát", "Tổng hợp kết quả khảo sát"));
        
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

        // 2. Seed Sample Workflow Templates
        seedWorkflowTemplate(
                "WF_SURVEY_STANDARD_6STEP",
                "Quy trình Khảo sát & Đánh giá CTĐT Chuẩn (6 Bước)",
                "Quy trình chuẩn áp dụng cho đánh giá định kỳ CTĐT gồm 6 bước cốt lõi từ Khảo sát nhu cầu tới Ma trận kiến thức kỹ năng.",
                createStandard6StepJson()
        );

        seedWorkflowTemplate(
                "WF_SURVEY_FASTTRACK_3STEP",
                "Quy trình Cập nhật CTĐT Rút gọn (3 Bước)",
                "Quy trình rút gọn dành cho cập nhật bổ sung học phần hàng năm gồm PLO, Đề cương CLO và Ma trận tích hợp.",
                createFastTrack3StepJson()
        );

        log.info("Workflow Step & Template seeding completed successfully.");
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

    private void seedWorkflowTemplate(String code, String name, String description, String jsonContent) {
        workflowTemplateRepository.findTopByCodeAndDeletedFalseOrderByVersionDesc(code).orElseGet(() -> {
            WorkflowTemplate template = WorkflowTemplate.builder()
                    .code(code)
                    .name(name)
                    .description(description)
                    .jsonContent(jsonContent)
                    .version(1)
                    .isActive(true)
                    .status(WorkflowStatus.ACTIVE)
                    .build();
            return workflowTemplateRepository.save(template);
        });
    }

    private String createStandard6StepJson() {
        return """
                {
                  "type": "linear",
                  "steps": [
                    { "tempId": "step_1", "masterStepId": "1", "name": "Khảo sát nhu cầu", "code": "S1_KHAO_SAT_NHU_CAU", "screenCode": "S1_KHAO_SAT_NHU_CAU", "executorRole": "LECTURER", "approverRole": "MANAGER", "isRequired": true, "orderNo": 1, "prerequisiteStepCodes": [] },
                    { "tempId": "step_2", "masterStepId": "2", "name": "Xây dựng mục tiêu & chuẩn đầu ra (PLO)", "code": "S2_PLO", "screenCode": "S2_PLO", "executorRole": "LECTURER", "approverRole": "MANAGER", "isRequired": true, "orderNo": 2, "prerequisiteStepCodes": [] },
                    { "tempId": "step_3", "masterStepId": "3", "name": "Xác định cấu trúc, khối lượng kiến thức", "code": "S3_XacDinhCauTruc_KhoiLuongKienThuc", "screenCode": "S3_XacDinhCauTruc_KhoiLuongKienThuc", "executorRole": "LECTURER", "approverRole": "MANAGER", "isRequired": true, "orderNo": 3, "prerequisiteStepCodes": ["S2_PLO"] },
                    { "tempId": "step_4", "masterStepId": "4", "name": "Đối chiếu, so sánh chương trình đào tạo", "code": "S4_DoiChieuSoSanh", "screenCode": "S4_DoiChieuSoSanh", "executorRole": "LECTURER", "approverRole": "MANAGER", "isRequired": true, "orderNo": 4, "prerequisiteStepCodes": ["S2_PLO"] },
                    { "tempId": "step_5", "masterStepId": "5", "name": "Xây dựng đề cương học phần (CLO)", "code": "S5_CLO", "screenCode": "S5_CLO", "executorRole": "LECTURER", "approverRole": "MANAGER", "isRequired": true, "orderNo": 5, "prerequisiteStepCodes": ["S2_PLO"] },
                    { "tempId": "step_6", "masterStepId": "6", "name": "Xây dựng ma trận phát triển kiến thức, kỹ năng", "code": "S6_MATRIX", "screenCode": "S6_MATRIX", "executorRole": "LECTURER", "approverRole": "REVIEWER", "isRequired": true, "orderNo": 6, "prerequisiteStepCodes": ["S5_CLO"] }
                  ],
                  "nodes": [
                    { "id": "node-start", "type": "start", "position": { "x": 50, "y": 150 }, "data": { "label": "Bắt đầu" } },
                    { "id": "step_1", "type": "state", "position": { "x": 300, "y": 150 }, "data": { "label": "Khảo sát nhu cầu", "orderNo": 1, "screenCode": "S1_KHAO_SAT_NHU_CAU", "performerRole": "LECTURER", "approverRole": "MANAGER", "prerequisiteStepCodes": [] } },
                    { "id": "step_2", "type": "state", "position": { "x": 700, "y": 150 }, "data": { "label": "Xây dựng mục tiêu & chuẩn đầu ra (PLO)", "orderNo": 2, "screenCode": "S2_PLO", "performerRole": "LECTURER", "approverRole": "MANAGER", "prerequisiteStepCodes": [] } },
                    { "id": "step_3", "type": "state", "position": { "x": 1100, "y": 150 }, "data": { "label": "Xác định cấu trúc, khối lượng kiến thức", "orderNo": 3, "screenCode": "S3_XacDinhCauTruc_KhoiLuongKienThuc", "performerRole": "LECTURER", "approverRole": "MANAGER", "prerequisiteStepCodes": ["S2_PLO"] } },
                    { "id": "step_4", "type": "state", "position": { "x": 1500, "y": 150 }, "data": { "label": "Đối chiếu, so sánh chương trình đào tạo", "orderNo": 4, "screenCode": "S4_DoiChieuSoSanh", "performerRole": "LECTURER", "approverRole": "MANAGER", "prerequisiteStepCodes": ["S2_PLO"] } },
                    { "id": "step_5", "type": "state", "position": { "x": 1900, "y": 150 }, "data": { "label": "Xây dựng đề cương học phần (CLO)", "orderNo": 5, "screenCode": "S5_CLO", "performerRole": "LECTURER", "approverRole": "MANAGER", "prerequisiteStepCodes": ["S2_PLO"] } },
                    { "id": "step_6", "type": "state", "position": { "x": 2300, "y": 150 }, "data": { "label": "Xây dựng ma trận phát triển kiến thức, kỹ năng", "orderNo": 6, "screenCode": "S6_MATRIX", "performerRole": "LECTURER", "approverRole": "REVIEWER", "prerequisiteStepCodes": ["S5_CLO"] } },
                    { "id": "node-end", "type": "end", "position": { "x": 2700, "y": 150 }, "data": { "label": "Kết thúc" } }
                  ],
                  "edges": [
                    { "id": "edge-start", "source": "node-start", "target": "step_1", "label": "Bắt đầu" },
                    { "id": "e-1-2", "source": "step_1", "target": "step_2", "label": "Tiếp tục" },
                    { "id": "e-2-3", "source": "step_2", "target": "step_3", "label": "Tiếp tục" },
                    { "id": "e-3-4", "source": "step_3", "target": "step_4", "label": "Tiếp tục" },
                    { "id": "e-4-5", "source": "step_4", "target": "step_5", "label": "Tiếp tục" },
                    { "id": "e-5-6", "source": "step_5", "target": "step_6", "label": "Tiếp tục" },
                    { "id": "edge-end", "source": "step_6", "target": "node-end", "label": "Hoàn thành" }
                  ]
                }""";
    }

    private String createFastTrack3StepJson() {
        return """
                {
                  "type": "linear",
                  "steps": [
                    { "tempId": "fstep_1", "masterStepId": "2", "name": "Rà soát chuẩn đầu ra (PLO)", "code": "S2_PLO", "screenCode": "S2_PLO", "executorRole": "LECTURER", "approverRole": "MANAGER", "isRequired": true, "orderNo": 1, "prerequisiteStepCodes": [] },
                    { "tempId": "fstep_2", "masterStepId": "5", "name": "Cập nhật Đề cương học phần (CLO)", "code": "S5_CLO", "screenCode": "S5_CLO", "executorRole": "LECTURER", "approverRole": "MANAGER", "isRequired": true, "orderNo": 2, "prerequisiteStepCodes": ["S2_PLO"] },
                    { "tempId": "fstep_3", "masterStepId": "6", "name": "Cập nhật Ma trận tích hợp", "code": "S6_MATRIX", "screenCode": "S6_MATRIX", "executorRole": "LECTURER", "approverRole": "REVIEWER", "isRequired": true, "orderNo": 3, "prerequisiteStepCodes": ["S5_CLO"] }
                  ],
                  "nodes": [
                    { "id": "node-start", "type": "start", "position": { "x": 50, "y": 150 }, "data": { "label": "Bắt đầu" } },
                    { "id": "fstep_1", "type": "state", "position": { "x": 300, "y": 150 }, "data": { "label": "Rà soát chuẩn đầu ra (PLO)", "orderNo": 1, "screenCode": "S2_PLO", "performerRole": "LECTURER", "approverRole": "MANAGER", "prerequisiteStepCodes": [] } },
                    { "id": "fstep_2", "type": "state", "position": { "x": 700, "y": 150 }, "data": { "label": "Cập nhật Đề cương học phần (CLO)", "orderNo": 2, "screenCode": "S5_CLO", "performerRole": "LECTURER", "approverRole": "MANAGER", "prerequisiteStepCodes": ["S2_PLO"] } },
                    { "id": "fstep_3", "type": "state", "position": { "x": 1100, "y": 150 }, "data": { "label": "Cập nhật Ma trận tích hợp", "orderNo": 3, "screenCode": "S6_MATRIX", "performerRole": "LECTURER", "approverRole": "REVIEWER", "prerequisiteStepCodes": ["S5_CLO"] } },
                    { "id": "node-end", "type": "end", "position": { "x": 1500, "y": 150 }, "data": { "label": "Kết thúc" } }
                  ],
                  "edges": [
                    { "id": "edge-start", "source": "node-start", "target": "fstep_1", "label": "Bắt đầu" },
                    { "id": "e-f1-f2", "source": "fstep_1", "target": "fstep_2", "label": "Tiếp tục" },
                    { "id": "e-f2-f3", "source": "fstep_2", "target": "fstep_3", "label": "Tiếp tục" },
                    { "id": "edge-end", "source": "fstep_3", "target": "node-end", "label": "Hoàn thành" }
                  ]
                }""";
    }
}
