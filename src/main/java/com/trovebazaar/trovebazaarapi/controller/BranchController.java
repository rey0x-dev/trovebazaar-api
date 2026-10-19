package com.trovebazaar.trovebazaarapi.controller;

import com.trovebazaar.trovebazaarapi.dto.BranchResponse;
import com.trovebazaar.trovebazaarapi.dto.CreateBranchRequest;
import com.trovebazaar.trovebazaarapi.dto.UpdateBranchRequest;
import com.trovebazaar.trovebazaarapi.service.BranchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @PostMapping
    public ResponseEntity<BranchResponse> createBranch(@RequestBody @Valid CreateBranchRequest createBranchRequest) {

        BranchResponse branchResponse = branchService.createBranch(createBranchRequest);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/api/branches/{id}")
                .buildAndExpand(branchResponse.branchId())
                .toUri();

        return ResponseEntity.created(uri).body(branchResponse);
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<BranchResponse> archiveBranch(@PathVariable Long id){

        BranchResponse branchResponse = branchService.archiveBranch(id);

        return ResponseEntity.ok(branchResponse);
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<BranchResponse> reactivateBranch(@PathVariable Long id){

        BranchResponse branchResponse = branchService.reactivateBranch(id);

        return ResponseEntity.ok(branchResponse);
    }

    @GetMapping("/archived")
    public ResponseEntity<List<BranchResponse>> getArchivedBranches(){

        List<BranchResponse> branchResponses = branchService.getAllArchivedBranches();
        return ResponseEntity.ok(branchResponses);
    }

    @GetMapping("/active")
    public ResponseEntity<List<BranchResponse>> getActiveBranches(){

        List<BranchResponse> branchResponses = branchService.getAllActiveBranches();
        return ResponseEntity.ok(branchResponses);
    }

    @GetMapping
    public ResponseEntity<List<BranchResponse>> getAllBranches(){

        List<BranchResponse> branchResponses = branchService.findAllBranches();
        return ResponseEntity.ok(branchResponses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BranchResponse> updateBranch(@PathVariable Long id, @RequestBody @Valid UpdateBranchRequest updateBranchRequest) {

        BranchResponse branchResponse = branchService.updateBranch(id, updateBranchRequest);
        return ResponseEntity.ok(branchResponse);
    }
}
