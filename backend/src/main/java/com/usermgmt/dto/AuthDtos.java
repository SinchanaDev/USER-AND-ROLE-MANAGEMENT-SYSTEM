package com.usermgmt.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public class AuthDtos {
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LoginResponse {
        private String accessToken, refreshToken;
        private String tokenType="Bearer";
        private UserDto user;
        private boolean firstLogin;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ChangePasswordRequest {
        @NotBlank private String currentPassword;
        @NotBlank @Size(min=8) private String newPassword;
        @NotBlank private String confirmPassword;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ResetPasswordRequest {
        @NotBlank @Email private String email;
        @NotBlank private String secretAnswer;
        @NotBlank @Size(min=8) private String newPassword;
        @NotBlank private String confirmPassword;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CompleteProfileRequest {
        @NotBlank private String firstName;
        @NotBlank private String lastName;
        private String phoneNumber;
        @NotBlank private String secretQuestion;
        @NotBlank private String secretAnswer;
        @NotBlank @Size(min=8) private String newPassword;
        @NotBlank private String confirmPassword;
    }
}