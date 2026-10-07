package com.securityauditor.scan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securityauditor.scan.entity.Scan;

public interface ScanRepository extends JpaRepository<Scan, Long> {

}