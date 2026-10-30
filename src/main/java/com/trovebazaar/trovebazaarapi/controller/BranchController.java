package com.trovebazaar.trovebazaarapi.controller;

import com.trovebazaar.trovebazaarapi.dto.BranchResponse;
import com.trovebazaar.trovebazaarapi.dto.CreateBranchRequest;
import com.trovebazaar.trovebazaarapi.dto.UpdateBranchRequest;
import com.trovebazaar.trovebazaarapi.exception.ApiError;
import com.trovebazaar.trovebazaarapi.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "Create a branch",
            description = "Creates a new branch and returns its details."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Branch created successfully",
                    headers = @Header(
                            name = "Location",
                            description = "URI of the newly created branch",
                            schema = @Schema(type = "string", format = "uri")
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = BranchResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or business rule violation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation error",
                                            summary = "The branch name is required",
                                            value = """
                                        {
                                          "errorMessage": "Request validation failed",
                                          "errorTimestamp": "2026-10-29T16:30:00",
                                          "errors": {
                                            "name": "El nombre de la sucursal es requerido"
                                          },
                                          "status": 400
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "Duplicate branch name",
                                            summary = "A branch with the requested name already exists",
                                            value = """
                                        {
                                          "errorMessage": "Branch with name Centro already exists",
                                          "errorTimestamp": "2026-10-29T16:30:00",
                                          "errors": null,
                                          "status": 400
                                        }
                                        """
                                    )
                            }
                    )
            )
    })
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


    @Operation(
            summary = "Create a branch",
            description = "Creates a new branch and returns its details."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Branch created successfully",
                    headers = @Header(
                            name = "Location",
                            description = "URI of the newly created branch",
                            schema = @Schema(type = "string", format = "uri")
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = BranchResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or business rule violation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiError.class
                            ),
                            examples = @ExampleObject(
                                    name = "Validation error",
                                    summary = "The branch name is required",
                                    value = """
                                        {
                                          "errorMessage": "Request validation failed",
                                          "errorTimestamp": "2026-10-29T16:30:00",
                                          "errors": {
                                            "name": "El nombre de la sucursal es requerido"
                                          },
                                          "status": 400
                                        }
                                        """
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<BranchResponse>> getAllBranches() {

        List<BranchResponse> branchResponses = branchService.findAllBranches();
        return ResponseEntity.ok(branchResponses);
    }


    @Operation(
            summary = "Get archived branches",
            description = "Retrieves all archived branches and their details. "
                    + "Returns an empty list if no archived branches exist."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Archived branches retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = BranchResponse.class
                                    )
                            )
                    )
            )
    })
    @GetMapping("/archived")
    public ResponseEntity<List<BranchResponse>> getArchivedBranches() {

        List<BranchResponse> branchResponses = branchService.getAllArchivedBranches();
        return ResponseEntity.ok(branchResponses);
    }


    @Operation(
            summary = "Get active branches",
            description = "Retrieves all active branches and their details. "
                    + "Returns an empty list if no active branches exist."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Active branches retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = BranchResponse.class
                                    )
                            )
                    )
            )
    })
    @GetMapping("/active")
    public ResponseEntity<List<BranchResponse>> getActiveBranches() {

        List<BranchResponse> branchResponses = branchService.getAllActiveBranches();
        return ResponseEntity.ok(branchResponses);
    }


    @Operation(
            summary = "Update a branch",
            description = "Updates the name and location of an existing branch."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Branch updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = BranchResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or business rule violation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiError.class
                            ),
                            examples = @ExampleObject(
                                    name = "Validation error",
                                    summary = "Branch name and location are required",
                                    value = """
                                        {
                                          "errorMessage": "Request validation failed",
                                          "errorTimestamp": "2026-10-29T16:30:00",
                                          "errors": {
                                            "name": "El nombre de la sucursal es requerido",
                                            "location": "La ubicación de la sucursal es requerida"
                                          },
                                          "status": 400
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Branch not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Branch not found",
                                    summary = "The requested branch does not exist",
                                    value = """
                                {
                                  "errorMessage": "Branch with id 1 does not exist",
                                  "errorTimestamp": "2026-10-29T16:30:00",
                                  "errors": null,
                                  "status": 404
                                }
                                """
                            )
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<BranchResponse> updateBranch(
            @Parameter(
                    description = "Unique identifier of the branch to update",
                    example = "1"
            )
            @PathVariable Long id,
            @RequestBody @Valid UpdateBranchRequest updateBranchRequest) {

        BranchResponse branchResponse = branchService.updateBranch(id, updateBranchRequest);
        return ResponseEntity.ok(branchResponse);
    }


    @Operation(
            summary = "Archive a branch",
            description = "Archives the branch identified by its unique ID. "
                    + "The operation is idempotent: archiving an already archived branch has no additional effect."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Branch archived successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = BranchResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Branch not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiError.class
                            ),
                            examples = @ExampleObject(
                                    name = "Branch not found",
                                    summary = "The requested branch does not exist",
                                    value = """
                                        {
                                          "errorMessage": "Branch with id 1 does not exist",
                                          "errorTimestamp": "2026-10-29T16:30:00",
                                          "errors": null,
                                          "status": 404
                                        }
                                        """
                            )
                    )
            )
    })
    @PutMapping("/{id}/archive")
    public ResponseEntity<BranchResponse> archiveBranch(
            @Parameter(
                    description = "Unique identifier of the branch to update",
                    example = "1"
            )
            @PathVariable Long id) {

        BranchResponse branchResponse = branchService.archiveBranch(id);

        return ResponseEntity.ok(branchResponse);
    }


    @Operation(
            summary = "Reactivate a branch",
            description = "Reactivates the branch identified by its unique ID. "
                    + "The operation is idempotent: reactivating an already active branch has no additional effect."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Branch reactivated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = BranchResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Branch not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiError.class
                            ),
                            examples = @ExampleObject(
                                    name = "Branch not found",
                                    summary = "The requested branch does not exist",
                                    value = """
                                        {
                                          "errorMessage": "Branch with id 1 does not exist",
                                          "errorTimestamp": "2026-10-29T16:30:00",
                                          "errors": null,
                                          "status": 404
                                        }
                                        """
                            )
                    )
            )
    })
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<BranchResponse> reactivateBranch(
            @Parameter(
                    description = "Unique identifier of the branch to update",
                    example = "1"
            )
            @PathVariable Long id) {

        BranchResponse branchResponse = branchService.reactivateBranch(id);

        return ResponseEntity.ok(branchResponse);
    }
}
