package com.trovebazaar.trovebazaarapi.model;

import jakarta.persistence.Column;

public record Money(
        @Column(name = "price_in_cents", nullable = false)
        Long priceInCents
) {
}
