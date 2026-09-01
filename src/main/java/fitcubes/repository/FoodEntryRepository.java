package fitcubes.repository;

import fitcubes.model.foodentry.FoodEntry;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodEntryRepository extends JpaRepository<FoodEntry, Long> {

    Optional<FoodEntry> findByIdAndUserId(Long id, Long userId);

    Page<FoodEntry> findByUserIdAndLoggedAtBetween(
            Long userId, Instant from, Instant to, Pageable pageable
    );

}
