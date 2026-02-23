package com.example.isp_backend;

import com.example.isp_backend.entity.*;
import com.example.isp_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.count() == 0) {

            Organization org = new Organization();
            org.setName("Default ISP Org");
            org.setCreatedAt(LocalDateTime.now());
            org.setUpdatedAt(LocalDateTime.now());
            organizationRepository.save(org);

            User admin = new User();
            admin.setEmail("admin@isp.com");
            admin.setFullName("System Admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);

            Membership membership = new Membership();
            membership.setOrganization(org);
            membership.setUser(admin);
            membership.setRole(Role.ADMIN);
            membershipRepository.save(membership);

            System.out.println("Default Admin Created ✅");
        }
    }
}
