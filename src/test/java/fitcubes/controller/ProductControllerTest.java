package fitcubes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import fitcubes.config.TestSecurityConfig;
import fitcubes.dto.product.CreateProductDto;
import fitcubes.dto.product.ProductDto;
import fitcubes.dto.product.UpdateProductDto;
import fitcubes.exception.AccessDeniedException;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.model.product.ProductCategory;
import fitcubes.model.user.Role;
import fitcubes.model.user.RoleName;
import fitcubes.model.user.User;
import fitcubes.security.JwtUtil;
import fitcubes.security.TokenBlacklistService;
import fitcubes.service.product.ProductService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@Import(TestSecurityConfig.class)
public class ProductControllerTest {

    private static final String BASE_URL = "/api/products";
    private static final Long PRODUCT_ID = 10L;
    private static final Long USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    private CreateProductDto createProductDto;
    private UpdateProductDto updateProductDto;
    private ProductDto productDto;
    private User user;

    @BeforeEach
    void setUp() {
        ProductCategory category = ProductCategory.values()[0];

        createProductDto = new CreateProductDto(
                "Rice",
                category,
                130,
                0.3,
                28.0,
                2.7
        );

        updateProductDto = new UpdateProductDto(
                "Brown Rice",
                category,
                140,
                0.5,
                29.0,
                3.0
        );

        productDto = new ProductDto(
                PRODUCT_ID,
                "Rice",
                category,
                130,
                0.3,
                28.0,
                2.7,
                10.8
        );

        user = new User();
        user.setId(USER_ID);
        user.setEmail("test@example.com");

        Role userRole = new Role();
        userRole.setName(RoleName.USER);

        user.setRoles(Set.of(userRole));
    }

    private UsernamePasswordAuthenticationToken userAuthentication() {
        return new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );
    }

    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        @DisplayName("createProduct_asUser_returnsCreatedProduct")
        void createProduct_asUser_returnsCreatedProduct() throws Exception {
            given(productService.save(createProductDto, USER_ID))
                    .willReturn(productDto);

            mockMvc.perform(post(BASE_URL)
                            .with(authentication(userAuthentication()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createProductDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(PRODUCT_ID))
                    .andExpect(jsonPath("$.name").value("Rice"));

            verify(productService).save(createProductDto, USER_ID);
        }

        @Test
        @DisplayName("createProduct_blankName_returnsBadRequest")
        void createProduct_blankName_returnsBadRequest() throws Exception {
            CreateProductDto invalidDto = new CreateProductDto(
                    " ",
                    createProductDto.category(),
                    130,
                    0.3,
                    28.0,
                    2.7
            );

            mockMvc.perform(post(BASE_URL)
                            .with(authentication(userAuthentication()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(productService, never()).save(any(), anyLong());
        }

        @Test
        @DisplayName("createProduct_negativeCalories_returnsBadRequest")
        void createProduct_negativeCalories_returnsBadRequest() throws Exception {
            CreateProductDto invalidDto = new CreateProductDto(
                    "Rice",
                    createProductDto.category(),
                    -10,
                    0.3,
                    28.0,
                    2.7
            );

            mockMvc.perform(post(BASE_URL)
                            .with(authentication(userAuthentication()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(productService, never()).save(any(), anyLong());
        }
    }

    @Nested
    @DisplayName("getAllProducts")
    class GetAllProducts {

        @Test
        @DisplayName("getAllProducts_asUser_returnsPage")
        void getAllProducts_asUser_returnsPage() throws Exception {
            Page<ProductDto> page = new PageImpl<>(List.of(productDto));

            given(productService.getAllProducts(
                    any(Pageable.class),
                    anyLong()
            )).willReturn(page);

            mockMvc.perform(get(BASE_URL)
                            .with(authentication(userAuthentication())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(PRODUCT_ID));

            verify(productService).getAllProducts(
                    any(Pageable.class),
                    anyLong()
            );
        }
    }

    @Nested
    @DisplayName("getProductById")
    class GetProductById {

        @Test
        @DisplayName("getProductById_asUser_returnsProduct")
        void getProductById_asUser_returnsProduct() throws Exception {
            given(productService.getProductById(PRODUCT_ID, USER_ID))
                    .willReturn(productDto);

            mockMvc.perform(get(BASE_URL + "/{productId}", PRODUCT_ID)
                            .with(authentication(userAuthentication())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PRODUCT_ID));

            verify(productService).getProductById(PRODUCT_ID, USER_ID);
        }

        @Test
        @DisplayName("getProductById_notFound_returnsNotFound")
        void getProductById_notFound_returnsNotFound() throws Exception {
            given(productService.getProductById(PRODUCT_ID, USER_ID))
                    .willThrow(new EntityNotFoundException(
                            "Product with productId: " + PRODUCT_ID + " not found"
                    ));

            mockMvc.perform(get(BASE_URL + "/{productId}", PRODUCT_ID)
                            .with(authentication(userAuthentication())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("getProductById_forbidden_returnsForbidden")
        void getProductById_forbidden_returnsForbidden() throws Exception {
            given(productService.getProductById(PRODUCT_ID, USER_ID))
                    .willThrow(new AccessDeniedException("Access denied"));

            mockMvc.perform(get(BASE_URL + "/{productId}", PRODUCT_ID)
                            .with(authentication(userAuthentication())))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {

        @Test
        @DisplayName("updateProduct_asUser_returnsUpdatedProduct")
        void updateProduct_asUser_returnsUpdatedProduct() throws Exception {
            given(productService.update(
                    updateProductDto,
                    PRODUCT_ID,
                    USER_ID
            )).willReturn(productDto);

            mockMvc.perform(patch(BASE_URL + "/{productId}", PRODUCT_ID)
                            .with(authentication(userAuthentication()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateProductDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PRODUCT_ID));

            verify(productService).update(
                    updateProductDto,
                    PRODUCT_ID,
                    USER_ID
            );
        }

        @Test
        @DisplayName("updateProduct_notFound_returnsNotFound")
        void updateProduct_notFound_returnsNotFound() throws Exception {
            given(productService.update(
                    updateProductDto,
                    PRODUCT_ID,
                    USER_ID
            )).willThrow(new EntityNotFoundException(
                    "Product with productId: " + PRODUCT_ID + " not found"
            ));

            mockMvc.perform(patch(BASE_URL + "/{productId}", PRODUCT_ID)
                            .with(authentication(userAuthentication()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateProductDto)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("updateProduct_forbidden_returnsForbidden")
        void updateProduct_forbidden_returnsForbidden() throws Exception {
            given(productService.update(
                    updateProductDto,
                    PRODUCT_ID,
                    USER_ID
            )).willThrow(new AccessDeniedException("Access denied"));

            mockMvc.perform(patch(BASE_URL + "/{productId}", PRODUCT_ID)
                            .with(authentication(userAuthentication()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateProductDto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProduct {

        @Test
        @DisplayName("deleteProduct_asUser_returnsNoContent")
        void deleteProduct_asUser_returnsNoContent() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{productId}", PRODUCT_ID)
                            .with(authentication(userAuthentication())))
                    .andExpect(status().isNoContent());

            verify(productService).deleteById(PRODUCT_ID, USER_ID);
        }

        @Test
        @DisplayName("deleteProduct_notFound_returnsNotFound")
        void deleteProduct_notFound_returnsNotFound() throws Exception {
            doThrow(new EntityNotFoundException(
                    "Product with productId: " + PRODUCT_ID + " not found"
            )).when(productService).deleteById(PRODUCT_ID, USER_ID);

            mockMvc.perform(delete(BASE_URL + "/{productId}", PRODUCT_ID)
                            .with(authentication(userAuthentication())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deleteProduct_forbidden_returnsForbidden")
        void deleteProduct_forbidden_returnsForbidden() throws Exception {
            doThrow(new AccessDeniedException("Access denied"))
                    .when(productService)
                    .deleteById(PRODUCT_ID, USER_ID);

            mockMvc.perform(delete(BASE_URL + "/{productId}", PRODUCT_ID)
                            .with(authentication(userAuthentication())))
                    .andExpect(status().isForbidden());
        }
    }
}
