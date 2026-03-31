package com.isp.sitesurvey.controller;

import com.isp.sitesurvey.entity.FileEntity;
import com.isp.sitesurvey.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000"})
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("ownerType") String ownerType,
            @RequestParam("ownerId") Long ownerId) {
        
        try {
            FileEntity saved = fileStorageService.storeFile(file, ownerType, ownerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileId", saved.getId());
            response.put("filename", saved.getFilename());
            response.put("fileSize", saved.getFileSize());
            response.put("message", "File uploaded successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable("fileId") Long fileId) {
        FileEntity file = fileStorageService.getFile(fileId);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file.getFileData());
    }

    @GetMapping("/{fileId}/preview")
    public ResponseEntity<byte[]> previewFile(@PathVariable("fileId") Long fileId) {
        FileEntity file = fileStorageService.getFile(fileId);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(file.getFileData());
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable("fileId") Long fileId) {
        fileStorageService.deleteFile(fileId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "File deleted successfully");
        
        return ResponseEntity.ok(response);
    }
}