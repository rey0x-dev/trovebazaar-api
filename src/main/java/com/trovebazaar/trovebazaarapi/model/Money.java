package com.trovebazaar.trovebazaarapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record Money(

        @Schema(
                description = "Price expressed in cents. Must be greater than zero",
                example = "15000",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "A price must be specified")
        @Positive(message = "A price must be greater than zero")
        @Column(name = "price_in_cents", nullable = false)
        Long priceInCents
) {
}
