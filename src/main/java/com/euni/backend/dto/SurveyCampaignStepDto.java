package com.euni.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyCampaignStepDto {
    private Long id;
    private Integer stepIndex;
    private String stepName;
    private LocalDateTime deadline;
    private String requiredDocuments;
    private String configuration;
    private String status;
    private String resultData;
}
