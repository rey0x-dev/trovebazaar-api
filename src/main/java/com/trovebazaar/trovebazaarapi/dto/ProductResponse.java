package com.trovebazaar.trovebazaarapi.dto;

import com.trovebazaar.trovebazaarapi.model.Money;
import com.trovebazaar.trovebazaarapi.model.ProductStatus;

public record ProductResponse(
        Long productId,
        String qrCode,
        String description,
        Money price,
        ProductStatus productStatus
) {
}
