package com.trovebazaar.trovebazaarapi.repository;

import com.trovebazaar.trovebazaarapi.model.Product;
import com.trovebazaar.trovebazaarapi.model.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByQrCode(String qrCode);
    Optional<Product> findByDescription(String description);
    boolean existsByQrCode(String qrCode);
    List<Product> findByStatus(ProductStatus status);
}
