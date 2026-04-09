package com.app.workflow_app.config;

import com.app.workflow_app.dto.CreateRequestDTO;
import com.app.workflow_app.dto.RegisterRequest;
import com.app.workflow_app.model.RequestType;
import com.app.workflow_app.model.Role;
import com.app.workflow_app.model.User;
import com.app.workflow_app.repository.UserRepository;
import com.app.workflow_app.service.RequestService;
import com.app.workflow_app.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final RequestService requestService;

    public DataInitializer(UserRepository userRepository,
                           UserService userService,
                           RequestService requestService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.requestService = requestService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User requester = userService.registerUser(
                new RegisterRequest("Test Requester", "requester@test.com", "password123", Role.REQUESTER)
        );

        userService.registerUser(
                new RegisterRequest("Test Approver", "approver@test.com", "password123", Role.APPROVER)
        );

        CreateRequestDTO leaveRequest = new CreateRequestDTO();
        leaveRequest.setTitle("Sample Leave Request");
        leaveRequest.setDescription("Annual leave");
        leaveRequest.setType(RequestType.LEAVE);
        leaveRequest.setStartDate(LocalDate.now());
        leaveRequest.setEndDate(LocalDate.now().plusDays(3));
        leaveRequest.setNumberOfDays(3);
        requestService.createRequest(leaveRequest, requester);

        CreateRequestDTO budgetRequest = new CreateRequestDTO();
        budgetRequest.setTitle("Sample Budget Request");
        budgetRequest.setDescription("Team lunch");
        budgetRequest.setType(RequestType.BUDGET);
        budgetRequest.setAmount(new BigDecimal("500.00"));
        budgetRequest.setCurrency("USD");
        requestService.createRequest(budgetRequest, requester);

        System.out.println("Test data initialized successfully");
    }
}
