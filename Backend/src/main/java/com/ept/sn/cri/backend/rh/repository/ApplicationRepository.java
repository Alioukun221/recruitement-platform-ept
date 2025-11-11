package com.ept.sn.cri.backend.rh.repository;

import com.ept.sn.cri.backend.entity.Application;
import com.ept.sn.cri.backend.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // Trouver toutes les candidatures d'une offre d'emploi (triées par score IA décroissant)
    @Query("SELECT a FROM Application a WHERE a.jobOffer.id = :jobOfferId AND a.jobOffer.createdBy.id = :rhId ORDER BY a.scoreIA DESC NULLS LAST, a.submitDate DESC")
    List<Application> findByJobOfferIdAndRhId(@Param("jobOfferId") Long jobOfferId, @Param("rhId") Long rhId);

    // Trouver les candidatures d'une offre filtrées par statut
    @Query("SELECT a FROM Application a WHERE a.jobOffer.id = :jobOfferId AND a.jobOffer.createdBy.id = :rhId AND a.applicationStatus = :status ORDER BY a.scoreIA DESC NULLS LAST, a.submitDate DESC")
    List<Application> findByJobOfferIdAndRhIdAndStatus(@Param("jobOfferId") Long jobOfferId, @Param("rhId") Long rhId, @Param("status") ApplicationStatus status);

    // Trouver une candidature spécifique (vérifier que le RH a accès)
    @Query("SELECT a FROM Application a WHERE a.id = :applicationId AND a.jobOffer.createdBy.id = :rhId")
    Optional<Application> findByIdAndRhId(@Param("applicationId") Long applicationId, @Param("rhId") Long rhId);

    // Trouver les candidatures avec un score supérieur à un seuil
    @Query("SELECT a FROM Application a WHERE a.jobOffer.id = :jobOfferId AND a.jobOffer.createdBy.id = :rhId AND a.scoreIA >= :minScore ORDER BY a.scoreIA DESC")
    List<Application> findByJobOfferIdAndMinScore(@Param("jobOfferId") Long jobOfferId, @Param("rhId") Long rhId, @Param("minScore") Integer minScore);

    // Compter les candidatures par statut pour une offre
    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobOffer.id = :jobOfferId AND a.jobOffer.createdBy.id = :rhId AND a.applicationStatus = :status")
    Long countByJobOfferIdAndStatus(@Param("jobOfferId") Long jobOfferId, @Param("rhId") Long rhId, @Param("status") ApplicationStatus status);

    // Trouver toutes les candidatures présélectionnées d'une offre
    @Query("SELECT a FROM Application a WHERE a.jobOffer.id = :jobOfferId AND a.jobOffer.createdBy.id = :rhId AND a.applicationStatus = 'SHORTLISTED' ORDER BY a.scoreIA DESC")
    List<Application> findShortlistedByJobOfferId(@Param("jobOfferId") Long jobOfferId, @Param("rhId") Long rhId);

    // Statistiques des candidatures par offre
    @Query("SELECT " +
            "COUNT(a), " +
            "AVG(a.scoreIA), " +
            "MAX(a.scoreIA), " +
            "MIN(a.scoreIA) " +
            "FROM Application a WHERE a.jobOffer.id = :jobOfferId AND a.jobOffer.createdBy.id = :rhId")
    Object[] getApplicationStatsByJobOfferId(@Param("jobOfferId") Long jobOfferId, @Param("rhId") Long rhId);
}