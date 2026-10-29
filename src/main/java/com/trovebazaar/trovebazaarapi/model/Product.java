package com.trovebazaar.trovebazaarapi.model;

import com.trovebazaar.trovebazaarapi.dto.UpdateProductRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @Enumerated(EnumType.STRING)
    @Column(name = "status",  nullable = false, length = 10)
    private ProductStatus status;


    public Product(Long productId, String qrCode, String description, Money price) {
        this.productId = productId;
        this.qrCode = qrCode;
        this.description = description;
        this.price = price;
        this.status = ProductStatus.ACTIVO;
    }

    public Product(String qrCode, String description, Money price) {
        this.qrCode = qrCode;
        this.description = description;
        this.price = price;
        this.status = ProductStatus.ACTIVO;
    }

    public Product(String description, Money price, ProductStatus status) {
        this.description = description;
        this.price = price;
        this.status = status;
    }

    public Product reactivateProduct() {
        this.status = ProductStatus.ACTIVO;
        return this;
    }

    public Product archiveProduct() {
        this.status = ProductStatus.ARCHIVADO;
        return this;
    }

    public Product updateProduct(Money price, String normalizedSentence) {
        this.description = normalizedSentence;
        this.price = price;

        return this;
    }
}
