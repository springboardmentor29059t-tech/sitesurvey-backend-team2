
package com.survey.site.controller;

import com.survey.site.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin("*")
public class AdminReportController {

    @Autowired
    private PdfService pdfService;

    @GetMapping("/generate-pdf/{floorId}")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long floorId) {

        byte[] pdf = pdfService.generateFloorPdf(floorId);

        if (pdf == null || pdf.length == 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=FloorReport.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}