package com.securityauditor.repository.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securityauditor.repository.entity.GitRepository;

public interface GitRepositoryRepository
        extends JpaRepository<GitRepository, Long> {

}