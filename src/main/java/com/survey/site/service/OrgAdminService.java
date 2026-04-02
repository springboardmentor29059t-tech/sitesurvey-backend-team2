package com.survey.site.service;

import com.survey.site.model.User;
import com.survey.site.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class OrgAdminService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Create engineer under this OrgAdmin
    public User createEngineer(User user) {
        // Set role as ENGINEER
        user.setRole("ENGINEER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Get all engineers
    public List<User> getEngineers() {
        return userRepository.findByRole("ENGINEER");
    }

    // Delete engineer by id
    public String deleteEngineer(Long id) {
        User engineer = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Engineer not found with id: " + id));
        userRepository.delete(engineer);
        return "Engineer deleted successfully with id: " + id;
    }
}