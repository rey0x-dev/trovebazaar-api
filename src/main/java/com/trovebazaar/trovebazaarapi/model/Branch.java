package com.trovebazaar.trovebazaarapi.model;

import com.trovebazaar.trovebazaarapi.exception.BusinessRuleViolation;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter

@Entity
@Table(name = "branch")
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id", nullable = false, updatable = false)
    private Long branchId;
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BranchStatus status;
    @Column(name = "location", nullable = false, length = 255)
    private String location;
    @Column(name = "creation_date", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime creationDate;
    @Column(name = "archived_at", nullable = true)
    private OffsetDateTime archivedAt;

    public Branch(String name, BranchStatus status, String location) {
        this.name = name;
        this.status = status;
        this.location = location;
    }

    public Branch(Long branchId, String name, BranchStatus status, String location) {
        this.branchId = branchId;
        this.name = name;
        this.status = status;
        this.location = location;
    }

    public void archive(){

        if (status == BranchStatus.ARCHIVADO) {
            throw new BusinessRuleViolation("Branch already archived");
        }

        this.status = BranchStatus.ARCHIVADO;
        this.archivedAt = OffsetDateTime.now();
    }

    public void reactivate(){
        if (status == BranchStatus.ACTIVO) {
            throw new BusinessRuleViolation("Branch already active");
        }

        this.status = BranchStatus.ACTIVO;
        this.archivedAt = null;
    }

    public void updateBranch(String name, String location) {

        this.name = name;
        this.location = location;
    }
}
