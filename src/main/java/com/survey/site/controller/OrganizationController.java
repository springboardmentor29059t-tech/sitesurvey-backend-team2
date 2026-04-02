package com.survey.site.controller;

import com.survey.site.model.Organization;
import com.survey.site.model.User;
import com.survey.site.service.OrganizationService;
import com.survey.site.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;

    // 1️⃣ Create Organization
    @PostMapping("/create")
    public Organization createOrganization(@RequestBody Organization organization) {
        return organizationService.createOrganization(organization);
    }

    // 2️⃣ View All Organizations
    @GetMapping("/all")
    public List<Organization> getAllOrganizations() {
        return organizationService.getAllOrganizations();
    }

    // 3️⃣ Assign ORG_ADMIN to Organization
    @PostMapping("/{orgId}/assign-admin/{adminId}")
    public String assignAdmin(@PathVariable Long orgId, @PathVariable Long adminId) {
        organizationService.assignAdmin(orgId, adminId);
        return "Org admin assigned successfully";
    }

    // 4️⃣ Create ORG_ADMIN
    @PostMapping("/create-org-admin")
    public User createOrgAdmin(@RequestBody User user) {
        return userService.createOrgAdmin(user);
    }

    // 5️⃣ View ORG_ADMINS
    @GetMapping("/org-admins")
    public List<User> getOrgAdmins() {
        return userService.getOrgAdmins();
    }

    // 6️⃣ Delete ORG_ADMIN
    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "User deleted successfully";
    }
    @DeleteMapping("/{id}")
    public String deleteOrganization(@PathVariable Long id) {

        organizationService.deleteOrganization(id);

        return "Organization deleted successfully";
    }
}