package com.example.isp_backend.controller;

import com.example.isp_backend.entity.*;
import com.example.isp_backend.repository.*;
import com.example.isp_backend.config.JwtUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ================= REGISTER =================
    @PostMapping("/register")
    public Map<String, String> register(@RequestBody User request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Create User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Attach user to default organization
        Organization org = organizationRepository.findAll().get(0);

        Membership membership = new Membership();
        membership.setUser(user);
        membership.setOrganization(org);

        // ✅ SET DEFAULT ROLE AS USER
        membership.setRole(Role.ADMIN);

        membershipRepository.save(membership);

        // Auto login after register
        String token = jwtUtil.generateToken(user.getEmail());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", "USER");

        return response;
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody User request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Get role from membership
        Membership membership = membershipRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Membership not found"));

        String role = membership.getRole().name();

        // Generate JWT
        String token = jwtUtil.generateToken(user.getEmail());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", role);

        return response;
    }
}