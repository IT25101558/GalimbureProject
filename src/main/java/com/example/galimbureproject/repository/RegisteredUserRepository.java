package com.example.galimbureproject.repository;

import com.example.galimbureproject.model.RegisteredUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegisteredUserRepository extends JpaRepository<RegisteredUser, Long> {

    boolean existsByEmailIgnoreCase(String email);
}
