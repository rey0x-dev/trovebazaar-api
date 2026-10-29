package com.trovebazaar.trovebazaarapi.tests.repository;

import com.trovebazaar.trovebazaarapi.exception.ResourceNotFoundException;
import com.trovebazaar.trovebazaarapi.model.Money;
import com.trovebazaar.trovebazaarapi.model.Product;
import com.trovebazaar.trovebazaarapi.model.ProductStatus;
import com.trovebazaar.trovebazaarapi.repository.ProductRepository;
import jakarta.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ProductRepositoryTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldPersistProductSuccessfully() {
        Money money = new Money(1000L);
        Product product = new Product("testCode", "testDescription", money);

        Product savedProduct = productRepository.saveAndFlush(product);
        Long savedIdProduct = savedProduct.getProductId();

        em.clear();

        Product foundProduct = productRepository.findById(savedIdProduct)
                .orElseThrow(() -> new EntityNotFoundException("product not found"));

        assertEquals(savedIdProduct, foundProduct.getProductId());
        assertEquals("testCode", foundProduct.getQrCode());
        assertEquals("testDescription", foundProduct.getDescription());
        assertEquals(1000L, foundProduct.getPrice().priceInCents());
        assertEquals(ProductStatus.ACTIVO, foundProduct.getStatus());
    }

    @Test
    void shouldFindProductsByQrCode() {
        Money money = new Money(1000L);

        Product product = new Product("camisaCodeTest", "camisaTestDescription1", money);
        productRepository.saveAndFlush(product);

        em.clear();

        Product foundProduct = productRepository.findByQrCode("camisaCodeTest")
                .orElseThrow(() -> new ResourceNotFoundException("product not found"));

        assertEquals("camisaCodeTest", foundProduct.getQrCode());
        assertEquals("camisaTestDescription1", foundProduct.getDescription());
        assertEquals(1000L, foundProduct.getPrice().priceInCents());
        assertEquals(ProductStatus.ACTIVO, foundProduct.getStatus());
    }

    @Test
    void shouldFindProductsByDescription() {
        Money money = new Money(1000L);

        Product product = new Product("pantalonCodeTest", "pantalonTestDescription1", money);
        productRepository.saveAndFlush(product);

        em.clear();

        Product foundProduct = productRepository.findByDescription("pantalonTestDescription1")
                .orElseThrow(() -> new ResourceNotFoundException("product not found"));

        assertEquals("pantalonCodeTest", foundProduct.getQrCode());
        assertEquals("pantalonTestDescription1", foundProduct.getDescription());
        assertEquals(1000L, foundProduct.getPrice().priceInCents());
    }

    @Test
    void shouldThrowExceptionWhenQrCodeIsDuplicated() {

        Money money = new Money(1000L);
        Product product = new Product("testCode", "testDescription", money);

        productRepository.save(product);

        Money money2 = new Money(1000L);
        Product product2 = new Product("testCode", "testDescription", money2);

        assertThrows(DataIntegrityViolationException.class, () -> productRepository.saveAndFlush(product2));
    }

    @Test
    void shouldThrowExceptionWhenQrCodeIsNull() {
        Money money = new Money(1000L);
        Product product = new Product(null, "testDescription", money);

        assertThrows(DataIntegrityViolationException.class, () -> productRepository.saveAndFlush(product));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsNull() {
        Money money = new Money(1000L);
        Product product = new Product("testCode", null, money);

        assertThrows(DataIntegrityViolationException.class, () -> productRepository.saveAndFlush(product));
    }

    @Test
    void shouldThrowExceptionWhenPriceIsNull() {
        Money money = new Money(1000L);
        Product product = new Product("testCode", "testDescription", null);

        assertThrows(DataIntegrityViolationException.class, () -> productRepository.saveAndFlush(product));
    }
}
