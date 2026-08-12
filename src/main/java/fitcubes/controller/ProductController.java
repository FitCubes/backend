package fitcubes.controller;

import fitcubes.dto.product.CreateProductDto;
import fitcubes.dto.product.ProductDto;
import fitcubes.dto.product.UpdateProductDto;
import fitcubes.model.user.User;
import fitcubes.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/products")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@Tag(name = "User Product Management",
        description = "Endpoints for managing user-owned custom products")
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "Create user's custom product",
            description = "Creates a new user-owned custom product. "
                    + "Users can only create products for themselves.",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Product created successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad request")
            }
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ProductDto createProduct(@RequestBody @Valid CreateProductDto createProductDto,
                                    @AuthenticationPrincipal User user) {
        return productService.save(createProductDto, user.getId());
    }

    @Operation(
            summary = "Get product by ID",
            description = "Retrieves a product by its ID. "
                    + "Users can only access products created by themselves or global products.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Product retrieved successfully"),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied: product belongs to another user"),
                    @ApiResponse(responseCode = "404",
                            description = "Product not found")
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{productId}")
    public ProductDto getProductById(@PathVariable Long productId,
                                     @AuthenticationPrincipal User user) {
        return productService.getProductById(productId, user.getId());
    }

    @Operation(
            summary = "Delete user's custom product",
            description = "Deletes a user-owned custom product by its ID. "
                    + "Users can only delete products created by themselves.",
            responses = {
                    @ApiResponse(responseCode = "204",
                            description = "Product deleted successfully"),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied: product belongs to another user "
                                    + "or is global"),
                    @ApiResponse(responseCode = "404",
                            description = "Product not found")
            }
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable Long productId, @AuthenticationPrincipal User user) {
        productService.deleteById(productId, user.getId());
    }

    @Operation(
            summary = "Update user's custom product",
            description = "Updates a user-owned custom product by its ID. "
                    + "Users can only modify products created by themselves.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Product updated successfully"),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied: product belongs to another user "
                                    + "or is global"),
                    @ApiResponse(responseCode = "404",
                            description = "Product not found")
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{productId}")
    public ProductDto updateProduct(@PathVariable Long productId,
                                    @AuthenticationPrincipal User user,
                                    @Valid @RequestBody UpdateProductDto updateProductDto) {
        return productService.update(updateProductDto, productId, user.getId());
    }
}
