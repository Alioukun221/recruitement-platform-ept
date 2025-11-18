package com.ept.sn.cri.backend.rh.service;

import com.ept.sn.cri.backend.entity.Application;
import com.ept.sn.cri.backend.entity.JobOffer;
import com.ept.sn.cri.backend.enums.ApplicationStatus;
import com.ept.sn.cri.backend.exception.ApplicationNotBelongToOfferException;
import com.ept.sn.cri.backend.exception.ResourceNotFoundException;
import com.ept.sn.cri.backend.exception.UnauthorizedActionException;
import com.ept.sn.cri.backend.rh.dto.*;
import com.ept.sn.cri.backend.rh.repository.ApplicationRepository;
import com.ept.sn.cri.backend.rh.repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobOfferRepository jobOfferRepository;

    /**
     * Obtenir toutes les candidatures pour une offre d'emploi
     */
    @Transactional(readOnly = true)
    public List<ApplicationListDTO> getApplicationsByJobOffer(Long jobOfferId, Long rhId, ApplicationStatus status) {
        // Vérifier que l'offre appartient au RH
        jobOfferRepository.findByIdAndCreatedById(jobOfferId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Offre d'emploi non trouvée ou vous n'avez pas les droits"));

        List<Application> applications;

        if (status != null) {
            applications = applicationRepository.findByJobOfferIdAndRhIdAndStatus(jobOfferId, rhId, status);
        } else {
            applications = applicationRepository.findByJobOfferIdAndRhId(jobOfferId, rhId);
        }

        return applications.stream()
                .map(this::mapToListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les détails complets d'une candidature
     */
    @Transactional(readOnly = true)
    public ApplicationDetailDTO getApplicationDetails(Long applicationId, Long rhId) {
        Application application = applicationRepository.findByIdAndRhId(applicationId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Candidature non trouvée ou vous n'avez pas les droits"));

        return mapToDetailDTO(application);
    }

    /**
     * Présélectionner des candidats pour l'entretien
     */
    @Transactional
    public List<ApplicationDetailDTO> shortlistApplications(Long jobOfferId, ShortlistApplicationsDTO dto, Long rhId) {
        // Vérifier que l'offre appartient au RH
        jobOfferRepository.findByIdAndCreatedById(jobOfferId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Offre d'emploi non trouvée ou vous n'avez pas les droits"));

        List<ApplicationDetailDTO> shortlisted = new ArrayList<>();

        for (Long applicationId : dto.getApplicationIds()) {
            Application application = applicationRepository.findByIdAndRhId(applicationId, rhId)
                    .orElseThrow(() -> new ResourceNotFoundException("Candidature avec ID " + applicationId + " non trouvée"));

            // Vérifier que la candidature appartient bien à cette offre
            if (!application.getJobOffer().getId().equals(jobOfferId)) {
                throw new ApplicationNotBelongToOfferException("La candidature " + applicationId + " n'appartient pas à cette offre");
            }

            // Changer le statut en SHORTLISTED
            application.setApplicationStatus(ApplicationStatus.SHORTLISTED);
            Application saved = applicationRepository.save(application);
            shortlisted.add(mapToDetailDTO(saved));
        }

        return shortlisted;
    }

    /**
     * Changer le statut d'une candidature
     */
    @Transactional
    public ApplicationDetailDTO updateApplicationStatus(Long applicationId, UpdateApplicationStatusDTO dto, Long rhId) {
        Application application = applicationRepository.findByIdAndRhId(applicationId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Candidature non trouvée ou vous n'avez pas les droits"));

        application.setApplicationStatus(dto.getStatus());

        Application updated = applicationRepository.save(application);
        return mapToDetailDTO(updated);
    }

    /**
     * Obtenir les candidats présélectionnés pour une offre
     */
    @Transactional(readOnly = true)
    public List<ApplicationDetailDTO> getShortlistedApplications(Long jobOfferId, Long rhId) {
        // Vérifier que l'offre appartient au RH
        jobOfferRepository.findByIdAndCreatedById(jobOfferId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Offre d'emploi non trouvée ou vous n'avez pas les droits"));

        List<Application> applications = applicationRepository.findShortlistedByJobOfferId(jobOfferId, rhId);

        return applications.stream()
                .map(this::mapToDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les statistiques des candidatures pour une offre
     */
    @Transactional(readOnly = true)
    public ApplicationStatsDTO getApplicationStats(Long jobOfferId, Long rhId) {
        // Vérifier que l'offre appartient au RH
        JobOffer jobOffer = jobOfferRepository.findByIdAndCreatedById(jobOfferId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Offre d'emploi non trouvée ou vous n'avez pas les droits"));

        Object[] stats = applicationRepository.getApplicationStatsByJobOfferId(jobOfferId, rhId);

        Long totalApplications = stats[0] != null ? ((Number) stats[0]).longValue() : 0L;
        Double averageScore = stats[1] != null ? ((Number) stats[1]).doubleValue() : 0.0;
        Integer maxScore = stats[2] != null ? ((Number) stats[2]).intValue() : 0;
        Integer minScore = stats[3] != null ? ((Number) stats[3]).intValue() : 0;

        // Compter par statut
        Long submittedCount = applicationRepository.countByJobOfferIdAndStatus(jobOfferId, rhId, ApplicationStatus.SUBMITTED);
        Long underReviewCount = applicationRepository.countByJobOfferIdAndStatus(jobOfferId, rhId, ApplicationStatus.UNDER_REVIEW);
        Long shortlistedCount = applicationRepository.countByJobOfferIdAndStatus(jobOfferId, rhId, ApplicationStatus.SHORTLISTED);
        Long rejectedCount = applicationRepository.countByJobOfferIdAndStatus(jobOfferId, rhId, ApplicationStatus.REJECTED);

        return ApplicationStatsDTO.builder()
                .jobOfferId(jobOfferId)
                .jobOfferTitle(jobOffer.getJobTitle())
                .totalApplications(totalApplications)
                .submittedCount(submittedCount)
                .underReviewCount(underReviewCount)
                .shortlistedCount(shortlistedCount)
                .rejectedCount(rejectedCount)
                .averageScore(averageScore)
                .maxScore(maxScore)
                .minScore(minScore)
                .build();
    }

    /**
     * Rejeter une candidature
     */
    @Transactional
    public ApplicationDetailDTO rejectApplication(Long applicationId, Long rhId) {
        Application application = applicationRepository.findByIdAndRhId(applicationId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Candidature non trouvée ou vous n'avez pas les droits"));

        application.setApplicationStatus(ApplicationStatus.REJECTED);
        Application updated = applicationRepository.save(application);

        return mapToDetailDTO(updated);
    }

    /**
     * Rejeter plusieurs candidatures en masse
     */
    @Transactional
    public List<ApplicationDetailDTO> rejectMultipleApplications(Long jobOfferId, ShortlistApplicationsDTO dto, Long rhId) {
        // Vérifier que l'offre appartient au RH
        jobOfferRepository.findByIdAndCreatedById(jobOfferId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Offre d'emploi non trouvée ou vous n'avez pas les droits"));

        List<ApplicationDetailDTO> rejected = new ArrayList<>();

        for (Long applicationId : dto.getApplicationIds()) {
            Application application = applicationRepository.findByIdAndRhId(applicationId, rhId)
                    .orElseThrow(() -> new ResourceNotFoundException("Candidature avec ID " + applicationId + " non trouvée"));

            if (!application.getJobOffer().getId().equals(jobOfferId)) {
                throw new ApplicationNotBelongToOfferException("La candidature " + applicationId + " n'appartient pas à cette offre");
            }

            application.setApplicationStatus(ApplicationStatus.REJECTED);
            Application saved = applicationRepository.save(application);
            rejected.add(mapToDetailDTO(saved));
        }

        return rejected;
    }

    /**
     * Obtenir les candidatures avec un score minimum
     */
    @Transactional(readOnly = true)
    public List<ApplicationListDTO> getApplicationsByMinScore(Long jobOfferId, Long minScore, Long rhId) {
        // Vérifier que l'offre appartient au RH
        jobOfferRepository.findByIdAndCreatedById(jobOfferId, rhId)
                .orElseThrow(() -> new UnauthorizedActionException("Offre d'emploi non trouvée ou vous n'avez pas les droits"));

        List<Application> applications = applicationRepository.findByJobOfferIdAndMinScore(jobOfferId, minScore, Math.toIntExact(rhId));

        return applications.stream()
                .map(this::mapToListDTO)
                .collect(Collectors.toList());
    }

    // Méthodes de mapping privées
    private ApplicationListDTO mapToListDTO(Application application) {
        return ApplicationListDTO.builder()
                .id(application.getId())
                .candidateName(application.getFirstName() + " " + application.getLastName())
                .candidateEmail(application.getEmail())
                .highestDegree(application.getHighestDegree())
                .majorField(application.getMajorField())
                .status(application.getApplicationStatus())
                .scoreIA(application.getScoreIA())
                .submitDate(application.getSubmitDate())
                .cvUrl(application.getCvUrl())
                .build();
    }

    private ApplicationDetailDTO mapToDetailDTO(Application application) {
        return ApplicationDetailDTO.builder()
                .id(application.getId())
                .candidateId(application.getCandidate().getId())
                .firstName(application.getFirstName())
                .lastName(application.getLastName())
                .email(application.getEmail())
                .address(application.getAddress())
                .highestDegree(application.getHighestDegree())
                .majorField(application.getMajorField())
                .cvUrl(application.getCvUrl())
                .status(application.getApplicationStatus())
                .scoreIA(application.getScoreIA())
                .matchingCompetences(application.getMatchingCompetences())
                .matchingExperience(application.getMatchingExperience())
                .matchingDiploma(application.getMatchingDiploma())
                .justificationIA(application.getJustificationIA())
                .submitDate(application.getSubmitDate())
                .updateDate(application.getUpdateDate())
                .jobOfferId(application.getJobOffer().getId())
                .jobOfferTitle(application.getJobOffer().getJobTitle())
                .evaluationCount(application.getEvaluations() != null ? application.getEvaluations().size() : 0)
                .build();
    }


}
