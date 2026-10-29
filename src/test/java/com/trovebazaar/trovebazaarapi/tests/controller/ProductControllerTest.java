package com.trovebazaar.trovebazaarapi.tests.controller;

import com.trovebazaar.trovebazaarapi.controller.ProductController;
import com.trovebazaar.trovebazaarapi.dto.CreateProductRequest;
import com.trovebazaar.trovebazaarapi.dto.ProductResponse;
import com.trovebazaar.trovebazaarapi.dto.UpdateProductRequest;
import com.trovebazaar.trovebazaarapi.exception.ResourceNotFoundException;
import com.trovebazaar.trovebazaarapi.model.Money;

import static com.trovebazaar.trovebazaarapi.model.ProductStatus.ARCHIVADO;
import static com.trovebazaar.trovebazaarapi.model.ProductStatus.ACTIVO;

import com.trovebazaar.trovebazaarapi.service.ProductService;
import com.trovebazaar.trovebazaarapi.service.QrCodeService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private QrCodeService qrCodeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Validator validator;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetCreatedStatus() throws Exception {

        CreateProductRequest productRequest = new CreateProductRequest(new Money(1000L),
                "Product test description");

        String json = objectMapper.writeValueAsString(productRequest);

        ProductResponse response = new ProductResponse(1L, "testCode",
                "descriptionTest", new Money(10000L), ACTIVO);

        when(productService.createProduct(productRequest))
                .thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .with(user("testuser"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.qrCode").value("testCode"))
                .andExpect(jsonPath("$.description").value("descriptionTest"))
                .andExpect(jsonPath("$.price.priceInCents").value(10000L))
                .andExpect(jsonPath("$.productStatus").value("ACTIVO"));

        verify(productService).createProduct(any(CreateProductRequest.class));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "",
                    " ",
                    "    "
            }
    )
    void shouldReturnApiErrorWhenProductDescriptionIsBlank(String description) throws Exception {

        CreateProductRequest productRequest = new CreateProductRequest(new Money(1000L),
                description);

        String json = objectMapper.writeValueAsString(productRequest);

        mockMvc.perform(post("/api/products")
                        .with(user("testuser"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.description").value("A description is required"))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(productService);
    }

    @Test
    void shouldReturnApiErrorWhenDescriptionIsGreaterThanMaxLengthWhenCreatingProduct() throws Exception {

        String description = "a".repeat(71);
        CreateProductRequest productRequest = new CreateProductRequest(new Money(1000L),
                description);

        String json = objectMapper.writeValueAsString(productRequest);

        mockMvc.perform(post("/api/products")
                        .with(user("testuser"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.description").value("Description can't exceed seventy characters"))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(productService);
    }

    @Test
    void shouldReturnApiErrorWhenPriceIsNullWhenCreatingProduct() throws Exception {

        CreateProductRequest productRequest = new CreateProductRequest(null,
                "Description Test");

        String json = objectMapper.writeValueAsString(productRequest);

        mockMvc.perform(post("/api/products")
                        .with(user("testuser"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").value("A price must be specified"))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(productService);
    }

    @Test
    void shouldReturnApiErrorWhenPriceIsNegativeWhenCreatingProduct() throws Exception {

        CreateProductRequest  productRequest = new CreateProductRequest(new Money(-1000L),
                "Description Test");

        String json = objectMapper.writeValueAsString(productRequest);

        mockMvc.perform(post("/api/products")
                        .with(user("testuser"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['price.priceInCents']").value("A price must be greater than zero"))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(productService);
    }

    @Test
    void shouldRejectBlankDescriptionsWhenCreatingProduct() throws Exception {

        CreateProductRequest productRequest = new CreateProductRequest(new Money(1000L),
                "");

        Set<ConstraintViolation<CreateProductRequest>> constraintViolations = validator.validate(productRequest);
        assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<CreateProductRequest> constraintViolation = constraintViolations.iterator().next();

        assertThat(constraintViolations).isNotEmpty();

        assertThat(constraintViolation.getMessage()).isEqualTo("A description is required");
        assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("description");

    }

    @Test
    void shouldRejectDescriptionWithMoreThanMaxLengthWhenCreatingProduct() throws Exception {

        String description = "a".repeat(71);
        CreateProductRequest productRequest = new CreateProductRequest(new Money(1000L),
                description);

        Set<ConstraintViolation<CreateProductRequest>> violations = validator.validate(productRequest);

        assertThat(violations).hasSize(1);

        ConstraintViolation<CreateProductRequest> constraintViolation = violations.iterator().next();

        assertThat(constraintViolation.getMessage()).isEqualTo("Description can't exceed seventy characters");
        assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("description");
    }

    @Test
    void shouldRejectNullPriceInCentsWhenCreatingProduct() throws Exception {

        CreateProductRequest productRequest = new CreateProductRequest(null, "Description Test");

        Set<ConstraintViolation<CreateProductRequest>> violations = validator.validate(productRequest);

        assertThat(violations).hasSize(1);

        ConstraintViolation<CreateProductRequest> constraintViolation = violations.iterator().next();

        assertThat(constraintViolation.getMessage()).isEqualTo("A price must be specified");
        assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("price");
    }

    @Test
    void shouldRejectNegativePriceInCentsWhenCreatingProduct() throws Exception {

        CreateProductRequest productRequest = new CreateProductRequest(new Money(-1000L),
                "Description Test");

        Set<ConstraintViolation<CreateProductRequest>> violations = validator.validate(productRequest);

        assertThat(violations).hasSize(1);
        ConstraintViolation<CreateProductRequest> constraintViolation = violations.iterator().next();

        assertThat(constraintViolation.getMessage()).isEqualTo("A price must be greater than zero");
        assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("price.priceInCents");
    }

    @Test
    void shouldAcceptValidProductRequest() {
        CreateProductRequest productRequest =
                new CreateProductRequest(
                        new Money(1000L),
                        "Product test description"
                );

        Set<ConstraintViolation<CreateProductRequest>> violations =
                validator.validate(productRequest);

        assertThat(violations).isEmpty();
    }


    @Test
    void shouldGetProductById() throws Exception {

        Long productId = 1L;

        ProductResponse response = new ProductResponse(1L, "testCode",
                "descriptionTest", new Money(10000L), ACTIVO);

        when(productService.findProductById(productId))
                .thenReturn(response);

        mockMvc.perform(get("/api/products/{id}", productId)
                        .with(user("testuser"))
                        .with(csrf()))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.qrCode").value("testCode"))
                .andExpect(jsonPath("$.description").value("descriptionTest"))
                .andExpect(jsonPath("$.price.priceInCents").value(10000L))
                .andExpect(jsonPath("$.productStatus").value("ACTIVO"));

        verify(productService).findProductById(productId);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() throws Exception {
        Long productId = 1L;

        when(productService.findProductById(productId))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(get("/api/products/{id}", productId)
                        .with(user("testuser"))
                        .with(csrf()))

                .andExpect(status().isNotFound());

        verify(productService).findProductById(productId);
    }

    @Test
    void shouldReturnAllProducts() throws Exception {

        ProductResponse response1 = new ProductResponse(1L, "testCode1",
                "descriptionTest", new Money(10000L), ACTIVO);

        ProductResponse response2 = new ProductResponse(2L, "testCode2",
                "descriptionTest", new Money(10000L), ACTIVO);

        ProductResponse response3 = new ProductResponse(3L, "testCode3",
                "descriptionTest", new Money(10000L), ARCHIVADO);

        when(productService.findAllProducts())
                .thenReturn(List.of(response1, response2, response3));

        mockMvc.perform(get("/api/products")
                        .with(user("testuser"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].productId").value(1L))
                .andExpect(jsonPath("$[1].productId").value(2L))
                .andExpect(jsonPath("$[2].productId").value(3L))
                .andExpect(jsonPath("$[0].qrCode").value("testCode1"))
                .andExpect(jsonPath("$[1].qrCode").value("testCode2"))
                .andExpect(jsonPath("$[2].qrCode").value("testCode3"));

        verify(productService).findAllProducts();
    }

    @Test
    void shouldReturnAllActiveProducts() throws Exception {

        ProductResponse response1 = new ProductResponse(1L, "testCode1",
                "descriptionTest", new Money(10000L), ACTIVO);

        ProductResponse response2 = new ProductResponse(2L, "testCode2",
                "descriptionTest", new Money(10000L), ACTIVO);

        ProductResponse response3 = new ProductResponse(3L, "testCode3",
                "descriptionTest", new Money(10000L), ACTIVO);

        when(productService.findAllActiveProducts())
                .thenReturn(List.of(response1, response2, response3));

        mockMvc.perform(get("/api/products/active")
                        .with(user("testuser"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].productId").value(1L))
                .andExpect(jsonPath("$[1].productId").value(2L))
                .andExpect(jsonPath("$[2].productId").value(3L))
                .andExpect(jsonPath("$[0].qrCode").value("testCode1"))
                .andExpect(jsonPath("$[1].qrCode").value("testCode2"))
                .andExpect(jsonPath("$[2].qrCode").value("testCode3"));

        verify(productService).findAllActiveProducts();
    }

    @Test
    void shouldReturnAllArchivedProducts() throws Exception {

        ProductResponse response1 = new ProductResponse(1L, "testCode1",
                "descriptionTest", new Money(10000L), ARCHIVADO);

        ProductResponse response2 = new ProductResponse(2L, "testCode2",
                "descriptionTest", new Money(10000L), ARCHIVADO);

        ProductResponse response3 = new ProductResponse(3L, "testCode3",
                "descriptionTest", new Money(10000L), ARCHIVADO);

        when(productService.findAllArchivedProducts())
                .thenReturn(List.of(response1, response2, response3));

        mockMvc.perform(get("/api/products/archive")
                        .with(user("testuser"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].productId").value(1L))
                .andExpect(jsonPath("$[1].productId").value(2L))
                .andExpect(jsonPath("$[2].productId").value(3L))
                .andExpect(jsonPath("$[0].qrCode").value("testCode1"))
                .andExpect(jsonPath("$[1].qrCode").value("testCode2"))
                .andExpect(jsonPath("$[2].qrCode").value("testCode3"));

        verify(productService).findAllArchivedProducts();
    }

    @Test
    void shouldUpdateProductSuccessfully() throws Exception {

        Long productId = 1L;

        UpdateProductRequest productRequest = new UpdateProductRequest(new Money(50000L),
                "updated product description");

        ProductResponse response = new ProductResponse(productId, "testcode",
                "updated product description", new Money(50000L), ACTIVO);

        when(productService.updateProduct(productId, productRequest))
                .thenReturn(response);

        String json = objectMapper.writeValueAsString(productRequest);

        mockMvc.perform(put("/api/products/{id}", productId)
                        .with(user("usertest"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.qrCode").value("testcode"))
                .andExpect(jsonPath("$.description").value("updated product description"))
                .andExpect(jsonPath("$.productStatus").value("ACTIVO"));

        verify(productService).updateProduct(productId, productRequest);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "    "
    })
    void shouldReturnBadRequestWhenUpdatingProductWithBlankDescription(
            String description
    ) throws Exception {

        UpdateProductRequest request =
                new UpdateProductRequest(new Money(1000L), description);

        mockMvc.perform(put("/api/products/{id}", 1L)
                        .with(user("testuser"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.description")
                        .value("A description is required"))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(productService);
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingProductWithDescriptionLongerThanMaximumLength()
            throws Exception {

        UpdateProductRequest request =
                new UpdateProductRequest(new Money(1000L), "a".repeat(71));

        mockMvc.perform(put("/api/products/{id}", 1L)
                        .with(user("testuser"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.description")
                        .value("Description can't exceed seventy characters"))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(productService);
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingProductWithNullPrice()
            throws Exception {

        UpdateProductRequest request =
                new UpdateProductRequest(null, "Description Test");

        mockMvc.perform(put("/api/products/{id}", 1L)
                        .with(user("testuser"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price")
                        .value("A price must be specified"))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(productService);
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingProductWithNegativePrice()
            throws Exception {

        UpdateProductRequest request =
                new UpdateProductRequest(new Money(-1000L), "Description Test");

        mockMvc.perform(put("/api/products/{id}", 1L)
                        .with(user("testuser"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['price.priceInCents']")
                        .value("A price must be greater than zero"))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(productService);
    }

    @Test
    void shouldFailValidationWhenUpdatingProductWithBlankDescription() {

        UpdateProductRequest request =
                new UpdateProductRequest(new Money(1000L), " ");

        Set<ConstraintViolation<UpdateProductRequest>> violations =
                validator.validate(request);

        assertThat(violations).hasSize(1);

        ConstraintViolation<UpdateProductRequest> violation =
                violations.iterator().next();

        assertThat(violation.getMessage())
                .isEqualTo("A description is required");

        assertThat(violation.getPropertyPath().toString())
                .isEqualTo("description");
    }

    @Test
    void shouldFailValidationWhenUpdatingProductWithDescriptionLongerThanMaximumLength() {

        UpdateProductRequest request =
                new UpdateProductRequest(new Money(1000L), "a".repeat(71));

        Set<ConstraintViolation<UpdateProductRequest>> violations =
                validator.validate(request);

        assertThat(violations).hasSize(1);

        ConstraintViolation<UpdateProductRequest> violation =
                violations.iterator().next();

        assertThat(violation.getMessage())
                .isEqualTo("Description can't exceed seventy characters");

        assertThat(violation.getPropertyPath().toString())
                .isEqualTo("description");
    }

    @Test
    void shouldFailValidationWhenUpdatingProductWithNullPrice() {

        UpdateProductRequest request =
                new UpdateProductRequest(null, "Description Test");

        Set<ConstraintViolation<UpdateProductRequest>> violations =
                validator.validate(request);

        assertThat(violations).hasSize(1);

        ConstraintViolation<UpdateProductRequest> violation =
                violations.iterator().next();

        assertThat(violation.getMessage())
                .isEqualTo("A price must be specified");

        assertThat(violation.getPropertyPath().toString())
                .isEqualTo("price");
    }

    @Test
    void shouldFailValidationWhenUpdatingProductWithNegativePrice() {

        UpdateProductRequest request =
                new UpdateProductRequest(new Money(-1000L), "Description Test");

        Set<ConstraintViolation<UpdateProductRequest>> violations =
                validator.validate(request);

        assertThat(violations).hasSize(1);

        ConstraintViolation<UpdateProductRequest> violation =
                violations.iterator().next();

        assertThat(violation.getMessage())
                .isEqualTo("A price must be greater than zero");

        assertThat(violation.getPropertyPath().toString())
                .isEqualTo("price.priceInCents");
    }

    @Test
    void shouldReactivateProductSuccessfully() throws Exception {
        Long productId = 1L;

        ProductResponse response = new ProductResponse(productId, "testcode",
                "product description", new Money(50000L), ACTIVO);

        when(productService.reactivateProduct(productId))
                .thenReturn(response);

        mockMvc.perform(put("/api/products/{id}/reactive", productId)
                        .with(user("testuser"))
                        .with(csrf()))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.qrCode").value("testcode"))
                .andExpect(jsonPath("$.description").value("product description"))
                .andExpect(jsonPath("$.productStatus").value("ACTIVO"));

        verify(productService).reactivateProduct(productId);
    }

    @Test
    void shouldArchiveProductSuccessfully() throws Exception {
        Long productId = 1L;

        ProductResponse response = new ProductResponse(productId, "testcode",
                "product description", new Money(50000L), ARCHIVADO);

        when(productService.archiveProduct(productId))
                .thenReturn(response);

        mockMvc.perform(put("/api/products/{id}/archive", productId)
                        .with(user("testuser"))
                        .with(csrf()))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.qrCode").value("testcode"))
                .andExpect(jsonPath("$.description").value("product description"))
                .andExpect(jsonPath("$.productStatus").value("ARCHIVADO"));

        verify(productService).archiveProduct(productId);
    }
}
