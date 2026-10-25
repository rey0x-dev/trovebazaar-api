package com.trovebazaar.trovebazaarapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBranchRequest(
        @NotBlank(message = "El nombre de la sucursal es requerido")
        @Size(message = "El nombre de la sucursal no puede exceder 50 carácteres")
        String name,
        @NotBlank(message = "La ubicación de la sucursal es requerida")
        @Size(message = "La ubicación de la sucursal no puede exceder los 255 carácteres")
        String location
) {
}
