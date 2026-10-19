package com.trovebazaar.trovebazaarapi.tests.repository;

import com.trovebazaar.trovebazaarapi.model.Branch;
import com.trovebazaar.trovebazaarapi.model.BranchStatus;
import com.trovebazaar.trovebazaarapi.repository.BranchRepository;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class BranchRepositoryTest {

    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldCreateBranchSuccessfully() {

        Branch branch = new Branch("aName", BranchStatus.ACTIVO, "aLocation");

        Branch savedBranch = branchRepository.saveAndFlush(branch);

        entityManager.clear();

        Branch foundBranch = branchRepository.findById(savedBranch.getBranchId())
                .orElseThrow(()->new NoSuchElementException("The branch was not found"));

        assertNotNull(foundBranch.getBranchId());
        assertEquals("aName", foundBranch.getName());
        assertEquals(BranchStatus.ACTIVO, foundBranch.getStatus());
        assertEquals("aLocation", foundBranch.getLocation());
        assertNotNull(foundBranch.getCreationDate());
        assertNull(foundBranch.getArchivedAt());
    }

    @Test
    void shouldCreateBranchSuccessfullyWithDefaultCreationDate() {

        Branch branch = new Branch("aName", BranchStatus.ACTIVO, "aLocation");

        OffsetDateTime beforeCreation = OffsetDateTime.now(ZoneOffset.UTC);

        Branch savedBranch = branchRepository.saveAndFlush(branch);

        OffsetDateTime afterCreation = OffsetDateTime.now(ZoneOffset.UTC);

        entityManager.clear();

        Branch foundBranch = branchRepository.findById(savedBranch.getBranchId())
                .orElseThrow(()->new NoSuchElementException("The branch was not found"));

        OffsetDateTime createdAt = foundBranch.getCreationDate();

        assertFalse(createdAt.isBefore(beforeCreation),"Creation date should not be before the operation started");
        assertFalse(createdAt.isAfter(afterCreation),"Creation date should not be after the operation finished");
    }

    @Test
    void shouldNotAllowDuplicateBranchNames(){
        Branch branch1 = new Branch("aName", BranchStatus.ACTIVO, "aLocation");
        Branch branch2 = new Branch("aName", BranchStatus.ACTIVO, "aLocation");

        branchRepository.saveAndFlush(branch1);

        assertThrows(DataIntegrityViolationException.class, () -> branchRepository.saveAndFlush(branch2));
    }

    @Test
    void shouldReturnBranchesBasedOnStatus(){

        Branch branch1 = new Branch(
                "Coacalco Centro",
                BranchStatus.ACTIVO,
                "Coacalco de Berriozábal, Estado de México"
        );

        Branch branch2 = new Branch(
                "Plaza Las Flores",
                BranchStatus.ACTIVO,
                "Coacalco de Berriozábal, Estado de México"
        );

        Branch branch3 = new Branch(
                "Tultepec",
                BranchStatus.ACTIVO,
                "Tultepec, Estado de México"
        );

        Branch branch4 = new Branch(
                "Ecatepec Norte",
                BranchStatus.ARCHIVADO,
                "Ecatepec de Morelos, Estado de México"
        );

        branchRepository.saveAllAndFlush(
                List.of(branch1, branch2, branch3, branch4)
        );

        entityManager.clear();

        List<Branch> archivedBranches = branchRepository.findByStatus(BranchStatus.ARCHIVADO);
        List<Branch> activeBranches = branchRepository.findByStatus(BranchStatus.ACTIVO);

        assertEquals(1, archivedBranches.size());
        assertEquals(3, activeBranches.size());

        assertTrue(archivedBranches.stream().allMatch(branch -> branch.getStatus() == (BranchStatus.ARCHIVADO)));
        assertTrue(activeBranches.stream().allMatch(branch -> branch.getStatus() == (BranchStatus.ACTIVO)));
    }
}
