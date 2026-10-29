package com.trovebazaar.trovebazaarapi.model;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record Money(
        @NotNull(message = "A price must be specified")
        @Positive(message = "A price must be greater than zero")
        @Column(name = "price_in_cents", nullable = false)
        Long priceInCents
) {
}
