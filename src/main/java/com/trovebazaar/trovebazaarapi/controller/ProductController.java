package com.trovebazaar.trovebazaarapi.controller;

import com.google.zxing.WriterException;
import com.trovebazaar.trovebazaarapi.dto.CreateProductRequest;
import com.trovebazaar.trovebazaarapi.dto.ProductResponse;
import com.trovebazaar.trovebazaarapi.dto.UpdateProductRequest;
import com.trovebazaar.trovebazaarapi.model.Product;
import com.trovebazaar.trovebazaarapi.service.ProductService;
import com.trovebazaar.trovebazaarapi.service.QrCodeService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final QrCodeService qrCodeService;

    public ProductController(ProductService productService,  QrCodeService qrCodeService) {
        this.productService = productService;
        this.qrCodeService = qrCodeService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid CreateProductRequest productRequest) {

        ProductResponse productResponse = productService.createProduct(productRequest);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productResponse.productId())
                .toUri();

        return ResponseEntity.created(uri).body(productResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {

        ProductResponse productResponse = productService.findProductById(id);
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {

        List<ProductResponse> productResponseList = productService.findAllProducts();
        return ResponseEntity.ok(productResponseList);
    }

    @GetMapping("/archive")
    public ResponseEntity<List<ProductResponse>> getAllArchivedProducts() {

        List<ProductResponse> productResponseList = productService.findAllArchivedProducts();
        return ResponseEntity.ok(productResponseList);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProductResponse>> getAllActiveProducts() {

        List<ProductResponse> productResponseList = productService.findAllActiveProducts();
        return ResponseEntity.ok(productResponseList);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @RequestBody @Valid UpdateProductRequest productRequest) {

        ProductResponse productResponse = productService.updateProduct(id, productRequest);
        return  ResponseEntity.ok(productResponse);
    }

    @PutMapping("/{id}/reactive")
    public ResponseEntity<ProductResponse> reactivateProduct(@PathVariable Long id) {

        ProductResponse productResponse = productService.reactivateProduct(id);
        return  ResponseEntity.ok(productResponse);
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<ProductResponse> archiveProduct(@PathVariable Long id) {

        ProductResponse productResponse = productService.archiveProduct(id);
        return  ResponseEntity.ok(productResponse);
    }


    @GetMapping("/{id}/qr")
    public ResponseEntity<byte[]> createQrCode(@PathVariable Long id) throws WriterException, IOException {

        Product foundProduct = productService.findProductEntityById(id);

        BufferedImage qrImage = qrCodeService.generateQrCodeImage(foundProduct.getQrCode());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        boolean written = ImageIO.write(qrImage, "PNG", outputStream);

        if (!written) {
            throw new IllegalStateException("No PNG image writer is available");
        }

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(outputStream.toByteArray());
    }
}
