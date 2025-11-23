package com.ept.sn.cri.backend.rh.service;


import com.ept.sn.cri.backend.auth.repository.UserRepository;
import com.ept.sn.cri.backend.entity.*;
import com.ept.sn.cri.backend.enums.CommissionRole;
import com.ept.sn.cri.backend.enums.CommissionStatus;
import com.ept.sn.cri.backend.exception.InvalidActionException;
import com.ept.sn.cri.backend.exception.ResourceNotFoundException;
import com.ept.sn.cri.backend.exception.UnauthorizedActionException;
import com.ept.sn.cri.backend.rh.dto.*;
import com.ept.sn.cri.backend.rh.repository.CommissionMemberRepository;
import com.ept.sn.cri.backend.rh.repository.CommissionRepository;
import com.ept.sn.cri.backend.rh.repository.JobOfferRepository;
import com.ept.sn.cri.backend.rh.repository.RHRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommissionService {

    private final CommissionRepository commissionRepository;
    private final CommissionMemberRepository commissionMemberRepository;
    private final JobOfferRepository jobOfferRepository;
    private final UserRepository userRepository;
    private final RHRepository rhRepository;

    /**
     * Créer une nouvelle commission de recrutement
     */
    @Transactional
    public CommissionResponseDTO createCommission(CreateCommissionDTO dto, Long rhId) {
        // Vérifier que le RH existe
        RH rh = rhRepository.findById(rhId)
                .orElseThrow(() -> new RuntimeException("RH non trouvé avec l'ID : " + rhId));

        // Vérifier que l'offre d'emploi existe et appartient au RH
        JobOffer jobOffer = jobOfferRepository.findByIdAndCreatedById(dto.getJobOfferId(), rhId)
                .orElseThrow(() -> new RuntimeException("Offre d'emploi non trouvée ou vous n'avez pas les droits"));

        // Vérifier qu'il n'y a pas déjà une commission pour cette offre
        if (commissionRepository.existsByJobOfferId(dto.getJobOfferId())) {
            throw new RuntimeException("Cette offre d'emploi a déjà une commission associée");
        }

        // Créer la commission
        Commission commission = new Commission();
        commission.setName(dto.getName());
        commission.setDescription(dto.getDescription());
        commission.setStatus(CommissionStatus.ACTIVE);
        commission.setCreatedBy(rh);
        commission.setJobOffer(jobOffer);

        Commission savedCommission = commissionRepository.save(commission);
        return mapToResponseDTO(savedCommission);
    }

    /**
     * Retourner la liste des users qui sont des commission_member
     */

    public List<CommissionMemberUserDTO> getAllCommissionMembersUsers() {
        return userRepository.findAllCommissionMembers()
                .stream()
                .map(this::mapToCommissionMemberUserDTO)
                .toList();
    }

    private CommissionMemberUserDTO mapToCommissionMemberUserDTO(User user) {
        CommissionMemberUserDTO dto = new CommissionMemberUserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    /**
     * Ajouter un membre à la commission
     */
    @Transactional
    public CommissionMemberResponseDTO addMemberToCommission(Long commissionId, AddCommissionMemberDTO dto, Long rhId) {
        // Vérifier que la commission existe et appartient au RH
        Commission commission = commissionRepository.findByIdAndCreatedById(commissionId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Commission non trouvée ou vous n'avez pas les droits"));

        // Vérifier que l'utilisateur existe
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID : " + dto.getUserId()));

        // Vérifier que l'utilisateur n'est pas déjà membre de cette commission
        if (commissionMemberRepository.existsByUserIdAndCommissionId(user.getId(), commissionId)) {
            throw new InvalidActionException("Cet utilisateur est déjà membre de la commission");
        }


        CommissionRole role;
        try {
            role = CommissionRole.valueOf(dto.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidActionException("Rôle invalide. Utilisez 'PRESIDENT' ou 'MEMBER'");
        }

        if (role == CommissionRole.PRESIDENT && commissionMemberRepository.hasPresident(commissionId)) {
            throw new InvalidActionException("Cette commission a déjà un président");
        }

        CommissionMember member;
        if (user instanceof CommissionMember) {
            member = (CommissionMember) user;
        } else {
            member = new CommissionMember();
            member.setId(user.getId());  // pour ne pas dupliquer
            member.setFirstName(user.getFirstName());
            member.setLastName(user.getLastName());
            member.setEmail(user.getEmail());
            member.setPassword(user.getPassword());
            member.setRole(user.getRole());
        }

        member.setCommission(commission);
        member.setCommissionRole(role);
        member.setExpertiseArea(dto.getExpertiseArea());

        CommissionMember savedMember = commissionMemberRepository.save(member);
        return mapToMemberResponseDTO(savedMember);
    }


    /**
     * Changer le président de la commission
     */
    @Transactional
    public CommissionMemberResponseDTO changePresident(Long commissionId, Long newPresidentMemberId, Long rhId) {
        // Vérifier que la commission existe et appartient au RH
        Commission commission = commissionRepository.findByIdAndCreatedById(commissionId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Commission non trouvée ou vous n'avez pas les droits"));

        // Vérifier que le nouveau président est bien un membre de la commission
        CommissionMember newPresident = commissionMemberRepository.findByIdAndCommissionId(newPresidentMemberId, commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre non trouvé dans cette commission"));

        // Rétrograder l'ancien président s'il existe
        commissionMemberRepository.findByCommissionIdAndRole(commissionId, CommissionRole.PRESIDENT)
                .ifPresent(oldPresident -> {
                    oldPresident.setCommissionRole(CommissionRole.MEMBER);
                    commissionMemberRepository.save(oldPresident);
                });

        // Promouvoir le nouveau président
        newPresident.setCommissionRole(CommissionRole.PRESIDENT);
        CommissionMember savedPresident = commissionMemberRepository.save(newPresident);

        // Mapping sans getUser(), car CommissionMember hérite de User
        return CommissionMemberResponseDTO.builder()
                .id(savedPresident.getId())
                .userId(savedPresident.getId())
                .userName(savedPresident.getFullName())
                .userEmail(savedPresident.getEmail())
                .role(savedPresident.getRole().name())
                .expertiseArea(savedPresident.getExpertiseArea())
                .build();
    }


    /**
     * Retirer un membre de la commission
     */
    @Transactional
    public void removeMemberFromCommission(Long commissionId, Long memberId, Long rhId) {
        // Vérifier que la commission existe et appartient au RH
        Commission commission = commissionRepository.findByIdAndCreatedById(commissionId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Commission non trouvée ou vous n'avez pas les droits"));

        // Vérifier que le membre existe
        CommissionMember member = commissionMemberRepository.findByIdAndCommissionId(memberId, commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre non trouvé dans cette commission"));

        commissionMemberRepository.delete(member);
    }

    /**
     * Modifier une commission
     */
    @Transactional
    public CommissionResponseDTO updateCommission(Long commissionId, UpdateCommissionDTO dto, Long rhId) {
        Commission commission = commissionRepository.findByIdAndCreatedById(commissionId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Commission non trouvée ou vous n'avez pas les droits"));

        if (dto.getName() != null) {
            commission.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            commission.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            commission.setStatus(dto.getStatus());
        }

        Commission updatedCommission = commissionRepository.save(commission);
        return mapToResponseDTO(updatedCommission);
    }

    /**
     * Supprimer une commission
     */
    @Transactional
    public void deleteCommission(Long commissionId, Long rhId) {
        Commission commission = commissionRepository.findByIdAndCreatedById(commissionId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Commission non trouvée ou vous n'avez pas les droits"));

        commissionRepository.delete(commission);
    }

    /**
     * Obtenir toutes les commissions d'un RH
     */
    @Transactional(readOnly = true)
    public List<CommissionListDTO> getAllCommissions(Long rhId, CommissionStatus status) {
        List<Commission> commissions;

        if (status != null) {
            commissions = commissionRepository.findByCreatedByIdAndStatusOrderByCreatedAtDesc(rhId, status);
        } else {
            commissions = commissionRepository.findByCreatedByIdOrderByCreatedAtDesc(rhId);
        }

        return commissions.stream()
                .map(this::mapToListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les détails d'une commission
     */
    @Transactional(readOnly = true)
    public CommissionResponseDTO getCommissionById(Long commissionId, Long rhId) {
        Commission commission = commissionRepository.findByIdAndCreatedById(commissionId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Commission non trouvée ou vous n'avez pas les droits"));

        return mapToResponseDTO(commission);
    }

    /**
     * Obtenir tous les membres d'une commission
     */
    @Transactional(readOnly = true)
    public List<CommissionMemberResponseDTO> getCommissionMembers(Long commissionId, Long rhId) {
        // Vérifier que la commission appartient au RH
        commissionRepository.findByIdAndCreatedById(commissionId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Commission non trouvée ou vous n'avez pas les droits"));

        List<CommissionMember> members = commissionMemberRepository.findByCommissionId(commissionId);

        return members.stream()
                .map(this::mapToMemberResponseDTO)
                .collect(Collectors.toList());
    }

    // Méthodes de mapping privées
    private CommissionResponseDTO mapToResponseDTO(Commission commission) {
        List<CommissionMemberResponseDTO> members = commission.getMembers().stream()
                .map(this::mapToMemberResponseDTO)
                .collect(Collectors.toList());

        CommissionMemberResponseDTO president = commission.getMembers().stream()
                .filter(m -> m.getCommissionRole() == CommissionRole.PRESIDENT)
                .findFirst()
                .map(this::mapToMemberResponseDTO)
                .orElse(null);

        return CommissionResponseDTO.builder()
                .id(commission.getId())
                .name(commission.getName())
                .description(commission.getDescription())
                .status(commission.getStatus())
                .createdAt(commission.getCreatedAt())
                .updatedAt(commission.getUpdatedAt())
                .createdById(commission.getCreatedBy().getId())
                .createdByName(commission.getCreatedBy().getFullName())
                .jobOfferId(commission.getJobOffer().getId())
                .jobOfferTitle(commission.getJobOffer().getJobTitle())
                .members(members)
                .memberCount(members.size())
                .president(president)
                .build();
    }

    private CommissionListDTO mapToListDTO(Commission commission) {
        String presidentName = commission.getMembers().stream()
                .filter(m -> m.getCommissionRole() == CommissionRole.PRESIDENT)
                .findFirst()
                .map(CommissionMember::getFullName)
                .orElse(null);
        return CommissionListDTO.builder()
                .id(commission.getId())
                .name(commission.getName())
                .status(commission.getStatus())
                .createdAt(commission.getCreatedAt())
                .jobOfferId(commission.getJobOffer().getId())
                .jobOfferTitle(commission.getJobOffer().getJobTitle())
                .memberCount(commission.getMembers().size())
                .presidentName(presidentName)
                .build();
    }

    private CommissionMemberResponseDTO mapToMemberResponseDTO(CommissionMember member) {
        return CommissionMemberResponseDTO.builder()
                .id(member.getId())
                .userId(member.getId())
                .userName(member.getFullName())
                .userEmail(member.getEmail())
                .role(member.getRole().name())
                .expertiseArea(member.getExpertiseArea())
                .build();
    }



}
