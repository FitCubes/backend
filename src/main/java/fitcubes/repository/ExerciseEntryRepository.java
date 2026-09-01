package fitcubes.repository;

import fitcubes.model.exerciseentry.ExerciseEntry;
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
public interface ExerciseEntryRepository extends JpaRepository<ExerciseEntry, Long> {

    Optional<ExerciseEntry> findByIdAndUserId(Long id, Long userId);

    Page<ExerciseEntry> findByUserIdAndLoggedAtBetween(
            Long userId, Instant from, Instant to, Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.caloriesBurned), 0) FROM ExerciseEntry e "
            + "WHERE e.userId = :userId AND e.loggedAt BETWEEN :from AND :to")
    BigDecimal sumCaloriesBurnedByUserIdAndLoggedAtBetween(@Param("userId") Long userId,
                                                           @Param("from") Instant from,
                                                           @Param("to") Instant to);
}
