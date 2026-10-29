package com.trovebazaar.trovebazaarapi.dto;

import com.trovebazaar.trovebazaarapi.model.Money;
import com.trovebazaar.trovebazaarapi.model.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(

        @NotNull(message = "A price must be specified")
        @Valid
        Money price,
        @NotBlank(message = "A description is required")
        @Size(max = 70, message = "Description can't exceed seventy characters")
        String description
) {
}
