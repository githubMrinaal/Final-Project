package com.app.workflow_app.service;

import com.app.workflow_app.exception.ResourceNotFoundException;
import com.app.workflow_app.model.AuditLog;
import com.app.workflow_app.model.Request;
import com.app.workflow_app.repository.AuditLogRepository;
import com.app.workflow_app.repository.RequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final RequestRepository requestRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, RequestRepository requestRepository) {
        this.auditLogRepository = auditLogRepository;
        this.requestRepository = requestRepository;
    }

    public List<AuditLog> getLogsForRequest(Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));

        return auditLogRepository.findByRequest(request);
    }
}
