package com.usermgmt.controller;

import com.usermgmt.dto.*;
import com.usermgmt.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {
    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDto> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @PostMapping
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody RoleDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleDto> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDto dto) {
        return ResponseEntity.ok(roleService.updateRole(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/groups")
    public ResponseEntity<List<RoleGroupDto>> getAllGroups() {
        return ResponseEntity.ok(roleService.getAllRoleGroups());
    }

    @GetMapping("/groups/{id}")
    public ResponseEntity<RoleGroupDto> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleGroupById(id));
    }

    @PostMapping("/groups")
    public ResponseEntity<RoleGroupDto> createGroup(@Valid @RequestBody RoleGroupDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRoleGroup(dto));
    }

    @PutMapping("/groups/{id}")
    public ResponseEntity<RoleGroupDto> updateGroup(@PathVariable Long id, @Valid @RequestBody RoleGroupDto dto) {
        return ResponseEntity.ok(roleService.updateRoleGroup(id, dto));
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        roleService.deleteRoleGroup(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionDto>> getPermissions() {
        return ResponseEntity.ok(roleService.getAllPermissions());
    }
}