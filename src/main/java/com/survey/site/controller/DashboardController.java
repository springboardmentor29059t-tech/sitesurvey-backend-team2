package com.survey.site.controller;

import com.survey.site.dto.DashboardStats;
import com.survey.site.service.DashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public DashboardStats getDashboardStats() {
        return dashboardService.getStats();
    }
}