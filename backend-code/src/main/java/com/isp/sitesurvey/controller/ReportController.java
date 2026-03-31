package com.isp.sitesurvey.controller;

import com.isp.sitesurvey.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/property/{id}")
    public ResponseEntity<byte[]> getPropertyReport(@PathVariable("id") Long id) {
        byte[] pdf = reportService.generatePropertyReport(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=property_report_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/rf/{id}")
    public ResponseEntity<byte[]> getRfReport(@PathVariable("id") Long id) {
        byte[] pdf = reportService.generateRfReport(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rf_signal_report_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
