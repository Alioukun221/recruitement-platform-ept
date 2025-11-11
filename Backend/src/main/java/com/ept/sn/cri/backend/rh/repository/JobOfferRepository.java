package com.ept.sn.cri.backend.rh.repository;

import com.ept.sn.cri.backend.entity.JobOffer;
import com.ept.sn.cri.backend.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {

    // Trouver toutes les offres créées par un RH spécifique, triées par date de création (DESC)
    List<JobOffer> findByCreatedByIdOrderByDatePublicationDesc(Long rhId);

    // Trouver les offres d'un RH filtrées par statut, triées par date de création (DESC)
    List<JobOffer> findByCreatedByIdAndJobStatusOrderByDatePublicationDesc(Long rhId, JobStatus jobStatus);

    // Trouver une offre spécifique créée par un RH
    Optional<JobOffer> findByIdAndCreatedById(Long offerId, Long rhId);

    // Vérifier si une offre appartient à un RH
    boolean existsByIdAndCreatedById(Long offerId, Long rhId);

    // Compter les offres d'un RH par statut
    @Query("SELECT COUNT(j) FROM JobOffer j WHERE j.createdBy.id = :rhId AND j.jobStatus = :status")
    Long countByRhAndStatus(@Param("rhId") Long rhId, @Param("status") JobStatus status);
}