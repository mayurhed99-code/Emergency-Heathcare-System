package com.emergency.platform.repository;

import com.emergency.platform.entity.AppUser;
import com.emergency.platform.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    List<AppUser> findByRole(Role role);
}
