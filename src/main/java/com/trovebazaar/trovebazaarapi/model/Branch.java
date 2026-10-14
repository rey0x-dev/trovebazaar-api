package com.trovebazaar.trovebazaarapi.model;

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
    @CreationTimestamp
    @Column(name = "creation_date", nullable = false, updatable = false)
    private OffsetDateTime creationDate;
}
