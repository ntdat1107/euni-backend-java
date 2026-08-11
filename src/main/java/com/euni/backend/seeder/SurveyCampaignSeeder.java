package com.euni.backend.seeder;

import com.euni.backend.entity.Program;
import com.euni.backend.entity.SurveyCampaign;
import com.euni.backend.entity.SurveyCampaignStep;
import com.euni.backend.entity.WorkflowTemplate;
import com.euni.backend.entity.enums.SurveyCampaignStatus;
import com.euni.backend.entity.ProgramCourse;
import com.euni.backend.entity.SurveyCampaignCourse;
import com.euni.backend.repository.ProgramCourseRepository;
import com.euni.backend.repository.ProgramRepository;
import com.euni.backend.repository.SurveyCampaignCourseRepository;
import com.euni.backend.repository.SurveyCampaignRepository;
import com.euni.backend.repository.WorkflowTemplateRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(4)
@ConditionalOnProperty(name = "app.seeder.survey-campaign.enabled", havingValue = "true", matchIfMissing = true)
public class SurveyCampaignSeeder implements CommandLineRunner {

    private final SurveyCampaignRepository surveyCampaignRepository;
    private final ProgramRepository programRepository;
    private final WorkflowTemplateRepository workflowTemplateRepository;
    private final ProgramCourseRepository programCourseRepository;
    private final SurveyCampaignCourseRepository surveyCampaignCourseRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting safe Survey Campaign seeding (natural key existence checks)...");

        // 1. Get programs and workflow templates
        Optional<Program> seProgOpt = programRepository.findByCode("CTDT-SE-CLC-2026");
        Optional<Program> csProgOpt = programRepository.findByCode("CTDT-CS-CLC-2025");
        Optional<Program> isProgOpt = programRepository.findByCode("CTDT-IS-2025");
        Optional<WorkflowTemplate> wfOpt = workflowTemplateRepository.findTopByCodeAndDeletedFalseOrderByVersionDesc("WF_SURVEY_STANDARD_6STEP");

        if (seProgOpt.isEmpty() || wfOpt.isEmpty()) {
            log.warn("Required Program or WorkflowTemplate not found, skipping campaign seeding.");
            return;
        }

        Program seProg = seProgOpt.get();
        WorkflowTemplate wf = wfOpt.get();

        // 2. Seed Campaign 1: Active SE Campaign with realistic steps & step execution data
        seedActiveSeCampaign(seProg, wf);

        // 3. Seed Campaign 2: Approved CS Campaign
        if (csProgOpt.isPresent()) {
            seedCompletedCsCampaign(csProgOpt.get(), wf);
        }

        // 4. Seed Campaign 3: Cancelled IS Campaign
        if (isProgOpt.isPresent()) {
            seedCancelledIsCampaign(isProgOpt.get(), wf);
        }

        log.info("Survey Campaign seeding completed successfully.");
    }

    private void seedActiveSeCampaign(Program program, WorkflowTemplate wfTemplate) {
        String code = "CAM-SE-CLC-2026";
        surveyCampaignRepository.findByCode(code).orElseGet(() -> {
            try {
                SurveyCampaign campaign = SurveyCampaign.builder()
                        .name("Đợt Khảo sát & Cập nhật CTĐT Kỹ thuật Phần mềm CLC 2026")
                        .code(code)
                        .description("Đợt khảo sát toàn diện rà soát chuẩn đầu ra PLO, xây dựng Đề cương CLO và Ma trận kỹ năng cho khóa 2026.")
                        .program(program)
                        .workflowTemplate(wfTemplate)
                        .status(SurveyCampaignStatus.ACTIVE)
                        .startDate(LocalDateTime.now().minusDays(5))
                        .endDate(LocalDateTime.now().plusDays(25))
                        .build();

                List<SurveyCampaignStep> steps = buildStepsFromTemplate(campaign, wfTemplate);
                campaign.setSteps(steps);
                SurveyCampaign saved = surveyCampaignRepository.save(campaign);
                seedCampaignCourses(saved, program);
                return saved;
            } catch (Exception e) {
                log.error("Failed to seed campaign {}", code, e);
                return null;
            }
        });
    }

    private void seedCompletedCsCampaign(Program program, WorkflowTemplate wfTemplate) {
        String code = "CAM-CS-CLC-2025";
        surveyCampaignRepository.findByCode(code).orElseGet(() -> {
            try {
                SurveyCampaign campaign = SurveyCampaign.builder()
                        .name("Đợt Khảo sát Đánh giá định kỳ CTĐT Khoa học Máy tính 2025")
                        .code(code)
                        .description("Đợt khảo sát hoàn thành và đã phê duyệt đồng bộ Master Data năm 2025.")
                        .program(program)
                        .workflowTemplate(wfTemplate)
                        .status(SurveyCampaignStatus.APPROVED)
                        .startDate(LocalDateTime.now().minusMonths(6))
                        .endDate(LocalDateTime.now().minusMonths(5))
                        .build();

                List<SurveyCampaignStep> steps = buildStepsFromTemplate(campaign, wfTemplate);
                steps.forEach(s -> s.setStatus("COMPLETED"));
                campaign.setSteps(steps);
                SurveyCampaign saved = surveyCampaignRepository.save(campaign);
                seedCampaignCourses(saved, program);
                return saved;
            } catch (Exception e) {
                log.error("Failed to seed campaign {}", code, e);
                return null;
            }
        });
    }

    private void seedCancelledIsCampaign(Program program, WorkflowTemplate wfTemplate) {
        String code = "CAM-IS-2025-CANCELLED";
        surveyCampaignRepository.findByCode(code).orElseGet(() -> {
            try {
                SurveyCampaign campaign = SurveyCampaign.builder()
                        .name("Đợt Khảo sát CTĐT Hệ thống Thông tin 2025 (Đã Hủy)")
                        .code(code)
                        .description("Đợt khảo sát đã hủy bỏ để khởi tạo đợt quy chuẩn mới.")
                        .program(program)
                        .workflowTemplate(wfTemplate)
                        .status(SurveyCampaignStatus.CANCELLED)
                        .startDate(LocalDateTime.now().minusMonths(3))
                        .endDate(LocalDateTime.now().minusMonths(2))
                        .build();

                List<SurveyCampaignStep> steps = buildStepsFromTemplate(campaign, wfTemplate);
                campaign.setSteps(steps);
                SurveyCampaign saved = surveyCampaignRepository.save(campaign);
                seedCampaignCourses(saved, program);
                return saved;
            } catch (Exception e) {
                log.error("Failed to seed campaign {}", code, e);
                return null;
            }
        });
    }

    private void seedCampaignCourses(SurveyCampaign campaign, Program program) {
        try {
            List<ProgramCourse> pcs = programCourseRepository.findAllByProgramId(program.getId());
            for (ProgramCourse pc : pcs) {
                if (!surveyCampaignCourseRepository.existsByCampaignIdAndCourseId(campaign.getId(), pc.getCourse().getId())) {
                    SurveyCampaignCourse scc = SurveyCampaignCourse.builder()
                            .campaign(campaign)
                            .course(pc.getCourse())
                            .status(campaign.getStatus() == SurveyCampaignStatus.APPROVED ? "COMPLETED" : "DRAFT")
                            .syllabusData("{}")
                            .build();
                    surveyCampaignCourseRepository.save(scc);
                }
            }
        } catch (Exception e) {
            log.error("Failed to seed campaign courses for campaign {}", campaign.getCode(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<SurveyCampaignStep> buildStepsFromTemplate(SurveyCampaign campaign, WorkflowTemplate wfTemplate) {
        List<SurveyCampaignStep> steps = new ArrayList<>();
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(wfTemplate.getJsonContent(), new TypeReference<>() {});
            List<Map<String, Object>> templateSteps = (List<Map<String, Object>>) jsonMap.get("steps");
            if (templateSteps == null) return steps;

            for (Map<String, Object> tStep : templateSteps) {
                String name = (String) tStep.get("name");
                String stepCode = (String) tStep.get("code");
                String screenCode = (String) tStep.get("screenCode");
                String masterStepId = (String) tStep.get("masterStepId");
                Integer orderNo = (Integer) tStep.get("orderNo");
                List<String> prereqs = (List<String>) tStep.get("prerequisiteStepCodes");

                Map<String, Object> config = new HashMap<>();
                config.put("screenCode", screenCode != null ? screenCode : stepCode);
                config.put("stepCode", stepCode);
                config.put("masterStepId", masterStepId);
                config.put("performerRole", tStep.get("executorRole"));
                config.put("approverRole", tStep.get("approverRole"));
                config.put("prerequisiteStepCodes", prereqs != null ? prereqs : List.of());

                String status = (orderNo != null && orderNo == 1) ? "ACTIVE" : "DRAFT";

                SurveyCampaignStep step = SurveyCampaignStep.builder()
                        .campaign(campaign)
                        .stepIndex(orderNo != null ? orderNo : 1)
                        .stepName(name)
                        .status(status)
                        .configuration(objectMapper.writeValueAsString(config))
                        .requiredDocuments(objectMapper.writeValueAsString(List.of("Tờ trình", "Hồ sơ đính kèm")))
                        .deadline(LocalDateTime.now().plusDays(10L * (orderNo != null ? orderNo : 1)))
                        .build();

                steps.add(step);
            }
        } catch (Exception e) {
            log.error("Error building steps from template JSON", e);
        }
        return steps;
    }
}
