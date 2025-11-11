package com.ept.sn.cri.backend.rh.repository;

import com.ept.sn.cri.backend.entity.Commission;
import com.ept.sn.cri.backend.enums.CommissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, Long> {

    // Trouver toutes les commissions créées par un RH spécifique
    List<Commission> findByCreatedByIdOrderByCreatedAtDesc(Long rhId);

    // Trouver les commissions d'un RH filtrées par statut
    List<Commission> findByCreatedByIdAndStatusOrderByCreatedAtDesc(Long rhId, CommissionStatus status);

    // Trouver une commission spécifique créée par un RH
    Optional<Commission> findByIdAndCreatedById(Long commissionId, Long rhId);

    // Vérifier si une commission appartient à un RH
    boolean existsByIdAndCreatedById(Long commissionId, Long rhId);

    // Trouver la commission associée à une offre d'emploi
    Optional<Commission> findByJobOfferId(Long jobOfferId);

    // Vérifier si une offre a déjà une commission
    boolean existsByJobOfferId(Long jobOfferId);

    // Compter les commissions actives d'un RH
    @Query("SELECT COUNT(c) FROM Commission c WHERE c.createdBy.id = :rhId AND c.status = :status")
    Long countByRhAndStatus(@Param("rhId") Long rhId, @Param("status") CommissionStatus status);
}