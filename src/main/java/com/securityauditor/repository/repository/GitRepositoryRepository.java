package com.securityauditor.repository.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securityauditor.repository.entity.GitRepository;

public interface GitRepositoryRepository
        extends JpaRepository<GitRepository, Long> {

    Optional<GitRepository> findByIdAndUser_Email(
            Long id,
            String email);
}