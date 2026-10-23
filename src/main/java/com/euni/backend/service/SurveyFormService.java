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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
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

    @Transactional(readOnly = true)
    public SurveyFormDto getFormForStep(Long stepId) {
        SurveyCampaignStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaignStep", "id", stepId));

        Optional<SurveyForm> currentFormOpt = formRepository.findByCampaignStepIdAndIsCurrentTrueAndDeletedFalse(stepId);
        if (currentFormOpt.isEmpty()) {
            currentFormOpt = formRepository.findByCampaignStepIdAndDeletedFalse(stepId);
        }

        if (currentFormOpt.isPresent()) {
            return convertToDto(currentFormOpt.get());
        }

        // Chưa có biểu mẫu trong DB: Trả về DTO mẫu dựng sẵn trong bộ nhớ, KHÔNG ghi vào DB
        Program program = step.getCampaign().getProgram();
        return buildDefaultInitialFormDto(step, program);
    }

    @Transactional(readOnly = true)
    public SurveyFormDto getOrCreateFormForStep(Long stepId, String username) {
        return getFormForStep(stepId);
    }

    @Transactional
    public SurveyFormDto saveForm(Long stepId, SurveyFormSaveRequest request, String username) {
        SurveyCampaignStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaignStep", "id", stepId));

        SurveyForm currentForm = formRepository.findByCampaignStepIdAndIsCurrentTrueAndDeletedFalse(stepId)
                .orElse(null);

        SurveyForm targetForm;
        if (currentForm == null) {
            // Lần đầu tiên maker lưu nháp: Khởi tạo version 1 trong cơ sở dữ liệu
            Program program = step.getCampaign().getProgram();
            targetForm = SurveyForm.builder()
                    .campaignStep(step)
                    .program(program)
                    .version(1)
                    .isCurrent(true)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .targetAudience(request.getTargetAudience())
                    .anonymous(request.isAnonymous())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .publicSlug(null)
                    .status(SurveyFormStatus.DRAFT)
                    .createdBy(username)
                    .build();
            targetForm = formRepository.save(targetForm);

            if (!"COMPLETED".equalsIgnoreCase(step.getStatus()) && !"UNDER_REVIEW".equalsIgnoreCase(step.getStatus())) {
                step.setStatus("IN_PROGRESS");
                stepRepository.save(step);
            }
        } else {
            long existingSubs = submissionRepository.countByFormIdAndDeletedFalse(currentForm.getId());
            if (existingSubs > 0) {
                // Yêu cầu 1: Nếu đã có đơn ý kiến, tăng version của phiếu khảo sát
                currentForm.setIsCurrent(false);
                if (currentForm.getStatus() == SurveyFormStatus.PUBLISHED) {
                    currentForm.setStatus(SurveyFormStatus.CLOSED);
                }
                formRepository.saveAndFlush(currentForm);

                int nextVersion = (currentForm.getVersion() != null ? currentForm.getVersion() : 1) + 1;
                targetForm = SurveyForm.builder()
                        .campaignStep(step)
                        .program(step.getCampaign().getProgram())
                        .version(nextVersion)
                        .isCurrent(true)
                        .previousVersionId(currentForm.getId())
                        .title(request.getTitle())
                        .description(request.getDescription())
                        .targetAudience(request.getTargetAudience())
                        .anonymous(request.isAnonymous())
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .publicSlug(null)
                        .status(SurveyFormStatus.DRAFT)
                        .createdBy(username)
                        .build();
                targetForm = formRepository.save(targetForm);
            } else {
                // Chưa có người làm k/s thì khi sửa không cần tăng version
                targetForm = currentForm;
                targetForm.setTitle(request.getTitle());
                targetForm.setDescription(request.getDescription());
                targetForm.setTargetAudience(request.getTargetAudience());
                targetForm.setAnonymous(request.isAnonymous());
                targetForm.setStartDate(request.getStartDate());
                targetForm.setEndDate(request.getEndDate());
                targetForm.setStatus(SurveyFormStatus.DRAFT);
                targetForm.setUpdatedBy(username);
                targetForm.getQuestions().clear();
                targetForm.getSections().clear();
                targetForm = formRepository.save(targetForm);
            }
        }

        // Recreate sections and questions
        if (request.getSections() != null) {
            for (int sIdx = 0; sIdx < request.getSections().size(); sIdx++) {
                SurveySectionDto sDto = request.getSections().get(sIdx);
                SurveySection section = SurveySection.builder()
                        .form(targetForm)
                        .title(sDto.getTitle())
                        .description(sDto.getDescription())
                        .orderIndex(sIdx + 1)
                        .build();
                SurveySection savedSection = sectionRepository.save(section);
                targetForm.getSections().add(savedSection);

                if (sDto.getQuestions() != null) {
                    for (int qIdx = 0; qIdx < sDto.getQuestions().size(); qIdx++) {
                        SurveyQuestionDto qDto = sDto.getQuestions().get(qIdx);
                        saveQuestionWithOrder(targetForm, savedSection, qDto, qIdx + 1);
                    }
                }
            }
        }

        if (request.getDirectQuestions() != null) {
            for (int qIdx = 0; qIdx < request.getDirectQuestions().size(); qIdx++) {
                SurveyQuestionDto qDto = request.getDirectQuestions().get(qIdx);
                saveQuestionWithOrder(targetForm, null, qDto, qIdx + 1);
            }
        }

        if (!"COMPLETED".equalsIgnoreCase(step.getStatus()) && !"UNDER_REVIEW".equalsIgnoreCase(step.getStatus())) {
            step.setStatus("IN_PROGRESS");
            stepRepository.save(step);
        }

        SurveyForm updated = formRepository.save(targetForm);
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
    public SurveyFormDto submitReview(Long stepId, String username) {
        SurveyCampaignStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaignStep", "id", stepId));

        SurveyForm form = formRepository.findByCampaignStepIdAndIsCurrentTrueAndDeletedFalse(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm for Step", "stepId", stepId));

        form.setStatus(SurveyFormStatus.PENDING_REVIEW);
        form.setUpdatedBy(username);
        formRepository.save(form);

        step.setStatus("UNDER_REVIEW");
        stepRepository.save(step);

        return convertToDto(form);
    }

    @Transactional
    public SurveyFormDto recallSubmission(Long stepId, String username) {
        SurveyCampaignStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaignStep", "id", stepId));

        SurveyForm form = formRepository.findByCampaignStepIdAndIsCurrentTrueAndDeletedFalse(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm for Step", "stepId", stepId));

        if (form.getStatus() != SurveyFormStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Chỉ có thể thu hồi khi biểu mẫu đang ở trạng thái Chờ phê duyệt!");
        }

        form.setStatus(SurveyFormStatus.DRAFT);
        form.setUpdatedBy(username);
        formRepository.save(form);

        step.setStatus("IN_PROGRESS");
        stepRepository.save(step);

        return convertToDto(form);
    }

    @Transactional
    public SurveyFormDto approveDesign(Long stepId, String username) {
        SurveyCampaignStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaignStep", "id", stepId));

        SurveyForm form = formRepository.findByCampaignStepIdAndIsCurrentTrueAndDeletedFalse(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm for Step", "stepId", stepId));

        form.setStatus(SurveyFormStatus.APPROVED);
        form.setUpdatedBy(username);
        formRepository.save(form);

        step.setStatus("IN_PROGRESS");
        updateStepConfigDesignReview(step, true, null, username);
        stepRepository.save(step);

        return convertToDto(form);
    }

    @Transactional
    public SurveyFormDto rejectDesign(Long stepId, String reason, String username) {
        SurveyCampaignStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaignStep", "id", stepId));

        SurveyForm form = formRepository.findByCampaignStepIdAndIsCurrentTrueAndDeletedFalse(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm for Step", "stepId", stepId));

        form.setStatus(SurveyFormStatus.DRAFT);
        form.setUpdatedBy(username);
        formRepository.save(form);

        step.setStatus("REJECTED");
        updateStepConfigDesignReview(step, false, reason, username);
        stepRepository.save(step);

        return convertToDto(form);
    }

    @Transactional
    public SurveyFormDto publishForm(Long stepId, String username) {
        SurveyCampaignStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaignStep", "id", stepId));

        SurveyForm form = formRepository.findByCampaignStepIdAndIsCurrentTrueAndDeletedFalse(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm for Step", "stepId", stepId));

        // Yêu cầu 2: Phải được approver duyệt rồi thì mới cho phép tạo link khảo sát
        if (form.getStatus() != SurveyFormStatus.APPROVED) {
            throw new IllegalStateException("Biểu mẫu khảo sát phải được cấp có thẩm quyền phê duyệt thiết kế trước khi xuất bản và tạo link khảo sát!");
        }

        if (form.getPublicSlug() == null || form.getPublicSlug().isBlank()) {
            Program program = form.getProgram();
            Long campaignId = step.getCampaign() != null ? step.getCampaign().getId() : form.getId();
            String codePrefix = (program != null && program.getCode() != null)
                    ? program.getCode().toLowerCase().replaceAll("[^a-z0-9]", "-")
                    : "survey";
            int v = form.getVersion() != null ? form.getVersion() : 1;
            String slug = "survey-" + codePrefix + "-c" + campaignId + "-v" + v + "-" + UUID.randomUUID().toString().substring(0, 8);
            form.setPublicSlug(slug);
        }

        form.setStatus(SurveyFormStatus.PUBLISHED);
        form.setUpdatedBy(username);
        step.setStatus("IN_PROGRESS");
        stepRepository.save(step);

        return convertToDto(formRepository.save(form));
    }

    @Transactional
    public SurveyFormDto requestRevision(Long stepId, String username) {
        // Yêu cầu 3: Đang khảo sát rồi thì approver có thể cho phép sửa form khảo sát, khi đó khóa link cũ
        SurveyCampaignStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyCampaignStep", "id", stepId));

        SurveyForm oldForm = formRepository.findByCampaignStepIdAndIsCurrentTrueAndDeletedFalse(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm for Step", "stepId", stepId));

        // 1. Khóa link cũ
        oldForm.setStatus(SurveyFormStatus.CLOSED);
        oldForm.setIsCurrent(false);
        oldForm.setUpdatedBy(username);
        formRepository.saveAndFlush(oldForm);

        // 2. Tạo form mới tăng version, trạng thái DRAFT
        int nextVersion = (oldForm.getVersion() != null ? oldForm.getVersion() : 1) + 1;
        SurveyForm newForm = SurveyForm.builder()
                .campaignStep(step)
                .program(oldForm.getProgram())
                .version(nextVersion)
                .isCurrent(true)
                .previousVersionId(oldForm.getId())
                .title(oldForm.getTitle())
                .description(oldForm.getDescription())
                .targetAudience(oldForm.getTargetAudience())
                .anonymous(oldForm.isAnonymous())
                .startDate(oldForm.getStartDate())
                .endDate(oldForm.getEndDate())
                .publicSlug(null)
                .status(SurveyFormStatus.DRAFT)
                .createdBy(username)
                .build();
        SurveyForm savedNewForm = formRepository.save(newForm);

        // Sao chép cấu trúc
        cloneFormStructure(oldForm, savedNewForm);

        step.setStatus("IN_PROGRESS");
        updateStepConfigRevision(step, username);
        stepRepository.save(step);

        return convertToDto(formRepository.findById(savedNewForm.getId()).orElse(savedNewForm));
    }

    private void cloneFormStructure(SurveyForm sourceForm, SurveyForm targetForm) {
        List<SurveySection> sourceSections = sectionRepository.findAllByFormIdAndDeletedFalseOrderByOrderIndexAsc(sourceForm.getId());
        List<SurveyQuestion> sourceQuestions = questionRepository.findAllByFormIdAndDeletedFalseOrderByOrderIndexAsc(sourceForm.getId());

        Map<Long, SurveySection> oldToNewSectionMap = new HashMap<>();
        for (SurveySection oldSec : sourceSections) {
            SurveySection newSec = sectionRepository.save(SurveySection.builder()
                    .form(targetForm)
                    .title(oldSec.getTitle())
                    .description(oldSec.getDescription())
                    .orderIndex(oldSec.getOrderIndex())
                    .build());
            targetForm.getSections().add(newSec);
            oldToNewSectionMap.put(oldSec.getId(), newSec);
        }

        for (SurveyQuestion oldQ : sourceQuestions) {
            SurveySection newSec = oldQ.getSection() != null ? oldToNewSectionMap.get(oldQ.getSection().getId()) : null;
            SurveyQuestion newQ = questionRepository.save(SurveyQuestion.builder()
                    .form(targetForm)
                    .section(newSec)
                    .content(oldQ.getContent())
                    .helpText(oldQ.getHelpText())
                    .questionType(oldQ.getQuestionType())
                    .required(oldQ.isRequired())
                    .orderIndex(oldQ.getOrderIndex())
                    .minRating(oldQ.getMinRating())
                    .maxRating(oldQ.getMaxRating())
                    .build());
            targetForm.getQuestions().add(newQ);

            if (oldQ.getOptions() != null) {
                for (SurveyQuestionOption oldOpt : oldQ.getOptions()) {
                    if (!oldOpt.isDeleted()) {
                        SurveyQuestionOption newOpt = optionRepository.save(SurveyQuestionOption.builder()
                                .question(newQ)
                                .content(oldOpt.getContent())
                                .orderIndex(oldOpt.getOrderIndex())
                                .build());
                        newQ.getOptions().add(newOpt);
                    }
                }
            }
        }
    }

    @Transactional
    public SurveyFormDto closeForm(Long stepId, String username) {
        SurveyForm form = formRepository.findByCampaignStepIdAndIsCurrentTrueAndDeletedFalse(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm for Step", "stepId", stepId));

        form.setStatus(SurveyFormStatus.CLOSED);
        form.setUpdatedBy(username);

        SurveyCampaignStep step = form.getCampaignStep();
        if (step != null) {
            stepRepository.save(step);
        }

        return convertToDto(formRepository.save(form));
    }

    @Transactional(readOnly = true)
    public List<SurveyFormVersionDto> getFormVersions(Long stepId) {
        List<SurveyForm> forms = formRepository.findAllByCampaignStepIdAndDeletedFalseOrderByVersionDesc(stepId);
        return forms.stream().map(f -> {
            long count = submissionRepository.countByFormIdAndDeletedFalse(f.getId());
            return SurveyFormVersionDto.builder()
                    .id(f.getId())
                    .campaignStepId(stepId)
                    .version(f.getVersion() != null ? f.getVersion() : 1)
                    .status(f.getStatus())
                    .title(f.getTitle())
                    .publicSlug(f.getPublicSlug())
                    .totalSubmissions(count)
                    .isCurrent(Boolean.TRUE.equals(f.getIsCurrent()))
                    .createdAt(f.getCreatedAt())
                    .updatedAt(f.getUpdatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SurveyFormDto getFormById(Long formId) {
        SurveyForm form = formRepository.findByIdAndDeletedFalse(formId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm", "id", formId));
        return convertToDto(form);
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

        SurveyCampaignStep step = form.getCampaignStep();
        if (step != null) {
            try {
                Map<String, Object> summary = new HashMap<>();
                summary.put("currentVersion", form.getVersion() != null ? form.getVersion() : 1);
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
        SurveyForm form = formRepository.findByCampaignStepIdAndIsCurrentTrueAndDeletedFalse(stepId)
                .or(() -> formRepository.findByCampaignStepIdAndDeletedFalse(stepId))
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm for Step", "stepId", stepId));

        return getSubmissionsForForm(form);
    }

    @Transactional(readOnly = true)
    public List<SurveySubmissionDto> getFormSubmissionsByFormId(Long formId) {
        SurveyForm form = formRepository.findByIdAndDeletedFalse(formId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm", "id", formId));
        return getSubmissionsForForm(form);
    }

    private List<SurveySubmissionDto> getSubmissionsForForm(SurveyForm form) {
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
        SurveyForm form = formRepository.findByCampaignStepIdAndIsCurrentTrueAndDeletedFalse(stepId)
                .or(() -> formRepository.findByCampaignStepIdAndDeletedFalse(stepId))
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm for Step", "stepId", stepId));

        return getSummaryForForm(form);
    }

    @Transactional(readOnly = true)
    public SurveySummaryDto getFormSummaryByFormId(Long formId) {
        SurveyForm form = formRepository.findByIdAndDeletedFalse(formId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyForm", "id", formId));
        return getSummaryForForm(form);
    }

    private SurveySummaryDto getSummaryForForm(SurveyForm form) {
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
                List<Integer> ratings = answers.stream()
                        .map(SurveyAnswer::getNumericValue)
                        .filter(Objects::nonNull)
                        .toList();
                if (!ratings.isEmpty()) {
                    avgScore = ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
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
                .publicUrl(form.getPublicSlug() != null ? "/public/survey/" + form.getPublicSlug() : null)
                .status(form.getStatus().name())
                .questionStats(questionStats)
                .build();
    }

    private void updateStepConfigDesignReview(SurveyCampaignStep step, boolean approved, String reviewNote, String username) {
        try {
            ObjectNode configNode;
            if (step.getConfiguration() != null && !step.getConfiguration().isBlank()) {
                JsonNode parsed = objectMapper.readTree(step.getConfiguration());
                configNode = parsed.isObject() ? (ObjectNode) parsed : objectMapper.createObjectNode();
            } else {
                configNode = objectMapper.createObjectNode();
            }
            configNode.put("designApproved", approved);
            if (approved) {
                configNode.put("designApprovedBy", username);
                configNode.put("designApprovedAt", Instant.now().toString());
                configNode.remove("reviewNote");
            } else {
                configNode.put("reviewNote", reviewNote != null ? reviewNote : "");
                configNode.put("reviewedBy", username);
                configNode.put("reviewedAt", Instant.now().toString());
            }
            step.setConfiguration(objectMapper.writeValueAsString(configNode));
        } catch (Exception e) {
            log.warn("Error updating step configuration", e);
        }
    }

    private void updateStepConfigRevision(SurveyCampaignStep step, String username) {
        try {
            ObjectNode configNode;
            if (step.getConfiguration() != null && !step.getConfiguration().isBlank()) {
                JsonNode parsed = objectMapper.readTree(step.getConfiguration());
                configNode = parsed.isObject() ? (ObjectNode) parsed : objectMapper.createObjectNode();
            } else {
                configNode = objectMapper.createObjectNode();
            }
            configNode.put("revisionRequestedBy", username);
            configNode.put("revisionRequestedAt", Instant.now().toString());
            configNode.put("designApproved", false);
            step.setConfiguration(objectMapper.writeValueAsString(configNode));
        } catch (Exception e) {
            log.warn("Error updating step configuration", e);
        }
    }

    private SurveyFormDto buildDefaultInitialFormDto(SurveyCampaignStep step, Program program) {
        String progName = program != null ? program.getName() : "Chương trình đào tạo";
        String progCode = program != null ? program.getCode() : "";
        Long progId = program != null ? program.getId() : null;

        List<SurveySectionDto> defaultSections = buildDefaultSectionDtos();

        return SurveyFormDto.builder()
                .id(null)
                .campaignStepId(step.getId())
                .programId(progId)
                .programCode(progCode)
                .programName(progName)
                .title("Phiếu Khảo sát Nhu cầu Đào tạo - " + progName)
                .description("Để phát triển và hoàn thiện chương trình đào tạo, kính đề nghị Quý cơ quan, Đơn vị, Chuyên gia và Cựu sinh viên đóng góp ý kiến về nhu cầu đào tạo nhân lực.")
                .targetAudience("Nhà tuyển dụng, Chuyên gia chuyên môn, Giảng viên, Cựu sinh viên")
                .publicSlug(null)
                .publicUrl(null)
                .status(null)
                .version(null)
                .isCurrent(false)
                .previousVersionId(null)
                .anonymous(false)
                .startDate(ZonedDateTime.now())
                .endDate(ZonedDateTime.now().plusMonths(1))
                .totalSubmissions(0)
                .sections(defaultSections)
                .directQuestions(new ArrayList<>())
                .build();
    }

    private List<SurveySectionDto> buildDefaultSectionDtos() {
        List<SurveySectionDto> sections = new ArrayList<>();

        List<SurveyOptionDto> opts1 = List.of(
                "Doanh nghiệp / Nhà tuyển dụng", "Nhà khoa học / Chuyên gia", 
                "Giảng viên", "Cựu sinh viên", "Đại diện hiệp hội nghề nghiệp"
        ).stream().map(txt -> SurveyOptionDto.builder().content(txt).build()).toList();

        SurveyQuestionDto q1 = SurveyQuestionDto.builder()
                .content("Nhóm đối tượng đóng góp ý kiến:")
                .questionType(SurveyQuestionType.MULTIPLE_CHOICE)
                .required(true)
                .orderIndex(1)
                .options(opts1)
                .build();

        sections.add(SurveySectionDto.builder()
                .title("I. Thông tin đối tượng tham gia khảo sát")
                .description("Vui lòng cho biết vai trò và lĩnh vực hoạt động chính của Quý cơ quan / cá nhân.")
                .orderIndex(1)
                .questions(List.of(q1))
                .build());

        List<String> kQuestions = List.of(
                "Kiến thức thực tế vững chắc, kiến thức lý thuyết sâu rộng trong phạm vi của ngành đào tạo.",
                "Kiến thức về công nghệ thông tin và ứng dụng chuyển đổi số đáp ứng yêu cầu công việc.",
                "Kiến thức cơ bản về quản lý, điều hành hoạt động chuyên môn."
        );
        List<SurveyQuestionDto> qSec2 = new ArrayList<>();
        for (int i = 0; i < kQuestions.size(); i++) {
            qSec2.add(SurveyQuestionDto.builder()
                    .content(kQuestions.get(i))
                    .questionType(SurveyQuestionType.LIKERT_SCALE)
                    .required(true)
                    .orderIndex(i + 1)
                    .minRating(1)
                    .maxRating(5)
                    .build());
        }
        sections.add(SurveySectionDto.builder()
                .title("II. Yêu cầu đối với nhân lực về KIẾN THỨC")
                .description("Vui lòng đánh giá mức độ quan trọng theo thang điểm 1 đến 5 (1: Hoàn toàn không đồng ý -> 5: Hoàn toàn đồng ý).")
                .orderIndex(2)
                .questions(qSec2)
                .build());

        List<String> sQuestions = List.of(
                "Kỹ năng giải quyết các vấn đề kỹ thuật phức tạp trong môi trường thực tế.",
                "Kỹ năng làm việc nhóm và giao tiếp, truyền đạt giải pháp chuyên môn.",
                "Năng lực ngoại ngữ và khả năng tự nghiên cứu tài liệu quốc tế."
        );
        List<SurveyQuestionDto> qSec3 = new ArrayList<>();
        for (int i = 0; i < sQuestions.size(); i++) {
            qSec3.add(SurveyQuestionDto.builder()
                    .content(sQuestions.get(i))
                    .questionType(SurveyQuestionType.LIKERT_SCALE)
                    .required(true)
                    .orderIndex(i + 1)
                    .minRating(1)
                    .maxRating(5)
                    .build());
        }
        sections.add(SurveySectionDto.builder()
                .title("III. Yêu cầu đối với nhân lực về KỸ NĂNG")
                .description("Đánh giá các nhóm kỹ năng nghề nghiệp và kỹ năng mềm.")
                .orderIndex(3)
                .questions(qSec3)
                .build());

        List<SurveyQuestionDto> qSec4 = List.of(
                SurveyQuestionDto.builder()
                        .content("Nhu cầu tuyển dụng nhân lực ngành đào tạo này hàng năm của Đơn vị (ghi rõ số lượng ước tính):")
                        .questionType(SurveyQuestionType.SHORT_TEXT)
                        .required(false)
                        .orderIndex(1)
                        .build(),
                SurveyQuestionDto.builder()
                        .content("Ý kiến đóng góp khác của Quý vị nhằm nâng cao chất lượng chương trình đào tạo:")
                        .questionType(SurveyQuestionType.LONG_TEXT)
                        .required(false)
                        .orderIndex(2)
                        .build()
        );
        sections.add(SurveySectionDto.builder()
                .title("IV. Nhu cầu tuyển dụng và ý kiến đóng góp")
                .description("Thông tin dự báo nhu cầu nhân lực trong 3-5 năm tới.")
                .orderIndex(4)
                .questions(qSec4)
                .build());

        return sections;
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
                .publicUrl((form.getStatus() == SurveyFormStatus.PUBLISHED && form.getPublicSlug() != null) ? "/public/survey/" + form.getPublicSlug() : null)
                .status(form.getStatus())
                .version(form.getVersion() != null ? form.getVersion() : 1)
                .isCurrent(Boolean.TRUE.equals(form.getIsCurrent()))
                .previousVersionId(form.getPreviousVersionId())
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
