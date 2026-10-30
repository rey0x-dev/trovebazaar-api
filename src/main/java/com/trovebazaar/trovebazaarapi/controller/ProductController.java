package com.trovebazaar.trovebazaarapi.controller;

import com.google.zxing.WriterException;
import com.trovebazaar.trovebazaarapi.dto.CreateProductRequest;
import com.trovebazaar.trovebazaarapi.dto.ProductResponse;
import com.trovebazaar.trovebazaarapi.dto.UpdateProductRequest;
import com.trovebazaar.trovebazaarapi.exception.ApiError;
import com.trovebazaar.trovebazaarapi.model.Product;
import com.trovebazaar.trovebazaarapi.service.ProductService;
import com.trovebazaar.trovebazaarapi.service.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    public ProductController(ProductService productService, QrCodeService qrCodeService) {
        this.productService = productService;
        this.qrCodeService = qrCodeService;
    }

    @Operation(
            summary = "Create a product",
            description = "Creates a new product and returns its details"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Product created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or business rule violation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or business rule violation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Validation error",
                                    summary = "The product price must be greater than zero",
                                    value = """
                                            {
                                              "errorMessage": "Request validation failed",
                                              "errorTimestamp": "2026-10-29T16:30:00",
                                              "errors": {
                                                "price.priceInCents": "A price must be greater than zero"
                                              },
                                              "status": 400
                                            }
                                            """
                            )
                    )
            )
    })
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


    @Operation(
            summary = "Get product by Id",
            description = "Retrieves a product by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or business rule violation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Product not found",
                                    summary = "The requested product does not exist",
                                    value = """
                                            {
                                              "errorMessage": "Product not found with id 1",
                                              "errorTimestamp": "2026-10-29T16:30:00",
                                              "errors": null,
                                              "status": 404
                                            }
                                            """
                            )
                    )
            )

    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(
                    description = "Unique identifier of the product",
                    example = "1"
            )
            @PathVariable Long id) {

        ProductResponse productResponse = productService.findProductById(id);
        return ResponseEntity.ok(productResponse);
    }


    @Operation(
            summary = "Gets all products",
            description = "Gets all products and their details, if no products exist an empty list is returned"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Products retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ProductResponse.class)
                            ),
                            examples = {
                                    @ExampleObject(
                                    name = "Products found",
                                    summary = "A list containing two products",
                                    value = """
                                            [
                                              {
                                                "productId": 1,
                                                "qrCode": "TBZ001",
                                                "description": "Camisas",
                                                "price": {
                                                  "priceInCents": 50000
                                                },
                                                "productStatus": "ACTIVO"
                                              },
                                              {
                                                "productId": 2,
                                                "qrCode": "TBZ002",
                                                "description": "Pantalones",
                                                "price": {
                                                  "priceInCents": 15000
                                                },
                                                "productStatus": "ARCHIVADO"
                                              }
                                            ]
                                            """
                            ),
                                    @ExampleObject(
                                            name = "No products found",
                                            summary = "An empty product list",
                                            value = "[]"
                                    )
                            }
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {

        List<ProductResponse> productResponseList = productService.findAllProducts();
        return ResponseEntity.ok(productResponseList);
    }


    @Operation(
            summary = "Get all archived products",
            description = "Returns all archived products and their details, if not archived products exist an empty list is returned"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Retrieve all archived products successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ProductResponse.class)
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "Products found",
                                            summary = "A list containing two products",
                                            value = """
                                            [
                                              {
                                                "productId": 1,
                                                "qrCode": "TBZ001",
                                                "description": "Camisas",
                                                "price": {
                                                  "priceInCents": 50000
                                                },
                                                "productStatus": "ARCHIVADO"
                                              },
                                              {
                                                "productId": 2,
                                                "qrCode": "TBZ002",
                                                "description": "Pantalones",
                                                "price": {
                                                  "priceInCents": 15000
                                                },
                                                "productStatus": "ARCHIVADO"
                                              }
                                            ]
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "No products found",
                                            summary = "An empty product list",
                                            value = "[]"
                                    )
                            }
                    )
            )
    })
    @GetMapping("/archive")
    public ResponseEntity<List<ProductResponse>> getAllArchivedProducts() {

        List<ProductResponse> productResponseList = productService.findAllArchivedProducts();
        return ResponseEntity.ok(productResponseList);
    }


    @Operation(
            summary = "Get all active products",
            description = "Returns all active products and their details, if no active products exist an empty list is returned"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Retrieve all active products successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ProductResponse.class)
                            ),
                            examples =  {
                                    @ExampleObject(
                                            name = "Products found",
                                            summary = "A list containing two products",
                                            value = """
                                            [
                                              {
                                                "productId": 1,
                                                "qrCode": "TBZ001",
                                                "description": "Camisas",
                                                "price": {
                                                  "priceInCents": 50000
                                                },
                                                "productStatus": "ACTIVO"
                                              },
                                              {
                                                "productId": 2,
                                                "qrCode": "TBZ002",
                                                "description": "Pantalones",
                                                "price": {
                                                  "priceInCents": 15000
                                                },
                                                "productStatus": "ACTIVO"
                                              }
                                            ]
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "No products found",
                                            summary = "An empty product list",
                                            value = "[]"
                                    )
                            }
                    )
            )
    })
    @GetMapping("/active")
    public ResponseEntity<List<ProductResponse>> getAllActiveProducts() {

        List<ProductResponse> productResponseList = productService.findAllActiveProducts();
        return ResponseEntity.ok(productResponseList);
    }


    @Operation(
            summary = "Update product",
            description = "Returns the updated product and its details, if the id of the requested product doesn't exist an exception is expected"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Updates product successfully by its unique identifier",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or business rule violation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Product not found",
                                    summary = "The requested product does not exist",
                                    value = """
                                            {
                                              "errorMessage": "Product not found with id 1",
                                              "errorTimestamp": "2026-10-29T16:30:00",
                                              "errors": null,
                                              "status": 404
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or business rule violation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Validation error",
                                    summary = "The product price must be greater than zero",
                                    value = """
                                            {
                                              "errorMessage": "Request validation failed",
                                              "errorTimestamp": "2026-10-29T16:30:00",
                                              "errors": {
                                                "price.priceInCents": "A price must be greater than zero"
                                              },
                                              "status": 400
                                            }
                                            """
                            )
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(
                    description = "Unique identifier of the product",
                    example = "1"
            )
            @PathVariable Long id,
            @RequestBody @Valid UpdateProductRequest productRequest) {

        ProductResponse productResponse = productService.updateProduct(id, productRequest);
        return ResponseEntity.ok(productResponse);
    }


    @Operation(
            summary = "Reactivate a product",
            description = "Reactivates the product identified by its unique ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product reactivated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ProductResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiError.class
                            ),
                            examples = @ExampleObject(
                                    name = "Product not found",
                                    summary = "The requested product does not exist",
                                    value = """
                                        {
                                          "errorMessage": "Product not found with id 1",
                                          "errorTimestamp": "2026-10-29T16:30:00",
                                          "errors": null,
                                          "status": 404
                                        }
                                        """
                            )
                    )
            )
    })
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<ProductResponse> reactivateProduct(
            @Parameter(
                    description = "Unique identifier of the product to reactivate",
                    example = "1"
            )
            @PathVariable Long id) {

        ProductResponse productResponse = productService.reactivateProduct(id);
        return ResponseEntity.ok(productResponse);
    }


    @Operation(
            summary = "Reactivate a product",
            description = "Reactivates the product identified by its unique ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product reactivated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ProductResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiError.class
                            ),
                            examples = @ExampleObject(
                                    name = "Product not found",
                                    summary = "The requested product does not exist",
                                    value = """
                                        {
                                          "errorMessage": "Product not found with id 1",
                                          "errorTimestamp": "2026-10-29T16:30:00",
                                          "errors": null,
                                          "status": 404
                                        }
                                        """
                            )
                    )
            )
    })
    @PutMapping("/{id}/archive")
    public ResponseEntity<ProductResponse> archiveProduct(
            @Parameter(
                    description = "Unique identifier of the product to archive",
                    example = "1"
            )
            @PathVariable Long id) {

        ProductResponse productResponse = productService.archiveProduct(id);
        return ResponseEntity.ok(productResponse);
    }


    @Operation(
            summary = "Get product QR code",
            description = "Generates and returns the QR code of the specified product as a PNG image."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "QR code generated successfully",
                    content = @Content(
                            mediaType = "image/png",
                            schema = @Schema(
                                    type = "string",
                                    format = "binary"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiError.class
                            ),
                            examples = @ExampleObject(
                                    name = "Product not found",
                                    summary = "The requested product does not exist",
                                    value = """
                                        {
                                          "errorMessage": "Product not found with id 1",
                                          "errorTimestamp": "2026-10-29T16:30:00",
                                          "errors": null,
                                          "status": 404
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "An unexpected error occurred while generating the QR code."
            )
    })
    @GetMapping("/{id}/qr")
    public ResponseEntity<byte[]> createQrCode(
            @Parameter(
                    description = "Unique identifier of the product",
                    example = "1"
            )
            @PathVariable Long id) throws WriterException, IOException {

        Product foundProduct = productService.findProductEntityById(id);

        BufferedImage qrImage =
                qrCodeService.generateQrCodeImage(foundProduct.getQrCode());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        boolean written = ImageIO.write(qrImage, "PNG", outputStream);

        if (!written) {
            throw new IllegalStateException("No PNG image writer is available");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(outputStream.toByteArray());
    }
}
