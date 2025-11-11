package com.ept.sn.cri.backend.entity;

import com.ept.sn.cri.backend.enums.JobStatus;
import com.ept.sn.cri.backend.enums.ContratType;
import com.ept.sn.cri.backend.enums.JobType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "job_offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class JobOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre du poste est obligatoire.")
    private String jobTitle;

    @NotNull(message = "Le type de poste est obligatoire.")
    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @NotNull(message = "Le type de contrat est obligatoire.")
    @Enumerated(EnumType.STRING)
    private ContratType typeContrat;

    @NotNull(message = "Le statut de l'offre est obligatoire.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus jobStatus =JobStatus.PUBLISHED;

    @NotBlank(message = "La description est obligatoire.")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Les compétences requises sont obligatoires.")
    @Column(columnDefinition = "TEXT")
    private String requiredSkills;

    @NotBlank(message = "Le niveau d'étude requis est obligatoire.")
    private String niveauEtudeRequis;

    @NotNull(message = "L'expérience minimale est obligatoire.")
    @Min(value = 0, message = "L'expérience minimale ne peut pas être négative.")
    private Integer experienceMin;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date datePublication;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Future(message = "La date limite doit être dans le futur.")
    @Temporal(TemporalType.DATE)
    private Date dateLimite;


    @OneToMany(mappedBy = "jobOffer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Application> applications = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false)
    private RH createdBy;


}
