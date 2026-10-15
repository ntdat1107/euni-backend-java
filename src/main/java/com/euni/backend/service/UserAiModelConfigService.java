package com.euni.backend.service;

import com.euni.backend.dto.UserAiModelConfigDto;
import com.euni.backend.dto.request.UserAiModelConfigRequest;
import com.euni.backend.entity.User;
import com.euni.backend.entity.UserAiCredential;
import com.euni.backend.entity.UserAiModelConfig;
import com.euni.backend.repository.UserAiCredentialRepository;
import com.euni.backend.repository.UserAiModelConfigRepository;
import com.euni.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAiModelConfigService {

    private final UserAiModelConfigRepository modelConfigRepository;
    private final UserAiCredentialRepository credentialRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserAiModelConfigDto> getModelConfigs(String username) {
        User user = getUserByUsername(username);
        return modelConfigRepository.findByUserIdAndDeletedFalse(user.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public UserAiModelConfigDto saveOrUpdateModelConfig(String username, UserAiModelConfigRequest req) {
        User user = getUserByUsername(username);

        UserAiCredential credential = null;
        if (req.getCredentialId() != null) {
            credential = credentialRepository.findByIdAndUserId(req.getCredentialId(), user.getId())
                    .orElseThrow(() -> new RuntimeException("Cấu hình API Key không tồn tại"));
        }

        String normalizedTaskType = req.getTaskType().trim().toUpperCase();

        Optional<UserAiModelConfig> existingOpt = modelConfigRepository.findByUserIdAndTaskType(user.getId(), normalizedTaskType);
        UserAiModelConfig config;
        if (existingOpt.isPresent()) {
            config = existingOpt.get();
            config.setCredential(credential);
            config.setModelName(req.getModelName().trim());
            if (req.getTemperature() != null) config.setTemperature(req.getTemperature());
            if (req.getMaxTokens() != null) config.setMaxTokens(req.getMaxTokens());
            if (req.getActive() != null) config.setActive(req.getActive());
        } else {
            config = UserAiModelConfig.builder()
                    .user(user)
                    .credential(credential)
                    .taskType(normalizedTaskType)
                    .modelName(req.getModelName().trim())
                    .temperature(req.getTemperature() != null ? req.getTemperature() : 0.5)
                    .maxTokens(req.getMaxTokens() != null ? req.getMaxTokens() : 4096)
                    .active(req.getActive() != null ? req.getActive() : true)
                    .build();
        }

        UserAiModelConfig saved = modelConfigRepository.save(config);
        return toDto(saved);
    }

    @Transactional
    public void deleteModelConfig(String username, Long id) {
        User user = getUserByUsername(username);
        UserAiModelConfig config = modelConfigRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình mô hình"));
        config.setDeleted(true);
        modelConfigRepository.save(config);
    }

    @Transactional(readOnly = true)
    public Optional<UserAiModelConfig> getEffectiveConfig(Long userId, String taskType) {
        String normalizedTaskType = taskType != null ? taskType.trim().toUpperCase() : "DEFAULT";
        Optional<UserAiModelConfig> specific = modelConfigRepository.findByUserIdAndTaskType(userId, normalizedTaskType);
        if (specific.isPresent() && specific.get().isActive()) {
            return specific;
        }
        Optional<UserAiModelConfig> fallbackDefault = modelConfigRepository.findByUserIdAndTaskType(userId, "DEFAULT");
        if (fallbackDefault.isPresent() && fallbackDefault.get().isActive()) {
            return fallbackDefault;
        }
        return Optional.empty();
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại: " + username));
    }

    public UserAiModelConfigDto toDto(UserAiModelConfig c) {
        return UserAiModelConfigDto.builder()
                .id(c.getId())
                .credentialId(c.getCredential() != null ? c.getCredential().getId() : null)
                .credentialAlias(c.getCredential() != null ? c.getCredential().getKeyAlias() : "Mặc định hệ thống")
                .provider(c.getCredential() != null ? c.getCredential().getProvider() : null)
                .taskType(c.getTaskType())
                .modelName(c.getModelName())
                .temperature(c.getTemperature())
                .maxTokens(c.getMaxTokens())
                .active(c.isActive())
                .build();
    }
}
