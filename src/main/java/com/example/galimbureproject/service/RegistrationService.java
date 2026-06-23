package com.example.galimbureproject.service;

import com.example.galimbureproject.dto.RegistrationForm;
import com.example.galimbureproject.model.Batch;
import com.example.galimbureproject.model.RegisteredUser;
import com.example.galimbureproject.model.UserRole;
import com.example.galimbureproject.repository.RegisteredUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final RegisteredUserRepository registeredUserRepository;
    private final BatchService batchService;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            RegisteredUserRepository registeredUserRepository,
            BatchService batchService,
            PasswordEncoder passwordEncoder
    ) {
        this.registeredUserRepository = registeredUserRepository;
        this.batchService = batchService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisteredUser register(RegistrationForm form) {
        String email = form.getEmail().trim().toLowerCase();
        if (registeredUserRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("This email is already registered.");
        }

        RegisteredUser user = new RegisteredUser();
        user.setFullName(form.getFullName().trim());
        user.setEmail(email);
        user.setPhone(form.getPhone().trim());
        user.setAddress(form.getAddress().trim());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRole(UserRole.STUDENT);
        Long batchId = form.getBatchId();
        if (batchId == null) {
            throw new IllegalArgumentException("Batch is required.");
        }
        Batch batch = batchService.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Selected batch was not found."));
        user.setBatch(batch);
        user.setBatchYear(batch.getBatchYear());

        return registeredUserRepository.save(user);
    }
}
