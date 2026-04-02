package com.survey.site.controller;

import com.survey.site.service.BulkImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/import")
@CrossOrigin("*")
public class BulkImportController {

    @Autowired
    private BulkImportService service;

    @PostMapping("/csv")
    public String uploadCSV(@RequestParam("file") MultipartFile file) {
        return service.importCSV(file);
    }

    @PostMapping("/excel")
    public String uploadExcel(@RequestParam("file") MultipartFile file) {
        return service.importExcel(file);
    }
}