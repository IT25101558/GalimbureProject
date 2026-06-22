package com.example.galimbureproject.service;

import com.example.galimbureproject.dto.RegistrationForm;
import com.example.galimbureproject.model.RegisteredUser;
import com.example.galimbureproject.model.UserRole;
import com.example.galimbureproject.repository.RegisteredUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final RegisteredUserRepository registeredUserRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(RegisteredUserRepository registeredUserRepository, PasswordEncoder passwordEncoder) {
        this.registeredUserRepository = registeredUserRepository;
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

        return registeredUserRepository.save(user);
    }
}
