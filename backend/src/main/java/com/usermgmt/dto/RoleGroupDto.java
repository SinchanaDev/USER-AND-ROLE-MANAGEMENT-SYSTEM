package com.usermgmt.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RoleGroupDto {
    private Long id;
    @NotBlank @Size(min=2,max=100) private String name;
    private String description;
    private Set<RoleDto> roles;
    private Set<Long> roleIds;
    private int userCount;
    private LocalDateTime createdAt;
}