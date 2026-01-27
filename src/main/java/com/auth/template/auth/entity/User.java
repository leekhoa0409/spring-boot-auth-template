package com.auth.template.auth.entity;

import com.auth.template.common.constant.AuthProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * User Entity
 * Represents a user in the system
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String password; // Nullable for OAuth2 users

    @Column(length = 100)
    private String fullName;

    @Column(length = 255)
    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean accountLocked = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column
    private LocalDateTime lockTime;

    @Column
    private LocalDateTime lastLoginAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(length = 100)
    private String providerId; // OAuth2 provider user ID

    @Column(length = 50)
    @Builder.Default
    private String role = "ROLE_USER";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Check if account is locked and should be unlocked
     */
    public boolean shouldUnlock() {
        if (!accountLocked || lockTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(lockTime.plusMinutes(30));
    }

    /**
     * Unlock account
     */
    public void unlock() {
        this.accountLocked = false;
        this.failedLoginAttempts = 0;
        this.lockTime = null;
    }

    /**
     * Increment failed login attempts and lock if necessary
     */
    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.accountLocked = true;
            this.lockTime = LocalDateTime.now();
        }
    }

    /**
     * Reset failed login attempts
     */
    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
    }
}
