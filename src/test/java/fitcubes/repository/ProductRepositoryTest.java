package fitcubes.repository;

import fitcubes.config.TestcontainersConfiguration;
import fitcubes.model.product.Product;
import fitcubes.model.product.ProductCategory;
import fitcubes.model.user.ActivityLevel;
import fitcubes.model.user.Gender;
import fitcubes.model.user.Goal;
import fitcubes.model.user.User;
import org.junit.jupiter.api.DisplayName;
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
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("findAllGlobalOrByUserId - Should return global products AND user-specific products")
    void findAllGlobalOrByUserId_userHasCustomProducts_returnsGlobalAndUserSpecificProducts() {
        // given
        User user1 = createAndPersistUser("user1@example.com");
        User user2 = createAndPersistUser("user2@example.com");

        Product globalProduct = createProduct("Apple", ProductCategory.FRUITS, null);
        entityManager.persist(globalProduct);

        Product user1Product = createProduct("Chicken Breast", ProductCategory.MEAT_AND_POULTRY, user1);
        entityManager.persist(user1Product);

        Product user2Product = createProduct("Salmon", ProductCategory.FISH_AND_SEAFOOD, user2);
        entityManager.persist(user2Product);

        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> resultPage = productRepository.findAllGlobalOrByUserId(user1.getId(), pageable);

        // then
        assertThat(resultPage.getContent()).hasSize(2);
        assertThat(resultPage.getContent())
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Apple", "Chicken Breast");
    }

    @Test
    @DisplayName("findAllGlobalOrByUserId - Should return ONLY global products if user has no custom products")
    void findAllGlobalOrByUserId_userHasNoCustomProducts_returnsOnlyGlobalProducts() {
        // given
        User user = createAndPersistUser("user@example.com");

        Product globalProduct1 = createProduct("Milk", ProductCategory.DAIRY_AND_CHEESE, null);
        Product globalProduct2 = createProduct("Oats", ProductCategory.GRAINS_AND_CEREALS, null);
        entityManager.persist(globalProduct1);
        entityManager.persist(globalProduct2);

        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> resultPage = productRepository.findAllGlobalOrByUserId(user.getId(), pageable);

        // then
        assertThat(resultPage.getContent()).hasSize(2);
        assertThat(resultPage.getContent())
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Milk", "Oats");
    }

    @Test
    @DisplayName("findAllGlobalOrByUserId - Should properly handle pagination")
    void findAllGlobalOrByUserId_validPageable_returnsPaginatedResults() {
        // given
        User user = createAndPersistUser("user@example.com");

        Product p1 = createProduct("Product 1", ProductCategory.OTHER, null);
        Product p2 = createProduct("Product 2", ProductCategory.OTHER, null);
        Product p3 = createProduct("Product 3", ProductCategory.OTHER, user);

        entityManager.persist(p1);
        entityManager.persist(p2);
        entityManager.persist(p3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 2);

        // when
        Page<Product> resultPage = productRepository.findAllGlobalOrByUserId(user.getId(), pageable);

        // then
        assertThat(resultPage.getContent()).hasSize(2);
        assertThat(resultPage.getTotalElements()).isEqualTo(3);
        assertThat(resultPage.getTotalPages()).isEqualTo(2);
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

    private Product createProduct(String name, ProductCategory category, User user) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setUser(user);
        product.setCalories(100);
        product.setFat(2.0);
        product.setProtein(5.0);
        product.setCarbohydrates(15.0);
        return product;
    }
}