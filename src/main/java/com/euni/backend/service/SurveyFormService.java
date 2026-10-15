package com.euni.backend.service;

import com.euni.backend.dto.*;
import com.euni.backend.dto.request.SurveyFormSaveRequest;
import com.euni.backend.dto.request.SurveySubmissionRequest;
import com.euni.backend.entity.*;
import com.euni.backend.entity.enums.SurveyFormStatus;
import com.euni.backend.entity.enums.SurveyQuestionType;
import com.euni.backend.exception.ResourceNotFoundException;
import com.euni.backend.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyFormService {

    private final SurveyFormRepository formRepository;
    private final SurveySectionRepository sectionRepository;
    private final SurveyQuestionRepository questionRepository;
    private final SurveyQuestionOptionRepository optionRepository;
    private final SurveySubmissionRepository submissionRepository;
    private final SurveyAnswerRepository answerRepository;
    private final SurveyCampaignStepRepository stepRepository;
    private final ProgramRepository programRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public SurveyFormDto getOrCreateFormForStep(Long stepId, String username) {
        SurveyCampaignStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaignStep", "id", stepId));

        Optional<SurveyForm> existingForm = formRepository.findByCampaignStepIdAndDeletedFalse(stepId);
        if (existingForm.isPresent()) {
            SurveyForm form = existingForm.get();
            // Ensure step status is properly synced with form status
            if (form.getStatus() == SurveyFormStatus.PUBLISHED && !"ACTIVE".equals(step.getStatus()) && !"IN_PROGRESS".equals(step.getStatus())) {
                step.setStatus("ACTIVE");
                stepRepository.save(step);
            } else if (form.getStatus() == SurveyFormStatus.CLOSED && !"COMPLETED".equals(step.getStatus())) {
                step.setStatus("COMPLETED");
                stepRepository.save(step);
            }
            return convertToDto(form);
        }

        // Initialize default Google Form template for Bước 1 (DRAFT mode, no public slug yet)
        Program program = step.getCampaign().getProgram();

        SurveyForm form = SurveyForm.builder()
                .campaignStep(step)
                .program(program)
                .title("Phiếu Khảo sát Nhu cầu Đào tạo - " + program.getName())
                .description("<p>Để phát triển và hoàn thiện chương trình đào tạo, kính đề nghị Quý cơ quan, Đơn vị, Chuyên gia và Cựu sinh viên đóng góp ý kiến về nhu cầu đào tạo nhân lực.</p>")
                .targetAudience("Nhà tuyển dụng, Chuyên gia chuyên môn, Giảng viên, Cựu sinh viên")
                .publicSlug(null)
                .status(SurveyFormStatus.DRAFT)
                .anonymous(false)
                .startDate(ZonedDateTime.now())
                .endDate(ZonedDateTime.now().plusMonths(1))
                .createdBy(username)
                .build();

        SurveyForm savedForm = formRepository.save(form);
        seedDefaultSectionsAndQuestions(savedForm);

        return convertToDto(formRepository.findById(savedForm.getId()).orElse(savedForm));
    }

    @Transactional
    public SurveyFormDto saveForm(Long stepId, SurveyFormSaveRequest request, String username) {
        SurveyCampaignStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaignStep", "id", stepId));

        SurveyForm form = formRepository.findByCampaignStepIdAndDeletedFalse(stepId)
                .orElseGet(() -> {
                    Program program = step.getCampaign().getProgram();
                    return SurveyForm.builder()
                            .campaignStep(step)
                            .program(program)
                            .publicSlug(null)
                            .status(SurveyFormStatus.DRAFT)
                            .build();
                });

        if (form.getStatus() == SurveyFormStatus.CLOSED) {
            throw new IllegalStateException("Không thể chỉnh sửa biểu mẫu khảo sát đã kết thúc.");
        }

        if (form.getId() != null) {
            long existingSubs = submissionRepository.countByFormIdAndDeletedFalse(form.getId());
            if (existingSubs > 0) {
                throw new IllegalStateException("Biểu mẫu đã tiếp nhận " + existingSubs + " phản hồi. Để tránh xung đột dữ liệu, không thể thay đổi lại cấu trúc các phần và câu hỏi.");
            }
        }

        form.setTitle(request.getTitle());
        form.setDescription(request.getDescription());
        form.setTargetAudience(request.getTargetAudience());
        form.setAnonymous(request.isAnonymous());
        form.setStartDate(request.getStartDate());
        form.setEndDate(request.getEndDate());
        form.setUpdatedBy(username);

        // Clear existing questions, options and sections to recreate cleanly
        form.getQuestions().clear();
        form.getSections().clear();
        SurveyForm savedForm = formRepository.save(form);

        Map<Long, SurveySection> tempSectionMap = new HashMap<>();
        if (request.getSections() != null) {
            for (int sIdx = 0; sIdx < request.getSections().size(); sIdx++) {
                SurveySectionDto sDto = request.getSections().get(sIdx);
                SurveySection section = SurveySection.builder()
                        .form(savedForm)
                        .title(sDto.getTitle())
                        .description(sDto.getDescription())
                        .orderIndex(sIdx + 1)
                        .build();
                SurveySection savedSection = sectionRepository.save(section);
                savedForm.getSections().add(savedSection);
                if (sDto.getId() != null) {
                    tempSectionMap.put(sDto.getId(), savedSection);
                }

                // Add questions inside section
                if (sDto.getQuestions() != null) {
                    for (int qIdx = 0; qIdx < sDto.getQuestions().size(); qIdx++) {
                        SurveyQuestionDto qDto = sDto.getQuestions().get(qIdx);
                        saveQuestionWithOrder(savedForm, savedSection, qDto, qIdx + 1);
                    }
                }
            }
        }

        // Add direct questions not belonging to any section
        if (request.getDirectQuestions() != null) {
            for (int qIdx = 0; qIdx < request.getDirectQuestions().size(); qIdx++) {
                SurveyQuestionDto qDto = request.getDirectQuestions().get(qIdx);
                saveQuestionWithOrder(savedForm, null, qDto, qIdx + 1);
            }
        }

        SurveyForm updated = formRepository.save(savedForm);
        return convertToDto(updated);
    }

    private void saveQuestionWithOrder(SurveyForm form, SurveySection section, SurveyQuestionDto qDto, int orderIndex) {
        SurveyQuestion q = SurveyQuestion.builder()
                .form(form)
                .section(section)
                .content(qDto.getContent())
                .helpText(qDto.getHelpText())
                .questionType(qDto.getQuestionType() != null ? qDto.getQuestionType() : SurveyQuestionType.SHORT_TEXT)
                .required(qDto.isRequired())
                .orderIndex(orderIndex)
                .minRating(qDto.getMinRating() != null ? qDto.getMinRating() : 1)
                .maxRating(qDto.getMaxRating() != null ? qDto.getMaxRating() : 5)
                .build();

        SurveyQuestion savedQ = questionRepository.save(q);
        form.getQuestions().add(savedQ);

        if (qDto.getOptions() != null && !qDto.getOptions().isEmpty()) {
            for (int optIdx = 0; optIdx < qDto.getOptions().size(); optIdx++) {
                SurveyOptionDto oDto = qDto.getOptions().get(optIdx);
                SurveyQuestionOption opt = SurveyQuestionOption.builder()
                        .question(savedQ)
                        .content(oDto.getContent())
                        .orderIndex(optIdx + 1)
                        .build();
                optionRepository.save(opt);
                savedQ.getOptions().add(opt);
            }
        }
    }

    @Transactional
    public SurveyFormDto publishForm(Long stepId, String username) {
        SurveyForm form = formRepository.findByCampaignStepIdAndDeletedFalse(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm for Step", "stepId", stepId));

        // Generate public slug on publish event if not yet assigned
        if (form.getPublicSlug() == null || form.getPublicSlug().isBlank()) {
            Program program = form.getProgram();
            Long campaignId = form.getCampaignStep() != null && form.getCampaignStep().getCampaign() != null
                    ? form.getCampaignStep().getCampaign().getId()
                    : form.getId();
            String codePrefix = program != null && program.getCode() != null
                    ? program.getCode().toLowerCase().replaceAll("[^a-z0-9]", "-")
                    : "survey";
            String slug = "survey-" + codePrefix + "-c" + campaignId + "-" + UUID.randomUUID().toString().substring(0, 8);
            form.setPublicSlug(slug);
        }

        form.setStatus(SurveyFormStatus.PUBLISHED);
        form.setUpdatedBy(username);
        
        // Also update step status to ACTIVE
        SurveyCampaignStep step = form.getCampaignStep();
        if (step != null) {
            step.setStatus("ACTIVE");
            stepRepository.save(step);
        }

        return convertToDto(formRepository.save(form));
    }

    @Transactional
    public SurveyFormDto closeForm(Long stepId, String username) {
        SurveyForm form = formRepository.findByCampaignStepIdAndDeletedFalse(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm for Step", "stepId", stepId));

        form.setStatus(SurveyFormStatus.CLOSED);
        form.setUpdatedBy(username);

        // When closed, step can be marked COMPLETED
        SurveyCampaignStep step = form.getCampaignStep();
        if (step != null) {
            step.setStatus("COMPLETED");
            stepRepository.save(step);
        }

        return convertToDto(formRepository.save(form));
    }

    @Transactional(readOnly = true)
    public SurveyFormDto getPublicForm(String slug) {
        SurveyForm form = formRepository.findByPublicSlugAndDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm", "slug", slug));

        if (form.getStatus() != SurveyFormStatus.PUBLISHED) {
            throw new IllegalStateException("Khảo sát này chưa được mở công khai hoặc đã đóng.");
        }

        return convertToDto(form);
    }

    @Transactional
    public Map<String, Object> submitPublicSurvey(String slug, SurveySubmissionRequest request, String ipAddress) {
        SurveyForm form = formRepository.findByPublicSlugAndDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm", "slug", slug));

        if (form.getStatus() != SurveyFormStatus.PUBLISHED) {
            throw new IllegalStateException("Khảo sát này hiện không mở để tiếp nhận câu trả lời.");
        }

        SurveySubmission submission = SurveySubmission.builder()
                .form(form)
                .respondentName(request.getRespondentName())
                .respondentEmail(request.getRespondentEmail())
                .respondentPhone(request.getRespondentPhone())
                .organization(request.getOrganization())
                .position(request.getPosition())
                .ipAddress(ipAddress)
                .submittedAt(ZonedDateTime.now())
                .build();

        SurveySubmission savedSub = submissionRepository.save(submission);

        if (request.getAnswers() != null) {
            for (SurveySubmissionRequest.AnswerItem item : request.getAnswers()) {
                if (item.getQuestionId() == null) continue;
                SurveyQuestion q = questionRepository.findById(item.getQuestionId()).orElse(null);
                if (q == null) continue;

                String selectedOptionsStr = null;
                if (item.getSelectedOptions() != null && !item.getSelectedOptions().isEmpty()) {
                    try {
                        selectedOptionsStr = objectMapper.writeValueAsString(item.getSelectedOptions());
                    } catch (JsonProcessingException e) {
                        selectedOptionsStr = String.join(", ", item.getSelectedOptions());
                    }
                }

                SurveyAnswer answer = SurveyAnswer.builder()
                        .submission(savedSub)
                        .question(q)
                        .textValue(item.getTextValue())
                        .numericValue(item.getNumericValue())
                        .selectedOptions(selectedOptionsStr)
                        .build();

                answerRepository.save(answer);
            }
        }

        long totalCount = submissionRepository.countByFormIdAndDeletedFalse(form.getId());
        
        // Update resultData of the step with high-level summary
        SurveyCampaignStep step = form.getCampaignStep();
        if (step != null) {
            try {
                Map<String, Object> summary = new HashMap<>();
                summary.put("totalSubmissions", totalCount);
                summary.put("lastSubmittedAt", ZonedDateTime.now().toString());
                summary.put("status", form.getStatus().name());
                step.setResultData(objectMapper.writeValueAsString(summary));
                stepRepository.save(step);
            } catch (Exception e) {
                log.warn("Failed to update step resultData", e);
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("submissionId", savedSub.getId());
        resp.put("totalSubmissions", totalCount);
        resp.put("message", "Cảm ơn Quý vị đã hoàn thành phiếu khảo sát!");
        return resp;
    }

    @Transactional(readOnly = true)
    public List<SurveySubmissionDto> getFormSubmissions(Long stepId) {
        SurveyForm form = formRepository.findByCampaignStepIdAndDeletedFalse(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm for Step", "stepId", stepId));

        List<SurveySubmission> subs = submissionRepository.findAllByFormIdAndDeletedFalseOrderBySubmittedAtDesc(form.getId());
        return subs.stream().map(s -> {
            List<SurveySubmissionDto.AnswerDto> aDtos = s.getAnswers().stream().map(a -> {
                List<String> optionsList = new ArrayList<>();
                if (a.getSelectedOptions() != null) {
                    try {
                        optionsList = objectMapper.readValue(a.getSelectedOptions(), List.class);
                    } catch (Exception e) {
                        optionsList = List.of(a.getSelectedOptions());
                    }
                }
                return SurveySubmissionDto.AnswerDto.builder()
                        .id(a.getId())
                        .questionId(a.getQuestion().getId())
                        .questionContent(a.getQuestion().getContent())
                        .questionType(a.getQuestion().getQuestionType().name())
                        .textValue(a.getTextValue())
                        .numericValue(a.getNumericValue())
                        .selectedOptions(optionsList)
                        .build();
            }).collect(Collectors.toList());

            return SurveySubmissionDto.builder()
                    .id(s.getId())
                    .formId(form.getId())
                    .respondentName(s.getRespondentName())
                    .respondentEmail(s.getRespondentEmail())
                    .respondentPhone(s.getRespondentPhone())
                    .organization(s.getOrganization())
                    .position(s.getPosition())
                    .submittedAt(s.getSubmittedAt())
                    .answers(aDtos)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SurveySummaryDto getFormSummary(Long stepId) {
        SurveyForm form = formRepository.findByCampaignStepIdAndDeletedFalse(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm for Step", "stepId", stepId));

        List<SurveySubmission> subs = submissionRepository.findAllByFormIdAndDeletedFalseOrderBySubmittedAtDesc(form.getId());
        long totalSubs = subs.size();

        List<SurveySummaryDto.QuestionStatDto> questionStats = new ArrayList<>();
        List<SurveyQuestion> questions = questionRepository.findAllByFormIdAndDeletedFalseOrderByOrderIndexAsc(form.getId());

        for (SurveyQuestion q : questions) {
            List<SurveyAnswer> answers = answerRepository.findAllByQuestionIdAndDeletedFalse(q.getId());

            Double avgScore = null;
            Map<String, Long> distribution = new LinkedHashMap<>();
            List<String> textSamples = new ArrayList<>();

            if (q.getQuestionType() == SurveyQuestionType.LIKERT_SCALE) {
                // Calculate average and distribution 1 to 5
                List<Integer> ratings = answers.stream()
                        .map(SurveyAnswer::getNumericValue)
                        .filter(Objects::nonNull)
                        .toList();
                if (!ratings.isEmpty()) {
                    avgScore = ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                    // round to 2 decimals
                    avgScore = Math.round(avgScore * 100.0) / 100.0;
                }
                for (int level = 1; level <= 5; level++) {
                    final int lvl = level;
                    long count = ratings.stream().filter(r -> r == lvl).count();
                    distribution.put("Mức " + lvl, count);
                }
            } else if (q.getQuestionType() == SurveyQuestionType.SINGLE_CHOICE || q.getQuestionType() == SurveyQuestionType.MULTIPLE_CHOICE) {
                for (SurveyQuestionOption opt : q.getOptions()) {
                    distribution.put(opt.getContent(), 0L);
                }
                for (SurveyAnswer a : answers) {
                    if (a.getSelectedOptions() != null) {
                        try {
                            List<String> chosen = objectMapper.readValue(a.getSelectedOptions(), List.class);
                            for (String c : chosen) {
                                distribution.put(c, distribution.getOrDefault(c, 0L) + 1L);
                            }
                        } catch (Exception e) {
                            distribution.put(a.getSelectedOptions(), distribution.getOrDefault(a.getSelectedOptions(), 0L) + 1L);
                        }
                    }
                }
            } else {
                // text answers
                textSamples = answers.stream()
                        .map(SurveyAnswer::getTextValue)
                        .filter(t -> t != null && !t.isBlank())
                        .limit(20)
                        .collect(Collectors.toList());
            }

            questionStats.add(SurveySummaryDto.QuestionStatDto.builder()
                    .questionId(q.getId())
                    .content(q.getContent())
                    .questionType(q.getQuestionType().name())
                    .averageScore(avgScore)
                    .optionDistribution(distribution)
                    .textSamples(textSamples)
                    .build());
        }

        return SurveySummaryDto.builder()
                .formId(form.getId())
                .formTitle(form.getTitle())
                .totalSubmissions(totalSubs)
                .publicSlug(form.getPublicSlug())
                .publicUrl("/public/survey/" + form.getPublicSlug())
                .status(form.getStatus().name())
                .questionStats(questionStats)
                .build();
    }

    private void seedDefaultSectionsAndQuestions(SurveyForm form) {
        // Section 1: Đối tượng & Lĩnh vực hoạt động
        SurveySection s1 = sectionRepository.save(SurveySection.builder()
                .form(form)
                .title("I. Thông tin đối tượng tham gia khảo sát")
                .description("Vui lòng cho biết vai trò và lĩnh vực hoạt động chính của Quý cơ quan / cá nhân.")
                .orderIndex(1)
                .build());
        form.getSections().add(s1);

        SurveyQuestion q1 = questionRepository.save(SurveyQuestion.builder()
                .form(form)
                .section(s1)
                .content("Nhóm đối tượng đóng góp ý kiến:")
                .questionType(SurveyQuestionType.MULTIPLE_CHOICE)
                .required(true)
                .orderIndex(1)
                .build());
        form.getQuestions().add(q1);
        List<String> opts1 = List.of("Doanh nghiệp / Nhà tuyển dụng", "Nhà khoa học / Chuyên gia", "Giảng viên", "Cựu sinh viên", "Đại diện hiệp hội nghề nghiệp");
        for (int i = 0; i < opts1.size(); i++) {
            optionRepository.save(SurveyQuestionOption.builder().question(q1).content(opts1.get(i)).orderIndex(i + 1).build());
        }

        // Section 2: Yêu cầu về kiến thức
        SurveySection s2 = sectionRepository.save(SurveySection.builder()
                .form(form)
                .title("II. Yêu cầu đối với nhân lực về KIẾN THỨC")
                .description("Vui lòng đánh giá mức độ quan trọng theo thang điểm 1 đến 5 (1: Hoàn toàn không đồng ý -> 5: Hoàn toàn đồng ý).")
                .orderIndex(2)
                .build());
        form.getSections().add(s2);

        List<String> kQuestions = List.of(
                "Kiến thức thực tế vững chắc, kiến thức lý thuyết sâu rộng trong phạm vi của ngành đào tạo.",
                "Kiến thức về công nghệ thông tin và ứng dụng chuyển đổi số đáp ứng yêu cầu công việc.",
                "Kiến thức cơ bản về quản lý, điều hành hoạt động chuyên môn."
        );
        for (int i = 0; i < kQuestions.size(); i++) {
            SurveyQuestion q = questionRepository.save(SurveyQuestion.builder()
                    .form(form)
                    .section(s2)
                    .content(kQuestions.get(i))
                    .questionType(SurveyQuestionType.LIKERT_SCALE)
                    .required(true)
                    .orderIndex(i + 1)
                    .minRating(1)
                    .maxRating(5)
                    .build());
            form.getQuestions().add(q);
        }

        // Section 3: Yêu cầu về kỹ năng
        SurveySection s3 = sectionRepository.save(SurveySection.builder()
                .form(form)
                .title("III. Yêu cầu đối với nhân lực về KỸ NĂNG")
                .description("Đánh giá các nhóm kỹ năng nghề nghiệp và kỹ năng mềm.")
                .orderIndex(3)
                .build());
        form.getSections().add(s3);

        List<String> sQuestions = List.of(
                "Kỹ năng giải quyết các vấn đề kỹ thuật phức tạp trong môi trường thực tế.",
                "Kỹ năng làm việc nhóm và giao tiếp, truyền đạt giải pháp chuyên môn.",
                "Năng lực ngoại ngữ và khả năng tự nghiên cứu tài liệu quốc tế."
        );
        for (int i = 0; i < sQuestions.size(); i++) {
            SurveyQuestion q = questionRepository.save(SurveyQuestion.builder()
                    .form(form)
                    .section(s3)
                    .content(sQuestions.get(i))
                    .questionType(SurveyQuestionType.LIKERT_SCALE)
                    .required(true)
                    .orderIndex(i + 1)
                    .minRating(1)
                    .maxRating(5)
                    .build());
            form.getQuestions().add(q);
        }

        // Section 4: Nhu cầu nhân lực và tuyển dụng
        SurveySection s4 = sectionRepository.save(SurveySection.builder()
                .form(form)
                .title("IV. Nhu cầu tuyển dụng và ý kiến đóng góp")
                .description("Thông tin dự báo nhu cầu nhân lực trong 3-5 năm tới.")
                .orderIndex(4)
                .build());
        form.getSections().add(s4);

        SurveyQuestion qDemand1 = questionRepository.save(SurveyQuestion.builder()
                .form(form)
                .section(s4)
                .content("Nhu cầu tuyển dụng nhân lực ngành đào tạo này hàng năm của Đơn vị (ghi rõ số lượng ước tính):")
                .questionType(SurveyQuestionType.SHORT_TEXT)
                .required(false)
                .orderIndex(1)
                .build());
        form.getQuestions().add(qDemand1);

        SurveyQuestion qFeedback = questionRepository.save(SurveyQuestion.builder()
                .form(form)
                .section(s4)
                .content("Ý kiến đóng góp khác của Quý vị nhằm nâng cao chất lượng chương trình đào tạo:")
                .questionType(SurveyQuestionType.LONG_TEXT)
                .required(false)
                .orderIndex(2)
                .build());
        form.getQuestions().add(qFeedback);
    }

    private SurveyFormDto convertToDto(SurveyForm form) {
        long totalSubs = submissionRepository.countByFormIdAndDeletedFalse(form.getId());

        List<SurveySection> sections = sectionRepository.findAllByFormIdAndDeletedFalseOrderByOrderIndexAsc(form.getId());
        List<SurveyQuestion> allQuestions = questionRepository.findAllByFormIdAndDeletedFalseOrderByOrderIndexAsc(form.getId());

        List<SurveySectionDto> sectionDtos = new ArrayList<>();
        for (SurveySection s : sections) {
            List<SurveyQuestionDto> qDtos = allQuestions.stream()
                    .filter(q -> q.getSection() != null && q.getSection().getId().equals(s.getId()))
                    .map(this::convertQuestionToDto)
                    .collect(Collectors.toList());

            sectionDtos.add(SurveySectionDto.builder()
                    .id(s.getId())
                    .title(s.getTitle())
                    .description(s.getDescription())
                    .orderIndex(s.getOrderIndex())
                    .questions(qDtos)
                    .build());
        }

        List<SurveyQuestionDto> directQuestions = allQuestions.stream()
                .filter(q -> q.getSection() == null)
                .map(this::convertQuestionToDto)
                .collect(Collectors.toList());

        return SurveyFormDto.builder()
                .id(form.getId())
                .campaignStepId(form.getCampaignStep() != null ? form.getCampaignStep().getId() : null)
                .programId(form.getProgram().getId())
                .programCode(form.getProgram().getCode())
                .programName(form.getProgram().getName())
                .title(form.getTitle())
                .description(form.getDescription())
                .targetAudience(form.getTargetAudience())
                .publicSlug(form.getStatus() == SurveyFormStatus.PUBLISHED ? form.getPublicSlug() : null)
                .publicUrl(form.getStatus() == SurveyFormStatus.PUBLISHED && form.getPublicSlug() != null ? "/public/survey/" + form.getPublicSlug() : null)
                .status(form.getStatus())
                .anonymous(form.isAnonymous())
                .startDate(form.getStartDate())
                .endDate(form.getEndDate())
                .totalSubmissions(totalSubs)
                .sections(sectionDtos)
                .directQuestions(directQuestions)
                .build();
    }

    private SurveyQuestionDto convertQuestionToDto(SurveyQuestion q) {
        List<SurveyOptionDto> optDtos = q.getOptions().stream()
                .filter(o -> !o.isDeleted())
                .map(o -> SurveyOptionDto.builder()
                        .id(o.getId())
                        .content(o.getContent())
                        .orderIndex(o.getOrderIndex())
                        .build())
                .collect(Collectors.toList());

        return SurveyQuestionDto.builder()
                .id(q.getId())
                .sectionId(q.getSection() != null ? q.getSection().getId() : null)
                .content(q.getContent())
                .helpText(q.getHelpText())
                .questionType(q.getQuestionType())
                .required(q.isRequired())
                .orderIndex(q.getOrderIndex())
                .minRating(q.getMinRating())
                .maxRating(q.getMaxRating())
                .options(optDtos)
                .build();
    }
}
