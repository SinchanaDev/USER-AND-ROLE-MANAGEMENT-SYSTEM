package com.usermgmt.service;

import com.usermgmt.dto.*;
import com.usermgmt.entity.*;
import com.usermgmt.exception.*;
import com.usermgmt.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepo;
    private final RoleGroupRepository groupRepo;
    private final PermissionRepository permRepo;

    @Transactional(readOnly=true)
    public List<RoleDto> getAllRoles() {
        return roleRepo.findAll().stream().map(this::toRoleDto).collect(Collectors.toList());
    }
    @Transactional(readOnly=true)
    public RoleDto getRoleById(Long id) { return toRoleDto(findRole(id)); }

    @Transactional
    public RoleDto createRole(RoleDto dto) {
        if (roleRepo.existsByName(dto.getName()))
            throw new DuplicateResourceException("Role exists: "+dto.getName());
        var role = Role.builder().name(dto.getName().toUpperCase())
            .description(dto.getDescription()).systemRole(false).build();
        if (dto.getPermissionIds()!=null)
            role.setPermissions(new HashSet<>(permRepo.findAllById(dto.getPermissionIds())));
        return toRoleDto(roleRepo.save(role));
    }

    @Transactional
    public RoleDto updateRole(Long id, RoleDto dto) {
        var role = findRole(id);
        if (role.isSystemRole()) throw new ValidationException("Cannot modify system roles");
        role.setName(dto.getName().toUpperCase());
        role.setDescription(dto.getDescription());
        if (dto.getPermissionIds()!=null)
            role.setPermissions(new HashSet<>(permRepo.findAllById(dto.getPermissionIds())));
        return toRoleDto(roleRepo.save(role));
    }

    @Transactional
    public void deleteRole(Long id) {
        var role = findRole(id);
        if (role.isSystemRole()) throw new ValidationException("Cannot delete system roles");
        for (RoleGroup g : new HashSet<>(role.getRoleGroups())) {
            g.getRoles().remove(role);
            groupRepo.save(g);
        }
        roleRepo.delete(role);
    }

    @Transactional(readOnly=true)
    public List<RoleGroupDto> getAllRoleGroups() {
        return groupRepo.findAll().stream().map(this::toGroupDto).collect(Collectors.toList());
    }
    @Transactional(readOnly=true)
    public RoleGroupDto getRoleGroupById(Long id) { return toGroupDto(findGroup(id)); }

    @Transactional
    public RoleGroupDto createRoleGroup(RoleGroupDto dto) {
        if (groupRepo.existsByName(dto.getName()))
            throw new DuplicateResourceException("Group exists: "+dto.getName());
        var group = RoleGroup.builder().name(dto.getName()).description(dto.getDescription()).build();
        if (dto.getRoleIds()!=null)
            group.setRoles(new HashSet<>(roleRepo.findAllById(dto.getRoleIds())));
        return toGroupDto(groupRepo.save(group));
    }

    @Transactional
    public RoleGroupDto updateRoleGroup(Long id, RoleGroupDto dto) {
        var group = findGroup(id);
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        if (dto.getRoleIds()!=null)
            group.setRoles(new HashSet<>(roleRepo.findAllById(dto.getRoleIds())));
        return toGroupDto(groupRepo.save(group));
    }

    @Transactional
    public void deleteRoleGroup(Long id) {
        var group = findGroup(id);
        for (var user : new HashSet<>(group.getUsers())) {
            user.getRoleGroups().remove(group);
        }
        group.getUsers().clear();
        groupRepo.delete(group);
    }

    @Transactional(readOnly=true)
    public List<PermissionDto> getAllPermissions() {
        return permRepo.findAll().stream().map(this::toPermDto).collect(Collectors.toList());
    }

    private Role findRole(Long id) {
        return roleRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not found: "+id));
    }
    private RoleGroup findGroup(Long id) {
        return groupRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Group not found: "+id));
    }

    public RoleDto toRoleDto(Role r) {
        return RoleDto.builder().id(r.getId()).name(r.getName()).description(r.getDescription())
            .systemRole(r.isSystemRole()).createdAt(r.getCreatedAt())
            .permissions(r.getPermissions()!=null ?
                r.getPermissions().stream().map(this::toPermDto).collect(Collectors.toSet()) : null)
            .build();
    }
    public RoleGroupDto toGroupDto(RoleGroup g) {
        return RoleGroupDto.builder().id(g.getId()).name(g.getName()).description(g.getDescription())
            .createdAt(g.getCreatedAt()).userCount(g.getUsers()!=null ? g.getUsers().size() : 0)
            .roles(g.getRoles()!=null ?
                g.getRoles().stream().map(this::toRoleDto).collect(Collectors.toSet()) : null)
            .build();
    }
    public PermissionDto toPermDto(Permission p) {
        return PermissionDto.builder().id(p.getId()).name(p.getName())
            .description(p.getDescription()).module(p.getModule()).build();
    }
}