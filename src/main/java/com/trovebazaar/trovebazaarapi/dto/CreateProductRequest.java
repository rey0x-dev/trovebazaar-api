package com.trovebazaar.trovebazaarapi.dto;

import com.trovebazaar.trovebazaarapi.model.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(

        @NotNull(message = "A price must be specified")
        @Valid
        Money price,
        @NotBlank(message = "A description is required")
        @Size(max = 70, message = "Description can't exceed seventy characters")
        String description
) {
}
