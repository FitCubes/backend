package fitcubes.repository;

import fitcubes.model.weightlog.WeightLog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WeightLogRepository extends JpaRepository<WeightLog, Long> {

    @Query("SELECT w FROM WeightLog w WHERE w.userId = :userId ORDER BY w.loggedAt DESC LIMIT 1")
    Optional<WeightLog> findLatestByUserId(@Param("userId") Long userId);

    @Query("SELECT w FROM WeightLog w WHERE w.userId = :userId ORDER BY w.loggedAt ASC LIMIT 1")
    Optional<WeightLog> findEarliestByUserId(@Param("userId") Long userId);
}
