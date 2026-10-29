package com.trovebazaar.trovebazaarapi.tests.service;

import com.trovebazaar.trovebazaarapi.dto.CreateProductRequest;
import com.trovebazaar.trovebazaarapi.dto.ProductResponse;
import com.trovebazaar.trovebazaarapi.dto.UpdateProductRequest;
import com.trovebazaar.trovebazaarapi.exception.ResourceNotFoundException;
import com.trovebazaar.trovebazaarapi.model.Money;
import com.trovebazaar.trovebazaarapi.model.Product;
import static com.trovebazaar.trovebazaarapi.model.ProductStatus.ARCHIVADO;
import static com.trovebazaar.trovebazaarapi.model.ProductStatus.ACTIVO;
import com.trovebazaar.trovebazaarapi.repository.ProductRepository;
import com.trovebazaar.trovebazaarapi.service.ProductService;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ProductService productService;

    @Test
    void shouldReturnProductResponse() {

        CreateProductRequest createProductRequest = new CreateProductRequest(new Money(10000L), "productTest");

        Product product = new Product(1L, "codeTest", "productTest", new Money(10000L));

        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        ProductResponse productResponse = productService.createProduct(createProductRequest);

        assertEquals(1L, productResponse.productId());
        assertEquals("codeTest", productResponse.qrCode());
        assertEquals("productTest", productResponse.description());
        assertEquals(10000L, productResponse.price().priceInCents());
        assertEquals(ACTIVO, productResponse.productStatus());

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldReturnProductById(){

        Long id = 1L;
        Product product = new Product(1L, "codeTest", "productTest", new Money(10000L));

        when(productRepository.findById(id))
                .thenReturn(Optional.of(product));

        ProductResponse productResponse = productService.findProductById(id);

        assertEquals(1L, productResponse.productId());
        assertEquals("codeTest", productResponse.qrCode());

        verify(productRepository).findById(1L);
    }

    @Test
    void shouldFindAllProducts(){

        Product product1 = new Product(1L, "codeTest1", "productTest1", new Money(10000L));
        Product product2 = new Product(2L, "codeTest2", "productTest2", new Money(10000L));
        Product product3 = new Product(3L, "codeTest3", "productTest3", new Money(10000L));

        when(productRepository.findAll())
        .thenReturn(List.of(product1, product2, product3));

        List<ProductResponse> productResponses = productService.findAllProducts();

        assertEquals(3, productResponses.size());
        assertEquals(1L, productResponses.get(0).productId());
        assertEquals(2L, productResponses.get(1).productId());
        assertEquals(3L, productResponses.get(2).productId());

        verify(productRepository).findAll();
    }

    @Test
    void shouldFindAllActiveProducts(){

        Product product1 = new Product("productTest1", new Money(10000L), ACTIVO);
        Product product2 = new Product("productTest2", new Money(10000L), ACTIVO);
        Product product3 = new Product("productTest3", new Money(10000L), ACTIVO);

        when(productRepository.findByStatus(ACTIVO))
        .thenReturn(List.of(product1, product2, product3));

        List<ProductResponse> productResponses = productService.findAllActiveProducts();

        assertEquals(3, productResponses.size());
        assertEquals("productTest1", productResponses.get(0).description());
        assertEquals("productTest2", productResponses.get(1).description());
        assertEquals("productTest3", productResponses.get(2).description());

        assertTrue(productResponses.stream()
                .allMatch(productResponse -> productResponse.productStatus().equals(ACTIVO)));

        verify(productRepository).findByStatus(ACTIVO);
    }

    @Test
    void shouldReturnEmptyListWhenNoProductsAreFound(){

        when(productRepository.findAll())
        .thenReturn(Collections.emptyList());

        List<ProductResponse> productResponses = productService.findAllProducts();

        assertEquals(0, productResponses.size());
        verify(productRepository).findAll();
    }

    @Test
    void shouldFindAllArchivedProducts(){

        Product product1 = new Product("productTest1", new Money(10000L), ARCHIVADO);
        Product product2 = new Product("productTest2", new Money(10000L), ARCHIVADO);
        Product product3 = new Product("productTest3", new Money(10000L), ARCHIVADO);

        when(productRepository.findByStatus(ARCHIVADO))
                .thenReturn(List.of(product1, product2, product3));

        List<ProductResponse> productResponses = productService.findAllArchivedProducts();

        assertEquals(3, productResponses.size());
        assertEquals("productTest1", productResponses.get(0).description());
        assertEquals("productTest2", productResponses.get(1).description());
        assertEquals("productTest3", productResponses.get(2).description());

        assertTrue(productResponses.stream()
                .allMatch(productResponse -> productResponse.productStatus().equals(ARCHIVADO)));

        verify(productRepository).findByStatus(ARCHIVADO);
    }

    @Test
    void shouldUpdateProductSuccessfully(){

        Long productId = 1L;
        UpdateProductRequest updateProductRequest = new UpdateProductRequest( new Money(10000L),
                "   nEW DescRIPTIOn  ");

        Product existingProduct = new Product(1L, "codeTest", "productTest", new Money(10000L));

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse productResponse = productService.updateProduct(productId, updateProductRequest);

        assertEquals(1L, productResponse.productId());
        assertEquals("New description", productResponse.description());
        assertEquals(ACTIVO, productResponse.productStatus());

        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundWhileUpdatingProduct(){

        Long productId = 1L;
        UpdateProductRequest updateProductRequest = new UpdateProductRequest(new Money(10000L),
                "   nEW DescRIPTIOn  ");

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(productId, updateProductRequest));

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldReactivateProductSuccessfully(){
        Long productId = 1L;

        when(productRepository.findById(productId))
        .thenReturn(Optional.of(new Product("productTest", new Money(10000L), ARCHIVADO)));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse productResponse = productService.reactivateProduct(productId);

        assertEquals("productTest", productResponse.description());
        assertEquals(ACTIVO, productResponse.productStatus());
        assertEquals(10000L, productResponse.price().priceInCents());

        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));

        verify(productRepository).save(argThat(
                product -> product.getStatus() == ACTIVO
        ));
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundWhileReactivatingProduct(){
        Long productId = 1L;

        when(productRepository.findById(productId))
        .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.reactivateProduct(productId));

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldArchiveProductSuccessfully(){
        Long productId = 1L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(new Product("productTest", new Money(10000L), ACTIVO)));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse productResponse = productService.archiveProduct(productId);

        assertEquals("productTest", productResponse.description());
        assertEquals(ARCHIVADO, productResponse.productStatus());
        assertEquals(10000L, productResponse.price().priceInCents());

        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));

        verify(productRepository).save(argThat(
                product -> product.getStatus() == ARCHIVADO
        ));
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundWhileArchivingProduct(){
        Long productId = 1L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.archiveProduct(productId));

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldReturnExceptionWhenProductNotFound(){
        Long id = 1L;

        when(productRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.findProductById(id));
    }

}
