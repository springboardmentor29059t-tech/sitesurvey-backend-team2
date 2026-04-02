package com.survey.site.controller;

import com.survey.site.model.User;
import com.survey.site.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    // CREATE ORG ADMIN
    @PostMapping("/create-org-admin")
    public User createOrgAdmin(@RequestBody User user) {

        return userService.createOrgAdmin(user);
    }

    // GET ORG ADMIN LIST
    @GetMapping("/org-admins")
    public List<User> getOrgAdmins() {

        return userService.getOrgAdmins();
    }

    // DELETE USER
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);

        return "User deleted successfully";
    }
}