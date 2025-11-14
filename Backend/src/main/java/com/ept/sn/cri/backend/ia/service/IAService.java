package com.ept.sn.cri.backend.ia.service;


import com.ept.sn.cri.backend.entity.Application;
import com.ept.sn.cri.backend.entity.JobOffer;
import com.ept.sn.cri.backend.exception.ResourceNotFoundException;
import com.ept.sn.cri.backend.ia.dto.IAJobOfferDTO;
import com.ept.sn.cri.backend.ia.dto.IAScoringResultDTO;
import com.ept.sn.cri.backend.ia.dto.ProcessCVRequestDTO;
import com.ept.sn.cri.backend.ia.dto.ProcessCVResponseDTO;
import com.ept.sn.cri.backend.rh.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class IAService {

    private final RestTemplate restTemplate;
    private final ApplicationRepository applicationRepository;


    @Value("${ia.service.url}")
    private String iaServiceUrl;

    @Value("${ia.service.timeout:30000}")
    private int iaServiceTimeout;

    @Value("${server.url}")
    private String serverUrl;

    /**
     * Traite un CV de manière asynchrone (Parsing + Scoring)
     * Envoie le résultat via webhook une fois terminé
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> processCVAsync(
            Long applicationId,
            MultipartFile cvFile,
            JobOffer jobOffer
    ) {
        log.info(" Début du traitement asynchrone du CV pour candidature {}", applicationId);

        try {
            // Encoder le CV en Base64
            String cvBase64 = encodeFileToBase64(cvFile);

            // Préparer les données de l'offre d'emploi
            IAJobOfferDTO jobOfferDTO = mapToIAJobOfferDTO(jobOffer);

            // Préparer la requête
            ProcessCVRequestDTO request = ProcessCVRequestDTO.builder()
                    .applicationId(applicationId)
                    .cvBase64(cvBase64)
                    .filename(cvFile.getOriginalFilename())
                    .jobOffer(jobOfferDTO)
                    .callbackUrl(serverUrl + "/api/v1/webhook/ia-result")
                    .build();

            // Appeler l'API IA
            log.info(" Envoi de la requête au service IA pour candidature {}", applicationId);
            ProcessCVResponseDTO response = callIAService(request);

            if (response.getSuccess()) {
                log.info(" Traitement IA réussi pour candidature {} - Score: {}",
                        applicationId, response.getScoringResult().getScoreGlobal());

                // Sauvegarder les résultats
                saveIAResults(applicationId, response);
            } else {
                log.error(" Échec du traitement IA pour candidature {}: {}",
                        applicationId, response.getErrorMessage());
            }

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error(" Erreur lors du traitement asynchrone pour candidature {}", applicationId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Sauvegarde les résultats de l'IA dans la base de données
     */
    private void saveIAResults(Long applicationId, ProcessCVResponseDTO response) {
        try {
            Application application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée: " + applicationId));

            // Sauvegarder les scores
            IAScoringResultDTO scoring = response.getScoringResult();
            if (scoring != null) {
                application.setScoreIA(scoring.getScoreGlobal().intValue());
                application.setMatchingCompetences(scoring.getMatchingCompetences().intValue());
                application.setMatchingExperience(scoring.getMatchingExperience().intValue());
                application.setMatchingDiploma(scoring.getMatchingDiploma().intValue());
                application.setJustificationIA(scoring.getJustification());
            }

            applicationRepository.save(application);
            log.info(" Résultats IA sauvegardés pour candidature {}", applicationId);

        } catch (Exception e) {
            log.error(" Erreur lors de la sauvegarde des résultats IA pour candidature {}", applicationId, e);
        }
    }

    /**
     * Appel synchrone au service IA avec retry
     */
    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    private ProcessCVResponseDTO callIAService(ProcessCVRequestDTO request) {
        String url = iaServiceUrl + "/api/ia/process-cv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ProcessCVRequestDTO> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<ProcessCVResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ProcessCVResponseDTO.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Réponse invalide du service IA: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Erreur lors de l'appel au service IA", e);
            throw new RuntimeException("Erreur de communication avec le service IA: " + e.getMessage(), e);
        }
    }

    /**
     * Encode un fichier en Base64
     */
    private String encodeFileToBase64(MultipartFile file) throws IOException {
        byte[] fileContent = file.getBytes();
        return Base64.getEncoder().encodeToString(fileContent);
    }

    /**
     * Mappe une JobOffer vers IAJobOfferDTO
     */
    private IAJobOfferDTO mapToIAJobOfferDTO(JobOffer jobOffer) {
        // Parser les compétences (séparées par des virgules)
        List<String> skills = Arrays.asList(jobOffer.getRequiredSkills().split(","));
        skills = skills.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        return IAJobOfferDTO.builder()
                .jobId(jobOffer.getId())
                .jobTitle(jobOffer.getJobTitle())
                .jobType(jobOffer.getJobType().name())
                .contractType(jobOffer.getTypeContrat().name())
                .description(jobOffer.getDescription())
                .requiredSkills(skills)
                .educationLevel(jobOffer.getNiveauEtudeRequis())
                .minExperience(jobOffer.getExperienceMin())
                .build();
    }

    /**
     * Vérifie la santé du service IA
     */
    public boolean checkIAServiceHealth() {
        try {
            String healthUrl = iaServiceUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            boolean isHealthy = response.getStatusCode() == HttpStatus.OK;

            if (isHealthy) {
                log.info(" Service IA opérationnel");
            } else {
                log.warn(" Service IA en mauvais état: {}", response.getStatusCode());
            }

            return isHealthy;
        } catch (Exception e) {
            log.error(" Service IA injoignable", e);
            return false;
        }
    }



}
