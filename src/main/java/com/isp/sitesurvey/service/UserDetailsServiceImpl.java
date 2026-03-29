package com.isp.sitesurvey.service;

import com.isp.sitesurvey.entity.User;
import com.isp.sitesurvey.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * User Details Service Implementation
 * Loads user-specific data for Spring Security
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
        User user = userRepository.findByEmailOrUsername(emailOrUsername)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email or username: " + emailOrUsername));

        if (!user.getIsEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        if (user.getIsAccountLocked()) {
            throw new RuntimeException("User account is locked");
        }

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(user.getIsAccountLocked())
                .credentialsExpired(false)
                .disabled(!user.getIsEnabled())
                .build();
    }
}