package com.survey.site.service;

import com.survey.site.model.User;
import com.survey.site.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ---------- ORG_ADMIN ----------
    public User createOrgAdmin(User user) {
        user.setRole("ORG_ADMIN");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public List<User> getOrgAdmins() {
        return userRepository.findByRole("ORG_ADMIN");
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // ---------- ENGINEERS ----------
    public User createEngineer(User user) {
        user.setRole("ENGINEER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public List<User> getEngineers() {
        return userRepository.findByRole("ENGINEER");
    }

    public void deleteEngineer(Long id) {
        userRepository.deleteById(id);
    }
}