package com.app.workflow_app.repository;

import com.app.workflow_app.model.AuditLog;
import com.app.workflow_app.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByRequest(Request request);
}
