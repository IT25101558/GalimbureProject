package com.example.galimbureproject.service;

import com.example.galimbureproject.dto.RegistrationForm;
import com.example.galimbureproject.model.Batch;
import com.example.galimbureproject.model.RegisteredUser;
import com.example.galimbureproject.model.UserRole;
import com.example.galimbureproject.repository.RegisteredUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegisteredUserRepository registeredUserRepository;

    @Mock
    private BatchService batchService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void registerStoresSelectedBatchYearForStudent() {
        RegistrationForm form = new RegistrationForm();
        form.setFullName("  Alice Student  ");
        form.setEmail("Alice@example.com");
        form.setPhone(" 0777 123456 ");
        form.setAddress(" 12 Main Street ");
        form.setPassword("Password123");
        form.setBatchId(99L);

        Batch batch = new Batch();
        batch.setBatchYear(2026);
        batch.setPlace("Gampaha");

        when(registeredUserRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        when(batchService.findById(99L)).thenReturn(Optional.of(batch));
        when(passwordEncoder.encode("Password123")).thenReturn("encoded-password");
        when(registeredUserRepository.save(any(RegisteredUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisteredUser saved = registrationService.register(form);

        assertEquals("Alice Student", saved.getFullName());
        assertEquals("alice@example.com", saved.getEmail());
        assertEquals("0777 123456", saved.getPhone());
        assertEquals("12 Main Street", saved.getAddress());
        assertEquals(UserRole.STUDENT, saved.getRole());
        assertSame(batch, saved.getBatch());
        assertEquals(2026, saved.getBatchYear());
        assertEquals("encoded-password", saved.getPasswordHash());

        verify(batchService).findById(99L);
        verify(registeredUserRepository).save(any(RegisteredUser.class));
    }
}
