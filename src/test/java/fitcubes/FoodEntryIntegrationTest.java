package fitcubes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import fitcubes.dto.foodentry.FoodEntryRequestDto;
import fitcubes.model.foodentry.FoodEntry;
import fitcubes.model.foodentry.MealType;
import fitcubes.model.foodentry.SourceType;
import fitcubes.model.product.Product;
import fitcubes.model.product.ProductCategory;
import fitcubes.model.recipe.Recipe;
import fitcubes.model.user.User;
import fitcubes.repository.FoodEntryRepository;
import fitcubes.repository.ProductRepository;
import fitcubes.repository.RecipeRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class FoodEntryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private FoodEntryRepository foodEntryRepository;

    private static final Long USER_ID = 42L;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(USER_ID);
    }

    private RequestPostProcessor asUser() {
        return authentication(
                new UsernamePasswordAuthenticationToken(
                        testUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    private Product saveProduct(double calories, double fat, double protein, double carbs) {
        Product product = new Product();
        product.setName("Egg");
        product.setCategory(ProductCategory.DAIRY_AND_CHEESE);
        product.setCalories(calories);
        product.setFat(fat);
        product.setProtein(protein);
        product.setCarbohydrates(carbs);
        return productRepository.save(product);
    }

    private Recipe saveRecipeWithPerServingValues(BigDecimal caloriesPerServing) {
        Recipe recipe = new Recipe();
        recipe.setName("Chicken Soup");
        recipe.setCategory("Soup");
        recipe.setServings(4);
        recipe.setRawWeight(BigDecimal.valueOf(1000));
        recipe.setCookedWeight(BigDecimal.valueOf(800));
        recipe.setCaloriesPer100g(BigDecimal.valueOf(62.5));
        recipe.setProteinPer100g(BigDecimal.valueOf(5));
        recipe.setCarbsPer100g(BigDecimal.valueOf(7.5));
        recipe.setFatsPer100g(BigDecimal.valueOf(2.5));
        recipe.setCaloriesPerServing(caloriesPerServing);
        recipe.setProteinPerServing(BigDecimal.valueOf(10));
        recipe.setCarbsPerServing(BigDecimal.valueOf(15));
        recipe.setFatsPerServing(BigDecimal.valueOf(5));
        return recipeRepository.save(recipe);
    }

    @Test
    void addFoodEntry_withProduct_calculatesAndPersistsCorrectCalories() throws Exception {
        Product egg = saveProduct(155.0, 11.0, 13.0, 1.1);

        FoodEntryRequestDto request = new FoodEntryRequestDto(
                SourceType.PRODUCT, egg.getId(), null, null, null,
                null, null, null, null,
                BigDecimal.valueOf(2), MealType.BREAKFAST, Instant.now()
        );

        String responseJson = mockMvc.perform(post("/api/food-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calories").value(310.0))
                .andExpect(jsonPath("$.protein").value(26.0))
                .andExpect(jsonPath("$.fat").value(22.0))
                .andExpect(jsonPath("$.carbs").value(2.2))
                .andExpect(jsonPath("$.nameSnapshot").value("Egg"))
                .andReturn().getResponse().getContentAsString();

        Long entryId = objectMapper.readTree(responseJson).get("id").asLong();

        FoodEntry persisted = foodEntryRepository.findByIdAndUserId(entryId, USER_ID).orElseThrow();
        assertThat(persisted.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(310.00));
        assertThat(persisted.getProductId()).isEqualTo(egg.getId());
        assertThat(persisted.getRecipeId()).isNull();
        assertThat(persisted.getUserId()).isEqualTo(USER_ID);
        assertThat(persisted.getSourceType()).isEqualTo(SourceType.PRODUCT);
    }

    @Test
    void addFoodEntry_withProduct_fractionalQuantity_roundsCorrectly() throws Exception {
        Product rice = saveProduct(130.0, 0.3, 2.7, 28.0);

        FoodEntryRequestDto request = new FoodEntryRequestDto(
                SourceType.PRODUCT, rice.getId(), null, null, null,
                null, null, null, null,
                BigDecimal.valueOf(0.5), MealType.LUNCH, Instant.now()
        );

        mockMvc.perform(post("/api/food-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calories").value(65.0));
    }

    @Test
    void addFoodEntry_withNonExistentProduct_returns404() throws Exception {
        FoodEntryRequestDto request = new FoodEntryRequestDto(
                SourceType.PRODUCT, 999999L, null, null, null,
                null, null, null, null,
                BigDecimal.ONE, MealType.BREAKFAST, Instant.now()
        );

        mockMvc.perform(post("/api/food-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addFoodEntry_withRecipe_multipliesCaloriesPerServingByQuantity() throws Exception {
        Recipe soup = saveRecipeWithPerServingValues(BigDecimal.valueOf(500));

        FoodEntryRequestDto request = new FoodEntryRequestDto(
                SourceType.RECIPE, null, soup.getId(), null, null,
                null, null, null, null,
                BigDecimal.valueOf(2), MealType.DINNER, Instant.now()
        );

        mockMvc.perform(post("/api/food-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calories").value(1000.0))
                .andExpect(jsonPath("$.nameSnapshot").value("Chicken Soup"));
    }

    @Test
    void addFoodEntry_withCustom_persistsExactProvidedValues() throws Exception {
        FoodEntryRequestDto request = new FoodEntryRequestDto(
                SourceType.CUSTOM, null, null, "Homemade dumplings", null,
                BigDecimal.valueOf(420), BigDecimal.valueOf(15), BigDecimal.valueOf(60), BigDecimal.valueOf(12),
                BigDecimal.ONE, MealType.DINNER, Instant.now()
        );

        mockMvc.perform(post("/api/food-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calories").value(420))
                .andExpect(jsonPath("$.protein").value(15))
                .andExpect(jsonPath("$.nameSnapshot").value("Homemade dumplings"));
    }

    @Test
    void updateFoodEntry_changedQuantity_recalculatesCalories() throws Exception {
        Product banana = saveProduct(105.0, 0.4, 1.3, 27.0);

        FoodEntryRequestDto createRequest = new FoodEntryRequestDto(
                SourceType.PRODUCT, banana.getId(), null, null, null,
                null, null, null, null,
                BigDecimal.valueOf(1), MealType.SNACK, Instant.now()
        );

        String createResponse = mockMvc.perform(post("/api/food-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long entryId = objectMapper.readTree(createResponse).get("id").asLong();

        FoodEntryRequestDto updateRequest = new FoodEntryRequestDto(
                SourceType.PRODUCT, banana.getId(), null, null, null,
                null, null, null, null,
                BigDecimal.valueOf(3), MealType.SNACK, Instant.now()
        );

        mockMvc.perform(patch("/api/food-entries/" + entryId)
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calories").value(315.0));

        FoodEntry persisted = foodEntryRepository.findByIdAndUserId(entryId, USER_ID).orElseThrow();
        assertThat(persisted.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(315.00));
        assertThat(persisted.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(3));
    }

    @Test
    void deleteFoodEntry_removesFromDatabase() throws Exception {
        Product apple = saveProduct(52.0, 0.2, 0.3, 14.0);

        FoodEntryRequestDto request = new FoodEntryRequestDto(
                SourceType.PRODUCT, apple.getId(), null, null, null,
                null, null, null, null,
                BigDecimal.ONE, MealType.SNACK, Instant.now()
        );

        String createResponse = mockMvc.perform(post("/api/food-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long entryId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(delete("/api/food-entries/" + entryId)
                        .with(csrf())
                        .with(asUser()))
                .andExpect(status().isNoContent());

        assertThat(foodEntryRepository.findByIdAndUserId(entryId, USER_ID)).isEmpty();

        mockMvc.perform(get("/api/food-entries/" + entryId).with(asUser()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFoodEntry_belongingToOtherUser_returns404() throws Exception {
        Product product = saveProduct(100.0, 5.0, 5.0, 10.0);

        FoodEntryRequestDto request = new FoodEntryRequestDto(
                SourceType.PRODUCT, product.getId(), null, null, null,
                null, null, null, null,
                BigDecimal.ONE, MealType.LUNCH, Instant.now()
        );

        String createResponse = mockMvc.perform(post("/api/food-entries")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long entryId = objectMapper.readTree(createResponse).get("id").asLong();

        User otherUser = new User();
        otherUser.setId(999L);
        RequestPostProcessor asOtherUser = authentication(
                new UsernamePasswordAuthenticationToken(
                        otherUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        mockMvc.perform(get("/api/food-entries/" + entryId).with(asOtherUser))
                .andExpect(status().isNotFound());
    }
}