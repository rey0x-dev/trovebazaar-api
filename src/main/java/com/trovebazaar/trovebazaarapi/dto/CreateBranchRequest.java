package com.trovebazaar.trovebazaarapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBranchRequest(

        @Schema(
                description = "Branch name. Must not be blank and must not exceed 50 characters",
                maxLength = 50,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "El nombre de la sucursal es requerido")
        @Size(max = 50, message = "El nombre de la sucursal no puede exceder 50 carácteres")
        String name,

        @Schema(
                description = "Branch location. Must not be blank and must not exceed 255 characters",
                maxLength = 255,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "La ubicación de la sucursal es requerida")
        @Size(max = 255, message = "La ubicación de la sucursal no puede exceder los 255 carácteres")
        String location
) {
}
