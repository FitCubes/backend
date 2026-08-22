package fitcubes.repository;

import fitcubes.model.recipe.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query("""
        SELECT r FROM Recipe r 
        WHERE (r.user.id = :userId OR r.user IS NULL) 
        AND (:category IS NULL OR r.category = :category)
            """)
    Page<Recipe> findAllGlobalOrByUserIdAndCategory(
            @Param("userId") Long userId,
            @Param("category") String category,
            Pageable pageable
    );
}
