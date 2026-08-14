package fitcubes.controller;

import fitcubes.dto.product.CreateProductDto;
import fitcubes.dto.product.ProductDto;
import fitcubes.dto.product.UpdateProductDto;
import fitcubes.service.AdminProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/products")
@Tag(name = "Admin Product Management",
        description = "Endpoints for managing global system products")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @Operation(
            summary = "Create global system product",
            description = "Creates a new product available to all users in the default dictionary.",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Global product created successfully"),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid payload provided"),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied: Admin role required")
            }
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ProductDto createProduct(@Valid @RequestBody CreateProductDto createProductDto) {
        return adminProductService.save(createProductDto);
    }

    @Operation(
            summary = "Get all products (Admin)",
            description = "Retrieves a paginated list of all products, "
                    + "including both global and user-owned products.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Products retrieved successfully"),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied: Admin role required")
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        return adminProductService.getAllProducts(pageable);
    }

    @Operation(
            summary = "Get product by ID (Admin)",
            description = "Retrieves any product by its ID regardless of ownership.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Product retrieved successfully"),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied: Admin role required"),
                    @ApiResponse(responseCode = "404",
                            description = "Product not found")
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{productId}")
    public ProductDto getProductById(@PathVariable Long productId) {
        return adminProductService.getProductById(productId);
    }

    @Operation(
            summary = "Update global or user product (Admin)",
            description = "Updates any product by its ID without owner restrictions.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Product updated successfully"),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid payload provided"),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied: Admin role required"),
                    @ApiResponse(responseCode = "404",
                            description = "Product not found")
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{productId}")
    public ProductDto updateProduct(@PathVariable Long productId,
                                    @RequestBody @Valid UpdateProductDto updateProductDto) {
        return adminProductService.update(updateProductDto, productId);
    }

    @Operation(
            summary = "Delete global or user product (Admin)",
            description = "Deletes any product by its ID without owner restrictions.",
            responses = {
                    @ApiResponse(responseCode = "204",
                            description = "Product deleted successfully"),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied: Admin role required"),
                    @ApiResponse(responseCode = "404",
                            description = "Product not found")
            }
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable Long productId) {
        adminProductService.deleteById(productId);
    }
}
