package com.trovebazaar.trovebazaarapi.tests.service;

import com.trovebazaar.trovebazaarapi.dto.CreateBranchRequest;
import com.trovebazaar.trovebazaarapi.dto.BranchResponse;
import com.trovebazaar.trovebazaarapi.dto.UpdateBranchRequest;
import com.trovebazaar.trovebazaarapi.exception.BusinessRuleViolation;
import com.trovebazaar.trovebazaarapi.exception.ResourceNotFoundException;
import com.trovebazaar.trovebazaarapi.model.Branch;

import static com.trovebazaar.trovebazaarapi.model.BranchStatus.ACTIVO;

import com.trovebazaar.trovebazaarapi.repository.BranchRepository;
import com.trovebazaar.trovebazaarapi.service.BranchService;

import static com.trovebazaar.trovebazaarapi.model.BranchStatus.ARCHIVADO;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private BranchService branchService;

    @Test
    void shouldCreateBranchSuccessfully() {
        CreateBranchRequest createBranchRequest = new CreateBranchRequest("Test Name", "Test Location");

        when(branchRepository.findByName("Test Name"))
                .thenReturn(Optional.empty());

        when(branchRepository.save(any(Branch.class)))
                .thenAnswer(i -> i.getArgument(0));

        branchService.createBranch(createBranchRequest);

        ArgumentCaptor<Branch> branchCaptor = ArgumentCaptor.forClass(Branch.class);

        verify(branchRepository).save(branchCaptor.capture());

        Branch branch = branchCaptor.getValue();

        assertEquals("Test Name", branch.getName());
        assertEquals("Test Location", branch.getLocation());
        assertEquals(ACTIVO, branch.getStatus());
    }

    @Test
    void shouldReturnBranchResponse() {
        CreateBranchRequest createBranchRequest = new CreateBranchRequest("Test Name", "Test Location");
        OffsetDateTime createdAt = OffsetDateTime.now();
        OffsetDateTime archivedAt = OffsetDateTime.now().plusYears(2);

        Branch branch = new Branch(1L, "Test Name", ACTIVO, "Test Location", createdAt, archivedAt);

        when(branchRepository.findByName("Test Name"))
                .thenReturn(Optional.empty());

        when(branchRepository.save(any(Branch.class)))
                .thenReturn(branch);

        BranchResponse response = branchService.createBranch(createBranchRequest);

        assertEquals("Test Name", response.name());
        assertEquals("Test Location", response.location());
        assertEquals(ACTIVO, response.status());
        assertEquals(createdAt, response.creationDate());
        assertEquals(archivedAt, response.archivedAt());

        verify(branchRepository).findByName("Test Name");
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void shouldThrowBusinessRuleViolationExceptionWhenDuplicatedNameIsEntered() {
        CreateBranchRequest createBranchRequest = new CreateBranchRequest("Test Name", "Test Location");

        OffsetDateTime createdAt = OffsetDateTime.now();
        OffsetDateTime archivedAt = OffsetDateTime.now().plusYears(2);
        Branch existingBranch = new Branch(1L, "Test Name", ACTIVO, "Test Location", createdAt, archivedAt);

        when(branchRepository.findByName("Test Name"))
                .thenReturn(Optional.of(existingBranch));

        assertThrows(BusinessRuleViolation.class, () -> branchService.createBranch(createBranchRequest));

        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    void shouldRejectDuplicateNameAfterNormalization() {
        CreateBranchRequest createBranchRequest = new CreateBranchRequest(
                "   tEST     nAME   ",
                "  tEST     lOCATION  "
        );

        OffsetDateTime archivedAt = OffsetDateTime.now().plusYears(2);
        OffsetDateTime createdAt = OffsetDateTime.now();
        Branch existingBranch = new Branch(1L, "Test Name", ACTIVO, "Test Location", createdAt, archivedAt);

        when(branchRepository.findByName("Test Name"))
                .thenReturn(Optional.of(existingBranch));

        assertThrows(BusinessRuleViolation.class, () -> branchService.createBranch(createBranchRequest)
                , "Even if a non normalized name is entered the system will normalize the name before" +
                        " performing the repository's <find by name> lookup");

        verify(branchRepository, never()).save(any(Branch.class));
        verify(branchRepository).findByName("Test Name");
    }

    @Test
    void shouldSaveBranchWithNormalizedText() {
        CreateBranchRequest createBranchRequest = new CreateBranchRequest(
                "   tEST     nAME   ",
                "  tEST     lOCATION  "
        );

        when(branchRepository.findByName("Test Name"))
                .thenReturn(Optional.empty());

        when(branchRepository.save(any(Branch.class)))
                .thenAnswer(i -> i.getArgument(0));

        branchService.createBranch(createBranchRequest);

        ArgumentCaptor<Branch> branchCaptor = ArgumentCaptor.forClass(Branch.class);

        verify(branchRepository).save(branchCaptor.capture());

        Branch branch = branchCaptor.getValue();

        assertEquals("Test Name", branch.getName());
        assertEquals("Test Location", branch.getLocation());
    }

    @Test
    void shouldArchiveBranchSuccessfully() {

        Long branchId = 1L;

        Branch activeBranch = new Branch(1L, "Test Name", ACTIVO, "Test Location", OffsetDateTime.now(), null);

        when(branchRepository.findById(branchId))
                .thenReturn(Optional.of(activeBranch));

        BranchResponse branchResponse = branchService.archiveBranch(branchId);

        assertEquals(1L, branchResponse.branchId());
        assertEquals(ARCHIVADO, branchResponse.status());
        assertNotNull(branchResponse.archivedAt());

        verify(branchRepository).save(activeBranch);
    }

    @Test
    void shouldThrowBusinessRuleViolationExceptionWhenArchivingAnArchivedBranch() {

        Long branchId = 1L;
        OffsetDateTime archivedAt =
                OffsetDateTime.of(2025, 6, 10, 12, 0, 0, 0, ZoneOffset.UTC);

        Branch activeBranch = new Branch(1L, "Test Name", ARCHIVADO, "Test Location", OffsetDateTime.now(), archivedAt);

        when(branchRepository.findById(branchId))
                .thenReturn(Optional.of(activeBranch));

        assertThrows(BusinessRuleViolation.class, () -> branchService.archiveBranch(branchId));

        verify(branchRepository, never()).save(any(Branch.class));
    }


    @Test
    void shouldReactivateBranchSuccessfully() {

        Long branchId = 1L;

        OffsetDateTime createdAt = OffsetDateTime.of(2002, 11, 14, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime archivedAt = OffsetDateTime.of(2025, 6, 10, 12, 0, 0, 0, ZoneOffset.UTC);

        Branch archivedBranch = new Branch(1L, "Test Name", ARCHIVADO, "Test Location", createdAt, archivedAt);

        when(branchRepository.findById(branchId))
                .thenReturn(Optional.of(archivedBranch));

        BranchResponse branchResponse = branchService.reactivateBranch(branchId);

        assertEquals(1L, branchResponse.branchId());
        assertEquals(ACTIVO, branchResponse.status());
        assertNull(branchResponse.archivedAt(), "after a reactivation archived at goes back to a null state");
        assertEquals(createdAt, branchResponse.creationDate(), "We do not store a reactivated at value, " +
                "this assertion proves that no matter how long a branch remained archived for even after reactivation it will preserve its creation date");

        verify(branchRepository).save(archivedBranch);
    }

    @Test
    void shouldThrowBusinessRuleViolationExceptionWhenReactivatingAnActiveBranch() {

        Long branchId = 1L;

        Branch activeBranch = new Branch(1L, "Test Name", ACTIVO, "Test Location", OffsetDateTime.now(), null);

        when(branchRepository.findById(branchId))
                .thenReturn(Optional.of(activeBranch));

        assertThrows(BusinessRuleViolation.class, () -> branchService.reactivateBranch(branchId));

        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    void shouldGetAllArchivedBranchesSuccessfully() {

        Branch archivedBranch1 = new Branch("Test name", ARCHIVADO, "Test Location");
        Branch archivedBranch2 = new Branch("Test name 2", ARCHIVADO, "Test Location");
        Branch archivedBranch3 = new Branch("Test name 3", ARCHIVADO, "Test Location");

        when(branchRepository.findByStatus(ARCHIVADO))
                .thenReturn(List.of(archivedBranch1, archivedBranch2, archivedBranch3));

        List<BranchResponse> branchResponses = branchService.getAllArchivedBranches();

        assertEquals(3, branchResponses.size());
        assertEquals(ARCHIVADO, branchResponses.get(0).status());
        assertEquals(ARCHIVADO, branchResponses.get(1).status());
        assertEquals(ARCHIVADO, branchResponses.get(2).status());

        verify(branchRepository).findByStatus(ARCHIVADO);
    }

    @Test
    void shouldReturnEmptyListWhenNoArchivedBranchesExist() {

        when(branchRepository.findByStatus(ARCHIVADO))
                .thenReturn(Collections.emptyList());

        List<BranchResponse> branchResponses = branchService.getAllArchivedBranches();

        assertTrue(branchResponses.isEmpty());

        verify(branchRepository).findByStatus(ARCHIVADO);
    }

    @Test
    void shouldGetAllActiveBranchesSuccessfully() {

        Branch activeBranch1 = new Branch("Test Name", ACTIVO, "Test Location");
        Branch activeBranch2 = new Branch("Test Name 2", ACTIVO, "Test Location");
        Branch activeBranch3 = new Branch("Test Name 3", ACTIVO, "Test Location");

        when(branchRepository.findByStatus(ACTIVO))
                .thenReturn(List.of(activeBranch1, activeBranch2, activeBranch3));

        List<BranchResponse> branchResponses = branchService.getAllActiveBranches();

        assertEquals(3, branchResponses.size());
        assertEquals(ACTIVO, branchResponses.get(0).status());
        assertEquals(ACTIVO, branchResponses.get(1).status());
        assertEquals(ACTIVO, branchResponses.get(2).status());

        verify(branchRepository).findByStatus(ACTIVO);
    }

    @Test
    void shouldReturnEmptyListWhenNoActiveBranchesExist() {

        when(branchRepository.findByStatus(ACTIVO))
                .thenReturn(Collections.emptyList());

        List<BranchResponse> branchResponses = branchService.getAllActiveBranches();

        assertTrue(branchResponses.isEmpty());

        verify(branchRepository).findByStatus(ACTIVO);
    }

    @Test
    void shouldGetAllBranchesSuccessfully() {

        Branch activeBranch1 = new Branch("Test Name", ACTIVO, "Test Location");
        Branch activeBranch2 = new Branch("Test Name 2", ACTIVO, "Test Location");
        Branch activeBranch3 = new Branch("Test Name 3", ACTIVO, "Test Location");
        Branch archivedBranch4 = new Branch("Test Name 4", ARCHIVADO, "Test Location");

        when(branchRepository.findAll())
                .thenReturn(List.of(activeBranch1, activeBranch2, activeBranch3, archivedBranch4));

        List<BranchResponse> branchResponses = branchService.findAllBranches();

        assertEquals(4, branchResponses.size());
        assertEquals(ACTIVO, branchResponses.get(0).status());
        assertEquals(ACTIVO, branchResponses.get(1).status());
        assertEquals(ACTIVO, branchResponses.get(2).status());
        assertEquals(ARCHIVADO, branchResponses.get(3).status());

        verify(branchRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoBranchesExist() {

        when(branchRepository.findAll())
                .thenReturn(Collections.emptyList());

        List<BranchResponse> branchResponses = branchService.findAllBranches();

        assertTrue(branchResponses.isEmpty());

        verify(branchRepository).findAll();
    }

    @Test
    void shouldUpdateBranchSuccessfully() {

        Long branchId = 1L;

        Branch existingBranch = new Branch(1L, "Test Name", ACTIVO, "Test Location");

        UpdateBranchRequest request = new UpdateBranchRequest(
                "  nEW     nAME ",
                "  nEW     lOCATION "
        );

        when(branchRepository.findById(branchId)).thenReturn(Optional.of(existingBranch));
        when(branchRepository.findByName("New Name")).thenReturn(Optional.empty());

        BranchResponse response = branchService.updateBranch(branchId, request);

        assertEquals("New Name", response.name());
        assertEquals("New Location", response.location());

        verify(branchRepository).findById(branchId);
        verify(branchRepository).findByName("New Name");
        assertEquals("New Name", existingBranch.getName());
        assertEquals("New Location", existingBranch.getLocation());
    }

    @Test
    void shouldRejectDuplicateNameUpdate() {

        Long branchId = 1L;

        Branch existingBranch = new Branch(1L, "Test Name", ACTIVO, "Test Location");

        UpdateBranchRequest request = new UpdateBranchRequest(
                "  nEW     nAME ",
                "  nEW     lOCATION "
        );

        Branch existingBranch2 = new Branch(2L, "New Name", ACTIVO, "Test Location");

        when(branchRepository.findById(branchId)).thenReturn(Optional.of(existingBranch));
        when(branchRepository.findByName("New Name")).thenReturn(Optional.of(existingBranch2));


        assertThrows(BusinessRuleViolation.class, () ->  branchService.updateBranch(branchId, request));

        assertEquals("Test Name", existingBranch.getName());
        assertEquals("Test Location", existingBranch.getLocation());
    }

    @Test
    void shouldThrowExceptionWhenBranchDoesNotExist() {
        Long branchId = 99L;

        UpdateBranchRequest request =
                new UpdateBranchRequest("New Name", "New Location");

        when(branchRepository.findById(branchId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> branchService.updateBranch(branchId, request)
        );

        verify(branchRepository, never()).findByName(anyString());
    }
}
