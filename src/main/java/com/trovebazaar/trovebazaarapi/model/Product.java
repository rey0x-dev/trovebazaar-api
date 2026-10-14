package com.trovebazaar.trovebazaarapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false,  updatable = false)
    private Long productId;
    @Column(name = "qr_code", nullable = false, unique = true, length = 30)
    private String qrCode;
    @Column(name = "description", nullable = false, length = 70)
    private String description;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "priceInCents",
                    column = @Column(name = "price_in_cents", nullable = false)
            )
    })
    private Money price;
}
