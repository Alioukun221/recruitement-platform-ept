package com.ept.sn.cri.backend.commission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationResponseDTO {

    private Long id;
    private Long evaluatorId;
    private String evaluatorName;
    private String evaluatorRole; // PRESIDENT ou MEMBER
    private Integer competenceScore;
    private Integer experienceScore;
    private Integer diplomaScore;
    private Integer motivationScore;
    private Integer softSkillsScore;
    private Double totalScore; // Moyenne des 5 scores
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}