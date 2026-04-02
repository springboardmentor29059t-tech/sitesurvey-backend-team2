package com.survey.site.service;

import com.survey.site.model.Organization;
import com.survey.site.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;

    // Create Organization
    public Organization createOrganization(Organization org) {
        return organizationRepository.save(org);
    }

    // View all organizations
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    // Assign admin
    public void assignAdmin(Long orgId, Long adminId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        org.setAdminId(adminId);
        organizationRepository.save(org);
    }

    public void deleteOrganization(Long id) {

        organizationRepository.deleteById(id);

    }
}