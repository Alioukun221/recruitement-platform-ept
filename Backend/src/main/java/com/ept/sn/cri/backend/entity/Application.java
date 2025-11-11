package com.ept.sn.cri.backend.entity;

import com.ept.sn.cri.backend.enums.ApplicationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "job_offer_id", nullable = false)
    @JsonIgnore
    private JobOffer jobOffer;


    @Column(length = 100, nullable = false)
    private String firstName;

    @Column(length = 100, nullable = false)
    private String lastName;

    @Column(length = 100, nullable = false)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(length = 255)
    private String cvUrl;

    @Column(length = 50)
    private String highestDegree;

    @Column(length = 255)
    private String specialization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus applicationStatus =  ApplicationStatus.SUBMITTED;


    @JsonIgnore
    private Integer scoreIA;

    @JsonIgnore
    private Integer matchingCompetences;

    @JsonIgnore
    private Integer matchingExperience;

    @JsonIgnore
    private Integer matchingDiploma;

    @JsonIgnore
    @Column(length = 2000)
    private String justificationIA;


    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime submitDate;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updateDate;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Evaluation> evaluations=new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;


}
