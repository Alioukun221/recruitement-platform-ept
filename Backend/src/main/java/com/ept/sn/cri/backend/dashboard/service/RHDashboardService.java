package com.ept.sn.cri.backend.dashboard.service;

import com.ept.sn.cri.backend.commission.repository.EvaluationRepository;
import com.ept.sn.cri.backend.dashboard.dto.*;
import com.ept.sn.cri.backend.entity.Application;
import com.ept.sn.cri.backend.entity.Commission;
import com.ept.sn.cri.backend.entity.JobOffer;
import com.ept.sn.cri.backend.enums.ApplicationStatus;
import com.ept.sn.cri.backend.enums.CommissionStatus;
import com.ept.sn.cri.backend.enums.JobStatus;
import com.ept.sn.cri.backend.rh.repository.ApplicationRepository;
import com.ept.sn.cri.backend.rh.repository.CommissionMemberRepository;
import com.ept.sn.cri.backend.rh.repository.CommissionRepository;
import com.ept.sn.cri.backend.rh.repository.JobOfferRepository;
import jakarta.validation.constraints.Future;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RHDashboardService {

    private final JobOfferRepository jobOfferRepository;
    private final ApplicationRepository applicationRepository;
    private final CommissionRepository commissionRepository;
    private final CommissionMemberRepository commissionMemberRepository;
    private final EvaluationRepository evaluationRepository;

    @Transactional(readOnly = true)
    public RHDashboardDTO getDashboardData(Long rhId) {
        List<JobOffer> allJobOffers = jobOfferRepository.findByCreatedByIdOrderByDatePublicationDesc(rhId);
        List<Commission> commissions = commissionRepository.findByCreatedByIdOrderByCreatedAtDesc(rhId);

        return RHDashboardDTO.builder()
                .overview(getOverview(allJobOffers, commissions, rhId))
                .jobOfferStats(getJobOfferStats(allJobOffers))
                .applicationStats(getApplicationStats(allJobOffers))
                .commissionStats(getCommissionStats(commissions, rhId))
                .topJobOffers(getTopJobOffers(allJobOffers))
                .recentActivities(getRecentActivities(allJobOffers))
                .alerts(getAlerts(allJobOffers, commissions, rhId))
                .build();
    }

    private DashboardOverviewDTO getOverview(List<JobOffer> allJobOffers, List<Commission> commissions, Long rhId) {
        Set<Long> uniqueCandidates = new HashSet<>();
        int totalApplications = 0, candidatesShortlisted = 0, candidatesInterviewed = 0, candidatesAccepted = 0;

        for (JobOffer jobOffer : allJobOffers) {
            if (jobOffer.getApplications() == null) continue;
            for (Application app : jobOffer.getApplications()) {
                uniqueCandidates.add(app.getCandidate().getId());
                totalApplications++;
                switch (app.getApplicationStatus()) {
                    case SHORTLISTED -> candidatesShortlisted++;
                    case INTERVIEW_COMPLETED, INTERVIEW_SCHEDULED -> candidatesInterviewed++;
                    case ACCEPTED -> candidatesAccepted++;
                }
            }
        }

        int pendingEvaluations = calculatePendingEvaluations(commissions, rhId);

        int totalActiveJobOffers = (int) allJobOffers.stream()
                .filter(jo -> jo.getJobStatus() == JobStatus.PUBLISHED)
                .count();

        return DashboardOverviewDTO.builder()
                .totalActiveJobOffers(totalActiveJobOffers)
                .totalCandidates(uniqueCandidates.size())
                .totalApplications(totalApplications)
                .totalCommissions(commissions.size())
                .candidatesShortlisted(candidatesShortlisted)
                .candidatesInterviewed(candidatesInterviewed)
                .candidatesAccepted(candidatesAccepted)
                .pendingEvaluations(Math.max(0, pendingEvaluations))
                .build();
    }

    private int calculatePendingEvaluations(List<Commission> commissions, Long rhId) {
        int pendingEvaluations = 0;
        for (Commission commission : commissions) {
            if (commission.getMembers() == null) continue;
            int membersCount = commission.getMembers().size();
            int shortlistedCount = applicationRepository.findShortlistedByJobOfferId(
                    commission.getJobOffer().getId(), rhId).size();
            int expectedEvaluations = membersCount * shortlistedCount;

            int actualEvaluations = commission.getMembers().stream()
                    .mapToInt(member -> evaluationRepository.countByCommissionMemberId(member.getId()).intValue())
                    .sum();

            pendingEvaluations += expectedEvaluations - actualEvaluations;
        }
        return pendingEvaluations;
    }

    private JobOfferStatsDTO getJobOfferStats(List<JobOffer> allJobOffers) {
        int totalJobOffers = allJobOffers.size();
        int publishedJobOffers = 0, draftJobOffers = 0, closedJobOffers = 0, archivedJobOffers = 0, jobOffersExpiringSoon = 0;
        int totalCandidates = 0;
        Map<String, Integer> jobOffersByType = new HashMap<>();
        Map<String, Integer> jobOffersByContract = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate sevenDaysFromNow = today.plusDays(7);

        for (JobOffer jobOffer : allJobOffers) {
            switch (jobOffer.getJobStatus()) {
                case PUBLISHED -> publishedJobOffers++;
                case DRAFT -> draftJobOffers++;
                case SUSPENDED -> closedJobOffers++;
                case ARCHIVED -> archivedJobOffers++;
            }

            jobOffersByType.put(jobOffer.getJobType().name(),
                    jobOffersByType.getOrDefault(jobOffer.getJobType().name(), 0) + 1);
            jobOffersByContract.put(jobOffer.getTypeContrat().name(),
                    jobOffersByContract.getOrDefault(jobOffer.getTypeContrat().name(), 0) + 1);

            @Future(message = "La date limite doit être dans le futur.") LocalDateTime dateSql = jobOffer.getDateLimite();
            if (dateSql != null) {
                LocalDate dateLimite = dateSql.toLocalDate();

                if (!dateLimite.isBefore(today) && !dateLimite.isAfter(sevenDaysFromNow)) {
                    jobOffersExpiringSoon++;
                }
            }

            if (jobOffer.getApplications() != null)
                totalCandidates += jobOffer.getApplications().size();
        }

        double averageCandidatesPerOffer = totalJobOffers > 0 ? (double) totalCandidates / totalJobOffers : 0.0;

        return JobOfferStatsDTO.builder()
                .totalJobOffers(totalJobOffers)
                .publishedJobOffers(publishedJobOffers)
                .draftJobOffers(draftJobOffers)
                .closedJobOffers(closedJobOffers)
                .archivedJobOffers(archivedJobOffers)
                .jobOffersByType(jobOffersByType)
                .jobOffersByContract(jobOffersByContract)
                .jobOffersExpiringSoon(jobOffersExpiringSoon)
                .averageCandidatesPerOffer(Math.round(averageCandidatesPerOffer * 100.0) / 100.0)
                .build();
    }

    private ApplicationStatsDTO getApplicationStats(List<JobOffer> allJobOffers) {
        int totalApplications = 0, submittedApplications = 0, underReviewApplications = 0,
                shortlistedApplications = 0, rejectedApplications = 0, withdrawnApplications = 0,
                applicationsLast30Days = 0;

        Map<String, Integer> applicationsByEducationLevel = new HashMap<>();
        List<JobOfferApplicationCountDTO> applicationCounts = new ArrayList<>();
        double totalIAScore = 0;
        int scoredApplications = 0;
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        for (JobOffer jobOffer : allJobOffers) {
            if (jobOffer.getApplications() == null) continue;
            int offerApplicationCount = 0;
            int offerShortlistedCount = 0;

            for (Application app : jobOffer.getApplications()) {
                totalApplications++;
                offerApplicationCount++;

                switch (app.getApplicationStatus()) {
                    case SUBMITTED -> submittedApplications++;
                    case UNDER_REVIEW -> underReviewApplications++;
                    case SHORTLISTED -> { shortlistedApplications++; offerShortlistedCount++; }
                    case REJECTED -> rejectedApplications++;
                    case WITHDRAWN -> withdrawnApplications++;
                }

                if (app.getSubmitDate().isAfter(thirtyDaysAgo)) applicationsLast30Days++;
                if (app.getScoreIA() != null) { totalIAScore += app.getScoreIA(); scoredApplications++; }

                String education = app.getHighestDegree();
                applicationsByEducationLevel.put(education,
                        applicationsByEducationLevel.getOrDefault(education, 0) + 1);
            }

            applicationCounts.add(JobOfferApplicationCountDTO.builder()
                    .jobOfferId(jobOffer.getId())
                    .jobTitle(jobOffer.getJobTitle())
                    .applicationCount(offerApplicationCount)
                    .shortlistedCount(offerShortlistedCount)
                    .status(jobOffer.getJobStatus().name())
                    .build());
        }

        double averageIAScore = scoredApplications > 0 ? totalIAScore / scoredApplications : 0.0;

        List<JobOfferApplicationCountDTO> topOffersByApplications = applicationCounts.stream()
                .sorted((a, b) -> b.getApplicationCount().compareTo(a.getApplicationCount()))
                .limit(5)
                .collect(Collectors.toList());

        return ApplicationStatsDTO.builder()
                .totalApplications(totalApplications)
                .submittedApplications(submittedApplications)
                .underReviewApplications(underReviewApplications)
                .shortlistedApplications(shortlistedApplications)
                .rejectedApplications(rejectedApplications)
                .withdrawnApplications(withdrawnApplications)
                .applicationsLast30Days(applicationsLast30Days)
                .growthRate(0.0) // à compléter si données historiques
                .averageIAScore(Math.round(averageIAScore * 100.0) / 100.0)
                .applicationsByEducationLevel(applicationsByEducationLevel)
                .topOffersByApplications(topOffersByApplications)
                .build();
    }

    private CommissionStatsDTO getCommissionStats(List<Commission> commissions, Long rhId) {
        int totalCommissions = commissions.size();
        int activeCommissions = (int) commissions.stream()
                .filter(c -> c.getStatus() == CommissionStatus.ACTIVE).count();

        int totalCommissionMembers = 0, totalEvaluations = 0, totalExpectedEvaluations = 0;
        CommissionActivityDTO mostActiveCommission = null;
        int maxEvaluations = 0;

        for (Commission commission : commissions) {
            if (commission.getMembers() == null) continue;
            int membersCount = commission.getMembers().size();
            totalCommissionMembers += membersCount;

            int commissionEvaluations = commission.getMembers().stream()
                    .mapToInt(m -> evaluationRepository.countByCommissionMemberId(m.getId()).intValue())
                    .sum();
            totalEvaluations += commissionEvaluations;

            int shortlistedCount = applicationRepository.findShortlistedByJobOfferId(
                    commission.getJobOffer().getId(), rhId).size();
            int expectedEvaluations = membersCount * shortlistedCount;
            totalExpectedEvaluations += expectedEvaluations;

            double completionRate = expectedEvaluations > 0 ? (double) commissionEvaluations / expectedEvaluations * 100 : 0.0;

            if (commissionEvaluations > maxEvaluations) {
                maxEvaluations = commissionEvaluations;
                mostActiveCommission = CommissionActivityDTO.builder()
                        .commissionId(commission.getId())
                        .commissionName(commission.getName())
                        .jobOfferTitle(commission.getJobOffer().getJobTitle())
                        .membersCount(membersCount)
                        .candidatesCount(shortlistedCount)
                        .evaluationsCount(commissionEvaluations)
                        .completionRate(Math.round(completionRate * 100.0) / 100.0)
                        .build();
            }
        }

        double evaluationCompletionRate = totalExpectedEvaluations > 0 ? (double) totalEvaluations / totalExpectedEvaluations * 100 : 0.0;
        double averageMembersPerCommission = totalCommissions > 0 ? (double) totalCommissionMembers / totalCommissions : 0.0;

        return CommissionStatsDTO.builder()
                .totalCommissions(totalCommissions)
                .activeCommissions(activeCommissions)
                .totalCommissionMembers(totalCommissionMembers)
                .totalEvaluations(totalEvaluations)
                .evaluationCompletionRate(Math.round(evaluationCompletionRate * 100.0) / 100.0)
                .mostActiveCommission(mostActiveCommission)
                .averageMembersPerCommission(Math.round(averageMembersPerCommission * 100.0) / 100.0)
                .build();
    }

    private List<TopJobOfferDTO> getTopJobOffers(List<JobOffer> allJobOffers) {
        return allJobOffers.stream()
                .map(jobOffer -> {
                    int totalApplications = jobOffer.getApplications() != null ? jobOffer.getApplications().size() : 0;
                    int shortlistedCount = jobOffer.getApplications() != null ?
                            (int) jobOffer.getApplications().stream()
                                    .filter(app -> app.getApplicationStatus() == ApplicationStatus.SHORTLISTED)
                                    .count() : 0;
                    double avgIAScore = jobOffer.getApplications() != null ?
                            jobOffer.getApplications().stream()
                                    .filter(app -> app.getScoreIA() != null)
                                    .mapToInt(Application::getScoreIA)
                                    .average().orElse(0.0) : 0.0;

                    LocalDateTime publishDate = jobOffer.getDatePublication() != null ?
                            jobOffer.getDatePublication().toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime() : LocalDateTime.now();
                    long daysActive = ChronoUnit.DAYS.between(publishDate, LocalDateTime.now());

                    return TopJobOfferDTO.builder()
                            .jobOfferId(jobOffer.getId())
                            .jobTitle(jobOffer.getJobTitle())
                            .totalApplications(totalApplications)
                            .shortlistedCount(shortlistedCount)
                            .averageIAScore(Math.round(avgIAScore * 100.0) / 100.0)
                            .daysActive((int) daysActive)
                            .status(jobOffer.getJobStatus().name())
                            .build();
                })
                .sorted((a, b) -> b.getTotalApplications().compareTo(a.getTotalApplications()))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<RecentActivityDTO> getRecentActivities(List<JobOffer> allJobOffers) {
        List<RecentActivityDTO> activities = new ArrayList<>();

        for (JobOffer jobOffer : allJobOffers) {
            if (jobOffer.getApplications() == null) continue;
            for (Application app : jobOffer.getApplications()) {
                if (app.getSubmitDate() != null) {
                    activities.add(RecentActivityDTO.builder()
                            .activityType("NEW_APPLICATION")
                            .description(app.getFirstName() + " " + app.getLastName() +
                                    " a postulé pour " + jobOffer.getJobTitle())
                            .timestamp(String.valueOf(app.getSubmitDate()))
                            .relatedEntity(jobOffer.getJobTitle())
                            .entityId(app.getId())
                            .build());
                }
                if (app.getApplicationStatus() == ApplicationStatus.SHORTLISTED && app.getUpdateDate() != null) {
                    activities.add(RecentActivityDTO.builder()
                            .activityType("SHORTLISTED")
                            .description(app.getFirstName() + " " + app.getLastName() +
                                    " a été présélectionné(e)")
                            .timestamp(String.valueOf(app.getUpdateDate()))
                            .relatedEntity(jobOffer.getJobTitle())
                            .entityId(app.getId())
                            .build());
                }
            }
        }

        return activities.stream()
                .sorted(Comparator.comparing(RecentActivityDTO::getTimestamp).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<DashboardAlertDTO> getAlerts(List<JobOffer> allJobOffers, List<Commission> commissions, Long rhId) {
        List<DashboardAlertDTO> alerts = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysFromNow = now.plusDays(7);

        long expiringSoon = allJobOffers.stream()
                .filter(jo -> jo.getDateLimite() != null)
                .filter(jo -> {
                    // Conversion sécurisée
                    LocalDateTime dateLimite = jo.getDateLimite()
                            .toLocalDate()          // java.sql.Date -> LocalDate
                            .atStartOfDay();        // LocalDate -> LocalDateTime à minuit
                    return dateLimite.isAfter(now) && dateLimite.isBefore(sevenDaysFromNow);
                })
                .count();

        if (expiringSoon > 0) {
            alerts.add(DashboardAlertDTO.builder()
                    .alertType("WARNING")
                    .title("Offres expirant bientôt")
                    .message(expiringSoon + " offre(s) expire(nt) dans les 7 prochains jours")
                    .actionUrl("/api/rh/job-offers")
                    .count((int) expiringSoon)
                    .build());
        }

        long pendingApplications = allJobOffers.stream()
                .filter(jo -> jo.getApplications() != null)
                .flatMap(jo -> jo.getApplications().stream())
                .filter(app -> app.getApplicationStatus() == ApplicationStatus.SUBMITTED)
                .count();

        if (pendingApplications > 0) {
            alerts.add(DashboardAlertDTO.builder()
                    .alertType("INFO")
                    .title("Nouvelles candidatures")
                    .message(pendingApplications + " candidature(s) en attente de traitement")
                    .actionUrl("/api/rh/applications")
                    .count((int) pendingApplications)
                    .build());
        }

        int pendingEvaluations = calculatePendingEvaluations(commissions, rhId);
        if (pendingEvaluations > 0) {
            alerts.add(DashboardAlertDTO.builder()
                    .alertType("INFO")
                    .title("Évaluations en attente")
                    .message(pendingEvaluations + " évaluation(s) en attente par les membres de commission")
                    .actionUrl("/api/rh/commissions")
                    .count(pendingEvaluations)
                    .build());
        }

        return alerts;
    }
}



