package com.careeros.career.repository;

import com.careeros.career.entity.CompanyPreparation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyPreparationRepository extends JpaRepository<CompanyPreparation, UUID> {

    @EntityGraph(attributePaths = {"company"})
    List<CompanyPreparation> findAllByUserId(UUID userId);

    @EntityGraph(attributePaths = {"company"})
    Optional<CompanyPreparation> findByIdAndUserId(UUID id, UUID userId);

    Optional<CompanyPreparation> findByUserIdAndCompanyId(UUID userId, UUID companyId);
}
