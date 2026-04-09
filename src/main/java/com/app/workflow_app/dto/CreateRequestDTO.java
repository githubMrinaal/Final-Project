package com.app.workflow_app.dto;

import com.app.workflow_app.model.RequestType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRequestDTO {

    private String title;
    private String description;
    private RequestType type;

    // Leave-specific fields
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfDays;

    // Budget-specific fields
    private BigDecimal amount;
    private String currency;
}
