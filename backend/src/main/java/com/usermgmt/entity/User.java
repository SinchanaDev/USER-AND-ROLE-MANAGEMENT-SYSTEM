package com.usermgmt.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(
    name="users",
    indexes={
        @Index(name="idx_username",columnList="username"),
        @Index(name="idx_email",columnList="email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude="roleGroups")
@EntityListeners(AuditingEntityListener.class)

public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true,nullable=false,length=50)
    private String username;

    @Column(unique=true,nullable=false,length=100)
    private String email;

    @JsonIgnore
    @Column(nullable=false,length=255)
    private String password;

    @Column(name="first_name",length=50)
    private String firstName;

    @Column(name="last_name",length=50)
    private String lastName;

    @Column(name="phone_number",length=20)
    private String phoneNumber;

    @Column(name="secret_question")
    private String secretQuestion;

    @JsonIgnore
    @Column(name="secret_answer")
    private String secretAnswer;

    @Column(name="profile_theme",length=20)
    @Builder.Default
    private String profileTheme = "DEFAULT";

    @Column(name="is_active")
    @Builder.Default
    private boolean active = true;

    @Column(name="first_login")
    @Builder.Default
    private boolean firstLogin = true;

    @Column(name="account_locked")
    @Builder.Default
    private boolean accountLocked = false;

    @Column(name="failed_attempts")
    @Builder.Default
    private int failedAttempts = 0;

    @Column(name="lock_time")
    private LocalDateTime lockTime;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(
        name="user_role_groups",
        joinColumns=@JoinColumn(name="user_id"),
        inverseJoinColumns=@JoinColumn(name="role_group_id")
    )
    private Set<RoleGroup> roleGroups = new HashSet<>();

    @CreatedDate
    @Column(name="created_at",updatable=false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @Column(name="last_login")
    private LocalDateTime lastLogin;

    public Set<Role> getAllRoles() {
        return roleGroups.stream()
                .flatMap(g -> g.getRoles().stream())
                .collect(Collectors.toSet());
    }

    public Set<Permission> getAllPermissions() {
        return getAllRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .collect(Collectors.toSet());
    }
}