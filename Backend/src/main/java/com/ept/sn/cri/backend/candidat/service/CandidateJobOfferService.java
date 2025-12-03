package com.ept.sn.cri.backend.candidat.service;

import com.ept.sn.cri.backend.candidat.dto.*;
import com.ept.sn.cri.backend.candidat.repository.CandidateApplicationRepository;
import com.ept.sn.cri.backend.candidat.repository.CandidateRepository;
import com.ept.sn.cri.backend.candidat.repository.PublicJobOfferRepository;
import com.ept.sn.cri.backend.entity.Application;
import com.ept.sn.cri.backend.entity.Candidate;
import com.ept.sn.cri.backend.entity.JobOffer;
import com.ept.sn.cri.backend.enums.ApplicationStatus;
import com.ept.sn.cri.backend.exception.*;
import com.ept.sn.cri.backend.ia.service.IAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateJobOfferService {


    private final PublicJobOfferRepository publicJobOfferRepository;
    private final CandidateApplicationRepository candidateApplicationRepository;
    private final CandidateRepository candidateRepository;
    private final IAService iaService;

    // Répertoire pour stocker les CVs
    private final String UPLOAD_DIR = "uploads/cvs/";

    /**
     * Obtenir toutes les offres disponibles (PUBLIC - pas besoin d'authentification)
     */
    @Transactional(readOnly = true)
    public List<PublicJobOfferListDTO> getAvailableJobOffers() {
        Date currentDate = new Date();
        List<JobOffer> jobOffers = publicJobOfferRepository.findAvailableJobOffers(currentDate);

        return jobOffers.stream()
                .map(this::mapToPublicListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les détails d'une offre spécifique (PUBLIC)
     */
    @Transactional(readOnly = true)
    public PublicJobOfferDetailDTO getJobOfferDetails(Long jobOfferId) {
        JobOffer jobOffer = publicJobOfferRepository.findPublishedJobOfferById(jobOfferId);

        if (jobOffer == null) {
            throw new ResourceNotFoundException("Offre d'emploi non trouvée ou non disponible");
        }

        return mapToPublicDetailDTO(jobOffer);
    }

    /**
     * Rechercher des offres par mot-clé (PUBLIC)
     */
    @Transactional(readOnly = true)
    public List<PublicJobOfferListDTO> searchJobOffers(String keyword) {
        Date currentDate = new Date();
        List<JobOffer> jobOffers = publicJobOfferRepository.searchByKeyword(keyword, currentDate);

        return jobOffers.stream()
                .map(this::mapToPublicListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Soumettre une candidature (PROTÉGÉ - candidat authentifié avec rôle CANDIDATE)
     */
    @Transactional
    public ApplicationSubmissionResponseDTO submitApplication(
            Long jobOfferId,
            SubmitApplicationDTO dto,
            MultipartFile cvFile,
            Long candidateId) {

        // Vérifier que le candidat existe
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé"));

        // Vérifier que l'offre existe et est disponible
        JobOffer jobOffer = publicJobOfferRepository.findPublishedJobOfferById(jobOfferId);
        if (jobOffer == null) {
            throw new ResourceNotFoundException("Offre d'emploi non trouvée ou non disponible");
        }
        if (jobOffer.getDateLimite() != null) {
            LocalDateTime dateLimite = jobOffer.getDateLimite()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atStartOfDay();

            LocalDateTime now = LocalDateTime.now();

            if (dateLimite.isBefore(now)) {
                throw new RuntimeException("La date limite pour cette offre est expirée");
            }
        }

        // Vérifier si le candidat n'a pas déjà postulé
        if (candidateApplicationRepository.existsByCandidateIdAndJobOfferId(candidateId, jobOfferId)) {
            throw new AlreadyAppliedException("Vous avez déjà postulé à cette offre");
        }

        // Upload du CV
        String cvUrl = null;
        if (cvFile != null && !cvFile.isEmpty()) {
            cvUrl = uploadCV(cvFile);
        }

        // Créer la candidature
        Application application = new Application();
        application.setJobOffer(jobOffer);
        application.setCandidate(candidate);
        application.setFirstName(dto.getFirstName());
        application.setLastName(dto.getLastName());
        application.setEmail(dto.getEmail());
        application.setAddress(dto.getAddress());
        application.setHighestDegree(dto.getHighestDegree());
        application.setMajorField(dto.getMajorField());
        application.setPhoneNumber(dto.getPhoneNumber());
        application.setNationality(dto.getNationality());
        application.setEducationalInstitution(dto.getEducationalInstitution());
        application.setYearOfGraduation(dto.getYearOfGraduation());
        application.setMotivationEcole(dto.getMotivationEcole());
        application.setMotivationPosition(dto.getMotivationPosition());
        application.setAvailableImmediately(dto.isAvailableImmediately());
        application.setCertifyAccurate(dto.isCertifyAccurate());
        application.setConsentGDPR(dto.isConsentGDPR());
        application.setElectronicSignature(dto.getElectronicSignature());
        application.setCvUrl(cvUrl);
        application.setApplicationStatus(ApplicationStatus.SUBMITTED);

        Application savedApplication = candidateApplicationRepository.save(application);

        log.info(" Candidature {} créée avec succès", savedApplication.getId());

        //  Lancer le traitement IA en asynchrone
        if (cvFile != null && !cvFile.isEmpty()) {
            log.info(" Lancement du traitement IA pour la candidature {}", savedApplication.getId());
            try {
                iaService.processCVAsync(savedApplication.getId(), cvFile, jobOffer);
                log.info(" Traitement IA démarré en arrière-plan pour candidature {}",
                        savedApplication.getId());
            } catch (Exception e) {
                log.error(" Erreur lors du lancement du traitement IA pour candidature {}: {}",
                        savedApplication.getId(), e.getMessage());
            }
        } else {
            log.warn(" Pas de CV fourni, traitement IA non lancé pour candidature {}",
                    savedApplication.getId());
        }

        return ApplicationSubmissionResponseDTO.builder()
                .id(savedApplication.getId())
                .message("Candidature soumise avec succès")
                .jobOfferId(jobOffer.getId())
                .jobOfferTitle(jobOffer.getJobTitle())
                .candidateName(dto.getFirstName() + " " + dto.getLastName())
                .submitDate(savedApplication.getSubmitDate())
                .status(savedApplication.getApplicationStatus().name())
                .build();
    }

    /**
     * Obtenir l'historique des candidatures d'un candidat (PROTÉGÉ)
     */
    @Transactional(readOnly = true)
    public List<CandidateApplicationHistoryDTO> getCandidateApplicationHistory(Long candidateId) {
        List<Application> applications = candidateApplicationRepository.findByCandidateIdOrderBySubmitDateDesc(candidateId);

        return applications.stream()
                .map(this::mapToHistoryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les détails d'une candidature spécifique du candidat (PROTÉGÉ)
     */
    @Transactional(readOnly = true)
    public CandidateApplicationDetailDTO getCandidateApplicationDetail(Long applicationId, Long candidateId) {
        Application application = candidateApplicationRepository.findByIdAndCandidateId(applicationId, candidateId)
                .orElseThrow(() -> new ApplicationNotFoundException("Candidature non trouvée"));

        return mapToDetailDTO(application);
    }

    /**
     * Retirer une candidature (PROTÉGÉ)
     */
    @Transactional
    public void withdrawApplication(Long applicationId, Long candidateId) {
        Application application = candidateApplicationRepository
                .findByIdAndCandidateId(applicationId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée."));

        // On peut retirer uniquement si le statut est SUBMITTED ou UNDER_REVIEW
        if (application.getApplicationStatus() != ApplicationStatus.SUBMITTED &&
                application.getApplicationStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new InvalidActionException("Vous ne pouvez plus retirer cette candidature.");
        }

        application.setApplicationStatus(ApplicationStatus.WITHDRAWN);
        candidateApplicationRepository.save(application);
    }


    // Méthode privée pour uploader le CV
    private String uploadCV(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Le fichier CV est vide ou invalide.");
        }

        try {

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Vérifier le nom du fichier
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                throw new FileStorageException("Le fichier doit avoir une extension valide.");
            }

            // Générer un nom de fichier unique
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID() + fileExtension;

            // Sauvegarder le fichier
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/cvs/" + uniqueFilename;

        } catch (IOException e) {
            throw new FileStorageException("Erreur lors de l'upload du CV : " + e.getMessage());
        }
    }


    // Méthodes de mapping privées
    private PublicJobOfferListDTO mapToPublicListDTO(JobOffer jobOffer) {

        List<String> skillsList = List.of();
        if (jobOffer.getRequiredSkills() != null && !jobOffer.getRequiredSkills().isBlank()) {
            // Séparateurs possibles : ',' ';' '|'
            skillsList = List.of(jobOffer.getRequiredSkills().split("\\s*[,;|]\\s*"));
        }


        return PublicJobOfferListDTO.builder()
                .id(jobOffer.getId())
                .jobTitle(jobOffer.getJobTitle())
                .jobType(jobOffer.getJobType())
                .typeContrat(jobOffer.getTypeContrat())
                .description(jobOffer.getDescription().length() > 200
                        ? jobOffer.getDescription().substring(0, 200) + "..."
                        : jobOffer.getDescription())
                .niveauEtudeRequis(jobOffer.getNiveauEtudeRequis())
                .experienceMin(jobOffer.getExperienceMin())
                .datePublication(jobOffer.getDatePublication())
                .dateLimite(jobOffer.getDateLimite())
                .applicationCount(jobOffer.getApplications() != null ? jobOffer.getApplications().size() : 0)
                .requiredSkills(skillsList)
                .build();
    }

    private PublicJobOfferDetailDTO mapToPublicDetailDTO(JobOffer jobOffer) {
        return PublicJobOfferDetailDTO.builder()
                .id(jobOffer.getId())
                .jobTitle(jobOffer.getJobTitle())
                .jobType(jobOffer.getJobType())
                .typeContrat(jobOffer.getTypeContrat())
                .description(jobOffer.getDescription())
                .requiredSkills(jobOffer.getRequiredSkills())
                .niveauEtudeRequis(jobOffer.getNiveauEtudeRequis())
                .experienceMin(jobOffer.getExperienceMin())
                .datePublication(jobOffer.getDatePublication())
                .dateLimite(jobOffer.getDateLimite())
                .applicationCount(jobOffer.getApplications() != null ? jobOffer.getApplications().size() : 0)
                .companyName(jobOffer.getCreatedBy().getDepartment())
                .build();
    }

    private CandidateApplicationHistoryDTO mapToHistoryDTO(Application application) {
        boolean canWithdraw = application.getApplicationStatus() == ApplicationStatus.SUBMITTED ||
                application.getApplicationStatus() == ApplicationStatus.UNDER_REVIEW;

        return CandidateApplicationHistoryDTO.builder()
                .id(application.getId())
                .jobOfferId(application.getJobOffer().getId())
                .jobTitle(application.getJobOffer().getJobTitle())
                .jobType(application.getJobOffer().getJobType())
                .typeContrat(application.getJobOffer().getTypeContrat())
                .status(application.getApplicationStatus().name())
                .submitDate(application.getSubmitDate())
                .lastUpdateDate(application.getUpdateDate())
                .cvUrl(application.getCvUrl())
                .canWithdraw(canWithdraw)
                .build();
    }

    private CandidateApplicationDetailDTO mapToDetailDTO(Application application) {
        return CandidateApplicationDetailDTO.builder()
                .id(application.getId())
                .jobOfferId(application.getJobOffer().getId())
                .jobTitle(application.getJobOffer().getJobTitle())
                .jobDescription(application.getJobOffer().getDescription())
                .firstName(application.getFirstName())
                .lastName(application.getLastName())
                .email(application.getEmail())
                .address(application.getAddress())
                .highestDegree(application.getHighestDegree())
                .specialization(application.getMajorField())
                .cvUrl(application.getCvUrl())
                .status(application.getApplicationStatus().name())
                .submitDate(application.getSubmitDate())
                .lastUpdateDate(application.getUpdateDate())
                .build();
    }


}
