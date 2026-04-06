package com.usermgmt.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(
    name="roles",
    indexes=@Index(name="idx_role_name",columnList="name")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude="roleGroups")
@EntityListeners(AuditingEntityListener.class)

public class Role {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true,nullable=false,length=50)
    private String name;

    @Column(length=200)
    private String description;

    @Builder.Default
    @Column(name="is_system_role")
    private boolean systemRole = false;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(
        name="role_permissions",
        joinColumns=@JoinColumn(name="role_id"),
        inverseJoinColumns=@JoinColumn(name="permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy="roles")
    private Set<RoleGroup> roleGroups = new HashSet<>();

    @CreatedDate
    @Column(name="created_at",updatable=false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name="updated_at")
    private LocalDateTime updatedAt;
}