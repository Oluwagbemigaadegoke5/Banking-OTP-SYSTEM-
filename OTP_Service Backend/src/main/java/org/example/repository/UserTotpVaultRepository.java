package org.example.repository;
import org.example.model.UserTotpVault;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserTotpVaultRepository extends JpaRepository<UserTotpVault, Long> {
    Optional<UserTotpVault> findByUserId(Long userId);
}