package com.ept.sn.cri.backend.rh.service;


import com.ept.sn.cri.backend.entity.JobOffer;
import com.ept.sn.cri.backend.entity.RH;
import com.ept.sn.cri.backend.enums.JobStatus;
import com.ept.sn.cri.backend.exception.ResourceNotFoundException;
import com.ept.sn.cri.backend.exception.UnauthorizedActionException;
import com.ept.sn.cri.backend.rh.dto.CreateJobOfferDTO;
import com.ept.sn.cri.backend.rh.dto.JobOfferListDTO;
import com.ept.sn.cri.backend.rh.dto.JobOfferResponseDTO;
import com.ept.sn.cri.backend.rh.dto.UpdateJobOfferDTO;
import com.ept.sn.cri.backend.rh.repository.JobOfferRepository;
import com.ept.sn.cri.backend.rh.repository.RHRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobOfferService {

    private final JobOfferRepository jobOfferRepository;
    private final RHRepository rhRepository;

    /**
     * Créer une nouvelle offre d'emploi
     */
    @Transactional
    public JobOfferResponseDTO createJobOffer(CreateJobOfferDTO dto, Long rhId) {
        RH rh = rhRepository.findById(rhId)
                .orElseThrow(() -> new ResourceNotFoundException("RH non trouvé avec l'ID : " + rhId));

        JobOffer jobOffer = new JobOffer();
        jobOffer.setJobTitle(dto.getJobTitle());
        jobOffer.setJobType(dto.getJobType());
        jobOffer.setTypeContrat(dto.getTypeContrat());
        jobOffer.setDescription(dto.getDescription());
        jobOffer.setRequiredSkills(dto.getRequiredSkills());
        jobOffer.setNiveauEtudeRequis(dto.getNiveauEtudeRequis());
        jobOffer.setExperienceMin(dto.getExperienceMin());
        jobOffer.setDateLimite(dto.getDateLimite());
        jobOffer.setJobStatus(JobStatus.PUBLISHED);
        jobOffer.setCreatedBy(rh);

        JobOffer savedOffer = jobOfferRepository.save(jobOffer);
        return mapToResponseDTO(savedOffer);
    }

    /**
     * Modifier une offre d'emploi existante
     */
    @Transactional
    public JobOfferResponseDTO updateJobOffer(Long offerId, UpdateJobOfferDTO dto, Long rhId) {
        JobOffer jobOffer = jobOfferRepository.findByIdAndCreatedById(offerId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Offre non trouvée ou vous n'avez pas les droits pour la modifier"));

        if (dto.getJobTitle() != null) {
            jobOffer.setJobTitle(dto.getJobTitle());
        }
        if (dto.getJobType() != null) {
            jobOffer.setJobType(dto.getJobType());
        }
        if (dto.getTypeContrat() != null) {
            jobOffer.setTypeContrat(dto.getTypeContrat());
        }
        if (dto.getJobStatus() != null) {
            jobOffer.setJobStatus(dto.getJobStatus());
        }
        if (dto.getDescription() != null) {
            jobOffer.setDescription(dto.getDescription());
        }
        if (dto.getRequiredSkills() != null) {
            jobOffer.setRequiredSkills(dto.getRequiredSkills());
        }
        if (dto.getNiveauEtudeRequis() != null) {
            jobOffer.setNiveauEtudeRequis(dto.getNiveauEtudeRequis());
        }
        if (dto.getExperienceMin() != null) {
            jobOffer.setExperienceMin(dto.getExperienceMin());
        }
        if (dto.getDateLimite() != null) {
            jobOffer.setDateLimite(dto.getDateLimite());
        }

        JobOffer updatedOffer = jobOfferRepository.save(jobOffer);
        return mapToResponseDTO(updatedOffer);
    }

    /**
     * Supprimer une offre d'emploi
     */
    @Transactional
    public void deleteJobOffer(Long offerId, Long rhId) {
        JobOffer jobOffer = jobOfferRepository.findByIdAndCreatedById(offerId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Offre non trouvée ou vous n'avez pas les droits pour la supprimer"));

        jobOfferRepository.delete(jobOffer);
    }


    /**
     * Obtenir toutes les offres d'un RH, avec filtre optionnel par statut
     */
    @Transactional(readOnly = true)
    public List<JobOfferListDTO> getAllJobOffers(Long rhId, JobStatus status) {
        List<JobOffer> offers;

        if (status != null) {
            offers = jobOfferRepository.findByCreatedByIdAndJobStatusOrderByDatePublicationDesc(rhId, status);
        } else {
            offers = jobOfferRepository.findByCreatedByIdOrderByDatePublicationDesc(rhId);
        }

        return offers.stream()
                .map(this::mapToListDTO)
                .collect(Collectors.toList());
    }


    /**
     * Obtenir les détails d'une offre spécifique
     */
    @Transactional(readOnly = true)
    public JobOfferResponseDTO getJobOfferById(Long offerId, Long rhId) {
        JobOffer jobOffer = jobOfferRepository.findByIdAndCreatedById(offerId, rhId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée ou vous n'avez pas les droits pour y accéder"));

        return mapToResponseDTO(jobOffer);
    }


    // Méthodes de mapping privées
    private JobOfferResponseDTO mapToResponseDTO(JobOffer jobOffer) {
        return JobOfferResponseDTO.builder()
                .id(jobOffer.getId())
                .jobTitle(jobOffer.getJobTitle())
                .jobType(jobOffer.getJobType())
                .typeContrat(jobOffer.getTypeContrat())
                .jobStatus(jobOffer.getJobStatus())
                .description(jobOffer.getDescription())
                .requiredSkills(jobOffer.getRequiredSkills())
                .niveauEtudeRequis(jobOffer.getNiveauEtudeRequis())
                .experienceMin(jobOffer.getExperienceMin())
                .datePublication(jobOffer.getDatePublication())
                .lastModifiedDate(jobOffer.getLastModifiedDate())
                .dateLimite(jobOffer.getDateLimite())
                .createdById(jobOffer.getCreatedBy().getId())
                .createdByName(jobOffer.getCreatedBy().getFullName())
                .applicationCount(jobOffer.getApplications() != null ? jobOffer.getApplications().size() : 0)
                .build();
    }

    private JobOfferListDTO mapToListDTO(JobOffer jobOffer) {
        return JobOfferListDTO.builder()
                .id(jobOffer.getId())
                .jobTitle(jobOffer.getJobTitle())
                .jobType(jobOffer.getJobType())
                .typeContrat(jobOffer.getTypeContrat())
                .jobStatus(jobOffer.getJobStatus())
                .datePublication(jobOffer.getDatePublication())
                .dateLimite(jobOffer.getDateLimite())
                .applicationCount(jobOffer.getApplications() != null ? jobOffer.getApplications().size() : 0)
                .build();
    }

}
