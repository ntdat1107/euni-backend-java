package com.euni.backend.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyCampaignStepDto {
    private UUID id;
    private Integer stepIndex;
    private String stepName;
    private LocalDateTime deadline;
    private String requiredDocuments;
    private String configuration;
    private String status;
    private String resultData;
}
