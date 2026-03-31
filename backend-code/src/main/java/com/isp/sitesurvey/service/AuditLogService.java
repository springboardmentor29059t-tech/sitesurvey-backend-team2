package com.isp.sitesurvey.service;

import com.isp.sitesurvey.entity.AuditLog;
import com.isp.sitesurvey.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(String action, String entityName, Long entityId, String details) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setPerformedBy(username);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}
