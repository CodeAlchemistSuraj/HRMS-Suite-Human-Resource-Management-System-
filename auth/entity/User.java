package com.hrms.auth.entity;

import com.hrms.auth.enums.MfaMethod;
import com.hrms.auth.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "password_hash")
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
    
    @Column(name = "employee_id")
    private UUID employeeId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "mfa_method")
    private MfaMethod mfaMethod = MfaMethod.NONE;
    
    @Column(name = "mfa_secret")
    private String mfaSecret;
    
    @Column(name = "failed_login_count")
    private int failedLoginCount = 0;
    
    @Column(name = "last_failed_login_at")
    private Instant lastFailedLoginAt;
    
    @Column(name = "last_login_at")
    private Instant lastLoginAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    // Business behaviors
    public boolean checkPassword(String plainPassword) {
        // Implementation will use PasswordEncoderUtil
        return true; // Placeholder
    }
    
    public void incrementFailedLogin() {
        this.failedLoginCount++;
        this.lastFailedLoginAt = Instant.now();
    }
    
    public void resetFailedLogin() {
        this.failedLoginCount = 0;
        this.lastFailedLoginAt = null;
    }
    
    public void lock() {
        this.status = UserStatus.LOCKED;
    }
    
    public void enable() {
        this.status = UserStatus.ACTIVE;
    }
    
    public void disable() {
        this.status = UserStatus.DISABLED;
    }
    
    public void setMfa(MfaMethod method, String secret) {
        this.mfaMethod = method;
        this.mfaSecret = secret; // Should be encrypted
    }
}