package com.ept.sn.cri.backend.auth.service;



import com.ept.sn.cri.backend.auth.dto.RegistrationRequest;
import com.ept.sn.cri.backend.auth.dto.AuthResponse;
import com.ept.sn.cri.backend.auth.dto.LoginRequest;
import com.ept.sn.cri.backend.auth.dto.RhRequest;
import com.ept.sn.cri.backend.entity.Candidate;
import com.ept.sn.cri.backend.entity.RH;
import com.ept.sn.cri.backend.entity.User;
import com.ept.sn.cri.backend.enums.Role;
import com.ept.sn.cri.backend.auth.repository.UserRepository;
import com.ept.sn.cri.backend.auth.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public void register(RegistrationRequest request) {
        var candidate =  new Candidate();
        candidate.setFirstName(request.getFirstName());
        candidate.setLastName(request.getLastName());
        candidate.setEmail(request.getEmail());
        candidate.setPassword(passwordEncoder.encode(request.getPassword()));
        candidate.setPhoneNumber(request.getPhoneNumber());
        candidate.setRole(Role.CANDIDATE);
        candidate.setAddress(request.getAdress());

        userRepository.save(candidate);
    }

    public void registerRh(RhRequest rhRequest) {
        var rh = new RH();
        rh.setFirstName(rhRequest.getFirstName());
        rh.setLastName(rhRequest.getLastName());
        rh.setEmail(rhRequest.getEmail());
        rh.setPassword(passwordEncoder.encode(rhRequest.getPassword()));
        rh.setPhoneNumber(rhRequest.getPhoneNumber());
        rh.setDepartment(rhRequest.getDepartment());
        rh.setPositionTitle(rhRequest.getPositionTitle());
        rh.setRole(Role.RH);

        userRepository.save(rh);

    }

    public AuthResponse authenticate(@Valid LoginRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var claims = new HashMap<String,Object>();
        var user = ((User)auth.getPrincipal());
        claims.put("fullname", user.getFullName());
        var jwtToken = jwtService.generateToken(claims,user);
        return AuthResponse.builder()
                .token(jwtToken).build();
    }
}
