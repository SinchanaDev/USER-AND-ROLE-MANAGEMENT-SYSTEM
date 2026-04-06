package com.usermgmt.service;

import com.usermgmt.dto.*;
import com.usermgmt.entity.*;
import com.usermgmt.exception.*;
import com.usermgmt.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class UserService {
    private static final Pattern STRONG_PASSWORD =
        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    private final UserRepository userRepo;
    private final RoleGroupRepository groupRepo;
    private final PasswordEncoder encoder;

    @Transactional(readOnly=true)
    public Page<UserDto> getAllUsers(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepo.searchUsers(search, pageable).map(this::toDto);
    }

    @Transactional(readOnly=true)
    public UserDto getUserById(Long id) { return toDto(findUser(id)); }

    @Transactional
    public UserDto createUser(UserDto dto) {
        if (userRepo.existsByUsername(dto.getUsername()))
            throw new DuplicateResourceException("Username exists: "+dto.getUsername());
        if (userRepo.existsByEmail(dto.getEmail()))
            throw new DuplicateResourceException("Email exists: "+dto.getEmail());
        String password = dto.getPassword()!=null ? dto.getPassword() : "Temp@"+UUID.randomUUID().toString().substring(0,8);
        validatePassword(password);
        var user = User.builder()
            .username(dto.getUsername()).email(dto.getEmail())
            .password(encoder.encode(password))
            .firstName(dto.getFirstName()).lastName(dto.getLastName())
            .phoneNumber(dto.getPhoneNumber())
            .profileTheme(dto.getProfileTheme()!=null ? dto.getProfileTheme() : "DEFAULT")
            .active(true).firstLogin(true).build();
        if (dto.getRoleGroupIds()!=null && !dto.getRoleGroupIds().isEmpty())
            user.setRoleGroups(new HashSet<>(groupRepo.findAllById(dto.getRoleGroupIds())));
        return toDto(userRepo.save(user));
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto dto) {
        var user = findUser(id);
        if (!user.getEmail().equals(dto.getEmail()) && userRepo.existsByEmail(dto.getEmail()))
            throw new DuplicateResourceException("Email exists");
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getProfileTheme()!=null) user.setProfileTheme(dto.getProfileTheme());
        if (dto.getRoleGroupIds()!=null)
            user.setRoleGroups(new HashSet<>(groupRepo.findAllById(dto.getRoleGroupIds())));
        return toDto(userRepo.save(user));
    }

    @Transactional
    public void deleteUser(Long id) { userRepo.delete(findUser(id)); }

    @Transactional
    public void toggleUserStatus(Long id) {
        var user = findUser(id);
        user.setActive(!user.isActive());
        userRepo.save(user);
    }

    @Transactional
    public void adminResetPassword(Long id, String newPassword) {
        validatePassword(newPassword);
        var user = findUser(id);
        user.setPassword(encoder.encode(newPassword));
        user.setFirstLogin(true);
        userRepo.save(user);
    }

    @Transactional
    public void changePassword(String username, AuthDtos.ChangePasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword()))
            throw new ValidationException("Passwords do not match");
        validatePassword(req.getNewPassword());
        var user = userRepo.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!encoder.matches(req.getCurrentPassword(), user.getPassword()))
            throw new ValidationException("Current password incorrect");
        user.setPassword(encoder.encode(req.getNewPassword()));
        userRepo.save(user);
    }

    @Transactional
    public void completeProfile(String username, AuthDtos.CompleteProfileRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword()))
            throw new ValidationException("Passwords do not match");
        validatePassword(req.getNewPassword());
        var user = userRepo.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setSecretQuestion(req.getSecretQuestion());
        user.setSecretAnswer(encoder.encode(req.getSecretAnswer()));
        user.setPassword(encoder.encode(req.getNewPassword()));
        user.setFirstLogin(false);
        userRepo.save(user);
    }

    @Transactional
    public void resetForgottenPassword(AuthDtos.ResetPasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword()))
            throw new ValidationException("Passwords do not match");
        validatePassword(req.getNewPassword());
        var user = userRepo.findByEmail(req.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!encoder.matches(req.getSecretAnswer(), user.getSecretAnswer()))
            throw new ValidationException("Incorrect secret answer");
        user.setPassword(encoder.encode(req.getNewPassword()));
        userRepo.save(user);
    }

    public byte[] exportUsersToExcel() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Users");
            String[] cols = {"ID","Username","Email","First Name","Last Name","Active","Created At"};
            Row header = sheet.createRow(0);
            for (int i=0;i<cols.length;i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                CellStyle style = wb.createCellStyle();
                Font font = wb.createFont(); font.setBold(true); style.setFont(font);
                cell.setCellStyle(style);
            }
            int rowIdx=1;
            for (var user : userRepo.findAll()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(user.getEmail());
                row.createCell(3).setCellValue(user.getFirstName()!=null?user.getFirstName():"");
                row.createCell(4).setCellValue(user.getLastName()!=null?user.getLastName():"");
                row.createCell(5).setCellValue(user.isActive()?"Yes":"No");
                row.createCell(6).setCellValue(user.getCreatedAt()!=null?user.getCreatedAt().toString():"");
            }
            for (int i=0;i<cols.length;i++) sheet.autoSizeColumn(i);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    private void validatePassword(String password) {
        if (!STRONG_PASSWORD.matcher(password).matches())
            throw new ValidationException("Password must be 8+ chars with uppercase, lowercase, digit and special char (@#$%^&+=!)");
    }

    private User findUser(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found: "+id));
    }

    public UserDto toDto(User user) {
        var dto = UserDto.builder()
            .id(user.getId()).username(user.getUsername()).email(user.getEmail())
            .firstName(user.getFirstName()).lastName(user.getLastName())
            .phoneNumber(user.getPhoneNumber()).profileTheme(user.getProfileTheme())
            .active(user.isActive()).firstLogin(user.isFirstLogin())
            .accountLocked(user.isAccountLocked())
            .createdAt(user.getCreatedAt()).lastLogin(user.getLastLogin()).build();
        if (user.getRoleGroups()!=null) {
            dto.setAllRoles(user.getAllRoles().stream().map(Role::getName).collect(Collectors.toSet()));
            dto.setAllPermissions(user.getAllPermissions().stream().map(Permission::getName).collect(Collectors.toSet()));
        }
        return dto;
    }
}