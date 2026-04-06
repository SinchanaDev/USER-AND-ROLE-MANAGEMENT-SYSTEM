package com.usermgmt.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RoleDto {
    private Long id;
    @NotBlank @Size(min=2,max=50) private String name;
    private String description;
    private boolean systemRole;
    private Set<PermissionDto> permissions;
    private Set<Long> permissionIds;
    private LocalDateTime createdAt;
}