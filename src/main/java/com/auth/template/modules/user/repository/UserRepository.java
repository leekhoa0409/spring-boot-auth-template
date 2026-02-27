package com.auth.template.modules.user.repository;

import com.auth.template.common.constant.AuthProvider;
import com.auth.template.modules.user.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts WHERE u.id = :userId")
    void updateFailedAttempts(@Param("userId") UUID userId, @Param("attempts") int attempts);

    @Modifying
    @Query("UPDATE User u SET u.accountLocked = :locked, u.lockTime = :lockTime WHERE u.id = :userId")
    void updateAccountLockStatus(@Param("userId") UUID userId, @Param("locked") boolean locked,
            @Param("lockTime") java.time.LocalDateTime lockTime);
}
