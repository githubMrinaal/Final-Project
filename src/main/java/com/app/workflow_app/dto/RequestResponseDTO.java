package com.app.workflow_app.dto;

import com.app.workflow_app.model.RequestStatus;
import com.app.workflow_app.model.RequestType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestResponseDTO {

    private Long id;
    private String title;
    private String description;
    private RequestType type;
    private RequestStatus status;
    private LocalDateTime createdAt;

    private String requesterName;

    // Decision fields
    private String decisionComment;
    private LocalDateTime decidedAt;
    private String decidedByName;

    // Leave-specific fields
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfDays;

    // Budget-specific fields
    private BigDecimal amount;
    private String currency;
}
