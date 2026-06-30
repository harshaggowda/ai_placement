package com.careeros.career.repository;

import com.careeros.career.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    List<Certificate> findAllByUserId(UUID userId);
    Optional<Certificate> findByIdAndUserId(UUID id, UUID userId);
}
