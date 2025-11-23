package com.ept.sn.cri.backend.rh.repository;

import com.ept.sn.cri.backend.entity.CommissionMember;
import com.ept.sn.cri.backend.entity.User;
import com.ept.sn.cri.backend.enums.CommissionRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionMemberRepository extends JpaRepository<CommissionMember, Long> {

    // Trouver tous les membres d'une commission
    List<CommissionMember> findByCommissionId(Long commissionId);




    // Trouver le président d'une commission
    Optional<CommissionMember> findByCommissionIdAndRole(Long commissionId, CommissionRole role);

    // Vérifier si un utilisateur est déjà membre d'une commission
    boolean existsByCommissionIdAndId(Long commissionId, Long id);

    // Trouver un membre spécifique
    Optional<CommissionMember> findByIdAndCommissionId(Long memberId, Long commissionId);

    // Compter les membres d'une commission
    Long countByCommissionId(Long commissionId);

    // Vérifier si une commission a un président
    @Query("SELECT CASE WHEN COUNT(cm) > 0 THEN true ELSE false END FROM CommissionMember cm WHERE cm.commission.id = :commissionId AND cm.role = 'PRESIDENT'")
    boolean hasPresident(@Param("commissionId") Long commissionId);

    // Trouver les commissions dont un utilisateur est membre
    @Query("SELECT cm FROM CommissionMember cm JOIN FETCH cm.commission WHERE cm.id = :userId")
    List<CommissionMember> findByUserId(@Param("userId") Long userId);

    // Trouver un membre spécifique par userId et commissionId
    @Query("SELECT cm FROM CommissionMember cm WHERE cm.id = :userId AND cm.commission.id = :commissionId")
    Optional<CommissionMember> findByUserIdAndCommissionId(@Param("userId") Long userId, @Param("commissionId") Long commissionId);

    // Vérifier si un user est membre d'une commission
    @Query("SELECT CASE WHEN COUNT(cm) > 0 THEN true ELSE false END FROM CommissionMember cm WHERE cm.id = :userId AND cm.commission.id = :commissionId")
    boolean existsByUserIdAndCommissionId(@Param("userId") Long userId, @Param("commissionId") Long commissionId);
}
