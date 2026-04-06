package com.usermgmt.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank @Size(min=3,max=50) private String username;
    @NotBlank @Email private String email;
    private String firstName, lastName, phoneNumber, profileTheme;
    private boolean active, firstLogin, accountLocked;
    private LocalDateTime createdAt, lastLogin;
    private Set<String> allRoles, allPermissions;
    @Size(min=8,max=50) private String password;
    private Set<Long> roleGroupIds;
}