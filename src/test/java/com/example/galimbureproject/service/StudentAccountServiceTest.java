package com.example.galimbureproject.service;

import com.example.galimbureproject.dto.ChangePasswordForm;
import com.example.galimbureproject.dto.StudentProfileForm;
import com.example.galimbureproject.model.RegisteredUser;
import com.example.galimbureproject.model.UserRole;
import com.example.galimbureproject.repository.RegisteredUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentAccountServiceTest {

    @Mock
    private RegisteredUserRepository registeredUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentAccountService studentAccountService;

    @Test
    void updateProfileTrimsValuesAndNormalizesEmail() {
        RegisteredUser student = buildStudent("old@example.com", "old-hash");
        when(registeredUserRepository.findById(7L)).thenReturn(Optional.of(student));
        when(registeredUserRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(registeredUserRepository.save(any(RegisteredUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentProfileForm form = new StudentProfileForm();
        form.setFullName("  New Name  ");
        form.setEmail(" NEW@example.com ");
        form.setPhone(" 0777 123456 ");
        form.setAddress(" 12 Main Street ");

        RegisteredUser updated = studentAccountService.updateProfile(7L, form);

        assertEquals("New Name", updated.getFullName());
        assertEquals("new@example.com", updated.getEmail());
        assertEquals("0777 123456", updated.getPhone());
        assertEquals("12 Main Street", updated.getAddress());
        verify(registeredUserRepository).save(student);
    }

    @Test
    void changePasswordEncodesNewPasswordAfterCurrentPasswordMatches() {
        RegisteredUser student = buildStudent("student@example.com", "old-hash");
        when(registeredUserRepository.findById(7L)).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("Current123", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword123", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword123")).thenReturn("new-hash");
        when(registeredUserRepository.save(any(RegisteredUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("Current123");
        form.setNewPassword("NewPassword123");

        RegisteredUser updated = studentAccountService.changePassword(7L, form);

        assertEquals("new-hash", updated.getPasswordHash());
        verify(registeredUserRepository).save(student);
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() {
        RegisteredUser student = buildStudent("student@example.com", "old-hash");
        when(registeredUserRepository.findById(7L)).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("Wrong123", "old-hash")).thenReturn(false);

        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("Wrong123");
        form.setNewPassword("NewPassword123");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> studentAccountService.changePassword(7L, form)
        );

        assertEquals("Current password is incorrect.", exception.getMessage());
        verify(registeredUserRepository, never()).save(any());
    }

    private RegisteredUser buildStudent(String email, String passwordHash) {
        RegisteredUser student = new RegisteredUser();
        ReflectionTestUtils.setField(student, "id", 7L);
        student.setRole(UserRole.STUDENT);
        student.setEmail(email);
        student.setPasswordHash(passwordHash);
        student.setFullName("Student Name");
        student.setPhone("0777 123456");
        student.setAddress("12 Main Street");
        return student;
    }
}
