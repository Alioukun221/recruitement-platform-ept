package com.ept.sn.cri.backend.candidat.repository;

import com.ept.sn.cri.backend.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateApplicationRepository extends JpaRepository<Application, Long> {

    // Trouver toutes les candidatures d'un candidat (triées par date de soumission DESC)
    List<Application> findByCandidateIdOrderBySubmitDateDesc(Long candidateId);

    // Vérifier si un candidat a déjà postulé une offre
    boolean existsByCandidateIdAndJobOfferId(Long candidateId, Long jobOfferId);

    // Trouver une candidature spécifique d'un candidat
    Optional<Application> findByIdAndCandidateId(Long applicationId, Long candidateId);

    // Compter le nombre de candidatures d'un candidat
    Long countByCandidateId(Long candidateId);

    // Trouver les candidatures d'un candidat pour une offre spécifique
    @Query("SELECT a FROM Application a WHERE a.candidate.id = :candidateId AND a.jobOffer.id = :jobOfferId")
    Optional<Application> findByCandidateAndJobOffer(@Param("candidateId") Long candidateId, @Param("jobOfferId") Long jobOfferId);
}