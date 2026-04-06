package com.usermgmt.service;

import com.usermgmt.dto.*;
import com.usermgmt.entity.User;
import com.usermgmt.exception.*;
import com.usermgmt.repository.UserRepository;
import com.usermgmt.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service @RequiredArgsConstructor
public class AuthService {
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_MINUTES = 30;

    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @Transactional
    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest req) {
        var user = userRepo.findByUsername(req.getUsername())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (user.isAccountLocked()) {
            if (user.getLockTime().plusMinutes(LOCK_MINUTES).isAfter(LocalDateTime.now()))
                throw new LockedException("Account locked. Try again later.");
            user.setAccountLocked(false);
            user.setFailedAttempts(0);
            userRepo.save(user);
        }

        try {
            var auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
            user.setFailedAttempts(0);
            user.setLastLogin(LocalDateTime.now());
            userRepo.save(user);
            var ud = (org.springframework.security.core.userdetails.UserDetails) auth.getPrincipal();
            return AuthDtos.LoginResponse.builder()
                .accessToken(jwtUtils.generateToken(ud))
                .refreshToken(jwtUtils.generateRefreshToken(ud))
                .user(userService.toDto(user))
                .firstLogin(user.isFirstLogin())
                .build();
        } catch (BadCredentialsException e) {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= MAX_ATTEMPTS) {
                user.setAccountLocked(true);
                user.setLockTime(LocalDateTime.now());
            }
            userRepo.save(user);
            throw e;
        }
    }

    public AuthDtos.LoginResponse refreshToken(String refreshToken) {
        String username = jwtUtils.extractUsername(refreshToken);
        var ud = userDetailsService.loadUserByUsername(username);
        if (!jwtUtils.validateToken(refreshToken, ud))
            throw new ValidationException("Invalid refresh token");
        var user = userRepo.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return AuthDtos.LoginResponse.builder()
            .accessToken(jwtUtils.generateToken(ud))
            .refreshToken(refreshToken)
            .user(userService.toDto(user))
            .firstLogin(user.isFirstLogin())
            .build();
    }
}