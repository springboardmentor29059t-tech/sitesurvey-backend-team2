package com.isp.sitesurvey.controller;

import com.isp.sitesurvey.entity.Property;
import com.isp.sitesurvey.repository.PropertyRepository;
import com.isp.sitesurvey.repository.SurveyResponseRepository;
import com.isp.sitesurvey.repository.SpaceRepository;
import jakarta.persistence.EntityManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/finance")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
public class FinanceController {

    private final PropertyRepository propertyRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final SpaceRepository spaceRepository;
    private final EntityManager entityManager;

    public FinanceController(PropertyRepository propertyRepository, 
                             SurveyResponseRepository surveyResponseRepository,
                             SpaceRepository spaceRepository,
                             EntityManager entityManager) {
        this.propertyRepository = propertyRepository;
        this.surveyResponseRepository = surveyResponseRepository;
        this.spaceRepository = spaceRepository;
        this.entityManager = entityManager;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalProperties = propertyRepository.count();
        long completedSurveys = ((Number) entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT space_id) FROM survey_responses WHERE status = 'SUBMITTED'")
                .getSingleResult()).longValue();
        
        // Derive Revenue ($5000 per completed survey)
        double totalRevenue = completedSurveys * 5000.0;
        
        // Derive Pending Invoices (Properties without a submitted survey)
        long pendingInvoices = totalProperties - completedSurveys;
        if (pendingInvoices < 0) pendingInvoices = 0;

        stats.put("totalRevenue", totalRevenue);
        stats.put("pendingInvoices", pendingInvoices);
        stats.put("activeQuotes", totalProperties);
        stats.put("avgCostPerSurvey", 420.00); 

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<Map<String, Object>>> getRecentInvoices() {
        List<Property> properties = propertyRepository.findAll();
        
        // Limit to most recent 5 for "Recent Invoices" feel
        List<Map<String, Object>> invoices = properties.stream()
                .limit(5)
                .map(p -> {
                    Map<String, Object> inv = new HashMap<>();
                    inv.put("id", "INV-2024-" + String.format("%03d", p.getId()));
                    inv.put("property", p.getName());
                    inv.put("amount", 2500.00 + (p.getId() * 150)); // Pseudo-dynamic amount
                    
                    // Check if it's "Paid" (has a survey) or "Pending"
                    boolean hasSurvey = ((Number) entityManager.createNativeQuery(
                            "SELECT COUNT(*) FROM survey_responses sr " +
                            "JOIN spaces s ON sr.space_id = s.id " +
                            "JOIN floors f ON s.floor_id = f.id " +
                            "JOIN buildings b ON f.building_id = b.id " +
                            "WHERE b.property_id = :propId AND sr.status = 'SUBMITTED'")
                            .setParameter("propId", p.getId())
                            .getSingleResult()).longValue() > 0;
                            
                    inv.put("status", hasSurvey ? "PAID" : "PENDING");
                    inv.put("date", "2024-03-" + (10 + (p.getId() % 20))); // Dummy date
                    return inv;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/estimates")
    public ResponseEntity<List<Map<String, Object>>> getCostEstimates() {
        List<Property> properties = propertyRepository.findAll();
        
        List<Map<String, Object>> estimates = properties.stream()
                .limit(4)
                .map(p -> {
                    Map<String, Object> est = new HashMap<>();
                    est.put("id", "EST-" + p.getId());
                    est.put("site", p.getName());
                    
                    // Base estimate on number of spaces
                    long spaceCount = ((Number) entityManager.createNativeQuery(
                            "SELECT COUNT(*) FROM spaces s " +
                            "JOIN floors f ON s.floor_id = f.id " +
                            "JOIN buildings b ON f.building_id = b.id " +
                            "WHERE b.property_id = :propId")
                            .setParameter("propId", p.getId())
                            .getSingleResult()).longValue();
                            
                    est.put("estimated", 5000 + (spaceCount * 450));
                    est.put("margin", (15 + (p.getId() % 10)) + "%");
                    est.put("probability", spaceCount > 5 ? "High" : spaceCount > 2 ? "Medium" : "Low");
                    return est;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(estimates);
    }
}
