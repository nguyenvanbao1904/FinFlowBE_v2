package com.finflow.backend.modules.identity.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class) // Để tự động điền registerDate
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(unique = true, nullable = false)
    String username;

    @Column(nullable = false)
    String password;

    String firstName;
    String lastName;
    LocalDate dob;

    @Column(unique = true, nullable = false)
    String email;

    @Builder.Default
    Boolean isActive = true;

    @Builder.Default
    Boolean accountVerified = false;

    @Builder.Default
    @Column(name = "is_biometric_enabled")
    Boolean isBiometricEnabled = false;

    @CreatedDate
    @Column(updatable = false)
    LocalDateTime registerDate;

    LocalDateTime lastLogin;

    @ManyToMany(fetch = FetchType.EAGER) // Load user là load luôn role để check quyền login
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_name")
    )
    Set<Role> roles;
}