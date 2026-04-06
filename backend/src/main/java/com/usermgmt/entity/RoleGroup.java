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
    name="role_groups",
    indexes=@Index(name="idx_rolegroup_name",columnList="name")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude={"roles","users"})
@EntityListeners(AuditingEntityListener.class)

public class RoleGroup {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true,nullable=false,length=100)
    private String name;

    @Column(length=300)
    private String description;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(
        name="role_group_roles",
        joinColumns=@JoinColumn(name="role_group_id"),
        inverseJoinColumns=@JoinColumn(name="role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy="roleGroups")
    private Set<User> users = new HashSet<>();

    @CreatedDate
    @Column(name="created_at",updatable=false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name="updated_at")
    private LocalDateTime updatedAt;
}