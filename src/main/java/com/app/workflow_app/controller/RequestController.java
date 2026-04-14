package com.app.workflow_app.controller;

import com.app.workflow_app.dto.CreateRequestDTO;
import com.app.workflow_app.dto.DecisionDTO;
import com.app.workflow_app.dto.RequestResponseDTO;
import com.app.workflow_app.exception.UnauthorizedActionException;
import com.app.workflow_app.model.Request;
import com.app.workflow_app.model.User;
import com.app.workflow_app.service.RequestService;
import com.app.workflow_app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    private final RequestService requestService;
    private final UserService userService;

    public RequestController(RequestService requestService, UserService userService) {
        this.requestService = requestService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<RequestResponseDTO> createRequest(@Valid @RequestBody CreateRequestDTO dto) {
        User currentUser = getCurrentUser();
        Request created = requestService.createRequest(dto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(created));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<RequestResponseDTO>> getMyRequests() {
        User currentUser = getCurrentUser();
        List<RequestResponseDTO> result = requestService.getMyRequests(currentUser)
                .stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestResponseDTO> getRequestById(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        Request request = requestService.getRequestById(id, currentUser);
        return ResponseEntity.ok(toDTO(request));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<RequestResponseDTO>> getPendingRequests() {
        List<RequestResponseDTO> result = requestService.getPendingRequests()
                .stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<RequestResponseDTO> approveRequest(@PathVariable Long id,
                                                             @Valid @RequestBody DecisionDTO dto) {
        User currentUser = getCurrentUser();
        Request approved = requestService.approveRequest(id, dto, currentUser);
        return ResponseEntity.ok(toDTO(approved));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<RequestResponseDTO> rejectRequest(@PathVariable Long id,
                                                            @Valid @RequestBody DecisionDTO dto) {
        User currentUser = getCurrentUser();
        Request rejected = requestService.rejectRequest(id, dto, currentUser);
        return ResponseEntity.ok(toDTO(rejected));
    }

    @PostMapping("/{id}/action/cancel")
    public ResponseEntity<RequestResponseDTO> cancelRequest(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        Request cancelled = requestService.cancelRequest(id, currentUser);
        return ResponseEntity.ok(toDTO(cancelled));
    }

    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UnauthorizedActionException("Not authenticated");
        }
        String email = (String) authentication.getPrincipal();
        return userService.findByEmail(email);
    }

    private RequestResponseDTO toDTO(Request request) {
        RequestResponseDTO dto = new RequestResponseDTO();
        dto.setId(request.getId());
        dto.setTitle(request.getTitle());
        dto.setDescription(request.getDescription());
        dto.setType(request.getType());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setRequesterName(request.getRequester().getName());
        dto.setDecisionComment(request.getDecisionComment());
        dto.setDecidedAt(request.getDecidedAt());
        dto.setDecidedByName(request.getDecidedBy() != null ? request.getDecidedBy().getName() : null);
        dto.setStartDate(request.getStartDate());
        dto.setEndDate(request.getEndDate());
        dto.setNumberOfDays(request.getNumberOfDays());
        dto.setAmount(request.getAmount());
        dto.setCurrency(request.getCurrency());
        return dto;
    }
}
