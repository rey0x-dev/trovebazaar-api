package com.trovebazaar.trovebazaarapi.service;

import com.trovebazaar.trovebazaarapi.dto.CreateBranchRequest;
import com.trovebazaar.trovebazaarapi.dto.BranchResponse;
import com.trovebazaar.trovebazaarapi.dto.UpdateBranchRequest;
import com.trovebazaar.trovebazaarapi.exception.BusinessRuleViolation;
import com.trovebazaar.trovebazaarapi.exception.ResourceNotFoundException;
import com.trovebazaar.trovebazaarapi.model.Branch;
import com.trovebazaar.trovebazaarapi.model.BranchStatus;
import com.trovebazaar.trovebazaarapi.repository.BranchRepository;
import com.trovebazaar.trovebazaarapi.utility.TextNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class BranchService {

    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public BranchResponse createBranch(CreateBranchRequest createBranchRequest) {

        String name = TextNormalizer.normalizeText(createBranchRequest.name());
        String location = TextNormalizer.normalizeText(createBranchRequest.location());

        if (branchRepository.findByName(name).isPresent()) {
            throw new BusinessRuleViolation("Branch with name " + name + " already exists");
        }

        Branch branch = new Branch(name, BranchStatus.ACTIVO, location);

        Branch savedBranch = branchRepository.save(branch);

        return convertIntoBranchResponse(savedBranch);
    }

    private BranchResponse convertIntoBranchResponse(Branch branch) {
        return new BranchResponse(branch.getBranchId(), branch.getName(), branch.getStatus(),
                branch.getLocation(), branch.getCreationDate(), branch.getArchivedAt());
    }

    @Transactional
    public BranchResponse archiveBranch(Long branchId) {

        Branch foundBranch = findBranchEntityById(branchId);

        foundBranch.archive();
        branchRepository.save(foundBranch);
        return convertIntoBranchResponse(foundBranch);
    }

    @Transactional
    public BranchResponse reactivateBranch(Long branchId) {

        Branch foundBranch = findBranchEntityById(branchId);

        foundBranch.reactivate();
        branchRepository.save(foundBranch);
        return convertIntoBranchResponse(foundBranch);
    }

    public List<BranchResponse> getAllArchivedBranches() {
        return branchRepository.findByStatus(BranchStatus.ARCHIVADO)
                .stream()
                .map(this::convertIntoBranchResponse)
                .toList();
    }

    public List<BranchResponse> getAllActiveBranches() {
        return branchRepository.findByStatus(BranchStatus.ACTIVO)
                .stream()
                .map(this::convertIntoBranchResponse)
                .toList();
    }

    public List<BranchResponse> findAllBranches() {

        return branchRepository.findAll().stream()
                .map(this::convertIntoBranchResponse)
                .toList();
    }

    private Branch findBranchEntityById(Long branchId) {

        return branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch with id "+branchId+" does not exist"));
    }

    @Transactional
    public BranchResponse updateBranch(Long branchId, UpdateBranchRequest updateBranchRequest) {

        Branch foundBranch = findBranchEntityById(branchId);

        String name = TextNormalizer.normalizeText(updateBranchRequest.name());

        branchRepository.findByName(name)
                .filter(branch -> !branch.getBranchId().equals(branchId))
                .ifPresent(branch -> {
                    throw new BusinessRuleViolation(
                            "Branch with name " + name + " already exists");
                });

        String location = TextNormalizer.normalizeText(updateBranchRequest.location());

        foundBranch.updateBranch(name, location);

        return convertIntoBranchResponse(foundBranch);
    }
}
