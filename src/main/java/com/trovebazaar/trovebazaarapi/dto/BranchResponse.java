package com.trovebazaar.trovebazaarapi.dto;

import com.trovebazaar.trovebazaarapi.model.BranchStatus;

import java.time.OffsetDateTime;

public record BranchResponse(
        Long branchId,
        String name,
        BranchStatus status,
        String location,
        OffsetDateTime creationDate,
        OffsetDateTime archivedAt
) {
}
