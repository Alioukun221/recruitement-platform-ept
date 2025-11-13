package com.ept.sn.cri.backend.auth.controller;


import com.ept.sn.cri.backend.auth.dto.*;
import com.ept.sn.cri.backend.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name="Authentification")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register/candidate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> register(
            @RequestBody  @Valid RegistrationRequest request
    ) {
        authenticationService.register(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/register/rh")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> registerRh(
            @RequestBody @Valid RhRequest request
    ){
        authenticationService.registerRh(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/register/commission-member")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> registerCommissionMember(
            @RequestBody @Valid CommissionMemberRequest request
    ){
        authenticationService.registerCommissionMember(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(
            @RequestBody @Valid LoginRequest request
    ){
        return  ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
