package com.usermgmt.controller;

import com.usermgmt.dto.*;
import com.usermgmt.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.LoginResponse> login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthDtos.LoginResponse> refresh(@RequestParam String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserDetails ud,
                                               @Valid @RequestBody AuthDtos.ChangePasswordRequest req) {
        userService.changePassword(ud.getUsername(), req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<Void> completeProfile(@AuthenticationPrincipal UserDetails ud,
                                                @Valid @RequestBody AuthDtos.CompleteProfileRequest req) {
        userService.completeProfile(ud.getUsername(), req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordRequest req) {
        userService.resetForgottenPassword(req);
        return ResponseEntity.ok().build();
    }
}