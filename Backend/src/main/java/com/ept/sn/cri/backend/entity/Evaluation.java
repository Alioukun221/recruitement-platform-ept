package com.ept.sn.cri.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "commission_member_id", nullable = false)
    private CommissionMember commissionMember;


    @ManyToOne
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;


    private Integer competenceScore;
    private Integer experienceScore;
    private Integer diplomaScore;
    private Integer motivationScore;
    private Integer softSkillsScore;

    @Column(length = 2000)
    private String comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}
