package com.trovebazaar.trovebazaarapi.tests.controller;

import com.trovebazaar.trovebazaarapi.controller.BranchController;
import com.trovebazaar.trovebazaarapi.dto.BranchResponse;
import com.trovebazaar.trovebazaarapi.dto.CreateBranchRequest;
import com.trovebazaar.trovebazaarapi.dto.UpdateBranchRequest;
import com.trovebazaar.trovebazaarapi.model.BranchStatus;
import com.trovebazaar.trovebazaarapi.service.BranchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BranchController.class)
public class BranchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BranchService branchService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateBranchSuccessfully() throws Exception {

        CreateBranchRequest branchRequest = new CreateBranchRequest("Test Name", "Location Test");
        String json = objectMapper.writeValueAsString(branchRequest);

        BranchResponse branchResponse = new BranchResponse(1L, "Test Name", BranchStatus.ACTIVO, "Location Test", OffsetDateTime.now(), null);

        when(branchService.createBranch(any(CreateBranchRequest.class)))
                .thenReturn(branchResponse);

        mockMvc.perform(post("/api/branches")
                        .with(user("usertest"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))

                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/branches/1")))
                .andExpect(jsonPath("$.name").value("Test Name"))
                .andExpect(jsonPath("$.location").value("Location Test"))
                .andExpect(jsonPath("$.status").value("ACTIVO"))
                .andExpect(jsonPath("$.archivedAt", nullValue()))
                .andExpect(jsonPath("$.branchId").value(1));
    }

    @Test
    void shouldArchiveBranchSuccessfully() throws Exception {

        Long branchId = 1L;

        OffsetDateTime createdAt = OffsetDateTime.of(2002, 11, 14, 0, 0, 0, 0, ZoneOffset.UTC);

        BranchResponse branchResponse = new BranchResponse(1L, "Test Name", BranchStatus.ARCHIVADO, "Location Test", createdAt, OffsetDateTime.now());

        when(branchService.archiveBranch(branchId))
                .thenReturn(branchResponse);

        mockMvc.perform(put("/api/branches/{id}/archive", branchId)
                        .with(user("usertest"))
                        .with(csrf()))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.branchId").value(1))
                .andExpect(jsonPath("$.name").value("Test Name"))
                .andExpect(jsonPath("$.location").value("Location Test"))
                .andExpect(jsonPath("$.status").value("ARCHIVADO"))
                .andExpect(jsonPath("$.archivedAt").isNotEmpty());

        verify(branchService).archiveBranch(branchId);
    }

    @Test
    void shouldReactivateBranchSuccessfully() throws Exception {

        Long branchId = 1L;
        OffsetDateTime createdAt = OffsetDateTime.of(2002, 11, 14, 0, 0, 0, 0, ZoneOffset.UTC);

        BranchResponse response = new BranchResponse(branchId, "Test Name", BranchStatus.ACTIVO, "Location Test", createdAt, null);

        when(branchService.reactivateBranch(branchId))
                .thenReturn(response);

        mockMvc.perform(put("/api/branches/{id}/reactivate", branchId)
                        .with(user("usertest"))
                        .with(csrf()))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.branchId").value(1))
                .andExpect(jsonPath("$.name").value("Test Name"))
                .andExpect(jsonPath("$.location").value("Location Test"))
                .andExpect(jsonPath("$.status").value("ACTIVO"))
                .andExpect(jsonPath("$.archivedAt").value(nullValue()))
                .andExpect(jsonPath("$.creationDate").value("2002-11-14T00:00:00Z"));

        verify(branchService).reactivateBranch(branchId);
    }

    @Test
    void shouldGetAllArchivedBranchesSuccessfully() throws Exception {
        List<BranchResponse> responses = List.of(
                new BranchResponse(
                        1L, "Branch One", BranchStatus.ARCHIVADO,
                        "Location One", OffsetDateTime.now(),
                        OffsetDateTime.now()),
                new BranchResponse(
                        2L, "Branch Two", BranchStatus.ARCHIVADO,
                        "Location Two", OffsetDateTime.now(),
                        OffsetDateTime.now())
        );

        when(branchService.getAllArchivedBranches()).thenReturn(responses);

        mockMvc.perform(get("/api/branches/archived")
                        .with(user("usertest"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Branch One"))
                .andExpect(jsonPath("$[0].status").value("ARCHIVADO"))
                .andExpect(jsonPath("$[1].name").value("Branch Two"))
                .andExpect(jsonPath("$[1].status").value("ARCHIVADO"));

        verify(branchService).getAllArchivedBranches();
    }

    @Test
    void shouldGetAllActiveBranchesSuccessfully() throws Exception {
        List<BranchResponse> responses = List.of(
                new BranchResponse(
                        1L, "Branch One", BranchStatus.ACTIVO,
                        "Location One", OffsetDateTime.now(), null),
                new BranchResponse(
                        2L, "Branch Two", BranchStatus.ACTIVO,
                        "Location Two", OffsetDateTime.now(), null)
        );

        when(branchService.getAllActiveBranches()).thenReturn(responses);

        mockMvc.perform(get("/api/branches/active")
                        .with(user("usertest"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Branch One"))
                .andExpect(jsonPath("$[0].status").value("ACTIVO"))
                .andExpect(jsonPath("$[1].name").value("Branch Two"))
                .andExpect(jsonPath("$[1].status").value("ACTIVO"));

        verify(branchService).getAllActiveBranches();
    }

    @Test
    void shouldGetAllBranchesSuccessfully() throws Exception {
        List<BranchResponse> responses = List.of(
                new BranchResponse(
                        1L, "Active Branch", BranchStatus.ACTIVO,
                        "Location One", OffsetDateTime.now(), null),
                new BranchResponse(
                        2L, "Archived Branch", BranchStatus.ARCHIVADO,
                        "Location Two", OffsetDateTime.now(),
                        OffsetDateTime.now())
        );

        when(branchService.findAllBranches()).thenReturn(responses);

        mockMvc.perform(get("/api/branches")
                        .with(user("usertest"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("ACTIVO"))
                .andExpect(jsonPath("$[1].status").value("ARCHIVADO"));

        verify(branchService).findAllBranches();
    }

    @Test
    void shouldUpdateBranchSuccessfully() throws Exception {
        Long branchId = 1L;

        UpdateBranchRequest request = new UpdateBranchRequest(
                "New Branch Name",
                "New Branch Location"
        );

        String json = objectMapper.writeValueAsString(request);

        BranchResponse response = new BranchResponse(
                branchId,
                "New Branch Name",
                BranchStatus.ACTIVO,
                "New Branch Location",
                OffsetDateTime.of(
                        2002, 11, 14, 0, 0, 0, 0, ZoneOffset.UTC),
                null
        );

        when(branchService.updateBranch(
                org.mockito.ArgumentMatchers.eq(branchId),
                any(UpdateBranchRequest.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/branches/{id}", branchId)
                        .with(user("usertest"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.branchId").value(1))
                .andExpect(jsonPath("$.name").value("New Branch Name"))
                .andExpect(jsonPath("$.location").value("New Branch Location"))
                .andExpect(jsonPath("$.status").value("ACTIVO"));

        verify(branchService).updateBranch(
                org.mockito.ArgumentMatchers.eq(branchId),
                any(UpdateBranchRequest.class)
        );
    }

    @Test
    void shouldRejectUpdateWithInvalidRequest() throws Exception {
        UpdateBranchRequest request = new UpdateBranchRequest("", "");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/branches/{id}", 1L)
                        .with(user("usertest"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(branchService, never()).updateBranch(
                org.mockito.ArgumentMatchers.eq(1L),
                any(UpdateBranchRequest.class)
        );
    }
}
