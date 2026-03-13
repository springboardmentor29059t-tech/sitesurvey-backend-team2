package com.isp.sitesurvey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Application Class for Site Survey Tool
 * 
 * This application helps Internet Service Providers plan and execute
 * network equipment installations in large properties.
 * 
 * @author ISP Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync // Enable async processing for email sending
public class SiteSurveyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiteSurveyApplication.class, args);
        System.out.println("==============================================");
        System.out.println("Site Survey Tool - Backend Started Successfully");
        System.out.println("Server running on: http://localhost:8080");
        System.out.println("API Documentation: http://localhost:8080/swagger-ui.html");
        System.out.println("==============================================");
    }
}