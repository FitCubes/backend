package fitcubes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fitcubes.config.TestSecurityConfig;
import fitcubes.dto.product.CreateProductDto;
import fitcubes.dto.product.ProductDto;
import fitcubes.dto.product.UpdateProductDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.model.product.ProductCategory;
import fitcubes.security.JwtUtil;
import fitcubes.security.TokenBlacklistService;
import fitcubes.service.product.AdminProductService;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminProductController.class)
@Import(TestSecurityConfig.class)
class AdminProductControllerTest {

    private static final String BASE_URL = "/api/admin/products";
    private static final Long PRODUCT_ID = 10L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminProductService adminProductService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    private CreateProductDto createProductDto;
    private UpdateProductDto updateProductDto;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        ProductCategory category = ProductCategory.values()[0];
        createProductDto = new CreateProductDto("Rice", category, 130, 0.3, 28.0, 2.7);
        updateProductDto = new UpdateProductDto("Brown Rice", category, 140, 0.5, 29.0, 3.0);
        productDto = new ProductDto(PRODUCT_ID, "Rice", category, 130, 0.3, 28.0, 2.7, 10.8);
    }

    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        @DisplayName("createProduct_asAdmin_returnsCreatedProduct")
        @WithMockUser(roles = "ADMIN")
        void createProduct_asAdmin_returnsCreatedProduct() throws Exception {
            given(adminProductService.save(createProductDto)).willReturn(productDto);

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createProductDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(PRODUCT_ID))
                    .andExpect(jsonPath("$.name").value("Rice"));

            verify(adminProductService).save(createProductDto);
        }

        @Test
        @DisplayName("createProduct_blankName_returnsBadRequest")
        @WithMockUser(roles = "ADMIN")
        void createProduct_blankName_returnsBadRequest() throws Exception {
            CreateProductDto invalidDto = new CreateProductDto(
                    " ", createProductDto.category(), 130, 0.3, 28.0, 2.7);

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(adminProductService, never()).save(any());
        }

        @Test
        @DisplayName("createProduct_negativeCalories_returnsBadRequest")
        @WithMockUser(roles = "ADMIN")
        void createProduct_negativeCalories_returnsBadRequest() throws Exception {
            CreateProductDto invalidDto = new CreateProductDto(
                    "Rice", createProductDto.category(), -10, 0.3, 28.0, 2.7);

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(adminProductService, never()).save(any());
        }

        @Test
        @DisplayName("createProduct_notAdmin_returnsForbidden")
        @WithMockUser(roles = "USER")
        void createProduct_notAdmin_returnsForbidden() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createProductDto)))
                    .andExpect(status().isForbidden());

            verify(adminProductService, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAllProducts")
    class GetAllProducts {

        @Test
        @DisplayName("getAllProducts_asAdmin_returnsPage")
        @WithMockUser(roles = "ADMIN")
        void getAllProducts_asAdmin_returnsPage() throws Exception {
            Page<ProductDto> page = new PageImpl<>(List.of(productDto));
            given(adminProductService.getAllProducts(any(Pageable.class))).willReturn(page);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(PRODUCT_ID));
        }

        @Test
        @DisplayName("getAllProducts_notAdmin_returnsForbidden")
        @WithMockUser(roles = "USER")
        void getAllProducts_notAdmin_returnsForbidden() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());

            verify(adminProductService, never()).getAllProducts(any());
        }
    }

    @Nested
    @DisplayName("getProductById")
    class GetProductById {

        @Test
        @DisplayName("getProductById_asAdmin_returnsProduct")
        @WithMockUser(roles = "ADMIN")
        void getProductById_asAdmin_returnsProduct() throws Exception {
            given(adminProductService.getProductById(PRODUCT_ID)).willReturn(productDto);

            mockMvc.perform(get(BASE_URL + "/{productId}", PRODUCT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PRODUCT_ID));
        }

        @Test
        @DisplayName("getProductById_notFound_returnsNotFound")
        @WithMockUser(roles = "ADMIN")
        void getProductById_notFound_returnsNotFound() throws Exception {
            given(adminProductService.getProductById(PRODUCT_ID))
                    .willThrow(new EntityNotFoundException(
                            "Product with productId: " + PRODUCT_ID + " not found"));

            mockMvc.perform(get(BASE_URL + "/{productId}", PRODUCT_ID))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("getProductById_notAdmin_returnsForbidden")
        @WithMockUser(roles = "USER")
        void getProductById_notAdmin_returnsForbidden() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{productId}", PRODUCT_ID))
                    .andExpect(status().isForbidden());

            verify(adminProductService, never()).getProductById(anyLong());
        }
    }

    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {

        @Test
        @DisplayName("updateProduct_asAdmin_returnsUpdatedProduct")
        @WithMockUser(roles = "ADMIN")
        void updateProduct_asAdmin_returnsUpdatedProduct() throws Exception {
            given(adminProductService.update(updateProductDto, PRODUCT_ID)).willReturn(productDto);

            mockMvc.perform(patch(BASE_URL + "/{productId}", PRODUCT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateProductDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PRODUCT_ID));

            verify(adminProductService).update(updateProductDto, PRODUCT_ID);
        }

        @Test
        @DisplayName("updateProduct_notFound_returnsNotFound")
        @WithMockUser(roles = "ADMIN")
        void updateProduct_notFound_returnsNotFound() throws Exception {
            given(adminProductService.update(updateProductDto, PRODUCT_ID))
                    .willThrow(new EntityNotFoundException(
                            "Product with productId: " + PRODUCT_ID + " not found"));

            mockMvc.perform(patch(BASE_URL + "/{productId}", PRODUCT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateProductDto)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("updateProduct_notAdmin_returnsForbidden")
        @WithMockUser(roles = "USER")
        void updateProduct_notAdmin_returnsForbidden() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{productId}", PRODUCT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateProductDto)))
                    .andExpect(status().isForbidden());

            verify(adminProductService, never()).update(any(), anyLong());
        }
    }

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProduct {

        @Test
        @DisplayName("deleteProduct_asAdmin_returnsNoContent")
        @WithMockUser(roles = "ADMIN")
        void deleteProduct_asAdmin_returnsNoContent() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{productId}", PRODUCT_ID)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(adminProductService).deleteById(PRODUCT_ID);
        }

        @Test
        @DisplayName("deleteProduct_notFound_returnsNotFound")
        @WithMockUser(roles = "ADMIN")
        void deleteProduct_notFound_returnsNotFound() throws Exception {
            org.mockito.Mockito.doThrow(new EntityNotFoundException(
                            "Product with productId: " + PRODUCT_ID + " not found"))
                    .when(adminProductService).deleteById(PRODUCT_ID);

            mockMvc.perform(delete(BASE_URL + "/{productId}", PRODUCT_ID)
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deleteProduct_notAdmin_returnsForbidden")
        @WithMockUser(roles = "USER")
        void deleteProduct_notAdmin_returnsForbidden() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{productId}", PRODUCT_ID)
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(adminProductService, never()).deleteById(anyLong());
        }
    }
}