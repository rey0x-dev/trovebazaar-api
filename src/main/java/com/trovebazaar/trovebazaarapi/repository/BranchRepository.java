package com.trovebazaar.trovebazaarapi.repository;

import com.trovebazaar.trovebazaarapi.model.Branch;
import com.trovebazaar.trovebazaarapi.model.BranchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findByStatus(BranchStatus status);
    Optional<Branch> findByName(String name);
}
