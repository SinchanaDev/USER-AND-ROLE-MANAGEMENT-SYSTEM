package com.usermgmt.config;

import com.usermgmt.entity.*;
import com.usermgmt.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final RoleGroupRepository groupRepo;
    private final PermissionRepository permRepo;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public void run(String... args) {

        if (permRepo.count() > 0) return;

        // Permissions
        var perms = permRepo.saveAll(List.of(
                perm("USER_READ",     "View users",         "USER"),
                perm("USER_WRITE",    "Manage users",       "USER"),
                perm("USER_DELETE",   "Delete users",       "USER"),
                perm("ROLE_READ",     "View roles",         "ROLE"),
                perm("ROLE_WRITE",    "Manage roles",       "ROLE"),
                perm("ROLE_DELETE",   "Delete roles",       "ROLE"),
                perm("REPORT_VIEW",   "View reports",       "REPORT"),
                perm("REPORT_EXPORT", "Export reports",     "REPORT")
        ));

        // Permission sets by module
        Set<Permission> userPerms = perms.stream()
                .filter(p -> p.getModule().equals("USER"))
                .collect(Collectors.toSet());

        Set<Permission> rolePerms = perms.stream()
                .filter(p -> p.getModule().equals("ROLE"))
                .collect(Collectors.toSet());

        Set<Permission> reportPerms = perms.stream()
                .filter(p -> p.getModule().equals("REPORT"))
                .collect(Collectors.toSet());

        Permission reportViewPerm = perms.stream()
                .filter(p -> p.getName().equals("REPORT_VIEW"))
                .findFirst().get();

        // ── 5 Roles ──────────────────────────────────────────
        var adminRole = roleRepo.save(Role.builder()
                .name("ADMIN")
                .description("Full access to all modules")
                .systemRole(true)
                .permissions(new HashSet<>(perms))
                .build());

        var userMgrRole = roleRepo.save(Role.builder()
                .name("USER_MANAGER")
                .description("Manage users")
                .permissions(userPerms)
                .build());

        var roleMgrRole = roleRepo.save(Role.builder()
                .name("ROLE_MANAGER")
                .description("Manage roles and groups")
                .permissions(rolePerms)
                .build());

        var reportViewerRole = roleRepo.save(Role.builder()
                .name("REPORT_VIEWER")
                .description("View reports only")
                .permissions(Set.of(reportViewPerm))
                .build());

        var reportMgrRole = roleRepo.save(Role.builder()
                .name("REPORT_MANAGER")
                .description("View and export reports")
                .permissions(reportPerms)
                .build());

        // ── 5 Role Groups ─────────────────────────────────────
        var adminGroup = groupRepo.save(RoleGroup.builder()
                .name("Administrators")
                .description("Full admin access")
                .roles(Set.of(adminRole))
                .build());

        var userMgrGroup = groupRepo.save(RoleGroup.builder()
                .name("User Managers")
                .description("User management access")
                .roles(Set.of(userMgrRole))
                .build());

        var roleMgrGroup = groupRepo.save(RoleGroup.builder()
                .name("Role Managers")
                .description("Role and group management access")
                .roles(Set.of(roleMgrRole))
                .build());

        var reportViewerGroup = groupRepo.save(RoleGroup.builder()
                .name("Report Viewers")
                .description("Read only report access")
                .roles(Set.of(reportViewerRole))
                .build());

        var reportMgrGroup = groupRepo.save(RoleGroup.builder()
                .name("Report Managers")
                .description("Full report access")
                .roles(Set.of(reportMgrRole))
                .build());

        // ── Fixed Users ───────────────────────────────────────
        userRepo.save(User.builder()
                .username("admin")
                .email("admin@company.com")
                .password(encoder.encode("Admin@1234"))
                .firstName("System")
                .lastName("Administrator")
                .active(true)
                .firstLogin(false)
                .secretQuestion("Pet name?")
                .secretAnswer(encoder.encode("default"))
                .profileTheme("DEFAULT")
                .roleGroups(Set.of(adminGroup))
                .build());

        userRepo.save(User.builder()
                .username("manager")
                .email("manager@company.com")
                .password(encoder.encode("Manager@123"))
                .firstName("User")
                .lastName("Manager")
                .active(true)
                .firstLogin(false)
                .secretQuestion("Pet name?")
                .secretAnswer(encoder.encode("default"))
                .profileTheme("DEFAULT")
                .roleGroups(Set.of(userMgrGroup))
                .build());

        // ── 25 Demo Users (5 per group) ───────────────────────
        String[] firstNames = {"Alice","Bob","Charlie","Diana","Edward",
                                "Fiona","George","Hannah","Ivan","Julia",
                                "Kevin","Laura","Mike","Nina","Oscar",
                                "Priya","Quinn","Rachel","Steve","Tara",
                                "Umar","Veena","William","Xena","Yusuf"};

        String[] lastNames  = {"Smith","Johnson","Williams","Brown","Jones",
                                "Garcia","Miller","Davis","Wilson","Taylor",
                                "Anderson","Thomas","Jackson","White","Harris",
                                "Martin","Thompson","Robinson","Clark","Lewis",
                                "Hall","Young","Allen","King","Scott"};

        RoleGroup[] groups = {adminGroup, userMgrGroup, roleMgrGroup,
                               reportViewerGroup, reportMgrGroup};

        for (int i = 0; i < 25; i++) {
            RoleGroup assignedGroup = groups[i / 5]; // 5 users per group

            userRepo.save(User.builder()
                    .username("user" + (i + 1))
                    .email("user" + (i + 1) + "@company.com")
                    .password(encoder.encode("User@1234" + (i + 1)))
                    .firstName(firstNames[i])
                    .lastName(lastNames[i])
                    .active(true)
                    .firstLogin(true)
                    .secretQuestion("Pet name?")
                    .secretAnswer(encoder.encode("default"))
                    .profileTheme("DEFAULT")
                    .roleGroups(Set.of(assignedGroup))
                    .build());
        }
    }

    private Permission perm(String name, String desc, String module) {
        return Permission.builder()
                .name(name)
                .description(desc)
                .module(module)
                .build();
    }
}

