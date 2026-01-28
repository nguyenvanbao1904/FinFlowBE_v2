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
@Table(name = "users") // Quan trọng: Tránh xung đột từ khóa SQL
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

    @CreatedDate // Tự động lấy giờ hiện tại khi insert
    @Column(updatable = false)
    LocalDateTime registerDate;

    LocalDateTime lastLogin;

    // Quan hệ Many-to-Many với Role
    @ManyToMany(fetch = FetchType.EAGER) // Load user là load luôn role để check quyền login
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_name")
    )
    Set<Role> roles;
}