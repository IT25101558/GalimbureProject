package com.example.galimbureproject.service;

import com.example.galimbureproject.dto.ChangePasswordForm;
import com.example.galimbureproject.dto.StudentProfileForm;
import com.example.galimbureproject.model.RegisteredUser;
import com.example.galimbureproject.model.UserRole;
import com.example.galimbureproject.repository.RegisteredUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentAccountService {

    private final RegisteredUserRepository registeredUserRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentAccountService(
            RegisteredUserRepository registeredUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.registeredUserRepository = registeredUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public RegisteredUser requireStudentById(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student account was not found.");
        }

        RegisteredUser student = registeredUserRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student account was not found."));

        if (student.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("Student account was not found.");
        }

        return student;
    }

    @Transactional
    public RegisteredUser updateProfile(Long studentId, StudentProfileForm form) {
        RegisteredUser student = requireStudentById(studentId);
        String email = normalizeEmail(form.getEmail());
        String currentEmail = student.getEmail() == null ? "" : student.getEmail().trim().toLowerCase();

        if (!currentEmail.equals(email) && registeredUserRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("This email is already registered.");
        }

        student.setFullName(form.getFullName().trim());
        student.setEmail(email);
        student.setPhone(form.getPhone().trim());
        student.setAddress(form.getAddress().trim());
        return registeredUserRepository.save(student);
    }

    @Transactional
    public RegisteredUser changePassword(Long studentId, ChangePasswordForm form) {
        RegisteredUser student = requireStudentById(studentId);
        if (!passwordEncoder.matches(form.getCurrentPassword(), student.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        if (passwordEncoder.matches(form.getNewPassword(), student.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from the current password.");
        }

        student.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        return registeredUserRepository.save(student);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
