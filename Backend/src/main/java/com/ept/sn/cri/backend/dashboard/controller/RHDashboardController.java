package com.ept.sn.cri.backend.dashboard.controller;


import com.ept.sn.cri.backend.dashboard.dto.RHDashboardDTO;
import com.ept.sn.cri.backend.dashboard.service.RHDashboardService;
import com.ept.sn.cri.backend.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rh/dashboard")
@RequiredArgsConstructor
@Tag(name="Dashboard RH")
public class RHDashboardController {

    private final RHDashboardService rhDashboardService;

    /**
     * Obtenir toutes les données du dashboard RH
     * GET /api/v1/rh/dashboard
     */
    @PreAuthorize("hasAuthority('RH')")
    @GetMapping
    public ResponseEntity<RHDashboardDTO> getDashboard(@AuthenticationPrincipal User user) {
        RHDashboardDTO dashboard = rhDashboardService.getDashboardData(user.getId());
        return ResponseEntity.ok(dashboard);
    }
}
