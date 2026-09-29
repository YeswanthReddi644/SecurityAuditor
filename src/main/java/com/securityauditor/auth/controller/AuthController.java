package com.securityauditor.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securityauditor.auth.dto.LoginRequest;
import com.securityauditor.auth.service.AuthService;
import com.securityauditor.user.entity.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @Valid @RequestBody LoginRequest request) {

        User user = authService.login(request);

        return ResponseEntity.ok(
                "Login successful for " + user.getEmail()
        );
    }
}