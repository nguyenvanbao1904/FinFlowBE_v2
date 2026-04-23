package com.finflow.backend.identity.infrastructure.security;

import com.finflow.backend.identity.domain.entity.User;
import com.finflow.backend.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> {
                            Set<GrantedAuthority> authorities = new HashSet<>();
                            authorities.add(new SimpleGrantedAuthority(normalizeRole(role.getName())));
                            if (role.getPermissions() != null) {
                                role.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getName())));
                            }
                            return authorities;
                        })
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()))
                .accountLocked(!user.getIsActive())
                .build();
    }

    private String normalizeRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return "ROLE_USER";
        }
        return roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
    }
}
