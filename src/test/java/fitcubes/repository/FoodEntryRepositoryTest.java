package fitcubes.repository;

import static org.assertj.core.api.Assertions.assertThat;

import fitcubes.model.foodentry.FoodEntry;
import fitcubes.model.foodentry.MealType;
import fitcubes.model.foodentry.SourceType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FoodEntryRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private FoodEntryRepository foodEntryRepository;

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    private FoodEntry buildEntry(Long userId, Instant loggedAt) {
        FoodEntry entry = new FoodEntry();
        entry.setUserId(userId);
        entry.setSourceType(SourceType.CUSTOM);
        entry.setNameSnapshot("Egg");
        entry.setQuantity(BigDecimal.valueOf(2));
        entry.setCalories(BigDecimal.valueOf(156));
        entry.setProtein(BigDecimal.valueOf(12));
        entry.setCarbs(BigDecimal.ZERO);
        entry.setFat(BigDecimal.valueOf(10));
        entry.setMealType(MealType.BREAKFAST);
        entry.setLoggedAt(loggedAt);
        return entry;
    }

    @Test
    void findByIdAndUserId_matchingUser_returnsEntry() {
        FoodEntry saved = foodEntryRepository.save(buildEntry(USER_ID, Instant.now()));

        Optional<FoodEntry> result = foodEntryRepository.findByIdAndUserId(saved.getId(), USER_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getNameSnapshot()).isEqualTo("Egg");
    }

    @Test
    void findByIdAndUserId_differentUser_returnsEmpty() {
        FoodEntry saved = foodEntryRepository.save(buildEntry(USER_ID, Instant.now()));

        Optional<FoodEntry> result = foodEntryRepository.findByIdAndUserId(saved.getId(), OTHER_USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndUserId_nonExistentId_returnsEmpty() {
        Optional<FoodEntry> result = foodEntryRepository.findByIdAndUserId(999999L, USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserIdAndLoggedAtBetween_returnsOnlyEntriesInRange() {
        Instant now = Instant.now();
        Instant inRange = now;
        Instant beforeRange = now.minus(10, ChronoUnit.DAYS);
        Instant afterRange = now.plus(10, ChronoUnit.DAYS);

        foodEntryRepository.save(buildEntry(USER_ID, inRange));
        foodEntryRepository.save(buildEntry(USER_ID, beforeRange));
        foodEntryRepository.save(buildEntry(USER_ID, afterRange));

        Instant from = now.minus(1, ChronoUnit.DAYS);
        Instant to = now.plus(1, ChronoUnit.DAYS);
        Pageable pageable = PageRequest.of(0, 20);

        Page<FoodEntry> result = foodEntryRepository
                .findByUserIdAndLoggedAtBetween(USER_ID, from, to, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLoggedAt()).isEqualTo(inRange);
    }

    @Test
    void findByUserIdAndLoggedAtBetween_differentUser_excludesOtherUsersEntries() {
        Instant now = Instant.now();

        foodEntryRepository.save(buildEntry(USER_ID, now));
        foodEntryRepository.save(buildEntry(OTHER_USER_ID, now));

        Pageable pageable = PageRequest.of(0, 20);
        Page<FoodEntry> result = foodEntryRepository.findByUserIdAndLoggedAtBetween(
                USER_ID, now.minus(1, ChronoUnit.DAYS), now.plus(1, ChronoUnit.DAYS), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void findByUserIdAndLoggedAtBetween_respectsPagination() {
        Instant now = Instant.now();
        for (int i = 0; i < 5; i++) {
            foodEntryRepository.save(buildEntry(USER_ID, now.plus(i, ChronoUnit.MINUTES)));
        }

        Pageable firstPage = PageRequest.of(0, 2);
        Page<FoodEntry> result = foodEntryRepository.findByUserIdAndLoggedAtBetween(
                USER_ID, now.minus(1, ChronoUnit.DAYS), now.plus(1, ChronoUnit.DAYS), firstPage);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    @Test
    void findByUserIdAndLoggedAtBetween_noMatches_returnsEmptyPage() {
        Instant now = Instant.now();
        foodEntryRepository.save(buildEntry(USER_ID, now));

        Pageable pageable = PageRequest.of(0, 20);
        Page<FoodEntry> result = foodEntryRepository.findByUserIdAndLoggedAtBetween(
                USER_ID, now.plus(10, ChronoUnit.DAYS), now.plus(20, ChronoUnit.DAYS), pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}