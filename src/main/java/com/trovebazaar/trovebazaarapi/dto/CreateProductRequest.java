package com.trovebazaar.trovebazaarapi.dto;

import com.trovebazaar.trovebazaarapi.model.Money;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(

        @Schema(
                description = "The product price, expressed in cents",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "A price must be specified")
        @Valid
        Money price,

        @Schema(
                description = "Product description. Must not be blank and must not exceed 70 characters",
                example = "Mesa de 50 pesos",
                maxLength = 70,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "A description is required")
        @Size(max = 70, message = "Description can't exceed seventy characters")
        String description
) {
}
