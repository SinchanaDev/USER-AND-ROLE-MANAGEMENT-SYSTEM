package com.usermgmt.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PermissionDto {
    private Long id;
    private String name, description, module;
}