package com.euni.backend.service;

import com.euni.backend.dto.AiPromptTemplateDto;
import com.euni.backend.dto.request.AiPromptTemplateRequest;
import com.euni.backend.entity.AiPromptTemplate;
import com.euni.backend.repository.AiPromptTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiPromptTemplateService {

    private final AiPromptTemplateRepository templateRepository;

    private static final Set<String> SYSTEM_DEFAULT_CODES = Set.of(
            "BASE_SYSTEM", "PEO", "CLO", "PLO", "SYLLABUS",
            "COURSE_DESCRIPTION", "PREREQUISITES", "ASSESSMENT", "DEFAULT"
    );

    @Transactional(readOnly = true)
    public List<AiPromptTemplateDto> getAllTemplates() {
        return templateRepository.findAllByDeletedFalseOrderByIdAsc()
                .stream()
                .map(AiPromptTemplateDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AiPromptTemplateDto getTemplateById(Long id) {
        AiPromptTemplate template = templateRepository.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Prompt Template với ID: " + id));
        return AiPromptTemplateDto.fromEntity(template);
    }

    @Transactional(readOnly = true)
    public AiPromptTemplateDto getTemplateByCode(String code) {
        AiPromptTemplate template = templateRepository.findByCodeAndDeletedFalse(code.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Prompt Template với mã: " + code));
        return AiPromptTemplateDto.fromEntity(template);
    }

    @Transactional
    public AiPromptTemplateDto createTemplate(AiPromptTemplateRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã Prompt Template không được để trống");
        }
        String cleanCode = request.getCode().trim().toUpperCase();

        if (templateRepository.existsByCodeAndDeletedFalse(cleanCode)) {
            throw new IllegalArgumentException("Mã Prompt Template '" + cleanCode + "' đã tồn tại trong hệ thống");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên Prompt Template không được để trống");
        }

        if (request.getTemplateContent() == null || request.getTemplateContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung Prompt không được để trống");
        }

        AiPromptTemplate template = AiPromptTemplate.builder()
                .code(cleanCode)
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .templateContent(request.getTemplateContent().trim())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        AiPromptTemplate saved = templateRepository.save(template);
        log.info("Đã tạo mới AI Prompt Template thành công: code={}, id={}", saved.getCode(), saved.getId());
        return AiPromptTemplateDto.fromEntity(saved);
    }

    @Transactional
    public AiPromptTemplateDto updateTemplate(Long id, AiPromptTemplateRequest request) {
        AiPromptTemplate template = templateRepository.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Prompt Template với ID: " + id));

        if (request.getCode() != null && !request.getCode().trim().isEmpty()) {
            String cleanCode = request.getCode().trim().toUpperCase();
            if (SYSTEM_DEFAULT_CODES.contains(template.getCode().toUpperCase()) && !cleanCode.equalsIgnoreCase(template.getCode())) {
                throw new IllegalArgumentException("Không thể thay đổi mã của Prompt Template mặc định hệ thống (" + template.getCode() + ")");
            }
            if (templateRepository.existsByCodeAndDeletedFalseAndIdNot(cleanCode, id)) {
                throw new IllegalArgumentException("Mã Prompt Template '" + cleanCode + "' đã được sử dụng ở bản ghi khác");
            }
            template.setCode(cleanCode);
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            template.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            template.setDescription(request.getDescription().trim());
        }

        if (request.getTemplateContent() != null && !request.getTemplateContent().trim().isEmpty()) {
            template.setTemplateContent(request.getTemplateContent().trim());
        }

        if (request.getActive() != null) {
            template.setActive(request.getActive());
        }

        AiPromptTemplate updated = templateRepository.save(template);
        log.info("Đã cập nhật AI Prompt Template: code={}, id={}", updated.getCode(), updated.getId());
        return AiPromptTemplateDto.fromEntity(updated);
    }

    @Transactional
    public AiPromptTemplateDto toggleActive(Long id) {
        AiPromptTemplate template = templateRepository.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Prompt Template với ID: " + id));

        template.setActive(!template.isActive());
        AiPromptTemplate updated = templateRepository.save(template);
        log.info("Đã thay đổi trạng thái AI Prompt Template id={}: active={}", id, updated.isActive());
        return AiPromptTemplateDto.fromEntity(updated);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        AiPromptTemplate template = templateRepository.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Prompt Template với ID: " + id));

        if (SYSTEM_DEFAULT_CODES.contains(template.getCode().toUpperCase())) {
            throw new IllegalArgumentException("Không thể xóa Prompt Template mặc định của hệ thống (" + template.getCode() + "). Bạn chỉ có thể chỉnh sửa hoặc tạm tắt.");
        }

        template.setDeleted(true);
        templateRepository.save(template);
        log.info("Đã xóa (soft delete) AI Prompt Template id={}", id);
    }
}
