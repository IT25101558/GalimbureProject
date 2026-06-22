package com.example.galimbureproject.service;

import com.example.galimbureproject.model.RegisteredUser;
import com.example.galimbureproject.model.UserRole;
import com.example.galimbureproject.repository.RegisteredUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserRoleBackfillService {

    private final RegisteredUserRepository registeredUserRepository;

    public UserRoleBackfillService(RegisteredUserRepository registeredUserRepository) {
        this.registeredUserRepository = registeredUserRepository;
    }

    @Transactional
    public void backfillMissingRoles() {
        List<RegisteredUser> usersWithoutRole = registeredUserRepository.findByRoleIsNull();
        if (usersWithoutRole.isEmpty()) {
            return;
        }

        usersWithoutRole.forEach(user -> user.setRole(UserRole.STUDENT));
        registeredUserRepository.saveAll(usersWithoutRole);
    }
}
