package com.trovebazaar.trovebazaarapi.service;

import com.trovebazaar.trovebazaarapi.dto.CreateProductRequest;
import com.trovebazaar.trovebazaarapi.dto.ProductResponse;
import com.trovebazaar.trovebazaarapi.dto.UpdateProductRequest;
import com.trovebazaar.trovebazaarapi.exception.ResourceNotFoundException;
import com.trovebazaar.trovebazaarapi.model.Product;
import static com.trovebazaar.trovebazaarapi.model.ProductStatus.ACTIVO;
import static com.trovebazaar.trovebazaarapi.model.ProductStatus.ARCHIVADO;
import com.trovebazaar.trovebazaarapi.repository.ProductRepository;
import com.trovebazaar.trovebazaarapi.utility.TextNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest productRequest) {

        String normalizedSentence = TextNormalizer.normalizedSentence(productRequest.description());
        Product product = new Product(generateUniqueQrCode(), normalizedSentence, productRequest.price());
        Product savedProduct = productRepository.save(product);

        return convertIntoProductResponse(savedProduct);
    }

    public ProductResponse findProductById(Long id) {

        Product foundProduct = findProductEntityById(id);
        return convertIntoProductResponse(foundProduct);
    }

    public List<ProductResponse> findAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(this::convertIntoProductResponse)
                .toList();
    }

    public List<ProductResponse> findAllActiveProducts() {

        return productRepository.findByStatus(ACTIVO)
                .stream()
                .map(this::convertIntoProductResponse)
                .toList();
    }

    public List<ProductResponse> findAllArchivedProducts() {

        return productRepository.findByStatus(ARCHIVADO)
                .stream()
                .map(this::convertIntoProductResponse)
                .toList();
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, UpdateProductRequest productRequest) {

        Product foundProduct = findProductEntityById(productId);

        String normalizedDescription = TextNormalizer.normalizedSentence(productRequest.description());

        Product updatedProduct = foundProduct.updateProduct(productRequest.price(), normalizedDescription);

        Product savedProduct = productRepository.save(updatedProduct);

        return convertIntoProductResponse(savedProduct);
    }

    @Transactional
    public ProductResponse archiveProduct(Long productId) {

        Product foundProduct = findProductEntityById(productId);
        Product archivedProduct = productRepository.save(foundProduct.archiveProduct());
        return convertIntoProductResponse(archivedProduct);
    }

    @Transactional
    public ProductResponse reactivateProduct(Long productId) {

        Product foundProduct = findProductEntityById(productId);
        Product reactivateProduct = productRepository.save(foundProduct.reactivateProduct());
        return convertIntoProductResponse(reactivateProduct);
    }

    public Product findProductEntityById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    private String generateUniqueQrCode() {

        String generatedCode;
        do {
            generatedCode = ProductCodeGenerator.generateCode();

        } while (productRepository.existsByQrCode(generatedCode));

        return generatedCode;
    }

    private ProductResponse convertIntoProductResponse(Product product) {
        return new ProductResponse(product.getProductId(), product.getQrCode(),
                product.getDescription(), product.getPrice(), product.getStatus());
    }


}
