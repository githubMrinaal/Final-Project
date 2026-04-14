package com.app.workflow_app.service;

import com.app.workflow_app.dto.CreateRequestDTO;
import com.app.workflow_app.dto.DecisionDTO;
import com.app.workflow_app.exception.InvalidRequestStateException;
import com.app.workflow_app.exception.ResourceNotFoundException;
import com.app.workflow_app.exception.UnauthorizedActionException;
import com.app.workflow_app.model.*;
import com.app.workflow_app.repository.AuditLogRepository;
import com.app.workflow_app.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final AuditLogRepository auditLogRepository;

    public RequestService(RequestRepository requestRepository, AuditLogRepository auditLogRepository) {
        this.requestRepository = requestRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public Request createRequest(CreateRequestDTO dto, User requester) {
        Request request = new Request();
        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());
        request.setType(dto.getType());
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        request.setRequester(requester);

        if (dto.getType() == RequestType.LEAVE) {
            request.setStartDate(dto.getStartDate());
            request.setEndDate(dto.getEndDate());
            request.setNumberOfDays(dto.getNumberOfDays());
        } else if (dto.getType() == RequestType.BUDGET) {
            request.setAmount(dto.getAmount());
            request.setCurrency(dto.getCurrency());
        }

        Request saved = requestRepository.save(request);

        AuditLog log = new AuditLog();
        log.setRequest(saved);
        log.setAction(AuditAction.CREATED);
        log.setPerformedBy(requester);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);

        return saved;
    }

    public List<Request> getMyRequests(User requester) {
        return requestRepository.findByRequester(requester);
    }

    public Request getRequestById(Long id, User currentUser) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));

        boolean isRequester = request.getRequester().getId().equals(currentUser.getId());
        boolean isApprover = currentUser.getRole() == Role.APPROVER;

        if (!isRequester && !isApprover) {
            throw new UnauthorizedActionException("You are not authorized to view this request");
        }

        return request;
    }

    public List<Request> getPendingRequests() {
        return requestRepository.findByStatus(RequestStatus.PENDING);
    }

    @Transactional
    public Request approveRequest(Long id, DecisionDTO dto, User approver) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));

        if (approver.getRole() != Role.APPROVER) {
            throw new UnauthorizedActionException("Only users with APPROVER role can approve requests");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestStateException("Only PENDING requests can be approved");
        }

        request.setStatus(RequestStatus.APPROVED);
        request.setDecidedBy(approver);
        request.setDecidedAt(LocalDateTime.now());
        request.setDecisionComment(dto.getComment());

        Request saved = requestRepository.save(request);

        AuditLog log = new AuditLog();
        log.setRequest(saved);
        log.setAction(AuditAction.APPROVED);
        log.setPerformedBy(approver);
        log.setComment(dto.getComment());
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);

        return saved;
    }

    @Transactional
    public Request cancelRequest(Long id, User requester) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));

        if (!request.getRequester().getId().equals(requester.getId())) {
            throw new UnauthorizedActionException("You are not authorized to cancel this request");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestStateException("Only PENDING requests can be cancelled");
        }

        request.setStatus(RequestStatus.CANCELLED);
        request.setDecidedAt(LocalDateTime.now());

        Request saved = requestRepository.save(request);

        AuditLog log = new AuditLog();
        log.setRequest(saved);
        log.setAction(AuditAction.CANCELLED);
        log.setPerformedBy(requester);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);

        return saved;
    }

    @Transactional
    public Request rejectRequest(Long id, DecisionDTO dto, User approver) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));

        if (approver.getRole() != Role.APPROVER) {
            throw new UnauthorizedActionException("Only users with APPROVER role can reject requests");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestStateException("Only PENDING requests can be rejected");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setDecidedBy(approver);
        request.setDecidedAt(LocalDateTime.now());
        request.setDecisionComment(dto.getComment());

        Request saved = requestRepository.save(request);

        AuditLog log = new AuditLog();
        log.setRequest(saved);
        log.setAction(AuditAction.REJECTED);
        log.setPerformedBy(approver);
        log.setComment(dto.getComment());
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);

        return saved;
    }
}
