package com.ept.sn.cri.backend.commission.service;

import com.ept.sn.cri.backend.commission.dto.*;
import com.ept.sn.cri.backend.commission.repository.CommissionApplicationRepository;
import com.ept.sn.cri.backend.commission.repository.EvaluationRepository;
import com.ept.sn.cri.backend.entity.Application;
import com.ept.sn.cri.backend.entity.CommissionMember;
import com.ept.sn.cri.backend.entity.Evaluation;
import com.ept.sn.cri.backend.rh.repository.CommissionMemberRepository;
import com.ept.sn.cri.backend.rh.repository.CommissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
            throw new RuntimeException("Vous n'êtes pas membre de cette commission");
        }

        List<Application> applications = commissionApplicationRepository
                .findShortlistedApplicationsByCommissionId(commissionId);

        CommissionMember member = commissionMemberRepository.findByUserIdAndCommissionId(userId, commissionId)
                .orElseThrow(() -> new RuntimeException("Membre de commission non trouvé"));

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
            throw new RuntimeException("Vous n'êtes pas membre de cette commission");
        }

        // Vérifier que la candidature appartient à cette commission et est présélectionnée
        Application application = commissionApplicationRepository
                .findShortlistedApplicationByIdAndCommissionId(applicationId, commissionId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée ou non accessible"));

        CommissionMember member = commissionMemberRepository.findByUserIdAndCommissionId(userId, commissionId)
                .orElseThrow(() -> new RuntimeException("Membre de commission non trouvé"));

        return mapToDetailDTO(application, member.getId());
    }

    /**
     * Évaluer un candidat
     */
    @Transactional
    public EvaluationResponseDTO evaluateApplication(Long applicationId, Long commissionId, CreateEvaluationDTO dto, Long userId) {
        // Vérifier que l'utilisateur est membre de cette commission
        if (!isMemberOfCommission(userId, commissionId)) {
            throw new RuntimeException("Vous n'êtes pas membre de cette commission");
        }

        // Vérifier que la candidature appartient à cette commission et est présélectionnée
        Application application = commissionApplicationRepository
                .findShortlistedApplicationByIdAndCommissionId(applicationId, commissionId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée ou non accessible"));

        CommissionMember member = commissionMemberRepository.findByUserIdAndCommissionId(userId, commissionId)
                .orElseThrow(() -> new RuntimeException("Membre de commission non trouvé"));

        // Vérifier si le membre a déjà évalué cette candidature
        boolean alreadyEvaluated = evaluationRepository.existsByCommissionMemberIdAndApplicationId(member.getId(), applicationId);

        Evaluation evaluation;
        if (alreadyEvaluated) {
            // Mettre à jour l'évaluation existante
            evaluation = evaluationRepository.findByCommissionMemberIdAndApplicationId(member.getId(), applicationId)
                    .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));
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
            throw new RuntimeException("Vous n'êtes pas membre de cette commission");
        }

        CommissionMember member = commissionMemberRepository.findByUserIdAndCommissionId(userId, commissionId)
                .orElseThrow(() -> new RuntimeException("Membre de commission non trouvé"));

        Evaluation evaluation = evaluationRepository.findByCommissionMemberIdAndApplicationId(member.getId(), applicationId)
                .orElseThrow(() -> new RuntimeException("Vous n'avez pas encore évalué ce candidat"));

        return mapToEvaluationResponseDTO(evaluation);
    }

    /**
     * Supprimer mon évaluation
     */
    @Transactional
    public void deleteMyEvaluation(Long applicationId, Long commissionId, Long userId) {
        if (!isMemberOfCommission(userId, commissionId)) {
            throw new RuntimeException("Vous n'êtes pas membre de cette commission");
        }

        CommissionMember member = commissionMemberRepository.findByUserIdAndCommissionId(userId, commissionId)
                .orElseThrow(() -> new RuntimeException("Membre de commission non trouvé"));

        Evaluation evaluation = evaluationRepository.findByCommissionMemberIdAndApplicationId(member.getId(), applicationId)
                .orElseThrow(() -> new RuntimeException("Vous n'avez pas encore évalué ce candidat"));

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
        if (averages[0] != null) {
            double sum = ((Number) averages[0]).doubleValue() +
                    ((Number) averages[1]).doubleValue() +
                    ((Number) averages[2]).doubleValue() +
                    ((Number) averages[3]).doubleValue() +
                    ((Number) averages[4]).doubleValue();
            averageScore = sum / 5.0;
        }

        return CommissionApplicationListDTO.builder()
                .id(application.getId())
                .candidateName(application.getFirstName() + " " + application.getLastName())
                .candidateEmail(application.getEmail())
                .highestDegree(application.getHighestDegree())
                .specialization(application.getSpecialization())
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

        return CommissionApplicationDetailDTO.builder()
                .id(application.getId())
                .firstName(application.getFirstName())
                .lastName(application.getLastName())
                .email(application.getEmail())
                .address(application.getAddress())
                .highestDegree(application.getHighestDegree())
                .specialization(application.getSpecialization())
                .cvUrl(application.getCvUrl())
                .scoreIA(application.getScoreIA())
                .matchingCompetences(application.getMatchingCompetences())
                .matchingExperience(application.getMatchingExperience())
                .matchingDiploma(application.getMatchingDiploma())
                .justificationIA(application.getJustificationIA())
                .submitDate(application.getSubmitDate())
                .jobOfferId(application.getJobOffer().getId())
                .jobOfferTitle(application.getJobOffer().getJobTitle())
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

        return EvaluationResponseDTO.builder()
                .id(evaluation.getId())
                .evaluatorId(evaluation.getCommissionMember().getUser().getId())
                .evaluatorName(evaluation.getCommissionMember().getUser().getFullName())
                .evaluatorRole(evaluation.getCommissionMember().getRole().name())
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

        if (averages[0] == null) {
            return null;
        }

        Double avgCompetence = ((Number) averages[0]).doubleValue();
        Double avgExperience = ((Number) averages[1]).doubleValue();
        Double avgDiploma = ((Number) averages[2]).doubleValue();
        Double avgMotivation = ((Number) averages[3]).doubleValue();
        Double avgSoftSkills = ((Number) averages[4]).doubleValue();
        Double overall = (avgCompetence + avgExperience + avgDiploma + avgMotivation + avgSoftSkills) / 5.0;

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
