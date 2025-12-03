package com.ept.sn.cri.backend.commission.service;

import com.ept.sn.cri.backend.commission.dto.*;
import com.ept.sn.cri.backend.commission.repository.CommissionApplicationRepository;
import com.ept.sn.cri.backend.commission.repository.EvaluationRepository;
import com.ept.sn.cri.backend.entity.Application;
import com.ept.sn.cri.backend.entity.CommissionMember;
import com.ept.sn.cri.backend.entity.Evaluation;
import com.ept.sn.cri.backend.exception.EvaluationNotFoundException;
import com.ept.sn.cri.backend.exception.ResourceNotFoundException;
import com.ept.sn.cri.backend.exception.UnauthorizedActionException;
import com.ept.sn.cri.backend.rh.repository.CommissionMemberRepository;
import com.ept.sn.cri.backend.rh.repository.CommissionRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.ResourceClosedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommissionMemberService {

    private final CommissionApplicationRepository commissionApplicationRepository;
    private final EvaluationRepository evaluationRepository;
    private final CommissionMemberRepository commissionMemberRepository;
    private final CommissionRepository commissionRepository;


    /**
     * Obtenir les commissions dont l'utilisateur est membre
     */
    @Transactional(readOnly = true)
    public List<MemberCommissionDTO> getMyCommissions(Long userId) {
        List<CommissionMember> memberships = commissionMemberRepository.findByUserId(userId);

        return memberships.stream()
                .map(cm -> {
                    Long shortlistedCount = (long) commissionApplicationRepository
                            .findShortlistedApplicationsByCommissionId(cm.getCommission().getId())
                            .size();

                    Long myEvaluationsCount = evaluationRepository.countByCommissionMemberId(cm.getId());

                    return MemberCommissionDTO.builder()
                            .commissionId(cm.getCommission().getId())
                            .commissionName(cm.getCommission().getName())
                            .jobOfferTitle(cm.getCommission().getJobOffer().getJobTitle())
                            .myRole(cm.getRole().name())
                            .shortlistedCandidatesCount(shortlistedCount.intValue())
                            .myEvaluationsCount(myEvaluationsCount.intValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les candidats présélectionnés pour une commission
     */
    @Transactional(readOnly = true)
    public List<CommissionApplicationListDTO> getShortlistedApplications(Long commissionId, Long userId) {
        // Vérifier que l'utilisateur est membre de cette commission
        if (!isMemberOfCommission(userId, commissionId)) {
            throw new UnauthorizedActionException("Vous n'êtes pas membre de cette commission");
        }

        List<Application> applications = commissionApplicationRepository
                .findShortlistedApplicationsByCommissionId(commissionId);

        CommissionMember member = commissionMemberRepository.findByUserIdAndCommissionId(userId, commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre de commission non trouvé"));

        return applications.stream()
                .map(app -> mapToListDTO(app, member.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les détails complets d'un candidat
     */
    @Transactional(readOnly = true)
    public CommissionApplicationDetailDTO getApplicationDetails(Long applicationId, Long commissionId, Long userId) {
        // Vérifier que l'utilisateur est membre de cette commission
        if (!isMemberOfCommission(userId, commissionId)) {
            throw new UnauthorizedActionException("Vous n'êtes pas membre de cette commission");
        }

        // Vérifier que la candidature appartient à cette commission et est présélectionnée
        Application application = commissionApplicationRepository
                .findShortlistedApplicationByIdAndCommissionId(applicationId, commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée ou non accessible"));

        CommissionMember member = commissionMemberRepository.findByUserIdAndCommissionId(userId, commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre de commission non trouvé"));

        return mapToDetailDTO(application, member.getId());
    }

    /**
     * Évaluer un candidat
     */
    @Transactional
    public EvaluationResponseDTO evaluateApplication(Long applicationId, Long commissionId, CreateEvaluationDTO dto, Long userId) {
        // Vérifier que l'utilisateur est membre de cette commission
        if (!isMemberOfCommission(userId, commissionId)) {
            throw new UnauthorizedActionException("Vous n'êtes pas membre de cette commission");
        }

        // Vérifier que la candidature appartient à cette commission et est présélectionnée
        Application application = commissionApplicationRepository
                .findShortlistedApplicationByIdAndCommissionId(applicationId, commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée ou non accessible"));

        CommissionMember member = commissionMemberRepository.findByUserIdAndCommissionId(userId, commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre de commission non trouvé"));

        // Vérifier si le membre a déjà évalué cette candidature
        boolean alreadyEvaluated = evaluationRepository.existsByCommissionMemberIdAndApplicationId(member.getId(), applicationId);

        Evaluation evaluation;
        if (alreadyEvaluated) {
            // Mettre à jour l'évaluation existante
            evaluation = evaluationRepository.findByCommissionMemberIdAndApplicationId(member.getId(), applicationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Évaluation non trouvée"));
            evaluation.setUpdatedAt(LocalDateTime.now());
        } else {
            // Créer une nouvelle évaluation
            evaluation = new Evaluation();
            evaluation.setCommissionMember(member);
            evaluation.setApplication(application);
            evaluation.setCreatedAt(LocalDateTime.now());
        }

        // Mettre à jour les scores
        evaluation.setCompetenceScore(dto.getCompetenceScore());
        evaluation.setExperienceScore(dto.getExperienceScore());
        evaluation.setDiplomaScore(dto.getDiplomaScore());
        evaluation.setMotivationScore(dto.getMotivationScore());
        evaluation.setSoftSkillsScore(dto.getSoftSkillsScore());
        evaluation.setComment(dto.getComment());

        Evaluation savedEvaluation = evaluationRepository.save(evaluation);
        return mapToEvaluationResponseDTO(savedEvaluation);
    }

    /**
     * Obtenir mon évaluation pour un candidat
     */
    @Transactional(readOnly = true)
    public EvaluationResponseDTO getMyEvaluation(Long applicationId, Long commissionId, Long userId) {
        if (!isMemberOfCommission(userId, commissionId)) {
            throw new UnauthorizedActionException("Vous n'êtes pas membre de cette commission");
        }

        CommissionMember member = commissionMemberRepository.findByUserIdAndCommissionId(userId, commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre de commission non trouvé"));

        Evaluation evaluation = evaluationRepository.findByCommissionMemberIdAndApplicationId(member.getId(), applicationId)
                .orElseThrow(() -> new EvaluationNotFoundException("Vous n'avez pas encore évalué ce candidat"));

        return mapToEvaluationResponseDTO(evaluation);
    }

    /**
     * Supprimer mon évaluation
     */
    @Transactional
    public void deleteMyEvaluation(Long applicationId, Long commissionId, Long userId) {
        if (!isMemberOfCommission(userId, commissionId)) {
            throw new UnauthorizedActionException("Vous n'êtes pas membre de cette commission");
        }

        CommissionMember member = commissionMemberRepository.findByUserIdAndCommissionId(userId, commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre de commission non trouvé"));

        Evaluation evaluation = evaluationRepository.findByCommissionMemberIdAndApplicationId(member.getId(), applicationId)
                .orElseThrow(() -> new EvaluationNotFoundException("Vous n'avez pas encore évalué ce candidat"));

        evaluationRepository.delete(evaluation);
    }

    // Méthodes privées
    private boolean isMemberOfCommission(Long userId, Long commissionId) {
        return commissionMemberRepository.existsByUserIdAndCommissionId(userId, commissionId);
    }

    private CommissionApplicationListDTO mapToListDTO(Application application, Long myMemberId) {
        Long evaluationCount = evaluationRepository.countByApplicationId(application.getId());
        boolean alreadyEvaluated = evaluationRepository.existsByCommissionMemberIdAndApplicationId(myMemberId, application.getId());

        // Calculer la moyenne des scores de commission
        Object[] averages = evaluationRepository.getAverageScoresByApplicationId(application.getId());
        Double averageScore = null;

        if (averages != null && averages.length == 5) {
            double sum = 0.0;
            int validScores = 0;

            // Parcourir chaque score et vérifier s'il n'est pas null
            for (Object avg : averages) {
                if (avg != null) {
                    sum += ((Number) avg).doubleValue();
                    validScores++;
                }
            }

            // Calculer la moyenne seulement s'il y a au moins un score valide
            if (validScores > 0) {
                averageScore = sum / validScores;
            }
        }

        return CommissionApplicationListDTO.builder()
                .id(application.getId())
                .candidateName(application.getFirstName() + " " + application.getLastName())
                .candidateEmail(application.getEmail())
                .highestDegree(application.getHighestDegree())
                .specialization(application.getMajorField())
                .scoreIA(application.getScoreIA())
                .submitDate(application.getSubmitDate())
                .cvUrl(application.getCvUrl())
                .evaluationCount(evaluationCount.intValue())
                .averageCommissionScore(averageScore)
                .alreadyEvaluatedByMe(alreadyEvaluated)
                .build();
    }
    private CommissionApplicationDetailDTO mapToDetailDTO(Application application, Long myMemberId) {
        List<Evaluation> evaluations = evaluationRepository.findByApplicationIdWithMembers(application.getId());

        List<EvaluationResponseDTO> evaluationDTOs = evaluations.stream()
                .map(this::mapToEvaluationResponseDTO)
                .collect(Collectors.toList());

        boolean alreadyEvaluated = evaluationRepository.existsByCommissionMemberIdAndApplicationId(myMemberId, application.getId());

        // Calculer les moyennes
        EvaluationAverageDTO averages = calculateAverages(application.getId());

        // Gérer le cas où jobOffer pourrait être null
        Long jobOfferId = null;
        String jobOfferTitle = null;
        if (application.getJobOffer() != null) {
            jobOfferId = application.getJobOffer().getId();
            jobOfferTitle = application.getJobOffer().getJobTitle();
        }

        return CommissionApplicationDetailDTO.builder()
                .id(application.getId())
                .firstName(application.getFirstName())
                .lastName(application.getLastName())
                .email(application.getEmail())
                .address(application.getAddress())
                .highestDegree(application.getHighestDegree())
                .specialization(application.getMajorField())
                .cvUrl(application.getCvUrl())
                .scoreIA(application.getScoreIA())
                .matchingCompetences(application.getMatchingCompetences())
                .matchingExperience(application.getMatchingExperience())
                .matchingDiploma(application.getMatchingDiploma())
                .justificationIA(application.getJustificationIA())
                .submitDate(application.getSubmitDate())
                .jobOfferId(jobOfferId)
                .jobOfferTitle(jobOfferTitle)
                .evaluations(evaluationDTOs)
                .evaluationCount(evaluations.size())
                .averageScores(averages)
                .alreadyEvaluatedByMe(alreadyEvaluated)
                .build();
    }
    private EvaluationResponseDTO mapToEvaluationResponseDTO(Evaluation evaluation) {
        double totalScore = (evaluation.getCompetenceScore() +
                evaluation.getExperienceScore() +
                evaluation.getDiplomaScore() +
                evaluation.getMotivationScore() +
                evaluation.getSoftSkillsScore()) / 5.0;

        // Gérer le cas où commissionMember pourrait être null
        Long evaluatorId = null;
        String evaluatorName = "Inconnu";
        String evaluatorRole = "UNKNOWN";

        if (evaluation.getCommissionMember() != null) {
            evaluatorId = evaluation.getCommissionMember().getId();
            evaluatorName = evaluation.getCommissionMember().getFullName();
            if (evaluation.getCommissionMember().getRole() != null) {
                evaluatorRole = evaluation.getCommissionMember().getRole().name();
            }
        }

        return EvaluationResponseDTO.builder()
                .id(evaluation.getId())
                .evaluatorId(evaluatorId)
                .evaluatorName(evaluatorName)
                .evaluatorRole(evaluatorRole)
                .competenceScore(evaluation.getCompetenceScore())
                .experienceScore(evaluation.getExperienceScore())
                .diplomaScore(evaluation.getDiplomaScore())
                .motivationScore(evaluation.getMotivationScore())
                .softSkillsScore(evaluation.getSoftSkillsScore())
                .totalScore(totalScore)
                .comment(evaluation.getComment())
                .createdAt(evaluation.getCreatedAt())
                .updatedAt(evaluation.getUpdatedAt())
                .build();
    }

    private EvaluationAverageDTO calculateAverages(Long applicationId) {
        Object[] averages = evaluationRepository.getAverageScoresByApplicationId(applicationId);

        // Vérifier que le tableau existe et a la bonne taille
        if (averages == null || averages.length != 5) {
            return null;
        }

        // Vérifier qu'au moins un score existe
        boolean hasAnyScore = Arrays.stream(averages).anyMatch(Objects::nonNull);
        if (!hasAnyScore) {
            return null;
        }

        // Convertir chaque score avec vérification null
        Double avgCompetence = averages[0] != null ? ((Number) averages[0]).doubleValue() : 0.0;
        Double avgExperience = averages[1] != null ? ((Number) averages[1]).doubleValue() : 0.0;
        Double avgDiploma = averages[2] != null ? ((Number) averages[2]).doubleValue() : 0.0;
        Double avgMotivation = averages[3] != null ? ((Number) averages[3]).doubleValue() : 0.0;
        Double avgSoftSkills = averages[4] != null ? ((Number) averages[4]).doubleValue() : 0.0;

        // Calculer la moyenne globale en ne comptant que les scores non-null
        double sum = 0.0;
        int count = 0;
        for (Object avg : averages) {
            if (avg != null) {
                sum += ((Number) avg).doubleValue();
                count++;
            }
        }
        Double overall = count > 0 ? sum / count : 0.0;

        return EvaluationAverageDTO.builder()
                .averageCompetenceScore(avgCompetence)
                .averageExperienceScore(avgExperience)
                .averageDiplomaScore(avgDiploma)
                .averageMotivationScore(avgMotivation)
                .averageSoftSkillsScore(avgSoftSkills)
                .overallAverage(overall)
                .build();
    }






















}
