package com.app.workflow_app.controller;

import com.app.workflow_app.config.JwtService;
import com.app.workflow_app.dto.AuthResponse;
import com.app.workflow_app.dto.LoginRequest;
import com.app.workflow_app.dto.RegisterRequest;
import com.app.workflow_app.exception.UnauthorizedActionException;
import com.app.workflow_app.model.User;
import com.app.workflow_app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        User saved = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "email", saved.getEmail(),
                "role", saved.getRole().name()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedActionException("Invalid email or password");
        }

        return ResponseEntity.ok(new AuthResponse(jwtService.generateToken(user)));
    }
}
