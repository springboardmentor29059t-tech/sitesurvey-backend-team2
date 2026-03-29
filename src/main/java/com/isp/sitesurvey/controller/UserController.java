package com.isp.sitesurvey.controller;

import com.isp.sitesurvey.entity.User;
import com.isp.sitesurvey.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * User Controller
 * Handles user profile updates and profile picture uploads.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
public class UserController {

    private final UserRepository userRepository;

    /**
     * Helper method to securely get the currently logged-in user
     */
    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database: " + email));
    }

    /**
     * Update Profile Details (Text Fields & Preferences)
     * PUT /api/users/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody Map<String, Object> updates) {
        try {
            User user = getCurrentAuthenticatedUser();
            log.info("Updating profile details for user: {}", user.getEmail());
            
            if (updates.containsKey("fullName") && updates.get("fullName") != null) {
                user.setFullName((String) updates.get("fullName"));
            }
            if (updates.containsKey("phoneNumber") && updates.get("phoneNumber") != null) {
                user.setPhoneNumber((String) updates.get("phoneNumber"));
            }
            if (updates.containsKey("receiveEmailAlerts") && updates.get("receiveEmailAlerts") != null) {
                user.setReceiveEmailAlerts((Boolean) updates.get("receiveEmailAlerts"));
            }
            
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
            
        } catch (Exception e) {
            log.error("Error updating profile details: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Upload Profile Picture
     * POST /api/users/profile/image
     */
    @PostMapping("/profile/image")
    public ResponseEntity<Map<String, String>> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        try {
            User user = getCurrentAuthenticatedUser();
            String uploadDir = "uploads/profiles/";
            Path uploadPath = Paths.get(uploadDir);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = "user_" + user.getId() + "_" + System.currentTimeMillis() + getFileExtension(file.getOriginalFilename());
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "http://localhost:8080/uploads/profiles/" + fileName;
            user.setProfilePictureUrl(fileUrl);
            userRepository.save(user);

            Map<String, String> response = new HashMap<>();
            response.put("profilePictureUrl", fileUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error uploading profile picture: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Helper method to extract file extension (e.g., .png, .jpg)
     */
    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.lastIndexOf(".") > 0) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return ".jpg"; // Default fallback
    }
}