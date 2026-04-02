package com.survey.site.controller;

import com.survey.site.model.SurveyResponse;
import com.survey.site.repository.SurveyResponseRepository;
import com.survey.site.repository.FloorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin("*")
public class ReportController {

    @Autowired
    private SurveyResponseRepository responseRepo;

    @Autowired
    private FloorRepository floorRepo;

    @GetMapping("/generate-pdf")
    public ResponseEntity<byte[]> generatePdf() {

        try {

            List<SurveyResponse> responses = responseRepo.findAll();

            // 🔥 GROUP: FLOOR → SPACE → QUESTIONS
            Map<String, Map<String, List<SurveyResponse>>> grouped =
                    responses.stream().collect(Collectors.groupingBy(
                            r -> r.getSpace() != null && r.getSpace().getFloor() != null
                                    ? r.getSpace().getFloor().getName()
                                    : "Unknown Floor",
                            Collectors.groupingBy(
                                    r -> r.getSpace() != null
                                            ? r.getSpace().getName()
                                            : "No Space"
                            )
                    ));

            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);

            document.add(new Paragraph("Survey Report", titleFont));
            document.add(new Paragraph(" "));

            for (String floor : grouped.keySet()) {

                document.add(new Paragraph("Floor: " + floor, sectionFont));
                document.add(new Paragraph(" "));

                Map<String, List<SurveyResponse>> spaces = grouped.get(floor);

                for (String space : spaces.keySet()) {

                    document.add(new Paragraph("Space: " + space));
                    document.add(new Paragraph("----------------------------"));

                    for (SurveyResponse r : spaces.get(space)) {

                        String q = r.getQuestion() != null
                                ? r.getQuestion().getQuestion()
                                : "No Question";

                        document.add(new Paragraph("Q: " + q));
                        document.add(new Paragraph("Ans: " + r.getAnswer()));
                        document.add(new Paragraph("Remarks: " + r.getRemarks()));

                        document.add(new Paragraph(" "));
                    }

                    document.add(new Paragraph(" "));
                }
            }

            document.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}