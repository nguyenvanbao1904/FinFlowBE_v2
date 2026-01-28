package com.finflow.backend.modules.identity.internal.security;

import com.finflow.backend.modules.identity.domain.entity.User;
import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Infrastructure Service: Custom User Details Service
 * 
 * WHY Infrastructure Service? ✅ VALID theo FEATURE_DEVELOPMENT_GUIDE:
 * - Framework integration (implements Spring Security's UserDetailsService interface)
 * - Technical concern: Bridge between our User entity and Spring Security
 * - Required by Spring Security for authentication
 * 
 * Responsibilities:
 * - Load user from database by username
 * - Transform our User entity to Spring Security's UserDetails
 * - Map roles to Spring Security authorities
 * 
 * Pattern: Spring Security Framework → This Service → Repository
 * NOT a UseCase because it's framework infrastructure, not business logic
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username for Spring Security authentication
     * 
     * @param username User's username
     * @return UserDetails object for Spring Security
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Find user in database
        // 1. Find user in database by Username OR Email
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 2. Transform our User entity to Spring Security UserDetails
        // Add "ROLE_" prefix as per Spring Security convention
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                        .collect(Collectors.toList()))
                .accountLocked(!user.getIsActive())
                .build();
    }
}