package com.ept.sn.cri.backend.commission.repository;

import com.ept.sn.cri.backend.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    // Trouver toutes les évaluations d'une candidature
    List<Evaluation> findByApplicationId(Long applicationId);

    // Trouver toutes les évaluations d'un membre de commission
    List<Evaluation> findByCommissionMemberId(Long commissionMemberId);

    // Compter les évaluations d'un membre de commission
    Long countByCommissionMemberId(Long commissionMemberId);

    // Vérifier si un membre a déjà évalué une candidature
    boolean existsByCommissionMemberIdAndApplicationId(Long commissionMemberId, Long applicationId);

    // Trouver l'évaluation d'un membre pour une candidature spécifique
    Optional<Evaluation> findByCommissionMemberIdAndApplicationId(Long commissionMemberId, Long applicationId);

    // Compter les évaluations d'une candidature
    Long countByApplicationId(Long applicationId);

    // Calculer la moyenne des scores pour une candidature
    @Query("SELECT " +
            "AVG(e.competenceScore), " +
            "AVG(e.experienceScore), " +
            "AVG(e.diplomaScore), " +
            "AVG(e.motivationScore), " +
            "AVG(e.softSkillsScore) " +
            "FROM Evaluation e WHERE e.application.id = :applicationId")
    Object[] getAverageScoresByApplicationId(@Param("applicationId") Long applicationId);

    @Query("SELECT e FROM Evaluation e JOIN FETCH e.commissionMember cm " +
            "WHERE e.application.id = :applicationId")
    List<Evaluation> findByApplicationIdWithMembers(@Param("applicationId") Long applicationId);
}
