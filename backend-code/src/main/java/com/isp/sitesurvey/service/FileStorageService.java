package com.isp.sitesurvey.service;

import com.isp.sitesurvey.entity.FileEntity;
import com.isp.sitesurvey.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileRepository fileRepository;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/png", "image/jpeg", "image/jpg", "application/pdf");

    @Transactional
    public FileEntity storeFile(MultipartFile file, String ownerType, Long ownerId) 
            throws IOException {
        log.info("Storing file: {} for {}:{}", file.getOriginalFilename(), ownerType, ownerId);
        validateFile(file);
        
        FileEntity entity = new FileEntity();
        entity.setOwnerType(ownerType);
        entity.setOwnerId(ownerId);
        entity.setFilename(file.getOriginalFilename());
        entity.setContentType(file.getContentType());
        entity.setFileData(file.getBytes());
        entity.setFileSize(file.getSize());
        
        return fileRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public FileEntity getFile(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found: " + fileId));
    }

    @Transactional
    public void deleteFile(Long fileId) {
        fileRepository.deleteById(fileId);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new RuntimeException("File is empty");
        if (file.getSize() > MAX_FILE_SIZE) throw new RuntimeException("File too large. Max: 10MB");
        if (!ALLOWED_TYPES.contains(file.getContentType())) 
            throw new RuntimeException("Invalid file type. Allowed: PNG, JPEG, PDF");
    }
}