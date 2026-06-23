package com.example.galimbureproject.repository;

import com.example.galimbureproject.model.RegisteredUser;
import com.example.galimbureproject.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegisteredUserRepository extends JpaRepository<RegisteredUser, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<RegisteredUser> findByEmailIgnoreCase(String email);

    List<RegisteredUser> findAllByOrderByCreatedAtDesc();

    List<RegisteredUser> findAllByRoleOrderByFullNameAsc(UserRole role);

    List<RegisteredUser> findAllByRoleAndBatchYearOrderByFullNameAsc(UserRole role, Integer batchYear);

    List<RegisteredUser> findByRoleIsNull();

    long countByRole(UserRole role);
}
