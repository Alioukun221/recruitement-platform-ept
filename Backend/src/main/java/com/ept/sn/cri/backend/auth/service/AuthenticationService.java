package com.ept.sn.cri.backend.auth.service;



import com.ept.sn.cri.backend.auth.dto.*;
import com.ept.sn.cri.backend.entity.Candidate;
import com.ept.sn.cri.backend.entity.CommissionMember;
import com.ept.sn.cri.backend.entity.RH;
import com.ept.sn.cri.backend.entity.User;
import com.ept.sn.cri.backend.enums.Role;
import com.ept.sn.cri.backend.auth.repository.UserRepository;
import com.ept.sn.cri.backend.auth.security.JwtService;
import com.ept.sn.cri.backend.exception.EmailAlreadyExistsException;
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

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Un utilisateur avec cet email existe déjà");
        }
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

        if (userRepository.existsByEmail(rhRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Un utilisateur avec cet email existe déjà");
        }
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
        claims.put("role", user.getRole().name());
        var jwtToken = jwtService.generateToken(claims,user);
        return AuthResponse.builder()
                .token(jwtToken)
                .role(user.getRole().name())
                .build();
    }

    public void registerCommissionMember(@Valid CommissionMemberRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Un utilisateur avec cet email existe déjà");
        }
        CommissionMember member = new CommissionMember();
        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setEmail(request.getEmail());
        member.setPassword(passwordEncoder.encode(request.getPassword()));
        member.setPhoneNumber(request.getPhoneNumber());
        member.setExpertiseArea(request.getExpertiseArea());
        member.setRole(Role.COMMISSION_MEMBER);

        userRepository.save(member);


    }
}
