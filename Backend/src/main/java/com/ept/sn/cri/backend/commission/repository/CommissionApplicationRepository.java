package com.ept.sn.cri.backend.commission.repository;

import com.ept.sn.cri.backend.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionApplicationRepository extends JpaRepository<Application, Long> {

    // Trouver les candidatures présélectionnées pour une commission spécifique
    @Query("SELECT a FROM Application a " +
            "WHERE a.jobOffer.id = (SELECT c.jobOffer.id FROM Commission c WHERE c.id = :commissionId) " +
            "AND a.applicationStatus = 'SHORTLISTED' " +
            "ORDER BY a.scoreIA DESC")
    List<Application> findShortlistedApplicationsByCommissionId(@Param("commissionId") Long commissionId);

    // Trouver une candidature présélectionnée spécifique pour une commission
    @Query("SELECT a FROM Application a " +
            "WHERE a.id = :applicationId " +
            "AND a.jobOffer.id = (SELECT c.jobOffer.id FROM Commission c WHERE c.id = :commissionId) " +
            "AND a.applicationStatus = 'SHORTLISTED'")
    Optional<Application> findShortlistedApplicationByIdAndCommissionId(
            @Param("applicationId") Long applicationId,
            @Param("commissionId") Long commissionId);

    // Vérifier qu'un membre de commission a accès à une candidature
    @Query("SELECT CASE WHEN COUNT(cm) > 0 THEN true ELSE false END " +
            "FROM CommissionMember cm " +
            "WHERE cm.user.id = :userId " +
            "AND cm.commission.jobOffer.id = (SELECT a.jobOffer.id FROM Application a WHERE a.id = :applicationId)")
    boolean canMemberAccessApplication(@Param("userId") Long userId, @Param("applicationId") Long applicationId);

    // Trouver les commissions dont un utilisateur est membre
    @Query("SELECT cm.commission.id FROM CommissionMember cm WHERE cm.user.id = :userId")
    List<Long> findCommissionIdsByUserId(@Param("userId") Long userId);
}