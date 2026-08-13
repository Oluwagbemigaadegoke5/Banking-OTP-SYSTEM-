package org.example.repository;

import org.example.model.AppUser; // Replace with your actual User entity path
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    // This allows Spring Data to automatically implement the database query
    Optional<AppUser> findByUsername(String username);
}