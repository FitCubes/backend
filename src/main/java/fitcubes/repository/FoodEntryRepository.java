package fitcubes.repository;

import fitcubes.model.foodentry.FoodEntry;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodEntryRepository extends JpaRepository<FoodEntry, Long> {

    Optional<FoodEntry> findByIdAndUserId(Long id, Long userId);

    Page<FoodEntry> findByUserIdAndLoggedAtBetween(
            Long userId, Instant from, Instant to, Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(f.calories), 0) FROM FoodEntry f "
            + "WHERE f.userId = :userId AND f.loggedAt BETWEEN :from AND :to")
    BigDecimal sumCaloriesByUserIdAndLoggedAtBetween(@Param("userId") Long userId,
                                                     @Param("from") Instant from,
                                                     @Param("to") Instant to);

}
