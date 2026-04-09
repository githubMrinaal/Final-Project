package com.app.workflow_app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private RequestType type;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    // Decision fields
    private String decisionComment;
    private LocalDateTime decidedAt;

    @ManyToOne
    @JoinColumn(name = "decided_by_id")
    private User decidedBy;

    // Leave-specific fields (null for BUDGET)
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfDays;

    // Budget-specific fields (null for LEAVE)
    private BigDecimal amount;
    private String currency;
}
