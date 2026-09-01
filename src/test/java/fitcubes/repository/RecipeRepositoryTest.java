package fitcubes.repository;

import fitcubes.config.TestcontainersConfiguration;
import fitcubes.model.recipe.Recipe;
import fitcubes.model.recipe.RecipeCategory;
import fitcubes.model.user.ActivityLevel;
import fitcubes.model.user.Gender;
import fitcubes.model.user.Goal;
import fitcubes.model.user.User;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RecipeRepositoryTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;

    private Recipe globalBreakfast;
    private Recipe globalLunch;
    private Recipe user1Breakfast;
    private Recipe user1Dinner;
    private Recipe user2Breakfast;

    @BeforeEach
    void setUp() {
        user1 = createAndPersistUser("user1@example.com");
        user2 = createAndPersistUser("user2@example.com");

        globalBreakfast = createAndPersistRecipe("Global Pancakes", RecipeCategory.BREAKFAST, null);
        globalLunch = createAndPersistRecipe("Global Soup", RecipeCategory.LUNCH, null);

        user1Breakfast = createAndPersistRecipe("User1 Eggs", RecipeCategory.BREAKFAST, user1);
        user1Dinner = createAndPersistRecipe("User1 Steak", RecipeCategory.DINNER, user1);

        user2Breakfast = createAndPersistRecipe("User2 Oats", RecipeCategory.BREAKFAST, user2);

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("findAllGlobalOrByUserIdAndCategory")
    class FindAllGlobalOrByUserIdAndCategory {

        @Test
        @DisplayName("Should return both global and user recipes when category is null")
        void returnsGlobalAndUserRecipes_whenCategoryIsNull() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Recipe> result = recipeRepository.findAllGlobalOrByUserIdAndCategory(
                    user1.getId(),
                    null,
                    pageable
            );

            assertThat(result.getContent())
                    .hasSize(4)
                    .extracting(Recipe::getName)
                    .containsExactlyInAnyOrder(
                            "Global Pancakes",
                            "Global Soup",
                            "User1 Eggs",
                            "User1 Steak"
                    );
            assertThat(result.getContent()).doesNotContain(user2Breakfast);
        }

        @Test
        @DisplayName("Should return global and user recipes filtered by specific category")
        void returnsGlobalAndUserRecipes_filteredByCategory() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Recipe> result = recipeRepository.findAllGlobalOrByUserIdAndCategory(
                    user1.getId(),
                    RecipeCategory.BREAKFAST,
                    pageable
            );

            assertThat(result.getContent())
                    .hasSize(2)
                    .extracting(Recipe::getName)
                    .containsExactlyInAnyOrder("Global Pancakes", "User1 Eggs");
        }

        @Test
        @DisplayName("Should return only global recipes if user has no recipes in given category")
        void returnsOnlyGlobalRecipes_whenUserHasNoneForCategory() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Recipe> result = recipeRepository.findAllGlobalOrByUserIdAndCategory(
                    user1.getId(),
                    RecipeCategory.LUNCH,
                    pageable
            );

            assertThat(result.getContent())
                    .hasSize(1)
                    .extracting(Recipe::getName)
                    .containsExactly("Global Soup");
        }

        @Test
        @DisplayName("Should return empty page when category does not match any global or user recipes")
        void returnsEmptyPage_whenCategoryDoesNotExist() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Recipe> result = recipeRepository.findAllGlobalOrByUserIdAndCategory(
                    user1.getId(),
                    RecipeCategory.DESSERT,
                    pageable
            );

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Should respect pagination parameters")
        void respectsPagination() {
            Pageable pageable = PageRequest.of(0, 2);

            Page<Recipe> result = recipeRepository.findAllGlobalOrByUserIdAndCategory(
                    user1.getId(),
                    null,
                    pageable
            );

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(4);
            assertThat(result.getTotalPages()).isEqualTo(2);
        }
    }

    private User createAndPersistUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setPassword("securePassword123");

        user.setAge(28);
        user.setHeight(180);
        user.setCurrentWeight(80.0);
        user.setTargetWeight(75.0);

        user.setGender(Gender.MALE);
        user.setActivityLevel(ActivityLevel.MODERATELY_ACTIVE);
        user.setGoal(Goal.WEIGHT_LOSS);

        return entityManager.persistAndFlush(user);
    }

    private Recipe createAndPersistRecipe(String name, String category, User user) {
        Recipe recipe = new Recipe();
        recipe.setName(name);
        recipe.setCategory(category);
        recipe.setServings(1);
        recipe.setRawWeight(BigDecimal.valueOf(100.0));
        recipe.setCookedWeight(BigDecimal.valueOf(100.0));
        recipe.setCaloriesPer100g(BigDecimal.valueOf(100.0));
        recipe.setProteinPer100g(BigDecimal.valueOf(10.0));
        recipe.setCarbsPer100g(BigDecimal.valueOf(10.0));
        recipe.setFatsPer100g(BigDecimal.valueOf(2.0));
        recipe.setUser(user);
        return entityManager.persist(recipe);
    }
}