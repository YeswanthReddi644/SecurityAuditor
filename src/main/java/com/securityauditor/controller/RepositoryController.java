package com.securityauditor.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securityauditor.repository.dto.CreateRepositoryRequest;
import com.securityauditor.repository.dto.GitRepositoryResponse;
import com.securityauditor.repository.service.RepositoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/repositories")
public class RepositoryController {

    private final RepositoryService repositoryService;

    public RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @PostMapping
    public ResponseEntity<GitRepositoryResponse> createRepository(
            @Valid @RequestBody CreateRepositoryRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        GitRepositoryResponse response =
                repositoryService.createRepository(request, email);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}