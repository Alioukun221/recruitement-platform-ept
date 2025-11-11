package com.ept.sn.cri.backend.candidat.repository;

import com.ept.sn.cri.backend.entity.JobOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PublicJobOfferRepository extends JpaRepository<JobOffer, Long> {

    // Trouver toutes les offres publiées dont la date limite n'est pas expirée
    @Query("SELECT j FROM JobOffer j WHERE j.jobStatus = 'PUBLISHED' AND (j.dateLimite IS NULL OR j.dateLimite >= :currentDate) ORDER BY j.datePublication DESC")
    List<JobOffer> findAvailableJobOffers(@Param("currentDate") Date currentDate);

    // Trouver une offre publiée par son ID
    @Query("SELECT j FROM JobOffer j WHERE j.id = :id AND j.jobStatus = 'PUBLISHED'")
    JobOffer findPublishedJobOfferById(@Param("id") Long id);

    // Rechercher par mots-clés dans le titre ou la description
    @Query("SELECT j FROM JobOffer j WHERE j.jobStatus = 'PUBLISHED' AND (j.dateLimite IS NULL OR j.dateLimite >= :currentDate) AND (LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY j.datePublication DESC")
    List<JobOffer> searchByKeyword(@Param("keyword") String keyword, @Param("currentDate") Date currentDate);
}